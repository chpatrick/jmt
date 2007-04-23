package jmt.gui.jwat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
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
import javax.swing.filechooser.FileFilter;

import jmt.engine.jwat.JwatSession;
import jmt.engine.jwat.MatrixOsservazioni;
import jmt.engine.jwat.input.Loader;
import jmt.engine.jwat.trafficAnalysis.ModelTrafficAnalysis;
import jmt.engine.jwat.workloadAnalysis.utils.ModelWorkloadAnalysis;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.panels.AboutDialogFactory;
import jmt.gui.common.resources.JMTImageLoader;
import jmt.gui.jwat.trafficAnalysis.panels.GraphArrivalPanel;
import jmt.gui.jwat.trafficAnalysis.panels.GraphPanel;
import jmt.gui.jwat.trafficAnalysis.panels.TextualPanel;
import jmt.gui.jwat.workloadAnalysis.panels.ClusterPanel;
import jmt.gui.jwat.workloadAnalysis.panels.ClusteringInfoPanel;
import jmt.gui.jwat.workloadAnalysis.panels.InputPanel;
import jmt.gui.jwat.workloadAnalysis.panels.LoadDemoPanel;
import jmt.gui.jwat.workloadAnalysis.panels.StatsPanel;

public class MainJwatWizard extends JWatWizard {
	//jWAT tool icons
	private String IMG_JWATICON = "JWATIcon";
	//private JToolBar toolBar = null;
	private JPanel menus = null;
	private JMenuBar mMenuBar = null;
	
	private JWatModel model = null;
	
	//Last panel visited, used to control correct next step
	private int lastPanel = 0;
	
	private HoverHelp help = null;
	// List of panels create for Workload Analysis tool
	private ArrayList JWatPanels = new ArrayList();
	// First panel
	private JWatMainPanel mainPanel = null;

	/**
	 * Constructor.
	 */
	public MainJwatWizard() {
		initGUI();
	}
	
	private JFileChooser fileSaveF = new JFileChooser("."){
		{
			setApproveButtonText("Save");
			setFileSelectionMode(JFileChooser.FILES_ONLY);	
		}
	};
	
	/*
	 * Initializes jWAT start screen GUI
	 */
	private void initGUI(){
		this.setIconImage(JMTImageLoader.loadImage(IMG_JWATICON).getImage());
		this.setResizable(false);
		this.setTitle("jWAT");
		this.setSize(800,600);
		centerWindow();
		menus = new JPanel(new BorderLayout());
		help = this.getHelp();
		getContentPane().add(menus,BorderLayout.NORTH);	
		//Aggiunta del pannello principale dell'applicazione
		mainPanel = new JWatMainPanel(this);
		this.addPanel(mainPanel);
	}
	
	// Set correct enviornement for traffic analysis
	public void setTrafficEnv(){
		//Initializes correct model
		model = new ModelTrafficAnalysis();
		//Creates and adds all necessary panels to jWAT main screen
		WizardPanel p = new jmt.gui.jwat.trafficAnalysis.panels.InputPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		p = new TextualPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		p = new GraphPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		p = new GraphArrivalPanel(this);
		JWatPanels.add(p);
		this.addPanel(p);
		//Sets menu and tool bars
		getTrafficToolbar();
		getTrafficMenubar();
		//Disables Saolve button 
        this.setEnableButton("Solve", false);
        //Shows next panel, the first of traffic analysis wizard
        showNextPanel();
	}
	
	//Adds all necessary panes concernig with Workload analysis
	public void setWorkloadEnv(String mode){
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
		//Set Workload ToolBar
		getWorkloadToolbar();
		//Set Workload MenuBar
		getWorkloadMenuBar();
		setEnableButton("Next >",false);
		setEnableButton("Solve",false);
		lastPanel = 1;
		showNextPanel();
	}
	
