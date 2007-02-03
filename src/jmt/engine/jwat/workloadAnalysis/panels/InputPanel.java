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
package jmt.engine.jwat.workloadAnalysis.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;

import jmt.engine.jwat.ProgressStatusListener;
import jmt.engine.jwat.workloadAnalysis.tables.JWatVariableInputTable;
import jmt.engine.jwat.workloadAnalysis.tables.JWatVariableInputTableModel;
import jmt.engine.jwat.workloadAnalysis.tables.listeners.RowDeleteListener;
import jmt.engine.jwat.workloadAnalysis.utils.FormatFileReader;
import jmt.engine.jwat.workloadAnalysis.utils.FormatFileWriter;
import jmt.engine.jwat.workloadAnalysis.utils.ModelWorkloadAnalysis;
import jmt.engine.jwat.workloadAnalysis.utils.SteppedComboBox;
import jmt.engine.jwat.workloadAnalysis.wizard.WorkloadAnalysisWizard;
import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.common.CommonConstants;
import jmt.gui.jwat.JWATConstants;
import jmt.gui.jwat.JWatWizard;
import jmt.gui.jwat.input.EventFinishAbort;
import jmt.gui.jwat.input.EventFinishLoad;
import jmt.gui.jwat.input.EventStatus;
import jmt.gui.jwat.input.Loader;
import jmt.gui.jwat.input.Parameter;
import jmt.gui.jwat.input.ProgressMonitorShow;

/**
 * Description: This class rapresents workload analysis tool input panel
 *
 * @author Brambilla Davide Matr 667986, Fumagalli Claudio 667971
 * Class created 30-lug-2006 10.54.14 Darksch
 */
public class InputPanel extends WizardPanel implements CommonConstants,JWATConstants{
	//Help strings
	private static String ADD_NEW_VARIABLE = "Add new variable";
	private static String SPINNER_VARIABLE = "Enter the number of variables";
	private static String COMBO_FILE_INPUT = "Select input file";
	private static String COMBO_FILTER_INPUT = "Select workload sampling method";
	private static String COMBO_FORMAT_INPUT = "Select format for input file";
	private static String PANEL_INFORMATION = "Name and observations of selected file";
	private static String INPUT_TABLE = "Click or drag to select classes; to edit data single-click and start typing. Right-click for a list of available operations";
	// JComboBox filter section texts
	private String FILTER_TEXT_COMPLETE = "Complete file";
	private String FILTER_TEXT_RANDOM = "Random Sampling";
	private String FILTER_TEXT_INTERVAL = "By interval";
	// Description of the panel
	private final static String INPUT_DESCRIPTION = HTML_START
			+ HTML_FONT_TITLE + "Inputs" + HTML_FONT_TIT_END + HTML_FONT_NORM
			+ "Define a new input format or open a saved format" + HTML_FONT_NOR_END
			+ HTML_END;
	// Text of label of log file input
	private final static String LOAD_FILE_LABEL = HTML_START + HTML_FONT_NORM
			+ "Load file" + HTML_FONT_NOR_END + HTML_END;
	// Text of label of format input file
	public final static String LOAD_FORMAT_LABEL = HTML_START + HTML_FONT_NORM
			+ "Load saved format " + HTML_FONT_NOR_END + HTML_END;
	// Text of label of filter on input file
	public final static String LOAD_FILTER_LABEL = HTML_START + HTML_FONT_NORM
			+ "Choose sampling method " + HTML_FONT_NOR_END + HTML_END;
	// Text button save new format
	public final static String BUTTON_SAVE_FORMAT = HTML_START + HTML_FONT_NORM
			+ "Save format" + HTML_FONT_NOR_END + HTML_END;
	//Text button save new format
	public final static String LOAD_LOG = HTML_START + HTML_FONT_NORM
			+ "LOAD" + HTML_FONT_NOR_END + HTML_END;
	// Text information about filtering on variable
	public final static String FILTER_NEXT_STEP = HTML_START + HTML_FONT_NORM
			+ "<CENTER>For filtering on variable see next step</CENTER>" + HTML_FONT_NOR_END + HTML_END;
	// String of browsing for a new file used in input file panel
	private static String browseInputPanelText = "Browse...";
	// Table used to define or show existing format of a log file ( variables information )
	private JWatVariableInputTable inputTable = null;
	// Reference to main startscreen
	private ModelWorkloadAnalysis model = null;
	// Input File panel
	private JPanel inputPanel = null;
	// File Format panel
	private JPanel formatPanel = null;
	// Main filtering panel
	private JPanel filterPanel = null;
	// Information panel
	private JPanel infoPanel = null;
	private JLabel observationNumb = null;
	private JLabel fileName = null;
	private static String obsNumbString = "Number of observations: ";
	private static String obsFileName = "File name: ";
	private int obsNumb = -1;
	// Combobox http log files retrived from default directory
	//private JComboBoxHorizScroll filechooser = new JComboBoxHorizScroll();
	private SteppedComboBox filechooser = new SteppedComboBox(new String[]{browseInputPanelText});
	// List of log files in save directory and opened by user
	//private ArrayList<File> nameFile = new ArrayList<File>();
	private File nameFile;
	// ComboBox file format retrived from default format directory
	private JComboBox formatchooser = new JComboBox();
	private JButton saveNewFormat = null;
	// ComboBox filtering possibilities
	private JComboBox filterchooser = new JComboBox();
	// Main filtering options panel
	private JPanel optionFilterPanel;
	// Random option panel and spinner element
	private JPanel randomOptionPanel = null;
	private JSpinner randomNObs = null;
	// Interval option panel and Spinner elements from / to observation
	private JPanel intervalOptionPanel = null;
	private JSpinner intervalFromObs = null;
	private JSpinner intervalToObs = null;
	// Panel used to organize filter option panel and others organizer panels
	private JPanel centerFilterOpt = new JPanel(new BorderLayout(0,5));

