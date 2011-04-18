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

package jmt.gui.jaba.link;

import java.awt.geom.Point2D;
import java.util.Vector;

import jmt.common.exception.InputDataException;
import jmt.common.exception.SolverException;
import jmt.engine.jaba.DPoint;
import jmt.engine.jaba.EngineConvex2D;
import jmt.engine.jaba.PerformanceEngine;
import jmt.engine.jaba.SectorEngine;
import jmt.engine.jaba.newPoint;
import jmt.engine.jaba.Hull.ConvexHullException;
import jmt.engine.jaba.Hull.Vertex;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaResults;

/**
 * Server side of the solver interface.<br>
 * This object takes a model, instantiates the correct solver, and solves it.<br>
 * Should probably be rewritten using a different data structure to hold model information

 * Debugged by Bertoli Marco: now uses visits correctly (31-jan-2006)

 */
public class SolverDispatcher {

	private static final boolean DEBUG = false;
	
	public SolverDispatcher() {
	}

	public JabaResults solve(JabaModel model) throws SolverException, InputDataException {

		/* disable all change-checking */
		model.discardResults();
		model.setChanged();

		JabaResults res = null;

		try {
			if (model.getClasses() == 2) {
				res = solve2classes(model);
			} else if (model.getClasses() == 3) {
				res = solve3classes(model);
			}
		} catch (InputDataException rse) {
			throw rse;
		} catch (SolverException se) {
			throw se;
		} catch (Exception e) {
			fail("Unhandled exception", e);
		}

		return res;
	}

	private void fail(String message, Throwable t) throws SolverException {
		if (DEBUG) {
			t.printStackTrace();
		}
		StringBuffer s = new StringBuffer(message);
		if (t != null) {
			s.append("\n");
			s.append(t.toString());
		}

		throw new SolverException(s.toString(), t);
	}

	private JabaResults solve2classes(JabaModel model) throws Exception {
		//Solve two class models
		int stations;
		double[][] visits;
		double[][][] serviceTimes;
		String[] stationNames;
		String[] classNames;
		Vector<newPoint> vertices;
		SectorEngine calc;
		Vector<Object> saturationSects;
		double prop;
		EngineConvex2D convexEngine;
		Vector<Point2D> v;
		
		stations = model.getStations();
		//Solver solver = null;
		stationNames = model.getStationNames();
		classNames = model.getClassNames();
		serviceTimes = model.getServiceTimes();
		visits = model.getVisits();
		vertices = new Vector<newPoint>();
		prop = 100;

		for (int i = 0; i < stations; i++) {
			// Crea il vettore da passare a Calc2D
			//System.out.println(i);
			// il giusto sarebbe moltiplicare per 1000000, ma dà problemi
			int a = (int) (serviceTimes[i][0][0] * prop * visits[i][0]);
			int b = (int) (serviceTimes[i][1][0] * prop * visits[i][1]);
			//System.out.println(i+": "+a+" "+b);
			vertices.addElement(new newPoint(a, b));
		}

		calc = new SectorEngine();
		JabaResults jres = new JabaResults();
		saturationSects = calc.Calc2D(vertices, stationNames, classNames);
		jres.setSaturationSectors(saturationSects);

		v = new Vector<Point2D>();
		for (int k = 0; k < stationNames.length; k++) {
			v.add(new DPoint(serviceTimes[k][0][0],
					serviceTimes[k][1][0], stationNames[k]));
		}
		convexEngine = new EngineConvex2D(v, saturationSects);
		jres.setConvex(convexEngine.getConvex());
		jres.setAllConvex(convexEngine.getAllConvex());
		jres.setAllDominants(convexEngine.getAllDominants());
		jres.setDominants(convexEngine.getDominants());
		jres.setFiltDominats(convexEngine.getFiltDominants());
		jres.setFiltConvex(convexEngine.getFiltConvex());
		jres.setFiltDominates(convexEngine.getFiltDominates());
		jres.setPoints(convexEngine.getPoints());
		jres.setDominates(convexEngine.getDominates());
		jres.setFilteredArea(convexEngine.getFilteredArea());
		jres.setAllPoints(convexEngine.getAllPoints());
		
		PerformanceEngine performanceEngine = new PerformanceEngine(serviceTimes, saturationSects, stationNames);
		
		jres.setUtilization(performanceEngine.getUtilization());
		//jres.setEquiUtilizationPoint(calc.calcEquiUtilizationPoint(vertices));

		/*
		// Per la visualizzazione
		ViewResults vres = new ViewResults();
		vres.ViewRes2D(res);
		*/

		return jres;

	}

	private JabaResults solve3classes(JabaModel model) throws ConvexHullException {

		// Solve 3 class models
		int stations = model.getStations();

		String[] stationNames = model.getStationNames();
		String[] classNames = model.getClassNames();
		double[][][] serviceTimes = model.getServiceTimes();
		double[][] visits = model.getVisits();

		Vector<Vertex> vertices = new Vector<Vertex>();

		double prop;
		prop = JabaConstants.SERVICE_DEMANDS_PROP;

		for (int i = 0; i < stations; i++) {
			// Crea il vettore da passare a Calc3D
			// il giusto sarebbe moltiplicare per 1000000, ma dà problemi
			int a = (int) Math.round((serviceTimes[i][0][0] * prop * visits[i][0]));
			int b = (int) Math.round((serviceTimes[i][1][0] * prop * visits[i][1]));
			int c = (int) Math.round((serviceTimes[i][2][0] * prop * visits[i][2]));
			//System.out.println(i+": "+a+" "+b+" "+c);
			vertices.addElement(new Vertex(a, b, c));
		}

		SectorEngine calc = new SectorEngine();
		Vector<Object> res = calc.Calc3D(vertices, stationNames, classNames);
		//System.out.println("res.size(): "+res.size());
		//ViewResults vres = new ViewResults();
		//System.out.println("vres");

		// Setta i risultati per la GUI
		JabaResults jres = new JabaResults();
		Vector<Object> out = new Vector<Object>();

		for (int i = 0; i < calc.getsett1staz().size(); i++) {
			out.addElement(calc.getsett1staz().get(i));
		}
		for (int i = 0; i < calc.getsett2staz().size(); i++) {
			out.addElement(calc.getsett2staz().get(i));
		}
		for (int i = 0; i < calc.gettriangles().size(); i++) {
			out.addElement(calc.gettriangles().get(i));
		}
		jres.setSaturationSectors(out);

		// Ritorna il risultato
		return jres;
	}

}
