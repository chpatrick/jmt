/**
 * The simjava package is a support library for discrete
 * event simulations.
 */

package jmt.engine.simEngine;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This is the system class which manages the simulation. All
 * the members of this class are static, so there is no need to
 * create an instance of this class.
 */
public class SimSystem {

	private static boolean DEBUG = false;

	// Private data members
	static private List entities; // The current entity list

	//TODO: verificare quale classe funziona meglio per la future event queue
	//	static private EventQueue future;   // The future event queue
	static private EventQueue future; // The future event queue
	//	static private SuperEventQueue future; // The future event queue

	static private EventQueue deferred; // The deferred event queue

	static private double clock; // Holds the current global simulation time
	static private boolean running; // Tells whether the run() member been called yet
	static private NumberFormat nf;

	//STE
	//temp variable used by runTick
	//static private SimEvent event = null;

	//NEW
	//@author Stefano Omini

	//if true a timer has been defined on the simulation
	static private boolean hasTimer = false;

	//if true, max simulation time has been reached and simulation must be aborted
	static private boolean timeout = false;

	//true if the simulation has been finished and therefore SimSystem must be stopped
	static private boolean simStopped = false;

	//end NEW

	//NEW
	//@author Stefano Omini

	/**
	 * Sets timeout property
	 * @param simTimeout True if max time has been reached
	 */
	static public void setTimeout(boolean simTimeout) {
		timeout = simTimeout;
		hasTimer = true;
	}

	//end NEW

	//
	// Public library interface
	//

	// Initializes system
	/** Initializes the system, this function works as a
	 * constructor, and should be called at the start of any simulation
	 * program. It comes in several flavours depending on what context
	 * the simulation is running.<P>
	 * This is the simplest, and should be used for standalone simulations
	 * It sets up trace output to a file in the current directory called
	 * `tracefile'
	 */
	static public void initialize() {

		entities = new ArrayList();

		//TODO: verificare quale classe funziona meglio per la future event queue

		// future = new ListEventQueue();
		// future = new CircularEventQueue();
		// future = new SuperEventQueue();
		future = new HybridEventQueue();
		
		deferred = new ListEventQueue();

		clock = 0.0;
		running = false;

		// Set the default number format
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(2);

	}

	/** Returns the number format used for generating
	 * times in trace lines
	 */
	static public NumberFormat getNumberFormat() {
		return nf;
	}

	// The two standard predicates
	/** A standard predicate that matches any event. */
	static public SimAnyP SIM_ANY = new SimAnyP();
	/** A standard predicate that does not match any events. */
	static public SimNoneP SIM_NONE = new SimNoneP();

	// Public access methods

	/** Get the current simulation time.
	 * @return The simulation time
	 */
	static public double clock() {
		return clock;
	}

	/** A different name for <tt>SimSystem.clock()</tt>.
	 * @return The current simulation time
	 */
	static public double getClock() {
		return clock;
	}

	/** Gets the current number of entities in the simulation
	 * @return A count of entities
	 */
	static public int getNumEntities() {
		return entities.size();
	}

	/** Finds an entity by its id number.
	 * @param id The entity's unique id number
	 * @return A reference to the entity, or null if it could not be found
	 */
	static final public SimEntity getEntity(int id) {
		return (SimEntity) entities.get(id);
	}

	/** Finds an entity by its name.
	 * @param name The entity's name
	 * @return A reference to the entity, or null if it could not be found
	 */
	static public SimEntity getEntity(String name) {
		SimEntity ent, found = null;

		for (Iterator it = entities.iterator(); it.hasNext();) {
			ent = (SimEntity) it.next();
			if (name.compareTo(ent.getName()) == 0) {
				found = ent;
			}
		}
		if (found == null) {
			System.out.println("SimSystem: could not find entity " + name);
		}
		return found;
	}

	/** Finds out an entity unique id number from its name.
	 * @param name The entity's name
	 * @return The entity's unique id number, or -1 if it could not be found
	 */
	static public int getEntityId(String name) {
		return entities.indexOf(getEntity(name));
	}

	/** Adds a new entity to the simulation.
	 * This is now done automatically in the SimEntity constructor,
	 * so there is no need to call this explicitly.
	 * @param e A reference to the new entity
	 */
	static public void add(SimEntity e) {
		SimEvent evt;
		if (running()) {
			/* Post an event to make this entity */
			evt = new SimEvent(SimEvent.CREATE, clock, e.getId(), 0, 0, e);
			future.add(evt);
		} else {
			if (e.getId() == -1) {

				//-1 is the default value written by the constructor.
				// Now a new ID is set

				// Only add once!
				e.setId(entities.size());
				entities.add(e);
			}
		}
	}

