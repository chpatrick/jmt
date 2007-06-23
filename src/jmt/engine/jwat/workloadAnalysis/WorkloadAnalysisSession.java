package jmt.engine.jwat.workloadAnalysis;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jmt.engine.jwat.JwatSession;
import jmt.engine.jwat.workloadAnalysis.clustering.Clustering;
import jmt.engine.jwat.workloadAnalysis.clustering.fuzzyKMean.FuzzyKMean;
import jmt.engine.jwat.workloadAnalysis.clustering.kMean.ClusterInfoKMean;
import jmt.engine.jwat.workloadAnalysis.clustering.kMean.ClusteringInfosKMean;
import jmt.engine.jwat.workloadAnalysis.clustering.kMean.KMean;
import jmt.engine.jwat.workloadAnalysis.utils.ModelWorkloadAnalysis;
import jmt.engine.jwat.workloadAnalysis.utils.ModifiedClustering;
import jmt.gui.jwat.JWATConstants;
import jmt.gui.jwat.JWatModel;

public class WorkloadAnalysisSession extends JwatSession {
	// vector containing the results of one or more clustering operations
	private Vector clusterOperation=null; //<Clustering> 
	// vector of the listener on adding clustering or deleting
	private Vector listenerOnModifyClustering = null;

	public WorkloadAnalysisSession() {
		super(new ModelWorkloadAnalysis());
		clusterOperation=new Vector();
		listenerOnModifyClustering = new Vector();
		
	}
	
	public WorkloadAnalysisSession(ModelWorkloadAnalysis model) {
		super(model);
		clusterOperation=new Vector();
		listenerOnModifyClustering = new Vector();
	}
	
	public WorkloadAnalysisSession(ModelWorkloadAnalysis model,String filepath,String filename){
		super(model,filepath,filename);
		clusterOperation=new Vector();
		listenerOnModifyClustering = new Vector();
	}

	public void addClustering(Clustering clust)
	{
		clusterOperation.add(clust);
		fireNotifyOnModifiedClustering();
	}

	public Vector getListOfClustering()
	{
		return clusterOperation;
	}

	private void fireNotifyOnModifiedClustering(){
		for(int i = 0; i < listenerOnModifyClustering.size(); i++){
			((ModifiedClustering) listenerOnModifyClustering.get(i)).onModifiedClustering();
		}
	}
	
	/**
	 * 
	 * @param listener
	 */
	public void addOnAddOrDeleteClustering(ModifiedClustering listener){
		listenerOnModifyClustering.add(listener);
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

	public void resetSession(){
		clusterOperation.removeAllElements();
		model.resetModel();
	}

	public void appendXMLResults(Document doc, Element root, ZipOutputStream zos) {
		Element varEl=doc.createElement("Results");
		Element tmp;
		int algo,numRes=clusterOperation.size();
		int[] varSel;
		String varStr=null;
		
		varEl.setAttribute("num", String.valueOf(numRes));
		
		for(int i=0;i<numRes;i++){
			tmp=doc.createElement("Clustering");
			tmp.setAttribute("name", ((Clustering)clusterOperation.get(i)).getName());
			algo=((Clustering)clusterOperation.get(i)).getClusteringType();
			tmp.setAttribute("algo", String.valueOf(algo));
			tmp.setAttribute("numcluster", String.valueOf(((Clustering)clusterOperation.get(i)).getNumCluster()));
			varSel=((Clustering)clusterOperation.get(i)).getVarClust();
			for(int s=0;s<varSel.length;s++){
				if(varStr==null){
					varStr=String.valueOf(varSel[s]);
				}
				else{
					varStr+=","+String.valueOf(varSel[s]);
				}
			}
			tmp.setAttribute("varsel", varStr);
			varEl.appendChild(tmp);			
		}
		root.appendChild(varEl);
	}
	
	private void saveKmeansData(ZipOutputStream zos,KMean clustering) throws IOException{
		int i,j,k;
		ClusteringInfosKMean curInfo;
		ClusterInfoKMean curClustInfo[];
		short clustAssign[][];
		DataOutputStream dos=new DataOutputStream(zos);
		clustAssign=clustering.getAsseg();
		
		for(i=0;i<clustering.getNumCluster();i++){
			curInfo=(ClusteringInfosKMean)clustering.getClusteringInfos(i);
			dos.writeInt(curInfo.numCluster+1);
			dos.writeDouble(curInfo.omsr);
			dos.writeDouble(curInfo.ratio);
			curClustInfo=curInfo.infoCluster;
			//Write clusters infos
			for(j=0;j<curClustInfo.length;j++){
				dos.writeInt(curInfo.numElem[j]);
				for(k=0;k<model.getMatrix().getNumVariables();k++){
					dos.writeDouble(curClustInfo[j].percVar[k]);
					dos.writeInt(curClustInfo[j].statClust[k].iNotZr);
					dos.writeDouble(curClustInfo[j].statClust[k].dMedia);
					dos.writeDouble(curClustInfo[j].statClust[k].dStdEr);
					dos.writeDouble(curClustInfo[j].statClust[k].dStdDv);
					dos.writeDouble(curClustInfo[j].statClust[k].dVarnz);
					dos.writeDouble(curClustInfo[j].statClust[k].dKurto);
					dos.writeDouble(curClustInfo[j].statClust[k].dSkewn);
					dos.writeDouble(curClustInfo[j].statClust[k].dRange);
					dos.writeDouble(curClustInfo[j].statClust[k].dMinOs);
					dos.writeDouble(curClustInfo[j].statClust[k].dMaxOs);
				}
			}
			//Write clustering assignment
			for(k=0;k<clustAssign[i].length;k++){
				dos.writeShort(clustAssign[i][k]);
			}
		}
	}
	
	private void saveFuzzyData(ZipOutputStream zos,FuzzyKMean clustering){
		
	}

	public void saveResultsFile(Document doc, Element root, ZipOutputStream zos) throws IOException {
		int algo,numRes=clusterOperation.size();
		String algoName;
		
		for(int i=0;i<numRes;i++){
			algo=((Clustering)clusterOperation.get(i)).getClusteringType();
			algoName=String.valueOf(((Clustering)clusterOperation.get(i)).getName());
			algoName+="_"+i;
			zos.putNextEntry(new ZipEntry(algoName+JwatSession.BINext));
			switch(algo){
			case JWATConstants.KMEANS:
				saveKmeansData(zos,(KMean)clusterOperation.get(i));
				break;
			case JWATConstants.FUZZYK:
				saveFuzzyData(zos,(FuzzyKMean)clusterOperation.get(i));
				break;
			}
			zos.closeEntry();
		}
		
	}

	public void copySession(JwatSession newSession){
		model.setMatrix(newSession.getDataModel().getMatrix());
		Vector listClust=((WorkloadAnalysisSession)newSession).getListOfClustering();
		
		for(int nn=0;nn<listClust.size();nn++){
			addClustering((Clustering)listClust.get(nn));
	}
	
	}
	
}
