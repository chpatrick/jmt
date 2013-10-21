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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import jmt.framework.gui.components.JMTDialog;
import jmt.gui.common.resources.JMTImageLoader;
import jmt.gui.common.startScreen.GraphStartScreen;

/**
 * <p>Title: About Dialog Factory</p>
 * <p>Description: This class will create dialogs to show credits for each
 * application.</p>
 *
 * @author Bertoli Marco
 *         Date: 1-feb-2006
 *         Time: 16.42.10
 */
public class AboutDialogFactory {
	public enum Company {
		POLIMI, ICL;
		
		/**
		 * @return the logo of the contributor company
		 */
		public JComponent getLogo() {
			switch (this) {
				case POLIMI:
					JLabel logo = new JLabel(GraphStartScreen.HTML_POLI);
					logo.setHorizontalTextPosition(SwingConstants.TRAILING);
					logo.setVerticalTextPosition(SwingConstants.CENTER);
					logo.setIconTextGap(10);
					logo.setIcon(JMTImageLoader.loadImage("logo", new Dimension(70, 70)));
					return logo;
				case ICL:
					return new JLabel(JMTImageLoader.loadImage("logo_icl", new Dimension(-1,40)));
				default:
					return null;
			}
		}
	}
	
	/** JMVA main contributors */
	private static final Contributors[] JMVA = {
		new Contributors(Company.POLIMI, "Bertoli Marco", "Conti Andrea", "Dall'Orso Federico", "Omini Stefano", "Granata Federico"),
		new Contributors(Company.ICL, "Makaronidis Michalis", "Bradshaw John", "Chugh Abhimanyu", "Casale Giuliano")
	};

	/** JSIMwiz main contributors */
	private static final Contributors[] JSIM = {
		new Contributors(Company.POLIMI, "Bertoli Marco", "Granata Federico", "Omini Stefano", "Radaelli Francesco", "Dall'Orso Federico")
	};

	/** JSIMgraph main contributors */
	private static final Contributors[] JMODEL = {
		new Contributors(Company.POLIMI, "Bertoli Marco", "D'Aquino Francesco", "Granata Federico", "Omini Stefano", "Radaelli Francesco", "Das Ashanka", "Spicuglia Sebastiano")
	};

	/** JABA main contributors */
	private static final Contributors[] JABA = {
		new Contributors(Company.POLIMI, "Bertoli Marco", "Zanzottera Andrea", "Gimondi Carlo", "Spicuglia Sebastiano")
	};

	/** JMCH main contributors */
	private static final Contributors[] JMCH = {
		new Contributors(Company.POLIMI, "Canakoglu Arif", "Di Mauro Ernesto")
	};

	/** JWAT main contributors */
	private static final Contributors[] JWAT = {
		new Contributors(Company.POLIMI, "Brambilla Davide", "Fumagalli Claudio")
	};
	
	/** Overall contributors is merged by the others */
	private static final Contributors[] JMT;
	
	static {
		List<Contributors> allContributors = new ArrayList<AboutDialogFactory.Contributors>();
		allContributors.addAll(Arrays.asList(JMVA));
		allContributors.addAll(Arrays.asList(JSIM));
		allContributors.addAll(Arrays.asList(JMODEL));
		allContributors.addAll(Arrays.asList(JABA));
		allContributors.addAll(Arrays.asList(JMCH));
		allContributors.addAll(Arrays.asList(JWAT));
		Map<Company, Set<String>> m = new EnumMap<Company, Set<String>>(Company.class);
		for (Contributors c : allContributors) {
			Set<String> set = m.get(c.getCompany());
			if (set == null) {
				set = new HashSet<String>();
				m.put(c.getCompany(), set);
			}
			set.addAll(c.getNames());
		}
		JMT = new Contributors[m.size()];
		int idx = 0;
		for (Company c : m.keySet()) {
			Set<String> names = m.get(c);
			JMT[idx] = new Contributors(c, names.toArray(new String[names.size()]));
			idx++;
		}
	}

	/**
	 * Variables
	 */
	private static final long AUTOCLOSE_TIMEOUT = 2500;
	
	private static boolean autoJMVAshown = false;

