package jmt.engine.jwat.trafficAnalysis;

import java.util.ArrayList;

import jmt.engine.jwat.MatrixOsservazioni;
import jmt.gui.jwat.JWatModel;

public class ModelTrafficAnalysis implements JWatModel{
	private MatrixOsservazioni matrix = null;
	private int epochs = -1;
	private ArrayList setParameters = new ArrayList();
	private ArrayList resetModel = new ArrayList();
	private BurstEngine engine = null;
	
	public void setMatrix(MatrixOsservazioni m){
		matrix = m;
		if (epochs != -1){
			engine = new BurstEngine(matrix.getVariables()[0],epochs,null);
			notifySetParams();
		}
	}
	
	public void setParameters(int epochs){
		this.epochs = epochs;
		if (matrix != null){
			engine = new BurstEngine(matrix.getVariables()[0],epochs,null);
			notifySetParams();
		}
	}
	
	public BurstEngine getEngine(){
		return engine;
	}
	
	public void addSetParamsListener(OnSetParamtersListener lst){
		if(!setParameters.contains(lst)) setParameters.add(lst);
	}
	
	public void removeSetParamsListener(OnSetParamtersListener lst){
		setParameters.remove(lst);
	}
	
	public MatrixOsservazioni getMatrix(){
		return matrix;
	}
	
	private void notifySetParams(){
		for(int i = 0; i < setParameters.size(); i++)
			((OnSetParamtersListener)setParameters.get(i)).ParamsSetted();
	}
	
	public void resetModel(){
		matrix = null;
		notifyResetModel();
	}
	
	public void addResetModelListener(OnResetModel listener){
		if(!resetModel.contains(listener)) resetModel.add(listener);
	}
	
	public void removeResetModelListener(OnResetModel listener){
		resetModel.remove(listener);
	}
	
	private void notifyResetModel(){
		for(int i = 0; i < resetModel.size(); i++)
			((OnResetModel)resetModel.get(i)).modelResetted();
	}

}
