package jmt.engine.jaba.convexHull3d;

import java.util.ArrayList;
import java.util.HashSet;

import jmt.engine.jaba.convexHull3d.exceptions.AllPointsCollinearException;
import jmt.engine.jaba.convexHull3d.exceptions.AllPointsCoplanarExceptions;

/**
 * This class constructs the convex hull for 3-classes model.
 * 
 * We use the algorithm presented in <i>Computational Geometry in C</i> 2th
 * edition by Joseph O'Rourke.
 * 
 * The original algorithm is coded in C language and uses global variables. In
 * order to make the concept of global variables available in Java, we adopt the
 * Singleton pattern for the class ConvexHull3D.
 * 
 * In order to make the code as much as possible similar to the original one we
 * have implemented double linked lists in the C way (next and prev pointer
 * inside the item). Exploiting the protected visibility for the pointer next
 * and prev, we use this approach only inside this package. The result (a list
 * of faces of the convex hull) is converted in a ArrayList before return.
 * 
 * Unless for the aforementioned exceptions the code is as the original version,
 * so for further information consult <i>Computational Geometry in C</i> 2th ed.
 * by Joseph O'Rourke (Cambridge University Press).
 * 
 * @author Sebastiano Spicuglia 19:42 09/05/2011
 * 
 */

public class ConvexHull3D {
	
	// Lists
	private HullVertex	vertices;
	private HullFace	faces;
	private HullEdge	edges;
	
	
	/**
	 * Creates a new ConvexHull3D object
	 */
	public ConvexHull3D() {
	}
	

	/**
	 * Builds the convex hull for the @stations set. It returns the faces
	 * (divided into triangles) of the convex hull.
	 * 
	 * @param stations
	 *            the set of vertices
	 * @param autoProject
	 *            if it is true the method explodes the @stations set, adding to
	 *            it the projection on the planes xy, yz, zx of all points in
	 *            the @stations set.
	 * @return It returns the faces (divided into triangles) of the convex hull.
	 * @throws AllPointsCoplanarExceptions
	 * @throws AllPointsCollinearException
	 */
	public ArrayList<HullFace> buildHull( HashSet<HullVertex> stations, boolean autoProject )
			throws AllPointsCoplanarExceptions, AllPointsCollinearException {
		HullVertex v, vnext;
		
		cleanState();
		createVertices( stations, autoProject );
		doubleTriangle();
		v = vertices;
		do {
			vnext = v.next;
			if ( !v.isProcessed() ) {
				v.setProcessed( true );
				this.addOne( v );
				this.cleanUp();
			}
			v = vnext;
		} while ( v != vertices );
		return makeResultUserFriendly();
	}
	

	/**
	 * Adds the @p edge to the global list of edges
	 * 
	 * @param p
	 *            the edge you want to add
	 */
	protected void addEdge( HullEdge p ) {
		if ( edges != null ) {
			p.next = edges;
			p.prev = edges.prev;
			edges.prev = p;
			p.prev.next = p;
		}
		else {
			edges = p;
			edges.next = p;
			edges.prev = p;
		}
	}
	

	/**
	 * Adds the @p face to the global list of faces
	 * 
	 * @param p
	 *            the face you want to add
	 */
	protected void addFace( HullFace p ) {
		if ( faces != null ) {
			p.next = faces;
			p.prev = faces.prev;
			faces.prev = p;
			p.prev.next = p;
		}
		else {
			faces = p;
			faces.next = p;
			faces.prev = p;
		}
	}
	

	/**
	 * Adds the @p vertex to the current hull. If necessary creates new faces
	 * making the hull enveloping @p point.
	 * 
	 * @param p
	 *            the vertex you want to add
	 * @return false if the @p vertex is already enveloped by the current hull.
	 */
	private boolean addOne( HullVertex p ) {
		HullFace f;
		HullEdge e, temp;
		boolean vis = false;
		
		f = faces;
		do {
			if ( f.volumeSign( p ) < 0 ) {
				f.setVisible( true );
				vis = true;
			}
			f = f.next;
		} while ( f != faces );
		
		if ( !vis ) {
			p.setOnHull( false );
			return false;
		}
		e = edges;
		do {
			temp = e.next;
			if ( e.areAllFacesVisible() ) {
				e.setDelete( true );
			}
			else if ( e.isOneFaceVisible() ) {
				HullFace temp2 = new HullFace( e, p, this );
				e.setNewFace( temp2 );
				addFace( temp2 );
			}
			e = temp;
		} while ( e != edges );
		return true;
	}
	

