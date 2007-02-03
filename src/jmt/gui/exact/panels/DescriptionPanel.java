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
  
package jmt.gui.exact.panels;

import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.exact.ExactConstants;
import jmt.gui.exact.ExactWizard;

import javax.swing.*;
import java.awt.*;

/**

 * @author alyf (Andrea Conti)
 * Date: 11-set-2003
 * Time: 23.48.19

 */

/**
 * description panel
 */
public final class DescriptionPanel extends WizardPanel implements ExactConstants{

	private ExactWizard ew;
	private HoverHelp help;
	private static final String helpText = "<html>In this panel you can enter any text describing this model</html>";

	private JTextPane textPane = new JTextPane();

	public DescriptionPanel(ExactWizard ew) {
		this.ew = ew;
		help = ew.getHelp();

		initComponents();

		/* sync status with data object */
		sync();
	}

	/**
	 * gets status from data object
	 */
	private void sync() {
		textPane.setText(ew.getData().getDescription());
	}

	/**
	 * Set up the panel contents and layout
	 */
	private void initComponents() {
		textPane = new JTextPane();
		help.addHelp(textPane, "Enter any text describing this model");

        //BEGIN Federico Dall'Orso 14/3/2005
        //OLD
        /*
        setLayout(new BorderLayout());
        JScrollPane esp = new JScrollPane(textPane);
        esp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(esp, BorderLayout.CENTER);
        add(Box.createVerticalStrut(20), BorderLayout.NORTH);
        add(Box.createHorizontalStrut(20), BorderLayout.EAST);
        add(Box.createVerticalStrut(20), BorderLayout.SOUTH);
        add(Box.createHorizontalStrut(20), BorderLayout.WEST);
        */
        //NEW

        JPanel intPanel = new JPanel(new BorderLayout(10,10));
        //Box intPanel = Box.createVerticalBox();

        JScrollPane jsp = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JLabel descrLabel = new JLabel(DESCRIPTION_COMMENT);

        intPanel.add(descrLabel, BorderLayout.NORTH);
        intPanel.add(jsp, BorderLayout.CENTER);
        //intPanel.add(jsp);

        setLayout(new BorderLayout());
        add(intPanel, BorderLayout.CENTER);
        add(Box.createVerticalStrut(20), BorderLayout.NORTH);
        add(Box.createVerticalStrut(20), BorderLayout.SOUTH);
        add(Box.createHorizontalStrut(20), BorderLayout.EAST);
        add(Box.createHorizontalStrut(20), BorderLayout.WEST);

        //END Federico Dall'Orso 14/3/2005

	}

	private void commit() {
		String text = textPane.getText();
		if (text == null) text = "";
		ew.getData().setDescription(text);
	}

	public String getName() {
		return "Comment";
	}

	public void gotFocus() {
		sync();
	}

	public void lostFocus() {
		commit();
	}

	public void help() {
		JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
	}

}