	/** Adds a new entity to the simulation, when the simulation is running.
	 * Note this is an internal method and should not be called from
	 * user simulations. Use <tt>add()</tt> instead to add entities
	 * on the fly.
	 * @param e A reference to the new entity
	 */
	static synchronized void addEntityDynamically(SimEntity e) {
		e.setId(entities.size());
		if (e == null) {
			System.out.println("Adding null entity :(");
		} else {
			System.out.println("Adding: " + e.getName());
		}
		entities.add(e);
		e.start();
	}

	/**
	 * Starts the simulation running, by calling the start() method of each entity.
	 * Of course this should be called after all the entities have been setup and added,
	 * and their ports linked.
	 */
	static public void runStart() {
		SimEntity ent;
		running = true;
		// Start all the entities' threads
		if (DEBUG) {
			System.out.println("SimSystem: Starting entities");
		}
		for (Iterator it = entities.iterator(); it.hasNext();) {
			ent = (SimEntity) it.next();
			ent.start();
		}
	}

	/**
	 * Runs one tick of the simulation: the system looks for events in the future queue.
	 * @return <tt>false</tt> if there are no more future events to be processed
	 */
	static public boolean runTick() throws jmt.common.exception.NetException {

		//NEW
		//@author Stefano Omini

		if (timeout) {
			//time has expired: simulation must be aborted
			return false;
		}

		// If there are more future events then deals with them
		SimEvent event;

		if (future.size() > 0) {
			event = future.pop();

			processEvent(event);
			double now = event.eventTime();
			// Checks if next events are at same time...
			boolean trymore = (future.size() > 0);
			while (trymore) {
				event = future.peek();
				if (event.eventTime() == now) {
					processEvent(future.pop());
					trymore = (future.size() > 0);
				} else {
					trymore = false;
				}
			}
		} else {
			running = false;
			//System.out.println("SimSystem: No more future events");

		}
		return running;
	}

	/** Stops the simulation, by calling the poison() method of each SimEntity.
	 */
	static public void runStop() {
		SimEntity ent;
		// Attempt to kill all the entity threads
		for (Iterator it = entities.iterator(); it.hasNext();) {
			ent = (SimEntity) it.next();
			ent.poison();
		}
		if (DEBUG) {
			System.out.println("Exiting SimSystem.run()");
		}
	}

	//TODO: usato solo per debug
	/** Starts the simulation running. This should be called after
	 * all the entities have been setup and added, and their ports linked.
	 * <br>
	 * The simulation goes on until runTick() becomes false (there are no more
	 * future events to be processed).
	 * Then the runStop() method is called.
	 */
	static public void run() throws jmt.common.exception.NetException {
		//Now the main loop
		//System.out.println("SimSystem: Entering main loop");
		runStart();
		while (runTick()) {
		}
		runStop();
	}

	//
	// Package level methods
	//

	static boolean running() {
		return running;
	}

	// Entity service methods

	// Called by an entity just before it becomes non-RUNNABLE
	//	static void paused() {
	//		onestopped.v();
	//	}

	static synchronized void hold(int src, double delay) {
		SimEvent e = new SimEvent(SimEvent.HOLD_DONE, clock + delay, src);
		future.add(e);
	}

	static synchronized void send(int src, int dest, double delay, int tag, Object data) {
		SimEvent e = new SimEvent(SimEvent.SEND, clock + delay, src, dest, tag, data);
		future.add(e);
	}

	static synchronized void wait(int src) {

	}

	static synchronized int waiting(int d, SimPredicate p) {
		int w = 0;
		SimEvent event;
		//DEK (Federico Granata) 25-11-2003
		for (Iterator it = deferred.iterator(); it.hasNext();) {
			event = (SimEvent) it.next();
			if (event.getDest() == d) {
				if (p.match(event)) {
					w++;
				}
			}
		}
		return w;

		/* old
		for (e = deferred.elements(); e.hasMoreElements();) {
			ev = (SimEvent) e.nextElement();
			if (ev.getDest() == d) {
				//	trcout.println("t: in waiting, - event from "+ev.getSrc()+" to "+
				//	       ev.getDest() + " type:"+ev.type()+" time:"+ev.eventTime());

				if (p.match(ev))
					w++;
			}
		}
		return w;*/
		//end DEK (Federico Granata) 25-11-2003
	}

