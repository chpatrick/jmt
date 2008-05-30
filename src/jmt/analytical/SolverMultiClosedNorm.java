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

/*
 * SolverMultiClosedNorm.java
 *
 * Created on 13 maggio 2002, 19.10
 */

package jmt.analytical;

import java.io.PrintWriter;

/**
 * Solves a multiclass closed model, using the normalization constant
 *  algorithm. It also uses a preordering algorithm to obtain a stable network.<br>
 *  For a description of the algorithm see:<br>
 * <em>
 *  S.C. Bruell, G. Balbo,<br>
 * "Computational Algorithms for closed Queueing Networks"<br>
 *  1980, elsevier North Holland
 * </em>
 * @author  Federico Granata
 *
 */
public class SolverMultiClosedNorm extends SolverMulti {

	private int[] population;//array of population for every class

	private int[] popMult;

	private int[] status;

	private double[] G;//normalization constant

	private double[] auxFun;// used in the inversion of the noralization const

	private final int[] intZeros;//an array of zeros

	private PrintWriter pw = new PrintWriter(System.out, true);

	/** Creates new SolverMultiClosedNorm
	 *  @param  stations    number of service centers
	 *  @param  classes     number o classes of customers
	 *  @param  population     array of population classes
	 */
	public SolverMultiClosedNorm(int classes, int stations, int[] population) {
		super(classes, stations);
		int maxPop = 0;

		population = new int[classes];
		System.arraycopy(population, 0, population, 0, population.length);
		popMult = new int[classes];
		popMult[0] = 1;
		for (int i = 1; i < popMult.length; i++) {
			popMult[i] = popMult[i - 1] * (population[i - 1] + 1);
		}

		for (int i = 0; i < classes; i++) {
			maxPop += population[i] + 1;
		}
		for (int i = 0; i < stations; i++) {
			for (int j = 0; j < classes; j++) {
				servTime[i][j] = new double[maxPop];
			}
		}
		G = new double[popMult[classes - 1] * (population[classes - 1] + 1)];
		auxFun = new double[G.length];
		status = new int[classes];
		intZeros = new int[classes];

	}

	//NEW
	//@author Stefano Omini

	/**
	 * A system is said to have sufficient capacity to process a given load
	 * <tt>lambda</tt> if no service center is saturated as a result of the combined loads
	 * of all the classes.
	 * <br>
	 * Must be implemented to create a multi class model solver.
	 * <br>
	 * WARNING: This method should be called before solving the system.
	 * @return true if sufficient capacity exists for the given workload, false otherwise
	 *
	 *
	 */
	public boolean hasSufficientProcessingCapacity() {
		//only closed class: no saturation
		return true;
	}

	//end NEW

