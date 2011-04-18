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
 * This class places orderly (hopefully) the labels on the saturation
 * sector graph
 * 
 * @author Sebastiano Spicuglia
 * 
 */
public class Sectors2DPlacer extends LabelPlacer {

	private static final int DISTANCE_BETWEEN_TWO_LABELS = 5;

	public Sectors2DPlacer(ArrayList<String> labels, ArrayList<DPoint> points) {
		super(labels, points);
	}

	public void place(Graphics2D g, int xCoord) {
		int i;
		int previousY, newY;
		String l;
		DPoint p;
		FontMetrics fm;

		p = points.get(0);
		l = labels.get(0);
		previousY = Integer.MAX_VALUE;
		for (i = 0; i < labels.size(); i++) {
			l = labels.get(labels.size() - i - 1);
			p = points.get(points.size() - i - 1);
			fm = g.getFontMetrics();

			if (fm != null) {
				if (p.getY() + fm.getStringBounds(l, g).getHeight()
						+ DISTANCE_BETWEEN_TWO_LABELS > previousY) {
					// overlapping
					newY = (int) (previousY
							- fm.getStringBounds(l, g).getHeight() - DISTANCE_BETWEEN_TWO_LABELS);
				} else {
					newY = (int) p.getY();
				}
				g.drawString(l, xCoord + 1,
						(int) (newY + fm.getStringBounds(l, g).getHeight() / 4));
			} else {
				if (p.getY() + DISTANCE_BETWEEN_TWO_LABELS > previousY) {
					// overlapping
					newY = (int) (previousY - DISTANCE_BETWEEN_TWO_LABELS);
				} else {
					newY = (int) p.getY();
				}
				g.drawString(l, xCoord + 1, (int) (newY));
			}
			g.draw(new Line2D.Double(p.getX(), p.getY(), xCoord, newY));
			previousY = newY;
		}
	}
}
