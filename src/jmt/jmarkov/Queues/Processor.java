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
 * Created on 11-mar-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jmt.jmarkov.Queues;

import jmt.jmarkov.MMQueues;
import jmt.jmarkov.Graphics.Notifier;

/**
 * Rappresenta il "Processore" vero e proprio:
 * preleva i processi dalla coda e li esegue
 * 
 * @author Ernesto
 *
 */
public final class Processor implements Runnable {

	//NEW
	//@author Stefano Omini
	// introduced DEBUG var to skip System.out.println() calls in final release
	private static final boolean DEBUG = false;
	//end NEW

	private MMQueues frame;

	private boolean limited, nomorework;

	private int jobsToDo;

	private Notifier[] n;

	private QueueStack q;

	private QueueLogic ql;

	private long rt = 0; //current process execution time (millisecondi)

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			while (true) {
				//System.out.println("Processor started!");
				if ((jobsToDo > 0) || (!limited)) {
					removeFromQueue();
				} else {
					if (DEBUG) {
						System.out.println("I've ended my work!");
					}
					nomorework = true;

					frame.stopProcessing();
					break;
				}
			}

		} catch (Exception e) {
			//TODO sometimes exception are thrown here, when a finite number of steps is chosen.
			if (DEBUG) {
				System.out.println("Exception in 'Processor.java': " + e.getLocalizedMessage());
			}
		}
	}

	public Processor(QueueLogic ql, QueueStack q) {
		this.ql = ql;
		this.q = q;
		this.jobsToDo = 0;
		this.limited = false;
		this.nomorework = false;
	}

	public Processor(QueueLogic ql, QueueStack q, Notifier n[], int jobsToDo) {
		this.ql = ql;
		this.q = q;
		this.n = n;
		this.jobsToDo = jobsToDo;
		if (jobsToDo == 0) {
			this.limited = false;
		} else {
			this.limited = true;
		}
		this.nomorework = false;
	}

	/**
	 * Rimuove un processo dalla coda per eseguirlo in un tempo dato da
	 * @link getExecutionTime
	 * 
	 */
	private void removeFromQueue() {
		getExecutionTime();
		try {
			q.removeFromQueue();
			notifyGraphics("");
			Thread.sleep(rt);
			if (jobsToDo > 0) {
				jobsToDo--;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void getExecutionTime() {
		rt = (long) ql.getRunTime();
	}

	/**
	 * Notifica un cambiamento alla parte grafica della simulazione
	 * @param gi
	 */
	private void notifyGraphics(String gi) {
		for (int i = 0; i < n.length; i++) {
			n[i].removingFromQ();
			n[i].runningIn(rt);
			//System.out.print("P: runtime: " + rt);
		}
	}

	public boolean haveMoreWorkToDo() {
		return !nomorework;
	}

	/**
	 * @param mf
	 */
	public void setEndAction(MMQueues mf) {
		this.frame = mf;
	}
}
