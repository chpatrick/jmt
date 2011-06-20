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

import java.awt.Color;

import jmt.engine.graphic.Vector4;

/**
 * 
 * @author Sebastiano Spicuglia - 2011
 * 
 */
public class HullFace {

	private static final Color COLOR_SINGLE = new Color(255, 200, 0);
	private static final Color COLOR_DOUBLE = new Color(255, 150, 75);
	private static final Color COLOR_MORE = new Color(192, 0, 0);
	public static final Color COLOR_ERROR = Color.GRAY;

	protected static final float COPLANAR_EPSILON = 0.001f;

	private HullEdge e0, e1, e2;
	private HullVertex v0, v1, v2;
	private boolean visible;
	private Vector4 normal;
	private int numCoplanarVertices = 0;
	private boolean marked = false;
	private float shade = 1.0f; // Shade level [0..1]

	protected HullFace next;
	protected HullFace prev;

	private static long currentID = 0;
	private long id = currentID++;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HullFace other = (HullFace) obj;
		if (id != other.id)
			return false;
		return true;
	}

	// Make face
	public HullFace(HullVertex v0, HullVertex v1, HullVertex v2, HullFace fold,
			ConvexHull3D builder) {
		init();
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		if (fold == null) {
			e0 = new HullEdge();
			e1 = new HullEdge();
			e2 = new HullEdge();
			builder.addEdge(e0);
			builder.addEdge(e1);
			builder.addEdge(e2);
		} else {
			e0 = fold.e2;
			e1 = fold.e1;
			e2 = fold.e0;
		}
		e0.setEndPoints(v0, v1);
		e1.setEndPoints(v1, v2);
		e2.setEndPoints(v2, v0);

		e0.setFirstAdjFace(this);
		e1.setFirstAdjFace(this);
		e2.setFirstAdjFace(this);

		updateNormal();
	}

	// Make cone face
	public HullFace(HullEdge e, HullVertex p, ConvexHull3D builder) {
		HullEdge[] newEdge = new HullEdge[2];
		int i, j;

		init();
		for (i = 0; i < 2; i++) {
			newEdge[i] = e.getEndPoint(i).getDuplicate();
			if (newEdge[i] == null) {
				newEdge[i] = new HullEdge();
				builder.addEdge(newEdge[i]);
				newEdge[i].setEndPoints(e.getEndPoint(i), p);
				e.getEndPoint(i).setDuplicate(newEdge[i]);
			}
		}
		e0 = e;
		e1 = newEdge[0];
		e2 = newEdge[1];
		this.makeCcw(e, p);

		for (i = 0; i < 2; i++) {
			for (j = 0; j < 2; j++) {
				if (newEdge[i].getFace(j) == null) {
					newEdge[i].setAdjFaceAt(this, j);
					break;
				}
			}
		}

		updateNormal();
	}

	public HullEdge getEdge(int i) {
		switch (i) {
		case 0:
			return e0;
		case 1:
			return e1;
		case 2:
			return e2;
		default:
			throw new IllegalArgumentException("Edge index not in {0,1,2}");
		}
	}

	public void setEdge(int i, HullEdge e) {
		switch (i) {
		case 0: {
			e0 = e;
			break;
		}
		case 1: {
			e1 = e;
			break;
		}
		case 2: {
			e2 = e;
			break;
		}
		default: {
			throw new IllegalArgumentException("Vertex index not in {0,1,2}");
		}
		}
	}

	public HullEdge[] getEdges() {
		HullEdge[] edges = new HullEdge[3];
		edges[0] = e0;
		edges[1] = e1;
		edges[2] = e2;
		return edges;
	}

	public void setVertex(int i, HullVertex v) {
		switch (i) {
		case 0: {
			v0 = v;
			break;
		}
		case 1: {
			v1 = v;
			break;
		}
		case 2: {
			v2 = v;
			break;
		}
		default: {
			throw new IllegalArgumentException("Vertex index not in {0,1,2}");
		}
		}
	}

	public HullVertex getVertex(int i) {
		switch (i) {
		case 0:
			return v0;
		case 1:
			return v1;
		case 2:
			return v2;
		default:
			throw new IllegalArgumentException("Vertex index not in {0,1,2}");
		}
	}

	public HullVertex[] getVertices() {
		HullVertex[] vertices = new HullVertex[3];
		vertices[0] = v0;
		vertices[1] = v1;
		vertices[2] = v2;
		return vertices;
	}

	public void updateNormal() {
		Vector4 vv0 = Vector4.createPoint3D(v0.x(), v0.y(), v0.z());
		Vector4 vv1 = Vector4.createPoint3D(v1.x(), v1.y(), v1.z());
		Vector4 vv2 = Vector4.createPoint3D(v2.x(), v2.y(), v2.z());
		vv1.subSelf(vv0);
		vv2.subSelf(vv0);
		vv1.crossSelf(vv2);
		normal = vv1;
		normal.normalize();
	}

	public Vector4 getNormal() {
		return normal;
	}

	public int getNumCoplanarVertices() {
		return numCoplanarVertices;
	}

	public void setNumCoplanarVertices(int n) {
		this.numCoplanarVertices = n;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public Color getColor() {
		if (numCoplanarVertices == 1) {
			return COLOR_SINGLE;
		} else if (numCoplanarVertices == 2) {
			return COLOR_DOUBLE;
		} else if (numCoplanarVertices > 2) {
			return COLOR_MORE;
		}
		return COLOR_ERROR;
	}

	// Null face;
	private void init() {
		visible = false;
	}

	private void makeCcw(HullEdge e, HullVertex p) {
		HullFace fv;
		int i;
		HullEdge s;

		if (e.getAdjFaces()[0].isVisible())// FIXME
			fv = e.getAdjFaces()[0];
		else
			fv = e.getAdjFaces()[1];

		HullVertex[] vertices = fv.getVertices();
		for (i = 0; vertices[i] != e.getEndPoints()[0]; i++)
			;
		if (vertices[(i + 1) % 3] != e.getEndPoints()[1]) {
			this.v0 = e.getEndPoints()[1];
			this.v1 = e.getEndPoints()[0];
		} else {
			this.v0 = e.getEndPoints()[0];
			this.v1 = e.getEndPoints()[1];
			// Swap
			s = this.e1;
			this.e1 = this.e2;
			this.e2 = s;
		}
		this.v2 = p;
	}

	protected void setSecondAdjFace(HullFace f) {
		e0.setSecondAdjFace(f);
		e1.setSecondAdjFace(f);
		e2.setSecondAdjFace(f);
	}

	protected boolean isVisible() {
		return visible;
	}

	protected void setVisible(boolean v) {
		visible = v;
	}

	public int volumeSign(HullVertex p) {
		double vol;
		double ax, ay, az, bx, by, bz, cx, cy, cz;

		ax = v0.x() - p.x();
		ay = v0.y() - p.y();
		az = v0.z() - p.z();
		bx = v1.x() - p.x();
		by = v1.y() - p.y();
		bz = v1.z() - p.z();
		cx = v2.x() - p.x();
		cy = v2.y() - p.y();
		cz = v2.z() - p.z();

		vol = ax * (by * cz - bz * cy) + ay * (bz * cx - bx * cz) + az
				* (bx * cy - by * cx);
		if (vol > 0.5)
			return 1;
		else if (vol < -0.5)
			return -1;
		else
			return 0;
	}

	public boolean isCoplanar(HullFace face) {
		Vector4 diff = this.normal.clone();
		diff.subSelf(face.normal);
		if (Math.abs(diff.x()) > COPLANAR_EPSILON
				|| Math.abs(diff.y()) > COPLANAR_EPSILON
				|| Math.abs(diff.z()) > COPLANAR_EPSILON) {
			return false;
		} else {
			return true;
		}
	}

	public float getShade() {
		return shade;
	}

	public void setShade(float shade) {
		this.shade = shade;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Face [" + v0 + ", " + v1 + ", " + v2 + "]";
	}

}