	/**
	 *  Solves the system throught the normalization constant algorithm.
	 */
	public void solve() {
		long start; // initial time.
		long end; // termination time.
		double sum = 0;
		double quad = 0;
		double Y = 0;
		double scalCons = 0;
		boolean flag;
		int count = 0;
		int totPop = 0;
		double[] FM = new double[G.length];

		/* Static scaling to control magnitude of G, it is not optimal, and in
		 * rare case it do not overcome overflow problem, but generally it's
		 * enough */
		sum = 0;
		quad = 0;
		for (int i = 0; i < stations; i++) {
			for (int j = 0; j < classes; j++) {
				if (type[i] == Solver.LI) {
					Y = visits[i][j] * servTime[i][j][0];
				} else {
					Y = visits[i][j] * servTime[i][j][1];
				}
				sum += Y;
				quad += Y * Y;
			}
		}
		scalCons = sum / quad;
		for (int i = 0; i < stations; i++) {
			for (int j = 0; j < classes; j++) {
				visits[i][j] = visits[i][j] * scalCons;
			}
		}

		// calculation for the first station
		pw.println("start solving");
		start = System.currentTimeMillis();
		this.initStatus();
		G[0] = 1;
		if (type[0] == Solver.LI) {
			for (int i = 1; i < G.length; i++) {
				count = 0;
				do {
					status[count] += 1;
					flag = true;
					if (status[count] > population[count]) {
						status[count] = 0;
						count += 1;
						flag = false;
					}
				} while ((count < classes) && !flag);
				sum = 0;
				for (int j = 0; j < classes; j++) {
					if (status[j] - 1 >= 0) {
						sum += visits[0][j] * servTime[0][j][0] * G[i - popMult[j]];
						//printStatus();//debug command
					}
				}
				G[i] = sum;
				//pw.println("G : " + G[i] + " at " + i);//debug command
				//printStatus();//debug command
			}
			//pw.println("G : " + G[G.length -1] + " at " + name[0]);//debug command
		} else if (type[0] == Solver.LD) {
			totPop = 0;
			for (int i = 1; i < G.length; i++) {
				count = 0;
				do {
					status[count] += 1;
					flag = true;
					if (status[count] > population[count]) {
						status[count] = 0;
						totPop -= population[count];
						count += 1;
						flag = false;
					} else {
						totPop += 1;
					}
				} while ((count < classes) && !flag);
				sum = 0;
				for (int j = 0; j < classes; j++) {
					if (status[j] - 1 >= 0) {
						double v = visits[0][j];
						double s = servTime[0][j][totPop];
						sum += v * s * G[i - popMult[j]];
					}
					//printStatus();
				}
				G[i] = sum;
				//pw.println("G : " + G[i] + " at " + i);
				//printStatus();
			}
			//pw.println("G : " + G[G.length -1] + " at " + name[0]);
		}
		if (stations == 2) {
			System.arraycopy(G, 0, auxFun, 0, G.length);
		}

		/* all others service center */
		for (int m = 1; m < stations; m++) {
			this.initStatus();
			if (type[m] == Solver.LI) {
				for (int i = 1; i < G.length; i++) {
					count = 0;
					do {
						status[count] += 1;
						flag = true;
						if (status[count] > population[count]) {
							status[count] = 0;
							count += 1;
							flag = false;
						}
					} while ((count < classes) && !flag);
					sum = 0;
					for (int j = 0; j < classes; j++) {
						if (status[j] - 1 >= 0) {
							sum += visits[m][j] * servTime[m][j][0] * G[i - popMult[j]];
						}
					}
					G[i] = G[i] + sum;
					//pw.println("G : " + G[i] + " at " + i);
					//printStatus(i);
				}
				//pw.println("G : " + G[G.length -1] + " at " + name[m]);
			} else if (type[m] == Solver.LD) {
				// calculate FM
				totPop = 0;
				FM[0] = 1;
				for (int i = 1; i < G.length; i++) {
					count = 0;
					do {
						status[count] += 1;
						flag = true;
						if (status[count] > population[count]) {
							status[count] = 0;
							totPop -= population[count];
							count += 1;
							flag = false;
						} else {
							totPop += 1;
						}
					} while ((count < classes) && !flag);
					sum = 0;
					for (int j = 0; j < classes; j++) {
						if (status[j] - 1 >= 0) {
							sum += visits[m][j] * servTime[m][j][totPop] * FM[i - popMult[j]];
						}
					}
					FM[i] = sum;
					//pw.println("FM : " + FM[i] + " at " + i);
					//printStatus(i);
				}

				status[0] += 1;
				for (int i = G.length - 1; i > 0; i--) {
					count = 0;
					do {
						status[count] -= 1;
						flag = true;
						if (status[count] < 0) {
							status[count] = population[count];
							count += 1;
							flag = false;
						}
					} while ((count < classes) && !flag);
					sum = 0;

					int[] newStatus = new int[status.length];
					newStatus[0] = -1;
					for (int c = 1; c < status.length; c++) {
						newStatus[c] = 0;
					}

					for (int j = 0; j <= i; j++) {
						count = 0;
						do {
							newStatus[count] += 1;
							flag = true;
							if (newStatus[count] > population[count]) {
								newStatus[count] = 0;
								count += 1;
								flag = false;
							}
						} while ((count < classes) && !flag);
						if (validStatus(newStatus)) {
							sum += FM[j] * G[i - j];
						}
					}
					G[i] = sum;
					//pw.println("G : " + G[i] + " at " + i);
					//printStatus(i);
				}
				//pw.println("G : " + G[G.length -1] + " at " + name[m]);
			}

			if (m == stations - 2) {
				System.arraycopy(G, 0, auxFun, 0, G.length);
			}
		}
		pw.println("end solving");
		end = System.currentTimeMillis();
		pw.println("Time elapsed in milliseconds : " + (end - start));
	}

