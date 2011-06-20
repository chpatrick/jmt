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

import java.awt.Dimension;
import java.security.InvalidParameterException;
import java.util.LinkedList;

/**
 * This class transforms 3D coordinates into 2D screen coordinates.
 * 
 * @author Andrea Zoppi.
 * 
 */
public class ScreenProjector {
	
	protected float					screenW;
	protected float					screenH;
	protected float					halfW;
	protected float					halfH;
	protected float					aspect;
	
	protected float					fov;
	protected float					near;
	protected float					far;
	
	protected float					focalLength;
	protected Matrix4				modelView	= Matrix4.createIdentity();
	protected Matrix4				projection	= Matrix4.createIdentity();
	
	protected LinkedList<Matrix4>	stack		= new LinkedList<Matrix4>();
	
	
	public ScreenProjector() {
		setScreenSize( new Dimension( 680, 480 ) );
		setNearClipping( 0.1f );
		setFarClipping( 200.0f );
		setFov( 90.0f );
		updateProjection();
	}
	

	public Dimension getScreenSize() {
		return new Dimension( (int)Math.round( screenW ), (int)Math.round( screenH ) );
	}
	

	public void setScreenSize( Dimension screenSize ) {
		if ( screenSize.width < 1 ) {
			throw new InvalidParameterException( "screenSize.width < 1" );
		}
		if ( screenSize.height < 1 ) {
			throw new InvalidParameterException( "screenSize.height < 1" );
		}
		screenW = screenSize.width;
		screenH = screenSize.height;
		halfW = screenW / 2;
		halfH = screenH / 2;
		aspect = screenH / screenW; // FIXME
	}
	

	public float getFov() {
		return (float)(fov * (180 / Math.PI));
	}
	

	/**
	 * Sets the <i>Field of View</i>.
	 * 
	 * @param fov
	 *            FOV in degrees.
	 */
	public void setFov( float fov ) {
		this.fov = (float)(Math.min( Math.max( 0.1f, fov ), 175.0f ) * (Math.PI / 180));
		focalLength = (float)(1.0 / Math.tan( this.fov / 2.0 ));
	}
	

	public float getNearClipping() {
		return near;
	}
	

	public void setNearClipping( float near ) {
		this.near = Math.max( 0.01f, Math.min( far, 9999.0f ) );
	}
	

	public float getFarClipping() {
		return far;
	}
	

	public void setFarClipping( float far ) {
		this.far = Math.max( 0.02f, Math.min( far, 10000.0f ) );
		if ( this.far == this.near ) {
			this.far += 0.01f; // Try to avoid degeneration
		}
	}
	

	public void lookAt( Vector4 eyePos, Vector4 targetPos, boolean translate ) {
		Vector4 forward = targetPos.sub( eyePos );
		forward.normalize();
		
		Vector4 up;
		if ( Math.abs( forward.x() ) < 0.01f && Math.abs( forward.z() ) < 0.01f ) {
			up = (forward.y() > 0.0f) ? Vector4.Z_AXIS_REV : Vector4.Z_AXIS;
		}
		else {
			up = Vector4.Y_AXIS_REV;
		}
		
		Vector4 left = up.cross( forward );
		left.normalize();
		
		up = forward.cross( left );
		up.normalize();
		
		modelView.set( new Matrix4( translate ? eyePos : Vector4.ORIGIN, left, up, forward ) );
	}
	

	public void lookAt( Vector4 eyePos, Vector4 targetPos, Vector4 up, boolean translate ) {
		Vector4 forward = targetPos.sub( eyePos );
		forward.normalize();
		
		Vector4 left = up.cross( forward );
		left.normalize();
		
		up = forward.cross( left );
		up.normalize();
		
		modelView.set( new Matrix4( translate ? eyePos : Vector4.ORIGIN, left, up, forward ) );
	}
	

	public void trans( float dx, float dy, float dz ) {
		modelView.transSelf( dx, dy, dz );
	}
	

	public void trans( Vector4 delta ) {
		modelView.transSelf( delta );
	}
	

	public void rotX( float w ) {
		modelView.multSelf( Matrix4.createRotationX( w ) );
	}
	

	public void rotY( float w ) {
		modelView.multSelf( Matrix4.createRotationY( w ) );
	}
	

	public void rotZ( float w ) {
		modelView.multSelf( Matrix4.createRotationZ( w ) );
	}
	

	public Matrix4 getModelView() {
		return modelView;
	}
	

	public Matrix4 getProjection() {
		return projection;
	}
	

	public void push( Matrix4 matrix ) {
		stack.push( matrix.clone() );
	}
	

	public Matrix4 pop() {
		return stack.pop();
	}
	

	public int stackSize() {
		return stack.size();
	}
	

	/**
	 * Updates the projection matrix after modifying the screen size, fov or
	 * clipping.
	 */
	public void updateProjection() {
		projection.set( 0.0f );
		projection.set( 0, 0, focalLength * aspect );
		projection.set( 1, 1, focalLength );
		projection.set( 2, 2, (far + near) / (far - near) );
		projection.set( 3, 2, (2 * near * far) / (near - far) );
		projection.set( 2, 3, 1.0f );
	}
	

	/**
	 * Projects a 3D point onto the screen surface, as a 2D point.
	 * 
	 * @param point
	 *            3D point to project.
	 * @return The projected 2D point.
	 */
	public Vector4 project( Vector4 point ) {
		Matrix4 T = projection.mult( modelView );
		Vector4 p = T.mult( point );
		p.x( (p.x() * screenW) / (2.0f * p.w()) + halfW );
		p.y( (p.y() * screenH) / (2.0f * p.w()) + halfH );
		p.z( p.z() / p.w() );
		return p;
	}
	

	/**
	 * Checks whether a projected 2D point is clipped by the frustum.
	 * 
	 * @param point
	 *            Point to check.
	 * @return The clipped condition.
	 */
	public boolean isClipped( Vector4 projectedPoint ) {
		int x = (int)projectedPoint.x();
		int y = (int)projectedPoint.y();
		return x < screenW && x < screenH && (x + 1) >= 0 && (y + 1) >= 0; // TODO: Z-tests
	}
}
