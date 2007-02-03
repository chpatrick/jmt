package jmt.gui.jwat.input;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import jmt.engine.jwat.MatrixOsservazioni;
import jmt.engine.jwat.TimeConsumingWorker;

public abstract class InputLoader extends TimeConsumingWorker {
	
	protected Parameter param;
	protected BufferedReader reader;
	protected VariableMapping[] map;
	protected ArrayList valori;
	protected int countObs,totalRaw;
	protected String msg;
	
	
	public InputLoader(Parameter param,String fileName,VariableMapping[] map,ProgressShow prg) throws FileNotFoundException{
		super(prg);
		this.param=param;
		this.map=map;
		valori=new ArrayList();
		reader=new BufferedReader(new FileReader(fileName));
	}
	
	public void finished()
	{
		if(this.get()!=null){
			fireEventStatus(new EventFinishLoad((MatrixOsservazioni)this.get(),totalRaw,countObs));
		}
		else{
			fireEventStatus(new EventFinishAbort(msg));
		}
	}
}
