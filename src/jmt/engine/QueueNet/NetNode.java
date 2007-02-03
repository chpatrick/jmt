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
  
package jmt.engine.QueueNet;

import jmt.engine.NodeSections.Queue;
import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;
import jmt.engine.simEngine.SimEntity;
import jmt.engine.simEngine.SimEvent;
import jmt.engine.simEngine.SimSystem;
import jmt.engine.simEngine.SimTypeP;

import java.util.ListIterator;


/**
 * This class implements a generic QueueNetwork node.
 * @author Francesco Radaelli, Stefano Omini, Marco Bertoli
 */
public class NetNode extends SimEntity {

    //these constants are used in exception messages

    /** Unable to broadcast a message */
	public static final int EXCEPTION_UNABLE_TO_BROADCAST = 0x0001;
	/** Required measure does not exist */
	public static final int EXCEPTION_MEASURE_DOES_NOT_EXIST = 0x0002;
	/** Input section has been already defined. */
	public static final int EXCEPTION_INPUT_SECTION_ALREADY_DEFINED = 0x0003;
	/** Service section has been already defined.  */
	public static final int EXCEPTION_SERVICE_SECTION_ALREADY_DEFINED = 0x0004;
	/** Output section has been already defined. */
	public static final int EXCEPTION_OUTPUT_SECTION_ALREADY_DEFINED = 0x0005;
    /** Required property is not available */
	public static final int EXCEPTION_PROPERTY_NOT_AVAILABLE = 0x0006;


    //ID of properties

    /** Property ID: number of jobs which arrived to this node */
	public static final int PROPERTY_ID_ARRIVED_JOBS = 0x0001;
	/** Property ID: number of jobs which left this node */
	public static final int PROPERTY_ID_LEFT_JOBS = 0x0002;
	/** Property ID: number of events */
	public static final int PROPERTY_ID_EVENTS = 0x0003;
	/** Property ID: number of jobs inside the node*/
	public static final int PROPERTY_ID_RESIDENT_JOBS = 0x0004;
    /** Property ID: residence time */
	public static final int PROPERTY_ID_RESIDENCE_TIME = 0x0005;
	/** Property ID: throughput */
	public static final int PROPERTY_ID_THROUGHPUT = 0x0006;
    private BlockingRegion region; // This is set only if this station is input station of a blocking region

	/**
     * The QueueNetwork which this NetNode belong to.
     */
	protected QueueNetwork Network;


	private JobInfoList jobsList;

	/** Input section of the NetNode.
     *
     */
	protected NodeSection inputSection;

	/** Service section of the NetNode.
     *
     */
	protected NodeSection serviceSection;

	/** Output section of the NetNode.
     *
     */
	protected NodeSection outputSection;

	private int eventsCounter;

	//DEK (Federico Granata)

	private SimEvent receiveBuffer;

    //temp variables to contain message and message event type
    private NetMessage message;
    private int eventType;

	private boolean stopped;

	private NodeList InputNodes;

	private NodeList OutputNodes;


	/** Creates a new instance of NetNode.
	 * @param name Name of the NetNode.
	 */
	public NetNode(String name) {
		super(name);
		InputNodes = new NodeList();
		OutputNodes = new NodeList();
		inputSection = serviceSection = outputSection = null;
		receiveBuffer = new SimEvent();
		stopped = false;
	}

	/** Adds a section to the node.
	 * @param  Section Reference to the section to be added.
	 * @throws jmt.common.exception.NetException if trying to add a section which has already been defined
	 */
	public void addSection(NodeSection Section) throws jmt.common.exception.NetException {
		switch (Section.getSectionID()) {
			case NodeSection.INPUT:
				if (inputSection == null)
					this.inputSection = Section;
				else
					throw new jmt.common.exception.NetException(this, EXCEPTION_INPUT_SECTION_ALREADY_DEFINED,
					        "input section has been already defined");
				break;
			case NodeSection.SERVICE:
				if (serviceSection == null)
					this.serviceSection = Section;
				else
					throw new jmt.common.exception.NetException(this, EXCEPTION_SERVICE_SECTION_ALREADY_DEFINED,
					        "service section has been already defined");
				break;
			case NodeSection.OUTPUT:
				if (outputSection == null)
					this.outputSection = Section;
				else
					throw new jmt.common.exception.NetException(this, EXCEPTION_OUTPUT_SECTION_ALREADY_DEFINED,
					        "output section has been already defined");
				break;
		}
		Section.setOwnerNode(this);
	}

