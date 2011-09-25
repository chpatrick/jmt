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
 
package jmt.engine.simDispatcher;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;

import javax.xml.parsers.*;

import jmt.framework.data.ArrayUtils;
import jmt.common.exception.SolverException;

import java.util.Arrays;
import java.io.File;
import java.io.FileReader;

/**
 * This class is a "stripped" copy of ExactModel used to check saturation before dispatching
 * @author Stefano Omini
 */
public class Dispatcher_Exact {

    public static final int STATION_DELAY = 0;
    public static final int STATION_LI = 1; //load independent
    public static final int STATION_LD = 2; //load dependent

    public static final int CLASS_CLOSED = 0;
    public static final int CLASS_OPEN = 1;

	//true if the model is closed
    private boolean closed;
	//true if the model is open
    private boolean open;
	//true if the model contains load dependent stations
    private boolean ld;

    //true if the model has been modified
    private boolean changed;
	//true if results are available
    private boolean hasResults;
	//true if the results are valid (no modify has been made in the model after results computation)
    private boolean resultsOK;
	//description of the model
    private String description;

    /***********************STATIONS AND CLASSES******************************/

	//number of service centers
	private int stations;
	//number of classes
	private int classes;
	//total population (computed as the sum of all closed class populations)
	private int maxpop;

	//class data is class population for closed classes, class arrival rate for open classes
	//dim: classData[classes]
	private double[] classData;
    //station names
	//dim: stationNames[stations]
	private String[] stationNames;
	//station types
	//dim: stationTypes[stations]
	private int[] stationTypes;
	//class names
	//dim: classNames[classes]
	private String[] classNames;
	//class types
	//dim: classTypes[classes]
	private int[] classTypes;

    /***********************SERVICE PARAMETERS**************************/

	/**
	 * visits to the service centers
	 * dim: visits[stations][classes]
	 */
	private double[][] visits;
    /**
	 * service times of the service centers
	 * dim: serviceTimes[stations][classes][p]
	 * p=maxpop     if stationTypes[s]==STATION_LD
	 * p=1          otherwise
	 */
	private double[][][] serviceTimes;

    /***********************RESULTS******************************/

	/**
	 * queue lengths
	 * dim: queueLen[stations][classes+1]
	 */
	private double[][] queueLen;

	/**
	 * throughput
	 * dim: throughput[stations+1][classes+1]
	 */
	private double[][] throughput;

	/**
	 * residence times
	 * dim: resTime[stations+1][classes+1]
	 */
	private double[][] resTimes;

	/**
	 * utilization
	 * dim: util[stations][classes+1]
	 */
	private double[][] util;


    private File modelFile;
    private String modelFilePath;
    private Document document;



    public Dispatcher_Exact(File modelFile){
        this.modelFile = modelFile;
        this.modelFilePath = modelFile.getAbsolutePath();
        try {
            init();
        } catch (SolverException se){
            se.printStackTrace();
        }
    }


    public Dispatcher_Exact(String modelPath){
        this.modelFilePath = modelPath;
        this.modelFile = new File (modelPath);
        try {
            init();
        } catch (SolverException se){
            se.printStackTrace();
        }

    }


    private void init() throws SolverException{

        try {
            DOMParser parser = new DOMParser();
            FileReader fr = new FileReader(modelFile);
            parser.parse(new InputSource(fr));
            document = parser.getDocument();

            if (!loadDocument(document)) {
				throw new SolverException("Error loading model from tempfile", null);
			}
		} catch (SAXException e) {
			throw new SolverException("XML parse error in tempfile", e);
		} catch (Exception e) {
			throw new SolverException("Error loading model from tempfile", e);
		}

    }






	/**
     * Clears all the results
     */
    public void discardResults() {
		hasResults = false;
		resultsOK = false;
		queueLen = null;
		throughput = null;
		resTimes = null;
		util = null;
		changed = true;
	}

