/**    
 * Copyright (C) 2011, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.gui.jaba.cartesian;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;

import jmt.engine.jaba.DPoint;
import jmt.gui.jaba.label.XAxisPlacer;
import jmt.gui.jaba.label.YAxisPlacer;

/**
 * This class manages and draws points in a dimensional cartesian plane. This
 * class draws just the first quadrant of the plane.
 * 
 * @author Sebastiano Spicuglia
 * 
 */
public class CartesianPositivePlane {
	private static int SPACE_BETWEEN_MAX_VALUE_AND_ARROW = 30;
	private static int SPACE_BETWEEN_LABEL_AND_ARROW = 7;
	private static int SPACE_BETWEEN_LEFT_MARGIN_AND_LABEL = 22;
	private static int SPACE_BETWEEN_X_AXIS_AND_LABEL = 15;
	private static int ARROW_WIDTH = 8;
	private static int ARROW_HEIGHT = 5;
	private static DecimalFormat FORMATTER = new DecimalFormat("0.000");

	private Graphics2D g;
	private DPoint origin;
	private int xAxisLenght;
	private int yAxisLenght;
	private double xMaxValue;
	private double yMaxValue;

	/**
	 * Creates a new CartesianPositivePlane object
	 * 
	 * @param g
	 *            the specified Graphics context
	 * @param origin
	 *            the point on g where to place the origin of the plane
	 * @param xAxisLenght
	 *            the x-axis length
	 * @param yAxisLength
	 *            the y-axis length
	 * @param xMaxValue
	 *            the max value of abscissa you want to place on the graph
	 * @param yMaxValue
	 *            the max value of ordinate you want to place on the graph
	 * @throws Exception
	 */
	public CartesianPositivePlane(Graphics2D g, DPoint origin, int xAxisLenght,
			int yAxisLength, double xMaxValue, double yMaxValue)
			throws Exception {
		if (g == null || origin == null)
			throw new Exception();
		this.g = g;
		this.origin = origin;
		this.xAxisLenght = xAxisLenght;
		this.yAxisLenght = yAxisLength;
		this.xMaxValue = xMaxValue;
		this.yMaxValue = yMaxValue;
	}

	/**
	 * Draws the value of the abscissa for all points in the @points list.
	 * 
	 * @param points
	 *            an arraylist of points
	 * @throws Exception
	 */
	public void drawValuesOnXAxis(ArrayList<DPoint> points) {
		int i;
		ArrayList<String> labels;
		ArrayList<DPoint> truePoints;
		XAxisPlacer placer;

		labels = new ArrayList<String>();
		truePoints = new ArrayList<DPoint>();
		for (i = 0; i < points.size(); i++) {
			labels.add(FORMATTER.format(points.get(i).getX()));
			truePoints.add(new DPoint(getTrueX(points.get(i).getX()),
					getTrueY(points.get(i).getY())));
		}
		placer = new XAxisPlacer(labels, truePoints);
		placer.place(g, getTrueY(0) + SPACE_BETWEEN_X_AXIS_AND_LABEL,
				getTrueY(0));
	}
	


	/**
	 * Draws the value of the ordinate for all points in the @points list.
	 * 
	 * @param points
	 *            an arraylist of points
	 */
	public void drawValuesOnYAxis(ArrayList<DPoint> points) {
		if (points.size() == 0)
			return;
		int i;
		ArrayList<String> labels;
		ArrayList<DPoint> truePoints;
		YAxisPlacer placer;

		labels = new ArrayList<String>();
		truePoints = new ArrayList<DPoint>();
		for (i = 0; i < points.size(); i++) {
			labels.add(FORMATTER.format(points.get(i).getY()));
			truePoints.add(new DPoint(getTrueX(points.get(i).getX()),
					getTrueY(points.get(i).getY())));
		}
		placer = new YAxisPlacer(labels, truePoints);
		placer.place(g, SPACE_BETWEEN_LEFT_MARGIN_AND_LABEL);
	}

	/**
	 * Draws the the orthogonal projection of the point @p onto the x-axis
	 * 
	 * @param p
	 *            a point
	 */
	public void drawProjectionOnTheXAxis(DPoint p) {
		if (p == null)
			return;
		g.draw(new Line2D.Double(getTrueX(p.getX()), getTrueY(p.getY()),
				getTrueX(p.getX()), getTrueY(0)));
	}

	/**
	 * Draws the the orthogonal projection of the point @p onto the y-axis
	 * 
	 * @param p
	 *            a point
	 */
	public void drawProjectionOnTheYAxis(DPoint p) {
		if (p == null)
			return;
		g.draw(new Line2D.Double(getTrueX(p.getX()), getTrueY(p.getY()),
				getTrueX(0), getTrueY(p.getY())));
	}

