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

/**
 * Column-major 4x4 matrix, used for 3D computations (homogeneous coordinates).
 * 
 * @author Andrea Zoppi
 * 
 */
public class Matrix4 {
	// Column-major representation of the matrix in vector form.
	protected float[]			m			= new float[ 16 ];
	
	// Identity matrix.
	public final static Matrix4	IDENTITY	= new Matrix4( 1.0f );
	
	// Axes indices.
	public final static int		X_INDEX		= 0;
	public final static int		Y_INDEX		= 1;
	public final static int		Z_INDEX		= 2;
	
	
	public Matrix4() {
		// All zeroes by default
	}
	

	/**
	 * Diagonal constructor.
	 * 
	 * @param scalar
	 *            Scalar value to put on diagonal.
	 */
	public Matrix4( float scalar ) {
		if ( scalar != 0.0f ) { // Avoid redundant assignments
			for ( int i = 0; i < 4; i++ ) {
				set( i, i, scalar );
			}
		}
	}
	

	/**
	 * Copy constructor.
	 * 
	 * @param m
	 *            Matrix to be copied.
	 */
	public Matrix4( Matrix4 m ) {
		set( m );
	}
	

	/**
	 * Array constructor.
	 * 
	 * @param a
	 *            Array to be converted into a matrix.
	 */
	public Matrix4( float[] a ) {
		set( a );
	}
	

	/**
	 * Created a matrix given the position and the individual axes.
	 * 
	 * @param position
	 *            Position vector.
	 * @param left
	 *            Left vector.
	 * @param up
	 *            Up vector.
	 * @param forward
	 *            Forward vector.
	 */
	public Matrix4( Vector4 position, Vector4 left, Vector4 up, Vector4 forward ) {
		for ( int i = 0; i < 3; i++ ) {
			set( i, 0, left.get( i ) );
			set( i, 1, up.get( i ) );
			set( i, 2, forward.get( i ) );
			set( i, 3, position.get( i ) );
		}
		set( 3, 3, 1.0f );
	}
	

	/**
	 * Gets a matrix value.
	 * 
	 * @param r
	 *            Row number.
	 * @param c
	 *            Column number.
	 * @return Value at (row, column).
	 */
	public float get( int r, int c ) {
		return m[(c << 2) + r];
	}
	

	/**
	 * Sets a matrix value.
	 * 
	 * @param r
	 *            Row number.
	 * @param c
	 *            Column number.
	 * @param value
	 *            Value at (row, column).
	 */
	public void set( int r, int c, float value ) {
		m[(c << 2) + r] = value;
	}
	

	/**
	 * Sets all matrix values to the given value.
	 * 
	 * @param allValues
	 *            Value to put in all cells.
	 */
	public void set( float allValues ) {
		for ( int i = 0; i < 16; i++ ) {
			m[i] = allValues;
		}
	}
	

	/**
	 * Copies all values from an existing matrix.
	 * 
	 * @param m
	 *            Matrix to be copied.
	 */
	public void set( Matrix4 m ) {
		for ( int i = 0; i < 16; i++ ) {
			this.m[i] = m.m[i];
		}
	}
	

	/**
	 * Sets all values from a given matrix in column-major vector array form.
	 * 
	 * @param a
	 *            Array to be converted into a matrix.
	 */
	public void set( float[] a ) {
		if ( a.length != 16 ) {
			throw new InvalidParameterException( "Array length != 16" );
		}
		for ( int i = 0; i < 16; i++ ) {
			m[i] = a[i];
		}
	}
	

	/**
	 * Gets a single row vector.
	 * 
	 * @param r
	 *            Row number.
	 * @return The copied row vector.
	 */
	public Vector4 getRow( int r ) {
		Vector4 res = new Vector4();
		for ( int i = 0; i < 4; i++ ) {
			res.v[i] = get( r, i );
		}
		return res;
	}
	

