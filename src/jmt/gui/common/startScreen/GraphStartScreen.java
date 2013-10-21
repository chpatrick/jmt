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

package jmt.gui.common.startScreen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import jmt.common.GlobalSettings;
import jmt.framework.gui.components.JMTFrame;
import jmt.framework.gui.components.QuickHTMLViewer;
import jmt.framework.net.BrowserLauncher;
import jmt.gui.common.panels.AboutDialogFactory;
import jmt.gui.common.panels.AboutDialogFactory.Company;
import jmt.gui.common.resources.JMTImageLoader;
import jmt.gui.common.startScreen.sampleAnimation.SampleQNAnimation;
import jmt.gui.exact.ExactWizard;
import jmt.gui.jaba.JabaWizard;
import jmt.gui.jmodel.mainGui.MainWindow;
import jmt.gui.jsim.JSIMMain;
import jmt.gui.jwat.MainJwatWizard;
import jmt.jmarkov.MMQueues;
import jmt.manual.ManualBookmarkers;
import jmt.manual.PDFViewerBuffer;

/**
 * <p>Title: Graph StartScreen</p>
 * <p>Description: A new StartScreen that displays a graph with all possible choices to help
 * user chosing the right application</p>
 * 
 * @author Bertoli Marco
 *         Date: 18-ott-2005
 *         Time: 12.34.22
 */
public class GraphStartScreen extends JMTFrame {
	private static final long serialVersionUID = 1L;
	private static final String IMAGE = "StartScreen.png";
	private static final int BORDERSIZE = 20;
	private static final int BUTTONSIZE = 25;
	private static URL imageURL = GraphStartScreen.class.getResource(IMAGE);
	private static final int FONT_SIZE = 4;
	private static String[] args;
	private JButton onlineDoc, introEng, about;

	// Queue Animation
	private SampleQNAnimation sampleQNAni;

	// Images
	public static final String IMG_LOGOPOLI = "logo";
	public static final String IMG_LOGO_ICL = "logo_icl";
	public static final String IMG_JMODELICON = "JMODELIcon";
	public static final String IMG_JMVAICON = "JMVAIcon";
	public static final String IMG_JSIMICON = "JSIMIcon";
	public static final String	IMG_JABAICON = "JABAIcon";
	public static final String IMG_JMCHICON = "JMCHIcon";
	public static final String IMG_JWATICON = "JWATIcon";
	public static final String IMG_SUITEICON = "JMTIcon";
	//names for URLS of documents to be shown as description of main applications
	private static final String	URL_DOCUMENTATION_ONLINE = "http://jmt.sourceforge.net/Documentation.html";
	// Content for logo panel
	private static final String FONT_TYPE = "Arial";
	public static final String HTML_CONTENT_TITLE = 
			String.format("<html><body><b><font face='%s' size='+2'>JMT - Java Modelling Tools v.%s</font></b><br>" +
					"<font face='%s' size='-1'>Project Coordinator: prof. G.Serazzi</font>" +
					"</body></html>",
					FONT_TYPE, GlobalSettings.getSetting(GlobalSettings.VERSION), FONT_TYPE);
	public static final String HTML_CONTENT_TITLE_HREF = 
			String.format("<html><body><b><font face='%s' size='+2'>JMT - Java Modelling Tools v.%s</font></b><br>" +
					"<font face='%s' size='-1'>Project Coordinator: prof. G.Serazzi<br><br>" +
					"<b>Home Page:</b> <a href=\"http://jmt.sourceforge.net\">http://jmt.sourceforge.net</a></font>" +
					"</body></html>",
					FONT_TYPE, GlobalSettings.getSetting(GlobalSettings.VERSION), FONT_TYPE);
	public static final String HTML_POLI = 
			String.format("<html><body><font face='%s' size='%s'><b>DEIB<br>" +
					"Politecnico di Milano<br>Italy</b></font></body></html>", 
					FONT_TYPE, FONT_SIZE);

	/**
	 * Constructs a new GraphStartScreen
	 */
	public GraphStartScreen() {
		super(true);
		initGUI();
		addListeners();
	}

