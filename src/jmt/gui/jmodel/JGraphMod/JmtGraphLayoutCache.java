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
  
package jmt.gui.jmodel.JGraphMod;

import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewFactory;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * An object that defines the view of a graphmodel. This object
 * maps between model cells and views and provides a set of methods
 * to change these views.
 * The view may also contain its own set of attributes and is therefore
 * an extension of an Observable, which may be observed by the GraphUI.
 * It uses the model to send its changes to the command history.
 *

 * @author Federico Granata
 * Date: 14-lug-2003
 * Time: 14.18.17

 * Modyfied by Bertoli Marco to support JGraph 5.8 - 21/mar/2006
 
 */
public class JmtGraphLayoutCache extends GraphLayoutCache {

	/**
	 * Constructs a view for the specified model that uses
	 * <code>factory</code> to create its views.
	 *
	 * @param model the model that constitues the data source
	 */
	public JmtGraphLayoutCache(GraphModel model, CellViewFactory factory) {
		super(model, factory);
	}


	/**
	 * Return all cells that intersect the given rectangle.
	 */
	public CellView[] getRoots(Rectangle clip) {
		java.util.List result = new ArrayList();
		CellView[] views = getRoots();
		Rectangle2D bounds;
		for (int i = 0; i < views.length; i++) {
			bounds = views[i].getBounds();
			if (bounds != null)
				if (bounds.intersects(clip))
					result.add(views[i]);
		}
		views = new CellView[result.size()];
		result.toArray(views);
		return views;
	}
}
