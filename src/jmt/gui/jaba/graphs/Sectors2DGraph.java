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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
import jmt.gui.jaba.cartesian.CartesianPositivePlane;
import jmt.gui.jaba.label.Sectors2DPlacer;

import org.freehep.util.export.ExportDialog;

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
public class Sectors2DGraph extends JPanel implements MouseListener,
		MouseMotionListener {

	private static final long serialVersionUID = 1L;
	
	private static final int GRAPH_LEFT_MARGIN = 60;
	private static final int GRAPH_BOTTOM_MARGIN = 25;
	// private static final int GraphRightMargin = 100;
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

	// private DPoint equiUPoint;
	private JabaModel data;
	// Popup menu
	private PlotPopupMenu popup = new PlotPopupMenu();
	// Value of the current zoom
	private float currentScale = 1;
	// Height value panel when currentScale is equals to 1;
	private int normalHeight;
	// Width value panel when currentScale is equals to 1;
	private int normalWidth;
	// The plane where we draw the graph
	private CartesianPositivePlane plane;

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
		// this.equiUPoint = equiU;
		this.setBorder(BorderFactory.createEtchedBorder());
		this.setBackground(BGCOLOR);

		normalHeight = this.getHeight();
		normalWidth = this.getWidth();

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
			plane = new CartesianPositivePlane(g2, graphOrigin,
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
		this.repaint();
	}

	  
	public void mouseDragged(MouseEvent arg0) {

	}

	  
	public void mouseMoved(MouseEvent ev) {
	}

	  
	public void mouseClicked(MouseEvent ev) {
		if (ev.getButton() == MouseEvent.BUTTON3) {
			popup.show(this, ev.getX(), ev.getY());
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
					export.showExportDialog((Component) Sectors2DGraph.this,
							"Export view as ...",
							(Component) Sectors2DGraph.this, "Export");
				}

			});
		}
	}

}
