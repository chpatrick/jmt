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
  
package jmt.gui.common.editors;

import jmt.framework.gui.components.JMTDialog;
import jmt.framework.gui.image.ImagePanel;
import jmt.framework.gui.layouts.SpringUtilities;
import jmt.framework.gui.listeners.KeyFocusAdapter;
import jmt.gui.common.distributions.Distribution;
import jmt.gui.common.distributions.Exponential;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * <p>Title: Distributions' Editor</p>
 * <p>Description: A modal dialog used to choose a specific distribution for a class or station service
 * and to enter its parameters. Users will enter owner Frame or Dialog and initial Distribution
 * (can be null) and will collect chosen distribution with <code>getResult()</code> method.</p>
 * 
 * @author Bertoli Marco
 *         Date: 29-giu-2005
 *         Time: 11.31.07
 */
public class DistributionsEditor extends JMTDialog {
    // Internal data structure
    protected Distribution initial, current, target;

    /**
     * This variable will be initialized only once.
     * It will contains every distribution that can be inserted
     */
    protected static HashMap distributions;

    // Constants
    protected static final int BORDERSIZE = 20;

    // Components
    protected JComboBox choser = new JComboBox();
    protected ImagePanel iconpanel = new ImagePanel();
    protected JPanel param_panel = new JPanel(new SpringLayout());
    protected JPanel mean_c_panel = new JPanel(new SpringLayout());

// --- Static methods ------------------------------------------------------------------------------
    /**
     * Returns a new instance of DistributionsEditor, given parent container (used to find
     * top level Dialog or Frame to create this dialog as modal)
     * @param parent any type of container contained in a Frame or Dialog
     * @param initial initial distribution to be set
     * @return new instance of DistributionsEditor
     */
    public static DistributionsEditor getInstance(Container parent, Distribution initial) {
        // Finds top level Dialog or Frame to invoke correct costructor
        while (!(parent instanceof Frame || parent instanceof Dialog))
            parent = parent.getParent();

        if (parent instanceof Frame)
            return new DistributionsEditor((Frame)parent, initial);
        else
            return new DistributionsEditor((Dialog)parent, initial);
    }

    /**
     * Uses reflection to return an HashMap of distributions. Search's key is distribution name and
     * value is the Class of found distribution
     * @return found distributions
     */
    protected static HashMap findDistributions() {
        Distribution[] all = Distribution.findAll();
        HashMap tmp = new HashMap();
        for (int i=0; i<all.length; i++)
            tmp.put(all[i].getName(), all[i].getClass());
        return tmp;
    }
// -------------------------------------------------------------------------------------------------

// --- Method to collect results -------------------------------------------------------------------
    /**
     * Returns Distribution selected in this dialog or initial one if cancel button was pressed
     * @return Selected distribution if okay button was pressed, initial otherwise. If this
     * dialog has not been shown yet, returns initial value too.
     */
    public Distribution getResult() {
        return target;
    }
// -------------------------------------------------------------------------------------------------

// --- Constructors to create modal dialog ---------------------------------------------------------
    /**
     * Builds a new Distribution Editor Dialog. This dialog is designed to be modal.
     * @param owner owner Dialog for this dialog.
     * @param initial Reference to initial distribution to be shown
     */
    public DistributionsEditor (Dialog owner, Distribution initial) {
        super(owner, true);
        initData(initial);
        initComponents();
    }

    /**
     * Builds a new Distribution Editor Dialog. This dialog is
     *  designed to be modal.
     * @param owner owner Frame for this dialog.
     * @param initial Reference to initial distribution to be shown
     */
    public DistributionsEditor (Frame owner, Distribution initial) {
        super(owner, true);
        initData(initial);
        initComponents();
    }
// -------------------------------------------------------------------------------------------------


// --- Actions performed by buttons and EventListeners ---------------------------------------------
    // When okay button is pressed
    protected AbstractAction okayAction = new AbstractAction("OK") {
        {
            putValue(Action.SHORT_DESCRIPTION, "Closes this window and apply changes");
        }
        public void actionPerformed(ActionEvent e) {
            // Checks if distribution parameters are correct
            if (current.checkValue()) {
                target = current;
                DistributionsEditor.this.dispose();
            }
            else
                JOptionPane.showMessageDialog(DistributionsEditor.this,
                        "Error in distribution parameters: " + current.getPrecondition(),
                        "Wrong parameters error",
                        JOptionPane.ERROR_MESSAGE);
        }
    };

