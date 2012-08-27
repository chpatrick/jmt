/**    
  * Copyright (C) 2009, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.ServiceStrategy;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobClass;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.QueueNet.NodeSection;

/**
 * This class implements a multi-class, single/multi server service.
 * Every class has a specific distribution and a own set of statistical
 * parameters.
 * A server service remains busy while processing one or more jobs.
 * @author  Francesco Radaelli, Stefano Omini, Bertoli Marco
 */
public class Server extends ServiceSection {

	public static final boolean DEBUG = false;

	/** Property Identifier:  Busy counter. */
	public static final int PROPERTY_ID_BUSY_COUNTER = 0x0101;
	/** Property Identifier:  Max jobs (number of servers). */
	public static final int PROPERTY_ID_MAX_JOBS = 0x0102;
	/** Property Identifier:  Visits per class. */
	public static final int PROPERTY_ID_VISITS_PER_CLASS = 0x0103;
	/** Property Identifier:  Service strategy. */
	public static final int PROPERTY_ID_SERVICE_STRATEGY = 0x0104;

	//TODO: use this to correct residence times (R=r*v) or remove them!!
	private int numberOfVisitsPerClass[];

	private int busyCounter, numberOfServers;

	private ServiceStrategy serviceStrategy[];

	private boolean blocked = false;

	/** Creates a new instance of Server.
	 * @param numberOfVisitsPerClass Number of job visits per class: if null
	 * the server will be single visit.
	 * @param serverNumber Number of jobs which can be served simultaneously.
	 * @param serviceStrategy Array of service strategies, one per class.
	 * @throws jmt.common.exception.NetException
	 */
	public Server(Integer serverNumber, Integer numberOfVisitsPerClass[], ServiceStrategy serviceStrategy[]) throws jmt.common.exception.NetException {
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
	public Server(int serverNumber, int numberOfVisitsPerClass[], ServiceStrategy serviceStrategy[]) throws jmt.common.exception.NetException {
		this.serviceStrategy = serviceStrategy;
		this.numberOfVisitsPerClass = numberOfVisitsPerClass;
		busyCounter = 0;
		this.numberOfServers = serverNumber;

	}

	@Override
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

	@Override
	public int getIntSectionProperty(int id, JobClass jobClass) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_VISITS_PER_CLASS:
				return numberOfVisitsPerClass[jobClass.getId()];
			default:
				return super.getIntSectionProperty(id, jobClass);
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.NodeSection#getDoubleSectionProperty(int, jmt.engine.QueueNet.JobClass)
	 */
	@Override
	public double getDoubleSectionProperty(int id, JobClass jobClass) throws NetException {
		if (id == PROPERTY_ID_UTILIZATION) {
			double divisor = numberOfServers;
			return jobsList.getBusyTimePerClass(jobClass) / NetSystem.getTime() / divisor;
		} else {
			return super.getDoubleSectionProperty(id, jobClass);
		}
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.NodeSection#getDoubleSectionProperty(int)
	 */
	@Override
	public double getDoubleSectionProperty(int id) throws NetException {
		if (id == PROPERTY_ID_UTILIZATION) {
			double divisor = numberOfServers;
			return jobsList.getBusyTime() / NetSystem.getTime() / divisor;
		} else {
			return super.getDoubleSectionProperty(id);
		}
	}

	@Override
	public Object getObject(int id, JobClass jobClass) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_SERVICE_STRATEGY:
				return serviceStrategy[jobClass.getId()];
			default:
				return super.getObject(id);
		}
	}

	@Override
	public Object getObject(int id) throws jmt.common.exception.NetException {
		switch (id) {
			case PROPERTY_ID_SERVICE_STRATEGY:
				return serviceStrategy;
			default:
				return super.getObject(id);
		}
	}

	@Override
	protected void nodeLinked(NetNode node) {
		//if (numberOfVisitsPerClass != null)
		//	tempJobsList = new JobInfoList(getJobClasses().size(), true);
		jobsList.setServerNumber(numberOfServers);
	}

	@Override
	protected int process(NetMessage message) throws jmt.common.exception.NetException {
//		if(message.getEvent() == 4 || message.getEvent() == 8) {
//			String debug = "I am the server of " + this.getOwnerNode().name + "\n" +
//						"\t I am processing  " + message.getEvent() + ":" + message.getJob().getId() + "\n" +
//						"\t Am I blocked? " + blocked;
//			System.err.println(debug);
//		}
		int c;
		Job job;
		double serviceTime;
		switch (message.getEvent()) {

			case NetEvent.EVENT_ACK:
				
				if(blocked) {
					blocked = false;
//					System.err.println("\t I am no longer blocked");
					sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
					if (busyCounter != 0) {
						busyCounter--;
					}
					return MSG_PROCESSED;
				}
				//EVENT_ACK
				//If there are no jobs in the service section, message is not processed.
				//Otherwise an ack is sent backward to the input section and
				//the counter of jobs in service is decreased.
				if (busyCounter == 0) {
					//it wasn't waiting for any job
					return NodeSection.MSG_NOT_PROCESSED;
				} else if (busyCounter == numberOfServers) {
					// Sends a request to the input section
					sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
					busyCounter--;
				} else {
					// Avoid ACK as we already sent ack
					busyCounter--;
				}
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

				if (isMine(message)) {
					// this job has been just served (the message has been sent by the server itself)
					// forwards the job to the output section
					sendForward(job, 0.0);
				} else {
					if(blocked) {
//						System.err.println("\t Sorry, I am blocked");
						return MSG_NOT_PROCESSED;
					}
					//message received from another node section: if the server is not completely busy,
					//it sends itself a message with this job
					if (busyCounter < numberOfServers) {
						// Gets the class of the job
						c = job.getJobClass().getId();
						// Auto-sends the job with delay equal to "serviceTime"
						serviceTime = serviceStrategy[c].wait(this);
						// Calculates the service time of job
						sendMe(job, serviceTime);

//						System.err.println("\t From now on I am blocked");
						blocked = true;

						busyCounter++;
						if (busyCounter < numberOfServers) {
							// Sends an ACK to the input section (remember not to propagate
							// this ack again when computation is finished)
							sendBackward(NetEvent.EVENT_ACK, message.getJob(), 0.0);
						}
					} else {
						//server is busy
						return NodeSection.MSG_NOT_PROCESSED;
					}
				}
				break;

			default:
				return MSG_NOT_PROCESSED;
		}
		return MSG_PROCESSED;
	}

}