	/**
	 * sets all the result data for this model.
	 * @throws IllegalArgumentException if any argument is null or not of the correct size
	 */
	public void setResults(double[][] queueLen, double[][] throughput, double[][] resTimes, double[][] util) {

        //OLD
        //int stp = stations + 1;
		//int clp = classes + 1;

        //NEW
        //@author Stefano Omini
        int stp = stations;
		int clp = classes;
        //end NEW

		if (queueLen == null || queueLen.length != stations || queueLen[0].length != clp) throw new IllegalArgumentException("queueLen must be non null and of size [stations+1][classes+1]");
		if (throughput == null || throughput.length != stp || throughput[0].length != clp) throw new IllegalArgumentException("throughput must be non null and of size [stations+1][classes+1]");
		if (resTimes == null || resTimes.length != stp || resTimes[0].length != clp) throw new IllegalArgumentException("resTimes must be non null and of size [stations+1][classes+1]");
		if (util == null || util.length != stations || util[0].length != clp) throw new IllegalArgumentException("util must be non null and of size [stations][classes+1]");
		//TODO: non controlla il numero di classi per tutte le stazioni, ma solo per la prima!!
        this.queueLen = ArrayUtils.copy2(queueLen);
		this.throughput = ArrayUtils.copy2(throughput);
		this.resTimes = ArrayUtils.copy2(resTimes);
		this.util = ArrayUtils.copy2(util);
		hasResults = true;
		resultsOK = true;
		changed = true;
	}



	/**
     * Gets the model description
     * @return the model description
     */
    public String getDescription() {
		return description;
	}

    /**
     * Sets the model description
     * @param description the model description
     */
	public void setDescription(String description) {
		if (!changed) if (description.equals(this.description)) return;
		this.description = description;
		changed = true;
	}

	/**
	 * @return true if this object describes a multiclass system
	 */
	public boolean isMultiClass() {
		return (classes > 1);
	}

	/**
	 * @return true if this object describes a closed system
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @return true if this object describes an open system
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * @return true if this object describes a mixed system
	 */
	public boolean isMixed() {
        //mixed = true only if closed = false and open = false
		return !(closed || open);
	}

	/**
	 * @return true if this object describes a system containing LD stations
	 */
	public boolean isLd() {
		return ld;
	}

	/**
	 * @return number of service centers
	 */
	public int getStations() {
		return stations;
	}

	/**
	 * @return number of classes
	 */
	public int getClasses() {
		return classes;
	}

	/**
	 * @return total population
	 */
	public int getMaxpop() {
		return maxpop;
	}

	/**
	 * @return names of the service centers
	 */
	public String[] getStationNames() {
		return stationNames;
	}

	/**
	 * sets the names of the service centers.
     * @param stationNames the names of the service centers
	 * @throws IllegalArgumentException if the array is not of the correct size
	 */
	public void setStationNames(String[] stationNames) {
		if (stationNames.length != stations) throw new IllegalArgumentException("stationNames.length!=stations");
		if (!changed) if (Arrays.equals(this.stationNames, stationNames)) return;
		this.stationNames = stationNames;
		changed = true;
	}

	/**
	 * @return names of the classes
	 */
	public String[] getClassNames() {
		return classNames;
	}

	/**
	 * sets the names of the classes.
     * @param classNames the names of the classes
	 * @throws IllegalArgumentException if the array is not of the correct size
	 */
	public void setClassNames(String[] classNames) {
		if (classNames.length != classes) throw new IllegalArgumentException("classNames.length!=classes");
		if (!changed) if (Arrays.equals(this.classNames, classNames)) return;
		this.classNames = classNames;
		changed = true;
	}

	/**
	 * @return data for the classes.
	 */
	public double[] getClassData() {
		return classData;
	}



	/**
	 * @return type of the classes
	 */
	public int[] getClassTypes() {
		return classTypes;
	}

	/**
	 * sets the type of the classes
     * @param classTypes the type of the classes
	 * @throws IllegalArgumentException if the array is not of the correct size
	 */
	public void setClassTypes(int[] classTypes) {
		if (classTypes.length != classes) throw new IllegalArgumentException("classTypes.length!=classes");
		if (!changed || resultsOK) if (Arrays.equals(this.classTypes, classTypes)) return;
		this.classTypes = classTypes;
		closed = calcClosed();
		open = calcOpen();
		changed = true;
		resultsOK = false;
	}

	/**
	 * @return type of the stations
	 */
	public int[] getStationTypes() {
		return stationTypes;
	}



	/**
	 * @return the matrix of visits
	 */
	public double[][] getVisits() {
		return visits;
	}

	/**
	 * sets the matrix of visits
     * @param visits the matrix of visits
	 * @throws IllegalArgumentException if the matrix is not of the correct size
	 */
	public void setVisits(double[][] visits) {
		if (visits.length != stations || visits[0].length != classes) throw new IllegalArgumentException("incorrect array dimension");
		if (!changed || resultsOK) if (ArrayUtils.equals2(this.visits, visits)) return;
		this.visits = visits;
		changed = true;
		resultsOK = false;
	}

