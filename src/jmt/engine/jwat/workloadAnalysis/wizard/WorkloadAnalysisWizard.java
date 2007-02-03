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
package jmt.engine.jwat.workloadAnalysis.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import jmt.engine.jwat.workloadAnalysis.panels.ClusterPanel;
import jmt.engine.jwat.workloadAnalysis.panels.ClusteringInfoPanel;
import jmt.engine.jwat.workloadAnalysis.panels.InputPanel;
import jmt.engine.jwat.workloadAnalysis.panels.LoadDemoPanel;
import jmt.engine.jwat.workloadAnalysis.panels.StatsPanel;
import jmt.engine.jwat.workloadAnalysis.utils.JWatWorkloadManager;
import jmt.engine.jwat.workloadAnalysis.utils.ModelWorkloadAnalysis;
import jmt.framework.gui.controller.Manager;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.panels.AboutDialogFactory;
import jmt.gui.common.resources.JMTImageLoader;
import jmt.gui.jwat.JWatWizard;

public class WorkloadAnalysisWizard extends JWatWizard {
	// List of panels create for Workload Analysis tool
	private ArrayList JWatPanels = new ArrayList();
	// Description strings for frame window
	private final String WORKLOAD_TITLE_FRAME = "jWAT - Workload Analysis";
	private String IMG_JWATICON = "WorkLoadIcon";
	//Workload analysis Jtoolbar and Jmenu
	private JToolBar workloadToolbar = null;
	private JMenuBar workloadMenubar = null;
	//private JPanel menus;
	private ModelWorkloadAnalysis model = null;
	private String mode;
	private HoverHelp help;
	private int lastPanel = 0;
	/*
	 * 
	 */
	protected AbstractAction EXIT_ACTION = new AbstractAction("Exit"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Exits Application");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
		}
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
	};
	/**
	 * Constructor, creates and sets up main JWat tools window
	 */
	public WorkloadAnalysisWizard(){
		super();
		mode="load";
		initGUI();
		addListeners();
	}
	/**
	 * 
	 * @param mode
	 */
	public WorkloadAnalysisWizard(String mode){
		super();
		help = getHelp();
		this.mode=mode;
		initGUI();
		addListeners();
	}
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.Plastic3DLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		Locale.setDefault(Locale.ENGLISH);
		new WorkloadAnalysisWizard(args[0]);
	}
	/**
	 * This method sets up all tabbedpane and other components necessary for
	 * workload analysis tool ( Old Jwat application).
	 */
	private void initGUI(){
		this.setIconImage(JMTImageLoader.loadImageAwt(IMG_JWATICON));
		this.setTitle(WORKLOAD_TITLE_FRAME);
		this.setSize(800, 600);
		//this.setResizable(false);
		JPanel menus = new JPanel(new BorderLayout());
		menus.add(getWorkloadMenuBar(), BorderLayout.NORTH);
		//menus.add(getWorkloadToolBar(), BorderLayout.SOUTH);
		menus.add(makeToolbar(), BorderLayout.SOUTH);
		getContentPane().add(menus, BorderLayout.NORTH);
		//Creates Workload analysis panels
		model = new ModelWorkloadAnalysis(this);
		WizardPanel p;
		if(mode.equals("load"))	p = new InputPanel(this);
		else p = new LoadDemoPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		p = new StatsPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		p = new ClusterPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		p = new ClusteringInfoPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		setEnableButton("Next >",false);
		setEnableButton("Solve",false);
		show();
	}
	/**
	 * 
	 * @return
	 */
	public ModelWorkloadAnalysis getModel(){
		return model;
	}
	/**
	 * Creates workload analysis menu
	 * @return menu
	 */
	private JMenuBar getWorkloadMenuBar() {
		if(workloadMenubar == null){
			workloadMenubar = new JMenuBar();
			JMenuItem[][] menuItems = {	{new JMenuItem(FILE_NEW),new JMenuItem(FILE_SAVE){{setEnabled(false);}},new JMenuItem(FILE_OPEN){{setEnabled(false);}},null, new JMenuItem(EXIT_ACTION)},
					{new JMenuItem(ACTION_SOLVE){{setEnabled(false);}}},
					{new JMenuItem(HELP_SHOWHELP){{setEnabled(false);}},null,new JMenuItem(HELP_CREDITS)}};
			String[] menuTitles = {"File", "Action", "Help"};
			char[] chars = {'F','A','e'};
			for(int i=0; i<menuItems.length; i++){
				JMenu menu = new JMenu(menuTitles[i]);
				menu.setMnemonic(chars[i]);
				for(int j=0; j<menuItems[i].length; j++){
					if(menuItems[i][j]==null)menu.addSeparator();
					else menu.add(menuItems[i][j]);
				}
				workloadMenubar.add(menu);
			}			
		}
		return workloadMenubar;
	}
	/*
	 * 
	 */
	private void addListeners(){
		this.addWindowListener(new WindowAdapter() {
			/**
			 * Invoked when a window has been closed.
			 */
			public void windowClosed(WindowEvent e) {
				JWatWorkloadManager.closeAll();
			}
		});
	}
	private AbstractAction HELP_SHOWHELP = new AbstractAction("Help"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Show Help");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Help"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
		}
		public void actionPerformed(ActionEvent e) {
			showHelp(e);
		}
	};
	private AbstractAction HELP_CREDITS = new AbstractAction("About JWAT"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Credits");
		}
		public void actionPerformed(ActionEvent e) {
			AboutDialogFactory.showJWAT(WorkloadAnalysisWizard.this);
		}
	};
	private void showHelp(ActionEvent event){
		JHelp helpViewer = null;
		try {
			// Get the classloader of this class.
			ClassLoader cl = this.getClass().getClassLoader();
			// Use the findHelpSet method of HelpSet to create a URL referencing the helpset file.
			URL url = HelpSet.findHelpSet(cl, "help/Jwat_eng/JWatWorkload.hs");
			// Create a new JHelp object with a new HelpSet.
			helpViewer = new JHelp(new HelpSet(cl, url));
			
			// Set the initial entry point in the table of contents.
			//helpViewer.setCurrentID("");
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Sorry, jWAT help is not available",
					"Help not found", JOptionPane.ERROR_MESSAGE);
			return;
		}	
		// Create a new frame.
		JFrame frame = new JFrame();
		// Set it's size.
		frame.setSize(650,510);
		// Add the created helpViewer to it.
		frame.getContentPane().add(helpViewer);
		// Set a default close operation.
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		// Make the frame visible.
		frame.setVisible(true);
		
	}
	private AbstractAction FILE_NEW = new AbstractAction("New"){
		{
			putValue(Action.SHORT_DESCRIPTION, "New input file");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("New"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		}
		public void actionPerformed(ActionEvent e) {
			if(JOptionPane.showConfirmDialog(WorkloadAnalysisWizard.this,"This operation will reset data. Continue?","Warning",JOptionPane.YES_NO_OPTION) 
					== JOptionPane.YES_OPTION){
				//Reset model and set first panel
				model.resetModel();
				tabbedPane.setSelectedIndex(0);
				((InputPanel)tabbedPane.getComponentAt(0)).resetOnNew();
			}
		}
	};
	private AbstractAction FILE_SAVE = new AbstractAction("Save"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Save session");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Save"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}
		public void actionPerformed(ActionEvent e) {
		}
	};
	private AbstractAction FILE_OPEN = new AbstractAction("Open...") {
		{
			putValue(Action.SHORT_DESCRIPTION, "Open session");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Open"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}
		public void actionPerformed(ActionEvent e) {
		}
	};
	private AbstractAction ACTION_SOLVE = new AbstractAction("Clusterize") {
		{
			putValue(Action.SHORT_DESCRIPTION, "Clusterize");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Sim"));
			putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
		}
		public void actionPerformed(ActionEvent e) {
		}
	};
	/**
	 * @return the toolbar for the exact wizard. Shamelessly uses icon from the main jmt frame
	 */
	protected JToolBar makeToolbar() {
		workloadToolbar = new JToolBar();
		workloadToolbar.setRollover(true);
		workloadToolbar.setOrientation(SwingConstants.HORIZONTAL);
		workloadToolbar.setFloatable(false);
		//null values add a gap between toolbar icons
		Action[] actions = {FILE_NEW,FILE_OPEN, FILE_SAVE,null,ACTION_SOLVE,null,HELP_SHOWHELP};
		String[] icons = {"New","Open", "Save","Sim","Help"};
		String[] htext = {"Select new input file","Opens a saved session", "Saves the current session",
				"Clusterize", "Show help"};
		JButton button;
		workloadToolbar.setBorderPainted(true);
		//i index scans actions' array which includes null values, while j scans other arrays.
		//so j must be decremented when a null value is found in action array.
		for (int i = 0, j=0; i < actions.length; i++,j++) {
			if(actions[i]==null){
				j--;
				workloadToolbar.addSeparator(new Dimension(20,2));
			}else{
				button = new JButton(actions[i]);
				button.setText("");
				button.setIcon(JMTImageLoader.loadImage(icons[j]));
				button.setRolloverIcon(JMTImageLoader.loadImage(icons[j] + "RO"));
				button.setPressedIcon(JMTImageLoader.loadImage(icons[j] + "P"));
				button.setFocusPainted(false);
				button.setContentAreaFilled(false);
				button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				workloadToolbar.add(button);
				help.addHelp(button, htext[j]);
				if(j == 1 || j == 2 || j == 3 || j == 4) button.setEnabled(false);
			}
		}
		toolBar = workloadToolbar;
		return workloadToolbar;
	}
	// Riseleziona il pannello da cui si e' arrivati
	public void setLastPanel(){
		tabbedPane.setSelectedIndex(lastPanel);
	}
	// Salva il pannello che da cui si e' arrivati
	public void setLastPanel(int panel){
		lastPanel = panel;
	}
}

