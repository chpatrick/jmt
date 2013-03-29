package jmt.gui.jaba.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.graphs.JabaCanvas;
import jmt.gui.jaba.graphs.Sectors2DGraph;
import jmt.gui.jaba.graphs.Sectors3DGraph;

// Pannello iniziale che conterrà quelli specifici a 2 o 3 dimensioni
public class SectorsGraphicPanel extends WizardPanel {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	// Keeps old data (used to avoid to redraw panel at each focus)
	private JabaModel data;
	private boolean redrawNeeded;
	Sectors2DGraph s2dp;

	public SectorsGraphicPanel() {
		super();
		initComponents();
	}

	@Override
	public String getName() {
		return "Saturation Sectors - Graph";
	}

	@Override
	public void gotFocus() {
		redraw();
	}

	@Override
	public void redraw() {
		// Redraws only if data has changed - Bertoli Marco
		if (!redrawNeeded)
			return;

		if (data.hasResults() && data.areResultsOK()
				&& data.getResults().getSaturationSectors().size() > 0) {
			if (data.getClasses() == 2) {
				this.removeAll();
				s2dp = new Sectors2DGraph(data);
				this.setLayout(new BorderLayout());
				this.add(new JabaCanvas(s2dp), BorderLayout.CENTER);
				this.add(new JLabel(JabaConstants.DESCRIPITION_GRAPH),
						BorderLayout.PAGE_END);
				repaint();
			} else if (data.getClasses() == 3) {
				this.removeAll();
				Sectors3DGraph s3dp = new Sectors3DGraph(data);
				this.setLayout(new BorderLayout());
				this.add(new JabaCanvas(s3dp), BorderLayout.CENTER);
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
			this.add(synScroll, BorderLayout.CENTER);
		}
		redrawNeeded = false;
	}

	@Override
	public void setData(JabaModel data) {
		if (this.data != data)
			redrawNeeded = true;
		this.data = data;
		repaint();
	}

	private void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.setLayout(new GridLayout(1, 1));
	}

}
