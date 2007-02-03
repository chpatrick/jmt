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
 * Created on 23-mar-2004 by Ernesto
 *
 */
package jmt.jmarkov.Graphics.constants;

import java.awt.*;

/**
 * MMQueues
 * --------------------------------------
 * 23-mar-2004 - Graphics.constants/drawConstrains.java
 * 
 * Questa interfaccia racchiude tutte le costanti necessarie al disegno delle
 * code
 * 
 * @author Ernesto
 */
public interface DrawConstrains {
	//generiche
	
	/**
	 * Restituisce il gap che separa i bordi dell'area di disegno
	 * dal disegno stesso
	 */
	double getStartingGap();
	
	/**
	 * Restituisce il gap tra due elementi della coda e tra il primo
	 * elemento della coda e il processore
	 */
	double getElementsGap();
	
	/**
	 * Restituisce il tipo di bordo degli elementi
	 */
	Stroke getDrawStroke();
	
	Stroke getBoldStroke();
	
	//font
	/**
	 * Restituisce il font con cui si scriverà nell'area di disegno
	 */
	Font getFont();

	/**
	 * Restituisce il font normale con cui si scriverà nella GUI
	 */
	Font getNormalGUIFont();
	
	/**
	 * Restituisce il font piccolo con cui si scriverà nella GUI
	 */
	Font getSmallGUIFont();
	
	/**
	 * Restituisce il font grande con cui si scriverà nella GUI
	 */
	Font getBigGUIFont();
	
	//coda
	/**
	 * Restituisce la larghezza di un elemento della coda
	 */
	double getElementWidth();
	
	/**
	 * Restituisce l'altezza di un elemento della coda
	 * @return
	 */
	double getElementHeight();
	
	//processore
	/**
	 * Restituisce il raggio del processore
	 */
	double getProcessorRadius();
	
	
	//stati
	/**
	 * Restituisce il raggio degli stati
	 */
	double getStatusRadius();
}
