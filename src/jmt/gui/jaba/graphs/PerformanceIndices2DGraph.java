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
package jmt.gui.jaba.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import javax.swing.BorderFactory;

import jmt.engine.jaba.DPoint;
import jmt.engine.jaba.FinalSect2D;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.cartesian.CartesianPositivePlane;
import jmt.gui.jaba.label.YAxisPlacer;

/**
 * This graph shows the behaviour of the utilization of the resources.
 * 
 * @author Sebastiano Spicuglia
 * 
 */
public class PerformanceIndices2DGraph extends JabaGraph implements
		MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;

	private static final int GRAPH_LEFT_MARGIN = 60;
	private static final int GRAPH_BOTTOM_MARGIN = 30;
	private static final int GRAPH_RIGHT_MARGIN = 150;
	private static final int GRAPH_TOP_MARGIN = 50;

	private static final BasicStroke LINES = new BasicStroke(1);
	private static final BasicStroke BOLD_LINES = new BasicStroke(2);
	private static final BasicStroke DOTTED_LINES = new BasicStroke(1, 1, 1, 1,
			new float[] { 2f }, 1);

	private static final Color BGCOLOR = Color.WHITE;
	private static final Color LINES_COLOR = Color.BLACK;
	private static final Color TOOL_TIP_COLOR = new Color(255, 255, 128);

	private static final DecimalFormat FORMATTER = new DecimalFormat("0.00");

	private CartesianPositivePlane plane;

	private int index = 0;

	private Color defaultColors[] = { Color.blue, Color.green, Color.magenta,
			Color.orange, Color.red };
	// This flag avoids lines change color when we repaint
	// even if data have not changed ( e.g. when we show
	// the coords of points)

	private boolean showStation[];
	private Rectangle stationLabels[];
	private ArrayList<Color> stationColors = new ArrayList<Color>();

	private JabaModel data;

	private int yMaxValue;

	private DPoint tooltip;

	public PerformanceIndices2DGraph(JabaModel data) {
		super();

		this.data = data;

		showStation = new boolean[data.getStations()];
		stationLabels = new Rectangle[data.getStations()];

		Random colorGen = new Random();
		for (int i = 0; i < data.getStations(); i++) {
			if (i < defaultColors.length) {
				stationColors.add(defaultColors[i]);
			} else {
				// The probability of white is
				// (1/255)^3, I rely on probability....
				stationColors.add(new Color(colorGen.nextInt(256), colorGen
						.nextInt(256), colorGen.nextInt(256)));
			}
			showStation[i] = true;
		}

		this.updateYMaxValue();

		this.setBorder(BorderFactory.createEtchedBorder());
		this.setBackground(BGCOLOR);

		addMouseListener(this);
		addMouseMotionListener(this);
	}

	private void updateYMaxValue() {
		switch (index) {
		case 0:
			yMaxValue = 1;
			break;
		case 1:
			plane.draw("% " + data.getClassNames()[0], "Throughput");
			break;
		case 2:
			plane.draw("% " + data.getClassNames()[0], "Response time");
			break;
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
			plane = new CartesianPositivePlane(
					g2,
					graphOrigin,
					(int) (getWidth() - GRAPH_LEFT_MARGIN - GRAPH_RIGHT_MARGIN),
					(int) (getHeight() - GRAPH_TOP_MARGIN - GRAPH_BOTTOM_MARGIN),
					1, yMaxValue);
		} catch (Exception e) {
			return;
		}

		g2.setStroke(DOTTED_LINES);
		ArrayList<DPoint> valuesOnX = new ArrayList<DPoint>();
		Vector<Object> sectors = data.getResults().getSaturationSectors();
		for (int i = 0; i < sectors.size() && sectors.size() != 1; i++) {
			FinalSect2D sect;// Current sector
			sect = (FinalSect2D) sectors.get(i);
			plane.drawProjectionOnTheXAxis(new DPoint(sect.getBeta1(),
					yMaxValue));
			valuesOnX.add(new DPoint(sect.getBeta1(), sect.getBeta11()));
		}
		g2.setStroke(LINES);

		if (!valuesOnX.contains(new DPoint(1, 0)))
			valuesOnX.add(new DPoint(1, 0));
		if (!valuesOnX.contains(new DPoint(0, 0)))
			valuesOnX.add(0, new DPoint(0, 0));
		plane.drawValuesOnXAxis(valuesOnX);

		ArrayList<DPoint>[] util;
		util = data.getResults().getUtilization();
		ArrayList<DPoint> valuesOnLeftY = new ArrayList<DPoint>();
		ArrayList<DPoint> valuesOnRightY = new ArrayList<DPoint>();
		g2.setStroke(BOLD_LINES);

		for (int j = 0; j < data.getStationNames().length; j++) {
			if (!showStation[j])
				continue;
			g.setColor(stationColors.get(j));
			int i;
			for (i = 0; i < util[j].size(); i = i + 2) {
				if (i == 0) {
					valuesOnLeftY.add(util[j].get(i));
				}
				plane.drawSegment(util[j].get(i), util[j].get(i + 1));
			}
			i = i - 2;
			valuesOnRightY.add(util[j].get(i + 1));
		}
		// g2.setColor(Color.black);
		// plane.drawSegment(new DPoint(0, 1), new DPoint(1, 1));
		g2.setStroke(LINES);

		drawSummary(g, stationColors);

		g.setColor(Color.black);
		{// we always draw certain points
			if (!valuesOnLeftY.contains(new DPoint(0, 1)))
				valuesOnLeftY.add(new DPoint(0, 1));
			if (!valuesOnRightY.contains(new DPoint(1, 1)))
				valuesOnRightY.add(new DPoint(1, 1));
		}
		plane.drawValuesOnYAxis(valuesOnLeftY);

		{// we draw the last points in another axis
			ArrayList<String> labels;
			ArrayList<DPoint> truePoints;
			YAxisPlacer placer;
			DecimalFormat formatter = new DecimalFormat("0.000");
			labels = new ArrayList<String>();
			truePoints = new ArrayList<DPoint>();
			for (int i = 0; i < valuesOnRightY.size(); i++) {
				labels.add(formatter.format(valuesOnRightY.get(i).getY()));
				truePoints.add(plane.getTruePoint(valuesOnRightY.get(i)));
			}
			placer = new YAxisPlacer(labels, truePoints);
			placer.place((Graphics2D) g, plane.getTrueX(1) + 10);
			plane.drawSegment(new DPoint(1, 0), new DPoint(1, 1));
		}

		plane.draw("% " + data.getClassNames()[0], "Utilization");

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
		g.dispose();
		this.revalidate();
	}

	private void drawSummary(Graphics g, ArrayList<Color> stationColors) {
		int xBase = this.getWidth() - GRAPH_RIGHT_MARGIN + 60;
		int yBase = 30;
		for (int i = 0; i < data.getStationNames().length; i++) {
			stationLabels[i] = new Rectangle(xBase, yBase
					- (g.getFontMetrics().getHeight() / 2), 8, 8);
			g.setColor(stationColors.get(i));
			if (showStation[i]) {
				g.fillRect(xBase, yBase - (g.getFontMetrics().getHeight() / 2),
						8, 8);
			} else {
				g.setColor(Color.black);
				g.drawRect(xBase, yBase - (g.getFontMetrics().getHeight() / 2),
						8, 8);
			}
			g.setColor(Color.black);
			g.drawString(data.getStationNames()[i], xBase + 10, yBase);
			stationLabels[i] = new Rectangle(xBase, yBase
					- (g.getFontMetrics().getHeight() / 2),
					(int) (xBase + 10 + g.getFontMetrics()
							.getStringBounds(data.getStationNames()[i], g)
							.getWidth()), 8);
			yBase += 15;
		}
	}

	public void drawIndex(int index) {
		this.index = index;
		repaint();
	}

	public void mouseClicked(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON3) {
			super.rightClick(ev);
			return;
		}
		DPoint test = this.adjustMousePoint(ev.getX(), ev.getY());
		for (int i = 0; i < data.getStationNames().length; i++) {
			if (stationLabels[i] != null && stationLabels[i].contains(test)) {
				showStation[i] = !showStation[i];
				repaint();
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent ev) {
		ArrayList<DPoint>[] util;
		util = data.getResults().getUtilization();

		for (int j = 0; j < data.getStationNames().length; j++) {
			DPoint test = this.adjustMousePoint(ev.getX(), ev.getY());
			if (stationLabels[j] != null && stationLabels[j].contains(test)) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				return;
			}
			int i;
			for (i = 0; i < util[j].size(); i = i + 2) {
				if (!showStation[j])
					continue;
				DPoint pointA = plane.getTruePoint(util[j].get(i));
				DPoint pointB = plane.getTruePoint(util[j].get(i + 1));

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
			}
		}
		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		if (tooltip != null) {
			tooltip = null;
			repaint();
		}
	}
}
