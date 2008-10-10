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

package jmt.engine.QueueNet;

import jmt.engine.simEngine.SimSystem;

/**
 * Controls the state of the simulation and determines when to stop the simulation.
 *
 * @author Federico Granata, Stefano Omini
 */

class NetController {

	private final boolean DEBUG = false;

	private boolean running;

	private double startTime, stopTime;

	private int stopLevel, abortLevel;

	//NEW
	//@author Stefano Omini

	//number of system "ticks"
	private int n;
	//check measures every refreshPeriod system ticks
	private int refreshPeriod = 12000;

	//check if some measures have not receive any sample yet
	//WARNING: this samples number must be a multiple of refreshPeriod!!
	private int reachabilityTest = refreshPeriod * 10;

	//log object
	//private NetLog log;
	//end NEW

	//NEW
	//@author Stefano Omini
	private boolean blocked = false;

	//end NEW

	NetController() {

		running = false;
		//NEW
		//@author Stefano Omini
		//initializes tick counter
		n = 0;
		//end NEW
	}

	/** This is the run method of the NetController (thread). */
	public void run() {

		//Date dateTime;
		//dateTime = new Date();

		try {
			SimSystem.runStart();
			startTime = NetSystem.getElapsedTime();

			while (SimSystem.runTick()) {

				synchronized (this) {

					//NEW
					//@author Stefano Omini
					//the presence of this "if" allows pause control
					if (blocked) {
						try {
							wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					//end NEW

					n++;

					if (n % refreshPeriod == 0) {

						//User may have defined measures that will not receive any sample
						if (n % reachabilityTest == 0) {
							//stop measures which haven't collected samples yet
							NetSystem.stopNoSamplesMeasures();
						}
						//refresh measures
						NetSystem.checkMeasures();
					}

				}

			}
			//sim is finished: get stop time
			stopTime = NetSystem.getElapsedTime();
			SimSystem.runStop();
			running = false;

		} catch (Exception Exc) {
			Exc.printStackTrace();
		}
	}

	public void start() {
		running = true;
	}

	/** Checks if the NetSystem Engine thread is running.
	 * @return True if NetSystem Engine thread is running.
	 */
	synchronized boolean isRunning() {
		return running;
	}

	/** Gets simulation time.
	 * @return Simulation time.
	 */
	synchronized double getSimulationTime() {
		return stopTime - startTime;
	}

	/** Imposes that NetController should be stopped if a specific getLog level is
	 * reached.
	 * @param StopLevel Level which NetSystem should be stopped.
	 */
	synchronized void stopOnLogLevel(int StopLevel) {
		this.stopLevel = StopLevel;
	}

	/** Imposes that NetController should be aborted if a specific getLog level is
	 * reached.
	 * @param AbortLevel Level which NetSystem should be aborted.
	 */
	synchronized void abortOnLogLevel(int AbortLevel) {
		this.abortLevel = AbortLevel;
	}

	/**
	 * Blocks NetController for synchronized access to data.
	 */
	public synchronized void block() {
		blocked = true;
	}

	/**
	 * Unblocks the object.
	 */
	public synchronized void unblock() {
		blocked = false;
		notifyAll();
	}

}