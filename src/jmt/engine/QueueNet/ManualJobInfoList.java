package jmt.engine.QueueNet;

import java.util.List;

import jmt.common.exception.NetException;
import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;

/**
 * <p><b>Name:</b> ManualJobInfoList</p> 
 * <p><b>Description:</b> 
 * A "dummy" JobInfoList instance that will not hold any queue of jobs.
 * Indices will be updated manually.
 * </p>
 * <p><b>Date:</b> 10/nov/2009
 * <b>Time:</b> 08.33.59</p>
 * @author Bertoli Marco [marco.bertoli@neptuny.com]
 * @version 3.0
 */
public class ManualJobInfoList implements JobInfoList {

	public boolean add(JobInfo jobInfo) {
		return true;
	}

	public boolean add(int index, JobInfo jobInfo, boolean isClassTail) {
		return true;
	}

	public boolean addFirst(JobInfo jobInfo) {
		return true;
	}

	public boolean addFirst(JobInfo jobInfo, boolean isClassTail) {
		return true;
	}

	public boolean addLast(JobInfo jobInfo, boolean isClassTail) {
		return true;
	}

	public boolean addLast(JobInfo jobInfo) throws NetException {
		return true;
	}

	public void analyzeDropRate(JobClass jobClass, InverseMeasure Measurement) {
		// TODO Auto-generated method stub

	}

	public void analyzeQueueLength(JobClass JobClass, Measure Measurement) {
		// TODO Auto-generated method stub

	}

	public void analyzeResidenceTime(JobClass JobClass, Measure Measurement) {
		// TODO Auto-generated method stub

	}

	public void analyzeResponseTime(JobClass jobClass, Measure Measurement) {
		// TODO Auto-generated method stub

	}

	public void analyzeThroughput(JobClass JobClass, InverseMeasure Measurement) {
		// TODO Auto-generated method stub

	}

	public void analyzeUtilization(JobClass jobClass, Measure Measurement) {
		// TODO Auto-generated method stub

	}

	public boolean dropJob(Job job) throws NetException {
		// TODO Auto-generated method stub
		return false;
	}

	public double getBusyTime() throws NetException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getBusyTimePerClass(JobClass JobClass) throws NetException {
		// TODO Auto-generated method stub
		return 0;
	}

	public List<JobInfo> getJobList() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getJobsIn() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getJobsInPerClass(JobClass JobClass) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getJobsOut() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getJobsOutPerClass(JobClass JobClass) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastJobDropTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastJobDropTimePerClass(JobClass JobClass) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastJobInTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastJobInTimePerClass(JobClass JobClass) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastJobOutTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastJobOutTimePerClass(JobClass JobClass) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastModifyTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getLastModifyTimePerClass(JobClass JobClass) {
		// TODO Auto-generated method stub
		return 0;
	}

	public JobInfo lookFor(Job Job) throws NetException {
		return null;
	}

	public boolean remove(JobInfo jobInfo) throws NetException {
		return true;
	}

	public boolean removeAfterDrop(JobInfo JobInfo) throws NetException {
		return true;
	}

	public boolean removeAfterRedirect(JobInfo JobInfo) throws NetException {
		return true;
	}

	public JobInfo removeFirst() throws NetException {
		return null;
	}

	public JobInfo removeFirst(JobClass jobClass) throws NetException {
		return null;
	}

	public JobInfo removeLast() throws NetException {
		return null;
	}

	public JobInfo removeLast(JobClass jobClass) throws NetException {
		return null;
	}

	public int size() throws NetException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int size(JobClass JobClass) throws NetException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#setProcessorSharing(boolean)
	 */
	public void setProcessorSharing(boolean processorSharing) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see jmt.engine.QueueNet.JobInfoList#setServerNumber(int)
	 */
	public void setServerNumber(int serverNumber) {
		// TODO Auto-generated method stub
		
	}

}
