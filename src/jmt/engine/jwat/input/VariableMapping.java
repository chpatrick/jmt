package jmt.engine.jwat.input;

import java.util.ArrayList;

public abstract class VariableMapping 
{
	public abstract double convertToDouble(String val);
	public Object getValue(double number)
	{
		return new Integer(valMap.indexOf(new Mapping(number,null)));
	}
	public double addNewValue(String value)
	{
		//Check if value is already inserted in the mapping.
		for(int i=0;i<valMap.size();i++) 
		{
			if (((Mapping)valMap.get(i)).getValue().equals(value)) 
			       return ((Mapping)valMap.get(i)).getConversion();
		}
			
		double trad=convertToDouble(value);
		valMap.add(new Mapping(trad,value));
		return trad;
	}
	
	ArrayList valMap=new ArrayList();
	
}
