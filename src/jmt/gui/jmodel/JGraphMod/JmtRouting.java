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

import org.jgraph.graph.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**

 * @author Federico Granata
 * Date: 28-nov-2003
 * Time: 12.05.46

 * Heavily modyfied by Bertoli Marco to support JGraph 5.8 - 21/mar/2006

 */
public class JmtRouting implements Edge.Routing {
    private int offset = 20;

    public List route(EdgeView edgeView) {
        List list = new ArrayList();
        int n = edgeView.getPointCount();
        Point2D from = edgeView.getPoint(0);
        // Gets source and target cell
        if (edgeView.getSource() instanceof PortView) {
            from = ((PortView) edgeView.getSource()).getLocation();
        } else if (edgeView.getSource() != null) {
            Rectangle2D b = edgeView.getSource().getBounds();
            from = edgeView.getAttributes().createPoint(b.getCenterX(),
                    b.getCenterY());
        }
        Point2D to = edgeView.getPoint(n - 1);
        CellView trg = edgeView.getTarget();
        if (trg instanceof PortView)
            to = ((PortView) trg).getLocation();
        else if (trg != null) {
            Rectangle2D b = trg.getBounds();
            to = edgeView.getAttributes().createPoint(b.getCenterX(),
                    b.getCenterY());
        }

        if (from != null && to != null) {
            DefaultEdge edge = (DefaultEdge) edgeView.getCell();
            JmtCell source = (JmtCell) ((DefaultPort) edge.getSource())
                    .getParent();
            JmtCell target = (JmtCell) ((DefaultPort) edge.getTarget())
                    .getParent();
            Point2D[] routed;
            int offset = this.offset;

            // Gets bounds for source and target cells
            Rectangle2D sourceBounds = (Rectangle2D) source.getAttributes().get("bounds");
            Rectangle2D targetBounds = (Rectangle2D) target.getAttributes().get("bounds");

            if(!source.isLeftInputCell() && !target.isLeftInputCell())
                offset = -offset;
            if(source == target) {
                //outoRing
                //gets the bounds of the source cell
                routed = new Point2D[4];
                routed[0] = new Point2D.Double(from.getX() + offset,
                        from.getY());
                routed[1] = new Point2D.Double(routed[0].getX(), routed[0].getY() + sourceBounds.getHeight());
                routed[2] = new Point2D.Double(to.getX() - offset , routed[1].getY());
                routed[3] = new Point2D.Double(routed[2].getX(), routed[0].getY());
            } else {
                //the source is on the left of the target
                if((from.getX() + offset * 2 < to.getX() && source.isLeftInputCell()
                        && target.isLeftInputCell()) ||
                        (from.getX() + offset * 2 > to.getX() && !source.isLeftInputCell()
                        && !target.isLeftInputCell())) {
                    routed = new Point2D[2];
                    routed[0] = new Point2D.Double(from.getX() + offset, from.getY());
                    routed[1] = new Point2D.Double(routed[0].getX(),  to.getY());
                } else {
                    routed =  new Point2D[4];
                    routed[0] = new Point2D.Double(from.getX() + offset, from.getY());
                    routed[3] = new Point2D.Double(to.getX() - offset *2, to.getY());
                    double maxY = Math.max(routed[0].getY() + sourceBounds.getHeight(),
                            routed[3].getY() + targetBounds.getBounds().getHeight());
                    routed[1] = new Point2D.Double(routed[0].getX(), maxY);
                    routed[2] = new Point2D.Double(routed[3].getX(), maxY);
                    //checks for the position to not intercept the source
//					if(routed[1].y > routed[2].y && source.isLeftInputCell()) {
//						routed[2].y = routed[1].y;
//					} else {
//						routed[1].y = routed[2].y;
//					}
                    //checks for the position to not intercept the target
                    double x;
                    if(from.getY() < to.getY()) {
                        double right = targetBounds.getX()
                                + (targetBounds.getWidth()  + offset)
                                * (target.isLeftInputCell() ? (1) : (-1));
                        x = Math.max(routed[0].getX(), right);
                        routed[0].setLocation(x, routed[0].getY());
                        routed[1].setLocation(x, routed[1].getY());
                    } else {
                        double left = sourceBounds.getX() - offset * 2;
                        x = Math.min(routed[2].getX(),  left);
                        routed[2].setLocation(x, routed[2].getY());
                        routed[3].setLocation(x, routed[3].getY());
                    }
                }
            }
            //Sets add points
            list.add(from);
            for (int i = 0; i < routed.length; i++) {
                list.add(routed[i]);
            }
            list.add(to);
        }
        return list;
    }

    public int getPreferredLineStyle(EdgeView edgeView) {
        return GraphConstants.STYLE_ORTHOGONAL;
    }
}
