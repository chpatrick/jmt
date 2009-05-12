/* EventQueue.java
 */

package jmt.engine.simEngine;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class implements an event queue used internally by the Sim_system to
 * manage
 * the list of future and deferred Sim_events. It should not be needed in
 * a user simulation. It works like a normal FIFO
 * queue, but during insertion events are kept in order from the smallest time
 * stamp to the largest. This means the next event to occur will be at the top
 * of the queue. <P>
 * The current implementation
 * is uses a Vector to store the queue and is inefficient for popping
 * and inserting elements because the rest of the array has to be
 * moved down one space. A better method would be to use a circular array.
 */

public class ListEventQueue extends ArrayList implements EventQueue {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Constructors
	/**
	 * Allocates a new EventQueue object.
	 */
	public ListEventQueue() {
		super();
	}

	/**
	 * Allocates a new EventQueue object, with an initial capacity.
	 * @param initialCapacity	The initial capacity of the queue.
	 */
	public ListEventQueue(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Remove and return the event at the top of the queue.
	 * @return           The next event.
	 */
	public SimEvent pop() {
		return (SimEvent) this.remove(0);
	}

	/**
	 * Return the event at the top of the queue, without removing it.
	 * @return	The next event.
	 */
	public SimEvent top() {
		return (SimEvent) this.get(0);
	}

	/**
	 * Add a new event to the queue, preserving the temporal order of the
	 * events in the queue.
	 * @param new_event	The event to be put on the queue.
	 */
	public void add(SimEvent new_event) {
		int i = -1;
		for (Iterator it = this.iterator(); it.hasNext() && (i == -1);) {
			SimEvent event = (SimEvent) it.next();
			if (event.eventTime() > new_event.eventTime()) {
				i = indexOf(event);
			}
		}

		if (i == -1) {
			super.add(new_event);
		} else {
			super.add(i, new_event);
		}
	}

	public SimEvent peek() {
		return top();
	}

	public boolean remove(SimEvent ev) {
		return super.remove(ev);
	}	
}