    // When cancel button is pressed
    protected AbstractAction cancelAction = new AbstractAction("Cancel") {
        {
            putValue(Action.SHORT_DESCRIPTION, "Closes this window discarding all changes");
        }
        public void actionPerformed(ActionEvent e) {
            target = initial;
            DistributionsEditor.this.dispose();
        }
    };

    /**
     * Listener used to set parameters (associated to param_panel's JTextFields).
     * Parameters are set when JTextField loses focus or ENTER key is pressed.
     */
    protected KeyFocusAdapter parameterListener = new KeyFocusAdapter() {
        /* (non-Javadoc)
         * @see jmt.framework.gui.listeners.KeyFocusAdapter#updateValues(java.awt.event.ComponentEvent)
         */
        protected void updateValues(ComponentEvent e) {
            // Finds parameter number
            JTextField sourcefield = (JTextField) e.getSource();
            int num = Integer.parseInt(sourcefield.getName());
            current.getParameter(num).setValue(sourcefield.getText());
            current.updateCM();
            refreshValues();
        }
    };

    /**
     * Listener that listens on Mean and C variations and updates parameters
     */
    protected KeyFocusAdapter cmListener = new KeyFocusAdapter(){
        /* (non-Javadoc)
         * @see jmt.framework.gui.listeners.KeyFocusAdapter#updateValues(java.awt.event.ComponentEvent)
         */
        protected void updateValues(ComponentEvent e) {
            // Finds parameter number
            JTextField sourcefield = (JTextField) e.getSource();
            try {
                if (sourcefield.getName().equals("mean")) {
                    current.setMean(Double.parseDouble(sourcefield.getText()));
                }else if (sourcefield.getName().equals("c")) {
                    current.setC(Double.parseDouble(sourcefield.getText()));
                }
            } catch (NumberFormatException ex) { // Do nothing
            }
            refreshValues();
        }
    };

    /**
     * Listener for choser ComboBox to instantiate a new distributions data object when
     * current distribution type is changed.
     */
    protected ItemListener change_listener = new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            try {
                current = (Distribution)((Class) distributions.get(e.getItem())).newInstance();
                refreshView();
            } catch (InstantiationException ex) {
                System.out.println("Error: Error instantiating selected Distribution");
                ex.printStackTrace();
            } catch (IllegalAccessException ex) {
                System.out.println("Error: Error accessing to selected Distribution");
                ex.printStackTrace();
            }
        }
    };
// -------------------------------------------------------------------------------------------------

