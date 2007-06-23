package jmt.engine.jwat.trafficAnalysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jmt.engine.jwat.JwatSession;
import jmt.engine.jwat.MatrixOsservazioni;
import jmt.gui.jwat.JWatModel;

public class TrafficAnalysisSession extends JwatSession {
	
	private BurstEngine engine = null;
	private int epochs = -1;
	private ArrayList setParameters = new ArrayList();
	
	public TrafficAnalysisSession() {
		super(new ModelTrafficAnalysis());
	}
	
	public TrafficAnalysisSession(ModelTrafficAnalysis model,int epochs) {
		super(model);
		engine = new BurstEngine(model.getMatrix().getVariables()[0],epochs,null);
		this.epochs = epochs;
	}
	
	public TrafficAnalysisSession(ModelTrafficAnalysis model,String filepath,String filename,int epochs){
		super(model,filepath,filename);
		engine = new BurstEngine(model.getMatrix().getVariables()[0],epochs,null);
		this.epochs = epochs;
	}
	
	public void addSetParamsListener(OnSetParamtersListener lst){
		if(!setParameters.contains(lst)) setParameters.add(lst);
	}
	
	public void removeSetParamsListener(OnSetParamtersListener lst){
		setParameters.remove(lst);
	}
	
	private void notifySetParams(){
		for(int i = 0; i < setParameters.size(); i++)
			((OnSetParamtersListener)setParameters.get(i)).ParamsSetted();
	}
	
	public BurstEngine getEngine(){
		return engine;
	}
	
	public void resetSession() {
		model.resetModel();
	}
	
	public void setMatrix(MatrixOsservazioni m){
		model.setMatrix(m);
		if (epochs != -1){
			engine = new BurstEngine(m.getVariables()[0],epochs,null);
			notifySetParams();
		}
	}
	
	public void setParameters(int epochs){
		this.epochs = epochs;
		if (model.getMatrix()!= null){
			engine = new BurstEngine(model.getMatrix().getVariables()[0],epochs,null);
			notifySetParams();
		}
	}

	public void appendXMLResults(Document doc, Element root, ZipOutputStream zos) {

	}

	public void saveResultsFile(Document doc, Element root, ZipOutputStream zos) throws IOException{
		
	}

	public void copySession(JwatSession newSession) {
		model.setMatrix(newSession.getDataModel().getMatrix());	
	}
}