	/** Calculates the indexes of interest for the system.
	 */
	public void indexes() {
		long start; // initial time.
		long end; // termination time.
		int count;
		boolean flag;
		double[] FM = new double[G.length];
		double sum = 0;
		double util;
		double[] tempQueue = new double[G.length];
		double margProb = 0;
		int totPop = 0;

		System.out.println("Start parameters calculation.");
		start = System.currentTimeMillis();
		/* calculation for last station. we do it because the auxiliary
		 * function it is calculated for free for this station in the norm const
		 * algorithm. */
		if (type[stations - 1] == Solver.LI) {
			tempQueue[0] = 0;
			initStatus();
			for (int n = 1; n < tempQueue.length; n++) {
				tempQueue[n] = 0;
				count = 0;
				do {
					status[count] += 1;
					flag = true;
					if (status[count] > population[count]) {
						status[count] = 0;
						count += 1;
						flag = false;
					}
				} while ((count < classes) && !flag);
				for (int k = 0; k < classes; k++) {
					if (status[k] - 1 >= 0) {
						util = visits[stations - 1][k] * (G[n - popMult[k]] / G[n]) * servTime[stations - 1][k][0];
						tempQueue[n] += util * (1 + tempQueue[n - popMult[k]]);
					}
				}
			}
			for (int j = 0; j < classes; j++) {
				throughput[stations - 1][j] = visits[stations - 1][j] * G[G.length - 1 - popMult[j]] / G[G.length - 1];
				if (type[stations - 1] == Solver.LI) {
					utilization[stations - 1][j] = servTime[stations - 1][j][0] * throughput[stations - 1][j];
					queueLen[stations - 1][j] = utilization[stations - 1][j] * (1 + tempQueue[tempQueue.length - 1 - popMult[j]]);
					residenceTime[stations - 1][j] = queueLen[stations - 1][j] / throughput[stations - 1][j];
					scThroughput[stations - 1] += throughput[stations - 1][j];
					scUtilization[stations - 1] += utilization[stations - 1][j];
					clsRespTime[j] = residenceTime[stations - 1][j];
				}

			}
		} else if (type[stations - 1] == Solver.LD) {
			for (int j = 0; j < classes; j++) {
				throughput[stations - 1][j] = visits[stations - 1][j] * G[G.length - 1 - popMult[j]] / G[G.length - 1];

				// calculate FM
				initStatus();
				totPop = 0;
				FM[0] = 1;
				for (int i = 1; i < G.length; i++) {
					count = 0;
					do {
						status[count] += 1;
						flag = true;
						if (status[count] > population[count]) {
							status[count] = 0;
							totPop -= population[count];
							count += 1;
							flag = false;
						} else {
							totPop += 1;
						}
					} while ((count < classes) && !flag);
					sum = 0;
					for (int cls = 0; cls < classes; cls++) {
						if (status[cls] - 1 >= 0) {
							sum += visits[stations - 1][cls] * servTime[stations - 1][cls][totPop] * FM[i - popMult[cls]];
						}
					}
					FM[i] = sum;
					//pw.println("FM : " + FM[i] + " at " + i);
					//printStatus(i);
				}

				initStatus();
				totPop = 0;
				for (int i = 1; i < G.length; i++) {
					count = 0;
					do {
						status[count] += 1;
						flag = true;
						if (status[count] > population[count]) {
							status[count] = 0;
							totPop -= population[count];
							count += 1;
							flag = false;
						} else {
							totPop += 1;
						}
					} while ((count < classes) && !flag);
					if (status[j] != 0) {
						margProb = FM[i] * auxFun[G.length - 1 - i] / G[G.length - 1];
						queueLen[stations - 1][j] += margProb * status[j];
						utilization[stations - 1][j] += margProb * status[j] / totPop;
					}
				}
				residenceTime[stations - 1][j] = queueLen[stations - 1][j] / throughput[stations - 1][j];
				scThroughput[stations - 1] += throughput[stations - 1][j];
				scUtilization[stations - 1] += utilization[stations - 1][j];
				clsRespTime[j] = residenceTime[stations - 1][j];
			}
		}

		/* index calculation for all other stations */
		for (int i = (stations - 2); i >= 0; i--) {
			if (type[i] == Solver.LI) {
				initStatus();
				for (int n = 1; n < tempQueue.length; n++) {
					tempQueue[n] = 0;
					count = 0;
					do {
						status[count] += 1;
						flag = true;
						if (status[count] > population[count]) {
							status[count] = 0;
							count += 1;
							flag = false;
						}
					} while ((count < classes) && !flag);
					for (int k = 0; k < classes; k++) {
						if (status[k] - 1 >= 0) {
							util = visits[i][k] * (G[n - popMult[k]] / G[n]) * servTime[i][k][0];
							tempQueue[n] += util * (1 + tempQueue[n - popMult[k]]);
						}
					}
				}
				for (int j = 0; j < classes; j++) {
					throughput[i][j] = visits[i][j] * G[G.length - 1 - popMult[j]] / G[G.length - 1];
					utilization[i][j] = servTime[i][j][0] * throughput[i][j];
					queueLen[i][j] = utilization[i][j] * (1 + tempQueue[tempQueue.length - 1 - popMult[j]]);
					residenceTime[i][j] = queueLen[i][j] / throughput[i][j];
					scThroughput[i] += throughput[i][j];
					scUtilization[i] += utilization[i][j];
					clsRespTime[j] += residenceTime[i][j];
				}
			} else if (type[i] == Solver.LD) {
				calcAuxFun(i);
				for (int j = 0; j < classes; j++) {
					throughput[i][j] = visits[i][j] * G[G.length - 1 - popMult[j]] / G[G.length - 1];

					// calculate FM
					initStatus();
					totPop = 0;
					FM[0] = 1;
					for (int n = 1; n < G.length; n++) {
						count = 0;
						do {
							status[count] += 1;
							flag = true;
							if (status[count] > population[count]) {
								status[count] = 0;
								totPop -= population[count];
								count += 1;
								flag = false;
							} else {
								totPop += 1;
							}
						} while ((count < classes) && !flag);
						sum = 0;
						for (int cls = 0; cls < classes; cls++) {
							if (status[cls] - 1 >= 0) {
								sum += visits[i][cls] * servTime[i][cls][totPop] * FM[n - popMult[cls]];
							}
						}
						FM[n] = sum;
						//pw.println("FM : " + FM[n] + " at " + n);
						//printStatus(n);
					}
					initStatus();
					totPop = 0;
					for (int k = 1; k < G.length; k++) {
						count = 0;
						do {
							status[count] += 1;
							flag = true;
							if (status[count] > population[count]) {
								status[count] = 0;
								totPop -= population[count];
								count += 1;
								flag = false;
							} else {
								totPop += 1;
							}
						} while ((count < classes) && !flag);
						if (status[j] != 0) {
							margProb = FM[k] * auxFun[G.length - 1 - k] / G[G.length - 1];
							queueLen[i][j] += margProb * status[j];
							utilization[i][j] += margProb * status[j] / totPop;
						}
					}
					residenceTime[i][j] = queueLen[i][j] / throughput[i][j];
					scThroughput[i] += throughput[i][j];
					scUtilization[i] += utilization[i][j];
					clsRespTime[j] += residenceTime[i][j];
				}
			}
		}
		for (int j = 0; j < classes; j++) {
			clsThroughput[j] = population[j] / clsRespTime[j];
			sysThroughput += clsThroughput[j];
		}
		for (int i = 0; i < stations; i++) {
			scQueueLen[i] = 0;
			for (int j = 0; j < classes; j++) {
				scQueueLen[i] += queueLen[i][j];
				scResidTime[i] += residenceTime[i][j] * clsThroughput[j];
			}
			scResidTime[i] /= sysThroughput;
		}
		for (int j = 0; j < classes; j++) {
			sysResponseTime += population[j] / sysThroughput;
		}
		/* Generate output */
		pw.println("End of parameters calculation.");
		end = System.currentTimeMillis();
		pw.println("Time elapsed in milliseconds : " + (end - start));
		return;
	}

