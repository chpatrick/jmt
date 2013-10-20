/**    
  * Copyright (C) 2013, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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
package jmt.gui.common.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import jmt.framework.gui.components.HtmlPanel;
import jmt.gui.common.panels.AboutDialogFactory.Contributors;
import jmt.gui.common.resources.JMTImageLoader;
import jmt.gui.common.startScreen.GraphStartScreen;

/**
 * This panel draws the about window. It is called by the AboutDialogFactory class.
 * @author Marco Bertoli
 *
 */
public class AboutDialogPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int BORDERSIZE = 15;
	private static final String TITLE_START = "<html><font face=\"Verdana\" size=+4><b>";
	private static final String TITLE_END = "</b></font></html>";

	private static final String LEGAL = "<html><font size='-2'>" + "  This program is free software; you can redistribute it and/or modify "
			+ "  it under the terms of the GNU General Public License as published by "
			+ "  the Free Software Foundation; either version 2 of the License, or " + "  (at your option) any later version." + "<br><br>"
			+ "  This program is distributed in the hope that it will be useful, "
			+ "  but WITHOUT ANY WARRANTY; without even the implied warranty of "
			+ "  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " + "  GNU General Public License for more details."
			+ "</font></html>";

	private JLabel title;
	private String dialogTitle;
	private JPanel mainArea;

	
	/**
	 * Builds an AboutDialogPanel
	 */
	public AboutDialogPanel() {
		super(new BorderLayout());
		initialize();
	}

	/**
	 * Initialize the panel
	 */
	private void initialize() {
		// Initialize dialog layout
		
		JPanel panel = new JPanel(new BorderLayout(BORDERSIZE / 2, BORDERSIZE / 2));
		panel.setBorder(BorderFactory.createEmptyBorder(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE));
		// Adds website image
		JPanel tmpPanel = new JPanel(new BorderLayout(BORDERSIZE, BORDERSIZE));
		// Adds polimi description
		HtmlPanel titleLabel = new HtmlPanel();
		titleLabel.setText(GraphStartScreen.HTML_CONTENT_TITLE_HREF);
		titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		titleLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
		titleLabel.setOpaque(false);
		tmpPanel.add(titleLabel, BorderLayout.CENTER);

		// Adds application title
		title = new JLabel();
		title.setHorizontalTextPosition(SwingConstants.RIGHT);
		title.setHorizontalAlignment(SwingConstants.CENTER);
		title.setIconTextGap(BORDERSIZE);
		tmpPanel.add(title, BorderLayout.SOUTH);

		panel.add(tmpPanel, BorderLayout.NORTH);

		// Adds text area
		mainArea = new JPanel();
		mainArea.setOpaque(false);
		BoxLayout mainLayout = new BoxLayout(mainArea, BoxLayout.Y_AXIS);
		mainArea.setLayout(mainLayout);
		
		panel.add(mainArea, BorderLayout.CENTER);

		JLabel legal = new JLabel(LEGAL);
		panel.add(legal, BorderLayout.SOUTH);
		panel.setPreferredSize(new Dimension(640,480));
		JScrollPane scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(scroll, BorderLayout.CENTER);
	}
	
	/**
	 * Add contributor names to current about window
	 * @param contributors contributors to be added
	 */
	public void setNames(Contributors... contributors) {
		for (Contributors c : contributors) {
			JPanel panel = new JPanel();
			BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
			panel.setLayout(layout);
			mainArea.add(panel);
			panel.add(c.getCompany().getLogo());
			
			StringBuilder sb = new StringBuilder(100);
			sb.append("<html><p><font face='Arial' size='-1'><b>Major Contributors: </b>");
			int idx=0;
			for (String name: c.getNames()) {
				if (idx++ > 0) {
					sb.append(", ");
				}
				sb.append(name);
			}
			sb.append(".</font></p></html>");
			JLabel label = new JLabel(sb.toString());
			label.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
			panel.add(label);
			panel.setBorder(
					BorderFactory.createCompoundBorder(
							BorderFactory.createEmptyBorder(5, 0, 5, 0),
							BorderFactory.createCompoundBorder(
									BorderFactory.createEtchedBorder(), 
									BorderFactory.createEmptyBorder(5, 5, 5, 5))));
		}
	}
	
	/**
	 * Sets the name and the icon of the JMT application to show
	 * @param dialogTitle the name of the dialog
	 * @param applicationName the name of the application
	 * @param iconName the icon of the application
	 */
	public void setTitles(String dialogTitle, String applicationName, String iconName) {
		title.setText(TITLE_START + applicationName + TITLE_END);
		title.setIcon(JMTImageLoader.loadImage(iconName, new Dimension(50, 50)));
		this.dialogTitle = dialogTitle;
	}
	
	/**
	 * @return the name of the dialog to display
	 */
	public String getDialogTitle() {
		return dialogTitle;
	}

}
