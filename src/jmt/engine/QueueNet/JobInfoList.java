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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;

/** This class implements a job info list.
 * @author Francesco Radaelli, Stefano Omini.
 */
public class JobInfoList {

	private static final boolean DEBUG = false;

	/** Required property is not available*/
	public final int PROPERTY_NOT_AVAILABLE = 0x0001;

	//contain JobInfo objects
	private LinkedList List, ListPerClass[];

	//arrivals and completions
	private int JobsIn, JobsOut, JobsInPerClass[], JobsOutPerClass[];

	private double BusyTime, BusyTimePerClass[];

	private double LastJobOutTime, LastJobInTime, LastJobDropTime, LastJobOutTimePerClass[], LastJobInTimePerClass[], LastJobDropTimePerClass[];

	private Measure Utilization, UtilizationPerClass[], ResponseTime, ResponseTimePerClass[], ResidenceTime, ResidenceTimePerClass[], QueueLength,
			QueueLengthPerClass[], DropRate, DropRatePerClass[];
	//OLD
	//private Measure Throughput, ThroughputPerClass[];
	private InverseMeasure Throughput, ThroughputPerClass[];

	/** Creates a new JobInfoList instance.
	* @param NumberOfJobClasses number of job classes.
	* @param Save True to create and use a list to add/remove
	* each job which arrives/departes, false otherwise.
	*/
	public JobInfoList(int NumberOfJobClasses, boolean Save) {
		int i;
		if (Save) {
			List = new LinkedList();
			ListPerClass = new LinkedList[NumberOfJobClasses];
			for (i = 0; i < NumberOfJobClasses; i++) {
				ListPerClass[i] = new LinkedList();
			}
		}

		JobsIn = 0;
		JobsInPerClass = new int[NumberOfJobClasses];
		for (i = 0; i < NumberOfJobClasses; i++) {
			JobsInPerClass[i] = 0;
		}

		JobsOut = 0;
		JobsOutPerClass = new int[NumberOfJobClasses];
		for (i = 0; i < NumberOfJobClasses; i++) {
			JobsOutPerClass[i] = 0;
		}

		BusyTimePerClass = new double[NumberOfJobClasses];
		for (i = 0; i < NumberOfJobClasses; i++) {
			BusyTimePerClass[i] = 0;
		}

		LastJobOutTime = LastJobInTime = LastJobDropTime = 0.0;
		LastJobInTimePerClass = new double[NumberOfJobClasses];
		for (i = 0; i < NumberOfJobClasses; i++) {
			LastJobInTimePerClass[i] = 0;
		}

		LastJobOutTimePerClass = new double[NumberOfJobClasses];
		for (i = 0; i < NumberOfJobClasses; i++) {
			LastJobOutTimePerClass[i] = 0;
		}

		ThroughputPerClass = new InverseMeasure[NumberOfJobClasses];

		LastJobDropTimePerClass = new double[NumberOfJobClasses];
		Arrays.fill(LastJobDropTimePerClass, 0.0);

	}

	/**---------------------------------------------------------------------
	 *-------------------- "GET" METHODS -----------------------------------
	 *---------------------------------------------------------------------*/

