package jmt.gui.exact.panels;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTabbedPane;

import jmt.analytical.SolverAlgorithm;
import jmt.framework.gui.wizard.WizardPanel;
import jmt.gui.exact.ExactWizard;

/**
 * <p>Title: Algorithm Panel</p>
 * <p>Description: This panel is used to provide tabs for different algorithms
 * in what-if analysis for closed models.</p>
 *
 * @author Abhimanyu Chugh
 * 
 */
public class AlgorithmPanel extends WizardPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	// Vector with all solution panels
	private ArrayList<SolutionPanel> panels = new ArrayList<SolutionPanel>();
	// Data structure
	protected ExactWizard ew;
	// Dimension of spinner

	private JTabbedPane tabber = new JTabbedPane();
	
	protected SolverAlgorithm algorithm;
	protected int iteration = 0;

	public AlgorithmPanel(ExactWizard ew, SolverAlgorithm alg) {
		this.ew = ew;
		this.algorithm = alg;
		initComponents();
	}

	/**
	 * Adds a solutionPanel to be notified when selected iteration changes.
	 * This component is placed in the north of given panel.
	 * @param s solutionPanel to be added
	 */
	public void addSolutionPanel(SolutionPanel s) {
		panels.add(s);
		tabber.add(s);
	}

	/**
	 * Selects iteration i in all given panels
	 * @param i iteration to be selected
	 */
	private void select(int i) {
		Iterator<SolutionPanel> it = panels.iterator();
		while (it.hasNext()) {
			it.next().setIteration(i);
		}
	}

	/**
	 * Initialize all gui objects
	 */
	private void initComponents() {
		setLayout(new BorderLayout());
		add(tabber, BorderLayout.CENTER);
		select(iteration);
	}

	/**
	 * @return the panel's name
	 */
	@Override
	public String getName() {
		return algorithm.toString();
	}
	
	/**
	 * Sets iteration number for results to be shown. This is used for
	 * what-if analysis. Default is first iteration (0)
	 * @param iteration number of iteration to be shown. Must be between 0
	 * and [number of iterations]-1
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
		select(iteration);
	}

}