// --- Initialize data structure and layout --------------------------------------------------------
    /**
     * Initialize this dialog data structures
     * @param initial Reference to initial distribution to be shown
     */
    protected void initData(Distribution initial) {
        this.initial = initial;
        this.target = initial;
        if (initial != null)
            this.current = (Distribution)initial.clone();
        else
            this.current = new Exponential(); // Default distribution if nothing is selected

        // If distributions is not already set, sets it!
        if (distributions == null)
            distributions = findDistributions();
    }

    /**
     * Initialize this dialod's components and default dialog property
     */
    protected void initComponents() {
        // Sets default title, close operation and dimensions
        this.setTitle("Editing Distribution...");
        int width = 320, height=400;

        // Centers this dialog on the screen
        this.centerWindow(width, height);

        // Creates a main panel and adds margins to it
        JPanel mainpanel = new JPanel(new BorderLayout());
        mainpanel.setLayout(new BorderLayout());
        mainpanel.add(Box.createVerticalStrut(BORDERSIZE), BorderLayout.NORTH);
        mainpanel.add(Box.createVerticalStrut(BORDERSIZE), BorderLayout.SOUTH);
        mainpanel.add(Box.createHorizontalStrut(BORDERSIZE), BorderLayout.WEST);
        mainpanel.add(Box.createHorizontalStrut(BORDERSIZE), BorderLayout.EAST);
        this.getContentPane().add(mainpanel, BorderLayout.CENTER);

        // Creates a subpanel that holds scrolledpanel and distr_panel and adds it to mainpanel
        JPanel subpanel = new JPanel(new BorderLayout());
        mainpanel.add(subpanel, BorderLayout.CENTER);
        JPanel distr_panel = new JPanel(new BorderLayout());
        subpanel.add(distr_panel, BorderLayout.NORTH);

        // Creates scrolledpanel that holds param_panel and mean_c_panel
        JPanel scrolledpanel = new JPanel(new GridLayout(2,1));
        subpanel.add(new JScrollPane(scrolledpanel), BorderLayout.CENTER);
        scrolledpanel.add(param_panel);
        scrolledpanel.add(mean_c_panel);
        mean_c_panel.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.gray));

        // Adds bottom_panel to contentpane
        JPanel bottom_panel = new JPanel(new FlowLayout());
        this.getContentPane().add((bottom_panel), BorderLayout.SOUTH);

        // Adds Okay button to bottom_panel
        JButton okaybutton = new JButton(okayAction);
        bottom_panel.add(okaybutton);

        // Adds Cancel button to bottom_panel
        JButton cancelbutton = new JButton(cancelAction);
        bottom_panel.add(cancelbutton);

        // Adds distribution chooser
        distr_panel.add(new JLabel("Selected Distribution: "), BorderLayout.WEST);
        Object[] distributionNames = distributions.keySet().toArray();
        Arrays.sort(distributionNames); // Sorts alphabetically distribution names
        choser = new JComboBox(distributionNames);
        choser.setToolTipText("Choose distribution type");
        // Select correct distribution
        if (current != null) {
            choser.setSelectedItem(current.getName());
            refreshView();
        }
        choser.addItemListener(change_listener);
        distr_panel.add(choser, BorderLayout.CENTER);

        // Adds image viewer with a couple of borders
        JPanel image_panel = new JPanel (new BorderLayout());
        distr_panel.add(image_panel, BorderLayout.SOUTH);
        image_panel.add(Box.createVerticalStrut(BORDERSIZE / 2), BorderLayout.NORTH);
        image_panel.add(Box.createVerticalStrut(BORDERSIZE / 2), BorderLayout.SOUTH);
        image_panel.add(iconpanel, BorderLayout.CENTER);
    }
// -------------------------------------------------------------------------------------------------

