package jmt.manual;

/**
 * 
 * @author Lucia Guglielmetti It is very important that returned string by enum
 *         items is equals to the title of related pdf section
 */
public enum ManualBookmarkers {
	JMVA {
		public String toString() {
			return "2 JMVA (Exact Solution Technique)";
		}
	},
	JSIMgraph {
		public String toString() {
			return "3 JSIMgraph (Simulation - Graphical)";
		}

	},
	JSIMwiz {
		public String toString() {
			return "4 JSIMwiz (Simulation - Textual)";
		}
	},
	JMCH {
		public String toString() {
			return "5 JMCH (Markov Chain)";
		}
	},
	JABA {
		public String toString() {
			return "6 JABA (Asymptotic Bound Analysis)";
		}
	},
	JWAT {
		public String toString() {
			return "7 JWAT - Workload Analyzer Tool";
		}
	},
	
}