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
  
package jmt.engine.simEngine;

import java.util.Enumeration;

/**
 * <em>THIS CLASS IS NOT USED IN JMT</em>

 * @author Federico Granata
 * @version Date: 28-ott-2003 Time: 10.54.47

 */
public class SuperEventQueue {

	private NewEvQueue future;
	private NewEvQueue now;

	private double clock;

	public SuperEventQueue() {
		future = new NewEvQueue();
		now = new NewEvQueue();
	}

	/**
	 * Returns the number of components in this structure.
	 *
	 * @return  the number of components in this structure.
	 */
	public final synchronized int size() {
		return (now.size() + future.size());
	}

	/**
	 * Add a new event to the queue, preserving the temporal order of the
	 * events in the queue.
	 * @param newEvent	The event to be put on the queue.
	 */
	public final void add(SimEvent newEvent) {
		double evTime = newEvent.eventTime();
		if (evTime > clock)
			future.add(newEvent);
		else if (evTime == clock)
			now.push(newEvent);
		else
			throw new RuntimeException("Impossible to Insert an event");
	}

	/**
	 * Remove and return the event at the top of the queue.
	 * @return           The next event.
	 */
	public final SimEvent pop() {
		if (size() == 0)
			return null;

		if (now.size() != 0) {
			if (now.top().eventTime() == clock)
				return now.pop();
			else
				throw new RuntimeException("Impossible to Pop an event");
		} else {//a bit complicate need to feel now queue with elements with same
			//timestamp
			SimEvent first = future.pop();
			if (clock > first.eventTime())
				throw new RuntimeException("Impossible to Pop an event");
			clock = first.eventTime();//sets new clock
			//while there r elemnts with same time timestamp it puts them in now
			//queue
			while (future.size() > 0 && future.top().eventTime() == clock) {
				now.add(future.pop());
			}
			//return first element
			return first;
		}
	}

	/**
	 * Return the event at the top of the queue, without removing it.
	 * @return	The next event.
	 */
	public final SimEvent top() {
		if (now.size() != 0)
			return now.top();
		else
			return future.top();
	}

	/**
	 * removes the selected element
	 * @param event
	 */
	public void removeElement(SimEvent event) {
		if (size() != 0) {
			now.removeElement(event);
			future.removeElement(event);
		}
	}

	/**
	 * Returns an enumeration of the components of this vector. The
	 * returned <tt>Enumeration</tt> object will generate all items in
	 * this vector. The first item generated is the item at index <tt>0</tt>,
	 * then the item at index <tt>1</tt>, and so on.
	 *
	 * @return  an enumeration of the components of this vector.
	 * @see     java.util.Enumeration
	 * @see     java.util.Iterator
	 */
	public Enumeration elements() {
		return new Enumeration() {
			boolean isFuture = false;
			Enumeration en = now.elements();

			public boolean hasMoreElements() {
				return size() > 0;
			}

			public Object nextElement() {
				synchronized (SuperEventQueue.this) {
					if (en.hasMoreElements())
						return en.nextElement();
					else {
						en = future.elements();
						return en.nextElement();
					}

				}
			}
		};
	}

}
