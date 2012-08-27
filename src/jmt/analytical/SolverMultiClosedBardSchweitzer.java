package jmt.analytical;

/**
 * This class implements the Bard-Schweitzer algorithm for solving multi-closed models
 * 
 * @author Abhimanyu Chugh
 *
 */
public class SolverMultiClosedBardSchweitzer extends SolverMultiClosedAMVA {

	public SolverMultiClosedBardSchweitzer(int classes, int stations, int[] classPopulation) {
		super(classes, stations, classPopulation);
	}

	@Override
	protected double[][] getQueueLensWithOneLessCustomer() {
		double[][] scQueueLens = new double[stations][classes];
		for (int k = 0; k < stations; k++) {
			for (int c = 0; c < classes; c++) {
				double currQueueLen = 0;
				for (int r = 0; r < classes; r++) {
					// if this is the class with one less customer, normalise its queue length
					// otherwise assume its queue length is same as queue length with full customer population
					if (c == r) {
						if (clsPopulation[r] != 0) {
							currQueueLen += queueLen[k][r]*(clsPopulation[r] - 1)/(double)clsPopulation[r];
						}
					}
					else {
						currQueueLen += queueLen[k][r];
					}
				}
				scQueueLens[k][c] = currQueueLen;
			}
		}
		return scQueueLens;
	}
	
}
