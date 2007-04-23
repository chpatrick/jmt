package jmt.engine.jwat;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jmt.engine.jwat.input.Mapping;
import jmt.engine.jwat.input.VariableMapping;
import Jama.Matrix;

public class JwatSession {
	
	private String filepath;
	private String filename;
	private static String XMLext=".xml";
	private static String BINext=".bin";
	private static String ROOT="JWat_Save";
	private static String DATA="Data_File";
	
	public JwatSession(String filepath,String filename){
		this.filepath=filepath;	
		this.filename=filename;
	}
	
	public void saveSession(MatrixOsservazioni matrix){
		System.out.println("PATH " + filepath);
		System.out.println("NAME " + filename);
		DocumentBuilderFactory dbf= DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db=dbf.newDocumentBuilder();
			Document doc=db.newDocument();
			//Init root
			Element root= doc.createElement(ROOT);
			doc.appendChild(root);
			root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
			root.setAttribute("xsi:noNamespaceSchemaLocation", "jwatsave.xsd");
			
			Observation[] obs=matrix.getListOss();
			VariableNumber[] var=matrix.getVariables();
			
			saveVariablesInfo(var, doc,root);
			saveDataInfo(obs, doc,root);
			saveMatrixData(obs,var);
			
			Transformer tr=TransformerFactory.newInstance().newTransformer();
			tr.setOutputProperty(OutputKeys.INDENT, "yes");
			tr.setOutputProperty(OutputKeys.METHOD,"xml");
			tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
			tr.transform( new DOMSource(doc),new StreamResult(new FileOutputStream(filepath+filename+".xml")));			
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	private void saveVariablesInfo(VariableNumber[] var,Document doc,Element root)
	{
		Element varEl=doc.createElement("Variables");
		Element tmp;
		int numVar=var.length;
		
		varEl.setAttribute("num", String.valueOf(numVar));
		
		for(int i=0;i<numVar;i++){
			tmp=doc.createElement("Variable");
			tmp.setAttribute("name", var[i].getName());
			tmp.setAttribute("type", String.valueOf(var[i].getType()));
			varEl.appendChild(tmp);
		}
		root.appendChild(varEl);
	}
	
	private void saveDataInfo(Observation[] obs,Document doc,Element root)
	{
		
		Element dataEl=doc.createElement("Data");
		dataEl.setAttribute("size", String.valueOf(obs.length));
		dataEl.setAttribute("filename", filename+BINext);
		
		root.appendChild(dataEl);
	}
	
	private void saveMatrixData(Observation[] obs, VariableNumber[] var){
		System.out.println("Saving matrix");
		try {
			DataOutputStream dos= new DataOutputStream(new FileOutputStream(filepath+filename+".bin"));
			int i,j;
			int size;
			
			
			size=obs[0].getSize();
			//save varaible mapping
			for(j=0;j<size;j++){
				if(var[j].getType()==VariableNumber.STRING) saveVarMapping(var[j]);
			}
			//save data
			for(i=0;i<obs.length;i++){
				dos.write(obs[i].getID());				
				for(j=0;j<size;j++){
					dos.writeDouble(obs[i].getIndex(j));
				}
			}
			dos.flush();
			dos.close();
			
			System.out.println("Save done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveVarMapping(VariableNumber  var) throws IOException{
		System.out.println("VAR "+ filepath + var.getName()+"_Map"+BINext);
		DataOutputStream dos=new DataOutputStream(new FileOutputStream(filepath + var.getName()+"_Map"+BINext));
		
		
		Mapping[] map=var.getMapping().getMappingValue();
		
		dos.write(map.length);
		for(int i=0;i<map.length;i++){
			dos.writeDouble(map[i].getConversion());
			dos.writeUTF(map[i].getValue().toString());
		}
		
	}
}
