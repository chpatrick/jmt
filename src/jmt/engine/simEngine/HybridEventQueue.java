package jmt.engine.simEngine;

import java.util.Comparator;
import java.util.Iterator;

import jmt.framework.data.CircularList;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.PriorityBuffer;

/**
 * <p><b>Name:</b> HybridEventQueue</p> 
 * <p><b>Description:</b> 
 * An hybrid implementation for the EventQueue interface. Future events are inserted and retrieved in a sorted
 * binary heap with log(n) performances, while events for the current time interval are inserted and retrieved
 * using an unbounded circular list. 
 * </p>
 * <p><b>Date:</b> 12/mag/2009
 * <b>Time:</b> 18:40:26</p>
 * @author Bertoli Marco
 * @version 1.0
 */
public class HybridEventQueue implements EventQueue{
	private int DEFAULT_INITIAL_CAPACITY = 111;
	/** Current events */
	private CircularList current;
	/** Future events*/
	private Buffer future;
	/** Current time. All events in current event queue will have this time. */
	private double currentTime;
	/** A counter used to order future events basing on event time and insertion order */
	private int order;
	
	public HybridEventQueue() {
		clear();
	}
	
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#add(jmt.engine.simEngine.SimEvent)
	 */
	public void add(SimEvent event) {
		double eventTime = event.eventTime();
		if (eventTime == currentTime) {
			current.add(event);
		} else if (eventTime > currentTime) {
			addToFuture(event);
		} else {
			// Probably this condition will never happen but we are robust to it. We leaqve to the simulation
			// engine the choice to deal with past events.
			moveCurrentToFuture();
			currentTime = eventTime;
			current.add(event);
		}
	}
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#clear()
	 */
	public void clear() {
		current = new CircularList();
		future = new PriorityBuffer(DEFAULT_INITIAL_CAPACITY, new SimEventComparator());
		currentTime = 0.0;
		order = Integer.MIN_VALUE;
	}
	
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#iterator()
	 */
	public Iterator iterator() {
		return new Iter();
	}
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#peek()
	 */
	public SimEvent peek() {
		handleCurrent();
		if (current.size() > 0) {
			return (SimEvent) current.getFirst();
		} else {
			return null;
		}
	}
	
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#pop()
	 */
	public SimEvent pop() {
		handleCurrent();
		if (current.size() > 0) {
			return (SimEvent) current.removeFirst();
		} else {
			return null;
		}
	}
	
	/**
	 * This method will move events from future to current queue if it's empty.
	 */
	private void handleCurrent() {
		if (current.size() == 0 && future.size() > 0) {
			SimEvent first = (SimEvent) future.remove();
			currentTime = first.eventTime();
			current.add(first);
			while (future.size() > 0 && ((SimEvent)future.get()).eventTime() == currentTime) {
				current.add(future.remove());
			}
		}
	}
	
	/**
	 * This method will move the entire current queue to future. This is needed when an event 
	 * older than current one is queued. Probably will never happen because the simulator currently 
	 * avoid this.
	 */
	private void moveCurrentToFuture() {
		while (current.size() > 0) {
			addToFuture((SimEvent)current.removeFirst());
		}
	}
	
	/**
	 * This method will build a new future buffer with new order indices. As the total number of indices is
	 * 2^32 this method probably will never be called.
	 */
	private void rebuildOrderIndices() {
		Buffer tmp = future;
		future = new PriorityBuffer(DEFAULT_INITIAL_CAPACITY, new SimEventComparator());
		order = Integer.MIN_VALUE;
		while (tmp.size() > 0) {
			addToFuture((SimEvent)tmp.remove());
		}
	}
	
	/**
	 * Adds an event to future queue.
	 * @param event
	 */
	private void addToFuture(SimEvent event) {
		if (order == Integer.MAX_VALUE) {
			// Need to rebuild future events queue becauseorder indices were finished. Probably this case will never happen
			// but we are robust to it.
			rebuildOrderIndices();
		}
		event.internalOrdering = order++;
		future.add(event);
	}
	
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#remove(jmt.engine.simEngine.SimEvent)
	 */
	public boolean remove(SimEvent ev) {
		if (ev.eventTime() == currentTime) {
			return current.remove(ev);
		} else {
			return future.remove(ev);
		}
	}
	/* (non-Javadoc)
	 * @see jmt.engine.simEngine.EventQueue#size()
	 */
	public int size() {
		return current.size() + future.size();
	}
	
	/**
	 * Internal Iterator implementation
	 */
	private class Iter implements Iterator {
		private Iterator currentIter = current.iterator();
		private Iterator futureIter = future.iterator();
		private boolean isCurrent = true;
		
		/* (non-Javadoc)
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return currentIter.hasNext() || futureIter.hasNext();
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#next()
		 */
		public Object next() {
			if (currentIter.hasNext()) {
				isCurrent = true;
				return currentIter.next();
			} else {
				isCurrent = false;
				return futureIter.next();
			}
		}

		/* (non-Javadoc)
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			if (isCurrent) {
				currentIter.remove();
			} else {
				futureIter.remove();
			}
		}
		
	}
	
	private static class SimEventComparator implements Comparator {
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			SimEvent e1 = (SimEvent) o1;
			SimEvent e2 = (SimEvent) o2;
			
			if (e1.eventTime() > e2.eventTime()) {
				return 1;
			} else if (e1.eventTime() < e2.eventTime()){
				return -1;
			} else {
				if (e1.internalOrdering > e2.internalOrdering) {
					return 1;
				} else if (e1.internalOrdering < e2.internalOrdering) {
					return -1;
				} else {
					return 0;
				}
			}
		}
		
	}
	
}
