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
 * Created on 16-mar-2004 by Ernesto
 *
 */
package jmt.jmarkov.Graphics;

import jmt.jmarkov.Graphics.constants.DrawConstrains;
import jmt.jmarkov.Graphics.constants.DrawNormal;

import javax.swing.*;
import java.util.Date;

/**
 * MMQueues
 * --------------------------------------
 * 16-mar-2004 - Graphics/TANotifier.java
 * 
 * @author Ernesto
 */
public class TANotifier extends JTextArea implements Notifier {
	
	static final int ROWS = 100, COLS = 40;
	long processes,runningProcess,runningTime;
	Date d = new Date();
	/**
	 * 
	 */
	public TANotifier() {
		super(ROWS,COLS);
		this.setFont(new DrawNormal().getFont());
	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#addingToQ()
	 */
	public void addingToQ(double t) {
		this.setRows(ROWS + 1);
		processes++;
		d = new Date();
		this.append("at " + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()
					+ " - job n." + intToString((int) processes) + " is added\n");		
	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#removingFromQ()
	 */
	public void removingFromQ() {
		this.setRows(ROWS + 1);
		runningProcess++;
		d = new Date();
		this.append("at " + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()
					+ " - job n." + intToString((int) runningProcess) + " is removed\n");

	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#runningIn(double)
	 */
	public void runningIn(double t) {
		//verifyFull();
		//this.append("Processo #" + runningProcess + " sarà eseguito in: " + t + "[ms]\n");

	}

	/* (non-Javadoc)
	 * @see Graphics.Notifier#reset()
	 */
	public void reset() {
		processes = 0;
		runningProcess = 0;
		runningTime = 0;
		super.setText("");
		this.setRows(1);
	}
	
	private void verifyFull(){
		if (super.getLineCount() > ROWS - 1) super.setText("");
	}
	
	private String intToString(int i){
		String s = "";
		for(int j = 1000000;j > 0;j = j/10){
			if((int)i < j ){
				s = s + "0";
			}
		}
		return (s + i);
	}

	/**
	 * @param dCst
	 */
	public void changeDrawSettings(DrawConstrains dCst) {
		this.setFont(dCst.getFont());
	}
}
