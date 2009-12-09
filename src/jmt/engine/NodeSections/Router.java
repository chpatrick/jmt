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
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NodeSection;

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
 * @author Bertoli Marco - Fixed bug with multiserver stations
 * 
 * Modified by Ashanka (Oct 2009) for FCR Bug fix: Events are created with job instead of null for EVENT_JOB_OUT_OF_REGION
 */
public class Router extends OutputSection {

	/** Property Identifier: Routing strategy.*/
	public static final int PROPERTY_ID_ROUTING_STRATEGY = 0x0101;
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

	/** Creates a new instance of Router.
	 * @param routingStrategies Routing strategies, one for each class.
	 */
	public Router(RoutingStrategy routingStrategies[]) {
		super();
		this.routingStrategies = routingStrategies;

		borderRouter = false;
		myBlockingRegion = null;
		regionInputStation = null;
	}

	/** Creates a new instance of blocking region border Router.
	 * @param routingStrategies Routing strategies, one for each class.
	 */
	public Router(RoutingStrategy routingStrategies[], BlockingRegion blockReg) {
		super();
		this.routingStrategies = routingStrategies;

		borderRouter = true;
		myBlockingRegion = blockReg;
		regionInputStation = myBlockingRegion.getInputStation();
	}

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

	@Override
	public Object getObject(int id, JobClass jobClass) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_ROUTING_STRATEGY:
				return routingStrategies[jobClass.getId()];
			default:
				return super.getObject(id);
		}
	}

	@Override
	protected int process(NetMessage message) throws jmt.common.exception.NetException {

		switch (message.getEvent()) {

			case NetEvent.EVENT_JOB:

				Job job = message.getJob();

				//EVENT_JOB
				//if the router is not busy, an output node is chosen using
				//the routing strategy and a message containing the job is sent to it.

				JobClass jobClass = job.getJobClass();

				//choose the outNode using the corresponding routing strategy
				NetNode outNode;

				outNode = routingStrategies[jobClass.getId()].getOutNode(getOwnerNode().getOutputNodes(), jobClass);

				// Bertoli Marco: sanity checks with closed classes and sinks were moved inside
				// routing strategies

				if (outNode == null) {
					return MSG_NOT_PROCESSED;
				}

				//send the job to all nodes identified by the strategy
				send(job, 0.0, outNode);

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

						//send(NetEvent.EVENT_JOB_OUT_OF_REGION, null, 0.0, NodeSection.INPUT, regionInputStation);
						send(NetEvent.EVENT_JOB_OUT_OF_REGION, job, 0.0, NodeSection.INPUT, regionInputStation);
						//Since now for blocking regions the job dropping is handles manually at node level 
						//hence need to create events with Jobs ..Modified for FCR Bug Fix
					}
				}
				return MSG_PROCESSED;

			case NetEvent.EVENT_ACK:
				//EVENT_ACK
				//
				//An ack is sent back to the service section.
				//

				sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
				break;

			default:
				return MSG_NOT_PROCESSED;
		}
		return MSG_PROCESSED;
	}
}
