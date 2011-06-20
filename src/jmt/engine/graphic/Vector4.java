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

package jmt.engine.graphic;

import java.security.InvalidParameterException;

import jmt.gui.jaba.JabaConstants;

/**
 * Homogeneous 3D coordinates vector for 3D computations.
 * 
 * @author Andrea Zoppi
 * 
 */
public class Vector4 {
	protected float[]			v			= new float[ 4 ];
	
	// Common points and axes (versors).
	public static final Vector4	ORIGIN		= createPoint3D( 0.0f, 0.0f, 0.0f );
	public static final Vector4	X_AXIS		= createAxis3D( 1.0f, 0.0f, 0.0f );
	public static final Vector4	Y_AXIS		= createAxis3D( 0.0f, 1.0f, 0.0f );
	public static final Vector4	Z_AXIS		= createAxis3D( 0.0f, 0.0f, 1.0f );
	public static final Vector4	X_AXIS_REV	= createAxis3D( -1.0f, 0.0f, 0.0f );
	public static final Vector4	Y_AXIS_REV	= createAxis3D( 0.0f, -1.0f, 0.0f );
	public static final Vector4	Z_AXIS_REV	= createAxis3D( 0.0f, 0.0f, -1.0f );
	
	// Axes indices.
	public final static int		X_INDEX		= 0;
	public final static int		Y_INDEX		= 1;
	public final static int		Z_INDEX		= 2;
	
	
	public Vector4() {
	}
	

	public Vector4( Vector4 v ) {
		this.v[0] = v.v[0];
		this.v[1] = v.v[1];
		this.v[2] = v.v[2];
		this.v[3] = v.v[3];
	}
	

	public Vector4( float[] a ) {
		set( a );
	}
	

	public Vector4( float x, float y, float z, float w ) {
		set( x, y, z, w );
	}
	

	public Vector4( float xyzValue ) {
		v[0] = v[1] = v[2] = xyzValue;
		v[3] = 1.0f;
	}
	

	public float x() {
		return v[0];
	}
	

	public float y() {
		return v[1];
	}
	

	public float z() {
		return v[2];
	}
	

	public float w() {
		return v[3];
	}
	

	public void x( float value ) {
		v[0] = value;
	}
	

	public void y( float value ) {
		v[1] = value;
	}
	

	public void z( float value ) {
		v[2] = value;
	}
	

	public void w( float value ) {
		v[3] = value;
	}
	

	public float get( int i ) {
		return v[i];
	}
	

	public void set( float x, float y, float z, float w ) {
		v[0] = x;
		v[1] = y;
		v[2] = z;
		v[3] = w;
	}
	

	public void set( int i, float value ) {
		v[i] = value;
	}
	

	public void set( Vector4 v ) {
		this.v[0] = v.v[0];
		this.v[1] = v.v[1];
		this.v[2] = v.v[2];
		this.v[3] = v.v[3];
	}
	

	public void set( float[] a ) {
		if ( a.length != 4 ) {
			throw new InvalidParameterException( "Array length != 4" );
		}
		v[0] = a[0];
		v[1] = a[1];
		v[2] = a[2];
		v[3] = a[3];
	}
	

	public float length() {
		return (float)Math.sqrt( v[0] * v[0] + v[1] * v[1] + v[2] * v[2] );
	}
	

	public float squaredLength() {
		return v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
	}
	

	public void normalize() {
		float inv = length();
		if ( inv != 0.0f ) {
			inv = 1.0f / inv;
			v[0] *= inv;
			v[1] *= inv;
			v[2] *= inv;
		}
	}
	

	public Vector4 unit() {
		float inv = 1.0f / length();
		return new Vector4( v[0] * inv, v[1] * inv, v[2] * inv, v[3] );
	}
	

	public Vector4 reverse() {
		return new Vector4( -v[0], -v[1], -v[2], v[3] );
	}
	