	/**
	 * Creates a new modal JMTDialog with specified owner and with panel inside, displaying current text.
	 * @param owner owner of the dialog. If it's null or invalid, created dialog will not
	 * be modal
	 * @param title title of dialog to be created
	 * @param autoclose to automatically close the dialog after a timeout
	 * @return created dialog
	 */
	protected static JMTDialog createDialog(Window owner, AboutDialogPanel panel, boolean autoclose) {
		final JMTDialog dialog;
		if (owner == null) {
			dialog = new JMTDialog();
		} else if (owner instanceof Dialog) {
			dialog = new JMTDialog((Dialog) owner, true);
		} else if (owner instanceof Frame) {
			dialog = new JMTDialog((Frame) owner, true);
		} else {
			dialog = new JMTDialog();
		}
		dialog.setTitle(panel.getDialogTitle());
		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane().add(panel, BorderLayout.CENTER);

		// Adds exit button
		JButton exit = new JButton();
		exit.setText("Close");
		exit.addActionListener(new ActionListener() {

			/**
			 * Invoked when an action occurs.
			 */
			public void actionPerformed(ActionEvent e) {
				dialog.close();
			}
		});

		JPanel bottom = new JPanel();
		bottom.add(exit);
		dialog.getContentPane().add(bottom, BorderLayout.SOUTH);
		dialog.centerWindow(640, 600);
		
		// Handles autoclose
		if (autoclose) {
			ExecutorService es = Executors.newSingleThreadExecutor();
			es.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(AUTOCLOSE_TIMEOUT);
						dialog.close();
					} catch (InterruptedException ex) {
						// Nothing to do
					}
				}
			});
			es.shutdown();
		}
		
		return dialog;
	}



	/**
	 * Shows JMVA about window
	 * @param owner owner of this window (if null, window will not be modal)
	 * @param autoclose to close automatically the window after a timeout
	 */
	public static void showJMVA(Window owner, boolean autoclose) {
		if (autoclose) {
			// Shows the window automatically only the first time
			if (autoJMVAshown) {
				return;
			}
			autoJMVAshown = true;
		}
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JMVA", "JMVA", GraphStartScreen.IMG_JMVAICON);
		panel.setNames(JMVA);
		createDialog(owner, panel, autoclose).setVisible(true);
	}

	/**
	 * Shows JSIM about window
	 * @param owner owner of this window (if null, window will not be modal)
	 */
	public static void showJSIM(Window owner) {
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JSIMwiz", "JSIM<em>wiz</em>", GraphStartScreen.IMG_JSIMICON);
		panel.setNames(JSIM);
		createDialog(owner, panel, false).setVisible(true);
	}

	/**
	 * Shows JMODEL about window
	 * @param owner owner of this window (if null, window will not be modal)
	 */
	public static void showJMODEL(Window owner) {
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JSIMgraph", "JSIM<em>graph</em>", GraphStartScreen.IMG_JMODELICON);
		panel.setNames(JMODEL);
		createDialog(owner, panel, false).setVisible(true);
	}

	/**
	 * Shows JABA about window
	 * @param owner owner of this window (if null, window will not be modal)
	 */
	public static void showJABA(Window owner) {
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JABA", "JABA", GraphStartScreen.IMG_JABAICON);
		panel.setNames(JABA);
		createDialog(owner, panel, false).setVisible(true);
	}

	/**
	 * Shows JMCH about window
	 * @param owner owner of this window (if null, window will not be modal)
	 */
	public static void showJMCH(Window owner) {
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JMCH", "JMCH", GraphStartScreen.IMG_JMCHICON);
		panel.setNames(JMCH);
		createDialog(owner, panel, false).setVisible(true);
	}

	/**
	 * Shows JWAT about window
	 * @param owner owner of this window (if null, window will not be modal)
	 */
	public static void showJWAT(Window owner) {
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JWAT", "JWAT", GraphStartScreen.IMG_JWATICON);
		panel.setNames(JWAT);
		createDialog(owner, panel, false).setVisible(true);
	}

	/**
	 * Shows JMT about window
	 * @param owner owner of this window (if null, window will not be modal)
	 */
	public static void showJMT(Window owner) {
		AboutDialogPanel panel = new AboutDialogPanel();
		panel.setTitles("About JMT", "Java Modelling Tools", GraphStartScreen.IMG_SUITEICON);
		panel.setNames(JMT);
		createDialog(owner, panel, false).setVisible(true);
	}

	/**
	 * Defines the major contributors of a tool, grouped by company
	 */
	public static class Contributors {
		private Company company;
		private List<String> names;
		
		/**
		 * Builds a contributors list
		 * @param company the company
		 * @param names the name of the contributors for that company
		 */
		private Contributors(Company company, String... names) {
			this(company, Arrays.asList(names));
		}

		/**
		 * Builds a contributors list
		 * @param company the company
		 * @param names the name of the contributors for that company
		 */
		private Contributors(Company company, List<String> names) {
			this.company = company;
			Collections.sort(names);
			this.names = Collections.unmodifiableList(names);
		}

		/**
		 * @return the company of the contributors
		 */
		public Company getCompany() {
			return company;
		}

		/**
		 * @return the contributor names
		 */
		public List<String> getNames() {
			return names;
		}
		
		
	}
}
