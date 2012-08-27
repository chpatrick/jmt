package jmt.analytical;

import jmt.framework.data.ArrayUtils;

/**
 * Wrapper class for single-class queueing models
 * Uses multi-class algorithm classes for solving the models
 * 
 * @author Abhimanyu Chugh
 *
 */
public class SolverSingleClosedAMVA extends Solver {

	// the multi-class algorithm solver to use
	private SolverAlgorithm algorithm;
	private SolverMultiClosedAMVA solver;

	public static final boolean DEBUG = false;

	// number of customers or jobs in the system
	protected int customers = 0;

	protected double tolerance;
	private double[][][] serviceTimes;
	private double[][] custVisits;

	public SolverSingleClosedAMVA(int customers, int stations, SolverAlgorithm alg, double tol) {
		this.customers = customers;
		this.stations = stations;
		this.algorithm = alg;
		this.tolerance = tol;
		initialiseSolver();
		
		name = new String[stations];
		type = new int[stations];
		// one service time for each possible population (from 1 to customers)
		// position 0 is used for LI stations
		servTime = new double[stations][customers + 1];
		visits = new double[stations];

		throughput = new double[stations];
		queueLen = new double[stations];
		utilization = new double[stations];
		residenceTime = new double[stations];
	}
	
	private void initialiseSolver() {
		int[] classPop = new int[1];
		classPop[0] = customers;
		
		// initialise the solver to the algorithm selected by user
		if (SolverAlgorithm.CHOW.equals(algorithm)) {
			solver = new SolverMultiClosedChow(1, stations, classPop);
		} else if (SolverAlgorithm.BARD_SCHWEITZER.equals(algorithm)) {
			solver = new SolverMultiClosedBardSchweitzer(1, stations, classPop);
		} else if (SolverAlgorithm.AQL.equals(algorithm)) {
			solver = new SolverMultiClosedAQL(1, stations, classPop);
		} else {
			solver = new SolverMultiClosedLinearizer(1, stations, classPop, SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER.equals(algorithm));
		}
		solver.setTolerance(tolerance);
	}

	public void setMaxIterations(int maxIterations) {
		solver.setMaxIterations(maxIterations);
	}
	
	public boolean hasSufficientProcessingCapacity() {
		// closed class: no saturation problem
		return true;
	}

	@Override
	public boolean input(String[] n, int[] t, double[][] s, double[] v) {
		serviceTimes = new double[stations][1][s[0].length];
		custVisits = new double[stations][1];
		if (!super.input(n, t, s, v)) {
			return false;
		}
		for (int i = 0; i < stations; i++) {
			for (int j = 0; j < s[0].length; j++) {
				serviceTimes[i][0][j] = s[i][j];
			}
			custVisits[i][0] = visits[i];
		}
		return solver.input(n, t, serviceTimes, custVisits);
	}
	
	public void solve() {
		solver.solve();
		
		totUser = customers;
		totRespTime = solver.sysResponseTime;
		totThroughput = solver.sysThroughput;
		
		queueLen = ArrayUtils.extract1(solver.queueLen, 0);
		throughput = ArrayUtils.extract1(solver.throughput, 0);
		residenceTime = ArrayUtils.extract1(solver.residenceTime, 0);
		utilization = ArrayUtils.extract1(solver.utilization, 0);
	}
	
	// get algorithm iterations
	public int getIterations() {
		return solver.getIterations();
	}

}
