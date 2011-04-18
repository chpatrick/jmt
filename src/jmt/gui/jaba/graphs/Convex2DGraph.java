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

package jmt.gui.jaba.graphs;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import jmt.engine.jaba.DPoint;
import jmt.engine.jaba.FinalSect2D;
import jmt.engine.jaba.Station2D;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;
import jmt.gui.jaba.cartesian.CartesianPositivePlane;

import org.freehep.util.export.ExportDialog;

/**
 * This class draws all the part of the graph
 * 
 * @author Carlo Gimondi, modified by Sebastiano Spicuglia
 */

public class Convex2DGraph extends JPanel implements MouseListener,
		MouseMotionListener {
	private static final long serialVersionUID = 1L;

	private static final int GRAPH_LEFT_MARGIN = 60;
	private static final int GRAPH_BOTTOM_MARGIN = 30;
	private static final int GRAPH_RIGHT_MARGIN = 150;
	private static final int GRAPH_TOP_MARGIN = 50;

	private static final int NUM_OF_MARK_ON_X_AXIS = 10;
	private static final int NUM_OF_MARK_ON_Y_AXIS = 5;

	private static final int WIDTH_POINT_SIZE_RATIO = 150;

	private static final Color BGCOLOR = Color.WHITE;
	private static final Color LINES_COLOR = Color.BLACK;

	private static final BasicStroke LINES = new BasicStroke(1);

	private static final int LEFT_MARGIN = 60;
	private static final int BOTTOM_MARGIN = 25;
	private static final int RIGHT_MARGIN = 100;
	private static final int TOP_MARGIN = 50;

	private static final DecimalFormat FORMAT_2_DEC = new DecimalFormat("0.00");
	private static final DecimalFormat FORMAT_4_DEC = new DecimalFormat("0.0000");

	private double xMaxValue;
	private double yMaxValue;
	private JabaModel data;
	private CartesianPositivePlane plane;

	private int mouseButtonPress;
	private Point dragPoint;// Il punto correntemente puntato dal mouse
	private boolean dragging;
	private DPoint selectedPoint;// Il punto del convex hull correntemente
									// selezionato o puntato dal mouse
	private JabaWizard mainWin;
	private ConvexSegment selectedConvexSegment;
	// Popupmenu
	private PlotPopupMenu popup = new PlotPopupMenu();
	// Value of the current zoom
	private float currentScale = 1;
	// Height value panel when currentScale is equals to 1;
	private int normalHeight;
	// Width value panel when currentScale is equals to 1;
	private int normalWidth;

	private boolean showAllLabels;

	/**
	 * Initialize the object from the vector where all the points are sored
	 * 
	 * @param mainWin
	 * 
	 * @param allDominants
	 *            The vector with all the points
	 * @param height
	 *            The height of the window
	 * @param width
	 *            The width of the window
	 */
	public Convex2DGraph(JabaModel data, JabaWizard mainWin) {
		DPoint p;

		this.setBorder(BorderFactory.createEtchedBorder());
		this.setBackground(BGCOLOR);

		addMouseListener(this);
		addMouseMotionListener(this);

		this.mainWin = mainWin;
		this.data = data;
		this.mouseButtonPress = 0;
		this.selectedPoint = null;
		this.selectedConvexSegment = null;
		this.dragging = false;
		this.dragPoint = new Point(0, 0);
		this.xMaxValue = 0;
		this.yMaxValue = 0;
		this.normalHeight = this.getHeight();
		this.normalWidth = this.getWidth();
		this.showAllLabels = true;

		for (int i = 0; i < data.getResults().getAllDominants().size(); i++) {
			p = (DPoint) data.getResults().getAllDominants().get(i);
			if (p.getX() > xMaxValue) {
				xMaxValue = p.getX();
			}

			if (p.getY() > yMaxValue) {
				yMaxValue = p.getY();
			}
		}

	}

	 
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2;
		DPoint graphOrigin;

		g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setColor(LINES_COLOR);
		try {
			graphOrigin = new DPoint(GRAPH_LEFT_MARGIN, getHeight()
					- GRAPH_BOTTOM_MARGIN);
			plane = new CartesianPositivePlane(g2, graphOrigin,
					(int) (getWidth() - GRAPH_LEFT_MARGIN - GRAPH_RIGHT_MARGIN),
					(int) (getHeight() - GRAPH_TOP_MARGIN - GRAPH_BOTTOM_MARGIN),
					xMaxValue, yMaxValue);
		} catch (Exception e) {
			return;
		}
		fillArea(g2, data.getResults().getAllConvex(), Color.orange);
		for (Point2D p : data.getResults().getDominants()) {
			drawMaskedOffRectangle(g2, p, new Color(255, 250, 120));
		}
		drawArea(g2, data.getResults().getAllConvex(), Color.gray);
		if (showAllLabels) {
			drawPoint(g2, data.getResults().getDominants(), Color.blue,
					getPointSize() + 1);
			drawPoint(g2, data.getResults().getDominates(), new Color(15, 185,
					100), getPointSize() + 1);
			drawPoint(g2, data.getResults().getFiltDominants(), Color.black,
					getPointSize() + 1);
			drawPoint(g2, data.getResults().getFiltDominates(), Color.gray,
					getPointSize() + 1);
			drawPoint(g2, data.getResults().getFiltConvex(), Color.black,
					getPointSize() + 1);
			pointLabel(g2, data.getResults().getPoints());
		} else {
			pointLabel(g2, data.getResults().getConvex());
		}
		drawPoint(g2, data.getResults().getConvex(), Color.red,
				getPointSize() + 3);

		if (selectedConvexSegment != null) {
			drawSelectLine(g2, selectedConvexSegment.getP1(),
					selectedConvexSegment.getP2());
		}

		summary(g2);
		/*
		 * // drawSelectLine(g2, lineP1, lineP2, s3d, // data.getClassNames());
		 * drawDominantArrow(g2, data.getResults().getDominants(),
		 * data.getResults().getDominates()); drawFiltArea(g2,
		 * data.getResults().getFilteredArea());
		 */
		g2.setColor(LINES_COLOR);

		ArrayList<DPoint> points = new ArrayList<DPoint>();
		for (int i = 0; i <= NUM_OF_MARK_ON_X_AXIS; i++) {
			points.add(new DPoint(i * xMaxValue / NUM_OF_MARK_ON_X_AXIS, 0));
		}
		plane.drawValuesOnXAxis(points);

		points = new ArrayList<DPoint>();
		for (int i = NUM_OF_MARK_ON_Y_AXIS; i > 0; i--) {
			points.add(new DPoint(0, i * yMaxValue / NUM_OF_MARK_ON_Y_AXIS));
		}
		plane.drawValuesOnYAxis(points);

		plane.draw(data.getClassNames()[0], data.getClassNames()[1]);

	}

	/**
	 * Draw the selected line of the convex hull and the information about it
	 * 
	 * @param g
	 *            Graphic object
	 * @param p1
	 *            The first point of the line
	 * @param p2
	 *            The second point of the line
	 * @param s3d
	 *            Information aboute the classes
	 * @param classNames
	 *            The name of the classes
	 */
	public void drawSelectLine(Graphics2D g, DPoint p1, DPoint p2) {
		if ((p1 != null) && (p2 != null)) {
			// Draw the selected line
			Stroke oldStro = g.getStroke();
			Stroke stroke = new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
			g.setStroke(stroke);
			g.setColor(Color.black);
			g.drawLine(plane.getTrueX(p1.getX()), plane.getTrueY(p1.getY()),
					plane.getTrueX(p2.getX()), plane.getTrueY(p2.getY()));
			g.setStroke(oldStro);

			// Set the middle point
			int x = plane.getTrueX(p2.getX() + (p1.getX() - p2.getX()) / 2);
			int y = plane.getTrueY(p2.getY() + (p1.getY() - p2.getY()) / 2);

			Font label = new Font("Arial", Font.PLAIN, 7 + getPointSize());
			g.setFont(label);

			// Draw the label
			for (int i = 0; i < data.getResults().getSaturationSectors().size(); i++) {
				// Current sector
				FinalSect2D sect = (FinalSect2D) data.getResults()
						.getSaturationSectors().get(i);
				String pb11 = FORMAT_2_DEC.format(sect.getBeta11() * 100);
				String pb12 = FORMAT_2_DEC.format(sect.getBeta1() * 100);
				String pb21 = FORMAT_2_DEC.format(sect.getBeta22() * 100);
				String pb22 = FORMAT_2_DEC.format(sect.getBeta2() * 100);

				if (sect.countStation() < 2) {
					continue;
				}

				Station2D d1 = (sect.getstation()).get(0);
				Station2D d2 = (sect.getstation()).get(1);
				int d1x = (int) (d1.getVert()).getX();
				int d1y = (int) (d1.getVert()).getY();
				int d2x = (int) (d2.getVert()).getX();
				int d2y = (int) (d2.getVert()).getY();
				int p1x = (int) (p1.getX() * 100);
				int p1y = (int) (p1.getY() * 100);
				int p2x = (int) (p2.getX() * 100);
				int p2y = (int) (p2.getY() * 100);
				double t1 = ((p1.getY() - p2.getY()) / ((p2.getX() * p1.getY()) - (p1
						.getX() * p2.getY())));
				double t2 = ((p2.getX() - p1.getX()) / ((p2.getX() * p1.getY()) - (p1
						.getX() * p2.getY())));

				if (((d1x == p1x) && (d1y == p1y) && (d2x == p2x) && (d2y == p2y))
						|| ((d1x == p2x) && (d1y == p2y) && (d2x == p1x) && (d2y == p1y))) {
					g.drawString("X_" + data.getClassNames()[0] + "="
							+ FORMAT_4_DEC.format(t1) + " job/sec", x, y
							- (8 + getPointSize()));
					g.drawString("X_" + data.getClassNames()[1] + "="
							+ FORMAT_4_DEC.format(t2) + " job/sec", x, y);

					g.drawString("U_" + data.getClassNames()[1] + "=% [" + pb22
							+ "," + pb12 + "]", x, y - 2 * (8 + getPointSize()));
					g.drawString("U_" + data.getClassNames()[0] + "=% [" + pb21
							+ "," + pb11 + "]", x, y - 3 * (8 + getPointSize()));
					break;
				}
			}
		}
	}

	/**
	 * It draws that explains the simbols on the graphic
	 * 
	 * @param g
	 *            The graphic object
	 */
	public void summary(Graphics2D g) {
		int size = getPointSize() + 1;
		int x = getWidth() - 53 - (getPointSize() * 5);
		int y = 7 + size;
		int fontSize = 6 + Math.min(getPointSize() + 1, 9);
		Font f = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(f);

		// Potential Bottleneck
		g.setColor(Color.red);
		g.fillOval(5 + (x) - ((size / 2)), (int) (y + (0.5) * fontSize)
				- ((Math.min(getPointSize() + 1, 9) / 2)),
				Math.min(getPointSize() + 1, 9),
				Math.min(getPointSize() + 1, 9));
		g.setColor(Color.black);
		g.drawString("Potential", x + 11 + ((size / 2)), y);
		g.drawString("Bottleneck", x + 11 + ((size / 2)), y + fontSize);
		g.drawString("Stations", x + 11 + ((size / 2)), y + 2 * fontSize);

		// Masked-off
		g.setColor(Color.blue);
		g.fillOval(5 + (x) - ((size / 2)),
				-3 + y + (4) * fontSize - Math.min(getPointSize() + 1, 9),
				Math.min(getPointSize() + 1, 9),
				Math.min(getPointSize() + 1, 9));
		g.setColor(Color.black);
		g.drawString("Masked-off", x + 11 + ((size / 2)), 3 + y + (3)
				* fontSize);
		g.drawString("Stations", x + 11 + ((size / 2)), 3 + y + (4) * fontSize);

		// Dominated
		g.setColor(new Color(15, 185, 100));
		g.fillOval(
				5 + (x) - ((size / 2)),
				6 + y + (((6 * fontSize) + (5 * fontSize)) / 2)
						- Math.min(getPointSize() + 1, 9),
				Math.min(getPointSize() + 1, 9),
				Math.min(getPointSize() + 1, 9));
		g.setColor(Color.black);
		g.drawString("Dominated", x + 11 + ((size / 2)), 6 + y + (5) * fontSize);
		g.drawString("Stations", x + 11 + ((size / 2)), 6 + y + (6) * fontSize);

		// Masked off-Area
		g.drawString("Masked-off", x + 11 + ((size / 2)), 2 + y + (8)
				* fontSize);
		g.drawString("Area", x + 11 + ((size / 2)), 2 + y + (9) * fontSize);
		int xP1 = 4 + x - ((size / 2));
		int yP1 = 3 + y + (8) * fontSize + ((size / 2));
		int xP2 = 4 + x + size;
		int yP2 = 3 + (y + (8) * fontSize) - size;

		Polygon p = twoPointRectangle(xP1, yP1, xP2, yP2);

		g.setColor(Color.orange);
		g.fill(p);
		g.setColor(Color.gray);
		g.draw(p);

		// Dominated Area
		g.setColor(Color.black);
		g.drawString("Dominated", x + 11 + ((size / 2)), 3 + y + (10)
				* fontSize);
		g.drawString("Area", x + 11 + ((size / 2)), 3 + y + (11) * fontSize);
		xP1 = 4 + x - ((size / 2));
		yP1 = 4 + y + (10) * fontSize + ((size / 2));
		xP2 = 4 + x + size;
		yP2 = 4 + (y + (10) * fontSize) - size;

		p = twoPointRectangle(xP1, yP1, xP2, yP2);

		g.setColor(new Color(255, 250, 120));
		g.fill(p);
		g.setColor(Color.gray);
		g.draw(p);
	}

	/**
	 * Create a Polygon that is a rectangle draw between two point
	 * 
	 * @param xP1
	 *            The x of the first point
	 * @param yP1
	 *            The y of the first point
	 * @param xP2
	 *            The x of the second point
	 * @param yP2
	 *            The y of the second point
	 * @return The rectangle in a polygon object
	 */
	public Polygon twoPointRectangle(int xP1, int yP1, int xP2, int yP2) {
		Polygon p = new Polygon();
		p.addPoint(xP1, yP1);
		p.addPoint(xP1, yP2);
		p.addPoint(xP2, yP2);
		p.addPoint(xP2, yP1);

		return p;
	}

	/**
	 * It draws the points contained in a vector. The coordinates of the point
	 * must be insered in a Point2D object
	 * 
	 * @param g
	 *            The graphic object
	 * @param points
	 *            The Vector who contains the points
	 * @param c
	 *            The color of the points
	 */
	public void drawPoint(Graphics g, Vector<Point2D> points, Color c, int size) {
		int sizeTuning = (int) size / 2;

		for (int j = 0; j < points.size(); j++) {
			Point2D p = points.get(j);
			g.setColor(c);
			g.fillOval(plane.getTrueX(p.getX()) - sizeTuning,
					plane.getTrueY(p.getY()) - sizeTuning, size, size);
		}
	}

	/**
	 * It draw a temporary point when a point is moved in another place
	 * 
	 * @param g
	 *            The graphic object
	 * @param p
	 *            The position of the point
	 * @param c
	 *            The color of the point
	 * @param size
	 *            The size of the point
	 */
	public void drawShadowPoint(Graphics2D g, Point p, Color c, int size) {
		g.setColor(c);

		int fontSize = 7 + getPointSize();
		Font f = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(f);
		double x = Math.max(p.getX(), LEFT_MARGIN);
		double y = Math.min(p.getY(), getHeight() - BOTTOM_MARGIN);

		g.drawString(
				"(" + FORMAT_2_DEC.format(getTrueX(x)) + ", "
						+ FORMAT_2_DEC.format(getTrueY(y)) + ")",
				(int) (x - (fontSize * 3)), (int) y - 5 - getPointSize());

		g.drawOval((int) x - (((size / 2))), (int) y - (((size / 2))), size,
				size);

		g.setColor(Color.gray);
		Composite oldComp = g.getComposite();
		Composite alphaComp = AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.3f);
		g.setComposite(alphaComp);
		g.fillOval((int) x - (((size / 2))), (int) y - (((size / 2))), size,
				size);
		g.setComposite(oldComp);
	}

	/**
	 * Return the scale factor
	 * 
	 * @return the scale factor
	 */
	public double getScale() {
		return 1;
	}

	/**
	 * This function draw the area among the axis and the point of the convex
	 * hull
	 * 
	 * @param g
	 *            The graphic object
	 * @param allConvex
	 *            The vector with the points of the convex hull
	 * @param allDominants
	 *            The vector with the dominant points
	 * @param area
	 *            The filter area
	 * @param maskedoff
	 *            The vector with the masked-off points
	 */
	public void drawArea(Graphics2D g, Vector<Point2D> allConvex, Color color) {
		Polygon poly = new Polygon();
		DPoint p;

		poly.addPoint(plane.getTrueX(0), plane.getTrueY(0));
		poly.addPoint(plane.getTrueX(xMaxValue), plane.getTrueY(0));
		// Add the point a the polygon for paint the convex area
		for (int i = 0; i < allConvex.size(); i++) {
			p = (DPoint) allConvex.get(i);
			poly.addPoint(plane.getTrueX(p.getX()), plane.getTrueY(p.getY()));
		}
		poly.addPoint(plane.getTrueX(0), plane.getTrueY(yMaxValue));

		g.setStroke(LINES);
		g.setColor(color);
		g.drawPolygon(poly);
	}

	public void fillArea(Graphics2D g, Vector<Point2D> allConvex, Color color) {
		Polygon poly = new Polygon();
		DPoint p;

		poly.addPoint(plane.getTrueX(0), plane.getTrueY(0));
		poly.addPoint(plane.getTrueX(xMaxValue), plane.getTrueY(0));
		// Add the point a the polygon for paint the convex area
		for (int i = 0; i < allConvex.size(); i++) {
			p = (DPoint) allConvex.get(i);
			poly.addPoint(plane.getTrueX(p.getX()), plane.getTrueY(p.getY()));
		}
		poly.addPoint(plane.getTrueX(0), plane.getTrueY(yMaxValue));

		g.setStroke(LINES);
		g.setColor(color);
		g.fillPolygon(poly);
	}

	public void drawMaskedOffRectangle(Graphics2D g, Point2D point, Color color) {
		Polygon poly = new Polygon();

		poly.addPoint(plane.getTrueX(0), plane.getTrueY(0));
		poly.addPoint(plane.getTrueX(0), plane.getTrueY(point.getY()));
		poly.addPoint(plane.getTrueX(point.getX()),
				plane.getTrueY(point.getY()));
		poly.addPoint(plane.getTrueX(point.getX()), plane.getTrueY(0));

		g.setStroke(LINES);
		g.setColor(color);
		g.fillPolygon(poly);
	}

	/**
	 * Print a label over every point, if the point is select the label contain
	 * the coordinate too
	 * 
	 * @param gra
	 *            The graphic object
	 * @param points
	 *            The vector with all points
	 */
	public void pointLabel(Graphics2D g, Vector<Point2D> points) {
		g.setColor(Color.black);

		// Setting the Font
		int fontSize = 7 + getPointSize();
		Font f = new Font("Arial", Font.PLAIN, fontSize);
		g.setFont(f);

		for (int i = 0; i < points.size(); i++) {
			DPoint p = (DPoint) points.get(i);

			if (selectedPoint == p && dragging) {
				drawShadowPoint(g, dragPoint, Color.BLACK, getPointSize() + 1);
			}
			if (p == selectedPoint) {
				g.drawString(p.getLabel() + " (" + FORMAT_2_DEC.format(p.getX())
						+ ", " + FORMAT_2_DEC.format(p.getY()) + ")",
						plane.getTrueX(p.getX()) - 15, plane.getTrueY(p.getY())
								- 3 - getPointSize());
			} else {
				g.drawString(p.getLabel(), plane.getTrueX(p.getX()) - 15,
						plane.getTrueY(p.getY()) - 3 - getPointSize());

			}
		}

	}

	/**
	 * When is select a Dominat point is draw an arrow from the dominant
	 * 
	 * @param g
	 *            The graphic object
	 * @param dominant
	 *            The vector with the dominant points
	 * @param dominates
	 *            The vector with the dominates points
	 */
	public void drawDominantArrow(Graphics2D g, Vector<Point2D> dominant,
			Vector<Point2D> dominates) {
	}

	/**
	 * Draw a semi-trasparent area that is the filtered area
	 * 
	 * @param g
	 *            The graphic object
	 * @param filteredArea
	 *            The filtered area
	 */
	public void drawFiltArea(Graphics2D g, Area filtArea) {
		// filtArea.
	}

	/**
	 * Return the true x on the graph from the mouse position
	 * 
	 * @param XonScreen
	 *            The x of the point on screen
	 * @return The true x point
	 */
	public double getTrueX(double XonScreen) {
		return (XonScreen - LEFT_MARGIN)
				/ (getWidth() - LEFT_MARGIN - RIGHT_MARGIN) * xMaxValue;
	}

	/**
	 * Return the true y on the graph from the mouse position
	 * 
	 * @param YonScreen
	 *            The y of the point on screen
	 * @return The true y point
	 */
	public double getTrueY(double YonScreen) {
		YonScreen = -(YonScreen - getHeight());
		return (YonScreen - BOTTOM_MARGIN)
				/ (getHeight() - TOP_MARGIN - BOTTOM_MARGIN) * yMaxValue;
	}

	/**
	 * Return the size of the points on screen
	 * 
	 * @return The size of the points
	 */
	public int getPointSize() {
		return getWidth() / WIDTH_POINT_SIZE_RATIO;
	}

	/**
	 * Return true if the point is on a line between the first and the second
	 * point
	 * 
	 * @param p1
	 *            The first point of the line
	 * @param p2
	 *            The second point of the line
	 * @param point
	 *            The point that could be on the line
	 * @return True if the point is on a line between the first and the second
	 *         point
	 */
	public boolean selectLine(DPoint p1, DPoint p2, Point point) {
		Polygon p = new Polygon();
		p.addPoint(plane.getTrueX(p1.getX()) - 3 * getPointSize(),
				plane.getTrueY(p1.getY()) + 3 * getPointSize());
		p.addPoint(plane.getTrueX(p1.getX()) + 3 * getPointSize(),
				plane.getTrueY(p1.getY()) + 3 * getPointSize());
		p.addPoint(plane.getTrueX(p2.getX()) + 3 * getPointSize(),
				plane.getTrueY(p2.getY()) - 3 * getPointSize());
		p.addPoint(plane.getTrueX(p2.getX()) - 3 * getPointSize(),
				plane.getTrueY(p2.getY()) - 3 * getPointSize());

		if (p.contains(point)) {
			return true;
		}
		return false;

	}

	/**
	 * If a generic point and a point on the screen are the same point
	 * 
	 * @param mousePoint
	 *            The point on screen
	 * @param ifPoint
	 *            A generic point
	 * @return If the point on screen and a generic point are the same
	 */
	public boolean theSame(Point mousePoint, DPoint ifPoint, int more) {
		if (Math.pow(mousePoint.getX() - plane.getTrueX(ifPoint.getX()), 2)
				+ Math.pow(mousePoint.getY() - plane.getTrueY(ifPoint.getY()),
						2) <= Math.pow(getPointSize(), 2)) {
			return true;
		}
		return false;
	}

	/**
	 * Zoom the graph
	 * 
	 * @param f
	 * @param abs
	 */
	public void zoom(float perc, boolean restore) {
		// TODO javadoc

		int Height = 0;
		int Width = 0;

		if (!restore) {
			Height = (int) (getHeight() * (1 + perc));
			Width = (int) (getWidth() * (1 + perc));
			currentScale += perc;
		} else {
			Height = (int) (normalHeight);
			Width = (int) (normalWidth);
			currentScale = 1;
		}

		setPreferredSize(new Dimension(Width - 15, Height));
		setSize(new Dimension(Width, Height));
		repaint();
	}

	 
	public void mouseMoved(MouseEvent e) {
		dragPoint = e.getPoint();
		Vector<Point2D> point = data.getResults().getAllPoints();
		DPoint p;

		selectedPoint = null;
		selectedConvexSegment = null;
		dragging = false;
		// If the mouse pass over a point, the mouse change
		for (int i = 0; i < point.size(); i++) {
			p = (DPoint) point.get(i);
			if (theSame(dragPoint, p, 2)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				selectedPoint = p;
				repaint();
				return;
			}
		}
		selectedPoint = null;
		// If the cursor pass over a saturation sector the mouse change
		Vector<Point2D> convex = data.getResults().getAllConvex();
		for (int k = 0; k < convex.size() - 1; k++) {
			DPoint pointA = plane.getTruePoint(convex.get(k));
			DPoint pointB = plane.getTruePoint(convex.get(k + 1));

			Polygon rect = new Polygon();
			rect.addPoint((int) pointA.getX(), (int) pointA.getY() - 16);
			rect.addPoint((int) pointA.getX(), (int) pointA.getY() + 16);
			rect.addPoint((int) pointB.getX(), (int) pointB.getY() - 16);
			rect.addPoint((int) pointB.getX(), (int) pointB.getY() + 16);
			DPoint test = new DPoint(dragPoint.getX(), dragPoint.getY());
			if (rect.contains(test)) {
				// setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				selectedConvexSegment = new ConvexSegment(new DPoint(convex
						.get(k).getX(), convex.get(k).getY()), new DPoint(
						convex.get(k + 1).getX(), convex.get(k + 1).getY()));
				repaint();
				return;
			}

			// /old
			/*
			 * p1 = new DPoint(plane.getX(convex.get(k).getX()),
			 * plane.getY(convex .get(k).getY())); p2 = new
			 * DPoint(plane.getX(convex.get(k + 1).getX()),
			 * plane.getY(convex.get(k + 1).getY())); if
			 * (segment.contains(dragPoint)) {
			 * setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			 * selectedConvexSegment = segment; repaint(); return; }
			 */
		}
		selectedConvexSegment = null;
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		repaint();
	}

	/*
	 * When a button of the mouse is press the position of the cursor and the
	 * number of the button are stored. If the cursor is on a point it is
	 * selected
	 */
	 
	public void mousePressed(MouseEvent e) {

		dragging = false;
		mouseButtonPress = e.getButton();
		// If the cursor is on a point and the left button is press
		// the point is select
		if (e.getButton() == 1) {
			// Select the dominant
			if (selectedPoint != null) {
				selectedPoint.setSelect(true);
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				dragging = true;
			}
		}
		repaint();
	}

	 
	public void mouseReleased(MouseEvent e) {
		double[][][] serviceDemands;
		Vector<Point2D> allPoints;

		if (!dragging || selectedPoint == null)
			return;
		mouseButtonPress = e.getButton();

		if (mouseButtonPress == 1) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			serviceDemands = this.data.getServiceTimes();

			allPoints = data.getResults().getAllPoints();
			for (int k = 0; k < allPoints.size(); k++) {
				if (((DPoint) allPoints.get(k)).equals(selectedPoint)) {
					serviceDemands[k][0][0] = Math
							.max(getTrueX(e.getX()), 0.01);
					serviceDemands[k][1][0] = Math
							.max(getTrueY(e.getY()), 0.01);
					commit(serviceDemands);
					mainWin.solve();
				}
			}
		}
		dragging = false;
		selectedPoint = null;
	}

	/*
	 * If the mouse is moving on the graph the dragPoint is update and the graph
	 * is repaint
	 */
	 
	public void mouseDragged(MouseEvent e) {
		dragging = true;
		dragPoint = e.getPoint();
		repaint();
	}

	/*
	 * When a button of the mouse is released the area between the begine point
	 * and the actual point is filtered or free if the button is right or left.
	 * If a point was selected and drag the point is released inn the new
	 * position
	 */

	/*
	 * If the cursor enter after it exited while that point was dragging the
	 * cursor have to change
	 */
	 
	public void mouseEntered(MouseEvent e) {
		if (selectedPoint != null) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
	}

	 
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * When the mouse's left button is clicked if the cursor is on a point this
	 * point is select, if the cursors is not on a point all points lost the
	 * selection
	 */
	 
	public void mouseClicked(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON3) {
			popup.show(this, ev.getX(), ev.getY());
		} else {
			repaint();
		}
	}

	/**
	 * If a point on the graph change position the new data is commit
	 * 
	 * @param serviceDemands
	 */
	private void commit(double[][][] serviceDemands) {

		synchronized (data) {
			data.setServiceTimes(serviceDemands);
			data.setVisits(data.createUnitaryVisits());
		}
	}

	// --- Methods for popup menu
	// -------------------------------------------------------------
	/**
	 * A simple JPopupMenu used to manage operations on plot. It gives the
	 * choice to zoom in and out on the plot, restore original view and save
	 * plot to images (in EPS or PNG format)
	 */
	protected class PlotPopupMenu extends JPopupMenu {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public JMenuItem restore;
		public JMenuItem zoomIn;
		public JMenuItem zoomOut;
		public JMenuItem saveAs;

		public PlotPopupMenu() {
			restore = new JMenuItem("Original view");
			zoomIn = new JMenuItem("Zoom in");
			zoomOut = new JMenuItem("Zoom out");
			saveAs = new JMenuItem("Save as...");
			this.add(restore);
			this.add(zoomIn);
			this.add(zoomOut);
			this.addSeparator();
			this.add(saveAs);
			addListeners();
		}

		public void addListeners() {
			restore.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					zoom(1, true);
				}
			});

			zoomIn.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					zoom(0.1f, false);
				}
			});

			zoomOut.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					zoom(-0.1f, false);
				}
			});

			saveAs.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					ExportDialog export = new ExportDialog();
					export.showExportDialog((Component) Convex2DGraph.this,
							"Export view as ...",
							(Component) Convex2DGraph.this, "Export");
				}

			});
		}
	}

	public void showAllLabels(boolean b) {
		showAllLabels = b;
		repaint();
	}
}

class ConvexSegment {

	private static final double Error = 1;
	private DPoint p1;
	private DPoint p2;

	public ConvexSegment(DPoint p1, DPoint p2) {
		this.p1 = p1;
		this.p2 = p2;
	}

	public DPoint getP2() {
		return p2;
	}

	public DPoint getP1() {
		return p1;
	}

	public boolean contains(Point p) {
		Rectangle2D area;

		area = new Rectangle2D.Double(p1.getX() - Error, p1.getY() - Error,
				p2.getX() + Error, p2.getY() + Error);
		if (area.contains(p))
			return true;
		return false;
	}

}