	/**
	 * @return the matrix of service times
	 */
	public double[][][] getServiceTimes() {
		return serviceTimes;
	}

	/**
	 * sets the matrix of service times
     * @param serviceTimes the matrix of service times
	 * @throws IllegalArgumentException if the matrix is not of the correct size
	 */
	public void setServiceTimes(double[][][] serviceTimes) {
		if (serviceTimes.length != stations || serviceTimes[0].length != classes) throw new IllegalArgumentException("incorrect array dimension");
		if (!changed || resultsOK) if (ArrayUtils.equals3(this.serviceTimes, serviceTimes)) return;
		int currSize;
		double[][] subST;

		//validate sizes
		for (int s = 0; s < stations; s++) {
			currSize = (stationTypes[s] == STATION_LD ? maxpop : 1);
			//TODO: se stazione è LD ma non ci sono customers, max pop = 0
            if (currSize == 0) currSize = 1;
			subST = serviceTimes[s];
			for (int c = 0; c < classes; c++)
				if (subST[c].length != currSize) {
					throw new IllegalArgumentException("Wrong size for station " + stationNames[s]);
				}
		}

		this.serviceTimes = serviceTimes;
		changed = true;
		resultsOK = false;
	}

	/**
	 * Resizes the data structures according to specified parameters. Data is preserved as far as possible
	 */
	public void resize(int stations, int classes) {
		if (stations <= 0 || classes <= 0) throw new IllegalArgumentException("stations and classes must be >0");
		if (this.stations != stations || this.classes != classes) {
            //other cases already handled in setXXX methods
			discardResults();
		}
		this.stations = stations;
		this.classes = classes;

		stationNames = ArrayUtils.resize(stationNames, stations, null);
		stationTypes = ArrayUtils.resize(stationTypes, stations, STATION_LI);
		ld = calcLD();

		visits = ArrayUtils.resize2(visits, stations, classes, 1.0);

		classNames = ArrayUtils.resize(classNames, classes, null);
		classTypes = ArrayUtils.resize(classTypes, classes, CLASS_CLOSED);
		closed = calcClosed();


		classData = ArrayUtils.resize(classData, classes, 0.0);

		maxpop = calcMaxpop();

		serviceTimes = ArrayUtils.resize3var(serviceTimes, stations, classes, calcSizes(), 0.0);
	}

	/**
     * @return queue lengths
     */
    public double[][] getQueueLen() {
		return queueLen;
	}

    /**
     * @return response times
     */
        public double[][] getResTimes() {
		return resTimes;
	}

   	/**
     * @return throughputs
     */
	public double[][] getThroughput() {
		return throughput;
	}

	/**
     * @return utilizations
     */
    public double[][] getUtilization() {
		return util;
	}


	/**
	 * Removes all LD stations, converting them into LI stations
	 */
	public void removeLD() {
		for (int i = 0; i < stations; i++) {
			if (stationTypes[i] == STATION_LD) {
				stationTypes[i] = STATION_LI;

                //NEW
                //@author Stefano Omini
                //clear old LD service times
                serviceTimes[i] = new double[classes][1];
                for (int c = 0; c < classes; c++) {
                    serviceTimes[i][c][0] = 0.0;
                }
                //end NEW
			}
		}
		ld = false;
	}

