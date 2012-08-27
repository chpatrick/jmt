package jmt.analytical;

/**
 * This class implements the AQL (Aggregated Queue Length) algorithm for solving multi-closed models
 * 
 * @author Abhimanyu Chugh
 *
 */
public class SolverMultiClosedAQL extends SolverMultiClosedAMVA {
	private double[][] gamma;

	public SolverMultiClosedAQL(int classes, int stations, int[] classPopulation) {
		super(classes, stations, classPopulation);
		this.scQueueLen = new double[stations];
		this.gamma = new double[stations][classes];
	}

	@Override
	public void solveLI() {
		double[][][] residenceTimes = new double[classes+1][stations][classes];
		double[][] scQueueLens = new double[classes+1][stations];
		double[][] clsThroughputs = new double[classes+1][classes];
		
		// initialisation
		int totalPopulation = 0;
		for (int c = 0; c < classes; c++) {
			totalPopulation += clsPopulation[c];
		}
		for (int t = 0; t <= classes; t++) {
			int pop = totalPopulation;
			if (pop > 0 && t != 0) {
				pop--;
			}
			for (int k = 0; k < stations; k++) {
				if (stations > 0) {
					scQueueLens[t][k] = pop/(double)stations;
				}
				if (t < classes) {
					gamma[k][t] = 0;
				}
			}
		}
		
		int iterations = 0;
		double[] scOldQueueLens = new double[stations];
		
		while (true) {
			iterations++;
			for (int k = 0; k < stations; k++) {
				scOldQueueLens[k] = scQueueLens[0][k];
			}
			
			for (int t = 0; t < classes+1; t++) {
				double[] tempClsRespTimes = new double[classes];
				for (int k = 0; k < stations; k++) {
					for (int c = 0; c < classes; c++) {
						double demand = (servTime[k][c][0]*visits[k][c]);
						int pop = totalPopulation;
						if (pop > 0 && t != 0) {
							pop--;
						}
						if (type[k] == Solver.DELAY) {
							residenceTimes[t][k][c] = demand;
						} else if (pop <= 0) {
							residenceTimes[t][k][c] = 0;
						} else {
							residenceTimes[t][k][c] = demand*(1 + ((pop-1)*((scQueueLens[t][k]/(double)pop)-gamma[k][c])));
						}
						
						tempClsRespTimes[c] += residenceTimes[t][k][c];
					}
				}
				for (int c = 0; c < classes; c++) {
					int clsPop = clsPopulation[c];
					if (clsPop > 0 && c == t-1) {
						clsPop--;
					}
					if (tempClsRespTimes[c] != 0) {
						clsThroughputs[t][c] = clsPop/tempClsRespTimes[c];
					}
				}
				for (int k = 0; k < stations; k++) {
					double sum = 0;
					for (int c = 0; c < classes; c++) {
						sum += clsThroughputs[t][c]*residenceTimes[t][k][c];
					}
					scQueueLens[t][k] = sum;
				}
			}
			
			// compute new values of gamma
			for (int k = 0; k < stations; k++) {
				for (int c = 0; c < classes; c++) {
					if (totalPopulation <= 0) {
						gamma[k][c] = 0;
					}
					else {
						gamma[k][c] = scQueueLens[0][k]/(double)totalPopulation;
						if (totalPopulation > 1) {
							gamma[k][c] -= scQueueLens[c+1][k]/((double)(totalPopulation-1));
						}
					}
				}
			}
			
			// Check convergence criteria
			if (iterations >= MAX_ITERATIONS || maxDiff(scOldQueueLens, scQueueLens[0]) < tolerance) {
				break;
			}
		}
		
		this.iterations = iterations;
		sysResponseTime = 0;
		sysNumJobs = 0;
		sysThroughput = 0;
		
		// calculate aggregate measures
		for (int c = 0; c < classes; c++) {
			clsRespTime[c] = 0;
			for (int k = 0; k < stations; k++) {
				residenceTime[k][c] = residenceTimes[0][k][c];
				clsRespTime[c] += residenceTime[k][c];
			}
			clsThroughput[c] = clsThroughputs[0][c];
			sysResponseTime += clsRespTime[c];
		}
		for (int c = 0; c < classes; c++) {
			clsNumJobs[c] = 0;
			for (int k = 0; k < stations; k++) {
				throughput[k][c] = clsThroughput[c]*visits[k][c];
				utilization[k][c] = throughput[k][c]*servTime[k][c][0];
				if (type[k] == Solver.DELAY) {
					queueLen[k][c] = utilization[k][c];
				} else {
					queueLen[k][c] = utilization[k][c]*(1+scQueueLens[c+1][k]);
				}
				clsNumJobs[c] += queueLen[k][c];
			}
		}
		for (int k = 0; k < stations; k++) {
			scResidTime[k] = 0;
			scQueueLen[k] = 0;
			scThroughput[k] = 0;
			scUtilization[k] = 0;
			for (int c = 0; c < classes; c++) {
				scResidTime[k] += residenceTime[k][c];
				scQueueLen[k] += queueLen[k][c];
				scThroughput[k] += throughput[k][c];
				scUtilization[k] += utilization[k][c];
			}
			sysNumJobs += scQueueLen[k];
		}
		if (sysResponseTime != 0) {
			sysThroughput = sysNumJobs/sysResponseTime;
		}
	}

	@Override
	protected double[][] getQueueLensWithOneLessCustomer() {
		// TODO Auto-generated method stub
		return null;
	}

}
