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

import org.jgraph.graph.DefaultEdge;

/**
 * <p>Title: JmtEdge connection structure</p>
 * <p>Description: This class is used to connect two elements into JmtGraph. It is designed to
 * store keys of source and target stations that are used when deleting or copying a connection</p>
 * 
 * @author Bertoli Marco
 *         Date: 17-giu-2005
 *         Time: 19.25.45
 */
public class JmtEdge extends DefaultEdge {
    protected Object sourceKey;
    protected Object targetKey;
    public JmtEdge() {
        super("");
    }

    public JmtEdge(Object o) {
        super(o);
    }

    /**
     * Creates a new JmtEdge connecting source to target
     * @param sourceKey key of source station
     * @param targetKey key of target station
     */
    public JmtEdge(Object sourceKey, Object targetKey) {
        this();
        this.sourceKey = sourceKey;
        this.targetKey = targetKey;
    }

    /**
     * Gets source station search key
     * @return source key
     */
    public Object getSourceKey() {
        return sourceKey;
    }

    /**
     * Gets target station search key
     * @return target key
     */
    public Object getTargetKey() {
        return targetKey;
    }

}
