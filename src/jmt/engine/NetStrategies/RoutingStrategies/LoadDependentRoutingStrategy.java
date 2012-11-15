package jmt.engine.NetStrategies.RoutingStrategies;

import jmt.common.exception.NetException;
import jmt.engine.NetStrategies.RoutingStrategy;
import jmt.engine.QueueNet.*;
import jmt.engine.random.Empirical;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Ashanka
 * Date: 9/4/11
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class LoadDependentRoutingStrategy extends RoutingStrategy {
    /**
     * This method should be overridden to implement a specific strategy.
     *
     * @param Nodes    List of nodes.
     * @param jobClass class ofcurrent job to be routed
     * @return Selected node .
     */

    private LoadDependentRoutingParameter[] parameters;
    private Empirical distribution;

    public LoadDependentRoutingStrategy(LoadDependentRoutingParameter[] params){
        Arrays.sort(params);
        this.parameters = params;
    }
    @Override
    public NetNode getOutNode(NodeList Nodes, JobClass jobClass) {
        return Nodes.get(1);  //To change body of implemented methods use File | Settings | File Templates.
    }
    @Override
    public NetNode getOutNode(NodeSection section, JobClass jobClass){
        NetNode outNode = null;
        try {
            int jobs = section.getOwnerNode().getIntNodeProperty(NetNode.PROPERTY_ID_RESIDENT_JOBS,jobClass);
            int index = Arrays.binarySearch(parameters, new Integer(jobs));
            if (index < 0) {
                index = -index - 2;
            }
            outNode = parameters[index].getOutNode();
        }catch (NetException ne){

        }
        return outNode;
    }

}
