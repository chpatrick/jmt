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
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.dataAnalysis.Measure;
import jmt.engine.log.JSimLogger;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is a timer task which is run when the simulation time exceeds the upper limit
 * which has been set by the user.
 * When the upper time limit is reached, this SimulationTimer aborts the simulation.
 *
 * @author Stefano Omini
 * @version 4-ott-2004 14.39.04
 */
public class SimulationTimer extends TimerTask {

    private static final boolean DEBUG = true;
    private Simulation sim;
    private long maxSimulationTime;
    private Timer timer = null;

    private JSimLogger logger = JSimLogger.getLogger(JSimLogger.STD_LOGGER);

    /**
     * Creates a simulation timer, which aborts the simulation if the max simulation
     * time has expired.
     * @param sim the simulation to be eventually aborted
     * @param maxSimulationTime the max simulation time (in milliseconds)
     */
    public SimulationTimer(Simulation sim, long maxSimulationTime) {
        this.sim = sim;
        this.maxSimulationTime = maxSimulationTime;
        this.timer = new Timer();
        timer.schedule(this, maxSimulationTime);
    }

    /**
     * This is the method which is invoked when the max time has expired: it aborts
     * the simulation and cancels the timer.
     */
    public void run() {

        if (!sim.hasFinished()) {
            //simulation hasn't finished yet: aborts the simulation

            //pause sim
            NetSystem.pause();

            //aborts remaining measures
            LinkedList measures = sim.getNetwork().getMeasures();
            int measureNumber = measures.size();

            logger.warn("Max simulation time has been reached. Simulation will be aborted.");
            logger.warn("Aborting measures...");

            for (int m = 0; m < measureNumber; m++) {
                //abort measure
                ((Measure) measures.get(m)).abortMeasure();
            }


            if (DEBUG) {
                System.out.println("Simulation aborted: max simulation time has been reached");
            }

            //aborts the simulation (next SimSystem.runTick() will be false)
            SimSystem.setTimeout(true);

            //resume sim
            NetSystem.restartFromPause();

            //terminate timer's task execution thread
            timer.cancel();
            //disable timer
            disableTimer();
        }

        //otherwise, the timer has been already cancelled when the simulation ended


    }


    public void stopTimer() {
        if (timer != null) {
            logger.debug("Simulation completed before max simulation time has been reached...");
            timer.cancel();
        }
        return;
    }


    public void disableTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        return;
    }



}