	static synchronized void select(int src, SimPredicate p) {
		SimEvent ev = null;
		boolean found = false;

		// retrieve + remove event with dest == src
		for (Iterator it = deferred.iterator(); it.hasNext() && !found;) {
			ev = (SimEvent) it.next();
			if (ev.getDest() == src) {
				if (p.match(ev)) {
					deferred.remove(ev);
					found = true;
				}
			}
		}
		if (found) {
			((SimEntity) entities.get(src)).setEvbuf((SimEvent) ev.clone());

		} else {
			((SimEntity) entities.get(src)).setEvbuf(null);

		}
	}

	static synchronized void cancel(int src, SimPredicate p) {
		SimEvent ev = null;
		boolean found = false;

		// retrieves + remove event with dest == src
		for (Iterator it = future.iterator(); it.hasNext() && !found;) {
			ev = (SimEvent) it.next();
			if (ev.getSrc() == src) {
				if (p.match(ev)) {
					it.remove();
					found = true;
				}
			}
		}
		if (found) {
			((SimEntity) entities.get(src)).setEvbuf((SimEvent) ev.clone());

		} else {
			((SimEntity) entities.get(src)).setEvbuf(null);

		}
	}

	static synchronized void putback(SimEvent ev) {
		deferred.add(ev);
	}

	//
	// Private internal methods
	//

	static private void processEvent(SimEvent e) throws jmt.common.exception.NetException {
		int dest, src;
		SimEntity destEnt;

		//System.out.println("SimSystem: Processing event");
		// Update the system's clock
		if (e.eventTime() < clock) {
			throw new jmt.common.exception.NetException("SimSystem: Error - past event detected! \n" + "Time: " + clock + ", event time: "
					+ e.eventTime() + ", event type: " + e.getType() + future);
		}
		clock = e.eventTime();

		// Ok now process it
		switch (e.getType()) {

			case (SimEvent.SEND):
				// Checks for matching wait
				dest = e.getDest();
				if (dest < 0) {
					throw new jmt.common.exception.NetException("SimSystem: Error - attempt to send to a null entity");
				} else {
					destEnt = (SimEntity) entities.get(dest);
					if (destEnt.getState() == SimEntity.WAITING) {
						SimPredicate p = destEnt.getWaitingPred();

						if (p == null) {
							//the entity was waiting for a generic predicate
							destEnt.setEvbuf((SimEvent) e.clone());
							destEnt.setState(SimEntity.RUNNABLE);
							try {
								destEnt.execute();
							} catch (jmt.common.exception.NetException e1) {
								abort();
								throw e1;
							}
						} else {
							//the entity was waiting for events with a specified predicate
							//this event matches with such predicate??
							if (destEnt.getWaitingPred().match(e)) {
								p = null;
								destEnt.setEvbuf((SimEvent) e.clone());
								destEnt.setState(SimEntity.RUNNABLE);
								try {
									destEnt.execute();
								} catch (jmt.common.exception.NetException e1) {
									abort();
									throw e1;
								}
							} else {
								//the event doesn't match with the predicate, so it's put in the deferred queue
								destEnt.simPutback(e);
							}
						}
					} else {
						//if the entity is not WAITING the event is put in the deferred queue
						deferred.add(e);
					}
				}
				break;

			case (SimEvent.ENULL):
				throw new jmt.common.exception.NetException("SimSystem: Error - event has a null type");

			case (SimEvent.CREATE):
				SimEntity newe = (SimEntity) e.getData();
				addEntityDynamically(newe);
				break;

			case (SimEvent.HOLD_DONE):
				src = e.getSrc();
				if (src < 0) {
					throw new jmt.common.exception.NetException("SimSystem: Error - NULL entity holding");
				} else {
					((SimEntity) entities.get(src)).setState(SimEntity.RUNNABLE);
					((SimEntity) entities.get(src)).restart();
				}
				break;
		}
	}

	/**
	 * Aborts the simulation!
	 *
	 */
	static public void abort() {
		running = false;
		if (DEBUG) {
			System.out.println("Simulation Aborted");
		}
	}

	//NEW
	//@author Stefano Omini

	/**
	 * By using this method, the simulation can be aborted or stopped even if there
	 * are still events in the future events queue.
	 * @param simStopped true to stop SimSystem
	 */
	public static void setSimStopped(boolean simStopped) {
		SimSystem.simStopped = simStopped;
	}

	//end NEW

}