	/**
	 * Gets a single column vector.
	 * 
	 * @param c
	 *            Column number.
	 * @return The copied column vector.
	 */
	public Vector4 getColumn( int c ) {
		Vector4 res = new Vector4();
		for ( int i = 0; i < 4; i++ ) {
			res.v[i] = get( i, c );
		}
		return res;
	}
	

	/**
	 * Sets a single row vector.
	 * 
	 * @param r
	 *            Row number.
	 * @param v
	 *            Vector to be copied onto the row.
	 */
	public void setRow( int r, Vector4 v ) {
		for ( int i = 0; i < 4; i++ ) {
			set( r, i, v.v[i] );
		}
	}
	

	/**
	 * Sets a single column vector.
	 * 
	 * @param r
	 *            Column number.
	 * @param v
	 *            Vector to be copied onto the column.
	 */
	public void setColumn( int c, Vector4 v ) {
		for ( int i = 0; i < 4; i++ ) {
			set( i, c, v.v[i] );
		}
	}
	

	/**
	 * Sets the matrix to an identity matrix.
	 */
	public void setIdentity() {
		set( IDENTITY );
	}
	

	/**
	 * Transposed version of the matrix.
	 * 
	 * @return A transposed copy of the matrix.
	 */
	public Matrix4 tr() {
		Matrix4 t = new Matrix4();
		for ( int r = 0; r < 4; r++ ) {
			for ( int c = 0; c < 4; c++ ) {
				t.set( r, c, this.get( c, r ) );
			}
		}
		return t;
	}
	

	/**
	 * Self-transposes the matrix.
	 */
	public void trSelf() {
		for ( int r = 1; r < 4; r++ ) {
			for ( int c = 0; c < r; c++ ) {
				float t = get( c, r );
				set( c, r, get( r, c ) );
				set( r, c, t );
			}
		}
	}
	

	/**
	 * Computes the determinant of the matrix.
	 * 
	 * @return The matrix determinant.
	 */
	public float det() {
		float p, m;
		
		p = get( 1, 1 ) * get( 2, 2 ) * get( 3, 3 ) * get( 4, 4 );
		p += get( 1, 1 ) * get( 3, 2 ) * get( 4, 3 ) * get( 2, 4 );
		p += get( 1, 1 ) * get( 4, 2 ) * get( 2, 3 ) * get( 3, 4 );
		p += get( 2, 1 ) * get( 1, 2 ) * get( 4, 3 ) * get( 3, 4 );
		p += get( 2, 1 ) * get( 3, 2 ) * get( 1, 3 ) * get( 4, 4 );
		p += get( 2, 1 ) * get( 4, 2 ) * get( 3, 3 ) * get( 1, 4 );
		p += get( 3, 1 ) * get( 1, 2 ) * get( 2, 3 ) * get( 4, 4 );
		p += get( 3, 1 ) * get( 2, 2 ) * get( 4, 3 ) * get( 1, 4 );
		p += get( 3, 1 ) * get( 4, 2 ) * get( 1, 3 ) * get( 2, 4 );
		p += get( 4, 1 ) * get( 1, 2 ) * get( 3, 3 ) * get( 2, 4 );
		p += get( 4, 1 ) * get( 2, 2 ) * get( 1, 3 ) * get( 3, 4 );
		p += get( 4, 1 ) * get( 3, 2 ) * get( 2, 3 ) * get( 1, 4 );
		
		m = get( 1, 1 ) * get( 2, 2 ) * get( 4, 3 ) * get( 3, 4 );
		m += get( 1, 1 ) * get( 3, 2 ) * get( 2, 3 ) * get( 4, 4 );
		m += get( 1, 1 ) * get( 4, 2 ) * get( 3, 3 ) * get( 2, 4 );
		m += get( 2, 1 ) * get( 1, 2 ) * get( 3, 3 ) * get( 4, 4 );
		m += get( 2, 1 ) * get( 3, 2 ) * get( 4, 3 ) * get( 1, 4 );
		m += get( 2, 1 ) * get( 4, 2 ) * get( 1, 3 ) * get( 3, 4 );
		m += get( 3, 1 ) * get( 1, 2 ) * get( 4, 3 ) * get( 2, 4 );
		m += get( 3, 1 ) * get( 2, 2 ) * get( 1, 3 ) * get( 4, 4 );
		m += get( 3, 1 ) * get( 4, 2 ) * get( 2, 3 ) * get( 1, 4 );
		m += get( 4, 1 ) * get( 1, 2 ) * get( 2, 3 ) * get( 3, 4 );
		m += get( 4, 1 ) * get( 2, 2 ) * get( 3, 3 ) * get( 1, 4 );
		m += get( 4, 1 ) * get( 3, 2 ) * get( 1, 3 ) * get( 2, 4 );
		
		return p - m;
	}
	

