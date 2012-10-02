/**    
  * Copyright (C) 2012, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.analytical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum class for all implemented algorithm solvers
 * 
 * @author Abhimanyu Chugh
 *
 */
public enum SolverAlgorithm {
	EXACT("MVA", true, true),
	//MoM("MoM", true, true),
	CHOW("Chow", true, false),
	BARD_SCHWEITZER("Bard-Schweitzer", true, false),
	AQL("AQL", true, false),
	LINEARIZER("Linearizer", true, false),
	DESOUZA_MUNTZ_LINEARIZER("De Souza-Muntz Linearizer", true, false);
	
	// string representation of the algorithm
	private String algorithmName;
	private boolean closed, exact;
	
	private static final String[] NAMES;
	private static final String[] CLOSED_NAMES;
	private static final SolverAlgorithm[] CLOSED_VALUES;
	private static final Map<String, SolverAlgorithm> REVERSE_MAP;
	static {
		HashMap<String, SolverAlgorithm> revMap = new HashMap<String, SolverAlgorithm>();
		NAMES = new String[SolverAlgorithm.values().length];
		ArrayList<String> closedNames = new ArrayList<String>();
		ArrayList<SolverAlgorithm> closedValues = new ArrayList<SolverAlgorithm>();
		for (int i=0; i<NAMES.length;i++) {
			SolverAlgorithm algo = SolverAlgorithm.values()[i];
			NAMES[i] = SolverAlgorithm.values()[i].toString();
			if (algo.isClosed()) {
				closedNames.add(algo.toString());
				closedValues.add(algo);
			}
			revMap.put(algo.toString(), algo);
		}
		CLOSED_NAMES = closedNames.toArray(new String[closedNames.size()]);
		CLOSED_VALUES = closedValues.toArray(new SolverAlgorithm[closedValues.size()]);
		REVERSE_MAP = Collections.unmodifiableMap(revMap);
	}
	
	private SolverAlgorithm(String algorithmName, boolean closed, boolean exact) {
		this.algorithmName = algorithmName;
		this.closed = closed;
		this.exact = exact;
	}
	
	@Override
	public String toString() {
		return algorithmName;
	}
	
	/**
	 * @return true if this algorithm is closed
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * @return true if this algorithm is exact
	 */
	public boolean isExact() {
		return exact;
	}
	
	/**
	 * Find the enum value that corresponds to the given string
	 */
	public static SolverAlgorithm fromString(String algName) {
		return REVERSE_MAP.get(algName);
	}
	
	/**
	 * Returns an array of string representations of all potential SolverAlgorithm values
	 */
	public static String[] names() {
		return NAMES;
	}
	
	
	/**
	 * Returns an array of string representations of all closed SolverAlgorithm values
	 */
	public static String[] closedNames() {
		return CLOSED_NAMES;
	}
	
	/**
	 * Returns an array of all closed SolverAlgorithm values
	 */
	public static SolverAlgorithm[] closedValues() {
		return CLOSED_VALUES;
	}
	
	public static int noOfExactAlgs() {
		return 1;
	}
}
