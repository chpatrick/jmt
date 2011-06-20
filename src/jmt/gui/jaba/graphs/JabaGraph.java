/*   
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import jmt.engine.jaba.DPoint;

import org.freehep.util.export.ExportDialog;

/**
 * @author Sebastiano Spicuglia
 */
public abstract class JabaGraph extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final float MIN_SCALE = 0.25f;
	private static final float MAX_SCALE = 5f;
	// Popup menu
	protected PlotPopupMenu popup = new PlotPopupMenu();
	// Value of the current zoom
	private float currentScale = 1;
	private int yTranslation;
	private int xTranslation;
	protected Point zoomStartPoint;

	public JabaGraph() {
	}

	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2;

		g2 = (Graphics2D) g;
		g2.scale(currentScale, currentScale);
		g2.translate(-xTranslation, -yTranslation);
	}

	public void rightClick(MouseEvent ev) {
		popup.show(this, ev.getX(), ev.getY());
	}

	public void zoom(float perc, boolean restore) {

		if (!restore) {
			currentScale += perc;
			if (currentScale <= MIN_SCALE) {
				popup.zoomOut.setEnabled(false);
				currentScale = MIN_SCALE;
			} else {
				popup.zoomOut.setEnabled(true);
			}
			if (currentScale >= MAX_SCALE) {
				popup.zoomIn.setEnabled(false);
				currentScale = MAX_SCALE;
			} else {
				popup.zoomIn.setEnabled(true);
			}
		} else {
			currentScale = 1;
			popup.zoomOut.setEnabled(true);
			popup.zoomIn.setEnabled(true);
		}
		if (this.getParent() instanceof JabaCanvas) {
			JabaCanvas c = (JabaCanvas) this.getParent();
			c.notifyNewSize(this.getWidth() * currentScale, this.getHeight()
					* currentScale);
		}
		repaint();
	}

	public void translateX(int value) {
		this.xTranslation = value;
		this.repaint();
	}

	public void translateY(int value) {
		this.yTranslation = value;
		this.repaint();
	}

	public DPoint adjustMousePoint(int x, int y) {
		return new DPoint(x / currentScale + xTranslation, y / currentScale
				+ yTranslation);
	}

	public Point adjustMousePoint2(int x, int y) {
		Point res = new Point();
		res.x = (int) (x / currentScale + xTranslation);
		res.y = (int) (y / currentScale + yTranslation);
		return res;
	}


	public Point adjustMousePoint(Point p) {
		Point res = new Point();
		res.x = (int) (p.x / currentScale + xTranslation);
		res.y = (int) (p.y / currentScale + yTranslation);
		return res;
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
					zoom(0.25f, false);
				}
			});

			zoomOut.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					zoom(-0.25f, false);
				}
			});

			saveAs.addActionListener(new AbstractAction() {
				private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
					ExportDialog export = new ExportDialog();
					export.showExportDialog((Component) JabaGraph.this,
							"Export view as ...", (Component) JabaGraph.this,
							"Export");
				}

			});
		}

	}
	}
