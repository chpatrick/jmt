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

import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.QueueNet.*;

/**
 * This class implements a multi-class, single/multi server service.
 * Every class has a specific distribution and a own set of statistical
 * parameters.
 * A server service remains busy while processing one or more jobs.
 * @author  Francesco Radaelli, Stefano Omini
 */
public class Server extends ServiceSection {

    public static final boolean DEBUG = false;

	/** Property Identifier:  Busy counter. */
	public static final int PROPERTY_ID_BUSY_COUNTER = 0x0101;
	/** Property Identifier:  Max jobs. */
	public static final int PROPERTY_ID_MAX_JOBS = 0x0102;
	/** Property Identifier:  Visits per class. */
	public static final int PROPERTY_ID_VISITS_PER_CLASS = 0x0103;
	/** Property Identifier:  Service strategy. */
	public static final int PROPERTY_ID_SERVICE_STRATEGY = 0x0104;


    //TODO: o si usano per correggere i response time (R=r*v) oppure toglierle!!
	private int numberOfVisitsPerClass[];

	private int busyCounter, numberOfServers;

	//private JobInfoList tempJobsList;

	private ServiceStrategy serviceStrategy[];



	/** Creates a new instance of Server.
	 * @param numberOfVisitsPerClass Number of job visits per class: if null
	 * the server will be single visit.
	 * @param serverNumber Number of jobs which can be served simultaneously.
	 * @param serviceStrategy Array of service strategies, one per class.
	 * @throws jmt.common.exception.NetException
	 */
	public Server(Integer serverNumber, Integer numberOfVisitsPerClass[],
	              ServiceStrategy serviceStrategy[]) throws jmt.common.exception.NetException {
		//numberOfVisitsPerClass is null
		if (numberOfVisitsPerClass == null) {
			this.serviceStrategy = serviceStrategy;
			this.numberOfVisitsPerClass = null;
			busyCounter = 0;
			this.numberOfServers = serverNumber.intValue();
		} else {
			//else creates an array of int & then use the same cosntructor
			int[] nVisits = new int[numberOfVisitsPerClass.length];
			for (int i = 0; i < numberOfVisitsPerClass.length; i++) {
				nVisits[i] = numberOfVisitsPerClass[i].intValue();

			}
			this.serviceStrategy = serviceStrategy;
			this.numberOfVisitsPerClass = nVisits;
			busyCounter = 0;
			this.numberOfServers = serverNumber.intValue();
		}


	}

	/** Creates a new instance of Server.
	 * @param serverNumber Number of jobs which can be served simultaneously.
     * @param numberOfVisitsPerClass Number of job visits per class: if null
	 * the server will be single visit.
	 * @param serviceStrategy Array of service strategies, one per class.
	 * @throws jmt.common.exception.NetException
	 */
	public Server(int serverNumber, int numberOfVisitsPerClass[],
	              ServiceStrategy serviceStrategy[]) throws jmt.common.exception.NetException {
		this.serviceStrategy = serviceStrategy;
		this.numberOfVisitsPerClass = numberOfVisitsPerClass;
		busyCounter = 0;
		this.numberOfServers = serverNumber;


	}