    /**
     * @return true if the model contains only closed stations
     */
	private boolean calcClosed() {
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] != CLASS_CLOSED) {
				//make sure we stay in a consistent state
                removeLD();
                //TODO: rimuove le LD perchè il caso LD multiclasse non è gestibile
				return false;
			}
		}
		return true;
	}

    /**
     * @return true if the model contains only open stations
     */
	private boolean calcOpen() {
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] != CLASS_OPEN) {
				return false;
			}
		}
		return true;
	}

    /**
     * @return true if the model contains Load Dependent stations
     */
	private boolean calcLD() {
		for (int i = 0; i < stations; i++) {
			if (stationTypes[i] == STATION_LD) {
				return true;
			}
		}
		return false;
	}

    /**
     * @return the total population (sum of the customers of all closed class)
     */
	private int calcMaxpop() {
		/* sum all the closed classes' customers */
		int maxpop = 0;
		for (int i = 0; i < classes; i++) {
			if (classTypes[i] == CLASS_CLOSED)
				maxpop += classData[i];
		}
		return maxpop;
	}

    /**
     * @return the sizes of service times for each station (max pop for LD stations, 1 for LI stations)
     */
	private int[] calcSizes() {
		int mp = (maxpop > 0 ? maxpop : 1);
		int[] sizes = new int[stations];
		for (int s = 0; s < stations; s++) {
			sizes[s] = (stationTypes[s] == STATION_LD ? mp : 1);
		}
		return sizes;
	}








	/**
	 * Creates a DOM representation of this object
     * @return a DOM representation of this object
	 */
	public Document createDocument() {
		Document root;
		try {
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			root = dbf.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException pce) {
			throw new RuntimeException(pce);
		}

		/* model */
		Element modelElement = root.createElement("model");

        modelElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        modelElement.setAttribute("xsi:noNamespaceSchemaLocation", "JMTmodel.xsd");

        root.appendChild(modelElement);

		/* description */
		if (!description.equals("")) {
			Element descriptionElement = root.createElement("description");
			descriptionElement.appendChild(root.createCDATASection(description));
			modelElement.appendChild(descriptionElement);
		}

		/* parameters */
		Element parametersElement = root.createElement("parameters");
		modelElement.appendChild(parametersElement);
		
		/* classes */
		Element classes_element = root.createElement("classes");
		parametersElement.appendChild(classes_element);
		classes_element.setAttribute("number", Integer.toString(classes));
		for (int i = 0; i < classes; i++){

            //NEW
            //@author Stefano Omini
            //TODO: controllare se va
            classes_element.appendChild(makeClassElement(root, i));
            //end NEW
		}

		/* stations */
		Element stationsElement = root.createElement("stations");
		parametersElement.appendChild(stationsElement);
		stationsElement.setAttribute("number", Integer.toString(stations));
		for(int i = 0; i < stations; i++){
            stationsElement.appendChild(makeStationElement(root, i));
		}


		//OLD
        //if (hasResults) modelElement.appendChild(makeSolutionElement(root));

        //NEW
        //@author Stefano Omini
        if (hasResults && resultsOK) modelElement.appendChild(makeSolutionElement(root));
        //end NEW
        
		return root;
		
		
	}

	private Element makeClassElement(Document root, int classNum) {
		Element classElement = null;
		if (classTypes[classNum] == CLASS_CLOSED) {
			classElement = root.createElement("closedclass");
			classElement.setAttribute("population", Integer.toString((int) classData[classNum]));
			classElement.setAttribute("name", classNames[classNum]);

		} else {
			classElement = root.createElement("openclass");
			classElement.setAttribute("rate", Double.toString(classData[classNum]));
			classElement.setAttribute("name", classNames[classNum]);

		}
		return classElement;
	}



    private Element makeStationElement(Document root, int stationNum) {

        Element station_element = null;
        Node servicetimes_element;
        Node visits_element;

        switch (this.stationTypes[stationNum]) {

            case STATION_LI:

                station_element = root.createElement("listation");
		        station_element.setAttribute("name", this.stationNames[stationNum]);

                /* create the section for service times */
                servicetimes_element = station_element.appendChild(root.createElement("servicetimes"));
                station_element.appendChild(servicetimes_element);

                /* create the section for visits */
                visits_element = station_element.appendChild(root.createElement("visits"));
                station_element.appendChild(visits_element);

                /* for each customer class */
                for (int j = 0; j < classes; j++) {
                    String class_name = this.classNames[j];
                    /* set service time */
                    Element st_element = root.createElement("servicetime");
                    st_element.setAttribute("customerclass", class_name);;
                    st_element.appendChild(root.createTextNode(Double.toString(this.serviceTimes[stationNum][j][0])));
                    servicetimes_element.appendChild(st_element);
                    /* set visit */
                    Element visit_element = root.createElement("visit");
                    visit_element.setAttribute("customerclass", class_name);
                    visit_element.appendChild(root.createTextNode(Double.toString(this.visits[stationNum][j])));
                    visits_element.appendChild(visit_element);
                }

                break;

            case STATION_DELAY:    //TODO: è uguale al caso Li ad eccezione del nome (forse si può semplificare)

                station_element = root.createElement("delaystation");
		        station_element.setAttribute("name", this.stationNames[stationNum]);

                /* create the section for service times */
                servicetimes_element = station_element.appendChild(root.createElement("servicetimes"));
                station_element.appendChild(servicetimes_element);

                /* create the section for visits */
                visits_element = station_element.appendChild(root.createElement("visits"));
                station_element.appendChild(visits_element);

                /* for each customer class */
                for (int j = 0; j < classes; j++) {
                    String class_name = this.classNames[j];
                    /* set service time */
                    Element st_element = root.createElement("servicetime");
                    st_element.setAttribute("customerclass", class_name);;
                    st_element.appendChild(root.createTextNode(Double.toString(this.serviceTimes[stationNum][j][0])));
                    servicetimes_element.appendChild(st_element);
                    /* set visit */
                    Element visit_element = root.createElement("visit");
                    visit_element.setAttribute("customerclass", class_name);
                    visit_element.appendChild(root.createTextNode(Double.toString(this.visits[stationNum][j])));
                    visits_element.appendChild(visit_element);
                }

                break;

            case STATION_LD:

                station_element = root.createElement("ldstation");
		        station_element.setAttribute("name", this.stationNames[stationNum]);

                /* create the section for service times */
                servicetimes_element = station_element.appendChild(root.createElement("servicetimes"));
                station_element.appendChild(servicetimes_element);

                /* create the section for visits */
                visits_element = station_element.appendChild(root.createElement("visits"));
                station_element.appendChild(visits_element);

                /* for each customer class */
                for (int j = 0; j < classes; j++) {
                    String class_name = this.classNames[j];
                    /* set service times, one for each population (values are CSV formatted) */
                    Element st_element = root.createElement("servicetimes");
                    st_element.setAttribute("customerclass", class_name);;
                    //TODO: questa parte va cambiata, devo avere una stringa csv con tutti i serv times (tranne l'elem 0 che è nullo)

                    String serv_t = ArrayUtils.toCSV(serviceTimes[stationNum][j]);

                    st_element.appendChild(root.createTextNode(serv_t));

                    servicetimes_element.appendChild(st_element);
                    /* set visit */
                    Element visit_element = root.createElement("visit");
                    visit_element.setAttribute("customerclass", class_name);
                    visit_element.appendChild(root.createTextNode(Double.toString(this.visits[stationNum][j])));
                    visits_element.appendChild(visit_element);
                }

                break;

            default:
                station_element = null;
        }//end switch

		return station_element;
	}


    private Element makeSolutionElement(Document root) {
		//TODO: non sono compresi i risultati aggregati (quelli che prendo sono giusti?? gli aggregati sono gli ultimi??)
        Element result_element = root.createElement("solutions");
		result_element.setAttribute("ok","true");
        result_element.setAttribute("solutionMethod", "analytical");
		for(int i = 0; i < stations; i++){
			Element stationresults_element = (Element) result_element.appendChild(root.createElement("stationresults"));
			stationresults_element.setAttribute("station", this.stationNames[i]);
			for(int j = 0; j < classes; j++){
				Element classesresults_element = (Element) stationresults_element.appendChild(root.createElement("classresults"));
				classesresults_element.setAttribute("customerclass", classNames[j]);



                Element Q_element = (Element) classesresults_element.appendChild(root.createElement("measure"));
                Q_element.setAttribute("measureType", "Queue length");
                Q_element.setAttribute("successful", "true");
                Q_element.setAttribute("meanValue", Double.toString(this.queueLen[i][j]));

                Element X_element = (Element) classesresults_element.appendChild(root.createElement("measure"));
				X_element.setAttribute("measureType", "Throughput");
                X_element.setAttribute("successful", "true");
                X_element.setAttribute("meanValue", Double.toString(this.throughput[i][j]));

				Element R_element = (Element) classesresults_element.appendChild(root.createElement("measure"));
				R_element.setAttribute("measureType", "Response time");
                R_element.setAttribute("successful", "true");
                R_element.setAttribute("meanValue", Double.toString(this.resTimes[i][j]));

                Element U_element = (Element) classesresults_element.appendChild(root.createElement("measure"));
				U_element.setAttribute("measureType", "Utilization");
                U_element.setAttribute("successful", "true");
                U_element.setAttribute("meanValue", Double.toString(this.util[i][j]));
			}
		}
		return result_element;
	}

	private void appendMatrixCSV(Document root, Element base, double[][] arr, String outer, String inner) {
		//TODO: forse devo usare questo per trattare anche il caso LD
        Element elems, elem;
		int n = arr.length;
		elems = root.createElement(outer);
		base.appendChild(elems);
		for (int i = 0; i < n; i++) {
			elem = root.createElement(inner);
			//TODO: separa i diversi elementi dell'array con ";"
            elem.appendChild(root.createTextNode(ArrayUtils.toCSV(arr[i])));
			elems.appendChild(elem);
		}
	}

	/**
	 * load the state of this object from the Document.
	 * @return true if the operation was successful.
     * WARNING: If the operation fails the object is left in an incorrect state and should be discarded.
	 */
	public boolean loadDocument(Document doc) {

		Node classNode = doc.getElementsByTagName("classes").item(0);
		Node stationNode = doc.getElementsByTagName("stations").item(0);

		NodeList descList = doc.getElementsByTagName("description");
		NodeList solList = doc.getElementsByTagName("solutions");

        //load description
		if (descList.item(0) != null) {
			if (!loadDescription((Element) descList.item(0))) {
                //description loading failed!
                return false;
            }
		} else {
			description = "";
		}

        //NEW
        //@author Stefano Omini

        //load classes
        if (classNode != null) {
			if (!loadClasses(classNode)) {
                //classes loading failed!
                return false;
            }
		}

        //load stations
        if (stationNode != null) {
			if (!loadStations(stationNode)) {
                //stations loading failed!
                return false;
            }
		}

        //end NEW

        //load solution
        if (solList.item(0) != null) {

            if (!loadSolution((Element) solList.item(0))) return false;
			hasResults = true;

		}

		// compute flags
		resize(stations, classes);
		changed = false;
		return true;
	}



	public boolean loadDescription(Element desc) {
		description = desc.getFirstChild().getNodeValue();
		return true;
	}


    //NEW
    //@author Stefano Omini
    public boolean loadClasses(Node classNode) {

        classes = Integer.parseInt(((Element) classNode).getAttribute("number"));

		classNames = new String[classes];
		classTypes = new int[classes];
		classData = new double[classes];

        NodeList classList = classNode.getChildNodes();

		int classNum = 0;

		maxpop = 0;
		Node n;
		Element current;
		closed = true;
		open = true;

		/* classes */
		for (int i = 0; i < classList.getLength(); i++) {
			n = classList.item(i);
			if (!(n instanceof Element)) continue;
			current = (Element) n;
			classNames[classNum] = current.getAttribute("name");
			if (current.getTagName().equals("closedclass")) {
				classTypes[classNum] = CLASS_CLOSED;
				classData[classNum] = Double.parseDouble(current.getAttribute("population"));
				maxpop += (int) classData[classNum];
				open = false;
			} else {
				classTypes[classNum] = CLASS_OPEN;
				classData[classNum] = Double.parseDouble(current.getAttribute("rate"));
				closed = false;
			}
			classNum++;
		}

		return true;
	}
    //end NEW


    //NEW
    //@author Stefano Omini
    public boolean loadStations(Node stationNode) {

        stations = Integer.parseInt(((Element) stationNode).getAttribute("number"));

		stationNames = new String[stations];
		stationTypes = new int[stations];
		visits = new double[stations][];
		serviceTimes = new double[stations][][];

        NodeList stationList = stationNode.getChildNodes();

		ld = false;

		String statType;
		NodeList sTimes;
		int stationNum = 0;

		/* stations */

        Node n;
		Element current;

		for (int i = 0; i < stationList.getLength(); i++) {
			n = stationList.item(i);
			if (!(n instanceof Element)) continue;
			current = (Element) n;
			statType = current.getTagName();
			stationNames[stationNum] = current.getAttribute("name");

			/* make arrays */

			visits[stationNum] = new double[classes];
			serviceTimes[stationNum] = new double[classes][];

			/* station types and service times */

			if (statType.equals("ldstation")) {
                //LD
				ld = true;
				if (maxpop == 0) {
					System.err.println("LD station with zero customers");
					return false;
				}
				stationTypes[stationNum] = STATION_LD;

				/* create arrays */
				for (int k = 0; k < classes; k++) {
					//TODO: maxpop o maxpop+1 ??????
                    //serviceTimes[stationNum] = new double[classes][maxpop + 1];
                    serviceTimes[stationNum] = new double[classes][maxpop];
				}

                //Element sTimesElem = (Element) current.getElementsByTagName("servicetimes").item(0);
                //TODO: non funziona
                Element sTimesElem = (Element) current.getElementsByTagName("servicetimes").item(0);
                sTimes = sTimesElem.getElementsByTagName("servicetimes");

                if (sTimes.getLength() != classes) {
                    System.err.println("Wrong number of service times sets for LD station " + stationNames[stationNum]);
                    return false;
                }


                Element visitsElem = (Element) current.getElementsByTagName("visits").item(0);
                NodeList visitsNodeList = visitsElem.getElementsByTagName("visit");

				for (int k = 0; k < classes; k++) {
                    String visit = (visitsNodeList.item(k).getFirstChild()).getNodeValue();
                    visits[stationNum][k] = Double.parseDouble(visit);

                    //string of LD service times for class k
                    Element class_st = (Element) sTimes.item(k);
                    String stimes = class_st.getFirstChild().getNodeValue();

                    double[] servt_arr = new double[maxpop];
                    ArrayUtils.fromCSV(servt_arr, stimes);

                    for (int p = 0; p < maxpop; p++) {
                        serviceTimes[stationNum][k][p] = servt_arr[p];
                    }
				}
			} else { //LI or delay
				if (statType.equals("delaystation")) {
					stationTypes[stationNum] = STATION_DELAY;
				} else {
					stationTypes[stationNum] = STATION_LI;
				}

				/* create arrays */

                sTimes = current.getElementsByTagName("servicetime");
                NodeList visitsNodeList = current.getElementsByTagName("visit");

                serviceTimes[stationNum] = new double[classes][1];
				visits[stationNum] = new double[classes];
				for (int k = 0; k < classes; k++) {

                    Node node = sTimes.item(k).getFirstChild();
                    String nodeValue = (node).getNodeValue();
                    serviceTimes[stationNum][k][0] = Double.parseDouble(nodeValue);
                    visits[stationNum][k] = Double.parseDouble((visitsNodeList.item(k).getFirstChild()).getNodeValue());
				}
			}
			stationNum++;
		}

		return true;
	}
    //end NEW





	public boolean loadSolution(Element sol) {

		String status = sol.getAttribute("ok");
		resultsOK = (status.equals("true") ? true : false);

        queueLen = loadResultsMatrix(sol, stations, classes, "Queue length");
        if (queueLen == null) return false;
		throughput = loadResultsMatrix(sol, stations, classes, "Throughput");
		if (throughput == null) return false;
		resTimes = loadResultsMatrix(sol, stations, classes, "Response time");
		if (resTimes == null) return false;
		util = loadResultsMatrix(sol, stations, classes, "Utilization");
		if (util == null) return false;

		return true;
	}


    //NEW
    //@author Stefano Omini
    //TODO: nuovo schema JMTmodel.xsd
    public double[][] loadResultsMatrix(Element base, int len1, int len2, String res) {

        //matrix of results
        double[][] arr = new double[len1][len2];

        if (base.getElementsByTagName("stationresults").getLength() != len1) return null;

        for (int i = 0; i < len1; i++) {
            Element s_res = (Element) base.getElementsByTagName("stationresults").item(i);
            for (int c = 0; c < len2; c++) {
                Element n_cls = (Element) s_res.getElementsByTagName("classresults").item(c);

                NodeList measure_list = n_cls.getElementsByTagName("measure");
                Element measure;
                String value = null;

                for (int m = 0; m < measure_list.getLength(); m++) {
                    measure = (Element) measure_list.item(m);
                    if (measure.getAttribute("measureType").equalsIgnoreCase(res)) {
                        //it's the measure we are searching for
                        value = measure.getAttribute("meanValue");
                        break;
                    }
                }

                //Element r = (Element) n_cls.getElementsByTagName(res).item(0);
                //String value = r.getFirstChild().getNodeValue();

                if (value != null) {
                    arr[i][c] = Double.parseDouble(value);
                } else {
                    arr[i][c] = 0.0;
                }

            }
		}
		return arr;
	}
    //end NEW


    public boolean hasSufficientProcessingCapacity(){

        double u;

        for (int s = 0; s < stations; s++) {
            if (stationTypes[s] == STATION_DELAY) {
                continue;
            }
            u = 0;
            for (int c = 0; c < classes; c++) {
                if (classTypes[c] == CLASS_OPEN) {
                    u += classData[c]*visits[s][c]*serviceTimes[s][c][0];
                }
            }
            if (u >= 1) {
                return false;
            }

        }

        return true;

    }





}
