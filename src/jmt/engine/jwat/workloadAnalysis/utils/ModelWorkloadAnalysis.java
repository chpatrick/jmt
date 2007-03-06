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
package jmt.engine.jwat.workloadAnalysis.utils;

import java.util.Vector;

import jmt.engine.jwat.MatrixOsservazioni;
import jmt.engine.jwat.filters.FilterOnVariable;
import jmt.engine.jwat.workloadAnalysis.clustering.Clustering;
import jmt.engine.jwat.workloadAnalysis.exceptions.TrasformException;
import jmt.gui.jwat.JWatModel;
import jmt.gui.jwat.MainJwatWizard;
import jmt.gui.jwat.workloadAnalysis.wizard.WorkloadAnalysisWizard;

public class ModelWorkloadAnalysis implements JWatModel{
	// Matrice delle osservazioni
	private MatrixOsservazioni matrix = null;
	// Finestra di partenza
	private MainJwatWizard parent = null;
	// vector of the listener on set matrix
	private Vector listenerOnMatrixChange = null; //<SetMatrixListener> 
	// vector of the listener on change variable ( transformations )
	private Vector listenerOnChangeVariable = null; //<ChangeVariableListener> 
	// vector containing the results of one or more clustering operations
	private Vector clusterOperation=null; //<Clustering> 
	// vector of the listener on adding clustering or deleting
	private Vector listenerOnModifyClustering = null;


	/**
	 * @param par
	 */
	public ModelWorkloadAnalysis(MainJwatWizard par){
		parent = par;
		listenerOnMatrixChange = new Vector();
		listenerOnChangeVariable = new Vector();
		clusterOperation=new Vector();
		listenerOnModifyClustering = new Vector();
	}
	
	public Vector getListOfClustering()
	{
		return clusterOperation;
	}
	
	public void addClustering(Clustering clust)
	{
		clusterOperation.add(clust);
		fireNotifyOnModifiedClustering();
	}

	public void removeClustering(int pos){
		if(pos < clusterOperation.size()){
			clusterOperation.remove(pos);
			fireNotifyOnModifiedClustering();
			System.err.println(Runtime.getRuntime().freeMemory());
			System.gc();
			System.err.println(Runtime.getRuntime().freeMemory());
		}
	}
	
	public void removeAllClustering(){
		clusterOperation.removeAllElements();
		fireNotifyOnModifiedClustering();
	}

	/**
	 * @return
	 */
	public MatrixOsservazioni getMatrix() {
		return matrix;
	}
	/**
	 * @return
	 */
	public MainJwatWizard getParent() {
		return parent;
	}
	/**
	 * @param matrix
	 */
	public void setMatrix(MatrixOsservazioni matrix) {
		this.matrix = matrix;
		fireNotifyOnSetMatrixObservation();
	}
	/**
	 *
	 *@param listener
	 */
	public void addOnSetMatrixObservationListener(SetMatrixListener listener){
		listenerOnMatrixChange.add(listener);
	}
	/**
	 * 
	 * @param listener
	 */
	public void addOnChangeVariableValue(ChangeVariableListener listener){
		if(!listenerOnChangeVariable.contains(listener)) listenerOnChangeVariable.add(listener);
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void addOnAddOrDeleteClustering(ModifiedClustering listener){
		listenerOnModifyClustering.add(listener);
	}
	/**
	 * 
	 * @param var
	 */
	public void setTransformation(){
		for(int i = 0; i < listenerOnChangeVariable.size(); i++){
			((ChangeVariableListener) listenerOnChangeVariable.get(i)).onChangeVariableValues();
		}
	}
	/*
	 * Notify change on matrix observation to all registered listener 
	 */
	private void fireNotifyOnSetMatrixObservation(){
		for(int i = 0; i < listenerOnMatrixChange.size(); i++){
			((SetMatrixListener) listenerOnMatrixChange.get(i)).onSetMatrixObservation();
		}
	}
	private void fireNotifyOnResetMatrixObservation(){
		for(int i = 0; i < listenerOnMatrixChange.size(); i++){
			((SetMatrixListener) listenerOnMatrixChange.get(i)).onResetMatrixObservation();
		}
	}
	private void fireNotifyOnModifiedClustering(){
		for(int i = 0; i < listenerOnModifyClustering.size(); i++){
			((ModifiedClustering) listenerOnModifyClustering.get(i)).onModifiedClustering();
		}
	}
	public void doTransformationOnVariable(int varSel,short type) throws TrasformException{
		matrix.applyTransformation(varSel,type);
		setTransformation();
	}
	public boolean undoTransformationOnVariable(int varSel){
		boolean b = matrix.undoTransformation(varSel);
		setTransformation();
		return b;
	}
	public void doSamplingOnVariable(int varSel,FilterOnVariable filter){
		matrix.doSampling(varSel,filter);
		setTransformation();
	}
	public void undoSamplingOnVariable(int varSel){
		matrix.undoSampling(varSel);
		setTransformation();
	}
	//UPDATE 28/10/2006: +spostamento operazioni di trasformazione e sampling in matrixOsservazioni
	public void resetModel(){
		matrix = null;
		clusterOperation.removeAllElements();
		fireNotifyOnResetMatrixObservation();
	}
}