	public int getIntSectionProperty(int id) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_BUSY_COUNTER:
				return busyCounter;
			case PROPERTY_ID_MAX_JOBS:
				return numberOfServers;
			default:
				return super.getIntSectionProperty(id);
		}
	}

	public int getIntSectionProperty(int id, JobClass jobClass) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_VISITS_PER_CLASS:
				return numberOfVisitsPerClass[jobClass.getId()];
			default:
				return super.getIntSectionProperty(id, jobClass);
		}
	}

	public Object getObject(int id, JobClass jobClass) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_SERVICE_STRATEGY:
				return serviceStrategy[jobClass.getId()];
			default:
				return super.getObject(id);
		}
	}


    public Object getObject(int id) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_SERVICE_STRATEGY:
				return serviceStrategy;
			default:
				return super.getObject(id);
		}
	}

	protected void nodeLinked(NetNode node) {
		//if (numberOfVisitsPerClass != null)
		//	tempJobsList = new JobInfoList(getJobClasses().size(), true);
	}

    /*
    protected int process(NetMessage message) throws jmt.common.exception.NetException {
		int c;
		Job job;
		double serviceTime;
		switch (message.getEvent()) {

            case NetEvent.EVENT_ACK:

                //EVENT_ACK
                //If there are no jobs in the service section, message is not processed.
                //Otherwise an ack is sent backward to the input section and
                //the counter of jobs in service is decreased.

				if (busyCounter == 0) {
					//it wasn't waiting for any job
                    return NodeSection.MSG_NOT_PROCESSED;
                }

                // Sends a request to the input section
				sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);

                //log.write(NetLog.LEVEL_ALL, message.getJob(), this, NetLog.ACK_JOB);

                busyCounter--;
				break;

            case NetEvent.EVENT_JOB:

                //EVENT_JOB
                //If the message has been sent by the server itself and if the
                //job is single visit, then the job is forwarded. Otherwise, if
                //the job is multi visit the number of visits done and requested
                //is checked through a JobVisitsInfo object (created during the
                //first visit). If all visits have been completed, the job is
                //forwarded, otherwise it's sent backward to input section
                //for further visits.
                //
                //If the message has been sent by another section, the server, if
                //is not completely busy, sends to itself a message containing the
                //job and with delay equal to the service time calculated using
                //the service strategy.
                //The counter of jobs in service is increased and, if further service
                //capacity is left, an ack is sent to the input section.

				// Gets the job from the message
				job = message.getJob();


                //----ZERO SERVICE TIME: properties setup-----//
                //NEW
                //@author Stefano Omini

                //first of all check if this job must be tunnelled (because the
                //corresponding jobClass has a zero service time distribution)
                if (job.TunnelThisJob()) {
                    //the job must be immediately forwarded to output section

                    job.setTunnelThisJob(false);
                    sendForward(job, 0.0);

                    //log.write(NetLog.LEVEL_DEBUG, job, this, NetLog.JOB_OUT);

                    return MSG_PROCESSED;

                    //the job forwarded is not a "normal" job, but a job whose class has a
                    //zero service time strategy
                    //in this case do not send any ack backward, otherwise other jobs
                    //would be sent by the queue (even if the server is already busy!!)

                    //WARNING: the service section won't receive from
                    //the output section any ack for the tunnelled job
                }

                //end NEW

                //----end ZERO SERVICE TIME properties setup-----//


				if (isMine(message)) {
                    // this job has been just served (the message has been sent by the server itself)
                    if (numberOfVisitsPerClass == null) {
                        // If the served job was not multi-visit forwards the job
						sendForward(job, 0.0);

                        //log.write(NetLog.LEVEL_DEBUG, job, this, NetLog.JOB_OUT);
					} else {

                        //multivisits job

						// Gets job class
						c = job.getJobClass().getId();

                        //find the JobVisitsInfo object related to this job
						JobVisitsInfo jobInfo = (JobVisitsInfo) tempJobsList.lookFor(job);

						if (jobInfo.getNumberOfVisits() < numberOfVisitsPerClass[c]) {
                             // If the number of visits of the job is less than
						    // the number of visists required sends the job back
						    // to the input section

                            //NEW
                            //@author Stefano Omini

                            //in the multivisit case, residence time should be updated for each visit
                            //and not only for the last one.
                            //the time property of JobInfo object shouldn't be reset (it will be reset
                            //while arriving at the queue for the next visit)

                            //this is not the last visit, an update residence time should be done!! then resets time
                            //the arrive time to be used is the one contained in the list of the node
                            //and not the one contained in this service section

                            //jobInfoList of the owner node
                            JobInfoList node_jobInfoList = this.getOwnerNode().getJobInfoList();
                            //find the jobInfo corresponding to this job
                            JobInfo node_jobInfo = node_jobInfoList.lookFor(job);

                            //update residence time measure
                            node_jobInfoList.updateResidenceTime_visits(node_jobInfo);
                            //reset arrive time in the node (the jobInfo arrive time is set only when the job is
                            //added for the first time to the node)
                            node_jobInfo.resetArriveTime();


                            //end NEW

                            //send job back to the queue
							sendBackward(job, 0.0);

						} else {
                            //all visits completed
                            tempJobsList.remove(jobInfo);
							sendForward(job, 0.0);

                            //log.write(NetLog.LEVEL_DEBUG, job, this, NetLog.JOB_OUT);
						}

					}


                } else {
                    //message received from another node section: if the server is not completely busy,
                    //it sends itself a message with this job
					if (busyCounter < numberOfServers) {

                        //NEW
                        //@author Stefano Omini
                        if (numberOfVisitsPerClass != null){
                            JobVisitsInfo jobInfo = (JobVisitsInfo) tempJobsList.lookFor(job);
                            if (jobInfo == null) {
                                //it's the first visit
                                jobInfo = new JobVisitsInfo(job, 0);
                                tempJobsList.add(jobInfo);
                            }
                            //increments number of visits
                            jobInfo.addVisit();
                        }
                        //end NEW

                        // Gets the class of the job
						c = job.getJobClass().getId();

                        // Auto-sends the job with delay equal to "serviceTime"
                        serviceTime = serviceStrategy[c].wait(this);
                        // Calculates the service time of job
                        sendMe(job, serviceTime);


                        busyCounter++;
						if (busyCounter < numberOfServers) {
							// Sends a request to the input section
							sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);

                            //log.write(NetLog.LEVEL_ALL, message.getJob(), this, NetLog.ACK_JOB);
						}
					} else
                        //server is busy
						return NodeSection.MSG_NOT_PROCESSED;
				}
				break;

            default:
				return MSG_NOT_PROCESSED;
		}
		return MSG_PROCESSED;
	}
    */



    protected int process(NetMessage message) throws jmt.common.exception.NetException {
		int c;
		Job job;
		double serviceTime;
		switch (message.getEvent()) {

            case NetEvent.EVENT_ACK:

                //EVENT_ACK
                //If there are no jobs in the service section, message is not processed.
                //Otherwise an ack is sent backward to the input section and
                //the counter of jobs in service is decreased.

				if (busyCounter == 0) {
					//it wasn't waiting for any job
                    return NodeSection.MSG_NOT_PROCESSED;
                }

                // Sends a request to the input section
				sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);

                //log.write(NetLog.LEVEL_ALL, message.getJob(), this, NetLog.ACK_JOB);

                busyCounter--;
				break;

            case NetEvent.EVENT_JOB:

                //EVENT_JOB
                //If the message has been sent by the server itself,
                // then the job is forwarded.
                //
                //If the message has been sent by another section, the server, if
                //is not completely busy, sends to itself a message containing the
                //job and with delay equal to the service time calculated using
                //the service strategy.
                //The counter of jobs in service is increased and, if further service
                //capacity is left, an ack is sent to the input section.

				// Gets the job from the message
				job = message.getJob();


                //----ZERO SERVICE TIME: properties setup-----//
                //NEW
                //@author Stefano Omini

                //first of all check if this job must be tunnelled (because the
                //corresponding jobClass has a zero service time distribution)
                if (job.TunnelThisJob()) {
                    //the job must be immediately forwarded to output section

                    job.setTunnelThisJob(false);
                    sendForward(job, 0.0);

                    return MSG_PROCESSED;

                    //the job forwarded is not a "normal" job, but a job whose class has a
                    //zero service time strategy
                    //in this case do not send any ack backward, otherwise other jobs
                    //would be sent by the queue (even if the server is already busy!!)

                    //WARNING: the service section won't receive from
                    //the output section any ack for the tunnelled job
                }

                //end NEW

                //----end ZERO SERVICE TIME properties setup-----//


				if (isMine(message)) {
                    // this job has been just served (the message has been sent by the server itself)
                    // forwards the job to the output section
				    sendForward(job, 0.0);
                } else {
                    //message received from another node section: if the server is not completely busy,
                    //it sends itself a message with this job
					if (busyCounter < numberOfServers) {
                        // Gets the class of the job
						c = job.getJobClass().getId();
                        // Auto-sends the job with delay equal to "serviceTime"
                        serviceTime = serviceStrategy[c].wait(this);
                        // Calculates the service time of job
                        sendMe(job, serviceTime);

                        busyCounter++;
						if (busyCounter < numberOfServers) {
							// Sends a request to the input section
							sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
                        }
					} else
                        //server is busy
						return NodeSection.MSG_NOT_PROCESSED;
				}
				break;

            default:
				return MSG_NOT_PROCESSED;
		}
		return MSG_PROCESSED;
	}

}
