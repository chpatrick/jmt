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

/*
 * Created on 29-mar-2004 by Ernesto
 *
 */
package jmt.jmarkov.Graphics;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

/**
 * MMQueues
 * --------------------------------------
 * 29-mar-2004 - Graphics/DataInfoModel.java
 * 
 * @author Ernesto
 */
class DataInfoModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected int NUM_COLUMNS = 3;
	protected int START_NUM_ROWS = 10;
	protected int nextEmptyRow = 0;
	protected int numRows = 0;

	static final public String job = "Job";
	static final public String operation = "operation";
	static final public String time = "time";

	protected Vector data = null;

	public DataInfoModel() {
		data = new Vector();
	}

	public String getColumnName(int column) {
		switch (column) {
			case 0:
				return job;
			case 1:
				return operation;
			case 2:
				return time;
		}
		return "";
	}

	//XXX Should this really be synchronized?
	public synchronized int getColumnCount() {
		return NUM_COLUMNS;
	}

	public synchronized int getRowCount() {
		if (numRows < START_NUM_ROWS) {
			return START_NUM_ROWS;
		} else {
			return numRows;
		}
	}

	public synchronized Object getValueAt(int row, int column) {
		try {
			Vector p = (Vector) data.elementAt(row);
			switch (column) {
				case 0:
					return p.elementAt(0);
				case 1:
					return p.elementAt(1);
				case 2:
					return p.elementAt(2);
			}
		} catch (Exception e) {
		}
		return "";
	}

	public synchronized void addData(Vector playerRecord) {

		data.addElement(playerRecord);
		int index = nextEmptyRow;
		nextEmptyRow++;

		//Notify listeners that the data changed.
		fireTableRowsInserted(index, index);

	}

	public synchronized void clear() {
		int oldNumRows = numRows;

		numRows = START_NUM_ROWS;
		data.removeAllElements();
		nextEmptyRow = 0;

		if (oldNumRows > START_NUM_ROWS) {
			fireTableRowsDeleted(START_NUM_ROWS, oldNumRows - 1);
		}
		fireTableRowsUpdated(0, START_NUM_ROWS - 1);
	}
}