	/**
	 * Computes an inverted copy of the matrix.
	 * 
	 * @return The inverted copy of the matrix.
	 */
	public Matrix4 inv() {
		Matrix4 res = this.tr();
		for ( int i = 1; i < 16; i += 2 ) {
			res.m[i] = -res.m[i]; // Algebraic complements
		}
		res.divSelf( this.det() );
		return res;
	}
	

	/**
	 * Self-inverts the matrix.
	 */
	public void invSelf() {
		this.set( this.inv() );
	}
	

	/**
	 * Adds a matrix to this one.
	 * 
	 * @param m
	 *            Matrix to be added.
	 * @return The sum of the matrices.
	 */
	public Matrix4 add( Matrix4 m ) {
		Matrix4 res = this.clone();
		for ( int i = 0; i < 16; i++ ) {
			res.m[i] += m.m[i];
		}
		return res;
	}
	

	/**
	 * Adds a matrix to itself.
	 * 
	 * @param m
	 *            Matrix to be added.
	 */
	public void addSelf( Matrix4 m ) {
		for ( int i = 0; i < 16; i++ ) {
			this.m[i] += m.m[i];
		}
	}
	

	/**
	 * Subtracts a mtrix to this one.
	 * 
	 * @param m
	 *            Matrix to be subtracted
	 * @return The difference between the matrices.
	 */
	public Matrix4 sub( Matrix4 m ) {
		Matrix4 res = this.clone();
		for ( int i = 0; i < 16; i++ ) {
			res.m[i] -= m.m[i];
		}
		return res;
	}
	

	/**
	 * Subtracts a matrix to itself.
	 * 
	 * @param m
	 *            Matrix to be subtracted.
	 */
	public void subSelf( Matrix4 m ) {
		for ( int i = 0; i < 16; i++ ) {
			this.m[i] -= m.m[i];
		}
	}
	

	/**
	 * Multiplied copy of the matrix by a scalar value.
	 * 
	 * @param scalar
	 *            Scalar value to be multiplied.
	 * @return The multiplied matrix.
	 */
	public Matrix4 mult( float scalar ) {
		Matrix4 res = this.clone();
		for ( int i = 0; i < 16; i++ ) {
			res.m[i] *= scalar;
		}
		return res;
	}
	

	/**
	 * Multiplies the matrix by another matrix.
	 * 
	 * @param m
	 *            Matrix to be multiplied.
	 * @return The multiplied matrix.
	 */
	public Matrix4 mult( Matrix4 m ) {
		Matrix4 res = new Matrix4( 0.0f );
		for ( int r = 0; r < 4; r++ ) {
			for ( int c = 0; c < 4; c++ ) {
				float sum = 0.0f;
				for ( int i = 0; i < 4; i++ ) {
					sum += this.get( r, i ) * m.get( i, c );
				}
				res.set( r, c, sum );
			}
		}
		return res;
	}
	

