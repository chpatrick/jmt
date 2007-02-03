package jmt.gui.jwat.input;
//UPDATE 28/1/2006: + modificato il mapping utilizzato per le stringhe 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import jmt.engine.jwat.ProgressStatusListener;
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
		FormatFileReader form = new FormatFileReader(absolutePath + "examples\\" + demoName + "Format.jwatformat");
		boolean[] varSelected=new boolean[form.getNumVars()];
	    String[] varName=new String[form.getNumVars()];
	    String[] regularExp=new String[form.getNumVars()];
	    String[] tokenExp=new String[form.getNumVars()];
	    int[] varType=new int[form.getNumVars()];
	    int options[]=new int[]{Loader.calcNumOfObs(absolutePath + "examples\\" + demoName + "Data.jwat")};
		
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
	public static void loadSession(String filePath)
	{
		
	}	
}
