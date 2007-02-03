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

import org.jgraph.graph.CellView;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jmt.gui.jmodel.JGraphMod.BlockingRegion;

/**
 * Handles all the events when the user is in the select mode
 *

 * @author dekkar (Federico Granata)
 * Date: Jun 20, 2003
 * Time: 10:42:28 AM


 * Modified by Bertoli Marco 28-giu-2005

 */
public class SelectState extends UIStateDefault {

    protected GraphMouseListner ml;//refernce to mouse listner

    /** Creates the select state
     *
     * @param mediator
     * @param ml
     */
    public SelectState(Mediator mediator, GraphMouseListner ml) {
        super(mediator);
        this.ml = ml;
    }

    /**
     * Handles press event, it selects the cell that is under the pointer
     * if there is no cell deselects. There is also the possibility of
     * activating the marquee handler
     *
     * @param e press mouse event
     */
    public void handlePress(MouseEvent e) {
        ml.setHandler(null);
        if (!e.isConsumed() && mediator.isGraphEnabled()) {
            mediator.graphRequestFocus();
            int s = mediator.getTolerance();
            Rectangle2D r = mediator.fromScreen(
                    new Rectangle(e.getX() - s, e.getY() - s, 2 * s, 2 * s));
            Point2D point = mediator.fromScreen(new Point(e.getPoint()));
            if (!(ml.getFocus() != null && ml.getFocus().intersects(mediator.getGraph(), r))) {
                ml.setFocus(null);
            }
            // Avoid toggling of selection between inner components and blocking region
            CellView next = mediator.getNextViewAt(ml.getFocus(), point.getX(), point.getY());
            if (!(ml.getFocus() != null &&
                    next.getCell() instanceof BlockingRegion))
                ml.setCell(next);
            if (ml.getFocus() == null)
                ml.setFocus(ml.getCell());

            if (!mediator.isForceMarqueeEvent(e)) {
                if (e.getClickCount() == mediator.getEditClickCount()
                        && ml.getFocus() != null
                        //&& ml.getFocus().isLeaf()
                        //&& ml.getFocus().getParentView() == null
                        ) {
                    // Start Editing Only if cell is editable - BERTOLI MARCO
                    if (mediator.isCellEditable(ml.getFocus().getCell())) {
                        ml.handleEditTrigger(ml.getFocus().getCell());
                        e.consume();
                        ml.setCell(null);
                    } // Otherwise do nothing - BERTOLI MARCO
                    else
                        e.consume();
                } else if (!mediator.isToggleSelectionEvent(e)) {
                    if (ml.getHandle() != null) {
                        ml.setHandler(ml.getHandle());
                        ml.getHandle().mousePressed(e);
                    }
                    // Immediate Selection
                    if (!e.isConsumed()
                            && ml.getCell() != null
                            && !mediator.isCellSelected(ml.getCell())) {
                        mediator.selectCellForEvent(ml.getCell().getCell(), e);
                        ml.setFocus(ml.getCell());
                        if (ml.getHandle() != null) {
                            ml.getHandle().mousePressed(e);
                            ml.setHandler(ml.getHandle());
                        }
                        e.consume();
                        ml.setCell(null);
                    }
                }
            }

            //Marquee Selection
            if (!e.isConsumed()
                    && (!mediator.isToggleSelectionEvent(e)
                    || ml.getFocus() == null)) {
                if (ml.getMarquee() != null) {
                    ml.getMarquee().mousePressed(e);
                    ml.setHandler(ml.getMarquee());
                }
            }
        }

    }

    public void handleMove(MouseEvent e) {
        if (ml.getPreviousCursor() == null)
            ml.setPreviousCursor(mediator.getGraphCursor());
        if (mediator.isGraphEnabled()) {
            if (ml.getMarquee() != null)
                ml.getMarquee().mouseMoved(e);
            if (ml.getHandle() != null)
                ml.getHandle().mouseMoved(e);
            if (!e.isConsumed() && ml.getPreviousCursor() != null) {
                mediator.setGraphCursor(ml.getPreviousCursor());
                ml.setPreviousCursor(null);
            }
        }
        e.consume();
    }

    public void handleDrag(MouseEvent e) {
        mediator.autoscroll(e.getPoint());
        if (ml.getHandler() != null && ml.getHandler() == ml.getMarquee())
            ml.getMarquee().mouseDragged(e);
        else if (
                ml.getHandler() == null && !mediator.isGraphEditing() && ml.getFocus() != null) {
            if (!mediator.isCellSelected(ml.getFocus().getCell())) {
                mediator.selectCellForEvent(ml.getFocus().getCell(), e);
                ml.setCell(null);
            }
            if (ml.getHandle() != null)
                ml.getHandle().mousePressed(e);
            ml.setHandler(ml.getHandle());
        }
        if (ml.getHandle() != null && ml.getHandler() == ml.getHandle()) {
            // BERTOLI MARCO - Added to avoid dragging of unselected elements (caused bugs)
            if (mediator.getGraph().getSelectionCells().length > 0)
                ml.getHandle().mouseDragged(e);
        }
    }


    public void handleRelease(MouseEvent e) {
        try {
            if (e != null && !e.isConsumed()) {
                if (ml.getHandler() == ml.getMarquee() && ml.getMarquee() != null)
                    ml.getMarquee().mouseReleased(e);
                else if (ml.getHandler() == ml.getHandle() && ml.getHandle() != null) {
                    ml.getHandle().mouseReleased(e);
                }
                if (ml.isDescendant(ml.getCell(), ml.getFocus()) && e.getModifiers() != 0) {
                    // Do not switch to parent if Special Selection
                    ml.setCell(ml.getFocus());

                }

                // Puts selected cells in good place to avoid overlapping
                mediator.putSelectedCellsInGoodPlace();

                if (!e.isConsumed() && ml.getCell() != null) {
                    Object tmp = ml.getCell().getCell();
                    boolean wasSelected = mediator.isCellSelected(tmp);
                    mediator.selectCellForEvent(tmp, e);
                    ml.setFocus(ml.getCell());
                    ml.postProcessSelection(e, tmp, wasSelected);
                }
                // Notify mediator that object can have been placed inside or
                // ouside a blocking region
                mediator.handlesBlockingRegionDrag();
            }
        } finally {
            ml.setHandler(null);
            ml.setCell(null);
        }

    }

    public void handleEnter(MouseEvent e) {
        mediator.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
