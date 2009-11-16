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
package jmt.engine.NetStrategies.QueueGetStrategies;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.QueueGetStrategy;
import jmt.engine.QueueNet.Job;
import jmt.engine.QueueNet.JobInfoList;

/**
 * <p><b>Name:</b> PSStrategy</p> 
 * <p><b>Description:</b> 
 * Processor sharing strategy. This is a special get strategy as it will make the queue pass jobs directly to
 * the service section without putting them in queue. This is needed because queue and service are tight coupled
 * in a processor sharing implementation.
 * </p>
 * <p><b>Date:</b> 15/nov/2009
 * <b>Time:</b> 22.59.23</p>
 * @author Bertoli Marco
 * @version 1.0
 */
public class PSStrategy implements QueueGetStrategy {

	public Job get(JobInfoList queue) throws NetException {
		return queue.removeFirst().getJob();
	}

	public boolean check() {
		return true;
	}

}
