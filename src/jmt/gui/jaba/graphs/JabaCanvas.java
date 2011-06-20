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

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;

/**
 * @author Sebastiano Spicuglia
 */
public class JabaCanvas extends JPanel implements AdjustmentListener{
	private static final long serialVersionUID = 1L;
	private JScrollBar hBar = new JScrollBar(JScrollBar.HORIZONTAL);
	private JScrollBar vBar = new JScrollBar(JScrollBar.VERTICAL);
	private JabaGraph graph;
	private int graphWidth;
	private int graphHeight;
	
	public JabaCanvas(JabaGraph graph) {
		graphWidth = this.getWidth();
		graphHeight = this.getHeight();
		this.graph = graph;
		makeBarNice(hBar);
		makeBarNice(vBar);
		hBar.addAdjustmentListener(this);
		vBar.addAdjustmentListener(this);
		this.setLayout(new BorderLayout());
		this.add(hBar, BorderLayout.SOUTH);
		this.add(vBar, BorderLayout.EAST);
		this.add(graph, BorderLayout.CENTER);
		updateBars();
		this.repaint();
	}

	private void makeBarNice(JScrollBar bar) {
	}

	private void updateBars() {
		int deltaWidth = (graphWidth - this.getWidth());
		int deltaHeight = (graphHeight-this.getHeight());
		if( deltaWidth > 0) {
			hBar.setMinimum(0);
			hBar.setMaximum(deltaWidth);
			if(!hBar.isVisible())
				hBar.setValue(0);
			hBar.setVisible(true);
		}else {
			hBar.setVisible(false);
			graph.translateX(0);
		}
		if(deltaHeight>0) {
			vBar.setMinimum(0);
			vBar.setMaximum(deltaHeight);
			if(!vBar.isVisible())
				vBar.setValue(0);
			vBar.setVisible(true);
		}else {
			vBar.setVisible(false);
			graph.translateY(0);
		}
	}

	public void adjustmentValueChanged(AdjustmentEvent ev) {
		if(ev.getSource() == hBar) {
			graph.translateX(ev.getValue());
		} else if(ev.getSource() == vBar) {
			graph.translateY(ev.getValue());
		}		
	}

	public void notifyNewSize(float width, float height) {
		graphWidth = (int) width;
		graphHeight = (int) height;
		updateBars();
	}
	
	
	
	
}
