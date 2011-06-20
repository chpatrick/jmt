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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;

import jmt.engine.jaba.DPoint;
import jmt.engine.jaba.FinalSect2D;
import jmt.engine.jaba.Station2D;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.cartesian.CartesianPositivePlane;
import jmt.gui.jaba.label.Sectors2DPlacer;

/**
 * <p>
 * Title: Sectors 2D Panel
 * </p>
 * <p>
 * Description: This panel is used to show saturation sectors on 2-class models.
 * </p>
 * <p>
 * This was reimlemented by Bertoli Marco as original one had strong redraw
 * problems, made strange things with rotations, didn't autoscale and overlapped
 * labels too frequently.
 * </p>
 * 
 * @author Bertoli Marco, Zanzottera Andrea Date: 8-feb-2006 Time: 11.33.48
 *         Modified by Sebastiano Spicuglia
 */
public class Sectors2DGraph extends JabaGraph implements MouseListener,
		MouseMotionListener {

	private static final long serialVersionUID = 1L;

	private static final int GRAPH_LEFT_MARGIN = 60;
	private static final int GRAPH_BOTTOM_MARGIN = 25;
	private static final int GRAPH_TOP_MARGIN = 50;
	private static final int DISTANCE_BETWEEN_GRAPH_AND_LABELS = 100;

	private static final Color SINGLE = new Color(125, 255, 0);
	private static final Color DOUBLE = new Color(0, 125, 255);
	private static final Color MORE = Color.RED;
	private static final Color BGCOLOR = Color.WHITE;

	private static final BasicStroke DOTTED = new BasicStroke(1, 1, 1, 1,
			new float[] { 2f }, 1);
	private static final BasicStroke SECTORS = new BasicStroke(3);
	private static final BasicStroke LINES = new BasicStroke(1);

	private static final Color TOOL_TIP_COLOR = new Color(255, 255, 128);
	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");

	private JabaModel data;
	// The plane where we draw the graph
	private CartesianPositivePlane plane;

	private DPoint tooltip;

	/**
	 * Builds a new Sectors2DPanel
	 * 
	 * @param s3d
	 *            vector with output of Jaba engine
	 * @param classNames
	 *            name of classes to be shown on axis
	 */
	public Sectors2DGraph(JabaModel data) {
		super();
		this.data = data;
		this.setBorder(BorderFactory.createEtchedBorder());
		this.setBackground(BGCOLOR);

		addMouseListener(this);
		addMouseMotionListener(this);

	}

	/**
	 * Overrides default paint method to draw the graph
	 * 
	 * @param g
	 *            graphic object. <b>Must</b> be an instance of Graphics2D
	 */

	public void paint(Graphics g) {
		super.paint(g);

		Sectors2DPlacer placer;
		Vector<Object> s3d;
		Graphics2D g2;

		DPoint graphOrigin;
		ArrayList<String> labels;
		ArrayList<DPoint> labelPoints, valuesOnY, valuesOnX;

		// If g is not instance of Graphic2D, aborts method
		if (g instanceof Graphics2D) {
			g2 = (Graphics2D) g;
		} else {
			return;
		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		try {
			graphOrigin = new DPoint(GRAPH_LEFT_MARGIN,
					(getHeight() - GRAPH_BOTTOM_MARGIN));
			plane = new CartesianPositivePlane(
					g2,
					graphOrigin,
					(int) (getHeight() - GRAPH_TOP_MARGIN - GRAPH_BOTTOM_MARGIN),
					(int) (getHeight() - GRAPH_TOP_MARGIN - GRAPH_BOTTOM_MARGIN),
					1, 1);
		} catch (Exception e) {
			return;
		}
		s3d = data.getResults().getSaturationSectors();
		labels = new ArrayList<String>();
		labelPoints = new ArrayList<DPoint>();
		valuesOnY = new ArrayList<DPoint>();
		valuesOnX = new ArrayList<DPoint>();
		for (int i = 0; i < s3d.size(); i++) {
			String label;
			DPoint sectorBegin, sectorEnd, labelPoint;
			FinalSect2D sect;// Current sector

			sect = (FinalSect2D) s3d.get(i);
			sectorBegin = new DPoint(sect.getBeta1(), sect.getBeta11());
			sectorEnd = new DPoint(sect.getBeta2(), sect.getBeta22());
			// labelPoint is the mean between sectorBegin and sectorEnd
			labelPoint = new DPoint(plane.getTrueX((sectorBegin.getX())
					+ ((sectorEnd.getX() - sectorBegin.getX()) / 2)),
					plane.getTrueY((sectorEnd.getY())
							+ ((sectorBegin.getY() - sectorEnd.getY()) / 2)));

			valuesOnY.add(sectorBegin);
			valuesOnX.add(sectorBegin);

			switch (sect.getstation().size()) {
			case 1:
				g2.setColor(SINGLE);
				break;
			case 2:
				g2.setColor(DOUBLE);
				break;
			default:
				g2.setColor(MORE);
			}

			g2.setStroke(SECTORS);
			plane.drawSegment(sectorBegin, sectorEnd);
			g2.setStroke(DOTTED);
			g2.setColor(Color.BLACK);
			plane.drawProjectionOnTheXAxis(sectorBegin);
			plane.drawProjectionOnTheXAxis(sectorEnd);
			plane.drawProjectionOnTheYAxis(sectorEnd);
			plane.drawProjectionOnTheYAxis(sectorBegin);
			label = labelGenerator(sect.getstation());
			labels.add(label);
			labelPoints.add(labelPoint);

		}
		g2.setColor(Color.BLACK);
		g2.setStroke(LINES);
		// We draw the plane
		plane.draw(data.getClassNames()[0] + " %", data.getClassNames()[1]
				+ " %");
		if (!valuesOnY.contains(new DPoint(0, 1)))
			valuesOnY.add(new DPoint(0, 1));
		if (!valuesOnX.contains(new DPoint(1, 0)))
			valuesOnX.add(new DPoint(1, 0));
		if (!valuesOnX.contains(new DPoint(0, 0)))
			valuesOnX.add(0, new DPoint(0, 0));

		plane.drawValuesOnXAxis(valuesOnX);
		plane.drawValuesOnYAxis(valuesOnY);
		// We draw the labels
		placer = new Sectors2DPlacer(labels, labelPoints);
		placer.place(g2, plane.getTrueX(1) + DISTANCE_BETWEEN_GRAPH_AND_LABELS);

		if (tooltip != null) {
			DPoint graphPoint = plane.getGraphPointFromTruePoint(tooltip);
			String content = FORMATTER.format(graphPoint.getX()) + ", "
					+ FORMATTER.format(graphPoint.getY());
			Rectangle2D bounds = g.getFontMetrics().getStringBounds(content, g);
			g.setColor(TOOL_TIP_COLOR);
			g.fillRect((int) tooltip.getX(), (int) tooltip.getY()
					- (int) bounds.getHeight() + 1, (int) bounds.getWidth(),
					(int) bounds.getHeight() + 2);
			g.setColor(Color.black);
			g.drawString(content, (int) tooltip.getX(), (int) tooltip.getY());
		}

	}

	private String labelGenerator(Vector<Station2D> stations) {
		String label;

		label = "";
		for (int j = 0; j < stations.size(); j++) {
			if (j != 0)
				label += "+";
			label += stations.get(j).getName();
		}
		return label;
	}

	public void mouseDragged(MouseEvent arg0) {

	}

	public void mouseMoved(MouseEvent ev) {
		DPoint test = adjustMousePoint(ev.getX(), ev.getY());

		DPoint pointA = plane.getTruePoint(new DPoint(0, 1));
		DPoint pointB = plane.getTruePoint(new DPoint(1, 0));

		Polygon rect = new Polygon();
		rect.addPoint((int) pointA.getX(), (int) pointA.getY() - 16);
		rect.addPoint((int) pointA.getX(), (int) pointA.getY() + 16);
		rect.addPoint((int) pointB.getX(), (int) pointB.getY() - 16);
		rect.addPoint((int) pointB.getX(), (int) pointB.getY() + 16);
		if (rect.contains(test)) {
			tooltip = test;
			repaint();
			return;
		}
		if (tooltip != null) {
			tooltip = null;
			repaint();
			return;
		}
	}

	public void mouseClicked(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON3) {
			rightClick(ev);
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent ev) {
	}

}
