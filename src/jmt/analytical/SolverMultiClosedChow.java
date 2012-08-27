package jmt.analytical;

/**
 * This class implements the AQL (Aggregated Queue Length) algorithm for solving multi-closed models
 * 
 * @author Abhimanyu Chugh
 *
 */
public class SolverMultiClosedChow extends SolverMultiClosedAMVA {

	public SolverMultiClosedChow(int classes, int stations, int[] classPopulation) {
		super(classes, stations, classPopulation);
	}

	@Override
	protected double[][] getQueueLensWithOneLessCustomer() {
		double[][] scQueueLens = new double[stations][classes];
		for (int k = 0; k < stations; k++) {
			for (int c = 0; c < classes; c++) {
				// assume the queue lengths with one less customer in system
				// are the same as queue lengths with full customer population
				double currQueueLen = 0;
				for (int r = 0; r < classes; r++) {
					currQueueLen += queueLen[k][r];
				}
				scQueueLens[k][c] = currQueueLen;
			}
		}
		return scQueueLens;
	}

}
