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
  
package jmt.gui.jmodel.controller;

import jmt.gui.jmodel.JGraphMod.JmtCell;
import org.jgraph.graph.PortView;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

//import org.jgraph.JGraph;

/**

 * @author Federico Granata
 * Date: 14-lug-2003
 * Time: 16.59.19

 */
public class ConnectState extends UIStateDefault {

	protected JmtCell startPoint;

	protected Point2D start, current;

	protected PortView port, firstPort, lastPort;

	protected GraphMouseListner ml;

	protected boolean pressed = false; //ture is the button is pressed
//	private final static boolean DEBUG = true;

	public ConnectState(Mediator mediator, GraphMouseListner ml) {
		super(mediator);
		this.ml = ml;
	}

	public void handlePress(MouseEvent e) {
		if (!e.isConsumed()) {
			start = mediator.snap(e.getPoint());
			firstPort = port = getOutPortViewAt(e.getX(), e.getY());
			;
			if (firstPort != null)
				start = mediator.toScreen(firstPort.getLocation(null));
			e.consume();
		}
		pressed = true;
	}

	public void handleExit(MouseEvent e) {
//		super.handleExit(e);
        mediator.setCursor(mediator.getOldCursor());
	}

	public void handleEnter(MouseEvent e) {
//		super.handleEnter(e);
		mediator.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}


	public void handleDrag(MouseEvent e) {
		if (firstPort != null) {
			if (!e.isConsumed()) {
				Graphics2D g = mediator.getGraphGraphics();
				Color bg = mediator.getGraphBackground();
				Color fg = Color.black;
				g.setColor(fg);
				g.setXORMode(bg);
				overlay(g);

				current = mediator.snap(e.getPoint());

				port = getInPortViewAt(e.getX(), e.getY());
				if (port != null)
					current = mediator.toScreen(port.getLocation(null));

				g.setColor(bg);
				g.setXORMode(fg);
				overlay(g);
				e.consume();
			}
		}

	}

	public void handleRelease(MouseEvent e) {
		if (e != null && !e.isConsumed()) {
			PortView end = getInPortViewAt(e.getX(), e.getY());
			if (end != null)
				mediator.connect(start, current, end, firstPort);

			e.consume();
			mediator.graphRepaint();
		}
		firstPort = null;
		port = null;
		start = null;
		current = null;
	}

	/** gets the first portView of the input port of the cell at position
	 *
	 * @param x
	 * @param y
	 * @return portView of the input port
	 */
	protected PortView getInPortViewAt(int x, int y) {
		return mediator.getInPortViewAt(x, y);
	}

	/** gets the first portView of the output port of the cell at position
	 *
	 * @param x
	 * @param y
	 * @return portView of the output port
	 */
	protected PortView getOutPortViewAt(int x, int y) {
		return mediator.getOutPortViewAt(x, y);
	}


	public void overlay(Graphics2D g) {
		if (start != null) {
			if (current != null) {
				g.draw(new Line2D.Double(start.getX(), start.getY(), current.getX(), current.getY()));
            }
		}
	}


}