	/**
	 * Adds the @p vertex to the global list of vertices
	 * 
	 * @param p
	 *            the vertex you want to add
	 */
	protected void addVertex( HullVertex p ) {
		if ( vertices != null ) {
			p.next = vertices;
			p.prev = vertices.prev;
			vertices.prev = p;
			p.prev.next = p;
		}
		else {
			vertices = p;
			vertices.next = p;
			vertices.prev = p;
		}
	}
	

	/**
	 * Removes edges no longer visible from the current hull.
	 */
	private void cleanEdges() {
		HullEdge e, t;
		
		e = edges;
		do {
			if ( e.getNewFace() != null ) {
				if ( e.getFace( 0 ).isVisible() ) {
					e.setFace( 0, e.getNewFace() );
				}
				else {
					e.setFace( 1, e.getNewFace() );
				}
				e.setNewFace( null );
			}
			e = e.next;
		} while ( e != edges );
		
		while ( edges != null && edges.isDeleted() ) {
			e = edges;
			deleteEdge( e );
		}
		e = edges.next;
		do {
			if ( e.isDeleted() ) {
				t = e;
				e = e.next;
				deleteEdge( t );
			}
			else {
				e = e.next;
			}
		} while ( e != edges );
	}
	

	/**
	 * Removes faces no longer visible from the current hull.
	 */
	private void cleanFaces() {
		HullFace f, t;
		
		while ( faces != null && faces.isVisible() ) {
			f = faces;
			deleteFace( f );
		}
		f = faces.next;
		do {
			if ( f.isVisible() ) {
				t = f;
				f = f.next;
				deleteFace( t );
			}
			else
				f = f.next;
		} while ( f != faces );
	}
	

	/**
	 * Reset the object private fields.
	 */
	private void cleanState() {
		vertices = null;
		faces = null;
		edges = null;
	}
	

	/**
	 * Removes elements no longer visible from the current hull.
	 */
	private void cleanUp() {
		cleanEdges();
		cleanFaces();
		cleanVertices();
	}
	

	/**
	 * Removes vertices no longer visible from the current hull.
	 */
	private void cleanVertices() {
		HullEdge e;
		HullVertex v, t;
		
		e = edges;
		do {
			e.getEndPoint( 0 ).setOnHull( true );
			e.getEndPoint( 1 ).setOnHull( true );
			e = e.next;
		} while ( e != edges );
		while ( vertices != null && vertices.isProcessed() && !vertices.isOnHull() ) {
			v = vertices;
			deleteVertex( v );
		}
		v = vertices.next;
		do {
			if ( v.isProcessed() && !v.isOnHull() ) {
				t = v;
				v = v.next;
				deleteVertex( t );
			}
			else {
				v = v.next;
			}
		} while ( v != vertices );
		v = vertices;
		do {
			v.setDuplicate( null );
			v.setOnHull( false );
			v = v.next;
		} while ( v != vertices );
	}
	

