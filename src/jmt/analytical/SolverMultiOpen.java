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
  
package jmt.analytical;

/**
 * Solves a multiclass open model (LD stations are not allowed).
 * @author Federico Granata, Stefano Omini
 */
public class SolverMultiOpen extends SolverMulti {

	private double[] lambda;

	/**
     * Creates a SolverMultiOpen
     * @param classes number of classes
     * @param stations number of stations
     * @param lambda array of arrival rates
     */
    public SolverMultiOpen(int classes, int stations, double[] lambda) {
		super(classes, stations);
		this.lambda = lambda;
	}


   	/**
	 * Solves the model, using an appropriate technique (LI or LD model).
	 */
	public void solve() {
		//tests if all the resources, stations, are load independent
		boolean loadIndep = true;
		for (int i = 0; i < stations && loadIndep; i++) {
			if (type[i] == LD)
				loadIndep = false;
		}

		if (loadIndep) {
			solveLI();
		} else {
			solveLD();
		}
	}



    /**
     * Solves a model with only delay or load independent stations.
	 */
    private void solveLI() {

        //NEW
        //@author Stefano Omini

        //initializes array of aggregate values
        for (int j = 0; j < stations; j++) {
            scUtilization[j] = 0;
        }
        //end NEW


		for (int i = 0; i < classes; i++) {
			//throughput of class i
            clsThroughput[i] = lambda[i];
			for (int j = 0; j < stations; j++) {
                //throughput of class i for station j
				throughput[j][i] = lambda[i] * visits[j][i];
				//utilization of class i for station j
                utilization[j][i] = lambda[i] * visits[j][i] * servTime[j][i][0];
                //aggregate utilization for station j
				scUtilization[j] += utilization[j][i];

			}

		}

		for (int i = 0; i < classes; i++) {

            for (int j = 0; j < stations; j++) {
				if (type[j] == Solver.DELAY)
					//delay stations
                    //residence time of class i in station j
                    residenceTime[j][i] = visits[j][i] * servTime[j][i][0];
				else
                    //queueing stations
                    //residence time of class i in station j
					residenceTime[j][i] = visits[j][i] * servTime[j][i][0]
					        / (1 - scUtilization[j]);
				//queue length of class i for station j
                queueLen[j][i] = residenceTime[j][i] * lambda[i];
				//aggregate response time for class i
                clsRespTime[i] += residenceTime[j][i];

			}
		}

        //NEW
        //@author Stefano Omini

        for (int j = 0; j < stations; j++) {
            //initializes array of aggregate values
            scThroughput[j] = 0;
            scQueueLen[j] = 0;
            scResidTime[j] = 0;
        }

        //end NEW


        for (int j = 0; j < stations; j++) {

			for (int i = 0; i < classes; i++) {
				//aggregate throughput for station j
                scThroughput[j] += throughput[j][i];
                //aggregate queue length for station j
				scQueueLen[j] += queueLen[j][i];

                //aggregate residence time for station j

                //NEW
                //@author Stefano Omini

                //TODO visits[j][1] è sbagliato o giusto??
                //OLDER
                //scResidTime[j] += visits[j][1] * queueLen[j][i] / throughput[j][i];
                //OLD
                //scResidTime[j] += visits[j][i] * queueLen[j][i] / throughput[j][i];
                scResidTime[j] += residenceTime[j][i];

                //end NEW

			}
            //NEW
            //@author Stefano Omini

            //system response time
            sysResponseTime += scResidTime[j];
            //end NEW

		}

        //TODO: controllare le misure aggiunte (misure aggregate del sistema)
        //NEW
        //@author Stefano Omini
        for (int i = 0; i < classes; i++) {
            for (int j = 0; j < stations; j++) {
                //mean number of jobs of class i in the system
                clsNumJobs[i] += queueLen[j][i];
			}
            //mean number of jobs in the system
            sysNumJobs += clsNumJobs[i];
        }

        //system throughput
        sysThroughput = sysNumJobs / sysResponseTime;

        //end NEW

	}



    /**
     * Solves a model with load dependent stations too. ANCORA DA FARE!!
	 */
    private void solveLD() {

        //TODO: tutta da fare!
        return;
    }


    //NEW
    //@author Stefano Omini
    //TODO aggiungere controllo su processing capacity
    /**
     * A system is said to have sufficient capacity to process a given load
     * <tt>lambda</tt> if no service center is saturated as a result of the combined loads
     * of all the classes.
     * <br>
     * WARNING: This method should be called before solving the system.
     * @return true if sufficient capacity exists for the given workload, false otherwise
     */
    public boolean hasSufficientProcessingCapacity(){

        //the maximum aggregate utilization between all the stations must be < 1
        //otherwise the system has no sufficient processing capacity
        for (int j = 0; j < stations; j++) {

            if (type[j] == SolverMulti.DELAY) {
                //delay station: don't check saturation
                continue;
            }

            //utiliz is the aggregate utilization for station j
            double utiliz = 0;
            for (int i = 0; i < classes; i++) {
                utiliz += lambda[i] * visits[j][i] * servTime[j][i][0];
			}
            if (utiliz >= 1) {
                return false;
            }
		}
        //there are no stations with aggregate utilization >= 1
        return true;
    }

    //end NEW






    /*

    OLD

    NEW: Use MultiSolver toString


    public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append(getClass().getName());
		buf.append("\n-------------------------");
		for (int i = 0; i < stations; i++) {
			buf.append("\n\nIndexes of station " + name[i]);
			for (int j = 0; j < classes; j++) {
				buf.append("\n- of class " + j);
				buf.append("\n  throughput        : " + throughput[i][j]);
				buf.append("\n  utilization       : " + utilization[i][j]);
				buf.append("\n  mean queue length : " + queueLen[i][j]);
                buf.append("\n  residence time    : " + residenceTime[i][j]);
			}
			buf.append("\n- aggregate values");
            buf.append("\n  throughput aggr     : " + scThroughput[i]);
			buf.append("\n  utilization aggr    : " + scUtilization[i]);
            buf.append("\n  queue length aggr   : " + scQueueLen[i]);
			buf.append("\n  residence time aggr : " + scResidTime[i]);
		}
		for (int j = 0; j < classes; j++) {
			buf.append("\n\nIndexes of class " + j);
			buf.append("\n  response time       : " + clsRespTime[j]);
			buf.append("\n  throughput          : " + clsThroughput[j]);
		}
		buf.append("\n\nSystem aggregate values");
        buf.append("\n  System Response Time    : " + sysResponseTime);
		buf.append("\n  System Throughput       : " + sysThroughput);
		return buf.toString();
	}
    */




}
