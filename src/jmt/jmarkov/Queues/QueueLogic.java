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

import jmt.jmarkov.Queues.Exceptions.InfiniteBufferException;
import jmt.jmarkov.Queues.Exceptions.NoJobsException;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;

/**
 * Rappresenta la logica che regola gli arrivi e l'esecuzione dei
 * processi/jobs, ovvero con che modalità sono generati gli arrivi
 * e come sono gestite le esecuzioni dei job
 * 
 * @author Ernesto
 *
 */
public interface QueueLogic {
	
	//methods:
	
	/**
	 * Calcola la probabilità che lo stato sia occupato
	 * @return probabilità che sia occupato
	 * @param status lo stato in questione
	 */
	public double getStatusProbability(int status) throws NonErgodicException;
	
	/**
	 * Calcola il tempo che il job rimarrà in esecuzione in [ms]
	 * @return tempo di esecuzione
	 */
	public double getRunTime();
	
	/**
	 * Calcola il tempo di interarrivo del prossimo job in [ms]
	 * @return tempo di interarrivo
	 */
	public double getArrivalTime() throws NoJobsException;
	
	/**
	 * Restituisce il numero massimo di job ammessi in coda (buffer)
	 * dopo i quali ogni nuovo job viene scartato
	 * @return 0 se il buffer è ideale (infinito), un valore > 0, altrimenti
	 */
	public int getMaxStates() throws InfiniteBufferException;
	
	
	/**
	 * Calcola il numero medio di jobs in coda (N)
	 * 
	 * @return
	 */
	public double mediaJobs() throws NonErgodicException;
	
	/**
	 * Calcola l'Utilizzo medio della coda (U)
	 */
	public double utilization() throws NonErgodicException;
	
	/**
	 * Calcola il Throughput (X) medio
	 * 
	 */
	public double throughput() throws NonErgodicException;

	/**
	 * Calcola il Tempo di Risposta medio (R)
	 * 
	 */
	public double responseTime() throws NonErgodicException;
}
