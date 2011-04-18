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

import java.util.ArrayList;

import jmt.engine.jaba.DPoint;

/**
 * This class places orderly (hopefully) the labels on the y-axis of a plane
 * 
 * @author Sebastiano Spicuglia
 * 
 */

public class YAxisPlacer extends LabelPlacer {

	public YAxisPlacer(ArrayList<String> labels, ArrayList<DPoint> points) {
		super(labels, points);
	}

	public void place(Graphics2D g, int xCoord) {
		int i;
		int newY;
		String l;
		DPoint p;
		FontMetrics fm;
		
		if(points == null || points.size()==0)
			return;

		p = points.get(0);
		l = labels.get(0);
		newY = 0;
		for (i = 0; i < labels.size(); i++) {
			l = labels.get(labels.size() - i - 1);
			p = points.get(points.size() - i - 1);
			fm = g.getFontMetrics();
			// if (p.getY() + fm.getStringBounds(l, g).getHeight()
			// + DistanceBetweenTwoLabels > previousY) {
			// overlapping
			// } else {
			//
			// }
			newY = (int) p.getY();
			g.drawString(l, xCoord, (int) (newY + fm.getStringBounds(l, g)
					.getHeight() / 4));
			// previousY = newY;
		}
	}
}
