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
 * Created on 5-apr-2004 by Ernesto
 *
 */
package jmt.jmarkov.Queues;

/**
 * MMQueues
 * --------------------------------------
 * 5-apr-2004 - Queues/MM1Data.java
 * 
 * Rappresenta i dati di una coda M/M/1 e M/M/1/k
 * 
 * @author Ernesto
 */
public class MM1Data {

	public double lambdai;
	public double si;
	public double buffer;
	
	/**
	 * 
	 */
	public MM1Data() {
		lambdai = 0.0;
		si = 0.0;
	}
	
	public double getNi(){
		return MM1Logic.getNi(getUi(), buffer);
	}
	
	public double getUi(){
		return MM1Logic.getUi(lambdai, si);
		
	}
	
	public double getRi(){
		return MM1Logic.getRi(getUi(), si);
	}
	
}
