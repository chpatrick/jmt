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

package jmt.gui.jaba.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JFrame;

import jmt.engine.graphic.Matrix4;
import jmt.engine.graphic.ScreenProjector;
import jmt.engine.graphic.Vector4;
import jmt.engine.jaba.convexHull3d.HullEdge;
import jmt.engine.jaba.convexHull3d.HullFace;
import jmt.engine.jaba.convexHull3d.HullVertex;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;

/**
 * This is a panel which draws the Convex Hull diagram, given the pre-computed
 * set of faces.
 * 
 * @author Andrea Zoppi
 * @author Sebastiano Spicuglia
 */

public class Convex3DGraph extends JabaGraph implements ComponentListener,
		MouseListener, MouseMotionListener, MouseWheelListener {
	protected static final Font FONT = new Font("Arial Narrow", Font.PLAIN, 10);

	protected static final long serialVersionUID = 1L;

	protected ScreenProjector prj = new ScreenProjector();

	protected static final Color BGCOLOR = Color.WHITE;
	private static final Color DARK_RED = new Color(128, 0, 0);
	private static final Color DARK_GREEN = new Color(0, 128, 0);
	private static final Color DARK_BLUE = new Color(0, 0, 128);


	// protected JabaModel data;//PRODUCTION SOLUTION
	// protected JabaWizard mainWin;//PRODUCTION SOLUTION
	private static final BasicStroke DOTTED = new BasicStroke(1, 1, 1, 1,
			new float[] { 2f }, 1);
	protected JFrame mainWin; // PROTOTYPE SOLUTION

	// Rendering options.
	protected boolean enableCustomZoom = false;
	protected boolean enableDebugInfo = false;
	protected boolean enableDebugEdges = false;

	protected boolean enableCoplanarEdges = true;
	protected boolean enableInternalVertices = false;
	protected boolean enableVisibleEdges = true;
	protected boolean enableHiddenEdges = false;
	protected boolean enableVisibleFaces = true;
	protected boolean enableHiddenFaces = false;
	protected boolean enableHullVerticesLabels = true;
	protected boolean enableTransparentFaces = false;

	// Viewing parameters.
	protected Vector4 axisPrjX = Vector4.createPoint3D(1.0f, 0.0f, 0.0f);
	protected Vector4 axisPrjY = Vector4.createPoint3D(0.0f, 1.0f, 0.0f);
	protected Vector4 axisPrjZ = Vector4.createPoint3D(0.0f, 0.0f, 1.0f);
	protected Vector4 axisLabelX = Vector4.createPoint3D(1.0f, 0.0f, 0.0f);
	protected Vector4 axisLabelY = Vector4.createPoint3D(0.0f, 1.0f, 0.0f);
	protected Vector4 axisLabelZ = Vector4.createPoint3D(0.0f, 0.0f, 1.0f);
	protected Vector4 originPrj = Vector4.createPoint3D(0.0f, 0.0f, 0.0f);
	protected Vector4 forward = Vector4.Z_AXIS_REV;
	protected float screenW;
	protected float screenH;
	protected float halfW;
	protected float halfH;
	protected float aspect;
	protected float rotY = (float) Math.toRadians(45.0f); // vertical rotation
															// angle
	protected float rotX = (float) Math.toRadians(45.0f); // head pitch angle
	protected float rotYdelta = 0.0f;
	protected float rotXdelta = 0.0f;
	protected float zoomFactor = 1.0f;

	// Mouse state.
	protected boolean mouseLeft = false;
	protected boolean mouseRight = false;
	protected Point mouseStart = new Point(0, 0);
	protected Point mouseEnd = new Point(0, 0);
	protected Point mouseDelta = new Point(0, 0);
	protected MouseMode mouseMode = MouseMode.NONE;

	// Convex hull data
	protected static final float VISIBILITY_EPSILON = 0.0f; // -0.001f;
	protected static final float COPLANAR_EPSILON = 0.001f;

	private static final Color SELECTED_STATION_COLOR = Color.GREEN;

	protected HashSet<HullVertex> allVerticesSet;
	protected HullVertex[] allVerticesArray = null;
	protected ArrayList<HullFace> faces;

	// Convex hull primitives sets
	protected HashSet<HullEdge> hullEdgesSet = new HashSet<HullEdge>();
	protected HashSet<PointData> hullVerticesSet = new HashSet<PointData>();

	protected HashSet<HullFace> visibleFacesSet = new HashSet<HullFace>();
	protected HashSet<HullFace> hiddenFacesSet = new HashSet<HullFace>();
	protected HashSet<HullEdge> visibleEdgesSet = new HashSet<HullEdge>();
	protected HashSet<HullEdge> hiddenEdgesSet = new HashSet<HullEdge>();
	protected HashSet<PointData> visibleVerticesSet = new HashSet<PointData>();
	protected HashSet<PointData> hiddenVerticesSet = new HashSet<PointData>();
	protected HashSet<PointData> verticesInsideSet = new HashSet<PointData>();

	// Convex hull primitives arrays (extracted from sets)
	protected HullEdge[] hullEdgesArray = null;
	protected PointData[] hullVerticesArray = null;
	protected HullFace[] hullFacesArray = null;

	protected HullFace[] visibleFacesArray = null;
	protected HullFace[] hiddenFacesArray = null;
	protected HullEdge[] visibleEdgesArray = null;
	protected HullEdge[] hiddenEdgesArray = null;
	protected PointData[] visibleVerticesArray = null;
	protected PointData[] hiddenVerticesArray = null;
	protected PointData[] verticesInsideArray = null;

	// Sorted vertices arrays
	protected PointData[] sortedVisibleVertices;
	protected PointData[] sortedHiddenVertices;
	protected PointData[] sortedVerticesInside;

	// Information for solid centering and scaling.
	protected float maxVertexDistance = 1.0f;
	protected Vector4 centroid = Vector4.createPoint3D(0.0f, 0.0f, 0.0f);

	// Internal eye rototranslation matrix.
	protected Matrix4 eyeRT = Matrix4.createIdentity();

	private String[] classNames;

	private String selectedStation;

	/**
	 * Keeps points coordinates both as world coords and as projected screen
	 * coords. Each point has its own ID for further operations.
	 * 
	 * @author Andrea Zoppi
	 */
	protected static class PointData implements Comparable<PointData> {
		public Vector4 worldPos = null;
		public Vector4 screenPos = null;
		public HullVertex vertex = null;

		public PointData(Vector4 worldPos, Vector4 screenPos, HullVertex vertex) {
			this.worldPos = worldPos;
			this.screenPos = screenPos;
			this.vertex = vertex;
		}

		@Override
		public int hashCode() {
			return vertex.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PointData other = (PointData) obj;
			if (vertex == null) {
				if (other.vertex != null)
					return false;
			} else if (!vertex.equals(other.vertex))
				return false;
			return true;
		}

		public int compareTo(PointData o) {
			if (this.screenPos.w() > o.screenPos.w()) {
				return 1;
			} else if (this.screenPos.w() < o.screenPos.w()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	/**
	 * Mouse action modes.
	 */
	protected enum MouseMode {
		NONE, ROTATE_VIEW, MOVE_VERTEX;
	}

	public boolean getEnableDebugInfo() {
		return enableDebugInfo;
	}

	public void setEnableDebugInfo(boolean enableDebugInfo) {
		this.enableDebugInfo = enableDebugInfo;
	}

	public boolean getEnableDebugEdges() {
		return enableDebugEdges;
	}

	public void setEnableDebugEdges(boolean enableDebugEdges) {
		this.enableDebugEdges = enableDebugEdges;
	}

	public boolean getEnableCustomZoom() {
		return enableCustomZoom;
	}

	public void setEnableCustomZoom(boolean enableCustomZoom) {
		this.enableCustomZoom = enableCustomZoom;
	}

	public boolean getEnableInternalVertices() {
		return enableInternalVertices;
	}

	public void setEnableInternalVertices(boolean enableInternalVertices) {
		this.enableInternalVertices = enableInternalVertices;
	}

	public boolean getEnableCoplanarEdges() {
		return enableCoplanarEdges;
	}

	public void setEnableCoplanarEdges(boolean enableCoplanarEdges) {
		this.enableCoplanarEdges = enableCoplanarEdges;
	}

	public boolean getEnableVisibleEdges() {
		return enableVisibleEdges;
	}

	public void setEnableVisibleEdges(boolean enableVisibleEdges) {
		this.enableVisibleEdges = enableVisibleEdges;
	}

	public boolean getEnableHiddenEdges() {
		return enableHiddenEdges;
	}

	public void setEnableHiddenEdges(boolean enableHiddenEdges) {
		this.enableHiddenEdges = enableHiddenEdges;
	}

	public boolean getEnableVisibleFaces() {
		return enableVisibleFaces;
	}

	public void setEnableVisibleFaces(boolean enableVisibleFaces) {
		this.enableVisibleFaces = enableVisibleFaces;
	}

	public boolean getEnableHiddenFaces() {
		return enableHiddenFaces;
	}

	public void setEnableHiddenFaces(boolean enableHiddenFaces) {
		this.enableHiddenFaces = enableHiddenFaces;
	}

	public boolean getEnableHullVerticesLabels() {
		return enableHullVerticesLabels;
	}

	public void setEnableHullVerticesLabels(boolean enableHullVerticesLabels) {
		this.enableHullVerticesLabels = enableHullVerticesLabels;
	}

	public boolean getEnableTransparentFaces() {
		return enableTransparentFaces;
	}

	public void setEnableTransparentFaces(boolean enableTransparentFaces) {
		this.enableTransparentFaces = enableTransparentFaces;
	}

	public float getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

	/**
	 * Check if the face is visible or not. Due to the convex solid property, a
	 * positive dot product means that the face has the normal in the same
	 * direction of the sight, thus it is not visible because there exist faces
	 * which cover the sight of the analyzed one.
	 * 
	 * @param faces
	 *            Faces to split.
	 * @param forward
	 *            Viewing direction.
	 */
	protected void splitByVisibility(Vector4 forward) {
		forward = forward.unit();
		visibleFacesSet.clear();
		hiddenFacesSet.clear();
		visibleEdgesSet.clear();
		hiddenEdgesSet.clear();
		visibleVerticesSet.clear();
		hiddenVerticesSet.clear();
		hullEdgesSet.clear();
		hullVerticesSet.clear();
		maxVertexDistance = 1.0f;
		centroid.set(Vector4.createPoint3D(0.0f, 0.0f, 0.0f));
		float[] max = { 0.0f, 0.0f, 0.0f };
		float[] min = { 0.0f, 0.0f, 0.0f };

		int size = faces.size();
		for (int i = 0; i < size; i++) {
			HullFace face = faces.get(i);

			Vector4 normal = face.getNormal();
			HullEdge[] edges = face.getEdges();
			HullVertex[] vertices = face.getVertices();

			float facing = normal.dot(forward);
			boolean isVisible = (facing <= VISIBILITY_EPSILON);
			if (isVisible) {
				visibleFacesSet.add(face);
			} else {
				hiddenFacesSet.add(face);
			}
			face.setShade(facing);

			// Create unique and visibility sets
			for (int j = 0; j < edges.length; j++) {
				HullEdge e = edges[j];
				if (isVisible) {
					visibleEdgesSet.add(e);

					// Fix edges flagged as hidden but actually visible
					if (hiddenEdgesSet.contains(e)) {
						hiddenEdgesSet.remove(e);
					}
				} else if (!visibleEdgesSet.contains(e)) {
					// Add only if no other faces have already claimed the edge
					// visibility.
					hiddenEdgesSet.add(e);
				}
				if (hullEdgesSet.add(e)) {
				}
			}
			for (int j = 0; j < vertices.length; j++) {
				HullVertex v = vertices[j];
				Vector4 worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				Vector4 screenPos = prj.project(worldPos);
				PointData p = new PointData(worldPos, screenPos, v);
				if (isVisible) {
					visibleVerticesSet.add(p);

					// Fix vertices flagged as hidden but actually visible
					if (hiddenVerticesSet.contains(p)) {
						hiddenVerticesSet.remove(p);
					}
				} else if (!visibleVerticesSet.contains(p)) {
					// Add only if no other faces have already claimed the
					// vertex visibility.
					hiddenVerticesSet.add(p);
				}
				if (p.vertex.getID() >= 0 && hullVerticesSet.add(p)) {
					if (v.x() > max[0]) {
						max[0] = v.x();
					}
					if (v.y() > max[1]) {
						max[1] = v.y();
					}
					if (v.z() > max[2]) {
						max[2] = v.z();
					}
					if (v.x() < min[0]) {
						min[0] = v.x();
					}
					if (v.y() < min[1]) {
						min[1] = v.y();
					}
					if (v.z() < min[2]) {
						min[2] = v.z();
					}
				}
			}
		}

		// Centering and scaling
		centroid.x((max[0] + min[0]) * 0.5f * zoomFactor);
		centroid.y((max[1] + min[1]) * 0.5f * zoomFactor);
		centroid.z((max[2] + min[2]) * 0.5f * zoomFactor);
		max[0] -= min[0];
		max[1] -= min[1];
		max[2] -= min[2];
		maxVertexDistance = 0.5f * (float) Math.sqrt(max[0] * max[0] + max[1]
				* max[1] + max[2] * max[2]);
		centroid.divSelf(maxVertexDistance * zoomFactor);

		// Build the verticesInside set.
		verticesInsideSet.clear();
		for (int i = 0; i < allVerticesArray.length; i++) {
			HullVertex v = allVerticesArray[i];
			PointData p = new PointData(null, null, v); // Dummy PointData, just
														// to check the HashSet
			if (!hullVerticesSet.contains(p)) {
				Vector4 worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				Vector4 screenPos = prj.project(worldPos);
				p = new PointData(worldPos, screenPos, v);
				verticesInsideSet.add(p);
			}
		}

		// TODO: Remove useless vertices

		// Convert sets into arrays for easy modifies
		hullEdgesArray = hullEdgesSet
				.toArray(new HullEdge[hullEdgesSet.size()]);
		hullVerticesArray = hullVerticesSet
				.toArray(new PointData[hullVerticesSet.size()]);
		hullFacesArray = this.faces.toArray(new HullFace[this.faces.size()]);

		visibleFacesArray = visibleFacesSet
				.toArray(new HullFace[visibleFacesSet.size()]);
		hiddenFacesArray = hiddenFacesSet.toArray(new HullFace[hiddenFacesSet
				.size()]);
		visibleEdgesArray = visibleEdgesSet
				.toArray(new HullEdge[visibleEdgesSet.size()]);
		hiddenEdgesArray = hiddenEdgesSet.toArray(new HullEdge[hiddenEdgesSet
				.size()]);
		visibleVerticesArray = visibleVerticesSet
				.toArray(new PointData[visibleVerticesSet.size()]);
		hiddenVerticesArray = hiddenVerticesSet
				.toArray(new PointData[hiddenVerticesSet.size()]);
		verticesInsideArray = verticesInsideSet
				.toArray(new PointData[verticesInsideSet.size()]);

		// Sort sets into separate arrays
		sortedVisibleVertices = visibleVerticesArray.clone();
		Arrays.sort(sortedVisibleVertices);
		sortedHiddenVertices = hiddenVerticesArray.clone();
		Arrays.sort(sortedHiddenVertices);
		sortedVerticesInside = verticesInsideArray.clone();
		Arrays.sort(sortedVisibleVertices);

		countFaceVertices();
	}

	protected void countFaceVertices() {
		/*
		 * Per ogni faccia faccia.contatore = 0; faccia.marcata = false;
		 * 
		 * Per ogni vertice € convex hull Per ogni faccia Se vertice € faccia
		 * incrementaFaccia(faccia) Per ogni faccia faccia.marcata = false
		 */

		// Reset vertex counters and marks
		for (int fi = 0; fi < hullFacesArray.length; fi++) {
			HullFace f = hullFacesArray[fi];
			f.setNumCoplanarVertices(0);
		}

		// Start propagating the number of vertices per face
		for (int vi = 0; vi < hullVerticesArray.length; vi++) {
			// Cleanup marked bits
			for (int fi = 0; fi < hullFacesArray.length; fi++) {
				HullFace f = hullFacesArray[fi];
				f.setMarked(false);
			}

			for (int fi = 0; fi < hullFacesArray.length; fi++) {
				HullVertex v = hullVerticesArray[vi].vertex;
				HullFace f = hullFacesArray[fi];

				// Start color propagation
				if (v.getID() == f.getVertex(0).getID()
						|| v.getID() == f.getVertex(1).getID()
						|| v.getID() == f.getVertex(2).getID()) {
					if (!f.isMarked()) {
						incrementFaceCounter(f);
					}
				}
			}
		}
	}

	protected void incrementFaceCounter(HullFace face) {
		/*
		 * faccia.contatore++ faccia.marcata = true
		 * 
		 * Per ogni spigolo € faccia Per ogni faccia adiacente € spigolo Se
		 * adiacente != questa && adiacente coplanare faccia &&
		 * !adiacente.marcata incrementaFaccia(adiacente)
		 */

		// Increment the counter and set as marked
		face.setNumCoplanarVertices(1 + face.getNumCoplanarVertices());
		face.setMarked(true);

		for (int ei = 0; ei < 3; ei++) {
			HullEdge e = face.getEdge(ei);
			for (int ai = 0; ai < 2; ai++) {
				HullFace adj = e.getFace(ai);
				/*
				 * if ( face.volumeSign( adj.getVertex( 0 ) ) == 0 &&
				 * face.volumeSign( adj.getVertex( 1 ) ) == 0 &&
				 * face.volumeSign( adj.getVertex( 2 ) ) == 0 &&
				 * !face.isMarked() ) { incrementFaceCounter( adj ); }
				 */
				if (!adj.isMarked() && face.isCoplanar(adj)) {
					incrementFaceCounter(adj);
				}
			}
		}
	}

	public Convex3DGraph(JabaModel data, JabaWizard mainWin) {

		this.setBackground(BGCOLOR);

		this.addComponentListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);

		this.classNames = data.getClassNames();
		this.faces = data.getResults().getFaces();
		this.mainWin = mainWin;
		this.allVerticesSet = data.getHullVertices();
		this.allVerticesArray = allVerticesSet
				.toArray(new HullVertex[allVerticesSet.size()]);

		this.setBorder(BorderFactory.createEtchedBorder());
		this.setBackground(BGCOLOR);

		repaint();
	}

	@Override
	public void paint(Graphics graphics) {
		super.paint(graphics);
		Graphics2D g = (Graphics2D) graphics;
//		 g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		updateProjection();

		// Draw 3D grids
		drawGrids(g, 1.0f, 10, false, false, false);

		g.setFont(FONT);

		// Draw hidden faces
		if (enableHiddenFaces) {
			for (int i = 0; i < hiddenFacesArray.length; i++) {
				HullFace f = hiddenFacesArray[i];
				HullVertex v = f.getVertex(0);
				Vector4 worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				Vector4 screenPos = prj.project(worldPos);
				PointData p1 = new PointData(worldPos, screenPos, v);

				v = f.getVertex(1);
				worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				screenPos = prj.project(worldPos);
				PointData p2 = new PointData(worldPos, screenPos, v);

				v = f.getVertex(2);
				worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				screenPos = prj.project(worldPos);
				PointData p3 = new PointData(worldPos, screenPos, v);

				drawFace(g, p1, p2, p3, f);
			}
		}

		// Draw visible faces
		if (enableVisibleFaces) {
			for (int i = 0; i < visibleFacesArray.length; i++) {
				HullFace f = visibleFacesArray[i];
				HullVertex v = f.getVertex(0);
				Vector4 worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				Vector4 screenPos = prj.project(worldPos);
				PointData p1 = new PointData(worldPos, screenPos, v);

				v = f.getVertex(1);
				worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				screenPos = prj.project(worldPos);
				PointData p2 = new PointData(worldPos, screenPos, v);

				v = f.getVertex(2);
				worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				screenPos = prj.project(worldPos);
				PointData p3 = new PointData(worldPos, screenPos, v);

				drawFace(g, p1, p2, p3, f);
			}
		}

		// Draw hidden edges
		if (enableHiddenEdges) {
			for (int i = 0; i < hiddenEdgesArray.length; i++) {
				HullEdge e = hiddenEdgesArray[i];
				HullVertex v = e.getEndPoint(0);
				Vector4 worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				Vector4 screenPos = prj.project(worldPos);
				PointData p1 = new PointData(worldPos, screenPos, v);

				v = e.getEndPoint(1);
				worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				screenPos = prj.project(worldPos);
				PointData p2 = new PointData(worldPos, screenPos, v);

				drawEdge(g, p1, p2, e, false);
			}
		}

		// Draw hidden hull vertices
		for (int i = 0; i < sortedHiddenVertices.length; i++) {
			PointData p = sortedHiddenVertices[i];
			if (p.vertex.getID() >= 0) {
				drawVertex(g, p, false, true);
			}
		}

		// Draws points inside
		if (enableInternalVertices) {
			for (int i = 0; i < sortedVerticesInside.length; i++) {
				PointData p = sortedVerticesInside[i];
				drawVertex(g, p, false, false);
			}
		}

		drawAxes(g, true);

		// Draw visible hull edges
		if (enableVisibleEdges) {
			for (int i = 0; i < visibleEdgesArray.length; i++) {
				HullEdge e = visibleEdgesArray[i];
				HullVertex v = e.getEndPoint(0);
				Vector4 worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				Vector4 screenPos = prj.project(worldPos);
				PointData p1 = new PointData(worldPos, screenPos, v);

				v = e.getEndPoint(1);
				worldPos = Vector4.createPoint3D(v.x(), v.y(), v.z());
				screenPos = prj.project(worldPos);
				PointData p2 = new PointData(worldPos, screenPos, v);

				drawEdge(g, p1, p2, e, true);
			}
		}

		// Draw visible hull vertices
		for (int i = 0; i < sortedVisibleVertices.length; i++) {
			PointData p = sortedVisibleVertices[i];
			if (p.vertex.getID() >= 0) {
				drawVertex(g, p, true, true);
			}
		}

		// DEBUG CODE

		if (enableDebugInfo) {
			g.setColor(Color.MAGENTA);
			if (mouseStart != null && mouseEnd != null) {
				g.drawLine(mouseStart.x, mouseStart.y, mouseEnd.x, mouseEnd.y);
			}

			printViewState(g);
		}
	}

	protected void drawAxes(Graphics2D g, boolean visible) {
		// TODO: Draw axes based on visibility
		Stroke tmp = g.getStroke();
		g.setStroke(DOTTED);
		g.setColor(DARK_RED);
		g.drawLine((int) originPrj.x(), (int) originPrj.y(),
				(int) axisPrjX.x(), (int) axisPrjX.y());
		drawBorderString(g, classNames[0], (int) axisLabelX.x(),
				(int) axisLabelX.y(), DARK_RED, null);
		g.setColor(DARK_GREEN);
		g.drawLine((int) originPrj.x(), (int) originPrj.y(),
				(int) axisPrjY.x(), (int) axisPrjY.y());
		drawBorderString(g, classNames[1], (int) axisLabelY.x(),
				(int) axisLabelY.y(), DARK_GREEN, null);
		g.setColor(DARK_BLUE);
		g.drawLine((int) originPrj.x(), (int) originPrj.y(),
				(int) axisPrjZ.x(), (int) axisPrjZ.y());
		drawBorderString(g, classNames[2], (int) axisLabelZ.x(),
				(int) axisLabelZ.y(), DARK_BLUE, null);
		g.setStroke(tmp);
	}

	protected void drawBorderString(Graphics2D g, String s, int x, int y,
			Color textColor, Color borderColor) {
		if (borderColor != null) {
			g.setColor(borderColor);
			g.drawString(s, x + 4, y + 4);
			g.drawString(s, x + 4, y + 6);
			g.drawString(s, x + 6, y + 4);
			g.drawString(s, x + 6, y + 6);
			g.drawString(s, x + 5, y + 4);
			g.drawString(s, x + 5, y + 6);
			g.drawString(s, x + 4, y + 5);
			g.drawString(s, x + 6, y + 5);
		}
		g.setColor(textColor);
		g.drawString(s, x + 5, y + 5);
	}

	protected void drawGrids(Graphics2D g, float size, int numSegments,
			boolean xEnable, boolean yEnable, boolean zEnable) {
		// size *= 0.5f;
		float delta = size / numSegments;
		float offset, limit = size;
		numSegments *= 2;
		Vector4 p = new Vector4();
		if (xEnable) {
			final Color lineColor = new Color(1.0f, 0.75f, 0.75f);
			g.setColor(lineColor);
			offset = -limit;
			for (int i = 0; i <= numSegments; i++) {
				p.set(0.0f, offset, -limit, 1.0f);
				Vector4 s1 = prj.project(p);
				p.set(0.0f, offset, limit, 1.0f);
				Vector4 e1 = prj.project(p);
				p.set(0.0f, -limit, offset, 1.0f);
				Vector4 s2 = prj.project(p);
				p.set(0.0f, limit, offset, 1.0f);
				Vector4 e2 = prj.project(p);
				g.drawLine((int) s1.x(), (int) s1.y(), (int) e1.x(),
						(int) e1.y());
				g.drawLine((int) s2.x(), (int) s2.y(), (int) e2.x(),
						(int) e2.y());
				offset += delta;
			}
		}
		if (yEnable) {
			final Color lineColor = new Color(0.75f, 1.0f, 0.75f);
			g.setColor(lineColor);
			offset = -limit;
			for (int i = 0; i <= numSegments; i++) {
				p.set(offset, 0.0f, -limit, 1.0f);
				Vector4 s1 = prj.project(p);
				p.set(offset, 0.0f, limit, 1.0f);
				Vector4 e1 = prj.project(p);
				p.set(-limit, 0.0f, offset, 1.0f);
				Vector4 s2 = prj.project(p);
				p.set(limit, 0.0f, offset, 1.0f);
				Vector4 e2 = prj.project(p);
				g.drawLine((int) s1.x(), (int) s1.y(), (int) e1.x(),
						(int) e1.y());
				g.drawLine((int) s2.x(), (int) s2.y(), (int) e2.x(),
						(int) e2.y());
				offset += delta;
			}
		}
		if (zEnable) {
			final Color lineColor = new Color(0.75f, 0.75f, 1.0f);
			g.setColor(lineColor);
			offset = -limit;
			for (int i = 0; i <= numSegments; i++) {
				p.set(offset, -limit, 0.0f, 1.0f);
				Vector4 s1 = prj.project(p);
				p.set(offset, limit, 0.0f, 1.0f);
				Vector4 e1 = prj.project(p);
				p.set(-limit, offset, 0.0f, 1.0f);
				Vector4 s2 = prj.project(p);
				p.set(limit, offset, 0.0f, 1.0f);
				Vector4 e2 = prj.project(p);
				g.drawLine((int) s1.x(), (int) s1.y(), (int) e1.x(),
						(int) e1.y());
				g.drawLine((int) s2.x(), (int) s2.y(), (int) e2.x(),
						(int) e2.y());
				offset += delta;
			}
		}
	}

	protected void drawFace(Graphics2D g, PointData p1, PointData p2,
			PointData p3, HullFace face) {
		boolean visible = face.getShade() <= VISIBILITY_EPSILON;
		float shadeMult = (float) Math
				.sqrt((face.getShade() <= VISIBILITY_EPSILON) ? -face
						.getShade() : (1.0f - face.getShade()));
		shadeMult = 0.5f + 0.5f * shadeMult;
		int[] x = new int[3];
		int[] y = new int[3];
		x[0] = (int) p1.screenPos.x();
		y[0] = (int) p1.screenPos.y();
		x[1] = (int) p2.screenPos.x();
		y[1] = (int) p2.screenPos.y();
		x[2] = (int) p3.screenPos.x();
		y[2] = (int) p3.screenPos.y();

		float fr = face.getColor().getRed() * shadeMult * (1.0f / 255);
		float fg = face.getColor().getGreen() * shadeMult * (1.0f / 255);
		float fb = face.getColor().getBlue() * shadeMult * (1.0f / 255);
		Color colorVisible = new Color(fr, fg, fb,
				enableTransparentFaces ? 0.5f : 1.0f);
		final Color colorHidden = new Color(0.75f * shadeMult,
				0.75f * shadeMult, 0.75f * shadeMult,
				enableTransparentFaces ? 0.5f : 1.0f);
		g.setColor(visible ? colorVisible : colorHidden);
		g.fillPolygon(x, y, 3);

		if (enableDebugInfo) {
			// Draw normals
			Vector4 wc = new Vector4();
			wc.x(p1.worldPos.x() + p2.worldPos.x() + p3.worldPos.x());
			wc.y(p1.worldPos.y() + p2.worldPos.y() + p3.worldPos.y());
			wc.z(p1.worldPos.z() + p2.worldPos.z() + p3.worldPos.z());
			wc.w(1.0f);
			wc.multSelf(1.0f / 3.0f);
			Vector4 pc = prj.project(wc);
			Vector4 pn = prj.project(wc.add(face.getNormal()));
			g.setColor(Color.BLUE);
			g.drawLine((int) pc.x(), (int) pc.y(), (int) pn.x(), (int) pn.y());
		}
	}

	protected void drawEdge(Graphics2D g, PointData p1, PointData p2,
			HullEdge edge, boolean visible) {
		final Color colorVisible = new Color(0.0f, 0.0f, 0.0f);
		final Color colorHidden = new Color(0.4f, 0.4f, 0.4f);

		// Check if the two adjacent faces are coplanar
		if (enableDebugEdges || !edge.getFace(0).isCoplanar(edge.getFace(1))) {
			int x1 = (int) p1.screenPos.x();
			int y1 = (int) p1.screenPos.y();
			int x2 = (int) p2.screenPos.x();
			int y2 = (int) p2.screenPos.y();
			g.setColor(enableDebugEdges ? Color.MAGENTA
					: (visible ? colorVisible : colorHidden));
			g.drawLine(x1, y1, x2, y2);
		}
	}

	protected void drawVertex(Graphics2D g, PointData p, boolean visible,
			boolean onHull) {
		final Color colorVisible = new Color(0.0f, 0.0f, 0.0f);
		final Color colorHidden = new Color(0.4f, 0.4f, 0.4f);
		final Color bulletColor = new Color(0.0f, 0.8f, 0.0f);

		int x = (int) p.screenPos.x();
		int y = (int) p.screenPos.y();
		g.setColor(visible ? colorVisible : colorHidden);
		if (onHull) {
			g.fillOval(x - 4, y - 4, 9, 9);
			if (p.vertex.getName().equals(selectedStation)) {
				g.setColor(SELECTED_STATION_COLOR);
			} else {
				g.setColor(visible ? bulletColor : Color.LIGHT_GRAY);
			}
			g.fillOval(x - 2, y - 2, 5, 5);

			if (enableHullVerticesLabels
					|| p.vertex.getName().equals(selectedStation)) {
				String s = p.vertex.getName() + " " + p.worldPos.toString3D();
				g.setColor(visible ? colorVisible : colorHidden);
				//g.drawString(s, x + 4, y + 4);
				//g.drawString(s, x + 4, y + 6);
				//g.drawString(s, x + 6, y + 4);
				//g.drawString(s, x + 6, y + 6);
				//g.drawString(s, x + 5, y + 4);
				//g.drawString(s, x + 5, y + 6);
				//g.drawString(s, x + 4, y + 5);
				//g.drawString(s, x + 6, y + 5);
				if (p.vertex.getName().equals(selectedStation)) {
					g.setColor(SELECTED_STATION_COLOR);
				} else {
					g.setColor(Color.BLACK);
				}
				g.drawString(s, x + 5, y + 5);
			}
		} else {
			g.fillOval(x - 4, y - 4, 9, 9);
			if (p.vertex.getName().equals(selectedStation)) {
				g.setColor(SELECTED_STATION_COLOR);
				String s = p.vertex.getName() + " " + p.worldPos.toString3D();
				g.setColor(visible ? colorVisible : colorHidden);
				//g.drawString(s, x + 4, y + 4);
				//g.drawString(s, x + 4, y + 6);
				//g.drawString(s, x + 6, y + 4);
				//g.drawString(s, x + 6, y + 6);
				//g.drawString(s, x + 5, y + 4);
				//g.drawString(s, x + 5, y + 6);
				//g.drawString(s, x + 4, y + 5);
				//g.drawString(s, x + 6, y + 5);
				g.setColor(SELECTED_STATION_COLOR);
				g.drawString(s, x + 5, y + 5);
			} else {
				g.setColor(Color.LIGHT_GRAY);
			}
			g.fillOval(x - 2, y - 2, 5, 5);
		}
	}

	/**
	 * Formats a floating point value with the specified number of decimals.
	 * 
	 * @param value
	 *            Value to convert into string.
	 * @param decimals
	 *            Number of decimals.
	 * @return
	 */
	protected static String floatString(float value, int decimals) {
		String res = (value >= 0 ? " " : "") + String.valueOf(value);
		int point = res.indexOf(".") + 1;
		if (decimals < 0) {
			throw new InvalidParameterException("decimals < 0");
		} else if (decimals == 0) {
			return res.substring(0, point - 2);
		} else {
			while (res.length() - point < decimals) {
				res += "0";
			}
			if (res.length() - point > decimals) {
				res = res.substring(0, point + decimals);
			}
			return res;
		}
	}

	/**
	 * Prints matrix coefficients in a human-readable manner.
	 * 
	 * @param g
	 *            Graphics context.
	 * @param m
	 *            Matrix to print.
	 * @param x
	 *            Left coordinate.
	 * @param y
	 *            Top coordinate.
	 * @param name
	 *            Displayed matrix name.
	 * @return The new Y coordinate after printing text lines.
	 */
	protected int printMatrix4(Graphics2D g, Matrix4 m, int x, int y,
			String name) {
		int fontSize = g.getFont().getSize();
		g.drawString(name + " =", x, y);
		y += fontSize;
		g.drawString("[", x, y);
		y += fontSize;
		for (int r = 0; r < 4; r++) {
			for (int c = 0; c < 4; c++) {
				g.drawString(floatString(m.get(r, c), 6), x + 24 + (c * 75), y);
			}
			y += fontSize;
		}
		g.drawString("]", x, y);
		return y;
	}

	/**
	 * Prints useful internal data, such as rotation angles and transform
	 * matrices.
	 * 
	 * @param g
	 *            Graphics context.
	 */
	protected void printViewState(Graphics2D g) {
		int y = 16;
		g.setColor(Color.LIGHT_GRAY);
		g.setFont(FONT);
		// FontMetrics fm = g.getFontMetrics();
		g.drawString("rotY = " + floatString(rotY, 6) + " rad", 16, y);
		g.drawString("= " + floatString((float) (rotY * 180 / Math.PI), 6)
				+ "°", 110, y);
		y += 10;
		g.drawString("rotX = " + floatString(rotX, 6) + " rad", 16, y);
		g.drawString("= " + floatString((float) (rotX * 180 / Math.PI), 6)
				+ "°", 110, y);
		y += 16;

		y = printMatrix4(g, prj.getModelView(), 16, y + 16, "viewMatrix");
		y = printMatrix4(g, prj.getProjection(), 16, y + 16, "projMatrix");
		y = printMatrix4(g, eyeRT, 16, y + 16, "eyeRT");
	}

	/**
	 * Keeps a value within bounds.
	 * 
	 * @param min
	 *            Minimum value.
	 * @param value
	 *            Value to check.
	 * @param max
	 *            Maximum value.
	 * @return The clamped value.
	 */
	protected static float clamp(float min, float value, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	/**
	 * @return The internal ScreenProjector instance.
	 */
	public ScreenProjector getScreenProjector() {
		return prj;
	}

	/**
	 * Projects 3D points onto the screen and repaints the diagram.
	 */
	public void updateProjection() {
		final Vector4 nx = Vector4.createPoint3D(2.0f, 0.0f, 0.0f);
		final Vector4 ny = Vector4.createPoint3D(0.0f, 2.0f, 0.0f);
		final Vector4 nz = Vector4.createPoint3D(0.0f, 0.0f, 2.0f);
		final Vector4 lx = Vector4.createPoint3D(1.7f, 0.0f, 0.0f);
		final Vector4 ly = Vector4.createPoint3D(0.0f, 1.2f, 0.0f);
		final Vector4 lz = Vector4.createPoint3D(0.0f, 0.0f, 1.7f);

		if (zoomFactor > 1.0f) {
			rotXdelta /= zoomFactor;
			rotYdelta /= zoomFactor;
		}

		// Keep pitch rotation between ±90°
		rotX = clamp((float) Math.toRadians(-89), rotX + rotXdelta,
				(float) Math.toRadians(89));

		// Yaw rotation modulo 360°
		rotY -= rotYdelta;
		if (rotY < 0) {
			rotY += (2 * Math.PI);
		}
		rotY %= 2 * Math.PI;

		rotXdelta = 0;
		rotYdelta = 0;

		Dimension size = getSize();
		if (size.width < 32 || size.height < 32) {
			return;
		}
		screenW = size.width;
		screenH = size.height;
		halfW = screenW / 2;
		halfH = screenH / 2;
		aspect = screenW / screenH;
		prj.setScreenSize(size);
		prj.updateProjection();

		Matrix4 mv = prj.getModelView();
		eyeRT = Matrix4.createScale(0.95f * (enableCustomZoom ? zoomFactor
				: 1.0f));
		eyeRT.multSelf(Matrix4.createRotationX(rotX));
		eyeRT.multSelf(Matrix4.createRotationY(rotY));
		eyeRT.multSelf(Matrix4.createTranslation(-centroid.x(), centroid.y(),
				centroid.z()));
		Vector4 eyePos = Vector4.createPoint3D(0.0f, 0.0f, 10.0f);
		prj.lookAt(eyePos, Vector4.ORIGIN, false);
		prj.push(mv.clone());
		prj.push(eyeRT.clone());
		mv.set(eyeRT.mult(mv));

		originPrj = prj.project(Vector4.ORIGIN);
		axisPrjX = prj.project(nx);
		axisPrjY = prj.project(ny);
		axisPrjZ = prj.project(nz);
		axisLabelX = prj.project(lx);
		axisLabelY = prj.project(ly);
		axisLabelZ = prj.project(lz);
		eyeRT.set(prj.pop());

		eyeRT.multSelf(Matrix4.createScale((aspect < 1.0f ? aspect : 1.0f)
				/ maxVertexDistance));
		mv.set(eyeRT.mult(prj.pop()));

		// TODO: Project points here instead of inside of paint()

		forward = mv.getRow(Matrix4.Z_INDEX);
		forward.normalize();
		splitByVisibility(forward);
	}

	public void componentHidden(ComponentEvent evt) {
	}

	public void componentMoved(ComponentEvent evt) {
	}

	public void componentResized(ComponentEvent evt) {
		repaint();
	}

	public void componentShown(ComponentEvent evt) {
		repaint();
	}

	public void mouseClicked(MouseEvent evt) {
		if (evt.getButton() == MouseEvent.BUTTON2) {
			// TODO: Picking
		} else if (evt.getButton() == MouseEvent.BUTTON3) {
			rightClick(evt);
		}
		repaint();
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	public void mousePressed(MouseEvent evt) {
		switch (evt.getButton()) {
		case MouseEvent.BUTTON1: {
			mouseLeft = true;
			break;
		}
		case MouseEvent.BUTTON2: {
			mouseRight = true;
			break;
		}
		}
		if (mouseMode == MouseMode.NONE) {
			// No action already set for mouse movements, so begin a new action.
			if (mouseLeft) {
				mouseMode = MouseMode.ROTATE_VIEW;
			} else if (mouseRight) {
				mouseMode = MouseMode.MOVE_VERTEX;
			}
			// This is the starting point for mouse tracking
			mouseStart.x = evt.getX();
			mouseStart.y = evt.getY();
			mouseEnd.x = mouseStart.x;
			mouseEnd.y = mouseStart.y;
			mouseDelta = new Point(0, 0);
		} else {
			// This is the last mouse action, so keep track of it.
			mouseEnd.x = evt.getX();
			mouseEnd.y = evt.getY();
			mouseDelta = new Point(mouseEnd.x - mouseStart.x, mouseEnd.y
					- mouseStart.y);
		}
		repaint();
	}

	public void mouseReleased(MouseEvent evt) {
		switch (evt.getButton()) {
		case MouseEvent.BUTTON1: {
			mouseLeft = false;
			break;
		}
		case MouseEvent.BUTTON2: {
			mouseRight = false;
			break;
		}
		}
		if (!mouseLeft && !mouseRight) {
			mouseMode = MouseMode.NONE;
		}
		// This is the last mouse action, so keep track of it.
		mouseEnd.x = evt.getX();
		mouseEnd.y = evt.getY();
		mouseDelta = new Point(mouseEnd.x - mouseStart.x, mouseEnd.y
				- mouseStart.y);
		repaint();
	}

	public void mouseDragged(MouseEvent evt) {
		handleMovements(evt);
	}

	public void mouseMoved(MouseEvent evt) {
		handleMovements(evt);
	}

	protected void handleMovements(MouseEvent evt) {
		if (mouseMode != MouseMode.NONE) {
			// This is the last mouse action, so keep track of it.
			mouseEnd.x = evt.getX();
			mouseEnd.y = evt.getY();
			mouseDelta = new Point(mouseEnd.x - mouseStart.x, mouseEnd.y
					- mouseStart.y);
			Dimension screenSize = prj.getScreenSize();
			switch (mouseMode) {
			case ROTATE_VIEW: {
				rotYdelta = (float) (mouseDelta.x * (Math.PI / screenSize.width));
				rotXdelta = (float) (mouseDelta.y * (Math.PI / screenSize.height));
				break;
			}
			case MOVE_VERTEX: {
				// TODO
				break;
			}
			default: {
				break;
			}
			}

			// Mouse movement was processed, so keep track of a new movement.
			mouseStart.x = mouseEnd.x;
			mouseStart.y = mouseEnd.y;

			repaint();
		}
	}

	public void selectStation(String name) {
		selectedStation = name;
		repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent evt) {
		if (enableCustomZoom) {
			int n = evt.getWheelRotation();
			zoomFactor *= ((n < 0) ? (-n * 1.1f) : (n * 0.9f));
			repaint();
		} else {
			// TODO: Add super signal
		}
	}

}
