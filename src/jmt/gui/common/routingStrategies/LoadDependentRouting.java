package jmt.gui.common.routingStrategies;

import jmt.gui.common.CommonConstants;
import jmt.gui.common.definitions.StationDefinition;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ashanka
 * Date: 9/1/11
 * Time: 12:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class LoadDependentRouting extends RoutingStrategy {

    Map<Integer, EmpiricalEntry[]> values = null;//Contains a Array of EmpiricalEntry where the Integer represents the From
    public LoadDependentRouting(){
        description = "Customers of each class are routed depending on their number on the selected station. " +
                "The outgoing paths must have associated probabilities that should sum to 1 for each range.";
    }
    @Override
    public String getName() {
        return "Load Dependent Routing";
    }

    @Override
    @Deprecated
    public Map<Object, Double> getValues() {
        return null;
    }

    public Map<Integer, EmpiricalEntry[]> getEmpiricalEntries(){
        return values;
    }

    @Override
    public RoutingStrategy clone() {
        LoadDependentRouting newLd = new LoadDependentRouting();
        if(values != null && values.keySet() != null){
            for(Integer from : values.keySet()){
                for (EmpiricalEntry entry : this.getEmpiricalEntryForFrom(from)){
                    newLd.addEmricalEntryForFrom(from, entry.getStationName(), entry.getProbability());
                }
            }
        }else{
            newLd.values = null;
        }
        return newLd;
    }

    @Override
    public String getClassPath() {
        return "jmt.engine.NetStrategies.RoutingStrategies.LoadDependentRoutingStrategy";  //To change body of implemented methods use File | Settings | File Templates.
    }

    public EmpiricalEntry[] getEmpiricalEntryForFrom(Integer i){
        EmpiricalEntry[] out = null;
        if(values != null) {
            out = values.get(i);
        }
        return out;
    }

    public void addEmricalEntryForFrom(Integer from, String stationName, Double prob){
        EmpiricalEntry newEntry = new EmpiricalEntry();
        newEntry.setStationName(stationName);
        newEntry.setProbability(prob);
        if(values != null){
            EmpiricalEntry[] existingEntries = values.get(from);
            if(existingEntries!= null && existingEntries.length>0){
                List<EmpiricalEntry> a = Arrays.asList(existingEntries);
                List<EmpiricalEntry> newlist = new ArrayList<EmpiricalEntry>(a);
                newlist.add(newEntry);
                EmpiricalEntry[] newEntries = new EmpiricalEntry[newlist.size()];
                newEntries = newlist.toArray(newEntries);
                values.put(from,newEntries);
            }else{
                values.put(from, new EmpiricalEntry[]{newEntry});
            }
        }else{
            values = new HashMap<Integer, EmpiricalEntry[]>();
            values.put(from, new EmpiricalEntry[]{newEntry});
        }
    }

    public boolean deleteEmpricalEntryForFrom(Integer from){
        if(values != null){
            values.remove(from);
        }else{
            return false;
        }
        return !values.containsKey(from);
    }

    /**
     * Validates the Routing to see if all the probailities sum to 1.
     * If there is no error then it returns List with size 0.
     * @return
     */
    public List<String> validate(){
        List<String> errors = new ArrayList<String>(0);
        List<String> warnings = new ArrayList<String>(0);

        if(this.getEmpiricalEntries() == null || this.getEmpiricalEntries().size() == 0){
            errors.add("No routing defined");
        }
        if(errors.size() == 0){
            List<Integer> fromAsList = new ArrayList<Integer>();
            fromAsList.addAll(this.getEmpiricalEntries().keySet());
            Collections.sort(fromAsList);
            for(Iterator<Integer> it = fromAsList.iterator(); it.hasNext(); ){
                Integer from = it.next();
                double sum = 0.0;
                for(int counter =0; counter < this.getEmpiricalEntries().get(from).length; counter++){
                    sum = sum + this.getEmpiricalEntries().get(from)[counter].getProbability();
                }
                if(sum < 1){
                    if(errors.isEmpty()){
                        errors.add("Sum of probabilities < 1.0 for following range(s):\n\"From\" : \""+from+"\"");
                    }else{
                        errors.add("\"From\" : \""+from+"\"");
                    }
                }else if(sum > 1){
                    for(int counter =0; counter < this.getEmpiricalEntries().get(from).length; counter++){
                        double newProb =  this.getEmpiricalEntries().get(from)[counter].getProbability()/sum;
                        this.getEmpiricalEntries().get(from)[counter].setProbability(newProb);
                    }
                    if(warnings.isEmpty()){
                        warnings.add("Probabilities > 1.0, normalised = 1.0 for range(s):\n\"From\" : \""+from+"\"");
                    }else{
                        warnings.add("\"From\" : \""+from+"\"");
                    }
                }
            }
        }
        errors.addAll(warnings);
        return errors;
    }

    /**
     * Removes the parameter station name from the entry list.
     * @param stationName
     */
    public void refreshRoutingOnStationDeletion(String stationName){
        LoadDependentRouting cloneRouting = (LoadDependentRouting)this.clone();
        this.values.clear();
        for(Iterator<Integer> itEntries = cloneRouting.values.keySet().iterator(); itEntries.hasNext();){
            Integer from = itEntries.next();
            EmpiricalEntry[] entries = cloneRouting.getEmpiricalEntryForFrom(from);
            for(EmpiricalEntry entry : entries){
                if(!entry.getStationName().equalsIgnoreCase(stationName)){
                    this.addEmricalEntryForFrom(from, entry.getStationName(), entry.getProbability());
                }
            }
        }
    }

    public void refreshRouting(Set<String> fstationNames){
        if(this.values != null && this.values.size() > 0){
            Integer[] froms = new Integer[this.values.keySet().size()];
            this.values.keySet().toArray(froms);
            for(Integer from : froms){
                List<EmpiricalEntry> newEntries = new ArrayList<EmpiricalEntry>(0);
                Set<String> oldFStationNames = new HashSet<String>(0);
                EmpiricalEntry[] entries = this.getEmpiricalEntryForFrom(from);
                for(EmpiricalEntry entry : entries){
                    if(fstationNames.contains(entry.stationName)){
                        newEntries.add(entry);
                    }
                    oldFStationNames.add(entry.getStationName());
                }
                for(String fstationName : fstationNames){
                    if(!oldFStationNames.contains(fstationName)){
                        EmpiricalEntry newEntry = new EmpiricalEntry();
                        newEntry.setStationName(fstationName);
                        newEntry.setProbability(0);
                        newEntries.add(newEntry);
                    }
                }
                if(this.deleteEmpricalEntryForFrom(from)){
                    for(EmpiricalEntry newEntry : newEntries){
                        this.addEmricalEntryForFrom(from, newEntry.getStationName(), newEntry.getProbability());
                    }
                }
            }
        }
    }

    /**
     * Returns true if the routing strategy is dependent from the state of
     * the model
     *
     * @return true if the routing strategy is dependent from the state of
     *         the model
     *         <p/>
     *         Author: Francesco D'Aquino
     */
    @Override
    public boolean isModelStateDependent() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public class EmpiricalEntry{


        public void setProbability(double probability) {
            this.probability = probability;
        }

        public double getProbability() {
            return probability;
        }


        public String getStationName() {
            return stationName;
        }

        public void setStationName(String stationName) {
            this.stationName = stationName;
        }

        private String stationName;
        private double probability;
    }


}
