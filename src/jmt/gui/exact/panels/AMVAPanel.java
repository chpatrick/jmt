/**    
  * Copyright (C) 2012, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.gui.exact.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmt.analytical.SolverAlgorithm;
import jmt.analytical.SolverMultiClosedAMVA;
import jmt.framework.gui.help.HoverHelp;
import jmt.gui.exact.ExactModel;
import jmt.gui.exact.ExactWizard;

/**
 * Panel representing the combo box on ExactWizard (GUI of JMVA)
 * 
 * @author Abhimanyu Chugh
 *
 */
public final class AMVAPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private HoverHelp help;
	private NumberFormat numFormat = new DecimalFormat("#.###############");

	private ExactWizard ew;
	
	public JLabel tolLabel, algLabel;
	public JTextField tolerance;
	public String algorithm;
	public JComboBox algorithmList;
	public String [] modelName;
	
	private ActionListener ACTION_CHANGE_ALGORITHM = new ActionListener() {
		// initial value
		int currentItem = 1;
		
		public void actionPerformed(ActionEvent e) {
			JComboBox algorithmList = (JComboBox)e.getSource();
			algorithm = (String)algorithmList.getSelectedItem();
			
			// check if algorithm or not
			if (SolverAlgorithm.fromString(algorithm) == null) {
				algorithmList.setSelectedIndex(currentItem);
			} else {
				currentItem = algorithmList.getSelectedIndex();
				ew.getData().setAlgorithmType(SolverAlgorithm.fromString(algorithm));
				SolverAlgorithm alg = SolverAlgorithm.fromString(algorithm);
				boolean exact = alg != null && alg.isExact();
				showToleranceField(!exact);
			}
		}
	};
	
	private ToleranceInputListener ACTION_CHANGE_TOLERANCE = new ToleranceInputListener();
	
	public AMVAPanel(ExactWizard ew) {
		this.ew = ew;
		help = ew.getHelp();
		
		int index = 0;
		String[] names = SolverAlgorithm.closedNames();
		modelName = new String[names.length+2];
		int noOfExactAlgs = SolverAlgorithm.noOfExactAlgs();
		for (int i = 0; i < names.length; i++) {
			if (i == 0) {
				modelName[index] = "--------- Exact ---------";
				index++;
			} else if (i == noOfExactAlgs) {
				modelName[index] = "----- Approximate -----";
				index++;
			}
			modelName[index] = names[i];
			index++;
		}
	}
	
	/**
	 * Enables or disables the AMVA algorithm panel
	 * @param enabled true to enable, false to disable
	 */
	public void setAlgPanelEnabled(boolean enabled) {
		algLabel.setEnabled(enabled);
		algorithmList.setEnabled(enabled);
		tolLabel.setEnabled(enabled);
		tolerance.setEnabled(enabled);
	}
	
	public void showToleranceField(boolean show) {
		tolLabel.setVisible(show);
		tolerance.setVisible(show);
	}
	
	public JComponent algorithmList() {
		Dimension d = new Dimension(160,30);
		algorithmList = new JComboBox(modelName);
		algorithmList.setMaximumSize(d);
		algorithmList.setSelectedIndex(1);
		algorithmList.addActionListener(ACTION_CHANGE_ALGORITHM);
		algorithmList.setRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6074991824391523738L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component comp = super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
				String str = (value == null) ? "" : value.toString();
				if (SolverAlgorithm.fromString(str) == null) {
					comp.setEnabled(false);
					comp.setFocusable(false);
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				} else {
					comp.setEnabled(true);
					comp.setFocusable(true);
				}
				return comp;
			}
		});
		help.addHelp(algorithmList, "Algorithm for solving model");
		return algorithmList;
	}
	
	public JComponent tolLabel() {
		Dimension d = new Dimension(70,30);
		tolLabel = new JLabel("  Tolerance:");
		tolLabel.setMaximumSize(d);
		tolLabel.setFocusable(false);
		return tolLabel;
	}

	public JComponent tolerance() {
		Dimension d = new Dimension(80,30);
		tolerance = new JTextField(10);
		tolerance.setText(numFormat.format(ew.getData().getTolerance()));
		//tolerance.setText(numFormat.format(SolverMultiClosedAMVA.DEFAULT_TOLERANCE));
		tolerance.setMaximumSize(d);
		help.addHelp(tolerance, "Input Tolerance for AMVA Algorithms");
		tolerance.setFocusable(true);
		tolerance.addKeyListener(ACTION_CHANGE_TOLERANCE);
		tolerance.addFocusListener(ACTION_CHANGE_TOLERANCE);
		return tolerance;
	}
	
	public void setAlgorithm (String alg) {
		algorithm = alg;
	}
	
	public String getName() {
		return "Classes";
	}
	
	public void update() {
		ExactModel data = ew.getData();
		algorithm = data.getAlgorithmType().toString();
		tolerance.setText(numFormat.format(data.getTolerance()));
		algorithmList.setSelectedItem(algorithm);
		setAlgPanelEnabled(data.isClosed() && !data.isWhatifAlgorithms());

	}

	public JComponent algLabel() {
		Dimension d = new Dimension(65,30);
		algLabel = new JLabel("Algorithm:");
		algLabel.setMaximumSize(d);
		algLabel.setFocusable(false);
		help.addHelp(algLabel, "Algorithm for solving model");
		return algLabel;
	}

	private void updateTolerance() {
		Double tol = SolverMultiClosedAMVA.validateTolerance(tolerance.getText());
		if (tol != null) {
			ew.getData().setTolerance(tol);
		}
		else {
			JOptionPane.showMessageDialog(ew, "Error: Invalid tolerance value. Using last valid value.", "Input data error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private class ToleranceInputListener implements KeyListener, FocusListener {
		@Override
		public void focusLost(FocusEvent e) {
			updateTolerance();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				updateTolerance();
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}


		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
}