	// --- Actions associated with buttons -----------------------------------------------------------------
	private AbstractAction startJMVA = new AbstractAction("JMVA") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "MVA solver with wizard interface");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage(IMG_JMVAICON, new Dimension(BUTTONSIZE, BUTTONSIZE)));
		}

		public void actionPerformed(ActionEvent e) {
			new ExactWizard();
		}
	};

	private AbstractAction startJMCH = new AbstractAction("JMCH") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Markov Chain solver with graphical interface");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage(IMG_JMCHICON, new Dimension(BUTTONSIZE, BUTTONSIZE)));
		}

		public void actionPerformed(ActionEvent e) {
			MMQueues.main(args);
		}
	};

	private AbstractAction startJWAT = new AbstractAction("JWAT") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Workload Analyzer Tool");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage(IMG_JWATICON, new Dimension(BUTTONSIZE, BUTTONSIZE)));
		}

		public void actionPerformed(ActionEvent e) {
			//JWatStartScreen.main(args);
			MainJwatWizard.main(args);
		}
	};

	private AbstractAction startJSIM = new AbstractAction("<html>JSIM<em>wiz</em></html>") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Simulator solver with wizard interface");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage(IMG_JSIMICON, new Dimension(BUTTONSIZE, BUTTONSIZE)));
		}

		public void actionPerformed(ActionEvent e) {
			JSIMMain.main(args);
		}
	};

	private AbstractAction startJMODEL = new AbstractAction("<html>JSIM<em>graph</em></html>") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Simulator and MVA solver with graphical interface");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage(IMG_JMODELICON, new Dimension(BUTTONSIZE, BUTTONSIZE)));
		}

		public void actionPerformed(ActionEvent e) {
			MainWindow.main(args);
		}
	};

	private AbstractAction startJABA = new AbstractAction("JABA") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			putValue(Action.SHORT_DESCRIPTION, "Asyntotic bound analysis solver with wizard interface");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage(IMG_JABAICON, new Dimension(BUTTONSIZE, BUTTONSIZE)));
		}

		public void actionPerformed(ActionEvent e) {
			JabaWizard.main(args);
		}
	};
	// -----------------------------------------------------------------------------------------------------

	// --- Buttons to be created ---------------------------------------------------------------------------
	/**
	 * Data structure used to create buttons. To add a new button simply add its action here
	 */
	protected AbstractAction[] buttonActions = { startJSIM, startJMODEL, startJMVA, startJMCH, startJABA, startJWAT };

	// -----------------------------------------------------------------------------------------------------

	// --- Methods to paint GUI ----------------------------------------------------------------------------
	/**
	 * Creates all gui related stuff
	 */
	private void initGUI() {
		//set tooltip delay for whole project
		ToolTipManager.sharedInstance().setInitialDelay(300);
		ToolTipManager.sharedInstance().setDismissDelay(100000);
		// Sets default title, close operation and dimensions
		this.setTitle("JMT - Java Modelling Tools v." + GlobalSettings.getSetting(GlobalSettings.VERSION));
		this.setIconImage(JMTImageLoader.loadImage(IMG_SUITEICON).getImage());
		this.setResizable(false);
		this.centerWindow(790, 550);

		JPanel mainPanel = new JPanel(new BorderLayout());
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		JLabel title = new JLabel(HTML_CONTENT_TITLE);
		title.setBorder(BorderFactory.createEmptyBorder(8,8,0,0));
		this.getContentPane().add(title, BorderLayout.NORTH);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(BORDERSIZE, BORDERSIZE, BORDERSIZE, BORDERSIZE));
		// Adjusts image
		Image image = new ImageIcon(imageURL).getImage();
		image = image.getScaledInstance(450, 370, Image.SCALE_SMOOTH);
		JLabel imageLabel = new JLabel();
		imageLabel.setBorder(BorderFactory.createEmptyBorder(BUTTONSIZE - 5, 1, 0, 0));
		imageLabel.setIcon(new ImageIcon(image));
		imageLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		imageLabel.setVerticalAlignment(SwingConstants.NORTH);
		mainPanel.add(imageLabel, BorderLayout.CENTER);

		// Add buttons taking them from buttonActions[]
		JPanel buttonPanel = new JPanel(new GridLayout(buttonActions.length, 1, 2, 2));
		for (AbstractAction buttonAction : buttonActions) {
			buttonPanel.add(createButton(buttonAction));
		}

		mainPanel.add(buttonPanel, BorderLayout.EAST);

		// Now adds a panel with logo on the top of everything else. Uses glassPanel to perform this
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(60, BORDERSIZE, BORDERSIZE, BORDERSIZE));
		topPanel.setOpaque(false);
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.setOpaque(false);
		this.setGlassPane(topPanel);
		topPanel.add(leftPanel, BorderLayout.WEST);
		// Adds logo and title to leftPanel
		{
			
			JPanel logoPanel = new JPanel();
			BoxLayout layout = new BoxLayout(logoPanel, BoxLayout.Y_AXIS);
			logoPanel.setLayout(layout);
			logoPanel.add(Company.POLIMI.getLogo());
			JComponent icl = Company.ICL.getLogo();
			icl.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
			logoPanel.add(icl);
			leftPanel.add(logoPanel, BorderLayout.NORTH);
		}
		this.getGlassPane().setVisible(true);
		// Now Adds Sample Animation in the bottom
		sampleQNAni = new SampleQNAnimation();
		sampleQNAni.start();
		JPanel pivotPanel = new JPanel(new GridBagLayout());
		sampleQNAni.setPreferredSize(new Dimension(200, 120));
		sampleQNAni.setBackground(new Color(151, 151, 151));
		pivotPanel.add(sampleQNAni);
		leftPanel.add(pivotPanel, BorderLayout.SOUTH);
		// Adds intro buttons in the centre
		JPanel introButtonArea = new JPanel(new GridLayout(-1, 1));
		onlineDoc = new JButton("Online Documentation");
		onlineDoc.addMouseListener(rollover);
		introEng = new JButton("Introduction to JMT");
		introEng.addMouseListener(rollover);
		about = new JButton("Credits");
		about.addMouseListener(rollover);
		
		introButtonArea.add(introEng);
		introButtonArea.add(new JPanel());
		introButtonArea.add(onlineDoc);
		introButtonArea.add(new JPanel());
		introButtonArea.add(about);
		introButtonArea.setOpaque(false);
		pivotPanel = new JPanel(new GridBagLayout());
		pivotPanel.add(introButtonArea);
		pivotPanel.setOpaque(false);
		leftPanel.add(pivotPanel, BorderLayout.CENTER);

	}

	//assigns each component its own listener
	private void addListeners() {
		onlineDoc.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				BrowserLauncher.openURL(URL_DOCUMENTATION_ONLINE);
			}
		});
		introEng.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Runnable r = new Runnable() {
					public void run() {
						try {
							new PDFViewerBuffer("Introduction to JMT", ManualBookmarkers.INTRO);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				EventQueue.invokeLater(r);
			}
		});
		about.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AboutDialogFactory.showJMT(GraphStartScreen.this);	
			}
		});
	}

	/**
	 * Helper method used to create a button inside a JPanel
	 * @param action action associated to that button
	 * @return created component
	 */
	private JComponent createButton(AbstractAction action) {
		JPanel panel = new JPanel(); // Use gridbag as centers by default
		JButton button = new JButton(action);
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		button.setPreferredSize(new Dimension((int) (BUTTONSIZE * 3.5), (BUTTONSIZE * 2)));
		button.addMouseListener(rollover);
		panel.add(button);
		return panel;
	}

	/**
	 * Shows a description window
	 * @param url url of html file to be shown inside window
	 * @param title title of the window
	 */
	private void showDescrWin(URL url, String title) {
		if (url != null) {
			QuickHTMLViewer qhv = new QuickHTMLViewer(url, title);
			qhv.centerWindow(730, 480);
			qhv.setVisible(true);
			qhv.setIconImage(getIconImage());
		}
	}

	/* (non-Javadoc)
	 * @see jmt.framework.gui.components.JMTFrame#doClose()
	 */
	@Override
	protected void doClose() {
		if (sampleQNAni != null) {
			sampleQNAni.stop();
		}
	}

	/**
	 * This class is used to perform rollover on the buttons by changing background
	 */
	public class Rollover extends MouseAdapter {
		private Color normal;
		private Color rollover;

		public Rollover() {
			// Finds colors
			normal = new JButton().getBackground();
			rollover = new Color(181, 189, 214);
		}

		/**
		 * Invoked when the mouse enters a component.
		 */
		@Override
		public void mouseEntered(MouseEvent e) {
			((Component) e.getSource()).setBackground(rollover);
		}

		/**
		 * Invoked when the mouse exits a component.
		 */
		@Override
		public void mouseExited(MouseEvent e) {
			((Component) e.getSource()).setBackground(normal);
		}
	}

	private Rollover rollover = new Rollover();

	// -----------------------------------------------------------------------------------------------------

	/**
	 * Main method
	 * @param args not used
	 */
	public static void main(String args[]) {
		GraphStartScreen.args = args;
		GraphStartScreen gss = new GraphStartScreen();
		gss.setVisible(true);
	}
}
