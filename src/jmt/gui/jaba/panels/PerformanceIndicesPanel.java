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
package jmt.gui.jaba.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.jaba.JabaConstants;
import jmt.gui.jaba.JabaModel;
import jmt.gui.jaba.JabaWizard;
import jmt.gui.jaba.graphs.PerformanceIndices2DGraph;

/**
 * 
 * @author Sebastiano Spicuglia
 *
 */
public class PerformanceIndicesPanel extends WizardPanel implements
		ActionListener {

	private static final long serialVersionUID = 1L;
	private JabaModel data;
	private boolean redrawNeeded;
	private JComboBox whichIndexComboBox;
	private String[] whichIndexArray = { "Utilization", "Throughput",
			"Response time" };
	private PerformanceIndices2DGraph graph;

	public PerformanceIndicesPanel(JabaWizard jabaWizard) {
		super();
		initComponents();
		repaint();
		
	}

	private void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		this.setLayout(new GridLayout(1, 1));
		redraw();
	}

	  
	public void redraw() {
		// We redraw only if data has changed - Sebastiano Spicuglia
		if (!redrawNeeded)
			return;
		this.removeAll();
		if (data.hasResults() && data.areResultsOK()
				&& data.getResults().getSaturationSectors().size() > 0) {
			if (data.getClasses() == 2) {
				this.removeAll();
				this.setLayout(new BorderLayout());
				graph = new PerformanceIndices2DGraph(data);
				this.add(new JScrollPane(graph), BorderLayout.CENTER);
				this.add(new JLabel(JabaConstants.DESCRIPITION_GRAPH_PERFORMANCE_INDEX),
						BorderLayout.PAGE_END);
				JPanel flowPanel = new JPanel(
						new FlowLayout(FlowLayout.LEADING));
				flowPanel.add(new JLabel(JabaConstants.SELECT_WHICH_INDEX));
				whichIndexComboBox = new JComboBox(whichIndexArray);
				whichIndexComboBox.addActionListener(this);
				flowPanel.add(whichIndexComboBox);
				//this.add(flowPanel, BorderLayout.PAGE_START);
				repaint();
			} else if (data.getClasses() == 3) {
				this.removeAll();
				// Not supported
				repaint();
			}
		} else {
			JEditorPane synView = new JTextPane();
			synView.setContentType("text/html");
			synView.setEditable(false);
			JScrollPane synScroll = new JScrollPane(synView);
			synScroll
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			synScroll
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			synView.setText("<html><body><center><font face=\"bold\" size=\"3\">Utilization will be here displayed once you solve the model.</font></center></body></html>");
			this.add(synScroll);
			repaint();
		}
		redrawNeeded = false;
	}

	  
	public void setData(JabaModel data) {
		if (this.data != data)
			redrawNeeded = true;
		this.data = data;
		repaint();
	}

	
	  
	public String getName() {
		return "Utilization - Graph";
		//return "Performance Indices - Graphics";
	}

	  
	public void gotFocus() {
		redraw();
	}

	  
	public void actionPerformed(ActionEvent arg0) {
		if(graph!=null) {
			int index = whichIndexComboBox.getSelectedIndex();
			graph.drawIndex(index);
		}
	}

}
