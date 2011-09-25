/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jmt.engine.NetStrategies.QueuePutStrategies;
import jmt.engine.NetStrategies.QueuePutStrategy;
import jmt.engine.QueueNet.*;

/**
 *
 * @author asus
 */
public class RandStrategy implements QueuePutStrategy {

	/**
	 * all arriving jobs are put at the beginning of the queue.
	 * @param job Job to be added to the queue.
	 * @param queue Queue.
	 * @param sourceSection Job source section.
	 * @param sourceNode Job source node.
	 * @param callingSection The section which calls this strategy.
	 */
	public void put(Job job, JobInfoList queue, byte sourceSection, NetNode sourceNode, NodeSection callingSection) {
		queue.addRand(new JobInfo(job));
	}

	/* (non-Javadoc)
	 * @see jmt.common.AutoCheck#check()
	 */
	public boolean check() {
		return true;
	}
}
