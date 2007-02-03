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


//TODO: per il momento considero casi semplici (non vincoli innestati, stazioni soggette a vincoli sono LI)
/**
 * This class solves a system with a memory constraint.<br>
 * A memory constraint places an upper bound on the concurrent utilization of a
 * processing resource (therefore places an upper bound on the throughput of the system).
 * <br>
 * These memory constraints violate the conditions required for the separability of the queue
 * network: the approach to solve the system is based on the concepts of FESCs (Flow Equivalent
 * Service Centers) and hierarchical modelling.
 * <br>
 * Here we consider a system with C customer classes (c = 1, 2, ... C) having only a region
 * (one or more stations) subjected to independent memory constraints (that is, a constraint for
 * each different class).
 *
 * <br><br>
 * Warning: AT THE MOMENT THE ALGORITHM OF THIS SOLVER HASN'T BEEN COMPLETED YET!
 * Warning: AT THE MOMENT THIS CLASS IS NOT USED IN JAVA MODELLING TOOLS!
 *
 * @author Stefano
 * @version 24-giu-2004 8.40.30
 */
public class MemoryConstraints {

    public final boolean DEBUG = true;


    /*
    We initially have a non-separable network.

    We decompose the model into two parts:
    1) the central subsystem plus the memory queue
    2) the external environment (that may be a terminal with N customers or a
    transaction source with arrival rate lambda)

    Then we define a load dependent center that is flow equivalent to part 1) from
    the point of view of the external environment.

    At last we analyze a model made up by the FESC and the external environment, which
    can be easily solved an which will give nearly the same results of the original model.
    */



    /**
     * the model used to describe and solve the original system without constraints
     */
    protected SolverMultiMixed solver_not_constr = null;


    /**
     * indicate which stations are subjected to memory constraint
     * (true if the station is subjected to constraints, false otherwise)
     */
    protected boolean[] subjected_stations = null;

    /**
     * one memory constraint for each class (in fact we are considering independent
     * memory constraints): a memory constraint is the maximum number of customers that can be
     * served at the same time in the specified region.
     * (-1 if the corresponding class has no constraints)
     */
    protected int[] class_constraints = null;

    /**
     * Counter of the number of class which are subjected to memory constraints
     */
    protected int constrained_classes = 0;

    /**
     * Counter of the number of stations which are subjected to memory constraints
     * (i.e. the stations included in the region subjected to memory constraints)
     */
    protected int constrained_stations = 0;

    /**
     * The class must be reordered: constrained classes are put first,
     * not constrained ones are put after.
     * This array contains the original positions, which will be restored
     * after solving the system.
     */
    protected int[] position = null;

    /**
     * For each class, number of customers in the region with memory constraints.
     * (WARNING: the class order does not correspond to the original model order!)
     */
    private double[] subsystem_customers = null;

    //during the computation, the constrained classes will be transformed into
    //batch class with a new population equal to subsystem_customers (see step2)
    private int[] new_classTypes = null;
    private double[] new_popPar = null;


    /**---------------MODEL DEFINITION------------------------*/

	/** number of service centers */
    protected int stations = 0;

	/** number of classes  */
    protected int classes = 0;

	/** array of names of service centers*/
    protected String[] name;

	/** array of types of service centers */
    protected int[] type;

	/** service times for each service station, class, population<br>
     * [station] [class] [population] */
    protected double[][][] servTime;

	/** visits for each service station, class<br>
     * [station] [class] */
    protected double[][] visits;


    /** array with class types */
	int[] classType;

    /** array describing the classes: each element can be either an
     *  arrival rate (for open classes) or a population (for closed ones),
     *  according to the class type
     */
	double[] popPar;
    /**-----------END MODEL DEFINITION------------------------*/



