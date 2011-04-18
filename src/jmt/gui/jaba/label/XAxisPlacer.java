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
package jmt.gui.jaba.label;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import jmt.engine.jaba.DPoint;

/**
 * This class places orderly (hopefully) the labels on the x-axis of a plane
 * 
 * @author Sebastiano Spicuglia
 * 
 */
public class XAxisPlacer extends LabelPlacer {

	private static final int DISTANCE_BETWEEN_TWO_LABELS = 2;
	private static final int MARK_WIDTH = 10;
	private static final int ODD_ADJUSTMENT = 0;

	public XAxisPlacer(ArrayList<String> labels, ArrayList<DPoint> points) {
		super(labels, points);
	}

	public void place(Graphics2D g, int yLabelCoord, int yMarkCoord) {
		int i;
		int previousX, newX, y;
		boolean overlapping;
		String l;
		DPoint p;
		FontMetrics fm;

		p = points.get(0);
		l = labels.get(0);
		previousX = 0;
		overlapping = false;
		for (i = 0; i < labels.size(); i++) {
			l = labels.get(i);
			p = points.get(i);
			fm = g.getFontMetrics();
			newX = (int) (p.getX() - fm.getStringBounds(l, g).getWidth() / 2);
			if (newX < previousX + DISTANCE_BETWEEN_TWO_LABELS && !overlapping) {
				overlapping = true;
				continue;
			} else {
				overlapping = false;
			}
			if (i % 2 == 0)
				y = yLabelCoord;
			else
				y = yLabelCoord + ODD_ADJUSTMENT;
			g.drawString(l, newX, y);
			g.draw(new Line2D.Double(p.getX(), yMarkCoord - MARK_WIDTH / 2, p
					.getX(), yMarkCoord + MARK_WIDTH / 2));
			previousX = (int) (newX + fm.getStringBounds(l, g).getWidth());
		}
	}
}
