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

import jmt.engine.dataAnalysis.InverseMeasure;
import jmt.engine.dataAnalysis.Measure;

/**
 * <p>Title: Global Job Info List</p>
 * <p>Description: This class provides a global "job info list" to be used during
 * simulation to compute global measures. This is designed to be associated to a
 * simulation object.</p>
 *
 * @author Bertoli Marco
 *         Date: 8-mar-2006
 *         Time: 12.27.42
 */
public class GlobalJobInfoList {

    private int numClass;

    // Measures to be evalueated
    private Measure[] responseTimePerClass, jobNumPerClass;
    private Measure responseTime, jobNum;
    private InverseMeasure[] throughputPerClass, dropRatePerClass;
    private InverseMeasure throughput, dropRate;

    // To calculate throughput and job number
    private double lastJobOutTime, lastModifyNumber, lastJobDropTime;
    private double[] lastJobOutTimePerClass, lastModifyNumberPerClass, lastJobDropTimePerClass;
    private int[] jobsPerClass;
    private int jobs;


    /**
     * Creates a new GlobalJobInfoList
     * @param classNum number of classes in current network model
     */
    public GlobalJobInfoList(int classNum) {
        initialize(classNum);
    }

    /**
     * Resets this info list
     * @param classNum number of classes in current network model
     */
    private void initialize(int classNum) {
        numClass = classNum;
        lastJobOutTime = lastJobDropTime = lastModifyNumber = 0.0;
        lastJobOutTimePerClass = new double[classNum];
        lastJobDropTimePerClass = new double[classNum];
        lastModifyNumberPerClass = new double[classNum];
        jobs = 0;
        jobsPerClass = new int[classNum];
        
        // Resets measures
        responseTimePerClass = jobNumPerClass = dropRatePerClass = throughputPerClass = null;
        responseTime = jobNum = dropRate = throughput = null;
    }


// --- Methods to be called on job events ---------------------------------------------
    /**
     * This method MUST be called each time a new job is added to the network
     * @param job identifier of created job
     */
    public void addJob(Job job) {
        job.resetSystemEnteringTime();
        updateJobNumber(job);
        // Updates job number data structures
        lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
        jobs++;
        jobsPerClass[job.getJobClass().getId()]++;
    }

    /**
     * This method MUST be called each time a job is removed from the network
     * @param job identifier of removed job
     */
    public void removeJob(Job job) {
        updateResponseTime(job);
        updateThroughput(job);
        updateJobNumber(job);

        // Updates jobs number and throughput data structures
        jobs--;
        jobsPerClass[job.getJobClass().getId()]--;

        lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
        lastJobOutTimePerClass[job.getJobClass().getId()] = lastJobOutTime = NetSystem.getTime();
    }

    /**
     * This method MUST be called each time a job cycles in its reference station
     * @param job identifier or cycling job
     */
    public void recycleJob(Job job) {
        updateResponseTime(job);
        updateThroughput(job);
        updateJobNumber(job);

        // Updates jobs number and throughput data structures
        lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
        lastJobOutTimePerClass[job.getJobClass().getId()] = lastJobOutTime = NetSystem.getTime();

        job.resetSystemEnteringTime();
    }

    /**
     * This method MUST be called each time a job is forked into a fork node
     * @param job identifier of new created job
     */
    public void addForkedJob(Job job) {
        updateJobNumber(job);
        // Updates job number data structure only
        lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
        jobs++;
        jobsPerClass[job.getJobClass().getId()]++;
    }

    /**
     * This method MUST be called each time a job is joined in a join node
     * @param job identifier of merged job
     */
    public void removeForkedJob(Job job) {
        updateJobNumber(job);
        // Updates job number data structure only
        jobs--;
        jobsPerClass[job.getJobClass().getId()]--;
        lastModifyNumberPerClass[job.getJobClass().getId()] = lastModifyNumber = NetSystem.getTime();
    }

    /**
     * This method must be called each time a job is dropped by a queue or by a blocking region
     * @param job dropped job identifier
     */
    public void dropJob(Job job) {
        removeForkedJob(job);
        // Updates dropped jobs and drop percentage measure
        updateDropRate(job);
        lastJobDropTimePerClass[job.getJobClass().getId()] = lastJobDropTime = NetSystem.getTime();
        
    }
// ------------------------------------------------------------------------------------

// --- Methods to specify measures to be analyzed -------------------------------------
    /**
     * Analyzes System Response Time for a specific job class or for every class
     * @param jobClass specified job class. If null measure will be job independent
     * @param Measure reference to a Measure object
     */
    public void analyzeResponseTime(JobClass jobClass, Measure Measure) {
        if (jobClass != null) {
            // If array is not initialized, initialize it
            if (responseTimePerClass == null)
                responseTimePerClass = new Measure[numClass];

            // Sets measure
            responseTimePerClass[jobClass.getId()] = Measure;
        }
        else
            responseTime = Measure;
    }

