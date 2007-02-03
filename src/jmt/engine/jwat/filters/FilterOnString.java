package jmt.engine.jwat.filters;

import jmt.engine.jwat.Observation;

public class FilterOnString implements FilterOnVariable {
	
	private int[] varIndex;
	private int index;
	
	public FilterOnString(int var,int[] index)
	{
		varIndex=index;
		this.index = var;
	}
	
	public boolean isMatching(String value) {	
		return false;
	}

	public boolean isMatching(Observation o, int pos) {
		for(int i = 0; i < varIndex.length; i++){
			if(o.getIndex(index) == varIndex[i]) return true;
		}
		return false;
	}

}
