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

package jmt.engine.random;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import jmt.common.exception.IncorrectDistributionParameterException;
import jmt.engine.dataAnalysis.Log;
import jmt.gui.common.controller.DispatcherThread;

/**
 * 
 * Manages the file containing numbers (previously generated).
 * 
 * @author Federico Granata Date: 28-lug-2003 Time: 9.40.51 Significantly
 *         improved by Spicuglia Sebastiano 10:08 28/05/2011
 */
public class ReplayerPar extends AbstractParameter implements Parameter {
	private static final double EPSILON = 0.000001d;
	private static final double DEFAULT_VALUE = 0.000001d; // Used when the file
															// is a bad one.
															
	private static String BAD_FILE_MSG = "<html><body>Replayer: empty file, or file with no valid values, <br /> used 0 values.</body></html>";
	private static String BAD_VALUE_MSG = "<html><body>Replayer: some values of the trace have been discarded <p> (only integer and floating-point numbers allowed).</body></html>";
																
	private String fileName;
	private FileReader fr;
	private BufferedReader in;
	private String[] buffer;
	private int bufferIndex;
	private boolean infiniteLoopRisk;
	private boolean useDefaultValue;

	/**
	 * Creates a ReplayerPar
	 * 
	 * @param fileName
	 *            the file containing the previously generated numbers.
	 */
	public ReplayerPar(String fileName) {
		try {
			fr = new FileReader(fileName);
			in = new BufferedReader(fr);
			this.fileName = fileName;
			infiniteLoopRisk = true;
			useDefaultValue = false;
		} catch (FileNotFoundException e) {
			// The file is a bad one, we will return always the default value.
			notifyError(BAD_FILE_MSG);
			useDefaultValue = true;
		}
	}

	/**
	 * Returns the next number from the file.
	 * 
	 */
	public double getNext() {
		String str = null;
		double result;
		if (useDefaultValue) {
			notifyError(BAD_FILE_MSG);
			return DEFAULT_VALUE;
		}
		try {
			if (buffer == null || bufferIndex >= buffer.length) {
				str = in.readLine();
				if (str == null) {
					if (infiniteLoopRisk) {
						// We have completed one loop and we have not found a
						// good
						// value, so we will use the default value.
						useDefaultValue = true;
						notifyError(BAD_FILE_MSG);
						return getNext();
					}
					// if the EOF is not reached it continues to read from the
					// file else it restarts from the beginning of the file,
					// it's cyclical
					fr.close();
					fr = new FileReader(fileName);
					in = new BufferedReader(fr);
					return getNext();
				}
				buffer = str.split(" ");
				bufferIndex = 0;
			}
		} catch (IOException e) {
			// The file is a bad one, we will return always the default value.
			useDefaultValue = true;
			notifyError(BAD_FILE_MSG);
			return getNext();
		}
		try {
			result = Double.parseDouble(buffer[bufferIndex]);
			infiniteLoopRisk = false; // At least one good value exists,
										// we will not cycle forever.
			bufferIndex++;
			if (result == 0)
				result = EPSILON;
			return result;
		} catch (NumberFormatException e) {
			// buffer[bufferIndex] is not a good value, next please!
			bufferIndex++;
			notifyError(BAD_VALUE_MSG);
			return getNext();
		}
	}
	
	private void notifyError(String msg) {
		if (Thread.currentThread() instanceof DispatcherThread) {
			((DispatcherThread) Thread.currentThread())
					.notifyDetectedMalformedReplayerFile(msg);
		}
	}

	/**
	 * Used only for test.
	 */
	public static boolean test(String fileName) {
		Log writer = new Log(fileName);
		Exponential e = new Exponential();
		ExponentialPar p = null;
		try {
			p = new ExponentialPar(1.0);
		} catch (IncorrectDistributionParameterException e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 5; i++) {
			try {
				writer.write(e.nextRand(p));
			} catch (IncorrectDistributionParameterException e1) {
				e1.printStackTrace();
			}
		}
		writer.close();
		ReplayerPar reader = new ReplayerPar(fileName);
		for (int i = 0; i < 15; i++) {
			System.out.println(reader.getNext());
		}
		return false;
	}
}
