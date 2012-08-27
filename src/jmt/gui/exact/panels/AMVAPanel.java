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
import javax.swing.JTextField;
import jmt.analytical.SolverAlgorithm;
import jmt.analytical.SolverMultiClosedAMVA;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.exact.ExactWizard;

/**
 * Panel representing the combo box on ExactWizard (GUI of JMVA)
 * 
 * @author Abhimanyu Chugh
 *
 */
public final class AMVAPanel extends WizardPanel {
	private static final long serialVersionUID = 1L;
	
	private HoverHelp help;
	private NumberFormat numFormat = new DecimalFormat("#.###############");
	private static final String helpText = "<html>In this panel you can select the algorithm that the solver will use.<br><br>";

	private static ExactWizard ew;
	
	public static JLabel tolLabel, algLabel;
	public static JTextField tolerance;
	public static String algorithm;
	public static JComboBox algorithmList;
	public static String [] modelName;
	
	private ActionListener ACTION_CHANGE_ALGORITHM = new ActionListener() {
		// initial value
		int currentItem = 1;
		
		public void actionPerformed(ActionEvent e) {
			JComboBox algorithmList = (JComboBox)e.getSource();
			algorithm = (String)algorithmList.getSelectedItem();
			
			// check if algorithm or not
			if (SolverAlgorithm.find(algorithm) == null) {
				algorithmList.setSelectedIndex(currentItem);
			} else {
				currentItem = algorithmList.getSelectedIndex();
				ew.getData().setAlgorithmType(algorithm);
				SolverAlgorithm alg = SolverAlgorithm.find(algorithm);
				boolean exact = alg != null && SolverAlgorithm.isExact(alg);
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
	
	public static void enableOrDisableAlgPanel(boolean enabled) {
		algLabel.setEnabled(enabled);
		algorithmList.setEnabled(enabled);
		tolLabel.setEnabled(enabled);
		tolerance.setEnabled(enabled);
	}
	
	public static void showToleranceField(boolean show) {
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
				if (SolverAlgorithm.find(str) == null) {
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
	
	public static void setAlgorithm (String alg) {
		algorithm = alg;
	}
	
	public String getName() {
		return "Classes";
	}
	
	public void update() {
		algorithm = ew.getData().getAlgorithmType();
		tolerance.setText(numFormat.format(ew.getData().getTolerance()));
		algorithmList.setSelectedItem(algorithm);
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
