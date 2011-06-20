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

package jmt.gui.jaba;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Vector;

import jmt.engine.jaba.DPoint;
import jmt.engine.jaba.convexHull3d.HullFace;

/**
 * Created by IntelliJ IDEA. User: Andrea Date: 14-ott-2005 Time: 12.22.46 To
 * change this template use File | Settings | File Templates.
 */
public class JabaResults {

	private Vector<Object> saturationSectors = new Vector<Object>();
	private DPoint equiUtilizationPoint = null;
	private Vector<Point2D> allConvex;
	private Vector<Point2D> allDominants;
	private Vector<Point2D> dominants;
	private Vector<Point2D> filtDominants;
	private Vector<Point2D> filtConvex;
	private Vector<Point2D> filtDominates;
	private Vector<Point2D> points;
	private Vector<Point2D> dominates;
	private Area filteredArea;
	private Vector<Point2D> convex;
	private Vector<Point2D> allPoints;
	private ArrayList<DPoint>[] util;
	private ArrayList<HullFace> faces;

	public void setSaturationSectors(Vector<Object> results) {
		this.saturationSectors = results;
	}

	public void setEquiUtilizationPoint(DPoint b) {
		equiUtilizationPoint = b;
	}

	public DPoint getEquiUtilizationPoint() {
		return equiUtilizationPoint;
	}

	public Vector<Object> getSaturationSectors() {
		return saturationSectors;
	}

	public boolean hasResults() {
		if (saturationSectors.size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	public Vector<Point2D> getAllDominants() {
		return this.allDominants;
	}

	public void setAllConvex(Vector<Point2D> allConvex) {
		this.allConvex = allConvex;
	}

	public void setAllDominants(Vector<Point2D> allDominants) {
		this.allDominants = allDominants;

	}

	public void setDominants(Vector<Point2D> dominants) {
		this.dominants = dominants;

	}

	public void setFiltDominats(Vector<Point2D> filtDominants) {
		this.filtDominants = filtDominants;

	}

	public void setFiltConvex(Vector<Point2D> filtConvex) {
		this.filtConvex = filtConvex;

	}

	public void setFiltDominates(Vector<Point2D> filtDominates) {
		this.filtDominates = filtDominates;

	}

	public void setPoints(Vector<Point2D> points) {
		this.points = points;

	}

	public void setDominates(Vector<Point2D> dominates) {
		this.dominates = dominates;

	}

	public void setFilteredArea(Area filteredArea) {
		this.filteredArea = filteredArea;

	}

	public Vector<Point2D> getAllConvex() {
		return allConvex;
	}

	public Vector<Point2D> getDominants() {
		return dominants;
	}

	public Vector<Point2D> getFiltDominants() {
		return filtDominants;
	}

	public Vector<Point2D> getFiltConvex() {
		return filtConvex;
	}

	public Vector<Point2D> getFiltDominates() {
		return filtDominates;
	}

	public Vector<Point2D> getPoints() {
		return points;
	}

	public Vector<Point2D> getDominates() {
		return dominates;
	}

	public Area getFilteredArea() {
		return filteredArea;
	}

	public Vector<Point2D> getConvex() {
		return convex;
	}

	public void setConvex(Vector<Point2D> convex) {
		this.convex = convex;
	}

	public Vector<Point2D> getAllPoints() {
		return allPoints;
	}
	
	public void setAllPoints(Vector<Point2D> allPoints) {
		this.allPoints = allPoints;
	}

	public void setUtilization(ArrayList<DPoint>[] util) {
		this.util = util;
	}
	
	public ArrayList<DPoint>[] getUtilization() {
		return util;
	}
	
	public ArrayList<HullFace> getFaces() {
		return faces;
	}

	public void setFaces(ArrayList<HullFace> cHullFaces) {
		this.faces = cHullFaces;
	}

}
