package jmt.analytical;

/**
 * This class implements the abstract superclass for approximate MVA algorithms for solving multi-closed models
 * 
 * @author Abhimanyu Chugh
 *
 */
public abstract class SolverMultiClosedAMVA extends SolverMulti {
	public final static double DEFAULT_TOLERANCE = Math.pow(10, -7);
	protected double tolerance = DEFAULT_TOLERANCE;
	protected int MAX_ITERATIONS = Integer.MAX_VALUE;
	protected int[] clsPopulation;
	protected int iterations = 0; // algorithm iterations

	public SolverMultiClosedAMVA(int classes, int stations, int[] classPopulation) {
		super(classes, stations);
		this.clsPopulation = classPopulation;
		this.scQueueLen = new double[stations];
	}

	/**
	 * check whether the given tolerance is valid or not
	 * @param tolerance
	 * @return a Double if valid, otherwise null
	 */
	public static Double validateTolerance(String tolerance) {
		try {
			return Double.parseDouble(tolerance);
		} catch (Exception ex) {
			return null;
		}
	}
	
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
	
	public void setMaxIterations(int maxIterations) {
		MAX_ITERATIONS = maxIterations;
	}
	
	public int getIterations() {
		return iterations;
	}
	
	/** Returns the number of jobs in the system
	 *  @return the total number of jobs
	 */
	public double getTotNumJobs() {
		return sysNumJobs;
	}

	@Override
	public void solve() {
		//tests if all the resources, stations, are load independent
		boolean loadIndep = true;
		for (int i = 0; i < stations && loadIndep; i++) {
			if (type[i] == LD) {
				loadIndep = false;
			}
		}

		if (loadIndep) {
			solveLI();
		} else {
			solveLD();
		}
	}
	
	private void solveLD() {
		// TODO Auto-generated method stub
		// still needs to be implemented
	}
	
	public void solveLI() {
		double[][] scQueueLensWithOneLessCustomer = new double[stations][classes];
		
		for (int c = 0; c < classes; c++) {
			for (int k = 0; k < stations; k++) {
				if (stations == 0) {
					queueLen[k][c] = 0;
				} else {
					queueLen[k][c] = clsPopulation[c]/(double)stations;
				}
			}
		}
		
		int iterations = 0;
		double[][] oldQueueLen = new double[stations][classes];
		
		while (true) {
			iterations++;
			for (int k = 0; k < stations; k++) {
				for (int c = 0; c < classes; c++) {
					oldQueueLen[k][c] = queueLen[k][c];
				}
			}
			
			scQueueLensWithOneLessCustomer = getQueueLensWithOneLessCustomer();
			
			for (int c = 0; c < classes; c++) {
				clsRespTime[c] = 0;
				for (int k = 0; k < stations; k++) {
					double demand = (servTime[k][c][0]*visits[k][c]);
					if (type[k] == Solver.DELAY) {
						residenceTime[k][c] = demand;
					} else {
						residenceTime[k][c] = demand*(1 + scQueueLensWithOneLessCustomer[k][c]);
					}
					clsRespTime[c] += residenceTime[k][c];
				}
				if (clsRespTime[c] == 0) {
					clsThroughput[c] = 0;
				} else {
					clsThroughput[c] = clsPopulation[c]/(double)clsRespTime[c];
				}
			}
			for (int k = 0; k < stations; k++) {
				for (int c = 0; c < classes; c++) {
					queueLen[k][c] = clsThroughput[c]*residenceTime[k][c];
				}
			}
			// Check convergence criteria
			if (iterations >= MAX_ITERATIONS || maxDiff(oldQueueLen, queueLen) < tolerance) {
				break;
			}
		}
		
		this.iterations = iterations;
		sysResponseTime = 0;
		sysNumJobs = 0;
		sysThroughput = 0;
		
		// calculate aggregate measures
		for (int c = 0; c < classes; c++) {
			clsNumJobs[c] = 0;
			for (int k = 0; k < stations; k++) {
				throughput[k][c] = clsThroughput[c]*visits[k][c];
				utilization[k][c] = throughput[k][c]*servTime[k][c][0];
				clsNumJobs[c] += queueLen[k][c];
			}
			sysResponseTime += clsRespTime[c];
			sysNumJobs += clsNumJobs[c];
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
		}
		if (sysResponseTime != 0) {
			sysThroughput = sysNumJobs/sysResponseTime;
		}
	}

	// method to compute station queue lengths with one less customer
	// array[k][c], where c is the class of the omitted customer and k the station
	protected abstract double[][] getQueueLensWithOneLessCustomer();

	/**
	 * returns the max difference between the old queue lengths and the current queue lengths of each station
	 * 
	 * @param scOldQueueLens the old queue lengths to compare with current queue lengths (scCurrentQueueLens)
	 * @param scCurrentQueueLens the current queue lengths used for comparison
	 * @return the max difference
	 */
	protected double maxDiff(double[] scOldQueueLens, double[] scCurrentQueueLens) {
		double maxDiff = 0;
		for (int k = 0; k < scOldQueueLens.length; k++) {
			double currDiff = Math.abs((scOldQueueLens[k] - scCurrentQueueLens[k])/scCurrentQueueLens[k]);
			if (currDiff > maxDiff) {
				maxDiff = currDiff;
			}
		}
		return maxDiff;
	}

	/**
	 * returns the max difference between the old queue lengths and the current queue lengths of each station and class
	 * 
	 * @param oldQueueLens the old queue lengths to compare with current queue lengths (scCurrentQueueLens)
	 * @param currentQueueLens the current queue lengths used for comparison
	 * @return the max difference
	 */
	protected double maxDiff(double[][] oldQueueLens, double[][] currentQueueLens) {
		double maxDiff = 0;
		for (int k = 0; k < oldQueueLens.length; k++) {
			for (int c = 0; c < classes; c++) {
				double currDiff = Math.abs((oldQueueLens[k][c] - currentQueueLens[k][c])/currentQueueLens[k][c]);
				if (currDiff > maxDiff) {
					maxDiff = currDiff;
				}
			}
		}
		return maxDiff;
	}

	@Override
	public boolean hasSufficientProcessingCapacity() {
		// only closed class - no saturation problem
		return true;
	}

}
