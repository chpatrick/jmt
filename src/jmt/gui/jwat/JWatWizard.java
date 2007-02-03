package jmt.gui.jwat;

import java.awt.BorderLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import jmt.framework.gui.help.HoverHelp;
import jmt.framework.gui.wizard.Wizard;

public class JWatWizard extends Wizard {
	private HoverHelp help;
	private JLabel helpLabel;
	private JButton[] btnList;
	protected JToolBar toolBar;
	
	public JWatWizard(){
	}
	
    public HoverHelp getHelp() {
        return help;
    }	
	/**
     * @return the button panel
     */
    protected JComponent makeButtons() {
        help = new HoverHelp();
        helpLabel = help.getHelpLabel();

        helpLabel.setBorder(BorderFactory.createEtchedBorder());

        ACTION_FINISH.putValue(Action.NAME, "Solve");
        ACTION_CANCEL.putValue(Action.NAME, "Exit");

        JPanel buttons = new JPanel();
        btnList=new JButton[5];
        
        JButton button_finish = new JButton(ACTION_FINISH);
        help.addHelp(button_finish, "Validates choices and start selected clustering");
        JButton button_cancel = new JButton(ACTION_CANCEL);
        help.addHelp(button_cancel, "Exits the wizard discarding all changes");
        JButton button_next = new JButton(ACTION_NEXT);
        help.addHelp(button_next, "Moves on to the next step");
        JButton button_previous = new JButton(ACTION_PREV);
        help.addHelp(button_previous, "Goes back to the previous step");
        JButton button_help = new JButton(ACTION_HELP);
        help.addHelp(button_help, "Displays help for the current panel");
        buttons.add(button_previous);
        btnList[0]=button_previous;
        buttons.add(button_next);
        btnList[1]=button_next;
        buttons.add(button_finish);
        btnList[2]=button_finish;
        buttons.add(button_cancel);
        btnList[3]=button_cancel;
        buttons.add(button_help);
        btnList[4]=button_help;


        JPanel labelbox = new JPanel();
        labelbox.setLayout(new BorderLayout());
        labelbox.add(Box.createVerticalStrut(20), BorderLayout.WEST);
        labelbox.add(helpLabel, BorderLayout.CENTER);

        Box buttonBox = Box.createVerticalBox();
        buttonBox.add(buttons);
        buttonBox.add(labelbox);
        return buttonBox;
    }

	public void setEnableButton(String button,boolean enabl)
	{
		for(int i = 0; i < btnList.length; i++){
			try{
				if(btnList[i].getText().equals(button)){
					btnList[i].setEnabled(enabl);
					break;
				}
			}catch(ClassCastException e){
				System.err.println("DEBUG: Casting not allowed");
			}
		}
		if(toolBar != null){
			toolBar.getComponent(4).setEnabled(enabl);
		}
	}
	
	public void setActionButton(String button,AbstractAction a)
	{
		for(int i = 0; i < btnList.length; i++){
			try{
				if(btnList[i].getText().equals(button)){
					a.putValue(Action.NAME, button);
					btnList[i].setAction(a);
					break;
				}
			}catch(ClassCastException e){
				System.err.println("DEBUG: Casting not allowed");
			}
		}				
	}
	
	public void setActionTool(AbstractAction a){
		if(toolBar != null){
			((JButton)toolBar.getComponent(4)).setAction(a);
		}
	}
	
	public void showNextPanel()
	{
		this.showNext();
	}
}