    /**
     * Main method.
     * @param args no args.
     */
    public static void main(String[] args) {
		try {
            UIManager.setLookAndFeel(new com.jgoodies.looks.plastic.Plastic3DLookAndFeel());
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
        Locale.setDefault(Locale.ENGLISH);
		new MainJwatWizard().setVisible(true);
	}
    
    public void setToolBar(JToolBar bar){
    	if (toolBar != null) menus.remove(toolBar);
		menus.add(bar, BorderLayout.SOUTH);
    	toolBar = bar;
    }
    public void setMenuBar(JMenuBar bar){
    	if (mMenuBar != null) menus.remove(mMenuBar);
		menus.add(bar, BorderLayout.NORTH);
    	mMenuBar = bar;
    }    
	// Riseleziona il pannello da cui si e' arrivati
	public void setLastPanel(){
		tabbedPane.setSelectedIndex(lastPanel);
	}
	// Salva il pannello che da cui si e' arrivati
	public void setLastPanel(int panel){
		lastPanel = panel;
	}
	public JWatModel getModel(){
		return model;
	}
	/*
	 * 
	 */
	protected AbstractAction WL_EXIT_ACTION = new AbstractAction("Exit"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Exits Application");
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
		}
		public void actionPerformed(ActionEvent e) {
			cancel();
		}
	};
	private AbstractAction WL_HELP_SHOWHELP = new AbstractAction("Help"){
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
	private AbstractAction WL_HELP_CREDITS = new AbstractAction("About JWAT"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Credits");
		}
		public void actionPerformed(ActionEvent e) {
			AboutDialogFactory.showJWAT(MainJwatWizard.this);
		}
	};
	private void showHelp(ActionEvent event){
		JHelp helpViewer = null;
		try {
			// Get the classloader of this class.
			ClassLoader cl = this.getClass().getClassLoader();
			// Use the findHelpSet method of HelpSet to create a URL referencing the helpset file.
			URL url = HelpSet.findHelpSet(cl, "./help/JWat_eng/JWatWorkload.hs");
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
	private AbstractAction WL_FILE_NEW = new AbstractAction("New"){
		{
			putValue(Action.SHORT_DESCRIPTION, "New input file");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("New"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		}
		public void actionPerformed(ActionEvent e) {
			if(JOptionPane.showConfirmDialog(MainJwatWizard.this,"This operation will reset data. Continue?","Warning",JOptionPane.YES_NO_OPTION) 
					== JOptionPane.YES_OPTION){
				//Reset model and set first panel
				model.resetModel();
				tabbedPane.setSelectedIndex(1);
				try{
					((InputPanel)tabbedPane.getComponentAt(1)).resetOnNew();
				}catch(ClassCastException cce){
					return;
				}
			}
		}
	};
	private AbstractAction WL_FILE_SAVE = new AbstractAction("Save"){
		{
			putValue(Action.SHORT_DESCRIPTION, "Save session");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Save"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		}
		public void actionPerformed(ActionEvent e) {
			JwatSession session;
			if(fileSaveF.showOpenDialog(MainJwatWizard.this) == JFileChooser.APPROVE_OPTION){
				File fFile = fileSaveF.getSelectedFile ();
				String fileName=fFile.getAbsolutePath();
				System.out.println(fileName);
				session=new JwatSession(fileName.substring(0,fileName.lastIndexOf("\\"))+"\\",fileName.substring(fileName.lastIndexOf("\\")+1));
				session.saveSession(model.getMatrix());
			}
		}
	};
	private AbstractAction WL_FILE_OPEN = new AbstractAction("Open...") {
		{
			putValue(Action.SHORT_DESCRIPTION, "Open session");
			putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Open"));
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
			putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		}
		public void actionPerformed(ActionEvent e) {
			if(fileSaveF.showOpenDialog(MainJwatWizard.this) == JFileChooser.APPROVE_OPTION){
				File fFile = fileSaveF.getSelectedFile ();
				String fileName=fFile.getAbsolutePath();
				MatrixOsservazioni m= Loader.loadSession(fileName.substring(0,fileName.lastIndexOf("\\"))+"\\",fileName.substring(fileName.lastIndexOf("\\")+1));
				
				try{
					model.setMatrix(m);
				}catch(OutOfMemoryError err){
					JOptionPane.showMessageDialog(MainJwatWizard.this,"Out of Memory error. Try with more memory","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				System.out.println("Setting new panel");
				((InputPanel)tabbedPane.getComponent(1)).setCanGoForward(true);
				tabbedPane.setSelectedIndex(2);
			}

			
		}
	};
	private AbstractAction WL_ACTION_SOLVE = new AbstractAction("Clusterize") {
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
	protected void getWorkloadToolbar() {
		JToolBar workloadToolbar = new JToolBar();
		workloadToolbar.setRollover(true);
		workloadToolbar.setOrientation(SwingConstants.HORIZONTAL);
		workloadToolbar.setFloatable(false);
		//null values add a gap between toolbar icons
		Action[] actions = {WL_FILE_NEW,WL_FILE_OPEN, WL_FILE_SAVE,null,WL_ACTION_SOLVE,null,WL_HELP_SHOWHELP};
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
				if(j == 1 || j == 2 || j == 3 /*|| j == 4*/) button.setEnabled(false);
			}
		}
		setToolBar(workloadToolbar);
	}
	/**
	 * Creates workload analysis menu
	 * @return menu
	 */
	private void getWorkloadMenuBar() {

		JMenuBar workloadMenubar = new JMenuBar();
		JMenuItem[][] menuItems = {	{new JMenuItem(WL_FILE_NEW),new JMenuItem(WL_FILE_SAVE),new JMenuItem(WL_FILE_OPEN),null, new JMenuItem(WL_EXIT_ACTION)},
				{new JMenuItem(WL_ACTION_SOLVE){{setEnabled(false);}}},
				{new JMenuItem(WL_HELP_SHOWHELP){{setEnabled(true);}},null,new JMenuItem(WL_HELP_CREDITS)}};
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
		setMenuBar(workloadMenubar);
	}
	public void resetScreen(){
		for(int i = 0; i < JWatPanels.size(); i++) tabbedPane.remove((Component)JWatPanels.get(i));
		JWatPanels.clear();
		mainPanel.makeMenubar();
		mainPanel.makeToolbar();
		this.validate();
	}
	
    /**
     * @return the toolbar for the jaba wizard. Shamelessly uses icon from the main jmt frame
     */
    protected void getTrafficToolbar() {

        JToolBar tb = new JToolBar();
        tb.setRollover(true);
        tb.setOrientation(SwingConstants.HORIZONTAL);
        tb.setFloatable(false);

         Action[] actions = {TR_FILE_NEW,null, TR_HELP};
         //Action[] actions = {FILE_NEW,null, ACTION_FINISH,null, HELP};
         //String[] icons = {"New","Sim", "Help"};
         //String[] htext = {"Creates a new model","Solves the current model", "Show help"};
         String[] icons = {"New","Help"};
         String[] htext = {"Creates a new model", "Show help"};

        JButton button;
        tb.setBorderPainted(true);

        for (int i = 0, j=0; i < actions.length; i++,j++) {
            if(actions[i]==null){
                j--;
                tb.addSeparator(new Dimension(20,2));
            }else{
                button = new JButton(actions[i]);
                button.setText("");
                button.setIcon(JMTImageLoader.loadImage(icons[j]));
                button.setRolloverIcon(JMTImageLoader.loadImage(icons[j] + "RO"));
                button.setPressedIcon(JMTImageLoader.loadImage(icons[j] + "P"));
                button.setFocusPainted(false);
                button.setContentAreaFilled(false);
                button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                tb.add(button);
                help.addHelp(button, htext[j]);
            }
        }
        setToolBar(tb);
    }

    private void getTrafficMenubar(){

        JMenuBar jmb = new JMenuBar();

        /*JMenuItem[][] menuItems = {{new JMenuItem(FILE_NEW),null, new JMenuItem(FILE_EXIT)},
                                   {new JMenuItem(ACTION_SOLVE),
                                    null, new JMenuItem(ACTION_NEXT), new JMenuItem(ACTION_PREV)},
                                   {new JMenuItem(HELP), null, new JMenuItem(ABOUT)} };*/
        JMenuItem[][] menuItems = {{new JMenuItem(TR_FILE_NEW),null, new JMenuItem(TR_FILE_EXIT)},
                {new JMenuItem(ACTION_NEXT), new JMenuItem(ACTION_PREV)},
                {new JMenuItem(TR_HELP), null, new JMenuItem(TR_ABOUT)} };

        String[] menuTitles = {"File", "Action", "Help"};
        char[] chars = {'F','A','e'};
        for(int i=0; i<menuItems.length; i++){
            JMenu menu = new JMenu(menuTitles[i]);
            menu.setMnemonic(chars[i]);
            for(int j=0; j<menuItems[i].length; j++){
                if(menuItems[i][j]==null)menu.addSeparator();
                else menu.add(menuItems[i][j]);
            }
            jmb.add(menu);
        }
        setMenuBar(jmb);
    }
    private AbstractAction TR_FILE_NEW = new AbstractAction("New...") {
        {
            putValue(Action.SHORT_DESCRIPTION, "Create New Model");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("New"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
        }
        public void actionPerformed(ActionEvent e) {
            trafficNewModel();
        }
    };

    private AbstractAction TR_FILE_EXIT = new AbstractAction("Exit") {
        {
            putValue(Action.SHORT_DESCRIPTION, "Exits Application");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Q));
        }
        public void actionPerformed(ActionEvent e) {
            cancel();
        }
    };

    private AbstractAction TR_HELP = new AbstractAction("Burstiness help") {
        {
            putValue(Action.SHORT_DESCRIPTION, "Show Burstiness help");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Help"));
            putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_H,
                            ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
        }
        public void actionPerformed(ActionEvent e) {
            showHelp(e);
        }
    };

    private AbstractAction TR_ABOUT = new AbstractAction("About Burstiness...") {
        {
            putValue(Action.SHORT_DESCRIPTION, "About Burstiness");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("helpIcon"));
            putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_H,
            ActionEvent.ALT_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_H));
        }
        public void actionPerformed(ActionEvent e) {
            trafficShowAbout();
        }
    };
    private AbstractAction TR_ACTION_SOLVE = new AbstractAction("Solve") {
        {
            putValue(Action.SHORT_DESCRIPTION, "Solve model");
            putValue(Action.SMALL_ICON, JMTImageLoader.loadImage("Sim"));
            putValue(Action.ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.CTRL_MASK));
            putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }
        public void actionPerformed(ActionEvent e) {
            if(checkFinish())
                finish();
        }
    };
    private void trafficNewModel(){
    	if(JOptionPane.showConfirmDialog(MainJwatWizard.this,"This operation will reset data. Continue?","Warning",JOptionPane.YES_NO_OPTION) 
				== JOptionPane.YES_OPTION){
			//Reset model and set first panel
			model.resetModel();
			tabbedPane.setSelectedIndex(1);
			try{
				((jmt.gui.jwat.trafficAnalysis.panels.InputPanel)tabbedPane.getComponentAt(1)).resetOnNew();
			}catch(ClassCastException cce){
				return;
			}
		}
    }

    private void trafficShowAbout(){
   	 JOptionPane.showMessageDialog(this, "Sorry, is not available",
                "About Burstiness not found", JOptionPane.ERROR_MESSAGE);
   }
}