    /**
     * Construction of the model with memory constraint: note that this method
     * changes the original order of the classes (and of the class dependent
     * parameters) in the model description .
     *
     * @param  n   array of names of service centers.
     * @param  t   array of the types (LD or LI) of service centers.
     * @param  s   matrix of service time of the service centers.
     * @param  v   matrix of visits to the service centers.
     * @param popParam array describing the classes: each element can be either an
     * arrival rate (for open classes) or a population (for closed ones),
     * according to the class type
     * @param clsType array of class types (open or closed)
     * @param subj_stat array which indicates if a station is comprised (true) or not (false)
     * in the network region which is subjected to memory constraints
     * @param class_constr memory constraints for each class (-1 if the corresponding
     * class has no constraints)
     */
    public MemoryConstraints(String[] n, int[] t, double[][][] s, double[][] v,
	                     double[] popParam, int[] clsType, boolean[] subj_stat, int[] class_constr){

        //model definition (parameters which are independent by the presence of constraints)
        //TODO: si può pensare di richiamare il metodo input dei risolutori per verificare correttezza
        stations = t.length;
        classes = clsType.length;

        name = n;
        type = t;
        servTime = new double[stations][classes][];
        visits = new double[stations][classes];
        popPar = new double[classes];
        classType = new int[classes];


        //model definition (parameters which are dependent by the presence of constraints)

        subjected_stations = subj_stat;
        class_constraints = class_constr;

        position = new int[classes];

        //counts the classes subjected to memory constraints
        for (int i = 0; i < class_constraints.length; i++) {
            if (class_constraints[i] != -1) {
                constrained_classes++;
            }
        }

        //counts the stations subjected to memory constraints
        for (int i = 0; i < subjected_stations.length; i++) {
            if (subjected_stations[i] == true) {
                constrained_stations++;
            }
        }

        //classes must be reordered

        if (DEBUG) {
            System.out.println("Prima del riordino");
            for (int i = 0; i < stations; i++) {
                System.out.println("stazione: " + i);
                for (int j = 0; j < classes; j++) {
                    System.out.println("[ ");
                    for (int c = 0; c < s[i][j].length; c++) {
                        System.out.println("  " + s[i][j][c] + "  ");
                    }
                    System.out.println(" ]");
                }
            }
        }


        int count_not_constrained = 0; //not constrained classes inserted up to now
        int count_constrained = 0; //constrained classes inserted up to now

        //the constraints in the new order
        int[] cls_constraints = new int[class_constraints.length];

        for (int i = 0; i < class_constraints.length; i++) {
            if (class_constraints[i] != -1) {
                //this class is constrained: must be put in the first positions

                position[count_constrained] = i;

                cls_constraints[count_constrained] = i;

                //must be changed: servTime, visits, popParam, classType
                classType[count_constrained] = clsType[i];
                popPar[count_constrained] = popParam[i];
                for (int j = 0; j < stations; j++) {
                    servTime[j][count_constrained] = new double[s[j][i].length];//TODO: inutile?
                    servTime[j][count_constrained] = s[j][i];
                    visits[j][count_constrained] = v[j][i];
                }

                count_constrained++;

            } else {
                //this class is not constrained: must be put in the last positions,
                //after all constrained classes
                position[constrained_classes + count_not_constrained] = i;

                cls_constraints[constrained_classes + count_not_constrained] = i;

                //must be changed: servTime, visits, classType
                classType[constrained_classes + count_not_constrained] = clsType[i];
                popPar[constrained_classes + count_not_constrained] = popParam[i];
                for (int j = 0; j < stations; j++){
                    servTime[j][constrained_classes + count_not_constrained] = new double[s[j][i].length];//TODO: inutile?
                    servTime[j][constrained_classes + count_not_constrained] = s[j][i];
                    visits[j][constrained_classes + count_not_constrained] = v[j][i];
                }

                count_not_constrained++;
            }

        }


        //sets the new order of class_constraints (cannot be done during the for cycle)
        class_constraints = cls_constraints;
        cls_constraints = null;



        if (DEBUG) {
            System.out.println("Dopo il riordino");
            for (int i = 0; i < stations; i++) {
                System.out.println("stazione: " + i);
                for (int j = 0; j < classes; j++) {
                    System.out.println("[ ");
                    for (int c = 0; c < servTime[i][j].length; c++) {
                        System.out.println("  " + servTime[i][j][c] + "  ");
                    }
                    System.out.println(" ]");
                }
            }
        }

    }



    /**
     * The solution technique changes the order of classes (then the order of parameters
     * and results too). This method restores the original order.
     */
    public void restoreOrder() {
        //TODO: da fare
    }




    private void step1() {
        /*
        first estimates the average customer population of each costrained class
        in the costrained region. To do this, solves the model without memory
        constraints (it becomes a separable network).
        */

        //creates, initializes and solves a multiclass model
        //this model does not correspond to the original one (order of classes has been changed)
        solver_not_constr = new SolverMultiMixed(classes, stations);

        if (solver_not_constr.input(name, type, servTime, visits)) {
            solver_not_constr.solve();
        } else {
            System.out.println("Error: wrong parameters");
            return;
        }

        //calculates the average customer population for each constrained class
        //in the constrained region (as the sum of the queues in each station
        //of the constrained region)
        //TODO: estenderlo anche per le classi non vincolate??
        subsystem_customers = new double[constrained_classes];
        for (int i = 0; i < constrained_classes; i++) {
            //initializes
            subsystem_customers[i] = 0;
        }


        for (int j = 0; j < stations; j++) {
            if (subjected_stations[j]){
                //this station is comprised in the constrained region
                //then add the class queue length
                for (int i = 0; i < constrained_classes; i++) {
                    subsystem_customers[i] += solver_not_constr.getQueueLen(j,i);
                }
            }
        }

        //updates subsystem customers, by choosing the minimum between
        //the average population in the subsystem (computed by the unconstrained model)
        //and the memory constraint
        for (int i = 0; i < constrained_classes; i++) {
            if (subsystem_customers[i] > class_constraints[i]) {
                subsystem_customers[i] = class_constraints[i];
            }
        }
    }