	private boolean canGoOn=false;

	private JButton loadFileBtn = null;
	private boolean loadOnRun = false;
	
	//Aggiunta 19/10/2006
	private JSpinner numOfVars = null;
	private JButton addVar = null;
	private HoverHelp help = null;
	private ChangeListener spinnerListener = new ChangeListener() {
		public void stateChanged(ChangeEvent ce) {
			/** Azzero format combo box **/
			if(((Integer)numOfVars.getValue()).intValue() == 0 || inputTable.getRowCount() == 0){
				formatchooser.setSelectedIndex(-1);
			}
			/** Rimozione variabile da spinner **/
			if(((Integer)numOfVars.getValue()).intValue() < inputTable.getRowCount() && inputTable.getRowCount() > 0){
				((JWatVariableInputTableModel) inputTable.getModel()).deleteRow(inputTable.getRowCount()-1);
				inputTable.tableChanged(new TableModelEvent(inputTable.getModel()));
				// Set formatchooser to none
				formatchooser.setSelectedIndex(-1);
				return;
			}
			/** Aggiunta variabile da spinner **/
			if(((Integer)numOfVars.getValue()).intValue() > inputTable.getRowCount()){
				addVariable();
			}
		}
	};

	protected AbstractAction loadFile = new AbstractAction(LOAD_LOG) {
		{ putValue(Action.SHORT_DESCRIPTION, "Load Data from file"); }
		// Adds a new row to inputTable and update varchooser comboBox used to show list of variables on which can be applied a filter
		public void actionPerformed(ActionEvent arg0) {
			if (!((JWatVariableInputTableModel) inputTable.getModel()).checkInfos()) {
				JOptionPane.showMessageDialog(InputPanel.this,"Some fileds of variable table are not correctly setted or are left blank");
				return;
			}
			if(nameFile == null){
				JOptionPane.showMessageDialog(InputPanel.this,"You have no selected any input file, please select one and then retry");
				return;
			}
			if(!loadOnRun){
				loadOnRun = true;
				//Recupero il table model di inut
				JWatVariableInputTableModel tableModel = ((JWatVariableInputTableModel) inputTable.getModel());
				Parameter fileParameter = tableModel.getParameter();
				//Controllo se necessario creare il filtering
				if(((String)filterchooser.getModel().getSelectedItem()).equals(FILTER_TEXT_COMPLETE)){
					try {
						fileParameter.setOption(new int[]{Loader.calcNumOfObs((nameFile.getAbsolutePath())),((SpinnerNumberModel)randomNObs.getModel()).getNumber().intValue()});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(((String)filterchooser.getModel().getSelectedItem()).equals(FILTER_TEXT_RANDOM)){
					// OPTION PER RANDOM
					fileParameter.setSampleMethod(Parameter.RANDOM_INPUT);
					try {
						fileParameter.setOption(new int[]{Loader.calcNumOfObs(nameFile.getAbsolutePath()),((SpinnerNumberModel)randomNObs.getModel()).getNumber().intValue()});
					} catch (Exception e) { e.printStackTrace(); }
				}
				if(((String)filterchooser.getModel().getSelectedItem()).equals(FILTER_TEXT_INTERVAL)){
					// OPTION PER INTERVAL
					fileParameter.setSampleMethod(Parameter.INTERVAL_INPUT);
					fileParameter.setOption(new int[]{((SpinnerNumberModel)intervalFromObs.getModel()).getNumber().intValue(),((SpinnerNumberModel)intervalToObs.getModel()).getNumber().intValue()});
				}
				//Chiamata a loader
				try {
					Loader.readData(nameFile.getAbsolutePath(),fileParameter,new ProgressMonitorShow(InputPanel.this,"Loading Data...",1000),new InputStatusListener());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	// Action applied to add new variable button used to add a variable definition to inputTable
	protected AbstractAction addNewVariable = new AbstractAction("New var") {
		{
			putValue(Action.SHORT_DESCRIPTION, ADD_NEW_VARIABLE);
		}
		// Adds a new row to inputTable and update varchooser comboBox used to show list of variables on which can be applied a filter
		public void actionPerformed(ActionEvent arg0) {
			addVariable();
		}
	};

	private void addVariable(){
		// Adds new row to table
		((JWatVariableInputTableModel) inputTable.getModel()).addNewRow();
		// Notify to inputTable changes
		inputTable.tableChanged(new TableModelEvent(inputTable.getModel()));
		// Update Spinner
		numOfVars.setValue(new Integer(inputTable.getRowCount()));
		// Refresh values in varchooser comboBox reset value to null and clean option panel
		centerFilterOpt.removeAll();
		// Set formatchooser to none
		formatchooser.setSelectedIndex(-1);
	}

	// Add save new format Action, this action saves new format in format directory
	protected AbstractAction saveFormat = new AbstractAction(BUTTON_SAVE_FORMAT) {
		{ putValue(Action.SHORT_DESCRIPTION, "Save current defined format"); }
		// Removes all rows from inputTable and update varchooser comboBox and option panel
		public void actionPerformed(ActionEvent arg0) {
			// Se esistono dei valori li salvo
			if(((JWatVariableInputTableModel) inputTable.getModel()).getSize() > 0){
				if(formatchooser.getSelectedIndex() >= 0){
					int response = JOptionPane.showConfirmDialog (null,
							"Overwrite existing file?","Confirm Overwrite",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if(response == JOptionPane.YES_OPTION){
						File file = new File(absolutePath + "jwatFormats/" + formatchooser.getSelectedItem().toString());
						JWatVariableInputTableModel mod = ((JWatVariableInputTableModel) inputTable.getModel());
						new FormatFileWriter(mod.getNames(),mod.getComments(),mod.getDelimiters(),mod.getRegExpr(),
								mod.getTypes(),file.getAbsolutePath(),mod.getDefaults(),mod.getReplaces());
						refreshComboFormatPanel();
						formatchooser.setSelectedItem(file.getName());
					}else{
						refreshComboFormatPanel();
					}
				}else{
					JFileChooser file = new JFileChooser(new File(absolutePath + "jwatFormats"));
					//file.setFileSystemView(new SingleDirFileSystem(new File(absolutePath + "jwatFormats")));
					file.setAcceptAllFileFilterUsed(false);
					file.setFileFilter(new FileFilter(){
						public boolean accept(File f) {
							if(f.getName().indexOf(".jwatformat") != -1){
								return true;
							}
							return false;
						}
						public String getDescription() {
							return "jWAT format";
						}
					});
					file.setApproveButtonText("Save");
					file.setFileSelectionMode(JFileChooser.FILES_ONLY);
					if(file.showOpenDialog(InputPanel.this) == JFileChooser.APPROVE_OPTION){
						File fFile = file.getSelectedFile ();
						if (fFile.exists ()) {
							int response = JOptionPane.showConfirmDialog (null,
									"Overwrite existing file?","Confirm Overwrite",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.QUESTION_MESSAGE);
							if (response == JOptionPane.NO_OPTION){
								return;
							}
						}
						JWatVariableInputTableModel mod = ((JWatVariableInputTableModel) inputTable.getModel());
						if(fFile.getName().indexOf(".jwatformat") == -1){
							fFile = new File(fFile.getAbsolutePath() + ".jwatformat");
						}
						new FormatFileWriter(mod.getNames(),mod.getComments(),mod.getDelimiters(),mod.getRegExpr(),
								mod.getTypes(),fFile.getAbsolutePath(),mod.getDefaults(),mod.getReplaces());
						refreshComboFormatPanel();
						formatchooser.setSelectedItem(file.getSelectedFile().getName());
					}else{
						JOptionPane.showMessageDialog(null,"No file choosen. Please try again",
								"Information",JOptionPane.INFORMATION_MESSAGE);
						refreshComboFormatPanel();
					}
				}
			}else{
				JOptionPane.showMessageDialog(null,"There are no variables defined. Please choose format and then clcik save button",
						"Information",JOptionPane.INFORMATION_MESSAGE);
				refreshComboFormatPanel();
			}
		}
	};

	private WorkloadAnalysisWizard parent;
	
	/**
	 * Creates and sets up a workload analysis input panel. This panels contains all necessary information to
	 * correctly load observation from a specified log file.
	 *
	 * @param parent reference to current Wizard frame
	 */
	public InputPanel(WorkloadAnalysisWizard parent) {
		this.parent = parent;
		model = parent.getModel();
		help = parent.getHelp();
		initGUI();
	}

	/**
	 * Initialize GUI
	 */
	private void initGUI(){
		Box mainHorizontalBox = Box.createHorizontalBox();
		mainHorizontalBox.add(Box.createHorizontalStrut(5));

		Box centralVerticalBox = Box.createVerticalBox();
		mainHorizontalBox.add(centralVerticalBox);

		centralVerticalBox.add(Box.createVerticalStrut(5));
		JPanel componentsPanel = new JPanel(new BorderLayout());
		centralVerticalBox.add(componentsPanel);

		JPanel centralUpperPanel = new JPanel(new BorderLayout());

		JPanel buttonNewVariable = new JPanel(new BorderLayout());

		JPanel spinnerP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		numOfVars = new JSpinner(new SpinnerNumberModel(0,0,50,1));
		numOfVars.setMaximumSize(new Dimension(600,25));
		numOfVars.setToolTipText(SPINNER_VARIABLE);
		help.addHelp(numOfVars,SPINNER_VARIABLE);
		spinnerP.add(new JLabel("Number:"));
		spinnerP.add(numOfVars);
		numOfVars.addChangeListener(spinnerListener);
		JPanel addP = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		addVar = new JButton(addNewVariable);
		help.addHelp(addVar,ADD_NEW_VARIABLE);
		addP.add(addVar);

		buttonNewVariable.add(spinnerP,BorderLayout.NORTH);
		buttonNewVariable.add(addP,BorderLayout.SOUTH);

		centralUpperPanel.add(buttonNewVariable,BorderLayout.EAST);
		centralUpperPanel.add(new JLabel(INPUT_DESCRIPTION), BorderLayout.CENTER);

		componentsPanel.add(centralUpperPanel, BorderLayout.NORTH);
		/********** TABLE **********/
		inputTable = new JWatVariableInputTable();
		inputTable.setToolTipText(INPUT_TABLE);
		help.addHelp(inputTable,INPUT_TABLE);
		inputTable.getTableHeader().setReorderingAllowed(false);
		inputTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		inputTable.setModel(new JWatVariableInputTableModel());
		inputTable.addDeleteRowListener(new RowDeleteListener(){
			public void rowsDeletedEvent() {
				numOfVars.setValue(new Integer(inputTable.getRowCount()));
				// Set formatchooser to none
				formatchooser.setSelectedIndex(-1);
			}
		});

		JScrollPane inputTabSP = new JScrollPane(inputTable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		inputTabSP.setPreferredSize(new Dimension(550, 400));

		componentsPanel.add(inputTabSP, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setMaximumSize(new Dimension(230,800));
		rightPanel.setPreferredSize(new Dimension(230,800));

		mainHorizontalBox.add(rightPanel);
		/********** INPUT FILE PANEL **********/
		inputPanel = new JPanel(new BorderLayout());
		inputPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Input file"));
		inputPanel.add(new JLabel(LOAD_FILE_LABEL), BorderLayout.NORTH);
		inputPanel.add(filechooser, BorderLayout.SOUTH);
		help.addHelp(inputPanel,"Select input file");

		/** UPDATE 09/11/06 **/
		JPanel infoFile = new JPanel(new BorderLayout());
		infoFile.add(inputPanel,BorderLayout.NORTH);
		
		infoPanel = new JPanel(new BorderLayout());
		help.addHelp(infoPanel,PANEL_INFORMATION);
		infoPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "File information"));

		observationNumb = new JLabel(obsNumbString);
		fileName = new JLabel(obsFileName);
		infoPanel.add(observationNumb, BorderLayout.NORTH);
		infoPanel.add(fileName, BorderLayout.SOUTH);
		
		infoFile.add(infoPanel,BorderLayout.SOUTH);
		
		//rightPanel.add(inputPanel, BorderLayout.NORTH);
		rightPanel.add(infoFile,BorderLayout.NORTH);

		JPanel centerInputPanel = new JPanel(new BorderLayout());
		rightPanel.add(centerInputPanel, BorderLayout.CENTER);
		/********** FORMAT FILE PANEL **********/
		formatPanel = new JPanel(new BorderLayout());
		help.addHelp(formatPanel,"Select a format for current input file");

		JPanel test = new JPanel(new GridLayout(1,2));
		formatPanel.setBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED), "File format"));
		test.add(new JLabel(LOAD_FORMAT_LABEL));
		saveNewFormat = new JButton(saveFormat);
		help.addHelp(saveNewFormat,"Save current defined format");
		test.add(saveNewFormat);

		formatPanel.add(test,BorderLayout.NORTH);
		formatPanel.add(formatchooser, BorderLayout.SOUTH);
		centerInputPanel.add(formatPanel, BorderLayout.NORTH);
		/********** SMAPLING PANEL **********/
		filterPanel = new JPanel(new BorderLayout());
		help.addHelp(filterPanel,"Specify the sampling method: Complete file, Random sampling or by interval");
		filterPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Workload sampling method"));

		JPanel chooseFilterPanel = new JPanel(new BorderLayout());
		filterPanel.add(chooseFilterPanel, BorderLayout.NORTH);
		chooseFilterPanel.add(new JLabel(LOAD_FILTER_LABEL), BorderLayout.NORTH);

		optionFilterPanel = new JPanel(new BorderLayout());
		optionFilterPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Loading size options"));

		chooseFilterPanel.add(filterchooser, BorderLayout.SOUTH);
		filterPanel.add(optionFilterPanel, BorderLayout.CENTER);
		centerInputPanel.add(filterPanel, BorderLayout.CENTER);
		/********** INFORMATION PANEL **********/
		JPanel southPanel = new JPanel(new BorderLayout());

		/*infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "File information"));
		southPanel.add(infoPanel,BorderLayout.NORTH);

		observationNumb = new JLabel(obsNumbString);
		fileName = new JLabel(obsFileName);
		infoPanel.add(observationNumb, BorderLayout.NORTH);
		infoPanel.add(fileName, BorderLayout.SOUTH);*/

		loadFileBtn = new JButton(loadFile);
		JPanel loadPanel = new JPanel();
		loadFileBtn.setPreferredSize(new Dimension((int)(BUTTONSIZE*4.5),(int)(BUTTONSIZE*0.8)));
		loadFileBtn.setBackground(Color.RED);
		loadPanel.add(loadFileBtn);

		southPanel.add(loadPanel,BorderLayout.SOUTH);
		centerInputPanel.add(southPanel,BorderLayout.SOUTH);
		/********** INIT COMBOBOXES ****************/
		initComboInputPanel();
		initComboFormatPanel();
		initComboFilterPanel();

		this.setLayout(new GridLayout(1, 1));
		this.add(mainHorizontalBox);
	}

	/**
	 * Returns the name of the pane
	 *
	 * @see java.awt.Component#getName()
	 */
	public String getName() {
		return "Input";
	}

	private JFileChooser file = new JFileChooser();

	/**
	 * Sets up combobox of file with format defined
	 */
	private void initComboInputPanel() {
//		File[] name;
//		File directory = new File(absolutePath + "FileSaves");
		file.setCurrentDirectory(new File("."));
		
		filechooser.setToolTipText(COMBO_FILE_INPUT);
		//filechooser.addItem(browseInputPanelText);
//		try {
//			if(directory.isDirectory()){
//				name = directory.listFiles();
//				for(int i = 0; i < name.length; i++){
//					filechooser.addItem(name[i].getCanonicalFile().getName());
//				}
//			}
//		}catch (IOException e) { e.printStackTrace(); }
		filechooser.setSelectedIndex(-1);

		filechooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String choice = (String) ((JComboBox) event.getSource()).getSelectedItem();
				if (choice != null && choice.equals(browseInputPanelText)) {
					//JFileChooser file = new JFileChooser();
					//file.setCurrentDirectory(new File("."));
					if(file.showOpenDialog(InputPanel.this) == JFileChooser.APPROVE_OPTION){
						nameFile = file.getSelectedFile();
						if(!nameFile.exists()){
							JOptionPane.showMessageDialog(InputPanel.this,"File does not exists","Error",JOptionPane.WARNING_MESSAGE);
							return;
						}
						try {
							obsNumb = Loader.calcNumOfObs((nameFile.getAbsolutePath()));
						} catch (Exception e) {
							e.printStackTrace();
						}
						boolean exist = false;
						//controllo se esiste gia' la voce selezionata
						for(int i = 1; i < filechooser.getModel().getSize();i++){
							if(((String)filechooser.getModel().getElementAt(i)).equals(file.getSelectedFile().getAbsolutePath())){
								exist = true;
								break;
							}
						}
						if(!exist){
							filechooser.insertItemAt(file.getSelectedFile().getAbsolutePath(),filechooser.getItemCount());
							filechooser.setSelectedIndex(filechooser.getItemCount()-1);
						}else{
							filechooser.setSelectedItem(file.getSelectedFile().getAbsolutePath());
						}
						//Update information
						observationNumb.setText(obsNumbString + obsNumb );
						fileName.setText(obsFileName + file.getSelectedFile().getName());
						fileName.setToolTipText(nameFile.getName());
					}else{
						filechooser.setSelectedIndex(-1);
					}
				}else{
					if(choice != null){
						if(!choice.equals("")){
							if(choice.indexOf("\\") == -1){
								nameFile = new File(absolutePath + "FileSaves\\" +choice);
								try {
									obsNumb = Loader.calcNumOfObs((nameFile.getAbsolutePath()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}else{
								nameFile = new File(choice);
								try {
									obsNumb = Loader.calcNumOfObs((nameFile.getAbsolutePath()));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							//Update information
							observationNumb.setText(obsNumbString + obsNumb );
							fileName.setText(obsFileName + nameFile.getName());
							fileName.setToolTipText(nameFile.getName());
						}else{
							filechooser.setSelectedIndex(-1);
							observationNumb.setText(obsNumbString);
							fileName.setText(obsFileName);
						}
					}else{
						observationNumb.setText(obsNumbString);
						fileName.setText(obsFileName);
					}
				}
			}
		});
	}

	/**
	 * Sets up combobox format defined
	 */
	private void initComboFormatPanel() {
		formatchooser.setToolTipText(COMBO_FORMAT_INPUT);
		refreshComboFormatPanel();
		addListenersComboFormatPanel();
	}

	private void refreshComboFormatPanel(){
		File[] name;
		File directory = new File(absolutePath + "jwatFormats");
		formatchooser.removeAllItems();
		//UPDATE 5/11/06: + rimozione e riabilitazione listener per combo per evitare update su reload combo
		ActionListener[] l = (ActionListener[]) formatchooser.getListeners(ActionListener.class);
		if(l.length != 0) formatchooser.removeActionListener(l[0]);
		try {
			if(directory.isDirectory()){
				name = directory.listFiles();
				for(int i = 0; i < name.length; i++){
					if(name[i].getName().indexOf(".jwatformat")!= -1){
						formatchooser.addItem(name[i].getCanonicalFile().getName());
					}
				}
			}
		}catch (IOException e) { e.printStackTrace(); }
		//UPDATE 5/11/06: + rimozione e riabilitazione listener per combo per evitare update su reload combo
		if(l.length !=0) formatchooser.addActionListener(l[0]);
		formatchooser.setSelectedIndex(-1);
	}
	private void addListenersComboFormatPanel(){
		formatchooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String choice = (String) ((JComboBox) event.getSource()).getSelectedItem();
				if(choice != null){
					((JWatVariableInputTableModel) inputTable.getModel()).clearTable();
					FormatFileReader form = new FormatFileReader(absolutePath + "jwatFormats\\" + choice);
					for(int i = 0;i < form.getNumVars();i++){
						String tipo = "";
						switch(form.getType()){
						case Parameter.NUMBER: tipo = "Numeric";
						break;
						case Parameter.STRING: tipo = "String";
						break;
						case Parameter.DATE: tipo = "Date";
						break;
						}
						((JWatVariableInputTableModel) inputTable.getModel()).
						addNewRow(form.getName(),tipo,form.getComment(),form.getDelimiters(),form.getRegExpr(),form.getDefaults(),form.getReplace());
						form.next();
					}
					inputTable.tableChanged(new TableModelEvent(inputTable.getModel()));
					numOfVars.setValue(new Integer(form.getNumVars()));
				}
			}
		});
	}

	/**
	 * Sets up combobox filter defined
	 */
	private void initComboFilterPanel() {
		filterchooser.addItem(FILTER_TEXT_COMPLETE);
		filterchooser.addItem(FILTER_TEXT_RANDOM);
		filterchooser.addItem(FILTER_TEXT_INTERVAL);
		filterchooser.setToolTipText(COMBO_FILTER_INPUT);
		
		createIntervalOptionPanel();
		createRandomOptionPanel();

		filterchooser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int index = ((JComboBox) event.getSource()).getSelectedIndex();
				switch (index) {
				case 0:
					optionFilterPanel.removeAll();
					optionFilterPanel.add(new JLabel(FILTER_NEXT_STEP),BorderLayout.SOUTH);
					optionFilterPanel.revalidate();
					optionFilterPanel.repaint();
					break;
				case 1:
					optionFilterPanel.removeAll();
					optionFilterPanel.add(createRandomOptionPanel(),BorderLayout.CENTER);
					optionFilterPanel.add(new JLabel(FILTER_NEXT_STEP),BorderLayout.SOUTH);
					optionFilterPanel.revalidate();
					optionFilterPanel.repaint();
					break;
				case 2:
					optionFilterPanel.removeAll();
					optionFilterPanel.add(createIntervalOptionPanel(),BorderLayout.CENTER);
					optionFilterPanel.add(new JLabel(FILTER_NEXT_STEP),BorderLayout.SOUTH);
					optionFilterPanel.revalidate();
					optionFilterPanel.repaint();
					break;
				}
			}
		});
		filterchooser.setSelectedIndex(0);
	}

