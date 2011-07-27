package jmt.gui.jaba.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;

import jmt.framework.gui.layouts.SpringUtilities;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;
import jmt.gui.jaba.graphs.Convex2DGraph;
import jmt.gui.jaba.graphs.Convex3DGraph;
import jmt.gui.jaba.graphs.JabaCanvas;

public class ConvexHullPanel extends WizardPanel implements ActionListener,
		ChangeListener, ItemListener {
	
	private static final String POINTS = "Points";

	private static final String WIREFRAME = "Wireframe";

	private static final String SOLID = "Solid";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int BORDER_SIZE = 20;

	// Keeps old data (used to avoid to redraw panel at each focus)
	private JabaModel data;
	private JabaWizard mainWin;

	private Convex2DGraph painter2D;
	private Convex3DGraph painter3D;
	private JCheckBox showLabelsBox;

	private JComboBox renderModeComboBox;

	private JCheckBox showLabels3DBox;

	private JCheckBox showOnlyBottleneckBox;

	private JComboBox stationList;

	public ConvexHullPanel(JabaWizard mainWin) {
		super();
		initComponents();
		this.mainWin = mainWin;

		setPreferredSize(new Dimension(200, 200));
	}

	public void setData(JabaModel data) {
		this.data = data;
		repaint();
	}

	private void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE,
				BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
		this.setLayout(new GridLayout(1, 1));
	}

	public String getName() {
		return "Convex Hull - Graph";
	}

	public void redraw() {
		if (data.hasResults() && data.areResultsOK()
				&& data.getResults().getSaturationSectors().size() > 0) {
			if (data.getClasses() == 2) {
				this.removeAll();
				painter2D = new Convex2DGraph(data, mainWin);
				this.setLayout(new BorderLayout());
				showLabelsBox = new JCheckBox(
						JabaConstants.OPTION_SHOW_ONLY_BOTTLENECK, true);
				showLabelsBox.setSelected(false);
				showLabelsBox.addActionListener(this);
				showLabelsBox.addChangeListener(this);
				showLabelsBox.addItemListener(this);
				this.add(showLabelsBox, BorderLayout.PAGE_START);
				this.add(new JabaCanvas(painter2D), BorderLayout.CENTER);
				this.add(new JLabel(JabaConstants.DESCRIPITION_CONVEX_2D_GRAPH),
						BorderLayout.PAGE_END);
				repaint();
			} else if (data.getClasses() == 3) {
				this.removeAll();
				painter3D = new Convex3DGraph(data, mainWin);
				this.setLayout(new BorderLayout());
				this.add(make3DOptionPanel(), BorderLayout.EAST);
				this.add(new JabaCanvas(painter3D), BorderLayout.CENTER);
				this.add(new JLabel(JabaConstants.DESCRIPITION_GRAPH),
						BorderLayout.PAGE_END);

				repaint();
			}
		} else {
			this.removeAll();
			JEditorPane synView = new JTextPane();
			synView.setContentType("text/html");
			synView.setEditable(false);
			JScrollPane synScroll = new JScrollPane(synView);
			synScroll
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			synScroll
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			synView.setText("<html><body><center><font face=\"bold\" size=\"3\">Saturation Sectors will be here displayed once you solve the model.</font></center></body></html>");
			this.add(synScroll);
			repaint();
		}
	}

	private JPanel make3DOptionPanel() {
		JPanel omni = new JPanel(new BorderLayout());
		omni.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE,
				BORDER_SIZE, BORDER_SIZE, BORDER_SIZE));
		JPanel res = new JPanel(new SpringLayout());

		//FIRST ROW
		String[] renderMode = {SOLID, WIREFRAME, POINTS};
		renderModeComboBox = new JComboBox(renderMode);
		renderModeComboBox.addActionListener(this);
		res.add(new JLabel("Render mode"));
		res.add(renderModeComboBox);
		//SECOND ROW
		res.add(new JLabel("View labels"));
		showLabels3DBox = new JCheckBox(
				"", true);
		showLabels3DBox.addActionListener(this);
		showLabels3DBox.addChangeListener(this);
		showLabels3DBox.addItemListener(this);
		res.add(showLabels3DBox);
		//THIRD ROW
		res.add(new JLabel("Show only bottlenecks  "));
		showOnlyBottleneckBox = new JCheckBox(
				"", true);
		showOnlyBottleneckBox.addActionListener(this);
		showOnlyBottleneckBox.addChangeListener(this);
		showOnlyBottleneckBox.addItemListener(this);
		res.add(showOnlyBottleneckBox);
		//FOURTH ROW
		String stations[] = new String[data.getStations()+1];
		stations[0] = null;
		for(int i = 0; i < data.getStations(); i++)
			stations[i+1] = data.getStationNames()[i];
		stationList = new JComboBox(stations);
		stationList.addActionListener(this);
		res.add(new JLabel("Select"));
		res.add(new JScrollPane(stationList));
		SpringUtilities.makeCompactGrid(res, 4, 2, //rows, cols
				6, 6, //initX, initY
				6, 6);//xPad, yPad		
		
		//CENTER PANEL
		omni.add(res, BorderLayout.NORTH);
		omni.add(new JLabel(JabaConstants.CONVEX_HULL_VERTEX_EXPLANATION), BorderLayout.CENTER);
		omni.add(new JLabel("   "), BorderLayout.SOUTH);
		return omni;
	}

	public void gotFocus() {
		redraw();
	}

	public void itemStateChanged(ItemEvent ev) {
	}

	public void stateChanged(ChangeEvent ev) {
		if (ev.getSource() == showLabelsBox) {
			if (showLabelsBox.getModel().isSelected())
				painter2D.showAllLabels(true);
			else
				painter2D.showAllLabels(false);
		} else if(ev.getSource() == showLabels3DBox ) {
			if (showLabels3DBox.getModel().isSelected())
				painter3D.setEnableHullVerticesLabels(true);
			else
				painter3D.setEnableHullVerticesLabels(false);
			painter3D.repaint();
		} else if(ev.getSource() == showOnlyBottleneckBox ) {
			if (showOnlyBottleneckBox.getModel().isSelected())
				painter3D.setEnableInternalVertices(false);
			else
				painter3D.setEnableInternalVertices(true);
			painter3D.repaint();
		}
	}

	public void actionPerformed(ActionEvent ev) {
		if (ev.getSource() == showLabelsBox) {
			if (showLabelsBox.getModel().isSelected())
				painter2D.showAllLabels(true);
			else
				painter2D.showAllLabels(false);
		} else if(ev.getSource() == renderModeComboBox) {
			String s = (String) renderModeComboBox.getSelectedItem();
			if(s.equals(SOLID)){
				painter3D.setEnableVisibleEdges(true);
				painter3D.setEnableHiddenEdges(false);
				painter3D.setEnableVisibleFaces(true);
				painter3D.setEnableHiddenFaces(true);
			} else if(s.equals(WIREFRAME)) {
				painter3D.setEnableVisibleEdges(true);
				painter3D.setEnableHiddenEdges(true);
				painter3D.setEnableVisibleFaces(false);
				painter3D.setEnableHiddenFaces(false);
			} else if(s.equals(POINTS)) {
				painter3D.setEnableVisibleEdges(false);
				painter3D.setEnableHiddenEdges(false);
				painter3D.setEnableVisibleFaces(false);
				painter3D.setEnableHiddenFaces(false);
			}
			painter3D.repaint();
		} else if(ev.getSource() == stationList) {
			painter3D.selectStation((String) stationList.getSelectedItem());
		}
	}

}
