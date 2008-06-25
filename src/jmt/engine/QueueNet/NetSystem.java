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

import java.util.ListIterator;

import jmt.common.exception.NetException;
import jmt.engine.dataAnalysis.Measure;
import jmt.engine.simEngine.QueueMeasure;
import jmt.engine.simEngine.SimSystem;

/**
 * This class controls the simulation running. It should be used to :
 * <ul>
 * <li>start the simulation;</li>
 * <li>stop the simulation;</li>
 * <li>abort the simulation;</li>
 * <li>get simulation elapsed time;</li>
 * <li>get total simulation time;</li>
 * <li>get real elapsed time.</li>
 * </ul>
 * Note that all methods are static and the class <b>should not be</b>
 * instanced.
 * @author Francesco Radaelli, Federico Granata, Stefano Omini
 * Modified by Bertoli Marco: fixed bug that avoided simulation termination with closed classes. 22-09-2005
 */
public class NetSystem {

	private static NetController netController;

	private static long startTime;

	private static NetworkList networkList;

	//QueueMeasure is used as a buffer: it contains the partial results
	//of measure computations, which are passed to the gui (which plots them)
	private static QueueMeasure queueMeasure = null;

	//NEW
	//@author Stefano Omini

	//true if the engine is connected to the gui, false otherwise
	//if true, the QueueMeasure is not used (to increase performances)
	private static boolean GUImode = false;

	//end NEW

	private NetSystem() {
	};

	/**
	 * Initializes the simulation system
	 *
	 */
	public static void initialize() {

		netController = new NetController();
		SimSystem.initialize();
		networkList = new NetworkList();

		//NEW
		//@author Stefano Omini
		if (GUImode) {
			queueMeasure = new QueueMeasure();
		}
		//end NEW

	}

	/** Terminates NetSystem and closes the log. You should recall the initialize method, rebuild
	 * all the network nodes and recall the <i>start</i> method in order to
	 * launch a new simulation.
	 */
	public static void terminate() throws jmt.common.exception.NetException {
		abort();

	}

	/** Gets the current simulation time.
	 *	@return Current simulation time.
	 */
	public static double getTime() {
		return SimSystem.getClock();
	}

	/** Gets the current "real" elapsed time.
	 *	@return Current simulation time.
	 */
	public static double getElapsedTime() {
		return (System.currentTimeMillis() - startTime) / 1000.0;
	}

	/** Starts the NetSystem Engine and executes the simulation.
	 * This method should be called when <b>all</b> NetNode of <b>all</b>
	 * QueueNetworks are ready to start.
	 * @throws Exception
	 */
	public static void start() throws Exception {
		NetNode Node;
		ListIterator Nets = networkList.listIterator();
		ListIterator Nodes;
		QueueNetwork Network;
		startTime = System.currentTimeMillis();

		//OLD
		// netController.start();

		while (Nets.hasNext()) {
			Network = (QueueNetwork) Nets.next();
			if (Network.getState() == QueueNetwork.STATE_READY) {

				//TODO: proviamo a cambiare
				//Nodes = Network.getReferenceNodes().listIterator();
				Nodes = Network.getNodes().listIterator();

				while (Nodes.hasNext()) {
					Node = (NetNode) Nodes.next();
					Node.send(NetEvent.EVENT_START, null, 0.0, NodeSection.NO_ADDRESS, NodeSection.INPUT, Node);
				}
				Network.setState(QueueNetwork.STATE_RUNNING);

			}
		}

		//NEW
		//@author Stefano Omini
		netController.start();
		//end NEW

		netController.run();
	}

	//NEW
	//@author Stefano Omini

	public static boolean pause() {
		//if (netController != null && netController.isRunning()) {
		if (netController != null) {
			netController.block();
			return true;
		}
		return false;
	}

	public static boolean restartFromPause() {
		//if (netController != null && netController.isRunning()) {
		if (netController != null) {
			netController.unblock();
			return true;
		}
		return false;
	}

	//end NEW

	/** Stops the NetSystem Engine and terminates the simulation (stops all the
	 * controlled QueueNetworks).
	 * @throws jmt.common.exception.NetException
	 */
	public static void stop() throws jmt.common.exception.NetException {
		ListIterator Nets = networkList.listIterator();
		QueueNetwork Network;
		while (Nets.hasNext()) {
			Network = (QueueNetwork) Nets.next();
			stop(Network);
		}
	}