	/**
	 * Creates or returns the random option panel
	 * @return random option panel
	 */
	private JPanel createRandomOptionPanel() {
		if (randomOptionPanel == null) {
			// Creazione del pannello alla prima chiamata
			randomOptionPanel = new JPanel();
			randomNObs = new JSpinner(new SpinnerNumberModel(0,0,0,1));
			randomNObs.setPreferredSize(new Dimension(70, 25));
			
			randomOptionPanel.add(new JLabel("Number of observations:"),
					BorderLayout.NORTH);
			randomOptionPanel.add(randomNObs, BorderLayout.SOUTH);
		}else{
			if(obsNumb == -1){
				randomNObs.setModel(new SpinnerNumberModel(0,0,0,1));
			}else{
				randomNObs.setModel(new SpinnerNumberModel(1,1,obsNumb,1));
			}
		}
		return randomOptionPanel;
	}

	/**
	 * Creates or returns the interval option panel
	 * @return interval option panel
	 */
	private JPanel createIntervalOptionPanel() {
		if (intervalOptionPanel == null) {
			intervalOptionPanel = new JPanel(new BorderLayout());

			JPanel fromPanel = new JPanel(new GridLayout(1,2));
			JLabel fromLabel = new JLabel("From observation #: ",JLabel.RIGHT);
			//fromLabel.setPreferredSize(new Dimension(70, 25));
			fromPanel.add(fromLabel);
			intervalFromObs = new JSpinner(new SpinnerNumberModel(0,0,0,1));
			intervalFromObs.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e) {
					if(((SpinnerNumberModel)intervalFromObs.getModel()).getNumber().longValue()
							>=
						((SpinnerNumberModel)intervalToObs.getModel()).getNumber().longValue())
							((SpinnerNumberModel)intervalToObs.getModel())
								.setValue(new Integer(((SpinnerNumberModel)intervalFromObs.getModel()).getNumber().intValue() + 1));
				}
			});
			intervalFromObs.setPreferredSize(new Dimension(70,25));
			JPanel fromPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
			fromPan.add(intervalFromObs);
			fromPanel.add(fromPan);

			JPanel toPanel = new JPanel(new GridLayout(1,2));
			JLabel toLabel = new JLabel("To observation #: ",JLabel.RIGHT);
			toLabel.setPreferredSize(new Dimension(70, 25));
			toPanel.add(toLabel);
			intervalToObs = new JSpinner(new SpinnerNumberModel(0,0,0,1));
			intervalToObs.setPreferredSize(new Dimension(70,25));
			JPanel toPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
			toPan.add(intervalToObs);
			toPanel.add(toPan);

			JPanel temp = new JPanel(new GridLayout(2,1,0,10));
			temp.add(fromPanel, BorderLayout.NORTH);
			temp.add(toPanel, BorderLayout.SOUTH);

			intervalOptionPanel.add(temp,BorderLayout.NORTH);
		}else{
			if(obsNumb == -1){
				intervalFromObs.setModel(new SpinnerNumberModel(0,0,0,1));
				intervalToObs.setModel(new SpinnerNumberModel(0,0,0,1));
			}else{
				intervalFromObs.setModel(new SpinnerNumberModel(1,1,obsNumb-1,1));
				intervalToObs.setModel(new SpinnerNumberModel(2,2,obsNumb,1));
			}
		}
		return intervalOptionPanel;
	}

	/********** MENUBAR, TOOLBAR AND ACTIONS ***********/

    /**
	 * Sets up workload analysis toolbar
	 */
