package jmt.gui.jaba.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;
import jmt.gui.jaba.graphs.Convex2DGraph;

public class ConvexHullPanel extends WizardPanel implements ActionListener, ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int BORDER_SIZE = 20;

	// Keeps old data (used to avoid to redraw panel at each focus)
	private JabaModel data;
	private boolean redrawNeeded;
	private JabaWizard mainWin;

	private Convex2DGraph painter2D;
	private JCheckBox showLabelsBox;

	public ConvexHullPanel(JabaWizard mainWin) {
		super();
		initComponents();
		this.mainWin = mainWin;

		setPreferredSize(new Dimension(200, 200));
		// setBackground(Color.white);
	}

	  
	public void setData(JabaModel data) {
		if (this.data != data)
			redrawNeeded = true;
		this.data = data;
		repaint();
	}

	private void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(BORDER_SIZE, BORDER_SIZE,
				BORDER_SIZE, BORDER_SIZE));
		this.setLayout(new GridLayout(1, 1));
	}

	  
	public String getName() {
		return "Convex Hull - Graph";
	}

	public void redraw() {
		// Redraws only if data has changed - Bertoli Marco
		if (!redrawNeeded)
			return;
		if (data.hasResults() && data.areResultsOK()
				&& data.getResults().getSaturationSectors().size() > 0) {
			if (data.getClasses() == 2) {
				this.removeAll();
				painter2D = new Convex2DGraph(data, mainWin);
				this.setLayout(new BorderLayout());
				showLabelsBox = new JCheckBox(
						JabaConstants.OPTION_SHOW_ALL_LABELS, true);
				showLabelsBox.addActionListener(this);
				showLabelsBox.addChangeListener(this);
				showLabelsBox.addItemListener(this);
				this.add(showLabelsBox, BorderLayout.PAGE_START);
				this.add(new JScrollPane(painter2D), BorderLayout.CENTER);
				this.add(new JLabel(JabaConstants.DESCRIPITION_GRAPH),
						BorderLayout.PAGE_END);
				repaint();
			} else if (data.getClasses() == 3) {
				this.removeAll();
				// Under costruction
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
		redrawNeeded = false;
	}

	  
	public void gotFocus() {
		redraw();
	}

	  
	public void itemStateChanged(ItemEvent arg0) {
		if(showLabelsBox.getModel().isSelected())
			painter2D.showAllLabels(true);
		else
			painter2D.showAllLabels(false);
	}

	  
	public void stateChanged(ChangeEvent arg0) {
		if(showLabelsBox.getModel().isSelected())
			painter2D.showAllLabels(true);
		else
			painter2D.showAllLabels(false);
	}

	  
	public void actionPerformed(ActionEvent arg0) {
		if(showLabelsBox.getModel().isSelected())
			painter2D.showAllLabels(true);
		else
			painter2D.showAllLabels(false);
	}

}