	/** Stops the NetSystem Engine and terminates the simulation.
	 * @param Network Reference to the netowrk to be stopped.
	 * @throws jmt.common.exception.NetException
	 */
	public static void stop(QueueNetwork Network) throws jmt.common.exception.NetException {
		NetNode Node;
		if (Network.getState() == QueueNetwork.STATE_RUNNING) {
			/* --- Bertoli Marco: Informs ALL nodes of stop event and not only Reference nodes
			ListIterator Nodes = Network.getReferenceNodes().listIterator();
			if (Network.getReferenceNodes().size() > 0) {
			 while (Nodes.hasNext()) {
			  Node = (NetNode) Nodes.next();
			  Node.send(NetEvent.EVENT_STOP, null, 0.0,
			    NodeSection.NO_ADDRESS,
			    NodeSection.NO_ADDRESS,
			    Node);
			 }
			} else {  */
			ListIterator Nodes = Network.getNodes().listIterator();

			while (Nodes.hasNext()) {
				Node = (NetNode) Nodes.next();
				Node.send(NetEvent.EVENT_STOP, null, 0.0, NodeSection.NO_ADDRESS, NodeSection.NO_ADDRESS, Node);
				// }

			}
			Network.setState(QueueNetwork.STATE_STOPPED);

		}
	}

	/** Aborts the NetSystem Engine and terminates the simulation (aborts all
	 * the controlled QueueNetworks).
	 * @throws jmt.common.exception.NetException
	 */
	public static void abort() throws jmt.common.exception.NetException {
		ListIterator Nets = networkList.listIterator();
		QueueNetwork Network;
		while (Nets.hasNext()) {
			Network = (QueueNetwork) Nets.next();
			abort(Network);
		}
		SimSystem.abort();
	}

	/** Aborts the NetSystem Engine and terminates the simulation.
	 * @param Network Reference to the netowrk to be stopped.
	 * @throws jmt.common.exception.NetException
	 */
	public static void abort(QueueNetwork Network) throws jmt.common.exception.NetException {
		NetNode Node;
		if (Network.getState() == QueueNetwork.STATE_RUNNING) {
			ListIterator Nodes = Network.getReferenceNodes().listIterator();
			//if (log.check(NetLog.LEVEL_RELEASE))
			//	log.write(NetLog.LEVEL_RELEASE, "Aborting network \"" +
			//	        Network.getName() + "\"...");
			Nodes = Network.getReferenceNodes().listIterator();
			while (Nodes.hasNext()) {
				Node = (NetNode) Nodes.next();
				Node.send(NetEvent.EVENT_ABORT, null, 0.0, NodeSection.NO_ADDRESS, NodeSection.NO_ADDRESS, Node);
			}
			Network.setState(QueueNetwork.STATE_ABORTED);

		}
	}

	/** Imposes that NetSystem should be stopped if a specific getLog level is
	 *  reached.
	 * @param StopLevel Level which NetSystem should be stopped.
	 */
	public static void stopOnLogLevel(int StopLevel) {
		netController.stopOnLogLevel(StopLevel);
	}

	/** Imposes that NetSystem should be aborted if a specific getLog level is
	 *  reached.
	 * @param AbortLevel Level which NetSystem should be aborted.
	 */
	public static void abortOnLogLevel(int AbortLevel) {
		netController.abortOnLogLevel(AbortLevel);
	}

	/** Adds a new network to the NetSystem.
	 * @param Network Reference to the network to be added.*/
	public static void addNetwork(QueueNetwork Network) {
		networkList.add(Network);
	}

	/** Gets networks list.
	 * @return Reference to a network list.*/
	public static NetworkList getNetworkList() {
		return networkList;
	}

	/** Checks if the NetSystem Engine is running.
	 * @return True if NetSystem Engine is running.
	 */
	public static boolean isRunning() {
		return netController.isRunning();
	}

	/** Gets total simulation time.
	 * @return Simulation time.
	 */
	public static double getSimulationTime() {
		return netController.getSimulationTime();
	}

	static final NetNode getNode(int Id) {
		return (NetNode) SimSystem.getEntity(Id);
	}

	/**Gets a node from its name. Searches the name between all the networks
	 *
	 * @param name the name of the node
	 * @return searched node
	 */
	public static NetNode getNode(String name) {
		return (NetNode) SimSystem.getEntity(name);
	}