	/** Connects the output of this netNode to the input of an another
	 *  netNode.
	 *  @param netNode netNode to be linked.
	 */
	public void connect(NetNode netNode) throws jmt.common.exception.NetException {
		OutputNodes.add(netNode);
		netNode.InputNodes.add(this);
	}


    //NEW
    //@author Stefano Omini
    /**
     * Gets the JobInfoList of this node.
     *
     */
    public JobInfoList getJobInfoList() {
        return jobsList;
    }
    //end NEW


    /** Gets input nodes.
	 * @return Input nodes linked to this node.
	 */
	public NodeList getInputNodes() {
		return InputNodes;
	}

	/** Gets output nodes.
	 * @return Output nodes linked to this node.
	 */
	public NodeList getOutputNodes() {
		return OutputNodes;
	}

	/** Gets an integer type property related to this node.
	 * @param Id Property identifier.
	 * @return Property value.
	 * @throws jmt.common.exception.NetException if the property is not available.
	 */
	public int getIntNodeProperty(int Id) throws jmt.common.exception.NetException {
		try {
			switch (Id) {
				case PROPERTY_ID_ARRIVED_JOBS:
					return jobsList.getJobsIn();
				case PROPERTY_ID_LEFT_JOBS:
					return jobsList.getJobsOut();
				case PROPERTY_ID_EVENTS:
					return eventsCounter;
				case PROPERTY_ID_RESIDENT_JOBS:
					return jobsList.size();
			}
		} catch (jmt.common.exception.NetException exc) {
			throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
			        "required property is not available.", exc);
		}
		throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
		        "required property is not available.");
	}

	/** Gets an integer type property of this node related to a specified
	 * job class.
	 * @param Id Property identifier.
	 * @param JobClass JobClass.
	 * @return Property value.
	 * @throws jmt.common.exception.NetException if the property is not available.
	 */
	public int getIntNodeProperty(int Id, JobClass JobClass) throws jmt.common.exception.NetException {
		try {
			switch (Id) {
				case PROPERTY_ID_ARRIVED_JOBS:
					return jobsList.getJobsInPerClass(JobClass);
				case PROPERTY_ID_LEFT_JOBS:
					return jobsList.getJobsOutPerClass(JobClass);
				case PROPERTY_ID_RESIDENT_JOBS:
					return jobsList.size(JobClass);
			}
		} catch (jmt.common.exception.NetException exc) {
			throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
			        "required property is not available.", exc);
		}
		throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
		        "required property is not available.");
	}

	/** Gets a double type property of this node.
	 * @param Id Property identifier.
	 * @return Property value.
	 * @throws jmt.common.exception.NetException if the property is not available.
	 */
	public double getDoubleNodeProperty(int Id) throws jmt.common.exception.NetException {
		try {
			switch (Id) {
				case PROPERTY_ID_RESIDENCE_TIME:
					return jobsList.getBusyTime() / jobsList.getJobsOut();
				case PROPERTY_ID_THROUGHPUT:
					return jobsList.getJobsOut() / NetSystem.getTime();
			}
		} catch (jmt.common.exception.NetException exc) {
			throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
			        "required property is not available.", exc);
		}
		throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
		        "required property is not available.");
	}

	/** Gets a double type property of this node related to a specified
	 * job class.
	 * @param Id Property identifier.
	 * @param JobClass JobClass.
	 * @return Property value.
	 * @throws jmt.common.exception.NetException if the property is not available.
	 */
	public double getDoubleNodeProperty(int Id, JobClass JobClass) throws jmt.common.exception.NetException {
		try {
			switch (Id) {
				case PROPERTY_ID_RESIDENCE_TIME:
					return jobsList.getBusyTimePerClass(JobClass) /
					        jobsList.getJobsOutPerClass(JobClass);
				case PROPERTY_ID_THROUGHPUT:
					return jobsList.getJobsOutPerClass(JobClass) /
					        NetSystem.getTime();
			}
		} catch (jmt.common.exception.NetException exc) {
			throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
			        "required property is not available.", exc);
		}
		throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
		        "required property is not available.");
	}


	/** Gets a generic object type property of this node.
	 * @param Id Property identifier.
	 * @return Property value.
	 * @throws jmt.common.exception.NetException if the property is not available.
	 */
	public Object getObjectNodeProperty(int Id) throws jmt.common.exception.NetException {
		switch (Id) {
            default:
				throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
				        "required property is not available.");
		}
	}

	/** Gets a generic object type property of this node related to a
	 * specified job class.
	 * @param Id Property identifier.
	 * @param JobClass JobClass.
	 * @return Property value.
	 * @throws jmt.common.exception.NetException if the property is not available.
	 */
	public Object getObjectNodeProperty(int Id, JobClass JobClass) throws jmt.common.exception.NetException {
		switch (Id) {
            default:
				throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
				        "required property is not available.");
		}
	}

	/** Gets a section of the node.
	 * @param Id Section identifier (constants defined in NodeSection).
	 * @return Node section.
	 * @throws jmt.common.exception.NetException EXCEPTION
	 */
	public NodeSection getSection(int Id) throws jmt.common.exception.NetException {
		switch (Id) {
			case NodeSection.INPUT:
				return inputSection;
			case NodeSection.SERVICE:
				return serviceSection;
			case NodeSection.OUTPUT:
				return outputSection;
			default:
				throw new jmt.common.exception.NetException(this, EXCEPTION_PROPERTY_NOT_AVAILABLE,
				        "required section is not available.");
		}
	}


	/** Gets the Network owner of this node.
	 * @return Value of property Network.
	 */
	public QueueNetwork getQueueNet() {
		return Network;
	}


    /** Analyzes a measure in the node.
	 * @param measureName name of the measure to be activated.
	 * @param jobClass Job class to be analyzed.
	 * @param measurement measure to be activated.
	 * @throws jmt.common.exception.NetException
	 */
    public void analyze(int measureName, JobClass jobClass, Measure measurement)
	        throws jmt.common.exception.NetException {
		switch (measureName) {
			case SimConstants.LIST_NUMBER_OF_JOBS:
                jobsList.analyzeQueueLength(jobClass, measurement);
                break;

            case SimConstants.LIST_RESIDENCE_TIME:
				//jobsList.analyzeResponseTime(jobClass, measurement);
                jobsList.analyzeResidenceTime(jobClass, measurement);
				break;
            //NEW Bertoli Marco
            case SimConstants.LIST_RESPONSE_TIME:
                jobsList.analyzeResponseTime(jobClass, measurement);
                break;
            //end NEW
            
            case SimConstants.LIST_DROP_RATE:
                jobsList.analyzeDropRate(jobClass, (InverseMeasure)measurement);
                break;

  			default:
				throw new jmt.common.exception.NetException(this, EXCEPTION_MEASURE_DOES_NOT_EXIST,
				        "required analyzer does not exist!");
		}
	}

	public boolean isRunning() {
		return (state != SimEntity.FINISHED);
	}

	/** Gets the list of the job classes of the owner queue network.
	 * @return Queue network job classes.
	 */
	public JobClassList getJobClasses() {
		return Network.getJobClasses();
	}

	/** Gets the first NetMessage in the queue which validates a specific
	 * predicate.
	 * @param message Reference to a NetMessage buffer to be filled with
	 * the message information
	 * @throws jmt.common.exception.NetException
	 */
	protected void receive(NetMessage message)
	        throws jmt.common.exception.NetException {

        //COMMENT
        //@author Stefano Omini

        //We want to set some properties of NetMessage parameter using the
        //information contained in a tag of the received SimEvent
        //Remember that:
        //a >> b means to "right-shift" of "b" positions the bits of "a"
        //a << b means to "left-shift" of "b" positions the bits of "a"
        //
        //int type is made of 32 bit (= 4 Bytes = 8 words)
        //
        //
        //EVENT_MASK        = 0x0000FFFF;    (as defined in NetEvent)
        //SOURCE_MASK       = 0xFF000000;    (as defined in NodeSection)
        //DESTINATION_MASK  = 0x00FF0000;    (as defined in NodeSection)
        //
        //SOURCE_SHIFT      = 24;            (as defined in NodeSection)
        //DESTINATION_SHIFT = 16;            (as defined in NodeSection)

        //receiveBuffer, that is the received SimEvent, has a tag with this structure:
        //      0xSSDDEEEE
        // where    SS -> 1 byte (=2 words) referring to source
        //          DD -> 1 byte (=2 words) referring to destination
        //          EEEE -> 2 bytes (=4 words) referring to event
        //This tag contains some useful informations.

        //To set event, sourceSection and destinationSection of the parameter NetMessage
        //some binary shifts have to be realized (corresponding bits must be recognized
        //in simEvent tag using the correct mask and then they must be shifted as required by the
        //definitions of these properties)

        //set event (int)


        //END COMMENT

        int section;
        message.setEvent(receiveBuffer.getTag() & NetEvent.EVENT_MASK);

        //set sourceSection and destinationSection (byte)

		section = receiveBuffer.getTag() & NodeSection.SOURCE_MASK;
		section >>= NodeSection.SOURCE_SHIFT;
		message.setSourceSection((byte) section);

		section = receiveBuffer.getTag() & NodeSection.DESTINATION_MASK;
		section >>= NodeSection.DESTINATION_SHIFT;
		message.setDestinationSection((byte) section);

        //set other properties

		message.setData(receiveBuffer.getData());
		message.setTime(receiveBuffer.eventTime());
		message.setSource(NetSystem.getNode(receiveBuffer.getSrc()));
		message.setDestination(NetSystem.getNode(receiveBuffer.getDest()));


        //Look if message is a job message and if job is arriving at this node (from the node
        //itself or from another node)
        if (message.getEvent() == NetEvent.EVENT_JOB) {
            //event job
            if (message.getSource() != this) {
                //external source
                Job job = message.getJob();
			    jobsList.add(new JobInfo(job));
            } else if ((message.getSourceSection() == NodeSection.OUTPUT)
		        && (message.getDestinationSection() == NodeSection.INPUT)) {
                //internal source
                Job job = message.getJob();
			    jobsList.add(new JobInfo(job));
            }
        }
	}

	/** This method implements the body of a NetNode.
	 **/
	public final void body() {
		message = new NetMessage();

        try {
			receiveBuffer = getEvbuf();

            //receive a new messege
			receive(message);
			eventsCounter++;

            eventType = message.getEvent();

            //if the deferred queue(where we put messages when the node is busy)
			//contains an abort event then poison the node.
			//TODO: da togliere e' meglio mettere qualcosa che faccia lo stesso sul sistema quando viene lanciato un evento di questo genere
			if (simWaiting(new SimTypeP(NetEvent.EVENT_ABORT)) > 0)
				poison();

            //process last event
            if (eventType == NetEvent.EVENT_KEEP_AWAKE) {
				poison();
			}

            //receive a stop event
            if (!stopped && eventType == NetEvent.EVENT_STOP) {
				stopped = true;
			}

            // if this node has been stopped sends automatically acks
			// to the node which sent job to this one.
			if (stopped) {
                if (eventType == NetEvent.EVENT_JOB)
					send(NetEvent.EVENT_ACK, message.getJob(), 0.0,
					        NodeSection.NO_ADDRESS,
					        message.getSourceSection(),
					        message.getSource());
			} else {
				dispatch(message);
            }
			simGetNext(SimSystem.SIM_ANY);

		} catch (jmt.common.exception.NetException Exc) {
			Exc.printStackTrace();
		}

	}

	/** This method should be overridden to implement a own dispatch.
	 *  This method implements the events dispacther of the NetNode; it
	 *  <b><u>should never</u></b> remains busy for a long time.
	 *  Remember to call the superclass dispatch method if you want to inherit
	 *  all the superclass handled events.
	 */
	protected void dispatch(NetMessage message) throws jmt.common.exception.NetException {
		int processed = NodeSection.MSG_NOT_PROCESSED;
		int section = NodeSection.NO_ADDRESS;
		NetNode source = message.getSource();
		byte sourceSection = message.getSourceSection();
		byte destinationSection = message.getDestinationSection();

        //message goes through this switch depending on the destination section
		//if the message is correctly processed by the appropriate node section
		//then the pocessed variable contains a value different from the initial
		//MSG_NOT_PROCESSED.
		switch (destinationSection) {

            case NodeSection.INPUT:
				section = NodeSection.INPUT;
				// checks jobs routing
				if (message.getEvent() == NetEvent.EVENT_JOB)
					if (source != this) {
                        // If this message is JOB and we are reference node for that job, signals it
                        // This is needed for global measures - Bertoli Marco
                        if (message.getEvent() == NetEvent.EVENT_JOB && message.getJob().getJobClass().getReferenceNodeName().equals(this.getName()))
                            this.getQueueNet().getJobInfoList().recycleJob(message.getJob());
                        if (sourceSection != NodeSection.OUTPUT) {
                            //the exterior source section can be the input
                            // section only in case of redirecting queue
                            NodeSection sourceSect = source.getSection(sourceSection);

                            //check if this job has been redirected, otherwise the message
                            //cannot be dispatched
                            boolean redirected =
                                    (sourceSect instanceof Queue) &&
                                    (((Queue) sourceSect).isRedirectionON());
                            if (!redirected) {
                                break;
                            }
                        }
					}
				if (inputSection != null)
					processed = inputSection.receive(message);
				break;

            case NodeSection.SERVICE:
				section = NodeSection.SERVICE;
				// checks jobs routing
				if (message.getEvent() == NetEvent.EVENT_JOB)
					if (source != this)
						break;
				if (serviceSection != null)
					processed = serviceSection.receive(message);
				break;

            case NodeSection.OUTPUT:
				section = NodeSection.OUTPUT;
				// checks jobs routing
				if (message.getEvent() == NetEvent.EVENT_JOB)
					if (source != this)
						break;
					else if (sourceSection == NodeSection.INPUT)
						break;
				if (outputSection != null)
					processed = outputSection.receive(message);
				break;

            default:
				;
		}

        if (processed == NodeSection.MSG_NOT_PROCESSED) {
        }

	}


	/** Sends a message to a NetNode.
	 * @param Event Event tag.
	 * @param Data  Data to be attached to the message.
	 * @param Delay Scheduling delay.
	 * @param SourceSection The source section.
	 * @param DestinationSection The destination section.
	 * @param Destination The destination node.
	 * @throws jmt.common.exception.NetException Exception
	 */
	void send(int Event, Object Data, double Delay, byte SourceSection,
	          byte DestinationSection, NetNode Destination)
	        throws jmt.common.exception.NetException {
		int Tag;
		//TODO: vedi analogo problema per receive
        //Look if message is a job message and if job is leaving this node

        if ((Event == NetEvent.EVENT_JOB) &&
                ((Destination != this) ||
		        ((Destination == this) && (SourceSection == NodeSection.OUTPUT) &&
		        (DestinationSection == NodeSection.INPUT)))
        ) {
			Job job = (Job) Data;
			JobInfo JobInfo = jobsList.lookFor(job);
			if (JobInfo != null)
                jobsList.remove(JobInfo);
		}


        //TODO: forse se il job è destinato al nodo stesso (circa come se fosse un multivisita)
        //non bisognerebbe eliminarlo.. e poi riaggiungerlo alla ricezione
        //NEW
        //@author Stefano Omini
        //Look if message is a job message and if job is leaving this node
		/*
        if ((Event == NetEvent.EVENT_JOB) && (Destination != this) ) {
			Job job = (Job) Data;
			JobInfo JobInfo = jobsList.lookFor(job);
			if (JobInfo != null)
                jobsList.remove(JobInfo);
        }
        */
        //end NEW





        //
        //EVENT_MASK        = 0x0000FFFF;
        //SOURCE_MASK       = 0xFF000000;
        //DESTINATION_MASK  = 0x00FF0000;
        //
        //SOURCE_SHIFT = 24;
        //DESTINATION_SHIFT = 16;
        //It's the opposite than receive(...): in this case we have event, source
        //and destination sections and we have to summarize them into a single tag
        //of a simEvent which will be scheduled by the simulator

        Tag = Event & NetEvent.EVENT_MASK;
		Tag += SourceSection << NodeSection.SOURCE_SHIFT;
		Tag += DestinationSection << NodeSection.DESTINATION_SHIFT;
		simSchedule(Destination.getId(), Delay, Tag, Data);
	}


    //TODO: non usato
    /** Sends a message to a section of all the NetNodes of the QueueNetwork.
	 * @param Event Event tag.
	 * @param Data  Data to be attached to the message.
	 * @param Delay Scheduling delay.
	 * @param SourceSection The source section.
	 * @param DestinationSection The destination section.
	 * @param NodeType Type of the node (reference or not: see constants in QueueNetwork).
	 * @throws jmt.common.exception.NetException Exception
	 */
	void sendBroadcast(int Event, Object Data, double Delay, byte SourceSection,
	                   byte DestinationSection, int NodeType)
	        throws jmt.common.exception.NetException {
		int Tag;
		ListIterator Iterator;
		if ((Event == NetEvent.EVENT_JOB) || (Event == NetEvent.EVENT_ACK))
			throw new jmt.common.exception.NetException(this, EXCEPTION_UNABLE_TO_BROADCAST, "message could not be broadcasted.");
		Tag = Event & NetEvent.EVENT_MASK;
		Tag += SourceSection << NodeSection.SOURCE_SHIFT;
		Tag += DestinationSection << NodeSection.DESTINATION_SHIFT;


		switch (NodeType) {
			case QueueNetwork.REFERENCE_NODE:
				Iterator = Network.getReferenceNodes().listIterator();
				while (Iterator.hasNext())
//					Entity.sim_schedule(((NetNode) Iterator.next()).Entity.get_id(), Delay, Tag, Data);
					simSchedule(((NetNode) Iterator.next()).getId(), Delay, Tag, Data);
				break;
			case QueueNetwork.NODE:
//@3G           Iterator=Network.getReferenceNodes().listIterator();
				Iterator = Network.getNodes().listIterator();
				while (Iterator.hasNext())
//					Entity.sim_schedule(((NetNode) Iterator.next()).Entity.get_id(), Delay, Tag, Data);
					simSchedule(((NetNode) Iterator.next()).getId(), Delay, Tag, Data);
				break;
		}
	}

	void setNetwork(QueueNetwork Network) {

		// Sets in this method queue network dependent properties
		this.Network = Network;
		// When a node is linked to a network, connections between nodes must
		// have been already done!!!
		jobsList = new JobInfoList(getJobClasses().size(), true);
	}


    public void start() {
		simSchedule(getId(), Double.MAX_VALUE, NetEvent.EVENT_KEEP_AWAKE, null);
		simGetNext(SimSystem.SIM_ANY);
	}

	/**
	 * Restarts the entity after an hold period: implements the method restart() of SimEntity.
	 */
	public void restart() {
        //TODO: è giusto che non faccia niente?
	}

	public void poison() {
		state = SimEntity.FINISHED;
	}



    //NEW
    //@author Stefano Omini

    /**
     * Redirects a message to a NetNode, without updating jobInfoList measures.
	 * @param Event Event tag.
	 * @param Data  Data to be attached to the message.
	 * @param Delay Scheduling delay.
	 * @param SourceSection The source section.
	 * @param DestinationSection The destination section.
	 * @param Destination The destination node.
	 * @throws jmt.common.exception.NetException Exception
	 */
	void redirect(int Event, Object Data, double Delay, byte SourceSection,
	          byte DestinationSection, NetNode Destination)
	        throws jmt.common.exception.NetException {
		int Tag;

        //Look if message is a job message and if job is leaving this node
		if ((Event == NetEvent.EVENT_JOB) && ((Destination != this) ||
		        ((Destination == this) && (SourceSection == NodeSection.OUTPUT) &&
		        (DestinationSection == NodeSection.INPUT)))) {
			Job job = (Job) Data;
			JobInfo JobInfo = jobsList.lookFor(job);
			if (JobInfo != null) {
                //removes the job without updating measures
                jobsList.removeAfterRedirect(JobInfo);
            }
		}

        Tag = Event & NetEvent.EVENT_MASK;
		Tag += SourceSection << NodeSection.SOURCE_SHIFT;
		Tag += DestinationSection << NodeSection.DESTINATION_SHIFT;

		simSchedule(Destination.getId(), Delay, Tag, Data);
	}



    /** Sends an "ack" message to a NetNode, to inform it that
     * the job previously sent has been dropped.
     * The dropped job is removed from the job info list.
     * <br>
     * The general "send" method cannot be used, because it updates
     * the job info list (by removing the job) only if the message contains
     * a job event (on the contrary, after dropping a job an ack event is sent!)
     *
	 * @param Event Event tag.
	 * @param Data  Data to be attached to the message.
	 * @param Delay Scheduling delay.
	 * @param SourceSection The source section.
	 * @param DestinationSection The destination section.
	 * @param Destination The destination node.
	 * @throws jmt.common.exception.NetException Exception
	 */
	void sendAckAfterDrop(int Event, Object Data, double Delay, byte SourceSection,
	          byte DestinationSection, NetNode Destination)
	        throws jmt.common.exception.NetException {

        int Tag;

        //Look if message is an ack message

        if ((Event == NetEvent.EVENT_ACK) &&
                ((Destination != this) || ((Destination == this) && (DestinationSection != SourceSection)))) {

			Job job = (Job) Data;
			JobInfo JobInfo = jobsList.lookFor(job);
			if (JobInfo != null) {
                //the job must be removed from the list but measures
                //shouldn't be updated, otherwise they would consider
                //this job in excess
                jobsList.removeAfterDrop(JobInfo);
            }

		}

        Tag = Event & NetEvent.EVENT_MASK;
		Tag += SourceSection << NodeSection.SOURCE_SHIFT;
		Tag += DestinationSection << NodeSection.DESTINATION_SHIFT;
		simSchedule(Destination.getId(), Delay, Tag, Data);
	}


    //end NEW

    /**
     * Returns true if this NetNode is a sink. This is extremely useful as sinks does not have
     * service secion and output section, causing a lot of null pointer exception in routing
     * strategies if not threated correctly.
     * Author: Bertoli Marco
     * @return true if this node doesn't have service section and output section
     */
    public boolean isSink() {
        // WARNING: to avoid circular dependancy and to speed up this method,
        // input section instanceof JobSink is not checked
        return (serviceSection == null && outputSection == null);
    }

    /**
     * @return true if this is a blocking region input station
     */
    public boolean isBlockingRegionInputStation() {
        return region != null;
    }

    /**
     * @param true if this is a blocking region input station
     */
    public void setBlockingRegionInputStation(BlockingRegion region) {
        this.region = region;
    }
    
    /**
     * @return blocking region, only if this is a region input station
     */
    public BlockingRegion getBlockingRegionInputStation() {
        return region;
    }
    
    
}