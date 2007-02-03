package jmt.engine.jwat.workloadAnalysis.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmt.engine.jwat.ProgressStatusListener;
import jmt.engine.jwat.workloadAnalysis.utils.FormatFileReader;
import jmt.engine.jwat.workloadAnalysis.utils.ModelWorkloadAnalysis;
import jmt.engine.jwat.workloadAnalysis.wizard.WorkloadAnalysisWizard;
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

public class LoadDemoPanel extends WizardPanel implements CommonConstants,JWATConstants{
	private String demoDescription="<HTML>"+
	"<b>Type of demo:</b> Workload analysis demo<p>" +
	"<b>Description:</b> This demo has been extracted from an Apache log file selecting only some significative variables<p>"+
	"<b># observations:</b> This demo has 37614 observations <p>" +
	"<b># variables:</b> Each observation is characterized by four variables<p>" +
	"<b>Variables description:</b><p>" +
	"<TABLE align=center border=1>" +
	"<TR><TD><B>Name</B></TD><TD><B>Description</B></TD><TD><B>Type</B></TD></TR>" +
	"<TR><TD>IP</TD><TD>IP address</TD><TD>String</TD></TR>" +
	"<TR><TD>Timestamp</TD><TD>Timestamp of the visit as seen by the web server</TD><TD>Date</TD></TR>" +
	"<TR><TD>Access request</TD><TD>The request made</TD><TD>String</TD></TR>" +
	"<TR><TD>Bytes transferred</TD><TD>The number of bytes transferred</TD><TD>Numeric</TD></TR>" +
	"</TABLE></HTML>";
	private String useDescription="<HTML>"+
	"<b>How to perform a demo</b><p><p>" +
	"<b>STEP 1:</b> Select demo file from the list then click 'Load demo'.<p><p>" +
	"<b>STEP 2:</b> Look at statistics panels then click next.<p><p>" +
	"<b>STEP 3:</b> Select clustering algorithm choose the options (it is recommanded to select (value-min)/(max-min) transformation) of clustering then click 'solve'.<p><p>" +
	"<b>STEP 4:</b> Look at statistics results (using back button you can return to STEP 3 to apply different clustering).<p><p>" +
 	"</HTML>";

	private JButton loadDemo;
	private boolean canGoOn;
	private ModelWorkloadAnalysis model;
	private JList demos;
	private JLabel demoDesc;
	private JLabel useDesc;
	
	public LoadDemoPanel(WorkloadAnalysisWizard parent) {
		super();
		model = parent.getModel();
		canGoOn=false;
		initGUI();
	}

	public String getName() {
		return "Load Demo";
	}
	
	private void initGUI()
	{
		this.setLayout(new BorderLayout());
		
		JPanel grid = new JPanel(new GridLayout(2,1,5,5));
		
		JPanel upper = new JPanel(new BorderLayout(10,10));
		demos = new JList(new String[]{"Demo1"});
		Font f = demos.getFont();
		demos.setFont(new Font(f.getFontName(), f.getStyle(), f
				.getSize() + 2));
		demos.setPreferredSize(new Dimension(150,200));
		demos.addListSelectionListener(new ListSelectionListener(){

			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					if(demos.getSelectedIndex() >=0){
						//TODO inserire stringhe particolari per demo
					}else{
						demos.setSelectedIndex(0);
					}
				}
			}
			
		});
		demos.setSelectedIndex(0);
		demoDesc = new JLabel(demoDescription);
		upper.add(new JScrollPane(demos),BorderLayout.WEST);
		upper.add(new JScrollPane(demoDesc),BorderLayout.CENTER);
		demoDesc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Demo description"));
		
		useDesc = new JLabel(useDescription);
		useDesc.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "How to description"));
		
		grid.add(upper);
		grid.add(new JScrollPane(useDesc));
		
		JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		south.add(new JLabel("Click this button to load selected demo   --->   "));
		loadDemo=new JButton("Load demo");
		loadDemo.setBackground(Color.RED);
		loadDemo.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
					//Chiamata a loader
					try {
						Loader.readData(absolutePath + "examples\\Demo1Data.jwat",Loader.loadParameter("Demo1"),new ProgressMonitorShow(LoadDemoPanel.this,"Loading Data...",1000),new InputStatusListener());
					} catch (FileNotFoundException ee) {
						JOptionPane.showMessageDialog(LoadDemoPanel.this,"Loading aborted. File not found.","ABORT!!",JOptionPane.WARNING_MESSAGE);
					}
					catch (IOException ee) {
						JOptionPane.showMessageDialog(LoadDemoPanel.this,"Loading demo failed.","ABORT!!",JOptionPane.WARNING_MESSAGE);
					}
				}
		});
		south.add(loadDemo);
		this.add(grid,BorderLayout.CENTER);
		this.add(south,BorderLayout.SOUTH);
	}

	public boolean canGoForward() {
		return canGoOn;
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
			JOptionPane.showMessageDialog(LoadDemoPanel.this,e.getMessage(),"LOADING ABORTED!!",JOptionPane.WARNING_MESSAGE);
			canGoOn=false;
			((JWatWizard)getParentWizard()).setEnableButton("Next >",false);
			((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
			
		}
	
//		dati caricati
		private void finishedEvent(final EventFinishLoad e) {
			model.setMatrix(e.getMatrix());
			((JWatWizard)getParentWizard()).setEnableButton("Next >",true);
			((JWatWizard)getParentWizard()).setEnableButton("Solve",false);
			canGoOn=true;
			((JWatWizard)getParentWizard()).showNextPanel();
				
		}	
	}

}