	/**
	 * Multiplies the matrix by a vector.
	 * 
	 * @param v
	 *            Vector to be multiplied.
	 * @return The resultant vector (column).
	 */
	public Vector4 mult( Vector4 v ) {
		Vector4 res = new Vector4( 0.0f );
		for ( int r = 0; r < 4; r++ ) {
			for ( int i = 0; i < 4; i++ ) {
				res.v[r] += this.get( r, i ) * v.v[i];
			}
		}
		return res;
	}
	

	/**
	 * Multiplies itself by a scalar value.
	 * 
	 * @param scalar
	 *            Scalar value to multiply by.
	 */
	public void multSelf( float scalar ) {
		for ( int i = 0; i < 16; i++ ) {
			this.m[i] *= scalar;
		}
	}
	

	/**
	 * Multiplied itself by another matrix.
	 * 
	 * @param m
	 *            Matrix to be multiplied by.
	 */
	public void multSelf( Matrix4 m ) {
		this.set( this.mult( m ) );
	}
	

	public Matrix4 div( float scalar ) {
		Matrix4 res = this.clone();
		scalar = 1.0f / scalar;
		for ( int i = 0; i < 16; i++ ) {
			res.m[i] *= scalar;
		}
		return res;
	}
	

	public void divSelf( float scalar ) {
		scalar = 1.0f / scalar;
		for ( int i = 0; i < 16; i++ ) {
			this.m[i] *= scalar;
		}
	}
	

	public Matrix4 trans( float dx, float dy, float dz ) {
		Matrix4 res = this.clone();
		res.set( 0, 3, res.get( 0, 3 ) + dx );
		res.set( 1, 3, res.get( 1, 3 ) + dy );
		res.set( 2, 3, res.get( 2, 3 ) + dz );
		return res;
	}
	

	public Matrix4 trans( Vector4 dv ) {
		Matrix4 res = this.clone();
		res.set( 0, 3, res.get( 0, 3 ) + dv.x() );
		res.set( 1, 3, res.get( 1, 3 ) + dv.y() );
		res.set( 2, 3, res.get( 2, 3 ) + dv.z() );
		return res;
	}
	

	public void transSelf( float dx, float dy, float dz ) {
		this.set( 0, 3, this.get( 0, 3 ) + dx );
		this.set( 1, 3, this.get( 1, 3 ) + dy );
		this.set( 2, 3, this.get( 2, 3 ) + dz );
	}
	

	public void transSelf( Vector4 dv ) {
		transSelf( dv.x(), dv.y(), dv.z() );
	}
	

	public float[] toArray() {
		return m.clone();
	}
	

	@Override
	public Matrix4 clone() {
		return new Matrix4( this );
	}
	

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( 512 );
		sb.append( "[ " );
		
		sb.append( get( 0, 0 ) );
		sb.append( ", " );
		sb.append( get( 0, 1 ) );
		sb.append( ", " );
		sb.append( get( 0, 2 ) );
		sb.append( ", " );
		sb.append( get( 0, 3 ) );
		
		sb.append( " ; " );
		
		sb.append( get( 1, 0 ) );
		sb.append( ", " );
		sb.append( get( 1, 1 ) );
		sb.append( ", " );
		sb.append( get( 1, 2 ) );
		sb.append( ", " );
		sb.append( get( 1, 3 ) );
		
		sb.append( " ; " );
		
		sb.append( get( 2, 0 ) );
		sb.append( ", " );
		sb.append( get( 2, 1 ) );
		sb.append( ", " );
		sb.append( get( 2, 2 ) );
		sb.append( ", " );
		sb.append( get( 2, 3 ) );
		
		sb.append( " ; " );
		
		sb.append( get( 3, 0 ) );
		sb.append( ", " );
		sb.append( get( 3, 1 ) );
		sb.append( ", " );
		sb.append( get( 3, 2 ) );
		sb.append( ", " );
		sb.append( get( 3, 3 ) );
		
