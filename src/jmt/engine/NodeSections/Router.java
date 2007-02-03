/**    
  * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.

  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
  
package jmt.engine.NodeSections;

import jmt.engine.NetStrategies.RoutingStrategy;
import jmt.engine.NetStrategies.ServiceStrategies.ZeroServiceTimeStrategy;
import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.QueueNet.*;

/**
 * This class implements a router, which routes the jobs according to the specified
 * routing strategies (one for each job class).
 * <br><br>
 * The class has different constructors to create a generic router or a blocking
 * region border queue, that is the router of a node which is inside a blocking
 * region and which sends jobs also to nodes outside the region.
 * When a job leaves the blocking region, the region input station must receive
 * a message, in order to serve the blocked jobs.
 * <br>
 * However it's also possible to create a generic router and then to turn on/off the
 * "border router" behaviour using the <tt>borderRouterTurnON(..)</tt> and
 * <tt>borderRouterTurnOFF()</tt> methods.
 *
 * @author Francesco Radaelli, Stefano Omini
 * @author Bertoli Marco - Fixed lockup issues with closed class and sinks 13/11/2005
 */
public class Router extends OutputSection {

	/** Property Identifier: Routing strategy.*/
	public static final int PROPERTY_ID_ROUTING_STRATEGY = 0x0101;
	/** Property Identifier: Busy.*/
	public static final int PROPERTY_ID_BUSY = 0x0102;

    //busy is true when a router has just sent a job and is waiting for an ack
    private boolean busy;

	private RoutingStrategy routingStrategies[];


    /*----------------BLOCKING REGION PROPERTIES---------------------*/
    //@author Stefano Omini

    /*
    these properties are used if this router is the border router of
    a blocking region (i.e. it is connected also to nodes outside the
    blocking region)
    in fact, if the router sends a job outside the region, a message
    must be sent to the input station of that region, to decrease the
    number of jobs inside the region
    */

    /**true if this router is the border router of a blocking region*/
    private boolean borderRouter;
    /** the blocking region this router belongs to */
    private BlockingRegion myBlockingRegion;
    /** the region input station of the blocking region */
    private NetNode regionInputStation;

    /*---------------------------------------------------------------*/



    //-------------------ZERO SERVICE TIME PROPERTIES------------------------------//
    //@author Stefano Omini

    //for each class, true if that class has a service time equal to zero and therefore
    //must be "tunnelled"
    private boolean hasZeroServiceTime[] = null;

    //-------------------end ZERO SERVICE TIME PROPERTIES--------------------------//



	/** Creates a new instance of Router.
	 * @param routingStrategies Routing strategies, one for each class.
	 */
	public Router(RoutingStrategy routingStrategies[]) {
		super();
		busy = false;
		this.routingStrategies = routingStrategies;

        //NEW
        //@author Stefano Omini
        borderRouter = false;
        myBlockingRegion = null;
        regionInputStation = null;
        //end NEW

        //NEW
        //@author Stefano Omini
        //log = NetSystem.getLog();
        //end NEW
	}


    //NEW
    //@author Stefano Omini

    /** Creates a new instance of blocking region border Router.
	 * @param routingStrategies Routing strategies, one for each class.
	 */
	public Router(RoutingStrategy routingStrategies[], BlockingRegion blockReg) {
		super();
		busy = false;
		this.routingStrategies = routingStrategies;

        borderRouter = true;
        myBlockingRegion = blockReg;
        regionInputStation = myBlockingRegion.getInputStation();

        //NEW
        //@author Stefano Omini
        //log = NetSystem.getLog();
        //end NEW
	}

    //end NEW


    //NEW
    //@author Stefano Omini
    /**
     * Tells whether this router is a border router of a blocking region.
     * @return true if this router is a border router of a blocking region.
     */
    public boolean isBorderRouter() {
        return borderRouter;
    }


    /**
     * Turns on the "borderRouter" behaviour.
     * @param region the blocking region to which the owner node
     * of this router belongs
     */
    public void borderRouterTurnON(BlockingRegion region) {
        //sets blocking region properties
        borderRouter = true;
        myBlockingRegion = region;
        regionInputStation = myBlockingRegion.getInputStation();
        return;
    }

    /**
     * Turns off the "borderRouter" behaviour.
     */
    public void borderRouterTurnOFF() {
        //sets blocking region properties
        borderRouter = false;
        myBlockingRegion = null;
        regionInputStation = null;
        return;
    }

    //end NEW


