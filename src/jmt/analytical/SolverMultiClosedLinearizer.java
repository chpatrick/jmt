package jmt.analytical;

/**
 * This class implements the Linearizer and De Souza-Muntz Linearizer algorithms for solving multi-closed models
 * 
 * @author Abhimanyu Chugh
 *
 */
public class SolverMultiClosedLinearizer extends SolverMultiClosedAMVA {
	// boolean to decide which version of Linearizer to use
	private boolean useDeSouzaMuntz;
	
	public SolverMultiClosedLinearizer(int classes, int stations, int[] classPopulation, boolean useDeSouzaMuntz) {
		super(classes, stations, classPopulation);
		this.useDeSouzaMuntz = useDeSouzaMuntz;
		//MAX_ITERATIONS = 3;
	}

	@Override
	public void solveLI() {
		double[][][] queueLengths = new double[classes+1][stations][classes];
		double[][][] custFracDiffs = new double[stations][classes][classes];
		
		// Step 1: Initialisation
		for (int k = 0; k < stations; k++) {
			for (int c = 0; c < classes; c++) {
				if (stations > 0) {
					queueLengths[0][k][c] = clsPopulation[c]/(double)stations;
				}
				for (int j = 1; j < classes+1; j++) {
					custFracDiffs[k][c][j-1] = 0;
					int clsPop = clsPopulation[c];
					if (c == j-1) {
						clsPop--;
					}
					if (stations > 0) {
						queueLengths[j][k][c] = clsPop/(double)stations;
					}
				}
			}
		}
		
		int iterations = 1;
		LinearizerCoreAlgorithm coreResult = null;
		LinearizerCoreAlgorithm[] coreResults = new LinearizerCoreAlgorithm[classes+1];
		double[][] scCustFracDiffs = null;
		for (int c = 0; c < classes+1; c++) {
			if (useDeSouzaMuntz) {
				coreResults[c] = new DeSouzaMuntzLinearizerCoreAlgorithm(c-1);
				scCustFracDiffs = new double[stations][classes];
			} else {
				coreResults[c] = new LinearizerCoreAlgorithm();
			}
		}
		while(true) {
			if (useDeSouzaMuntz) {
				for (int k = 0; k < stations; k++) {
					for (int j = 0; j < classes; j++) {
						scCustFracDiffs[k][j] = 0;
						for (int c = 0; c < classes; c++) {
							scCustFracDiffs[k][j] += clsPopulation[c]*custFracDiffs[k][c][j];
						}
					}
				}
				for (int c = 0; c < classes+1; c++) {
					if (coreResults[c] instanceof DeSouzaMuntzLinearizerCoreAlgorithm) {
						((DeSouzaMuntzLinearizerCoreAlgorithm) coreResults[c]).setScCustFracDiffs(scCustFracDiffs);
					}
				}
			}
			
			// Step 2
			coreResults[0].solve(clsPopulation, queueLengths[0], custFracDiffs);
			
			// Step 3: termination criteria
			if (iterations >= MAX_ITERATIONS || maxDiff(queueLengths[0], coreResults[0].getQueueLengths()) < tolerance) {
				coreResult = coreResults[0];
				break;
			}
			
			queueLengths[0] = coreResults[0].getQueueLengths();
			
			// Step 4
			for (int j = 1; j < classes+1; j++) {
				int[] newClsPopulation = new int[classes];
				for (int c = 0; c < classes; c++) {
					newClsPopulation[c] = clsPopulation[c];
					if (c == j-1) {
						newClsPopulation[c]--;
					}
				}
				coreResults[j].solve(newClsPopulation, queueLengths[j], custFracDiffs);
				queueLengths[j] = coreResults[j].getQueueLengths();
			}
			
			// Step 5
			double[][][] fractions = new double[classes+1][stations][classes];
			for (int k = 0; k < stations; k++) {
				for (int c = 0; c < classes; c++) {
					for (int j = 0; j < classes+1; j++) {
						int clsPop = clsPopulation[c];
						if (c == j-1) {
							clsPop--;
						}
						if (clsPop != 0) {
							fractions[j][k][c] = queueLengths[j][k][c]/(double)clsPop;
						}
					}
				}
			}
			
			for (int k = 0; k < stations; k++) {
				for (int c = 0; c < classes; c++) {
					for (int j = 0; j < classes; j++) {
						custFracDiffs[k][c][j] = fractions[j+1][k][c] - fractions[0][k][c];
					}
				}
			}
			
			// Step 6
			iterations++;
		}
		
		this.iterations = iterations;
		
		residenceTime = coreResult.getResidenceTimes();
		queueLen = coreResult.getQueueLengths();

		sysResponseTime = 0;
		sysNumJobs = 0;
		sysThroughput = 0;
		
		// calculate aggregate measures
		for (int c = 0; c < classes; c++) {
			clsRespTime[c] = 0;
			for (int k = 0; k < stations; k++) {
				clsRespTime[c] += residenceTime[k][c];
			}
			if (clsRespTime[c] == 0) {
				clsThroughput[c] = 0;
			} else {
				clsThroughput[c] = clsPopulation[c]/clsRespTime[c];
			}
			sysResponseTime += clsRespTime[c];
		}
		for (int c = 0; c < classes; c++) {
			clsNumJobs[c] = 0;
			for (int k = 0; k < stations; k++) {
				throughput[k][c] = clsThroughput[c]*visits[k][c];
				utilization[k][c] = throughput[k][c]*servTime[k][c][0];
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
	
	/**
	 * The original Linearizer Core algorithm
	 * 
	 * @author Abhimanyu Chugh
	 *
	 */
	private class LinearizerCoreAlgorithm {
		private double[][] resTimes = new double[stations][classes];
		protected double[][][] queueLengths;
		protected double[] scQueueLengthsWithWholePop = new double[stations];
		
		public LinearizerCoreAlgorithm() {
			queueLengths = new double[classes+1][stations][classes];
		}
		
		public double[][] getResidenceTimes() {
			return resTimes;
		}
		
		public double[][] getQueueLengths() {
			return queueLengths[0];
		}
		
		public void solve(int[] clsPopulation, double[][] inputQueueLengths, double[][][] inputCustFracDiffs) {
			double[] clsThroughputs = new double[classes];
			double[][] oldQueueLengths = new double[stations][classes];
			
			for (int k = 0; k < stations; k++) {
				scQueueLengthsWithWholePop[k] = 0;
				for (int c = 0; c < classes; c++) {
					queueLengths[0][k][c] = inputQueueLengths[k][c];
					scQueueLengthsWithWholePop[k] += inputQueueLengths[k][c];
				}
			}
			
			int iterations = 0;
			while(true) {
				iterations++;
				
				for (int k = 0; k < stations; k++) {
					for (int c = 0; c < classes; c++) {
						oldQueueLengths[k][c] = queueLengths[0][k][c];
					}
				}
				
				// Step 2
				double[][] scQueueLengths = new double[classes][stations];
				scQueueLengths = getSCQueueLengths(clsPopulation, inputCustFracDiffs);
				
				for (int c = 0; c < classes; c++) {
					double clsRespTime = 0;
					for (int k = 0; k < stations; k++) {
						double demand = (servTime[k][c][0]*visits[k][c]);
						if (type[k] == Solver.DELAY) {
							resTimes[k][c] = demand;
						} else {
							resTimes[k][c] = demand*(1+scQueueLengths[c][k]);
						}
						clsRespTime += resTimes[k][c];
					}
					if (clsRespTime == 0) {
						clsThroughputs[c] = 0;
					} else {
						clsThroughputs[c] = clsPopulation[c]/clsRespTime;
					}
				}
				
				for (int k = 0; k < stations; k++) {
					scQueueLengthsWithWholePop[k] = 0;
					for (int c = 0; c < classes; c++) {
						queueLengths[0][k][c] = clsThroughputs[c]*resTimes[k][c];
						scQueueLengthsWithWholePop[k] += queueLengths[0][k][c];
					}
				}
				
				// Step 4: termination criteria
				if (maxDiff(oldQueueLengths, queueLengths[0]) < tolerance) {
					break;
				}
			}
		}

		protected double[][] getSCQueueLengths(int[] clsPopulation, double[][][] inputCustFracDiffs) {
			double[][] scQueueLengths = new double[classes][stations];
			for (int k = 0; k < stations; k++) {
				for (int c = 0; c < classes; c++) {
					double fraction = 0;
					if (clsPopulation[c] > 0) {
						fraction = queueLengths[0][k][c]/(double)clsPopulation[c];	
					}
					for (int j = 1; j < classes+1; j++) {
						int clsPop = clsPopulation[c];
						if (clsPop > 0 && c == j-1) {
							clsPop--;
						}
						queueLengths[j][k][c] = clsPop*(fraction + inputCustFracDiffs[k][c][j-1]);
					}
				}
			}
			
			for (int k = 0; k < stations; k++) {
				for (int j = 0; j < classes; j++) {
					scQueueLengths[j][k] = 0;
					for (int c = 0; c < classes; c++) {
						scQueueLengths[j][k] += queueLengths[j+1][k][c];
					}
				}
			}
			return scQueueLengths;
		}
	}

	/**
	 * The modified Core algorithm proposed by De Souza and Muntz
	 * 
	 * @author Abhimanyu Chugh
	 *
	 */
	private class DeSouzaMuntzLinearizerCoreAlgorithm extends LinearizerCoreAlgorithm {
		private int classIndexWithOneLessCustomer;
		double[][] scCustFracDiffs = null;
		
		public DeSouzaMuntzLinearizerCoreAlgorithm(int classIndexWithOneLessCustomer) {
			super();
			queueLengths = new double[1][stations][classes];
			this.classIndexWithOneLessCustomer = classIndexWithOneLessCustomer;
		}
		
		public void setScCustFracDiffs(double[][] scCustFracDiffs) {
			this.scCustFracDiffs = scCustFracDiffs;
		}
		
		@Override
		protected double[][] getSCQueueLengths(int[] clsPopulation,
				double[][][] inputCustFracDiffs) {
			double[][] scQueueLengths = new double[classes][stations];
			
			if (scCustFracDiffs == null) {
				System.out.println("service centre cust frac diffs are NULL");
				for (int k = 0; k < stations; k++) {
					for (int j = 0; j < classes; j++) {
						scCustFracDiffs[k][j] = 0;
						for (int c = 0; c < classes; c++) {
							scCustFracDiffs[k][j] += clsPopulation[c]*inputCustFracDiffs[k][c][j];
						}
					}
				}
			}
			
			for (int j = 0; j < classes; j++) {
				for (int k = 0; k < stations; k++) {
					double scQueueLength = scQueueLengthsWithWholePop[k] + scCustFracDiffs[k][j] - inputCustFracDiffs[k][j][j];
					if (clsPopulation[j] > 0) {
						scQueueLength -= queueLengths[0][k][j]/(double)clsPopulation[j];
					}
					if (classIndexWithOneLessCustomer >= 0) {
						scQueueLength -= inputCustFracDiffs[k][classIndexWithOneLessCustomer][j];
					}
					scQueueLengths[j][k] = scQueueLength;
				}
			}
			
			return scQueueLengths;
		}
	}
	
	@Override
	protected double[][] getQueueLensWithOneLessCustomer() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