	/**
	 * Draws the plane
	 * 
	 * @param xAxisLabel
	 *            the x-axis label
	 * @param yAxisLabel
	 *            the y-axis label
	 */
	public void draw(String xAxisLabel, String yAxisLabel) {
		Polygon xArrow, yArrow;
		int xLenght, yLenght;

		xLenght = xAxisLenght + SPACE_BETWEEN_MAX_VALUE_AND_ARROW;
		yLenght = yAxisLenght + SPACE_BETWEEN_MAX_VALUE_AND_ARROW;
		// X Axis
		g.drawLine((int) origin.getX(), (int) origin.getY(),
				(int) origin.getX() + xLenght, (int) origin.getY());
		xArrow = new Polygon();
		xArrow.addPoint((int) origin.getX() + xLenght - ARROW_WIDTH,
				(int) origin.getY() - ARROW_HEIGHT);
		xArrow.addPoint((int) origin.getX() + xLenght - ARROW_WIDTH,
				(int) origin.getY() + ARROW_HEIGHT);
		xArrow.addPoint((int) origin.getX() + xLenght, (int) origin.getY());
		g.fillPolygon(xArrow);
		g.draw(xArrow);
		g.drawString(xAxisLabel, (int) origin.getX() + xLenght
				+ SPACE_BETWEEN_LABEL_AND_ARROW, (int) origin.getY()
				+ SPACE_BETWEEN_LABEL_AND_ARROW);

		// Y Axis
		g.drawLine((int) origin.getX(), (int) origin.getY(),
				(int) origin.getX(), (int) origin.getY() - yLenght);
		yArrow = new Polygon();
		yArrow.addPoint((int) origin.getX() - ARROW_HEIGHT, (int) origin.getY()
				- yLenght + ARROW_WIDTH);
		yArrow.addPoint((int) origin.getX() + ARROW_HEIGHT, (int) origin.getY()
				- yLenght + ARROW_WIDTH);
		yArrow.addPoint((int) origin.getX(), (int) origin.getY() - yLenght);
		g.fillPolygon(yArrow);
		g.draw(yArrow);
		g.drawString(yAxisLabel, (int) origin.getX()
				- SPACE_BETWEEN_LABEL_AND_ARROW, (int) origin.getY() - yLenght
				- SPACE_BETWEEN_LABEL_AND_ARROW);
	}

	/**
	 * Draws a segment between two points
	 * 
	 * @param p1
	 *            start point
	 * @param p2
	 *            end point
	 */
	public void drawSegment(DPoint p1, DPoint p2) {
		if (p1 == null || p2 == null)
			return;
		g.draw(new Line2D.Double(getTrueX(p1.getX()), getTrueY(p1.getY()),
				getTrueX(p2.getX()), getTrueY(p2.getY())));
	}

	/**
	 * Returns the x-coodinate of the @x point into the graphics context
	 * 
	 * @param x
	 *            a point
	 * @return the x-coodinate of the @x point into the graphics context
	 */
	public int getTrueX(double x) {
		return (int) (origin.getX() + x / xMaxValue * xAxisLenght);
	}

	/**
	 * Returns the y-coodinate of the @y point into the graphics context
	 * 
	 * @param y
	 *            a point
	 * @return the y-coodinate of the @y point into the graphics context
	 */
	public int getTrueY(double y) {
		return (int) (origin.getY() - y / yMaxValue * yAxisLenght);
	}

	/**
	 * Returns the coodinates of the @p point into the graphics context
	 * 
	 * @param p
	 *            a point
	 * @return the coodinates of the @p point into the graphics context
	 */
	public DPoint getTruePoint(DPoint p) {
		return new DPoint(getTrueX(p.getX()), getTrueY(p.getY()));
	}

	/**
	 * Returns the coodinates of the @p point into the graphics context
	 * 
	 * @param p
	 *            a point
	 * @return the coodinates of the @p point into the graphics context
	 */
	public DPoint getTruePoint(Point2D p) {
		return new DPoint(getTrueX(p.getX()), getTrueY(p.getY()));
	}

	/**
	 * Returns the coodinates of the @p point (on the graphics context) into the
	 * plane
	 * 
	 * @param p
	 *            a point
	 * @return the coodinates of the @p point into the plane
	 */
	public DPoint getGraphPointFromTruePoint(DPoint p) {
		return new DPoint(xMaxValue * (p.getX() - origin.getX()) / xAxisLenght,
				(origin.getY() - p.getY()) * yMaxValue / yAxisLenght);
	}

}
