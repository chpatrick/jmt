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
 * This class is an abstraction of an edge ( place where surfaces end )
 * 
 * @author Sebastiano Spicuglia
 * 
 */
public class HullEdge {
	
	private HullVertex	p0, p1;
	private HullFace	f0, f1;
	
	private HullFace	newFace;
	private boolean		delete;
	
	protected HullEdge	next;
	protected HullEdge	prev;
	
	private static long	currentID	= 0;
	private long		id			= currentID++;
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int)(id ^ (id >>> 32));
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
		HullEdge other = (HullEdge)obj;
		if ( id != other.id )
			return false;
		return true;
	}
	

	/**
	 * Creates a new Edge object.
	 */
	public HullEdge() {
		this.f0 = null;
		this.f1 = null;
		this.newFace = null;
		this.p0 = null;
		this.p1 = null;
		this.delete = false;
	}
	

	/**
	 * Returns true if the faces adjacent to this edge are both visible in the
	 * current convex hull, false otherwise.
	 * 
	 * @return
	 */
	protected boolean areAllFacesVisible() {
		return ((f0 != null && f0.isVisible()) && (f1 != null && f1.isVisible()));
	}
	

	/**
	 * Returns the array of the faces adjacent to this edge.
	 * 
	 * @return the array of the faces adjacent to this edge.
	 */
	public HullFace[] getAdjFaces() {
		HullFace[] adjFaces = new HullFace[ 2 ];
		adjFaces[0] = f0;
		adjFaces[1] = f1;
		return adjFaces;
	}
	

	/**
	 * Returns the points that limit this edge.
	 * 
	 * @return the points that limit this edge.
	 */
	public HullVertex[] getEndPoints() {
		HullVertex[] endPoints = new HullVertex[ 2 ];
		endPoints[0] = p0;
		endPoints[1] = p1;
		return endPoints;
	}
	

	/**
	 * Returns the new face.
	 * 
	 * @return the new face.
	 */
	protected HullFace getNewFace() {
		return newFace;
	}
	

	/**
	 * True if this edge is no longer in the convex hull, false otherwise.
	 * 
	 * @return
	 */
	protected boolean isDeleted() {
		return delete;
	}
	

	/**
	 * Returns true if one or more faces adjacent to this edge are visible in
	 * the current convex hull, false otherwise.
	 * 
	 * @return
	 */
	protected boolean isOneFaceVisible() {
		return ((f0 != null && f0.isVisible()) || (f1 != null && f1.isVisible()));
	}
	

	/**
	 * Setter for the delete field
	 * 
	 * @param b
	 *            the value you want to set
	 */
	protected void setDelete( boolean b ) {
		delete = b;
	}
	

	/**
	 * Allows you to set at @j index the value @face in the adjFaces field.
	 * 
	 * @param face
	 *            the value you want to set
	 * @param j
	 *            the index you want to modify
	 */
	protected void setAdjFaceAt( HullFace face, int i ) {
		switch ( i ) {
			case 0: {
				f0 = face;
				break;
			}
			case 1: {
				f1 = face;
				break;
			}
			default: {
				throw new IllegalArgumentException( "Index \"i\" not in {0;1}" );
			}
		}
	}
	

	/**
	 * Setters for endPoints field.
	 * 
	 * @param p0
	 *            first point
	 * @param p1
	 *            second point
	 */
	public void setEndPoints( HullVertex p0, HullVertex p1 ) {
		this.p0 = p0;
		this.p1 = p1;
	}
	

	public HullVertex getEndPoint( int i ) {
		switch ( i ) {
			case 0: {
				return p0;
			}
			case 1: {
				return p1;
			}
			default: {
				throw new IllegalArgumentException( "Index\"i\" not in {0;1}" );
			}
		}
	}
	

	public void setEndPoint( int i, HullVertex v ) {
		switch ( i ) {
			case 0: {
				p0 = v;
				break;
			}
			case 1: {
				p1 = v;
				break;
			}
			default: {
				throw new IllegalArgumentException( "Index\"i\" not in {0;1}" );
			}
		}
	}
	

	public HullFace getFace( int i ) {
		switch ( i ) {
			case 0: {
				return f0;
			}
			case 1: {
				return f1;
			}
			default: {
				throw new IllegalArgumentException( "Index\"i\" not in {0;1}" );
			}
		}
	}
	

	public void setFace( int i, HullFace f ) {
		switch ( i ) {
			case 0: {
				f0 = f;
				break;
			}
			case 1: {
				f1 = f;
				break;
			}
			default: {
				throw new IllegalArgumentException( "Index\"i\" not in {0;1}" );
			}
		}
	}
	

	/**
	 * Setter for first adjacent face
	 * 
	 * @param face
	 *            the face you want to set
	 */
	protected void setFirstAdjFace( HullFace face ) {
		this.f0 = face;
	}
	

	/**
	 * Setter for second adjacent face
	 * 
	 * @param face
	 *            the face you want to set
	 */
	protected void setSecondAdjFace( HullFace face ) {
		this.f1 = face;
	}
	

	/**
	 * Setter for new face field
	 * 
	 * @param face
	 *            the face you want to set
	 */
	protected void setNewFace( HullFace face ) {
		this.newFace = face;
	}
}