    /**
     * Analyzes System Number of Jobs for a specific job class or for every class
     * @param jobClass specified job class. If null measure will be job independent
     * @param Measure reference to a Measure object
     */
    public void analyzeJobNumber(JobClass jobClass, Measure Measure) {
        if (jobClass != null) {
            // If array is not initialized, initialize it
            if (jobNumPerClass == null)
                jobNumPerClass = new Measure[numClass];

            // Sets measure
            jobNumPerClass[jobClass.getId()] = Measure;
        }
        else
            jobNum = Measure;
    }

    /**
     * Analyzes System Throughput for a specific job class or for every class
     * @param jobClass specified job class. If null measure will be job independent
     * @param Measure reference to a Measure object
     */
    public void analyzeThroughput(JobClass jobClass, Measure Measure) {
        if (jobClass != null) {
            // If array is not initialized, initialize it
            if (throughputPerClass == null)
                throughputPerClass = new InverseMeasure[numClass];

            // Sets measure
            throughputPerClass[jobClass.getId()] = (InverseMeasure)Measure;
        }
        else
            throughput = (InverseMeasure)Measure;
    }
    
    /**
     * Analyzes Drop Rate for a specific job class or for every class
     * @param jobClass specified job class. If null measure will be job independent
     * @param Measure reference to a Measure object
     */
    public void analyzeDropRate(JobClass jobClass, Measure Measure) {
        if (jobClass != null) {
            // If array is not initialized, initialize it
            if (dropRatePerClass == null)
                dropRatePerClass = new InverseMeasure[numClass];

            // Sets measure
            dropRatePerClass[jobClass.getId()] = (InverseMeasure)Measure;
        }
        else
            dropRate = (InverseMeasure)Measure;
    }
// ------------------------------------------------------------------------------------

// --- Methods to update measures -----------------------------------------------------
    /**
     * Updates System Response Time measures.
     * @param job current job
     */
    private void updateResponseTime(Job job) {
        if (responseTimePerClass != null) {
            // Retrives measure (if not null)
            Measure m = responseTimePerClass[job.getJobClass().getId()];
            if (m != null)
                m.update(NetSystem.getTime() - job.getSystemEnteringTime(), 1.0);
        }
        if (responseTime != null) {
            responseTime.update(NetSystem.getTime() - job.getSystemEnteringTime(), 1.0);
        }
    }

    /**
     * Updates System Job Number measures.
     * @param job current job
     */
    private void updateJobNumber(Job job) {
        if (jobNumPerClass != null) {
            // Retrives measure (if not null)
            int index = job.getJobClass().getId();
            Measure m = jobNumPerClass[index];
            if (m != null)
                m.update(jobsPerClass[index], NetSystem.getTime() - lastModifyNumberPerClass[index]);
        }
        if (jobNum != null) {
            jobNum.update(jobs, NetSystem.getTime() - lastModifyNumber);
        }
    }

    /**
     * Updates System Throughput measures.
     * @param job current job
     */
    private void updateThroughput(Job job) {
        if (throughputPerClass != null) {
            // Retrives measure (if not null)
            // new sample is the inter-departures time (1/throughput)
            int index = job.getJobClass().getId();
            InverseMeasure m = throughputPerClass[index];
            if (m != null)
                m.update(NetSystem.getTime() - lastJobOutTimePerClass[index], 1.0);
        }
        if (throughput != null) {
            throughput.update(NetSystem.getTime() - lastJobOutTime, 1.0);
        }
    }
    
    /**
     * Updates System Drop Rate measures.
     * @param job current job
     */
    private void updateDropRate(Job job) {
        if (dropRatePerClass != null) {
            // Retrives measure (if not null)
            int index = job.getJobClass().getId();
            Measure m = dropRatePerClass[index];
            if (m != null)
                m.update(NetSystem.getTime() - lastJobDropTimePerClass[index], 1.0);
        }
        if (dropRate != null) {
            dropRate.update(NetSystem.getTime() - lastJobDropTime, 1.0);
        }
    }
// ------------------------------------------------------------------------------------

}
