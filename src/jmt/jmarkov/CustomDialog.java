/**    
  * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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

package jmt.jmarkov;

import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/* 1.4 example used by DialogDemo.java. */
class CustomDialog extends JDialog implements ActionListener, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int typedValue;
	private String typedText = null;
	private JPanel selectionP;
	private GridBagConstraints c;
	private JTextField textField;
	private JRadioButton nolimitRB, limitedRB;
	private String magicWord;
	private JOptionPane optionPane;

	private String btnString1 = "Enter";
	private String btnString2 = "Cancel";

	/**
	 * Returns null if the typed string was invalid;
	 * otherwise, returns the string as the user entered it.
	 */
	public String getValidatedText() {
		return typedText;
	}

	public int getValidatedValue() {
		return typedValue;
	}

	/** Creates the reusable dialog. */
	public CustomDialog(Frame aFrame) {
		super(aFrame, true);
		setTitle("Enter n. of jobs to simulate");
		textField = new JTextField(10);
		textField.setEnabled(false);
		textField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE))) {
					getToolkit().beep();
					e.consume();
				}
			}
		});
		textField.setBackground(Color.LIGHT_GRAY);
		nolimitRB = new JRadioButton("Unlimited", true);
		nolimitRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				limitedRB.setSelected(false);
				textField.setEnabled(false);
				textField.setBackground(Color.LIGHT_GRAY);
			}
		});
		limitedRB = new JRadioButton("Limited (Type in)", false);
		limitedRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nolimitRB.setSelected(false);
				textField.setEnabled(true);
				textField.setBackground(Color.WHITE);
			}
		});

		//adding to panel
		selectionP = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridx = 0;
		c.gridy = 0;
		selectionP.add(nolimitRB, c);
		c.gridy = 1;
		selectionP.add(limitedRB, c);
		c.gridx = 1;
		selectionP.add(textField, c);

		//Create an array of the text and components to be displayed.
		String msgString1 = "Select how many jobs you ";
		String msgString2 = "want to simulate";
		Object[] array = { msgString1, msgString2, selectionP };

		//Create an array specifying the number of dialog buttons
		//and their text.
		Object[] options = { btnString1, btnString2 };

		//Create the JOptionPane.
		optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION, null, options, options[0]);

		//Make this dialog display it.
		setContentPane(optionPane);

		//Handle window closing correctly.
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				/*
				 * Instead of directly closing the window,
				 * we're going to change the JOptionPane's
				 * value property.
				 */
				optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
			}
		});

		//Ensure the text field always gets the first focus.
		addComponentListener(new ComponentAdapter() {
			public void componentShown(ComponentEvent ce) {
				textField.requestFocusInWindow();
			}
		});

		//Register an event handler that puts the text into the option pane.
		textField.addActionListener(this);

		//Register an event handler that reacts to option pane state changes.
		optionPane.addPropertyChangeListener(this);
	}

	/** This method handles events for the text field. */
	public void actionPerformed(ActionEvent e) {
		optionPane.setValue(btnString1);
	}

	/** This method reacts to state changes in the option pane. */
	public void propertyChange(PropertyChangeEvent e) {
		String prop = e.getPropertyName();

		if (isVisible() && (e.getSource() == optionPane)
				&& (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
			Object value = optionPane.getValue();

			if (value == JOptionPane.UNINITIALIZED_VALUE) {
				//ignore reset
				return;
			}

			//Reset the JOptionPane's value.
			//If you don't do this, then if the user
			//presses the same button next time, no
			//property change event will be fired.
			optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

			if (btnString1.equals(value)) {
				if (nolimitRB.isSelected()) {
					typedText = "unlimited";
					typedValue = 0;
					clearAndHide();
				} else {
					typedText = textField.getText();
					try {
						typedValue = Integer.parseInt(typedText);
					} catch (NumberFormatException nfe) {
						typedValue = 0;
					}
					if ((typedValue > 100000) || (typedValue < 1)) {
						//text was invalid
						textField.selectAll();
						JOptionPane.showMessageDialog(CustomDialog.this, "Sorry, '" + typedValue + "' " + "isn't a valid response.\n"
								+ "Please enter a number between 1 and 100'000.", "Please try again", JOptionPane.ERROR_MESSAGE);
						typedText = null;
						typedValue = 0;
						textField.requestFocusInWindow();
					} else {
						clearAndHide();
					}
				}
			} else { //user closed dialog or clicked cancel
				typedValue = 0;
				typedText = null;
				clearAndHide();
			}
		}
	}

	/** This method clears the dialog and hides it. */
	public void clearAndHide() {
		textField.setText(null);
		setVisible(false);
	}
}
