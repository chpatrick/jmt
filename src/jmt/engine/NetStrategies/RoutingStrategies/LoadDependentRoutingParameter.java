package jmt.engine.NetStrategies.RoutingStrategies;

import jmt.common.exception.IncorrectDistributionParameterException;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NetSystem;
import jmt.engine.random.Empirical;
import jmt.engine.random.EmpiricalEntry;
import jmt.engine.random.EmpiricalPar;

/**
 * Created by IntelliJ IDEA.
 * User: Ashanka
 * Date: 9/4/11
 * Time: 10:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadDependentRoutingParameter implements Comparable<Object>{
    private int from;
    private Empirical distribution;
	private EmpiricalPar param;
    private EmpiricalEntry[] empiricalEntries;
    private double[] probabilities;
    private String[] stationName;
    private NetNode[] nodes;

    public LoadDependentRoutingParameter(Integer from, EmpiricalEntry empiricals[]) throws IncorrectDistributionParameterException {
        this.from = from.intValue();
        this.empiricalEntries = empiricals;
        probabilities = new double[empiricals.length];
        stationName = new String[empiricals.length];

        for(int e=0; e<empiricals.length; e++){
            probabilities[e] = empiricals[e].getProbability();
            stationName[e] = (String)empiricals[e].getValue();
        }
        distribution = new Empirical();
        param = new EmpiricalPar(probabilities);
    }

    public NetNode getOutNode(){
        NetNode node = null;
        if(nodes == null || nodes.length ==0){
            nodes = new NetNode[stationName.length];
            for(int n=0; (stationName != null && n<stationName.length); n++){
                nodes[n] = NetSystem.getNode(stationName[n]);
            }
        }
        try{
            int nodePos = (int) distribution.nextRand(param);
            node = this.nodes[nodePos];
        }catch(IncorrectDistributionParameterException id){

        }
        return node;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * <p/>
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     * <p/>
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p/>
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     * <p/>
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     * <p/>
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    public int compareTo(Object o) {
        if(o instanceof LoadDependentRoutingParameter){
            return this.from - ((LoadDependentRoutingParameter)o).from;
        }else if(o instanceof Integer){
            return this.from - ((Integer)o).intValue();
        }else{
            throw new ClassCastException("Incorrect Class to compare");
        }
    }
}