	/** Gets list size.
	 * @return Number of job info object in the list.
	 * @throws jmt.common.exception.NetException
	 */
	public int size() throws jmt.common.exception.NetException {
		if (List != null) {
			return List.size();
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/**
	 * Returns the number of jobs of a specific job class in the list.
	 * @param JobClass Job class to look for.
	 * @return Number of jobs of a specified job class.
	 */
	public int size(JobClass JobClass) throws jmt.common.exception.NetException {
		if (ListPerClass != null) {
			return ListPerClass[JobClass.getId()].size();
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/** Gets the number of jobs added to the list.
	 * @return Arrived Jobs.
	 */
	public int getJobsIn() {
		return JobsIn;
	}

	/** Gets the number of jobs of a specific job class added to the list.
	 * @param JobClass Job class to look for.
	 * @return Arrived jobs of a specific job class.
	 */
	public int getJobsInPerClass(JobClass JobClass) {
		return JobsInPerClass[JobClass.getId()];
	}

	/** Gets the number of jobs added to the list for each job class.
	 * @return Arrived jobs for each job class.
	 */
	public int[] getJobsInPerClass() {
		return JobsInPerClass;
	}

	/** Gets the number of jobs removed from the list.
	 * @return Departed Jobs.
	 */
	public int getJobsOut() {
		return JobsOut;
	}

	/** Gets the number of jobs of a specific job class removed from the list.
	 * @param JobClass Job class to look for.
	 * @return Departed jobs of a specific job class.
	 */
	public int getJobsOutPerClass(JobClass JobClass) {
		return JobsOutPerClass[JobClass.getId()];
	}

	/** Gets the number of jobs added to the list for each job class.
	 * @return Arrived jobs per each job class.
	 */
	public int[] getJobsOutPerClass() {
		return JobsOutPerClass;
	}

	/** Gets busy time.
	 * @return Busy time.
	 * @throws jmt.common.exception.NetException
	 */
	public double getBusyTime() throws jmt.common.exception.NetException {
		if (List != null) {
			return BusyTime;
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/** Gets busy time for job class.
	 * @return Busy time for job class.
	 * @throws jmt.common.exception.NetException
	 */
	public double getBusyTimePerClass(JobClass JobClass) throws jmt.common.exception.NetException {
		if (ListPerClass != null) {
			return BusyTimePerClass[JobClass.getId()];
		} else {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
	}

	/** Gets time of the last job arrived.
	 * @return Time of last job arrived.
	 */
	public double getLastJobInTime() {
		return LastJobInTime;
	}

	/** Gets time of the last job arrived of a specified job class.
	 * @return Time of last job arrived of a specified job class.
	 */
	public double getLastJobInTimePerClass(JobClass JobClass) {
		return LastJobInTimePerClass[JobClass.getId()];
	}

	/** Gets time of the last job was dropped.
	 * @return Time of last job departed.
	 */
	public double getLastJobOutTime() {
		return LastJobOutTime;
	}

	/** Gets time of the last job was dropped.
	 * @return Time of last job departed.
	 */
	public double getLastJobDropTime() {
		return LastJobDropTime;
	}

	/** Gets time of the last job departed of a specified job class.
	 * @return Time of last job departed of a specified job class.
	 */
	public double getLastJobOutTimePerClass(JobClass JobClass) {
		return LastJobOutTimePerClass[JobClass.getId()];
	}

	/** Gets time of the last job dropped of a specified job class.
	 * @return Time of last job dropped of a specified job class.
	 */
	public double getLastJobDropTimePerClass(JobClass JobClass) {
		return LastJobDropTimePerClass[JobClass.getId()];
	}

	/** Gets time of the last modify of the list.
	 * @return Time of last modify of the list.
	 */
	public double getLastModifyTime() {
		if (LastJobOutTime >= LastJobInTime && LastJobOutTime >= LastJobDropTime) {
			return LastJobOutTime;
		} else if (LastJobInTime >= LastJobOutTime && LastJobInTime >= LastJobDropTime) {
			return LastJobInTime;
		} else {
			return LastJobDropTime;
		}
	}

	/** Gets time of the last modify of the list for a specified job class.
	 * @return Time of the last modify of the list for a specified job class.
	 */
	public double getLastModifyTimePerClass(JobClass JobClass) {
		if (LastJobOutTimePerClass[JobClass.getId()] >= LastJobInTimePerClass[JobClass.getId()]
				&& LastJobOutTimePerClass[JobClass.getId()] >= LastJobDropTimePerClass[JobClass.getId()]) {
			return LastJobOutTimePerClass[JobClass.getId()];
		} else if (LastJobInTimePerClass[JobClass.getId()] >= LastJobOutTimePerClass[JobClass.getId()]
				&& LastJobInTimePerClass[JobClass.getId()] >= LastJobDropTimePerClass[JobClass.getId()]) {
			return LastJobInTimePerClass[JobClass.getId()];
		} else {
			return LastJobDropTimePerClass[JobClass.getId()];
		}
	}

	/** Looks for an information job object which references to a specific job.
	 * @param Job The specified job.
	 * @return JobInfo object which references to the specified job, null otherwise.
	 */
	public JobInfo lookFor(Job Job) throws jmt.common.exception.NetException {
		if (ListPerClass == null) {
			throw new jmt.common.exception.NetException(this, PROPERTY_NOT_AVAILABLE, "property not available");
		}
		ListIterator Iterator;
		//creates an iterator for the job class list of the job class of the specified job
		Iterator = ListPerClass[Job.getJobClass().getId()].listIterator();
		JobInfo jobInfo;
		while (Iterator.hasNext()) {
			jobInfo = (JobInfo) Iterator.next();
			if (jobInfo.getJob() == Job) {
				return jobInfo;
			}
		}
		return null;
	}

	/**
	 * Gets the job info list
	 */
	public List getJobList() {
		return List;
	}

	/**---------------------------------------------------------------------
	 *-------------------- "ADD" AND "REMOVE" METHODS ----------------------
	 *---------------------------------------------------------------------*/

	/** Adds a new job info to the list.
	 * @param jobInfo Reference to the job info to be added.
	 * @return True if the job has been added (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean add(JobInfo jobInfo) {
		if (List != null) {
			updateAdd(jobInfo);
			ListPerClass[jobInfo.getJob().getJobClass().getId()].add(jobInfo);
			List.add(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/** Adds a new job info to the top of the list.
	 * @param jobInfo reference to job info to be added.
	 * @return True if the job has been added (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean addFirst(JobInfo jobInfo) {
		if (List != null) {
			updateAdd(jobInfo);
			ListPerClass[jobInfo.getJob().getJobClass().getId()].addFirst(jobInfo);
			List.addFirst(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	//----------------METHODS USED BY PRIORITY BASED STRATEGIES---------------//

	//NEW
	//@author Stefano Omini

	/** Adds a new job info in the specified position.
	 * The jobs must be inserted in both general and class jobs lists. The
	 * specified position is relative to general list. In its own class job list, a job
	 * can be put at the beginning (head) or at the end (tail).
	 *
	 * @param index the specified position
	 * @param jobInfo reference to job info to be added.
	 * @param isClassTail if true, job will be put in the last position of its own class job list (tail
	 * strategy); if false, it will be put in first position (head strategy)
	 * @return True if the job has been added (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean add(int index, JobInfo jobInfo, boolean isClassTail) {

		if (List != null) {
			updateAdd(jobInfo);

			if (isClassTail) {
				ListPerClass[jobInfo.getJob().getJobClass().getId()].addLast(jobInfo);
			} else {
				ListPerClass[jobInfo.getJob().getJobClass().getId()].addFirst(jobInfo);
			}
			List.add(index, jobInfo);

			return true;
		} else {
			return false;
		}
	}

	/** Adds a new job info in the specified position.
	 * The jobs must be inserted in both general and class jobs lists. The
	 * specified position is relative to general list. In its own class job list, a job
	 * can be put at the beginning (head) or at the end (tail).
	 *
	 * @param jobInfo reference to job info to be added.
	 * @param isClassTail if true, job will be put in the last position of its own class job list (tail
	 * strategy); if false, it will be put in first position (head strategy)
	 * @return True if the job has been added (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean addFirst(JobInfo jobInfo, boolean isClassTail) {

		if (List != null) {
			updateAdd(jobInfo);

			if (isClassTail) {
				ListPerClass[jobInfo.getJob().getJobClass().getId()].addLast(jobInfo);
			} else {
				ListPerClass[jobInfo.getJob().getJobClass().getId()].addFirst(jobInfo);
			}
			List.addFirst(jobInfo);

			return true;
		} else {
			return false;
		}
	}

	/** Adds a new job info in the specified position.
	 * The jobs must be inserted in both general and class jobs lists. The
	 * specified position is relative to general list. In its own class job list, a job
	 * can be put at the beginning (head) or at the end (tail).
	 *
	 * @param jobInfo reference to job info to be added.
	 * @param isClassTail if true, job will be put in the last position of its own class job list (tail
	 * strategy); if false, it will be put in first position (head strategy)
	 * @return True if the job has been added (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean addLast(JobInfo jobInfo, boolean isClassTail) {

		if (List != null) {
			updateAdd(jobInfo);

			if (isClassTail) {
				ListPerClass[jobInfo.getJob().getJobClass().getId()].addLast(jobInfo);
			} else {
				ListPerClass[jobInfo.getJob().getJobClass().getId()].addFirst(jobInfo);
			}
			List.addLast(jobInfo);

			return true;
		} else {
			return false;
		}
	}

	//end NEW

	//----------------end METHODS USED BY PRIORITY BASED STRATEGIES---------------//

	/** Adds a new job info to the bottom of the list.
	 * @param jobInfo reference to job info to be added.
	 * @return True if the job has been added (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean addLast(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (List != null) {
			updateAdd(jobInfo);
			ListPerClass[jobInfo.getJob().getJobClass().getId()].addLast(jobInfo);
			List.addLast(jobInfo);
			return true;
		} else {
			return false;
		}
	}

	/** Removes a job info from the list and updates the measures related to
	 * throughput, utilization and response time.
	 * @param jobInfo reference to job info to be removed.
	 * @return True if the job has been removed (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean remove(JobInfo jobInfo) throws jmt.common.exception.NetException {
		if (List != null) {
			Job job = jobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			updateThroughput(job);
			updateUtilization(jobClass);

			//NEW
			//@author Stefano Omini
			updateQueueLength(jobClass);
			updateResidenceTime(jobInfo);
			//end NEW

			updateResponseTime(jobInfo);

			ListPerClass[c].remove(jobInfo);
			List.remove(jobInfo);
			LastJobOutTimePerClass[c] = LastJobOutTime = NetSystem.getTime();
			double time = LastJobOutTime - jobInfo.getTime();
			JobsOut++;
			JobsOutPerClass[c]++;
			BusyTime += time;
			BusyTimePerClass[c] += time;

			return true;
		} else {
			return false;
		}
	}

	/** Removes a job info from the top of the list and updates the measures related to
	 * throughput, utilization and response time.
	 * @return A Job info object if it has been found, null otherwise (list
	 * empty or Save property is false)
	 */
	public JobInfo removeFirst() throws jmt.common.exception.NetException {
		if (List != null) {
			JobInfo jobInfo = ((JobInfo) List.getFirst());
			if (jobInfo != null) {
				remove(jobInfo);
				return jobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/** Removes a job info of a specified job class from the top of the list and updates the measures related to
	 * throughput, utilization and response time.
	 * @return A Job info object if it has been found, null otherwise (list
	 * empty or Save property is false)
	 */
	public JobInfo removeFirst(JobClass jobClass) throws jmt.common.exception.NetException {
		if (List != null) {
			int c = jobClass.getId();
			JobInfo JobInfo = ((JobInfo) ListPerClass[c].getFirst());
			if (JobInfo != null) {
				remove(JobInfo);
				return JobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/** Removes a job info from the bottom of the list and updates the measures related to
	 * throughput, utilization and response time.
	 * @return A Job info object if it has been found, null otherwise (list
	 * empty or Save property is false)
	 */
	public JobInfo removeLast() throws jmt.common.exception.NetException {
		if (List != null) {
			JobInfo JobInfo = (JobInfo) List.getLast();
			if (JobInfo != null) {
				remove(JobInfo);
				return JobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/** Removes a job info of a specified job class from the bottom of the list  and updates the measures related to
	 * throughput, utilization and response time.
	 * @return A Job info object if it has been found, null otherwise (list
	 * empty or Save property is false)
	 */
	public JobInfo removeLast(JobClass jobClass) throws jmt.common.exception.NetException {
		if (List != null) {
			int c = jobClass.getId();
			JobInfo JobInfo = (JobInfo) ListPerClass[c].getLast();
			if ((JobInfo != null)) {
				remove(JobInfo);
				return JobInfo;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**---------------------------------------------------------------------
	 *---------------- "ANALYZE" AND "UPDATE" METHODS ----------------------
	 *---------------------------------------------------------------------*/

	/** Analyzes class utilization.
	 * @param jobClass Job class to be analyzed. If null, measure will be
	 * job class independent.
	 * @param Measurement Reference to a measure object.
	 */
	public void analyzeUtilization(JobClass jobClass, Measure Measurement) {
		if (jobClass != null) {
			if (UtilizationPerClass == null) {
				UtilizationPerClass = new Measure[ListPerClass.length];
			}
			UtilizationPerClass[jobClass.getId()] = Measurement;
		} else {
			Utilization = Measurement;
		}
	}

	/** Analyzes class response time.
	 * @param jobClass Job class to be analyzed. If null, measure will be
	 * job class independent.
	 * @param Measurement Reference to a measure object.
	 */
	public void analyzeResponseTime(JobClass jobClass, Measure Measurement) {
		if (jobClass != null) {
			if (ResponseTimePerClass == null) {
				ResponseTimePerClass = new Measure[ListPerClass.length];
			}
			ResponseTimePerClass[jobClass.getId()] = Measurement;
		} else {
			ResponseTime = Measurement;
		}
	}

	/** Analyzes class drop rate. Bertoli Marco
	 * @param jobClass Job class to be analyzed. If null, measure will be
	 * job class independent.
	 * @param Measurement Reference to a measure object.
	 */
	public void analyzeDropRate(JobClass jobClass, InverseMeasure Measurement) {
		if (jobClass != null) {
			if (DropRatePerClass == null) {
				DropRatePerClass = new InverseMeasure[ListPerClass.length];
			}
			DropRatePerClass[jobClass.getId()] = Measurement;
		} else {
			DropRate = Measurement;
		}
	}

	/**
	 *
	 * Analyzes class throughput. <br>
	 * WARNING: An InverseMeasure must be used.
	 * The aim is to save computational time: in fact it's easier to analyze throughput
	 * by passing samples which are equals to 1/X,
	 * instead of doing one division for each sample (this would make simulation much slower).
	 * At the end the correct value is passed.
	 * @param JobClass Job class to be analyzed. If null, measure will be
	 * job class independent.
	 * @param Measurement Reference to a InverseMeasure object.
	 *
	 *
	 */

	public void analyzeThroughput(JobClass JobClass, InverseMeasure Measurement) {
		if (JobClass != null) {
			if (ThroughputPerClass == null) {
				ThroughputPerClass = new InverseMeasure[ListPerClass.length];
			}
			ThroughputPerClass[JobClass.getId()] = Measurement;
		} else {
			Throughput = Measurement;
		}
	}

	/** Analyzes class residence time.
	 * @param JobClass Job class to be analyzed. If null, measure will be
	 * job class independent.
	 * @param Measurement Reference to a measure object.
	 */
	public void analyzeResidenceTime(JobClass JobClass, Measure Measurement) {
		if (JobClass != null) {
			if (ResidenceTimePerClass == null) {
				ResidenceTimePerClass = new Measure[ListPerClass.length];
			}
			ResidenceTimePerClass[JobClass.getId()] = Measurement;
		} else {
			ResidenceTime = Measurement;
		}
	}

	/**
	 * Updates Response time measure
	 * <br>Author: Bertoli Marco
	 * @param JobInfo current JobInfo
	 */
	private void updateResponseTime(JobInfo JobInfo) {
		int c = JobInfo.getJob().getJobClass().getId();
		double ArriveTime = JobInfo.getTime();
		if (ResponseTimePerClass != null) {
			Measure m = ResponseTimePerClass[c];
			if (m != null) {
				m.update(NetSystem.getTime() - ArriveTime, 1.0);
			}
		}
		if (ResponseTime != null) {
			ResponseTime.update(NetSystem.getTime() - ArriveTime, 1.0);
		}
	}

	private void updateUtilization(JobClass JobClass) {
		if (UtilizationPerClass != null) {
			int c = JobClass.getId();
			Measure m = UtilizationPerClass[c];
			if (m != null) {

				m.update(ListPerClass[c].size(), NetSystem.getTime() - getLastModifyTimePerClass(JobClass));
			}
		}
		if (Utilization != null) {
			Utilization.update(List.size(), NetSystem.getTime() - getLastModifyTime());
		}
	}

	private void updateResidenceTime(JobInfo JobInfo) {
		int c = JobInfo.getJob().getJobClass().getId();
		double ArriveTime = JobInfo.getTime();
		if (ResidenceTimePerClass != null) {
			Measure m = ResidenceTimePerClass[c];
			if (m != null) {
				m.update(NetSystem.getTime() - ArriveTime, 1.0);
			}
		}
		if (ResidenceTime != null) {
			ResidenceTime.update(NetSystem.getTime() - ArriveTime, 1.0);
		}
	}

	private void updateDropRate(JobClass jobClass) {
		int c = jobClass.getId();
		if (DropRatePerClass != null) {
			Measure m = DropRatePerClass[c];
			if (m != null) {
				// Inverse measure must be used to compute drop rate
				m.update(NetSystem.getTime() - getLastJobDropTimePerClass(jobClass), 1.0);
			}
		}
		if (DropRate != null) {
			DropRate.update(NetSystem.getTime() - getLastJobDropTime(), 1.0);
		}
	}

	private void updateThroughput(Job Job) {
		int c = Job.getJobClass().getId();
		if (ThroughputPerClass != null) {
			Measure m = ThroughputPerClass[c];
			if (m != null) {
				// new sample is the inter-departures time (1/throughput)
				// Inverse measure must be used to compute throughput
				m.update(NetSystem.getTime() - getLastJobOutTimePerClass(Job.getJobClass()), 1.0);
			}
			if (DEBUG) {
				System.out.println(NetSystem.getTime() - getLastJobOutTimePerClass(Job.getJobClass()));
			}
		}
		if (Throughput != null) {
			Throughput.update(NetSystem.getTime() - getLastJobOutTime(), 1.0);
		}
	}

	private void updateAdd(JobInfo JobInfo) {
		Job job = JobInfo.getJob();
		JobClass jobClass = job.getJobClass();
		int c = jobClass.getId();

		updateUtilization(jobClass);
		updateQueueLength(jobClass);

		JobsIn++;
		JobsInPerClass[c]++;
		LastJobInTimePerClass[c] = LastJobInTime = NetSystem.getTime();

	}

	//NEW
	//@author Stefano Omini
	//modified 21/5/2004

	/** Analyzes list residence time.
	 * @param JobClass Job class to be analyzed. If null, measure will be
	 * job class independent.
	 * @param Measurement Reference to a measure object.
	 */
	public void analyzeQueueLength(JobClass JobClass, Measure Measurement) {
		if (JobClass != null) {
			if (QueueLengthPerClass == null) {
				QueueLengthPerClass = new Measure[ListPerClass.length];
			}
			QueueLengthPerClass[JobClass.getId()] = Measurement;
		} else {
			QueueLength = Measurement;
		}
	}

	/**
	 * WARNING: updateQueueLength is implemented exactly as updateUtilization: the
	 * difference is that in the former case the resident jobs counted
	 * ( ListPerClass[c].size() ) are all the jobs in the node, in the latter case
	 * are only the jobs in the service sections.
	 * This difference must be guaranteed at upper level (in Simulation class) where
	 * "analyze" methods are called
	 * @param JobClass
	 */
	private void updateQueueLength(JobClass JobClass) {
		if (QueueLengthPerClass != null) {
			int c = JobClass.getId();
			Measure m = QueueLengthPerClass[c];
			if (m != null) {
				m.update(ListPerClass[c].size(), NetSystem.getTime() - getLastModifyTimePerClass(JobClass));
			}
		}
		if (QueueLength != null) {
			QueueLength.update(List.size(), NetSystem.getTime() - getLastModifyTime());
		}
	}

	//END NEW

	//NEW
	//@author Stefano Omini

	/** Removes a job info from the list without updating measures.
	 *
	 * @param JobInfo reference to job info to be removed.
	 * @return True if the job has been removed (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean removeAfterRedirect(JobInfo JobInfo) throws jmt.common.exception.NetException {
		if (List != null) {
			Job job = JobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			ListPerClass[c].remove(JobInfo);
			List.remove(JobInfo);

			//the job has been redirected: it shouldn't be counted
			JobsIn--;
			JobsInPerClass[c]--;

			return true;
		} else {
			return false;
		}
	}

	/** Removes a job info from the list after drop.
	 *
	 * @param JobInfo reference to job info to be removed.
	 * @return True if the job has been removed (True if <tt>Save</tt> property is true,
	 * otherwise no list was created by the constructor)
	 */
	public boolean removeAfterDrop(JobInfo JobInfo) throws jmt.common.exception.NetException {
		if (List != null) {
			Job job = JobInfo.getJob();
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			ListPerClass[c].remove(JobInfo);
			List.remove(JobInfo);

			return dropJob(job);
		} else {
			return false;
		}
	}

	/** drops a Job. This method must be called when a job is dropped but it was not in the info list.
	*
	 * @param JobInfo reference to job info to be removed.
	 * @return True if the job has been removed (True if <tt>Save</tt> property is true,
	* otherwise no list was created by the constructor)
	 */
	public boolean dropJob(Job job) throws jmt.common.exception.NetException {
		if (List != null) {
			JobClass jobClass = job.getJobClass();
			int c = jobClass.getId();

			updateQueueLength(jobClass);
			updateDropRate(jobClass);

			//Update last drop time 
			LastJobDropTimePerClass[c] = LastJobDropTime = NetSystem.getTime();
			return true;
		} else {
			return false;
		}
	}

}