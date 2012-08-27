/**    
  * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.gui.exact.panels;

import jmt.analytical.SolverAlgorithm;
import jmt.framework.gui.help.HoverHelp;
import jmt.gui.exact.ExactConstants;
import jmt.gui.exact.ExactModel;
import jmt.gui.exact.ExactWizard;
import jmt.gui.exact.table.ExactTableModel;

/**

 * @author alyf (Andrea Conti)
 * Date: 11-set-2003
 * Time: 23.48.19

 */

/**
 * 6th panel: throughput
 */
public final class ThroughputPanel extends SolutionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[][][] throughput;
	private double[][] classAggr, stationAggr;
	private double[] globalAggr;

	/* EDITED by Abhimanyu Chugh */
	public ThroughputPanel(ExactWizard ew, SolverAlgorithm alg) {
		super(ew, alg);
		helpText = "<html>Throughput</html>";
		name = "Throughput";
	}
	/* END */

	/**
	 * gets status from data object
	 */
	@Override
	protected void sync() {
		super.sync();
		/* EDITED by Abhimanyu Chugh */
		throughput = data.getThroughput(algorithm);
		classAggr = data.getPerClassX(algorithm);
		stationAggr = data.getPerStationX(algorithm);
		globalAggr = data.getGlobalX(algorithm);
		/* END */
	}

	@Override
	protected ExactTableModel getTableModel() {
		return new TPTableModel();
	}

	@Override
	protected String getDescriptionMessage() {
		return ExactConstants.DESCRIPTION_THROUGHPUTS;
	}

	private class TPTableModel extends ExactTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		TPTableModel() {
			prototype = new Double(1000);
			rowHeaderPrototype = "Station1000";
		}

		public int getRowCount() {
			if (throughput == null) {
				return 0;
			}
			//OLD
			/*
			if (stations == 1) return 1;
			return stations;
			*/
			//NEW
			//@author Dall'Orso
			return stations + 1;
			//end NEW
		}

		public int getColumnCount() {
			if (throughput == null) {
				return 0;
			}
			//OLD
			/*
			if (isSingle) return 1;
			return classes;
			*/
			//NEW
			//@author Dall'Orso
			return classes + 1;
			//end NEW
		}

		@Override
		protected Object getRowName(int rowIndex) {
			if (rowIndex == 0) {
				return "<html><i>Aggregate</i></html>";
			} else {
				return stationNames[rowIndex - 1];
			}
		}

		@Override
		public String getColumnName(int index) {
			if (index == 0) {
				return "<html><i>Aggregate</i></html>";
			} else {
				return classNames[index - 1];
			}
		}

		@Override
		protected Object getValueAtImpl(int rowIndex, int columnIndex) {
			double d;
			if (rowIndex == 0 && columnIndex == 0) {
				d = globalAggr[iteration];
			} else if (rowIndex == 0 && columnIndex > 0) {
				d = classAggr[columnIndex - 1][iteration];
			} else if (rowIndex > 0 && columnIndex == 0) {
				d = stationAggr[rowIndex - 1][iteration];
			} else {
				d = throughput[rowIndex - 1][columnIndex - 1][iteration];
			}
			if (d < 0) {
				return null; //causes the renderer to display a gray cell
			}
			return new Double(d);
		}

	}
}
