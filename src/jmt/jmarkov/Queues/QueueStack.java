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

import java.util.Stack;

/**
 * @author Ernesto
 *
 * Rappresenta la coda vera e propria, a cui si aggiungono i
 * processi in attesa di essere eseguiti
 */
public class QueueStack extends Stack {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int aCounter = 0, exCounter = 0, maxjobs = 0;
	boolean unlimited = true;

	Integer element; //elemento fittizio con cui riempio la coda

	/**
	 * 
	 */
	public QueueStack(int max) {
		super();
		if (max == 0) {
			maxjobs = 0;
			unlimited = true;
		} else {
			maxjobs = max;
			unlimited = false;
		}

	}

	/**
	 * aggiunge un processo alla coda
	 */
	public synchronized void addToQueue() {
		if ((unlimited) || (maxjobs > 0)) {
			super.push(new Integer(0));
			notify();
		} else {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Toglie un processo dalla coda per eseguirlo
	 * @return true se la coda contiene almeno un processo 
	 * in attesa di essere eseguito, false altrimenti
	 */
	public synchronized void removeFromQueue() {
		if (super.empty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		super.pop();

	}

	public void clearQueue() {
		exCounter = 0;
		aCounter = 0;
		super.removeAllElements();
		maxjobs = 0;
		unlimited = true;
	}

	public void moreJobsToDo() {
		if ((unlimited) || (maxjobs > 0)) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
