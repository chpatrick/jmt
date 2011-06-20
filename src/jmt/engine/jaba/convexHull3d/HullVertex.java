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
package jmt.engine.jaba.convexHull3d;

/**
 * This class is an abstraction of a vertex.
 * 
 * @author Sebastiano Spicuglia
 * 
 */
public class HullVertex {
	
	private int				x;
	private int				y;
	private int				z;
	
	private HullEdge		duplicate;
	private boolean			onHull;
	private boolean			mark;
	private int				id	= -1; // -1 (invalid) in projected vertices
	
	protected HullVertex	next;
	protected HullVertex	prev;
	private String name;
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	

	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		HullVertex other = (HullVertex)obj;
		if ( id != other.id )
			return false;
		return true;
	}
	

	/**
	 * Creates a new vertex object
	 * 
	 * @param x
	 *            the x-coord
	 * @param y
	 *            the y-coord
	 * @param z
	 *            the z-coord
	 * @param id
	 *            the index of the related station
	 * @param stationNames 
	 */
	public HullVertex( int x, int y, int z, int id, String name ) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.id = id;
		this.name = name;
		duplicate = null;
		onHull = false;
		mark = false;
	}
	

	/**
	 * True if @a, @b, @c are collinear, false otherwise.
	 * 
	 * @param a
	 *            the first point
	 * @param b
	 *            the second point
	 * @param c
	 *            the third point
	 * @return if @a, @b, @c are collinear
	 */
	public static boolean areCollinear( HullVertex a, HullVertex b, HullVertex c ) {
		return (c.z - a.z) * (b.y - a.y) - (b.z - a.z) * (c.y - a.y) == 0
				&& (b.z - a.z) * (c.x - a.x) - (b.x - a.x) * (c.z - a.z) == 0
				&& (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x) == 0;
	}
	

	/**
	 * Clones this object
	 * 
	 * @return a clone of this object
	 */
	public HullVertex clone() {
		return new HullVertex( x, y, z, id, name);
	}
	

	/**
	 * True if this vertex is on the current hull surface.
	 * 
	 * @return
	 */
	protected boolean isOnHull() {
		return onHull;
	}
	

	/**
	 * True if this vertex is already processed
	 * 
	 * @return
	 */
	protected boolean isProcessed() {
		return mark;
	}
	

	protected HullEdge getDuplicate() {
		return duplicate;
	}
	

	/**
	 * Returns the object ID related to this vertex, or <tt>null</tt> if this
	 * vertex is a projected one.
	 * 
	 * @return The related object ID.
	 */
	public int getID() {
		return id;
	}
	

	/**
	 * Returns the x-coord
	 * 
	 * @return the x-coord
	 */
	public int x() {
		return x;
	}
	

	/**
	 * Returns the y-coord
	 * 
	 * @return the y-coord
	 */
	public int y() {
		return y;
	}
	

	/**
	 * Returns the z-coord
	 * 
	 * @return the z-coord
	 */
	public int z() {
		return z;
	}
	

	/**
	 * Setter for the field onHull
	 * 
	 * @param b
	 *            the value you want to set
	 */
	protected void setOnHull( boolean b ) {
		onHull = b;
	}
	

	/**
	 * Setter for the field duplicate
	 * 
	 * @param edge
	 *            the value you want to set
	 */
	protected void setDuplicate( HullEdge edge ) {
		this.duplicate = edge;
	}
	

	/**
	 * Setter for the field mark
	 * 
	 * @param m
	 *            the value you want to set
	 */
	protected void setProcessed( boolean m ) {
		mark = m;
	}
	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	public String toString() {
		return "Vertex [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
	
}