		sb.append( " ]" );
		return sb.toString();
	}
	

	public static Matrix4 createIdentity() {
		return IDENTITY.clone();
	}
	

	public static Matrix4 createTranslation( float dx, float dy, float dz ) {
		Matrix4 res = createIdentity();
		res.set( 0, 3, dx );
		res.set( 1, 3, dy );
		res.set( 2, 3, dz );
		return res;
	}
	

	public static Matrix4 createTranslationOnly( float dx, float dy, float dz ) {
		Matrix4 res = new Matrix4( 0.0f );
		res.set( 0, 3, dx );
		res.set( 1, 3, dy );
		res.set( 2, 3, dz );
		return res;
	}
	

	public static Matrix4 createRotationXYZ( float rx, float ry, float rz ) {
		Matrix4 res = null;
		if ( rx != 0.0f ) {
			res = createRotationX( rx );
		}
		if ( ry != 0.0f ) {
			if ( res == null ) {
				res = createRotationY( ry );
			}
			else {
				res.multSelf( createRotationY( ry ) );
			}
		}
		if ( rz != 0.0f ) {
			if ( res == null ) {
				res = createRotationZ( rz );
			}
			else {
				res.multSelf( createRotationZ( rz ) );
			}
		}
		return (res != null) ? res : createIdentity();
	}
	

	public static Matrix4 createRotationZXY( float rx, float ry, float rz ) {
		Matrix4 res = null;
		if ( rz != 0.0f ) {
			res = createRotationZ( rz );
		}
		if ( rx != 0.0f ) {
			if ( res == null ) {
				res = createRotationX( rx );
			}
			else {
				res.multSelf( createRotationX( rx ) );
			}
		}
		if ( ry != 0.0f ) {
			if ( res == null ) {
				res = createRotationY( ry );
			}
			else {
				res.multSelf( createRotationY( ry ) );
			}
		}
		return (res != null) ? res : createIdentity();
	}
	

	public static Matrix4 createRotationX( float rad ) {
		Matrix4 res = createIdentity();
		if ( rad != 0.0f ) {
			float c = (float)Math.cos( rad );
			float s = (float)Math.sin( rad );
			res.set( 1, 1, c );
			res.set( 2, 2, c );
			res.set( 1, 2, -s );
			res.set( 2, 1, s );
		}
		return res;
		
	}
	

	public static Matrix4 createRotationY( float rad ) {
		Matrix4 res = createIdentity();
		if ( rad != 0.0f ) {
			float c = (float)Math.cos( rad );
			float s = (float)Math.sin( rad );
			res.set( 0, 0, c );
			res.set( 2, 2, c );
			res.set( 0, 2, s );
			res.set( 2, 0, -s );
		}
		return res;
	}
	

	public static Matrix4 createRotationZ( float rad ) {
		Matrix4 res = createIdentity();
		if ( rad != 0.0f ) {
			float c = (float)Math.cos( rad );
			float s = (float)Math.sin( rad );
			res.set( 0, 0, c );
			res.set( 1, 1, c );
			res.set( 0, 1, -s );
			res.set( 1, 0, s );
		}
		return res;
	}
	

	public static Matrix4 createDiagonal( float allValues ) {
		return new Matrix4( allValues );
	}
	

	public static Matrix4 createDiagonal( float d00, float d11, float d22, float d33 ) {
		Matrix4 res = new Matrix4();
		res.set( 0, 0, d00 );
		res.set( 1, 1, d11 );
		res.set( 2, 2, d22 );
		res.set( 3, 3, d33 );
		return res;
	}
	

	public static Matrix4 createScale( float xyzScale ) {
		return createDiagonal( xyzScale, xyzScale, xyzScale, 1.0f );
	}
	

	public static Matrix4 createScale( float sx, float sy, float sz ) {
		return createDiagonal( sx, sy, sz, 1.0f );
	}
}