/*	private void initToolBar(){
		JToolBar toolbar = model.getParent().getWorkloadToolBar();
        Action[] items = new Action[]{
                FILE_NEW, FILE_SAVE, null,
                HELP_SHOWHELP
            };
            String[] iconNames = new String[]{
                "New", "Save", "Help"
            };
            for(int i=0, j=0; i<items.length; i++, j++){
                // Tis is a particularcase as it's a toggle button
                if(items[i] != null){
                    JButton button = new JButton(items[i]);
                    button.setText("");
                    button.setRolloverEnabled(true);
                    button.setBorderPainted(false);
                    button.setFocusPainted(false);
                    button.setContentAreaFilled(false);
                    button.setPressedIcon(ImageLoader.loadImage(iconNames[j]+"P"));
                    button.setRolloverIcon(ImageLoader.loadImage(iconNames[j]+"RO"));
                    button.setBorder(new EmptyBorder(0,0,0,0));
                    button.setMaximumSize(getMinimumSize());
                    toolbar.add(button);
                }else{
                    toolbar.addSeparator();
                    j--;
                }
            }
	}*/

	/**
	 * Sets up workload analysis menubar
	 */
	/*private void initMenuBar(){
		JMenuBar menuBar = model.getParent().getWorkloadMenuBar();
        Action[][] menus = new Action[][]{
            //File menu
            {FILE_NEW, FILE_SAVE, null, FILE_EXIT},
            //Help Menu
            {HELP_SHOWHELP, null, HELP_CREDITS}
        };
        String[] menuNames = new String[]{"File","?"};
        char[] mnemChars = new char[]{'f','?'};
        for(int i=0; i<menus.length; i++){
            //initialize menu
            JMenu menu = new JMenu(menuNames[i]);
            menu.setMnemonic(mnemChars[i]);
            for(int j=0; j<menus[i].length;j++){
                if(menus[i][j]!=null){
                    menu.add(new JMenuItem(menus[i][j]));
                }else menu.addSeparator();
            }
            menuBar.add(menu);
        }
	}*/

	/********** WIZARD MANAGEMENT FUNCTIONS **********/

	// TODO controllare validita dei dati forniti nel pannello e creazione e passaggio informazioni al modello per il prossimo panello
	// Chiamata prima di passare al prossimo pannello
	public boolean canGoForward() {
		return canGoOn;
	}

	// TODO controllare con Fuma cosa fare
	// Chiamata quando dal pannello si torna indietro
	public boolean canGoBack() {
		if (JOptionPane.showConfirmDialog(this,"Are you sure want to go back to start screen ?",
				"Back operation", JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)
				== JOptionPane.NO_OPTION)
			return false;
		//model.getParent().removeWorkload();
		return true;
	}

	private class InputStatusListener implements ProgressStatusListener
	{

		public void statusEvent(EventStatus e) {

			switch(e.getType()){
				case EventStatus.ABORT_EVENT:	abortEvent((EventFinishAbort)e);
					break;
				case EventStatus.DONE_EVENT:	finishedEvent((EventFinishLoad)e);
					break;
			}
			
		}
		
		//Abort caricamento file input
		private void abortEvent(EventFinishAbort e) {
			JOptionPane.showMessageDialog(InputPanel.this,e.getMessage(),"LOADING ABORTED!!",JOptionPane.WARNING_MESSAGE);
			loadOnRun = false;
			canGoOn=false;
			((JWatWizard)getParentWizard()).setEnableButton("Next >",false);
			((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
			
		}
		
//		dati caricati
		private void finishedEvent(final EventFinishLoad e) {
			JButton[] optBtn=new JButton[3];
			JOptionPane pane;
			
			optBtn[0]=new JButton("Continue");
			optBtn[1]=new JButton("Show Log");
			optBtn[2]=new JButton("Cancel");
			
			pane = new JOptionPane("# observations processed: "+ e.valToRead()+"\n# correct observations "+ e.valReaded()+ "\nTo see errors press Show Log",JOptionPane.QUESTION_MESSAGE,JOptionPane.DEFAULT_OPTION,null,optBtn,null);
			final JDialog dialog= pane.createDialog(InputPanel.this.getParentWizard(), "Loading Complete");
			pane.selectInitialValue();
			
			optBtn[0].addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent ev) {
					
					dialog.dispose();
					try{
						model.setMatrix(e.getMatrix());
					}catch(OutOfMemoryError err){
						JOptionPane.showMessageDialog(InputPanel.this,"Out of Memory error. Try with more memory","Error",JOptionPane.ERROR_MESSAGE);
						loadOnRun = false;
						return;
					}
					((JWatWizard)getParentWizard()).setEnableButton("Next >",true);
					((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
					loadOnRun = false;
					canGoOn=true;
					((JWatWizard)getParentWizard()).showNextPanel();
					
				}
				
			});
			
			optBtn[1].addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent ev) {
					dialog.setVisible(false);
					final JDialog f=new JDialog();
					f.setModal(true);
					f.setTitle("Error log");
					f.setSize(new Dimension(400,400));
					f.setContentPane(new LogVisualizer(f));
					f.addWindowListener(new WindowAdapter(){
						public void windowClosing(WindowEvent e) {
							dialog.setVisible(true);
						}
						public void windowClosed(WindowEvent e) {
							dialog.setVisible(true);	
						}
					});
					
					f.setVisible(true);
				}
				
			});
			
			optBtn[2].addActionListener(new ActionListener(){
				
				public void actionPerformed(ActionEvent ev) {
					dialog.dispose();
					loadOnRun = false;
					canGoOn=false;
					System.gc();
					((JWatWizard)getParentWizard()).setEnableButton("Next >",false);
					((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
				}
				
			});
			
			dialog.show();
		}	
	}
	private static final String helpText = "<html>In this panel you can choose input data file and define or select a file format.<br><br>" +
	"To load a file:" +
	"<UL><LI> Select input file</LI>" +
	"<LI> Select or define a format for the input file</LI>" +
	"<LI> Select a sampling method (<I>if necessary</I>)</LI>" +
	"<LI> Press Load button</LI></UL>" +
    " To edit values, double-click on the desired cell" +
    " and start typing.<br> <b>For a list of the available operations right-click" +
    " on the table</b>.<br>" +
    " Pressing DELETE removes all selected classes from the system.</html>";

	public void help() {
		JOptionPane.showMessageDialog(this, helpText, "Help", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public void gotFocus(){
		// Chiedere a utente se cancellare tutto
		if(model.getMatrix() != null){
			if(JOptionPane.showConfirmDialog(this,"This operation resets all data. Continue ?",
					"WARNING",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
	    		model.resetModel();
	    		resetOnNew();
	    		((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
			}else{
				parent.setLastPanel();
			}
		}else{
			((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
		}
	}
	
	public void lostFocus(){
		parent.setLastPanel(0);
	}
	
	public void resetOnNew(){
		canGoOn = false;
		((JWatVariableInputTableModel)inputTable.getModel()).resetTable();
		inputTable.tableChanged(new TableModelEvent(inputTable.getModel()));
		filechooser.setSelectedIndex(-1);
		formatchooser.setSelectedIndex(-1);
		filterchooser.setSelectedIndex(0);
		numOfVars.setValue(new Integer(0));
		observationNumb.setText(obsNumbString);
		fileName.setText(obsFileName);
	}
}