	public void reverseSelf() {
		v[0] = -v[0];
		v[1] = -v[1];
		v[2] = -v[2];
	}
	

	public Vector4 add( Vector4 a ) {
		return new Vector4( this.v[0] + a.v[0], this.v[1] + a.v[1], this.v[2] + a.v[2], this.v[3] /*+v.v[3]*/);
	}
	

	public void addSelf( Vector4 a ) {
		this.v[0] += a.v[0];
		this.v[1] += a.v[1];
		this.v[2] += a.v[2];
		//this.v[3] += a.v[3];
	}
	

	public Vector4 sub( Vector4 v ) {
		return new Vector4( this.v[0] - v.v[0], this.v[1] - v.v[1], this.v[2] - v.v[2], this.v[3] /*- v.v[3]*/);
	}
	

	public void subSelf( Vector4 v ) {
		this.v[0] -= v.v[0];
		this.v[1] -= v.v[1];
		this.v[2] -= v.v[2];
		//this.v[3] -= v.v[3];
	}
	

	public float dot( Vector4 v ) {
		return this.v[0] * v.v[0] + this.v[1] * v.v[1] + this.v[2] * v.v[2];
	}
	

	public Vector4 cross( Vector4 v ) {
		Vector4 res = new Vector4();
		res.v[0] = this.v[1] * v.v[2] - this.v[2] * v.v[1];
		res.v[1] = this.v[2] * v.v[0] - this.v[0] * v.v[2];
		res.v[2] = this.v[0] * v.v[1] - this.v[1] * v.v[0];
		res.v[3] = 0.0f;
		return res;
	}
	

	public void crossSelf( Vector4 v ) {
		Vector4 res = new Vector4();
		res.v[0] = this.v[1] * v.v[2] - this.v[2] * v.v[1];
		res.v[1] = this.v[2] * v.v[0] - this.v[0] * v.v[2];
		res.v[2] = this.v[0] * v.v[1] - this.v[1] * v.v[0];
		res.v[3] = 0.0f;
		this.set( res );
	}
	

	public Vector4 mult( Matrix4 m ) {
		Vector4 res = new Vector4( 0.0f );
		for ( int c = 0; c < 4; c++ ) {
			for ( int i = 0; i < 4; i++ ) {
				res.v[c] += this.v[i] * m.m[4 * i + c];
			}
		}
		return res;
	}
	

	public Vector4 mult( float scalar ) {
		return new Vector4( v[0] * scalar, v[1] * scalar, v[2] * scalar, v[3] );
	}
	

	public void multSelf( float scalar ) {
		v[0] *= scalar;
		v[1] *= scalar;
		v[2] *= scalar;
		//v[3] *= scalar;
	}
	

	public Vector4 div( float scalar ) {
		scalar = 1.0f / scalar;
		return new Vector4( v[0] * scalar, v[1] * scalar, v[2] * scalar, v[3] );
	}
	

	public void divSelf( float scalar ) {
		scalar = 1.0f / scalar;
		v[0] *= scalar;
		v[1] *= scalar;
		v[2] *= scalar;
		//v[3] *= scalar;
	}
	

	public float[] toArray() {
		return v.clone();
	}
	

	@Override
	public Vector4 clone() {
		return new Vector4( this );
	}
	

	@Override
	public String toString() {
		return "[ " + x() + ", " + y() + ", " + z() + ", " + w() + " ]";
	}
	

	public String toString3D() {
		int prop = JabaConstants.SERVICE_DEMANDS_PROP;;
		return "[ " + x()/prop + ", " + y()/prop + ", " + z()/prop + " ]";
	}
	

	public static Vector4 createPoint3D( float x, float y, float z ) {
		return new Vector4( x, y, z, 1.0f );
	}
	

	public static Vector4 createAxis3D( float x, float y, float z ) {
		return new Vector4( x, y, z, 0.0f );
	}
}
