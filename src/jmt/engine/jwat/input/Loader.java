/**    
  * Copyright (C) 2007, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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
package jmt.engine.jwat.input;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.stream.FileImageInputStream;
import javax.xml.parsers.DocumentBuilder;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jmt.engine.jwat.MatrixOsservazioni;
import jmt.engine.jwat.Observation;
import jmt.engine.jwat.ProgressStatusListener;
import jmt.engine.jwat.VariableNumber;
import jmt.engine.jwat.workloadAnalysis.utils.FormatFileReader;
import jmt.gui.jwat.JWATConstants;

public class Loader implements JWATConstants{
	
	public static int calcNumOfObs(String filePath)throws FileNotFoundException,IOException
	{
		int numObs=0;
		BufferedReader reader;
		reader= new BufferedReader(new FileReader(filePath));
		
		while(reader.readLine()!=null)
		{
			numObs++;
		}
		return numObs;
	}
	
	public static void readData(String filePath,Parameter param,ProgressShow prShow,ProgressStatusListener pStatusList)
	{  
		InputLoader loads=null;
		VariableMapping[] map;  
		
		//Initializating the object necessary to load data.
		map=new VariableMapping[param.getNumVar()];
		
		int[] varType=param.getVarType();
		
		//Creo i Mapping
		for(int i=0;i<map.length;i++)
		{
			switch(varType[i])
			{
			case Parameter.NUMBER:
				map[i]=null;
				break;
			case Parameter.DATE:
				map[i]=new DataMapping();
				break;
			case Parameter.STRING:
				map[i]=new StringMapping();
				//map[i]=new positionalMapping();
				break;
			}
		}
		try{	
			switch (param.getSampleMethod()){
				case Parameter.ALL_INPUT: loads=new AllInputLoader(param,filePath,map,prShow);
					break;
				case Parameter.INTERVAL_INPUT: loads=new IntervalInputLoader(param,filePath,map,prShow);
					break;
				case Parameter.RANDOM_INPUT: loads=new RndInputLoader(param,filePath,map,prShow);
					break;
			}
		}catch(FileNotFoundException e){
			pStatusList.statusEvent(new EventFinishAbort("Loading aborted. File not found."));
		}
		loads.addStatusListener(pStatusList);
		loads.start();
	}
	
	public static Parameter loadParameter(String demoName) throws FileNotFoundException,IOException
	{
		FormatFileReader form = new FormatFileReader(absolutePath + "examples/" + demoName + "Format.jwatformat");
		boolean[] varSelected=new boolean[form.getNumVars()];
	    String[] varName=new String[form.getNumVars()];
	    String[] regularExp=new String[form.getNumVars()];
	    String[] tokenExp=new String[form.getNumVars()];
	    int[] varType=new int[form.getNumVars()];
	    int options[]=new int[]{Loader.calcNumOfObs(absolutePath + "examples/" + demoName + "Data.jwat")};
		
		for(int i = 0;i < form.getNumVars();i++){
			varSelected[i]=true;
			varType[i]=form.getType();
			varName[i]=form.getName();
			regularExp[i]=form.getRegExpr();
			tokenExp[i]=form.getDelimiters();
			if(tokenExp[i].length()==0) tokenExp[i]=null;
			form.next();
		}
		
		return new Parameter(varSelected,
				varType,regularExp,tokenExp,
	    		varName,Parameter.ALL_INPUT,null,options,
	    		null,null);
	}
		
	/**
	 * This function reads a specific session from an XML file
	 * 
	 * @param filePath
	 */
	public static MatrixOsservazioni loadSession(String xmlfilePath,String xmlfileName)
	{
		Observation[] valori;
		String[] selName;
		int[] selType;
		double[] valLst;
		int numObs,numVar,i,j,id;
		MatrixOsservazioni m=null;
		VariableMapping[] map;
		DOMParser domP;
		NodeList tmpNodeLst;
		Node tmpNode;
		String tmpVal,file;
		
		DataInputStream dis;
		System.out.println("PATH " + xmlfilePath);
		System.out.println("NAME " + xmlfileName);
		try {
			//load xml file
			domP=new DOMParser();
			domP.parse(xmlfilePath+xmlfileName);
			Document doc=domP.getDocument();
			//parse variables
			tmpNodeLst=doc.getElementsByTagName("Variables");
			tmpNode=tmpNodeLst.item(0);
			tmpVal=tmpNode.getAttributes().getNamedItem("num").getNodeValue();
			numVar=Integer.parseInt(tmpVal);
			selName=new String[numVar];
			selType=new int[numVar];
			
			tmpNodeLst=tmpNode.getChildNodes();
			System.out.println(tmpNodeLst.getLength());
			j=0;
			for(i=0;i<tmpNodeLst.getLength();i++){
				tmpNode=tmpNodeLst.item(i);
				if(tmpNode.getNodeType()==Node.ELEMENT_NODE){
					System.out.println("ELEMENTO " + tmpNode.getNodeValue());
					tmpVal=tmpNode.getAttributes().getNamedItem("name").getNodeValue();
					selName[j]=tmpVal;
					tmpVal=tmpNode.getAttributes().getNamedItem("type").getNodeValue();
					selType[j]=Integer.parseInt(tmpVal);
					j++;
				}
			}
			
			//parse data
			tmpNodeLst=doc.getElementsByTagName("Data");
			tmpNode=tmpNodeLst.item(0);
			tmpVal=tmpNode.getAttributes().getNamedItem("size").getNodeValue();
			numObs=Integer.parseInt(tmpVal);
			file=tmpNode.getAttributes().getNamedItem("filename").getNodeValue();
			
			valori=new Observation[numObs];
			map=new VariableMapping[numVar];
			valLst=new double[numVar];
			
			System.out.println("Reading varaibles mapping");
			for(j=0;j<numVar;j++){
				if(selType[j]==VariableNumber.DATE) map[j]=new DataMapping();
				if(selType[j]==VariableNumber.NUMERIC) map[j]=null;
				if(selType[j]==VariableNumber.STRING) map[j]=loadVarMapping(xmlfilePath,selName[j]);
				System.out.println(selName[j] + " " + selType[j]);
			}
			
			//load data file
			dis = new DataInputStream(new FileInputStream(xmlfilePath+file));
			
			for(i=0;i<numObs;i++){
				id=dis.read();				
				for(j=0;j<numVar;j++){
					valLst[j]=dis.readDouble();
				}
				valori[i]=new Observation(valLst,id);
			}
			System.out.println("Create matrix");
			m=new MatrixOsservazioni(valori,selName,selType,map);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return m;
	}	
	
	private static StringMapping loadVarMapping(String path,String varName) throws IOException{
		DataInputStream dis=new DataInputStream(new FileInputStream(path + varName+"_Map.bin"));
		
		StringMapping map=new StringMapping();  
		double val;
		String str;
		int numEl=dis.read();
		
		for(int i=0;i<numEl;i++){
			val=dis.readDouble();
			str=dis.readUTF();
			map.addNewMapping(val, str);
		}
		
		return map;
	}
}
