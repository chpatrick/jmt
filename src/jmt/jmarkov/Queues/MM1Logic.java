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
 */
package jmt.jmarkov.Queues;

import jmt.jmarkov.Queues.Exceptions.InfiniteBufferException;
import jmt.jmarkov.Queues.Exceptions.NoJobsException;
import jmt.jmarkov.Queues.Exceptions.NonErgodicException;

import java.util.Random;


/**
 * Contiene gli algoritmi che regolano il funzionamento 
 * di una coda <b>M/M/1</b>, dove <b>lambda</b> e <b>s</b> rappresentano, rispettivamente,
 * i numero di job medio al secondo e il tempo medio di esecuzione di un processo.
 * Ricordiamo che in una coda <b>M/M/1</b> gli arrivi e le esecuzioni sono dei processi di Poisson e che
 * viene eseguito un solo processo per volta 
 * 
 * @author Ernesto
 *
 */
public class MM1Logic implements QueueLogic {


    //NEW
    //@author Stefano Omini
    // introduced DEBUG var to skip System.out.println() calls in final release
    private static final boolean DEBUG = false;
    //end NEW


	private double mult = 1.0;

	/**
	 * media degli arrivi [job/ms]
	 */
	private double lambda;

	/**
	 * media dei tempi di esecuzione [ms]
	 */
	private double s;
	
	/**
	 * Utilizzo della coda [job]
	 */
	
	private Random rnd;

	/**
	 * Inizializza la coda  
	 * @param lambda rappresenta la media del <i>processo di Poisson</i> che regola gli arrivi <b>[job/s]</b>
	 * @param s rappresenta la media del <i>processo di Poisson</i> che regola le esecuzioni <b>[ms]</b>
	 */
	public MM1Logic(double lambda, double s){
		this.lambda = lambda;
		this.s = s;
		rnd = new Random();
	}
	
	/* (non-Javadoc)
	 * @see Queues.QueueLogic#getArrivalTime()
	 */
	public double getArrivalTime() throws NoJobsException {
		if (lambda == 0) throw new NoJobsException();
		return (this.getTime(1000.0 * mult / lambda));
	}

	/* (non-Javadoc)
	 * @see Queues.QueueLogic#getRunTime()
	 */
	public double getRunTime() {
		return(this.getTime(s * 1000.0 * mult ));
	}

	/* (non-Javadoc)
	 * @see Queues.QueueLogic#getStatusProbability(int)
	 */
	public double getStatusProbability(int status) throws NonErgodicException{
		return ((1 - utilization()) * Math.pow(utilization(),(double)status));
	}

	/**
	 * Calcola un valore casuale di tempo per una <i>distribuzione di Poisson</i> con
	 * parametro della distribuzione pari a <b>media</b>.
	 * 
	 * @param media è la media della distribuzione <b>[ms]</b>
	 * @return un tempo "casuale" (in <b>ms</b>) determinato tenendo conto della distribuzione specifica
	 */
	private double getTime(double media) {
		//forma esplicita della legge di distribuzione Poisson
		return((- media * Math.log(rnd.nextDouble())));
		
	}

	/**
	 * inizializza <b>lambda</b> ad un nuovo valore
	 * @param lambda nuovo valore <b>[job/s]</b>
	 */
	public void setLambda(double lambda){
		this.lambda = lambda;
		if (DEBUG) {
            System.out.println("l=" + lambda);
        }
	}
	
	/**
	 * inizializza <b>s</b> ad un nuovo valore
	 * @param s nuovo valore <b>[s]</b>
	 */
	public void setS(double s){
		this.s = s;
		if (DEBUG) {
            System.out.println("s=" + s);
        }
	}
	
	/**
	 * Calcola il numero medio di stati occupati, 
	 * a seconda dei parametri lambda e mu inseriti
	 * 
	 * @return numero medio di stati occupati o 0 se il processo non è ergodico
	 * 
	 * @exception NonErgodicException la coda è non ergodica (U > 1 o U < 0)
	 */
	public double mediaJobs() throws NonErgodicException{
		return (double)(utilization())/(1.0 - utilization());	
	}

	/**
	 * Calcola il traffico offerto in base ai parametri
	 * lambda[job/s] e s[ms]
	 * 
	 * @return
	 */
	public double utilization() throws NonErgodicException{
		if((lambda * s) > 1){
			throw new NonErgodicException();	
		}
		else return (lambda * s);
	}

	/* (non-Javadoc)
	 * @see Queues.QueueLogic#getMaxStates()
	 */
	public int getMaxStates() throws InfiniteBufferException {
		return 0;
	}

	/**
	 * @param buffer
	 */
	public void setMaxStates(int buffer) {	
	}
	
	
	/**
	 * Accelera la simulazione
	 * @param t t = 1 per tempo reale, t > 1 per accelerare
	 */
	public void setTimeMultiplier(double tm){
		//double t = 1/tm;
		if(tm > 100.0) this.mult = 1.0/100.0;
		if(tm < 1.0)  this.mult = 1.0;
		if(tm <= 100.0) this.mult = 1.0/tm;
	}
	
	public double getTimeMultiplier(){
		return 1.0/mult;
	}

	/* (non-Javadoc)
	 * @see Queues.QueueLogic#throughput()
	 */
	public double throughput() throws NonErgodicException {
		return mediaJobs() / responseTime();
	}

	/* (non-Javadoc)
	 * @see Queues.QueueLogic#responseTime()
	 */
	public double responseTime() throws NonErgodicException {
		return 	s / (1.0 - utilization());
	}

	/**
	 * Restituisce il lambda massimo, fissato S, tale che il processo sia
	 * ergodico
	 * @return
	 */
	public double getMaxErgodicLambda(){
		return (1.0 / s);
	}
	
	/**
	 * Restituisce S massimo, fissato lambda, tale che il processo sia
	 * ergodico
	 * @return
	 */
	public double getMaxErgodicS(){
		return (1.0/lambda);
	}
	
	/**
	 * Restituisce l'utilizzo dell'iesimo job
	 * @param lambdai lamda iesimo
	 * @param si s iesimo
	 * @return
	 */
	public static double getUi(double lambdai, double si){
		if ((si / lambdai) < 1.0) return (si / lambdai);
		else return 1.0;
	}

	/**
	 * Restituisce la media dell'iesimo job
	 * @param Ui Utilizzo iesimo 
	 * @return
	 */	
	public static double getNi(double Ui, double buffer){
		if ((Ui <= 1.0) && (Ui >= 0.0))
			return (Ui / (1.0 - Ui));
		return Double.MAX_VALUE;
	}
	
	/**
	 * Restituisce il tempo di risposta iesimo
	 * @param Ui Utilizzo iesimo 
	 * @param si s iesimo
	 * @return 
	 */
	public static double getRi(double Ui, double si){
		if ((Ui <= 1.0) && (Ui >= 0.0))
			return (si / (1.0 - Ui));
		return Double.MAX_VALUE;
	}
	
	public double getLambda(){
		return lambda;
	}
	
	public double getS(){
		return s;
	}
}
