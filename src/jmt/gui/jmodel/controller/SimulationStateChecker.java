/**    
  * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

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
  
package jmt.gui.jmodel.controller;

import jmt.common.exception.NetException;
import jmt.engine.NodeSections.Queue;
import jmt.engine.NodeSections.Server;
import jmt.engine.QueueNet.*;
import jmt.engine.simDispatcher.Dispatcher_jSIMschema;
import jmt.gui.common.CommonConstants;
import jmt.gui.jmodel.JGraphMod.GraphicalQueueState;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;

/**
 * <p>Title: SimulationStateChecker </p>
 * <p>Description: this class is used to check the state of the model </p>
 *
 * @author Francesco D'Aquino
 *         Date: 7-nov-2005
 *         Time: 12.22.37
 */
public class SimulationStateChecker {
    private boolean initialized;

    private double maxJobCount;

    private Vector servers;
    private Vector classes;
    private Mediator mediator;
    private Dispatcher_jSIMschema dispatcher;
    //It implements a mapping between the graphical representation (the CommonModel Key) and the
    //represenatation at simulation engine level. The key of the HashMap is the Key of
    //graphical representation
    HashMap serverMap;
    //It implements a mapping between the graphical representation (the CommonModel Key) and the
    //represenatation at simulation engine level. The key of the HashMap is the Key of
    //graphical representation
    HashMap classMap;
    //Hashmap containing for each server (identified by the graphical Key) a
    //singleServerContentQueues HashMap
    ModelSnapshot serversContent;
    //Hashmap containing for each server (identified by the graphical Key) a
    //singleServerContentUtilizations HashMap
    ModelSnapshot serversUtilization;
    //HashMap containing for each class the number of resident jobs
    HashMap singleServerContentQueues;
    //HashMap containing for each class the value of utilization
    HashMap singleServerContentUtilizations;
    //used to draw the queue andutilization state
    GraphicalQueueState graphicalRepresentation;

    int iteration;

    public SimulationStateChecker(Mediator mediator, Dispatcher_jSIMschema dispatcher) {
        maxJobCount = Double.MIN_VALUE;
        initialized = false;
        this.mediator = mediator;
        this.dispatcher = dispatcher;
        serverMap = new HashMap(0);
        classMap = new HashMap(0);
        singleServerContentQueues = new HashMap(0);
        singleServerContentUtilizations = new HashMap(0);
        classes = mediator.getClassDefinition().getClassKeys();
        Vector stationsTemp = mediator.getStationDefinition().getStationKeysNoSourceSink();
        servers = new Vector(0,1);
        graphicalRepresentation = new GraphicalQueueState(mediator);

        for (int i=0;i<stationsTemp.size();i++) {
            if (mediator.getStationDefinition().getStationType(stationsTemp.get(i)).equals(CommonConstants.STATION_TYPE_SERVER))
            servers.add(stationsTemp.get(i));
        }
        serversContent = new ModelSnapshot(servers,classes);
        serversUtilization = new ModelSnapshot(servers,classes);
        iteration = 1;

    }

    /*public void renew(Mediator mediator, Dispatcher_jSIMschema dispatcher) {
        reset();
        this.mediator = mediator;
        this.dispatcher = dispatcher;
        serverMap = new HashMap(0);
        classMap = new HashMap(0);
        singleServerContentQueues = new HashMap(0);
        classes = mediator.getClassDefinition().getClassKeys();
        Vector stationsTemp = mediator.getStationDefinition().getStationKeysNoSourceSink();
        servers = new Vector(0,1);
        for (int i=0;i<stationsTemp.size();i++) {
            if (mediator.getStationDefinition().getStationType(stationsTemp.get(i)).equals(JSIMConstants.STATION_TYPE_SERVER))
            servers.add(stationsTemp.get(i));
        }
        serversContent = new ModelSnapshot(servers,classes);
    }*/

    private void reset() {
        maxJobCount = Double.MIN_VALUE;
        initialized = false;
        mediator = null;
        dispatcher = null;
        serverMap = null;
        classMap = null;
        singleServerContentQueues.clear();
        singleServerContentQueues = null;
        singleServerContentUtilizations.clear();
        singleServerContentUtilizations = null;
        classes.removeAllElements();
        classes = null;
        servers.removeAllElements();
        servers = null;
        serversContent.reset();
        serversContent = null;
        serversUtilization.reset();
        serversUtilization = null;
    }

    /**
     * Initializes the stationMap and classMap variable. It realizes a mapping between
     * the representation of classes and servers at CommonModel level (by a Key) and
     * the representation at engine level
     */
    public void initialize() {
        QueueNetwork net = dispatcher.getSimulation().getNetwork();
        NodeList nodeList = net.getNodes();
        JobClassList classList = net.getJobClasses();
        TreeMap tm = new TreeMap();
        //a TreeMap is used to speedup the following code
        for (int j=0; j<servers.size();j++) {
                    Object thisKey = servers.get(j);
                    String thisName = mediator.getStationDefinition().getStationName(thisKey);
                    tm.put(thisName,thisKey);
        }
        //performs the server mapping
        for (int i=0; i<nodeList.size();i++) {
            NetNode temp = nodeList.get(i);
            NodeSection serviceSection = null;
            try {
                serviceSection = temp.getSection(NodeSection.SERVICE);
            }
            catch (NetException ne){
            }
            if (serviceSection instanceof Server) {
                serverMap.put(tm.get(temp.getName()),temp);
            }
        }
        tm.clear();
        //performs the class mapping
        for (int j=0; j<classes.size();j++) {
                    Object thisKey = classes.get(j);
                    String thisName = mediator.getClassDefinition().getClassName(thisKey);
                    tm.put(thisName,thisKey);
        }
        for (int i=0; i<classList.size();i++) {
            JobClass temp = classList.get(i);
            classMap.put(tm.get(temp.getName()),temp);
        }
        //set the initialized state
        initialized = true;
    }