	/** Calculates the auxiliary function needful to calculate marginal
	 * probabilities
	 * @param center service center for witch it is calculated
	 */
	private void calcAuxFun(int center) {
		double[] FM = new double[G.length];
		double sum = 0;
		int totPop = 0;
		int count = 0;
		boolean flag;

		initStatus();
		FM[0] = 1;
		for (int i = 1; i < G.length; i++) {
			count = 0;
			do {
				status[count] += 1;
				flag = true;
				if (status[count] > population[count]) {
					status[count] = 0;
					totPop -= population[count];
					count += 1;
					flag = false;
				} else {
					totPop += 1;
				}
			} while ((count < classes) && !flag);
			sum = 0;
			for (int j = 0; j < classes; j++) {
				if (status[j] - 1 >= 0) {
					sum += visits[center][j] * servTime[center][j][totPop] * FM[i - popMult[j]];
				}
			}
			FM[i] = sum;
			//pw.println("FM : " + FM[i] + " at " + i);
			//printStatus(i);
		}

		initStatus();
		auxFun[0] = 1;
		for (int i = 1; i < G.length - 1; i++) {
			auxFun[i] = 0;
			count = 0;
			do {
				status[count] += 1;
				flag = true;
				if (status[count] > population[count]) {
					status[count] = 0;
					count += 1;
					flag = false;
				}
			} while ((count < classes) && !flag);
			sum = 0;

			int[] newStatus = new int[status.length];
			newStatus[0] = -1;
			for (int c = 1; c < status.length; c++) {
				newStatus[c] = 0;
			}

			for (int j = 0; j <= i; j++) {
				count = 0;
				do {
					newStatus[count] += 1;
					flag = true;
					if (newStatus[count] > population[count]) {
						newStatus[count] = 0;
						count += 1;
						flag = false;
					}
				} while ((count < classes) && !flag);
				if (validStatus(newStatus)) {
					sum += FM[j] * auxFun[i - j];
				}
			}
			auxFun[i] = G[i] - sum;
			//pw.println("Auxil Function : " + auxFun[i] + " at " + i);
			//printStatus(i);
		}
		//pw.println("Auxil Function : " + auxFun[G.length -1] + " at " + name[center]);
	}

	/*Reinitializes the status variable*/
	private final void initStatus() {
		System.arraycopy(intZeros, 0, status, 0, intZeros.length);
	}

	/* calculates the actual status*/
	private final void calcStatus(int pos) {
		for (int i = (classes - 1); i >= 0; i--) {
			status[i] = pos / popMult[i];
			pos = pos - (status[i] * popMult[i]);
		}
	}

	/** Prints the actual status of the system: it's a debug function
	 */
	public void printStatus() {
		for (int i = 0; i < classes; i++) {
			pw.print(status[i]);
			pw.print(" ");
		}
		pw.print("\n");
	}

	/** Prints the actual status of the system:it's a debug function
	 * @param pos the actual position
	 */
	public void printStatus(int pos) {
		calcStatus(pos);
		printStatus();
	}

	/*returns true if the status is valid(p.e. population < 0)*/
	private final boolean validStatus(int[] ns) {
		for (int i = 0; i < status.length; i++) {
			if (status[i] - ns[i] < 0) {
				return false;
			}
		}
		return true;
	}

}