    private void step2() {

        /* modify the model by changing each constrained class i into a batch class
        with population equal to subsystem_customers[i]
        the uncostrained classes are left in the original form
        the results is a separable network */

        //changes the model: type and popPar of the constrained classes
        new_classTypes = new int[classes];
        new_popPar = new double[classes];

        for (int i = 0; i < constrained_classes; i++) {
            new_classTypes[i] = SolverMulti.CLOSED_CLASS;
            //exact MVA accepts only integer population!!
            //if approximated MVA should be use, double population would be accepted too
            //TODO: se usassi metodi approssimati potrei usare anche double
            //TODO: usare round o ceil??
            new_popPar[i] = (int) java.lang.Math.round(subsystem_customers[i]);
        }

        //the other classes are left unchanged
        for (int i = constrained_classes; i < classes; i++) {
            new_classTypes[i] = type[i];
            new_popPar[i] = popPar[i];
        }
    }



    private void step3() {
        /*
        For each constrained class c:
        - replace the number of customers (subsystem[c]) with each population n of class c
        from 0 to Mc (memory constraint for class c). Evaluate the network (only the subsystem?)
        and obtain the throughput of class c, Xc(n)
        - create a FESC, a single class load dependent service center, whose throughput is
        Xc(n) for n<Mc and Xc(Mc) for n>=Mc
        - define and evaluate a single class separable model made up of this FESC and of the
        external environment. Obtain the queue length distribution at the FESC and use it to
        compute a new estimate for subsystem customers of class c

        This iteration must be repeated until successive estimates of subsystem customers
        for each class are sufficiently close.
        */

        boolean finished = false;
        double[] X_class_c = null;
        int counter = 0;//how many class population have been correctly estimated
        int inserted = 0;//counter of stations inserted in the models under construction


        //the model used to contain the solution of the system with constraints
        SolverMultiMixed solver_mmix = null;

        //parameters of the model used to determine the throughput of class c in the aggregate

        //array of names of service centers
        String[] name_mmix = null;

        // array of types of service centers
        int[] type_mmix = null;

        // service times for each service station, class, population
        // [station] [class] [population]
        double[][][] servTime_mmix = null;

        // visits for each service station, class
        // [station] [class]
        double[][] visits_mmix = null;



        //the single class (only class c) LD model containing the fesc and the complementary network
        SolverSingleClosedMVA solver_smva = null;

        //parameters of the model used to determine the throughput of class c in the aggregate

        //array of names of service centers
        String[] name_smva = null;

        // array of types of service centers
        int[] type_smva = null;

        // service times for each service station, population
        // [station] [population]
        double[][] servTime_smva = null;

        // visits for each service station
        // [station]
        double[] visits_smva = null;


        do {
            counter = 0;

            for (int c = 0; c < constrained_classes; c++) {
                //solves a multiclass model, changing only class c population
                //the other class populations are considered equal to the average populations
                //in subsystem

                //TODO: ci vuole o no il valore per 0 customers? sì (vedi single closed..)
                X_class_c = new double[class_constraints[c]+1];
                X_class_c[0] = 0;

                for (int n = 1; n <= class_constraints[c]; n++) {

                    //create the solver
                    //TODO: cambiare modello (considerare solo l'aggregato)
                    solver_mmix = new SolverMultiMixed(classes, constrained_stations);

                    //prepare the input: uses new_popPar, except population of class c,
                    //which goes from 1 to Mc
                    double[] popParam = new double[classes];
                    for (int i = 0; i < classes; i++) {
                        popParam[i] = new_popPar[i];
                    }
                    //changes only class c population
                    popParam[c] = n;

                    //modify names, servTimes, ... considering only constrained stations
                    inserted = 0;

                    servTime_mmix = new double[constrained_stations][classes][];

                    for (int j = 0; j < stations; j++) {
                        if (subjected_stations[j] == true) {
                            //this station belongs to the subsytem
                            name_mmix[inserted] = name[j];
                            type_mmix[inserted] = type[j];
                            for (int cls = 0; cls < classes; cls++) {
                                servTime_mmix[inserted][cls] = servTime[j][cls];
                                visits_mmix[inserted][cls] = visits[j][cls];
                                }
                            inserted++;

                        }
                    }

                    if (solver_mmix.input(name_mmix, type_mmix, servTime_mmix, visits_mmix, popParam, new_classTypes)) {
                        //solves the model
                        solver_mmix.solve();
                        //obtain the throughput of class c with n customers of class c
                        X_class_c[n] = solver_mmix.getClsThroughput(c);
                        if (DEBUG) {
                            System.out.println("Classe " + c + ", popolazione " + n);
                            solver_mmix.toString();
                        }
                    } else {
                        System.out.println("Wrong input");
                        return;
                    }
                }

                //creates a single class FESC center using the X obtained (and X(n)>X(Mc) for n>Mc)
                //evaluate a single class model with the FESC and the external environment of
                //class c
                //TODO: problema! Se la classe è aperta come faccio a risolvere il LD??? Se invece devo usare i nuovi tipi (tutte chiuse) è ok

                int model_populat = (int) Math.round(popPar[c]);
                //the stations in the model are the ones not comprised in the constrained region,
                //plus the FESC
                int model_stations = (stations - constrained_stations) + 1;

                //create the single class solver
                solver_smva = new SolverSingleClosedMVA(model_populat, model_stations);

                //prepare the parameters (to have a single class model)

                //insert the stations (first the station of the complementary network, then the FESC)
                inserted = 0;
                for (int j = 0; j < stations; j++) {
                    if (subjected_stations[j] == false) {
                        //the station belongs to the unconstrained subnetwork, must be added
                        name_smva[inserted] = name[j];
                        type_smva[inserted] = type[j];
                        visits_smva[inserted] = visits[j][c];
                        //the other station are LI      //TODO: e se non fosse così??
                        servTime_smva[inserted] = new double[1];
                        servTime_smva[inserted][0] = servTime[j][c][0];
                        inserted++;
                    }
                }
                //now adds the FESC
                name_smva[inserted] = "FESC cls" + c;
                type_smva[inserted] = Solver.LD; //FESC is Load Dependent
                visits_smva[inserted] = 1; //TODO: giusto?
                //TODO: quanti customer devo inserire? popolazione media Nc o vincolo Mc??? o forse N;
                servTime_smva[inserted] = new double[model_populat + 1];
                servTime_smva[inserted][0] = 0;
                for (int cust = 1; cust < servTime_smva.length; cust++) {
                    //service rate FESC with n customers = throughput Subsystem with n customers
                    //service time = 1 / service rate
                    servTime_smva[inserted][cust] = 1 / X_class_c[cust];
                }

                //solves the model
                if (solver_smva.input(name_smva, type_smva, servTime_smva, visits_smva)) {
                    solver_smva.solve();
                } else {
                    System.out.println("Wrong input");
                    return;
                }

                //obtain the queue length distribution at the FESC


                //new estimate for class c subsystem customers

                //if |vecchia-nuova| < soglia, then counter++











            }


            if (counter == constrained_classes) {
                //all average populations in subsystem have been estimated with
                //sufficient precision: stop iteration
                finished = true;
            }

        } while (!finished);



    }