// --- Shows current distribution ------------------------------------------------------------------
    protected void refreshView() {
        if (current != null) {
            // Flushes param_panel
            param_panel.removeAll();
            mean_c_panel.removeAll();
            // Shows image
            iconpanel.setImage(current.getImage());
            // Maximum width (used to line up elements of both panels)
            int maxwidth = new JLabel("mean:", JLabel.TRAILING).getMinimumSize().width;

            // Shows this distribution's parameters on param_panel
            JLabel label;
            JTextField textfield;
            for (int i=0; i<current.getNumberOfParameters(); i++) {
                // Creates the label
                label = new JLabel(current.getParameter(i).getDescription() + ":", JLabel.TRAILING);
                // Corrects maxwidth if needed
                if (maxwidth < label.getMinimumSize().width)
                    maxwidth = label.getMinimumSize().width;
                param_panel.add(label, new SpringLayout.Constraints(Spring.constant(0), Spring.constant(0),
                        Spring.constant(maxwidth), Spring.constant(label.getMinimumSize().height)));
                // Creates the fextfield used to input values
                textfield = new JTextField(5);
                textfield.setMaximumSize(new Dimension(textfield.getMaximumSize().width,
                                                    textfield.getMinimumSize().height));
                label.setLabelFor(textfield);
                textfield.setName(Integer.toString(i));
                textfield.addFocusListener(parameterListener);
                textfield.addKeyListener(parameterListener);
                param_panel.add(textfield);
            }
            SpringUtilities.makeCompactGrid(param_panel,
                                            current.getNumberOfParameters(), 2, //rows, cols
                                            6, 6,                               //initX, initY
                                            6, 6);                              //xPad, yPad

            // Now shows mean and c (if applicable) on mean_c_panel
            if (current.hasC() || current.hasMean()) {
                int rows = 0;
                mean_c_panel.setVisible(true);
                // Builds mean section
                if (current.hasMean()) {
                    rows++;
                    // Creates the label
                    label = new JLabel("mean:", JLabel.TRAILING);
                    mean_c_panel.add(label, new SpringLayout.Constraints(Spring.constant(0), Spring.constant(0),
                        Spring.constant(maxwidth), Spring.constant(label.getMinimumSize().height)));
                    // Creates the fextfield used to input mean values
                    textfield = new JTextField(5);
                    textfield.setMaximumSize(new Dimension(textfield.getMaximumSize().width,
                                                        textfield.getMinimumSize().height));
                    label.setLabelFor(textfield);
                    textfield.setName("mean");
                    textfield.addFocusListener(cmListener);
                    textfield.addKeyListener(cmListener);
                    mean_c_panel.add(textfield);
                }

                // Builds c section
                if (current.hasC()) {
                    rows++;
                    // Creates the label
                    label = new JLabel("c:", JLabel.TRAILING);
                    mean_c_panel.add(label, new SpringLayout.Constraints(Spring.constant(0), Spring.constant(0),
                        Spring.constant(maxwidth), Spring.constant(label.getMinimumSize().height)));
                    // Creates the fextfield used to input mean values
                    textfield = new JTextField(5);
                    textfield.setMaximumSize(new Dimension(textfield.getMaximumSize().width,
                                                        textfield.getMinimumSize().height));
                    label.setLabelFor(textfield);
                    textfield.setName("c");
                    textfield.addFocusListener(cmListener);
                    textfield.addKeyListener(cmListener);
                    mean_c_panel.add(textfield);
                }
                SpringUtilities.makeCompactGrid(mean_c_panel,
                                                rows, 2,                            //rows, cols
                                                6, 6,                               //initX, initY
                                                6, 6);                              //xPad, yPad
            }
            else
                mean_c_panel.setVisible(false);
            // Sets text for values
            refreshValues();
            param_panel.getParent().repaint();
        }
    }

    protected void refreshValues() {
        // refresh all values into param_panel
        Component[] components = param_panel.getComponents();
        // Formatter to show only 12 decimal digits to avoid machine-epsilon problems
        DecimalFormat df = new DecimalFormat("#.############");
        df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ENGLISH));
        int num;
        for (int i=0; i<components.length; i++) {
            if (components[i] instanceof JTextField) {
                num = Integer.parseInt(components[i].getName());
                Object value = current.getParameter(num).getValue();
                if (value instanceof Double) {
                    double val = ((Double) value).doubleValue();
                    ((JTextField)components[i]).setText(df.format(val));
                }
                else
                    ((JTextField)components[i]).setText(value.toString());
            }
        }
        // refresh all values into mean_c_panel
        components = mean_c_panel.getComponents();
        for (int i=0; i<components.length; i++) {
            // Shows only first 10 decimal digits
            if (components[i] instanceof JTextField && components[i].getName().equals("mean")) {
                ((JTextField)components[i]).setText(df.format(current.getMean()));
            } else if (components[i] instanceof JTextField && components[i].getName().equals("c")) {
                ((JTextField)components[i]).setText(df.format(current.getC()));
            }
        }
        param_panel.getParent().repaint();
    }
// -------------------------------------------------------------------------------------------------
}