    public Object getObject(int id, JobClass jobClass) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_ROUTING_STRATEGY:
				return routingStrategies[jobClass.getId()];
			default:
				return super.getObject(id);
		}
	}

	public boolean isEnabled(int id) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_BUSY:
				return busy;
			default:
				return super.isEnabled(id);
		}
	}

	protected int process(NetMessage message) throws jmt.common.exception.NetException {

        switch (message.getEvent()) {

            case NetEvent.EVENT_JOB:

                Job job = message.getJob();

                //NEW
                //@author Stefano Omini

                //TODO: questa parte eventualmente potrebbe essere messa nella version overridden di NodeLinked
                //at the first execution checks which classes have service time always equal to zero
                if (hasZeroServiceTime == null) {
                    int numberOfClasses = this.getJobClasses().size();
                    hasZeroServiceTime = new boolean[numberOfClasses];

                    NodeSection serviceSect = this.getOwnerNode().getSection(NodeSection.SERVICE);
                    if (serviceSect instanceof ServiceTunnel) {
                        //case #1: service tunnel
                        //nothing to control, every job has service time zero
                        //it's useless to force the tunnel behaviour
                        for (int c = 0; c < numberOfClasses; c++) {
                            hasZeroServiceTime[c] = false;
                        }
                    } else {
                        //case #2: server
                        //check if there are classes with zero service time distribution
                        if (serviceSect instanceof Server) {
                            for (int c = 0; c < this.getJobClasses().size(); c++) {
                                ServiceStrategy[] servStrat = (ServiceStrategy[]) ((Server) serviceSect).getObject(Server.PROPERTY_ID_SERVICE_STRATEGY);

                                if (servStrat[c] instanceof ZeroServiceTimeStrategy) {
                                    hasZeroServiceTime[c] = true;
                                } else {
                                    hasZeroServiceTime[c] = false;
                                }
                            }
                        } else {
                            //case #3: serviceSect is nor a service tunnel neither a server
                            //use default behaviour
                            for (int c = 0; c < this.getJobClasses().size(); c++) {
                                hasZeroServiceTime[c] = false;
                            }
                        }
                    }
                }

                //checks if this job has been tunnelled
                //tunnelled jobs can be forwarded even if router is already busy
                //no acks must be sent backward for tunnelled jobs
                boolean tunnelledJob = hasZeroServiceTime[job.getJobClass().getId()];

                //end NEW


                //EVENT_JOB
                //if the router is not busy, an output node is chosen using
                //the routing strategy and a message containing the job is sent to it.
                //The router becomes busy, waiting for the ack.
                //
                //Otherwise if the router is busy, message is not processed

				// if not busy sends the job

                //OLD
                //if (!busy) {

                //NEW
                //@author Stefano Omini
                if (!busy || tunnelledJob) {
                //end NEW

                    JobClass jobClass = job.getJobClass();

                    //choose the outNode using the corresponding routing strategy
                    NetNode outNode;

                    outNode = routingStrategies[jobClass.getId()]
                            .getOutNode(getOwnerNode().getOutputNodes(), jobClass);

                    // Bertoli Marco: sanity checks with closed classes and sinks were moved inside
                    // routing strategies

                    if (outNode == null)
						return MSG_NOT_PROCESSED;


                    //send the job to all nodes identified by the strategy
					send(job, 0.0, outNode);

                    //busy becomes true (if job isn't tunnelled:
                    //the router is waiting for the ack)

                    //OLD
                    //busy = true;

                    //NEW
                    //@author Stefano Omini
                    if (!tunnelledJob) {
                        //busy = true only for non-tunnelled jobs
                        busy = true;
                    }
                    //end NEW


					//log.write(NetLog.LEVEL_DEBUG, job, this, NetLog.JOB_OUT);


                    //NEW
                    //@author Stefano Omini

                    //Border router behaviour (used in case of blocking region)
                    if (isBorderRouter()) {
                        //the owner node of this router is inside the region: if the outNode is outside
                        //the region, it means that one job has left the blocking region so the region
                        //input station (its blocking router) must receive a particular message
                        if (!myBlockingRegion.belongsToRegion(outNode)) {

                            //the first time finds the input station
                            if (regionInputStation == null) {
                                regionInputStation = myBlockingRegion.getInputStation();
                            }

                            myBlockingRegion.decreaseOccupation(jobClass);

                            send(NetEvent.EVENT_JOB_OUT_OF_REGION, null, 0.0, NodeSection.INPUT, regionInputStation);
                        }
                    }
                    //end NEW

                    return MSG_PROCESSED;
				}
				// otherwise the job will be lost
				else
					//TODO: attenzione: se ho a valle una coda finita, perde i messaggi
                    //arrivano altri job mentre il router è ancora busy, quindi non processa il messaggio
                    //non si potrebbe bloccare l'invio di altri job??
                    return MSG_NOT_PROCESSED;


            case NetEvent.EVENT_ACK:

                //EVENT_ACK
                //
                //If the router is not busy (is not waiting for the ack for a routed job)
                //message is no processed.
                //Otherwise an ack is sent back to the service section and busy becomes false.
                //

                //NEW
                //@author Stefano Omini

                //first controls if the ack is relative to a tunnelled job
                int jobClassIndex = message.getJob().getJobClass().getId();

                if (hasZeroServiceTime[jobClassIndex]) {
                    //ok, this job had been tunnelled
                    //no acks must be sent backward for tunnelled jobs
                    return MSG_PROCESSED;
                }

                //end NEW

				if (!busy)
					return MSG_NOT_PROCESSED;
				else {
					sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);

                    //log.write(NetLog.LEVEL_ALL, message.getJob(), this, NetLog.ACK_JOB);

                    busy = false;
				}
				break;

            default:
				return MSG_NOT_PROCESSED;
		}
		return MSG_PROCESSED;
	}
}