	/**
	 * Gets a <tt>double</tt> type measure
	 * @param name The name of the node which measure refers to.
	 * @param measureID The type of measure requested.
	 * @return
	 * @throws jmt.common.exception.NetException
	 */
	//TODO: NON USATA
	public static double getMeasure(String name, int measureID) throws jmt.common.exception.NetException {
		QueueNetwork net = getNode(name).getQueueNet();
		return net.getMeasure(name, measureID);
	}

	/**
	 * Gets the QueueMeasure object controlled by NetSystem
	 */
	public static QueueMeasure getQueueMeasure() {
		return queueMeasure;
	}

	static void checkMeasures() throws jmt.common.exception.NetException {
		ListIterator networks = networkList.listIterator();
		QueueNetwork network;
		ListIterator measures;
		int count, num;
		Measure measure;
		while (networks.hasNext()) {
			network = (QueueNetwork) networks.next();
			num = network.getMeasures().size();
			if (num > 0) {
				measures = network.getMeasures().listIterator();
				count = 0;

				while (measures.hasNext()) {
					measure = (Measure) measures.next();
					if (measure.hasFinished()) {
						count++;
					}
				}

				switch (network.getBehaviour()) {
					case QueueNetwork.BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_STOP:
						if (count == num) {
							stop(network);
						}
						break;
					case QueueNetwork.BEHAVIOUR_OBTAIN_ALL_MEASURES_THEN_ABORT:
						if (count == num) {
							queueMeasure.setEnd(true);
							abort(network);
						}
						break;
					case QueueNetwork.BEHAVIOUR_ABORT:
						abort();
						break;
					case QueueNetwork.BEHAVIOUR_STOP:
						stop();
						break;

				}
			}
		}
	}

	/**
	 * Checks simulation progress, showing a percentage of completed works
	 * <br>Author: Bertoli Marco
	 * @param network network to be checked for progress
	 * @return estimated simulation progress
	 * @throws jmt.common.exception.NetException if network il null
	 */
	public static double checkProgress(QueueNetwork network) throws jmt.common.exception.NetException {

		if (network == null) {
			throw new NetException("Can't measure progress of a network which does not exist.");
		}
		// We extimate on the slowest not completed measure
		ListIterator measures;
		double slowest = 1;
		Measure measure;
		measures = network.getMeasures().listIterator();
		while (measures.hasNext()) {
			measure = (Measure) measures.next();
			if (!measure.hasFinished()) {
				// find slowest measure
				if (measure.getSamplesAnalyzedPercentage() < slowest) {
					slowest = measure.getSamplesAnalyzedPercentage();
				}
			}
		}
		return slowest;
	}

	//TODO: va cambiato....
	//NEW
	//@author Stefano Omini
	public static double getTempMeasures(QueueNetwork network) throws jmt.common.exception.NetException {

		if (network == null) {
			throw new NetException("Can't get measures of a network which does not exist.");
		}

		ListIterator measures;
		int count = 0;
		Measure measure;
		int num = network.getMeasures().size();

		if (num > 0) {
			measures = network.getMeasures().listIterator();
			while (measures.hasNext()) {
				measure = (Measure) measures.next();
				//count finished measures
				if (measure.hasFinished()) {
					count++;
				} else {
				}
			}
		}
		return (double) count / (double) num;
	}

	//end NEW

	//NEW
	//@author Stefano Omini

	static void stopNoSamplesMeasures() {
		ListIterator networks = networkList.listIterator();
		QueueNetwork network;
		ListIterator measures;
		int num;
		Measure measure;
		while (networks.hasNext()) {
			network = (QueueNetwork) networks.next();
			num = network.getMeasures().size();
			if (num > 0) {
				measures = network.getMeasures().listIterator();

				while (measures.hasNext()) {
					measure = (Measure) measures.next();

					if (!measure.hasFinished()) {
						measure.testDeadMeasure();

						/*
						int nsamples = measure.getAnalyzer().getSamples();

						String name = measure.getName();
						double perc = measure.getMaxPrecisionPercentage();
						double mean = measure.getExtimatedMeanValue();
						System.out.println(name + " --> " + nsamples + " - " + Double.toString(perc) +
						    " - " + Double.toString(mean));

						if (nsamples == 0) {
						    measure.stop_NoSamples();
						}
						*/

					}
				}
			}
		}
	}
	//end NEW

}