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

package jmt.engine.simEngine;

import java.util.Vector;

import org.w3c.dom.Document;

//Todo Verificare la correttezza e il significato di quanto scritto, specie l'ultima frase
//Viene utilizzata come deposito temporaneo di misure tra engine e gui; è invocata da Getter tramite il mediator della gui.

/**
 * It's a queue that allows to control the progress of the Engine: all measures of the
 * Model are put inside the queue; after being read, the measures are deleted.
 * Accessing data in synchronized mode assures that data are read in correct order.
 * It also allows to control the progress of the simulation
 * taking control of the queue from another thread pause the simulation!
 *
 * @author Federico Granata
 */
public class QueueMeasure {

	private Vector data;
	private boolean blocked = false;

	private boolean end;

	public QueueMeasure() {
		data = new Vector();
		end = false;
	}

	/**
	 * Inserts a new document in the vector.
	 * @param elem The element to be inserted.
	 */
	public synchronized void put(Document elem) {
		if (blocked) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		data.add(elem);
		//		valueSet = true;
		notify();
	}

	/**
	 * Deletes and returns all the documents contained in QueueMeasure
	 *
	 */

	public synchronized Document[] get() {
		//		if (!valueSet || blocked)
		if (blocked) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		notify();
		if (data.size() > 0) {
			Document[] measures = (Document[]) data.toArray(new Document[data.size()]);
			data.removeAllElements();
			return measures;
		}
		return null;
	}

	/**
	 * Blocks the QueueMeasure object for synchronized access to data.
	 */
	public synchronized void block() {
		blocked = true;
	}

	/**
	 * Unblocks the QueueMeasure object.
	 */
	public synchronized void unblock() {
		blocked = false;
		notifyAll();
	}

	/**
	 * Sets the end property, which indicates if the measure has finished.
	 * @param end
	 */

	public void setEnd(boolean end) {
		this.end = end;
	}

	/**
	 * Checks if the measure has finished
	 * @return true if finished, false otherwise.
	 */
	public boolean isEnd() {
		return end;
	}

}
