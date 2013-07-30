/**
 * Copyright (C) 2013, Giuliano Casale
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jmt.analytical;

import javax.naming.OperationNotSupportedException;

import QueuingNet.MoMSolver;
import DataStructures.BigRational;
import DataStructures.QNModel;
import Exceptions.BTFMatrixErrorException;
import Exceptions.InconsistentLinearSystemException;
import Exceptions.InternalErrorException;
import QueuingNet.QNSolver;

/**
 * This class implements the API to allow use by other applications. One can
 * define the queueing network model details, call the solvers transparently and,
 * afterwards, read the results.
 *
 * @author Michail Makaronidis, 2010
 */

public class SolverMultiClosedMoM extends SolverMulti {
	/**
	 * Array containing population for each class
	 */
	protected int[] population;
	/**
	 * Contains the queuing network model
	 */
	protected QNModel qnm;
	/**
	 * Number of threads that the MoMSolver should use
	 */
	protected int nThreads;

	/** Creates new MoMSolverDispatcher
	 *  @param  stations    number of service centers
	 *  @param  classes     number o classes of classes
	 *  @param  population     array of population classes
	 */
	public SolverMultiClosedMoM(int classes, int stations, int[] population) {
		super(classes, stations);
		this.classes = classes;
		this.stations = stations;
		this.population = population;
	}

	/** Initializes the solver with the system parameters.
	 * It must be called before trying to solve the model.
	 *  @param  t   array of the types (LD or LI) of service centers
	 *  @param  s   matrix of service times of the service centers
	 *  @param  v   array of visits to the service centers
	 *  @param  nThreads The number of threads the solver should use. If nThreads > 2 then the parallel algorithm is used.
	 *  @return True if the operation is completed with success
	 */
	public boolean input(int[] t, double[][][] s, double[][] v, int nThread) {
		if ((t.length > stations) || (s.length > stations) || (v.length > stations)) {
			// wrong input.
			return false;
		}
		nThreads = nThread;
		visits = new double[stations][classes];
		for (int i = 0; i < stations; i++) {
			System.arraycopy(v[i], 0, visits[i], 0, classes);
		}

		servTime = new double[stations][classes][1];
		for (int i = 0; i < stations; i++) {
			for (int r = 0; r < classes; r++) {
				servTime[i][r][0]=s[i][r][0] * 1000000;
			}
		}

		try {
			int M = 0, R = this.classes;
			Integer[] Z = new Integer[classes];
			for (int r = 0; r < R; r++) {
				Z[r] = 0;
			}

			// Discover delay times (think times)
			for (int i = 0; i < stations; i++) {
				if (t[i] == LI) {
					M++;
				} else if (t[i] == LD) {
					for (int r = 0; r < classes; r++) {
						Z[r] += (int) (servTime[i][r][0] * visits[i][r]);
					}
				} else {
					return false;
				}
			}
			// Now Z contains the delay times

			// Discover service demands
			Integer[][] D = new Integer[M][R];
			int mIndex = 0; // current queue
			for (int i = 0; i < stations; i++) {
				if (t[i] == LI) {
					for (int r = 0; r < classes; r++) {
						D[mIndex][r] = (int) (servTime[i][r][0] * visits[i][r]);
					}
					mIndex++;
				}
			}
			// Now D contains service demands

			// Create queue multiplicities array
			// All multiplicities are set to 1, as JMT does not seem to use queue multiplicities
			// If this array is instantiated properly, the rest of the MoMSolver can support them
			Integer[] multiplicities = new Integer[M];
			for (int m = 0; m < M; m++) {
				multiplicities[m] = 1;
			}

			// Transform population from int[] to Integer[]
			Integer[] N = new Integer[R];
			for (int r = 0; r < R; r++) {
				N[r] = population[r];
			}

			// Instantiate queuing network model
			qnm = new QNModel(R, M, N, Z, multiplicities, D);
			qnm.N.print();

		} catch (Exception ex) {
			ex.printStackTrace();
			// Return false if initialisation fails for any reason.
			return false;
		}
		return true;
	}

	/**
	 * Solves the system through the MoM algorithm.
	 * input(...) must have been called first.
	 *
	 * @throws InternalErrorException Thrown when any error is encountered during computations, i.e. due to linear system singularities
	 * @throws BTFMatrixErrorException 
	 * @throws InconsistentLinearSystemException 
	 * @throws OperationNotSupportedException 
	 */
	@Override
	public void solve() {
		QueuingNet.QNSolver c = null;

		c = new MoMSolver(qnm,nThreads);

		try {
			c.computeNormalisingConstant();
		} catch (OperationNotSupportedException | InternalErrorException
				| InconsistentLinearSystemException | BTFMatrixErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			c.computePerformanceMeasures();
		} catch (InternalErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		clsThroughput = qnm.getMeanThroughputsAsDoubles();
		queueLen = qnm.getMeanQueueLengthsAsDoubles();
		
		throughput = new double[stations][classes];
		utilization = new double[stations][classes];
		scThroughput = new double[stations];
		scUtilization = new double[stations];
		scResidTime = new double[stations];
		scQueueLen = new double[stations];
		for (int m = 0; m < qnm.M; m++) {
			for (int r = 0; r < qnm.R; r++) {
				throughput[m][r] = clsThroughput[r] * visits[m][r] * 1000000;
				utilization[m][r] = throughput[m][r] * servTime[m][r][0] / 1000000; // Umc=Xmc*Smc
				residenceTime[m][r] = queueLen[m][r] / clsThroughput[r] / 1000000;
				scThroughput[m] += throughput[m][r];
				scUtilization[m] += utilization[m][r];
				scResidTime[m] += residenceTime[m][r];
				scQueueLen[m] += queueLen[m][r];
			}
		}


		//NEW
		//@author Stefano Omini
		//compute system parameters
		sysResponseTime = 0;
		sysNumJobs = 0;

		//		for (c = 0; c < classes; c++) {
		//	for (m = 0; m < stations; m++) {
		//		clsRespTime[c] += residenceTime[m][c];
		//		sysNumJobs += queueLen[m][c];
		//	}
		//	sysResponseTime += clsRespTime[c];
		//}

		sysThroughput = sysNumJobs / sysResponseTime;
		//end NEW

	}

	@Override
	public boolean hasSufficientProcessingCapacity() {
		// only closed class - no saturation problem
		return true;
	}
}