    /**
     * Gets the number of residence jobs in each station and fills the serversContent
     * HashMap
     */
    public void getModelState() {
        serversContent.clear();
        serversUtilization.clear();
        //maxJobCount = Double.MIN_VALUE;
        //for each server ...
        for (int i=0; i<servers.size(); i++) {
            Object thisServerKey = servers.get(i);
            // get the mapped NetNode ...
            NetNode thisNode = (NetNode)serverMap.get(thisServerKey);
            Queue thisQueue = null;
            Server thisServer = null;
            // get the input section and the service section ...
            try {
                thisQueue = (Queue)thisNode.getSection(NodeSection.INPUT);
                thisServer = (Server)thisNode.getSection(NodeSection.SERVICE);
            }
            catch (NetException ne){
            }
            singleServerContentQueues.clear();
            //for each class ...
            for (int j=0; j<classes.size(); j++) {
                try {
                    Object thisClassKey = classes.get(j);
                    if (thisQueue != null) {
                        // get the jobs of this class inside the queue
                        int jobsNumber = thisQueue.getIntSectionProperty(NodeSection.PROPERTY_ID_RESIDENT_JOBS,(JobClass)classMap.get(thisClassKey));
                        //if the job number is greater than the maximum update it
                        if (jobsNumber > maxJobCount)  maxJobCount = jobsNumber;
                        //put the number of jobs for this class inside the treemap
                        singleServerContentQueues.put(thisClassKey,new Integer(jobsNumber));
                    }
                    if (thisServer != null) {
                        //get the utilization for this class
                        double utilization = thisServer.getDoubleSectionProperty(NodeSection.PROPERTY_ID_UTILIZATION,(JobClass)classMap.get(thisClassKey));
                        //put the utilization this class inside the treemap
                        singleServerContentUtilizations.put(thisClassKey,new Double(utilization));
                    }
                }
                catch (NetException ne){
                }
            }
            //put the singleServerContentQueues inside the serversContent
            serversContent.put(thisServerKey,singleServerContentQueues.clone());
            //put the singleServerContentUtilizations inside the serversUtilization
            serversUtilization.put(thisServerKey,singleServerContentUtilizations.clone());
        }
        
        serversContent.setMaxValue(this.maxJobCount);
    }

    /**
     * Used only for test purpose
     */
    public void print() {
        System.out.println("Iteration: " + iteration);

        for (int i=0; i<servers.size(); i++) {
            Object thisServerKey = servers.get(i);
            String thisServerName = mediator.getStationDefinition().getStationName(thisServerKey);
            HashMap perServerMapQueues = (HashMap)this.serversContent.get(thisServerKey);
            HashMap perServerMapUtilizations = (HashMap)this.serversUtilization.get(thisServerKey);
            //System.out.println("***********************************************");
            System.out.println("    "+thisServerName + ":");
            System.out.println("        Queues state:");
            for (int j=0; j<classes.size();j++) {
                Object thisClassKey = classes.get(j);
                String thisClassName = mediator.getClassDefinition().getClassName(thisClassKey);
                String jobs = Integer.toString(((Integer)perServerMapQueues.get(thisClassKey)).intValue());
                System.out.println("                Jobs of " + thisClassName + " at " + thisServerName + ": " + jobs);
            }
            System.out.println("        Utilization state:");
            for (int j=0; j<classes.size();j++) {
                Object thisClassKey = classes.get(j);
                String thisClassName = mediator.getClassDefinition().getClassName(thisClassKey);
                String utilization = Float.toString(((Double)perServerMapUtilizations.get(thisClassKey)).floatValue());
                System.out.println("                Utilization of " + thisClassName + " at " + thisServerName + ": " + utilization);

            }
            System.out.println("        Degrees per class:");
            int start = 0;
            for (int j=0; j<classes.size();j++) {
                Object thisClassKey = classes.get(j);
                String thisClassName = mediator.getClassDefinition().getClassName(thisClassKey);
                int degrees = (int) (((Double)perServerMapUtilizations.get(thisClassKey)).floatValue()*360);
                System.out.println("                "+ thisClassName + " at " + thisServerName + ":    from " + start + "°" + " to " + (start+degrees) + "°");
                start+=degrees;
            }
        }
        System.out.println("\n    Max n° job: " + Integer.toString((int)this.maxJobCount));
        System.out.println("***********************************************\n");
        iteration++;
   }

    /**
     * Calls the draw method from  to draw queues and utilizations
     */
   public void forceDraw() {
       graphicalRepresentation.draw(serversContent,serversUtilization);
   }

    /**
     * Forces mediator to repaint the JGraph
     */
   public void forceRepaint() {
       mediator.graphRepaint();
   }

    /**
     * Check for the state of SimulationStateChecker
     * @return true if SimulationStateChecker has been initialized
     */
   public boolean isInitialized() {
        return initialized;
   }
}