	/**
	 * Adds to the global list of vertices all the points in the @stations set.
	 * 
	 * @param stations
	 *            set of points you want add to global list of vertices
	 * @param stationNames 
	 * @param autoProject
	 *            if it is true the method explodes the @stations set, adding to
	 *            it the projection on the planes xy, yz, zx of all points in
	 *            the @station set.
	 */
	private void createVertices( HashSet<HullVertex> stations, boolean autoProject ) {
		if ( autoProject ) { // FIXME
			addVertex( new HullVertex( 0, 0, 0, -1, null ) );
		}
		for ( HullVertex p : stations ) {
			addVertex( p );
			if ( autoProject ) {
				if ( p.x() != 0 ) {
					addVertex( new HullVertex( 0, p.y(), p.z(), -1, null ) );
				}
				if ( p.y() != 0 ) {
					addVertex( new HullVertex( p.x(), 0, p.z(), -1, null ) );
				}
				if ( p.z() != 0 ) {
					addVertex( new HullVertex( p.x(), p.y(), 0, -1, null ) );
				}
				if ( p.x() != 0 && p.y() != 0 ) {
					addVertex( new HullVertex( 0, 0, p.z(), -1,null ) );
				}
				if ( p.x() != 0 && p.z() != 0 ) {
					addVertex( new HullVertex( 0, p.y(), 0, -1,null ) );
				}
				if ( p.y() != 0 && p.z() != 0 ) {
					addVertex( new HullVertex( p.x(), 0, 0, -1, null ) );
				}
			}
		}
	}
	

	/**
	 * Deletes @p edge from the global list of egdes.
	 * 
	 * @param p
	 *            the edge you want to delete
	 */
	private void deleteEdge( HullEdge p ) {
		if ( edges != null ) {
			if ( edges == edges.next )
				edges = null;
			else if ( p == edges )
				edges = edges.next;
			p.next.prev = p.prev;
			p.prev.next = p.next;
		}
	}
	

	/**
	 * Deletes @p face from the global list of faces.
	 * 
	 * @param p
	 *            the face you want to delete
	 */
	private void deleteFace( HullFace p ) {
		if ( faces != null ) {
			if ( faces == faces.next )
				faces = null;
			else if ( p == faces )
				faces = faces.next;
			p.next.prev = p.prev;
			p.prev.next = p.next;
		}
	}
	

	/**
	 * Deletes @p vertex from the global list of vertices.
	 * 
	 * @param p
	 *            the vertex you want to delete
	 */
	private void deleteVertex( HullVertex p ) {
		if ( vertices != null ) {
			if ( vertices == vertices.next )
				vertices = null;
			else if ( p == vertices )
				vertices = vertices.next;
			p.next.prev = p.prev;
			p.prev.next = p.next;
		}
	}
	

	/**
	 * Creates initial politoype(a bihedron).
	 * 
	 * @throws AllPointsCoplanarExceptions
	 * @throws AllPointsCollinearException
	 */
	private void doubleTriangle() throws AllPointsCoplanarExceptions, AllPointsCollinearException {
		HullVertex v0, v1, v2, v3;
		HullFace f0, f1 = null;
		int vol;
		
		v0 = vertices;
		while ( HullVertex.areCollinear( v0, v0.next, v0.next.next ) ) {
			v0 = v0.next;
			if ( v0 == vertices ) {
				throw new AllPointsCollinearException( "All points are collinear." );
			}
		}
		v1 = v0.next;
		v2 = v1.next;
		v0.setProcessed( true );
		v1.setProcessed( true );
		v2.setProcessed( true );
		
		f0 = new HullFace( v0, v1, v2, f1, this );
		f1 = new HullFace( v2, v1, v0, f0, this );
		addFace( f0 );
		addFace( f1 );
		
		f0.setSecondAdjFace( f1 );
		f1.setSecondAdjFace( f0 );
		
		v3 = v2.next;
		vol = f0.volumeSign( v3 );
		while ( vol == 0 ) {// C: if(-1) = true
			v3 = v3.next;
			if ( v3 == v0 ) {
				throw new AllPointsCoplanarExceptions( "All points are coplanar." );
			}
			vol = f0.volumeSign( v3 );
		}
		vertices = v3;
	}
	

	/**
	 * Transforms the double linked list of faces in a more user-friendly
	 * ArrayList.
	 * 
	 * @return the ArrayList of the faces of the convex hull.
	 */
	private ArrayList<HullFace> makeResultUserFriendly() {
		ArrayList<HullFace> result;
		result = new ArrayList<HullFace>();
		HullFace f;
		f = faces;
		do {
			result.add( f );
			f = f.next;
		} while ( f != faces );
		return result;
	}
	
}
