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

import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.NodeSection;

/**
 * This abstract class implements a generic service section of a NetNode.
 * @author Francesco Radaelli
 */
public abstract class ServiceSection extends PipeSection {

	/** Creates a new instance of serviceSection
	 */
	public ServiceSection() {
		super(NodeSection.SERVICE);
	}

	/** Creates a new instance of serviceSection
	 *  @param auto  auto refresh of the jobsList attribute.
	 */
	public ServiceSection(boolean auto) {
		super(NodeSection.SERVICE, auto);
	}

	/** Sends a job to the output section.
	 * @param job job to be sent.
	 * @param delay Scheduling delay.
	 * @throws jmt.common.exception.NetException
	 */
	protected void sendForward(Job job, double delay)
	        throws jmt.common.exception.NetException {
		send(job, delay, NodeSection.OUTPUT);
	}

	/** Sends a message to the output section.
	 * @param event Message tag.
	 * @param delay Scheduling delay.
	 * @throws jmt.common.exception.NetException
	 */
	protected void sendForward(int event, Object data, double delay)
	        throws jmt.common.exception.NetException {
		send(event, data, delay, NodeSection.OUTPUT);
	}

	/** Sends a job to the input section.
	 * @param job job to be sent.
	 * @param delay Scheduling delay.
	 * @throws jmt.common.exception.NetException
	 */
	protected void sendBackward(Job job, double delay)
	        throws jmt.common.exception.NetException {
		send(job, delay, NodeSection.INPUT);
	}

	/** Sends a message to the input section.
	 * @param event Message tag.
	 * @param delay Scheduling delay.
	 * @throws jmt.common.exception.NetException
	 */
	protected void sendBackward(int event, Object data, double delay)
	        throws jmt.common.exception.NetException {
		send(event, data, delay, NodeSection.INPUT);
	}
}
