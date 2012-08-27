package jmt.analytical;

/**
 * Enum class for all implemented algorithm solvers
 * 
 * @author Abhimanyu Chugh
 *
 */
public enum SolverAlgorithm {
	EXACT("MVA"),
	//MoM("MoM"),
	CHOW("Chow"),
	BARD_SCHWEITZER("Bard-Schweitzer"),
	AQL("AQL"),
	LINEARIZER("Linearizer"),
	DESOUZA_MUNTZ_LINEARIZER("De Souza-Muntz Linearizer"),
	OPEN("Open"),
	MIXED("Mixed");
	
	// string representation of the algorithm
	private String algorithmName;
	
	// array storing string representations of each enum value
	private static String[] NAMES = null;
	
	// all closed algorithm enums and their string representations
	private static String[] CLOSED_NAMES = null;
	private static SolverAlgorithm[] CLOSED_VALUES = null;
	
	private SolverAlgorithm(String algorithmName) {
		this.algorithmName = algorithmName;
	}
	
	@Override
	public String toString() {
		return algorithmName;
	}
	
	/**
	 * Find the enum value that corresponds to the given string
	 */
	public static SolverAlgorithm find(String algName) {
		for (SolverAlgorithm alg : values()) {
			if (alg.toString().equals(algName)) {
				return alg;
			}
		}
		return null;
	}
	
	/**
	 * Returns an array of string representations of all potential SolverAlgorithm values
	 */
	public static String[] names() {
		if (NAMES == null) {
			SolverAlgorithm[] values = values();
			NAMES = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				NAMES[i] = values[i].toString();
			}
		}
		return NAMES;
	}
	
	public static boolean isClosed(SolverAlgorithm alg) {
		return alg == SolverAlgorithm.EXACT || /*alg == SolverAlgorithm.MoM ||*/
				alg == SolverAlgorithm.CHOW || alg == SolverAlgorithm.BARD_SCHWEITZER ||
				alg == SolverAlgorithm.AQL || alg == SolverAlgorithm.LINEARIZER ||
				alg == SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER;
	}
	
	public static boolean isClosed(String algorithm) {
		SolverAlgorithm alg = find(algorithm);
		return alg != null && (alg == SolverAlgorithm.EXACT || /*alg == SolverAlgorithm.MoM ||*/
								alg == SolverAlgorithm.CHOW || alg == SolverAlgorithm.BARD_SCHWEITZER ||
								alg == SolverAlgorithm.AQL || alg == SolverAlgorithm.LINEARIZER ||
								alg == SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER);
	}
	
	public static boolean isExact(SolverAlgorithm alg) {
		return alg == SolverAlgorithm.EXACT /*|| alg == SolverAlgorithm.MoM*/;
	}
	
	public static boolean isApproximate(SolverAlgorithm alg) {
		return alg == SolverAlgorithm.CHOW || alg == SolverAlgorithm.BARD_SCHWEITZER ||
				alg == SolverAlgorithm.AQL || alg == SolverAlgorithm.LINEARIZER ||
				alg == SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER;
	}
	
	public static boolean isApproximate(String algorithm) {
		SolverAlgorithm alg = find(algorithm);
		return alg != null && (alg == SolverAlgorithm.CHOW || alg == SolverAlgorithm.BARD_SCHWEITZER ||
								alg == SolverAlgorithm.AQL || alg == SolverAlgorithm.LINEARIZER ||
								alg == SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER);
	}
	
	/**
	 * Returns an array of string representations of all closed SolverAlgorithm values
	 */
	public static String[] closedNames() {
		if (CLOSED_NAMES == null) {
			SolverAlgorithm[] values = values();
			int closedAlgs = 0;
			for (int i = 0; i < values.length; i++) {
				if (isClosed(values[i])) {
					closedAlgs++;
				}
			}
			
			CLOSED_NAMES = new String[closedAlgs];
			int index = 0;
			for (int i = 0; i < values.length; i++) {
				if (isClosed(values[i])) {
					CLOSED_NAMES[index] = values[i].toString();
					index++;
				}
			}
		}
		return CLOSED_NAMES;
	}
	
	/**
	 * Returns an array of all closed SolverAlgorithm values
	 */
	public static SolverAlgorithm[] closedValues() {
		if (CLOSED_VALUES == null) {
			SolverAlgorithm[] values = values();
			int closedAlgs = 0;
			for (int i = 0; i < values.length; i++) {
				if (isClosed(values[i])) {
					closedAlgs++;
				}
			}
			
			CLOSED_VALUES = new SolverAlgorithm[closedAlgs];
			int index = 0;
			for (int i = 0; i < values.length; i++) {
				if (isClosed(values[i])) {
					CLOSED_VALUES[index] = values[i];
					index++;
				}
			}
		}
		return CLOSED_VALUES;
	}
	
	public static int noOfExactAlgs() {
		SolverAlgorithm[] values = values();
		int exactAlgs = 0;
		for (int i = 0; i < values.length; i++) {
			if (isExact(values[i])) {
				exactAlgs++;
			}
		}
		return exactAlgs;
	}
}
