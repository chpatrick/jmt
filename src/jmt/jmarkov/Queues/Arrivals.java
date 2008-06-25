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

import jmt.jmarkov.Graphics.Notifier;
import jmt.jmarkov.Queues.Exceptions.NoJobsException;

/**
 * Rappresenta i processi che arrivando si aggiungono alla
 * coda
 * 
 * @author Ernesto
 */
public final class Arrivals implements Runnable {

	//NEW
	//@author Stefano Omini
	// introduced DEBUG var to skip System.out.println() calls in final release
	private static final boolean DEBUG = false;
	//end NEW

	private boolean limited;

	private boolean noJobs = true;

	private int jobsToDo = 0;

	private Notifier n[];

	private QueueStack q;

	private long at;

	private QueueLogic ql;

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		//System.out.println("Arrivals started!");
		try {
			while (true) {
				if ((jobsToDo > 0) || (!limited)) {
					getInterarrivalTime();
					while (noJobs) {
						Thread.sleep(1000);
						getInterarrivalTime();
					}
					Thread.sleep(at);
					if (q.size() < ql.getMaxStates() || ql.getMaxStates() == 0) {
						notifyGraphics("");
						addToQueue();
					}
				} else {
					break;
				}
			}
		} catch (Exception e) {
		}

	}

	public Arrivals(QueueLogic ql, QueueStack q) {
		this.ql = ql;
		this.q = q;
		this.jobsToDo = 0;
		this.limited = false;
	}

	public Arrivals(QueueLogic ql, QueueStack q, Notifier n[], int jobsToDo) {
		this.ql = ql;
		this.q = q;
		this.n = n;
		this.jobsToDo = jobsToDo;
		if (jobsToDo == 0) {
			this.limited = false;
		} else {
			this.limited = true;
		}
	}

	/**
	 * Utilizzata per mettere la simulazione in "pausa"
	 * @param p true se si vuole mettere in pausa la simulazione,
	 * false se si vuole riprendere la simulazione
	 * @return true se la chiamata ha avuto effetto
	 */
	public boolean pause(boolean p) {
		return false;
	}

	/**
	 * Serve per aggiungere un processo alla coda
	 *
	 */
	private void addToQueue() {
		q.addToQueue();
		if (jobsToDo > 0) {
			jobsToDo--;
		}
	}

	/**
	 * Notifica un cambiamento alla parte grafica della simulazione
	 * @param gi
	 */
	private void notifyGraphics(String gi) {
		for (int i = 0; i < n.length; i++) {
			//			System.out.println("Calling Graphics " + i);
			n[i].addingToQ(at / 1000.0);

			if (DEBUG) {
				System.out.print("A: arrival time: " + at + "\n");
			}
		}
	}

	/**
	 * Preleva da QueueLogic il prossimo tempo di interarrivo
	 *
	 */
	private void getInterarrivalTime() {
		try {
			at = (long) ql.getArrivalTime();
			//			System.out.println("a new job arrived");
			noJobs = false;
		} catch (NoJobsException e) {
			noJobs = true;

			if (DEBUG) {
				System.out.println("No arrivals!");
			}
		}
	}

}