    //TODO: DEBUG: serve solo per vedere se funziona il meccanismo che sposta tutte le classi vincolate all'inizio
    private static void TestReorder() {

        System.out.println("Sistema misto con 3 classi");


        int[] classTypes = new int[3];
        classTypes[0] = SolverMulti.CLOSED_CLASS;
        classTypes[1] = SolverMulti.CLOSED_CLASS;
        classTypes[2] = SolverMulti.OPEN_CLASS;

        double[] classData = new double[3];
        classData[0] = 5;
        classData[1] = 10;
        classData[2] = 0.05;


        String[] name = {"Station1", "Station2"};
		int[] type = {Solver.LI, Solver.LI};
		double[][][] servt = new double[2][][];

		//station 1
        servt[0] = new double[3][2];
        servt[0][0][0] = 0.002;
        servt[0][0][1] = 0.022;
        servt[0][1][0] = 0.005;
        servt[0][1][1] = 0.004;
        servt[0][2][0] = 0.002;
        servt[0][2][1] = 0.008;

        //station 2
        servt[1] = new double[3][1];
        servt[1][0][0] = 0.003;
        servt[1][1][0] = 0.004;
        servt[1][2][0] = 0.002;


        double[][] visits = new double[2][3];

        //station 1
        visits[0][0] = 5;
        visits[0][1] = 25;
        visits[0][2] = 15;

        //station 2
        visits[1][0] = 1;
        visits[1][1] = 3;
        visits[1][2] = 8;

        boolean[] constr_stat = {true, true};
        int[] constr = {-1, -1, 4};


        MemoryConstraints mc = new MemoryConstraints(name, type, servt, visits,
                classData, classTypes, constr_stat, constr);
    }

       
}

