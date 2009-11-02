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

import jmt.common.exception.NetException;
import jmt.engine.QueueNet.BlockingRegion;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.NetEvent;
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;

/**
 * This class implements a blocking router, i.e. a router which sends a job inside
 * a region with constraints (the "blocking region"). The destination node has been
 * already defined (the node each job has been redirected from), therefore no
 * routing strategy is required.
 * @author  Stefano Omini
 */
public class BlockingRouter extends OutputSection {

	/** Property Identifier: Routing strategy.*/
	public static final int PROPERTY_ID_ROUTING_STRATEGY = 0x0101;
	/** Property Identifier: Busy.*/
	public static final int PROPERTY_ID_BUSY = 0x0102;

	//if true, router is waiting for an ack of a job previously sent
	private boolean busy;

	//TODO: dopo le modifiche non serve più
	//the blocking region
	private BlockingRegion blockingRegion;

	//to speed up performance
	Job job = null;
	NetNode realDestinationNode = null;

	/** Creates a new instance of Router which limitates the number of
	 * jobs entering in the region with constraints.
	 * No routing strategy is required: the destination node is already known
	 * when the job arrives to the input station.
	 * @param blockingReg The blocking Region whose access is controlled by this blocking router
	 */
	public BlockingRouter(BlockingRegion blockingReg) {
		//FCR bug fix: Constructor modified from super() to spuer(true,false) which will enable
		//Jobs in Joblist at NodeSection to be automatically dropped where as for Node to be manually handled.
		super(true,false);
		//TODO: non aggiorna joblist
		//super(false);
		busy = false;
		this.blockingRegion = blockingReg;

		//NEW
		//@author Stefano Omini
		//log = NetSystem.getLog();
		//end NEW

	}

	protected void nodeLinked(NetNode node) {
		blockingRegion.setInputStation(node);
		return;
	}

	public boolean isEnabled(int id) throws NetException {
		switch (id) {
			case PROPERTY_ID_BUSY:
				return busy;
			default:
				return super.isEnabled(id);
		}
	}

	protected int process(NetMessage message) throws NetException {
		switch (message.getEvent()) {

			case NetEvent.EVENT_JOB:

				//EVENT_JOB
				//if the router is not busy, an ouput node is chosen using
				//the routing strategy and a message containing the job is sent to it.
				//The router becomes busy, waiting for the ack.
				//
				//Otherwise if the router is busy, message is not processed

				// if not busy sends the job
				if (!busy) {

					//OLD
					/*
					Job job = message.getJob();

					NetNode realDestinationNode = null;

					if (job.isRedirected()) {
					    //this is the real destination, i.e. the internal node that at first
					    //had redirected the job to the input station
					    realDestinationNode = job.getDestinationNode();
					} else {
					    //This is not a redirected job: we don't know the destination!!!
					    return MSG_NOT_PROCESSED;
					}
					*/

					//NEW
					job = message.getJob();
					//this is the real destination, i.e. the internal node that at first
					//had redirected the job to the input station
					realDestinationNode = job.getOriginalDestinationNode();
					send(job, 0.0, realDestinationNode);

					busy = true;
					//log.write(NetLog.LEVEL_DEBUG, job, this, NetLog.JOB_OUT);

					return MSG_PROCESSED;
				}
				// otherwise the job will be lost
				else {
					return MSG_NOT_PROCESSED;
				}

			case NetEvent.EVENT_ACK:

				//EVENT_ACK
				//
				//If the router is not busy (is not waiting for the ack for a routed job)
				//message is no processed.
				//Otherwise an ack is sent back to the service section and busy becomes false.
				//

				if (!busy) {
					return MSG_NOT_PROCESSED;
				} else {
					//this ack has been received from one of the output nodes (router was waiting
					//for this ack)
					//sends an ack back to the service section to request another job to be routed
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
