package jmt.test.analytical;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import jmt.analytical.Solver;
import jmt.analytical.SolverMulti;
import jmt.analytical.SolverMultiClosedAQL;
import jmt.analytical.SolverMultiClosedBardSchweitzer;
import jmt.analytical.SolverMultiClosedChow;
import jmt.analytical.SolverMultiClosedLinearizer;
import jmt.analytical.SolverMultiClosedMVA;
import jmt.engine.math.Printer;

public class TestAMVA {

	public static void main(String[] args) {
		//testMultiClosedLI();
//		System.out.println("\n------------------------FOO 1----------------------------\n");
//		testMultiClosedLI_visits_not1();
//		System.out.println("\n------------------------FOO 2----------------------------\n");
//		testMultiClosedLI_big();
//		System.out.println("\n------------------------FOO 3----------------------------\n");
//		testLinearizerExample();
//		System.out.println("\n------------------------FOO 4----------------------------\n");
//		testMultiClosedLI_huge();
		//testMultiClosedLI_MoM_huge();
//		testMultiClosedLI2();
		
		TestAMVA test = new TestAMVA();
		//test.runTests2();
		//test.runTests3();
		test.runTests();
	}
	
	public void runTests() {
		int queues = 5;
		int classes = 5;
		int[] populations = {100, 300, 500, 700};
		//int[] populations = {100};
		//double[] tolerances = {Math.pow(10, -2), Math.pow(10, -4), Math.pow(10, -7)};
		//double[] tolerances = {Math.pow(10, -2), Math.pow(10, -5)};
		double[] tolerances = {Math.pow(10, -7)};
		
		// error = max_{c,k}((Q_{c,k} - Q*{c,k})/N_c)
		// Q* are the exact values
		
		for (int c = 5; c <= classes; c++) {
			for (int k = 5; k <= queues; k++) {
				for (int pop : populations) {
					for (double tol : tolerances) {
						//System.out.println("##############################################");
						System.out.println(",,,,");
						System.out.print("Classes = " + c);
						System.out.print(",Stations = " + k);
						System.out.print(",Population = " + pop);
						System.out.print(",Tolerance = " + tol);
						System.out.println();
						System.out.println("Algorithm,Max error,Elapsed time,Memory usage,Memory usage wo diff");
						runTest2(c, k, pop, tol);
						System.out.println(",,,,");
						//System.out.println("\n##############################################\n");
					}
				}
			}
		}
	}
	
	private void runTest2(int classes, int stations, int population, double tolerance) {
		int[] pop = new int[classes];
		String[] name = new String[stations];
		int[] type = new int[stations];
		for (int i = 0; i < stations; i++) {
			name[i] = "Station " + i;
			type[i] = Solver.LI;
		}
		for (int i = 0; i < classes; i++) {
			pop[i] = (int)Math.ceil(population/classes);
		}
		
		// service demands here
		double[][][] servt = new double[stations][classes][1];
		double[][] visits = new double[stations][classes];
		
		Random rand = new Random();
		for (int k = 0; k < stations; k++) {
			for (int c = 0; c < classes; c++) {
				servt[k][c][0] = rand.nextDouble() * 5;
				visits[k][c] = 1;
			}
		}
		
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		
		double[][] mvaQueueLen = null;
		SolverMulti solver;
		String algorithm;
		if (classes < 5) {
			solver = new SolverMultiClosedMVA(classes, stations);
			algorithm = "MVA";
			if (((SolverMultiClosedMVA)solver).input(name, type, servt, visits, pop)) {
				long start = System.currentTimeMillis();
				solver.solve();
				System.out.print(algorithm+",");
				System.out.print(0+",");
				//System.out.println(algorithm);
				//System.out.println("-------------------------------------");
				mvaQueueLen = solver.getQueueLen();
				//System.out.println(queueLength(stations, classes, solver));
				long stop = System.currentTimeMillis();
				System.out.print(((stop-start)/(double)1000) + ",");
				//System.out.println("Elapsed time (ms) = " + (stop - start));
				start = 0; stop = 0;
				
				long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				solver = null;
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				
				System.out.print(((mem0-mem1)/(double)1024) + ",");
				System.out.print((mem0/(double)1024) + "\n");
	//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
	//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
	//			System.out.println("-------------------------------------");
				mem0 = 0; mem1 = 0;
			} else {
				System.out.println("Wrong input!!");
			}
		}
		
		solver = new SolverMultiClosedChow(classes, stations, pop);
		algorithm = "Chow";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			//System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		algorithm = "Bard-Schweitzer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			//System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedAQL(classes, stations, pop);
		algorithm = "AQL";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			//System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		algorithm = "Linearizer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			//System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		algorithm = "DeSouza-Muntz Linearizer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			//System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		if (classes > 4) {
			solver = new SolverMultiClosedMVA(classes, stations);
			algorithm = "MVA";
			if (((SolverMultiClosedMVA)solver).input(name, type, servt, visits, pop)) {
				long start = System.currentTimeMillis();
				solver.solve();
				System.out.print(algorithm+",");
				System.out.print(0+",");
				//System.out.println(algorithm);
				//System.out.println("-------------------------------------");
				mvaQueueLen = solver.getQueueLen();
				//System.out.println(queueLength(stations, classes, solver));
				long stop = System.currentTimeMillis();
				System.out.print(((stop-start)/(double)1000) + ",");
				//System.out.println("Elapsed time (ms) = " + (stop - start));
				start = 0; stop = 0;
				
				long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				solver = null;
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				System.gc(); System.gc(); System.gc(); System.gc();
				long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				
				System.out.print(((mem0-mem1)/(double)1024) + ",");
				System.out.print((mem0/(double)1024) + "\n");
	//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
	//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
	//			System.out.println("-------------------------------------");
				mem0 = 0; mem1 = 0;
			} else {
				System.out.println("Wrong input!!");
			}
		}
		
	}
	
	public void runTests3() {
		int queues = 5;
		int classes = 7;
		int[][] populations1 = {{10000, 5000, 1000, 500, 100, 50, 10}};
		//double[] tolerances = {Math.pow(10, -4), Math.pow(10, -7)};
		double[] tolerances = {Math.pow(10, -7)};
		
		// error = max_{c,k}((Q_{c,k} - Q*{c,k})/N_c)
		// Q* are the exact values
		
		double[][][] servt = new double[queues][classes][1];
		double[][] visits = new double[queues][classes];
		
		servt[0][0][0] = 64;
		servt[1][0][0] = 68;
		servt[2][0][0] = 28;
		servt[3][0][0] = 35;
		servt[4][0][0] = 10;
		
		servt[0][1][0] = 77;
		servt[1][1][0] = 70;
		servt[2][1][0] = 2;
		servt[3][1][0] = 97;
		servt[4][1][0] = 85;
		
		servt[0][2][0] = 93;
		servt[1][2][0] = 14;
		servt[2][2][0] = 68;
		servt[3][2][0] = 69;
		servt[4][2][0] = 88;
		
		servt[0][3][0] = 27;
		servt[1][3][0] = 22;
		servt[2][3][0] = 34;
		servt[3][3][0] = 15;
		servt[4][3][0] = 71;
		
		servt[0][4][0] = 19;
		servt[1][4][0] = 32;
		servt[2][4][0] = 22;
		servt[3][4][0] = 82;
		servt[4][4][0] = 62;
		
		servt[0][5][0] = 60;
		servt[1][5][0] = 2;
		servt[2][5][0] = 47;
		servt[3][5][0] = 83;
		servt[4][5][0] = 79;
		
		servt[0][6][0] = 8;
		servt[1][6][0] = 64;
		servt[2][6][0] = 81;
		servt[3][6][0] = 10;
		servt[4][6][0] = 98;
		
		int[] type = new int[queues];
		for (int k = 0; k < queues; k++) {
			type[k] = Solver.LI;
			for (int c = 0; c < classes; c++) {
				visits[k][c] = 1;
			}
		}
		
		for (int[] pop : populations1) {
			for (double tol : tolerances) {
				//System.out.println("##############################################");
				System.out.println(",,,,");
				System.out.print("Classes = " + classes);
				System.out.print(",Stations = " + queues);
				System.out.print(",Population = " + Arrays.toString(pop));
				System.out.print(",Tolerance = " + tol);
				System.out.println();
				System.out.println("Algorithm,Max error,Elapsed time,Memory usage,Memory usage wo diff");
				runTest2(classes, queues, pop, servt, visits, type, tol);
				System.out.println(",,,,");
				//System.out.println("\n##############################################\n");
			}
		}
	}

	public void runTests2() {
		int queues = 13;
		int classes = 5;
		int[][] populations1 = {{30, 10, 50, 40, 50}, {50, 30, 100, 70, 100}, {100, 50, 150, 50, 200}};
		int[][] populations2 = {{10000, 5000, 1000, 500, 100, 50, 10}};
		//double[] tolerances = {Math.pow(10, -4), Math.pow(10, -7)};
		double[] tolerances = {Math.pow(10, -7)};
		
		// error = max_{c,k}((Q_{c,k} - Q*{c,k})/N_c)
		// Q* are the exact values
		
		double[][][] servt = new double[queues][classes][1];
		double[][] visits = new double[queues][classes];
		
		for (int k = 0; k < 9; k++) {
			servt[k][0][0] = 12.98;
			servt[k][1][0] = 13.64;
			servt[k][2][0] = 2.64;
			servt[k][3][0] = 2.54;
			servt[k][4][0] = 24.22;
		}
		for (int k = 9; k < 11; k++) {
			servt[k][0][0] = 5.32;
			servt[k][1][0] = 5.18;
			servt[k][2][0] = 1.24;
			servt[k][3][0] = 1.04;
			servt[k][4][0] = 17.07;
		}
		servt[11][0][0] = 1.12;
		servt[11][1][0] = 1.27;
		servt[11][2][0] = 0.58;
		servt[11][3][0] = 0.03;
		servt[11][4][0] = 1.68;
		servt[12][0][0] = 2000;
		servt[12][1][0] = 2000;
		servt[12][2][0] = 2000;
		servt[12][3][0] = 2000;
		servt[12][4][0] = 3000;
		
		int[] type = new int[queues];
		for (int k = 0; k < queues; k++) {
			if (k == queues - 1) {
				type[k] = Solver.DELAY;
			}
			else {
				type[k] = Solver.LI;
			}
			for (int c = 0; c < classes; c++) {
				visits[k][c] = 1;
			}
		}
		
		for (int[] pop : populations1) {
			for (double tol : tolerances) {
				//System.out.println("##############################################");
				System.out.println(",,,,");
				System.out.print("Classes = " + classes);
				System.out.print(",Stations = " + queues);
				System.out.print(",Population = " + Arrays.toString(pop));
				System.out.print(",Tolerance = " + tol);
				System.out.println();
				System.out.println("Algorithm,Max error,Elapsed time,Memory usage,Memory usage wo diff");
				runTest2(classes, queues, pop, servt, visits, type, tol);
				System.out.println(",,,,");
				//System.out.println("\n##############################################\n");
			}
		}
	}

	private void runTest2(int classes, int stations, int[] pop, double[][][] servt, double[][] visits, int[] type, double tolerance) {
		String[] name = new String[stations];
		for (int i = 0; i < stations; i++) {
			name[i] = "Station " + i;
		}
		
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		
		SolverMulti solver = new SolverMultiClosedChow(classes, stations, pop);
		String algorithm = "Chow";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.println(algorithm);
			System.out.println(solver);
			
			System.out.print(algorithm+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
			
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		algorithm = "Bard-Schweitzer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.println(algorithm);
			System.out.println(solver);
			
			System.out.print(algorithm+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedAQL(classes, stations, pop);
		algorithm = "AQL";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.println(algorithm);
			System.out.println(solver);
			
			System.out.print(algorithm+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		algorithm = "Linearizer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.println(algorithm);
			System.out.println(solver);
			
			System.out.print(algorithm+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		algorithm = "DeSouza-Muntz Linearizer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.println(algorithm);
			System.out.println(solver);
			
			System.out.print(algorithm+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

	}	
	
	private double maxDiff(int[] pop, double[][] queueLen1, double[][] queueLen2) {
		double maxDiff = 0;
		for (int k = 0; k < queueLen1.length; k++) {
			for (int c = 0; c < queueLen1[0].length; c++) {
				double currDiff = Math.abs(queueLen1[k][c] - queueLen2[k][c])/(double)pop[c];
				if (currDiff > maxDiff) {
					maxDiff = currDiff;
				}
			}
		}
		return maxDiff;
	}
	
	private void runTest(int classes, int stations, int population, double tolerance) {
		int[] pop = new int[classes];
		String[] name = new String[stations];
		int[] type = new int[stations];
		for (int i = 0; i < stations; i++) {
			name[i] = "Station " + i;
			type[i] = Solver.LI;
		}
		for (int i = 0; i < classes; i++) {
			pop[i] = (int)Math.ceil(population/classes);
		}
		
		// service demands here
		double[][][] servt = new double[stations][classes][1];
		double[][] visits = new double[stations][classes];
		
		Random rand = new Random();
		for (int k = 0; k < stations; k++) {
			for (int c = 0; c < classes; c++) {
				servt[k][c][0] = rand.nextDouble() * 5;
				visits[k][c] = 1;
			}
		}
		
		/*
		Map<String, SolverMulti> models = new LinkedHashMap<String, SolverMulti>();
		Map<String, Long> elapsedTimes = new LinkedHashMap<String, Long>();
		Map<String, Long> memoryUsage = new LinkedHashMap<String, Long>();
		
		String algorithm1 = "MVA";
		String algorithm2 = "Chow";
		String algorithm3 = "Bard-Schweitzer";
		String algorithm4 = "AQL";
		String algorithm5 = "Linearizer";
		String algorithm6 = "DeSouza-Muntz Linearizer";
		
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(classes, stations);
		models.put(algorithm1, mClosedMVA);
		SolverMultiClosedChow mClosedChow = new SolverMultiClosedChow(classes, stations, pop);
		models.put(algorithm2, mClosedChow);
		SolverMultiClosedBardSchweitzer mClosedBS = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		models.put(algorithm3, mClosedBS);
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(classes, stations, pop);
		models.put(algorithm4, mClosedAQL);
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		models.put(algorithm5, mClosedLinearizer);
		SolverMultiClosedLinearizer mClosedDeSouzaMuntzLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		models.put(algorithm6, mClosedDeSouzaMuntzLinearizer);
		*/
		
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		System.gc(); System.gc(); System.gc(); System.gc();
		
		double[][] mvaQueueLen = null;
		
		SolverMulti solver = new SolverMultiClosedMVA(classes, stations);
		String algorithm = "MVA";
		if (((SolverMultiClosedMVA)solver).input(name, type, servt, visits, pop)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			System.out.print(0+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			mvaQueueLen = solver.getQueueLen();
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		solver = new SolverMultiClosedChow(classes, stations, pop);
		algorithm = "Chow";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		algorithm = "Bard-Schweitzer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedAQL(classes, stations, pop);
		algorithm = "AQL";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		algorithm = "Linearizer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		solver = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		algorithm = "DeSouza-Muntz Linearizer";
		if (solver.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			solver.solve();
			System.out.print(algorithm+",");
			System.out.print(maxDiff(pop, solver.getQueueLen(), mvaQueueLen)+",");
			//System.out.println(algorithm);
			//System.out.println("-------------------------------------");
			//System.out.println(queueLength(stations, classes, solver));
			long stop = System.currentTimeMillis();
			System.out.print(((stop-start)/(double)1000) + ",");
			//System.out.println("Elapsed time (ms) = " + (stop - start));
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			solver = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.print(((mem0-mem1)/(double)1024) + ",");
			System.out.print((mem0/(double)1024) + "\n");
//			System.out.println("Memory Usage (KB) = " + ((mem0 - mem1)/(double)1024));
//			System.out.println("Memory Usage w/o diff (KB) = " + ((mem0)/(double)1024));
//			System.out.println("-------------------------------------");
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}

		/*
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			long start = System.currentTimeMillis();
			mClosedMVA.solve();
			solver.solve();
			System.out.println(algorithm + "\n-------------------------------------");
			System.out.println(queueLength(stations, classes, solver));
			
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm6, stop - start);
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			mClosedMVA = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			memoryUsage.put(algorithm1, mem0-mem1);
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		if (mClosedChow.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			mClosedChow.solve();
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm6, stop - start);
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			mClosedChow = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			memoryUsage.put(algorithm1, mem0-mem1);
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		if (mClosedBS.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			mClosedBS.solve();
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm6, stop - start);
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			mClosedBS = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			memoryUsage.put(algorithm1, mem0-mem1);
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		if (mClosedAQL.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			mClosedAQL.solve();
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm6, stop - start);
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			mClosedAQL = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			memoryUsage.put(algorithm1, mem0-mem1);
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			mClosedLinearizer.solve();
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm6, stop - start);
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			mClosedLinearizer = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			memoryUsage.put(algorithm1, mem0-mem1);
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		if (mClosedDeSouzaMuntzLinearizer.input(name, type, servt, visits)) {
			long start = System.currentTimeMillis();
			mClosedDeSouzaMuntzLinearizer.solve();
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm6, stop - start);
			start = 0; stop = 0;
			
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			mClosedDeSouzaMuntzLinearizer = null;
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			memoryUsage.put(algorithm1, mem0-mem1);
			mem0 = 0; mem1 = 0;
		} else {
			System.out.println("Wrong input!!");
		}
		
		System.out.println("Elapsed times (ms): ");
		for (String algorithm : elapsedTimes.keySet()) {
			System.out.println("  " + algorithm + " - " + elapsedTimes.get(algorithm));
		}
		System.out.println("Memory usage (KB): ");
		for (String algorithm : memoryUsage.keySet()) {
			System.out.println("  " + algorithm + " - " + (memoryUsage.get(algorithm)/(double)1024));
		}
		
		System.out.println(toString(stations, classes, models));
		*/
	}
	
	private String queueLength(int stations, int classes, SolverMulti solver) {
		StringBuffer buf = new StringBuffer();
		buf.append("Queue Length (Q)");
		buf.append("\n------------------------------------\n");
		for (int m = 0; m < stations; m++) {
			buf.append("[   ");
			for (int c = 0; c < classes; c++) {
				buf.append(solver.getQueueLen(m, c) + "   ");
			}
			buf.append("]\n");
		}
		return buf.toString();
	}
	
	public static void testMultiClosedLI() {

		System.out.println("Closed system with 3 classes");
		long start = System.currentTimeMillis();

		String[] name = { "Station1", "Station2" };
		int[] type = { Solver.LI, Solver.LI, };
		double[][][] servt = new double[2][3][1];

		int[] pop = new int[3];
		pop[0] = 10;
		pop[1] = 10;
		pop[2] = 10;

		//station 1
		servt[0][0][0] = 10;
		servt[0][1][0] = 5;
		servt[0][2][0] = 1;

		//station 2
		servt[1][0][0] = 5;
		servt[1][1][0] = 9;
		servt[1][2][0] = 1;

		double[][] visits = new double[2][3];

		//station 1
		visits[0][0] = 1;
		visits[0][1] = 1;
		visits[0][2] = 1;

		//station 2
		visits[1][0] = 1;
		visits[1][1] = 1;
		visits[1][2] = 1;

		Map<String, SolverMulti> models = new LinkedHashMap<String, SolverMulti>();
		Map<String, Double> elapsedTimes = new LinkedHashMap<String, Double>();
		
		int stations = 2;
		int classes = 3;
		
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(classes, stations);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			String algorithm = "MVA";
			mClosedMVA.solve();
			models.put(algorithm, mClosedMVA);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedChow mClosedChow = new SolverMultiClosedChow(classes, stations, pop);
		if (mClosedChow.input(name, type, servt, visits)) {
			String algorithm = "Chow";
			mClosedChow.solve();
			models.put(algorithm, mClosedChow);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedBardSchweitzer mClosedBS = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		if (mClosedBS.input(name, type, servt, visits)) {
			String algorithm = "Bard-Schweitzer";
			mClosedBS.solve();
			models.put(algorithm, mClosedBS);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(classes, stations, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			String algorithm = "AQL";
			mClosedAQL.solve();
			models.put(algorithm, mClosedAQL);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Linearizer";
			mClosedLinearizer.solve();
			models.put(algorithm, mClosedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedOptimisedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		if (mClosedOptimisedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Optimised Linearizer";
			mClosedOptimisedLinearizer.solve();
			models.put(algorithm, mClosedOptimisedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		System.out.println("Elapsed times (s): ");
		for (String algorithm : elapsedTimes.keySet()) {
			System.out.println("  " + algorithm + " - " + elapsedTimes.get(algorithm));
		}
		
		System.out.println(toString(stations, classes, models));
		
/*		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(3, 2);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			mClosedMVA.solve();
			System.out.println(mClosedMVA);
			System.out.println();
			System.out.println("End Multi Closed LI - MVA");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(3, 2, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			mClosedAQL.solve();
			System.out.println(mClosedAQL);
			System.out.println();
			System.out.println("End Multi Closed LI - AQL");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(3, 2, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			mClosedLinearizer.solve();
			System.out.println(mClosedLinearizer);
			System.out.println();
			System.out.println("End Multi Closed LI - Linearizer");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}
*/	}


	public static void testMultiClosedLI_visits_not1() {

		System.out.println("Closed system with 2 classes - visits > 1");
		long start = System.currentTimeMillis();

		String[] name = { "Station1", "Station2" };
		int[] type = { Solver.LI, Solver.LI, };
		double[][][] servt = new double[2][2][1];

		int[] pop = new int[2];
		pop[0] = 100;
		pop[1] = 50;

		//station 1
		servt[0][0][0] = 10;
		servt[0][1][0] = 5;

		//station 2
		servt[1][0][0] = 5;
		servt[1][1][0] = 9;

		double[][] visits = new double[2][2];

		//station 1
		visits[0][0] = 3;
		visits[0][1] = 4;

		//station 2
		visits[1][0] = 2;
		visits[1][1] = 5;

		Map<String, SolverMulti> models = new LinkedHashMap<String, SolverMulti>();
		Map<String, Double> elapsedTimes = new LinkedHashMap<String, Double>();
		
		int stations = 2;
		int classes = 2;
		
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(classes, stations);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			String algorithm = "MVA";
			mClosedMVA.solve();
			models.put(algorithm, mClosedMVA);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedChow mClosedChow = new SolverMultiClosedChow(classes, stations, pop);
		if (mClosedChow.input(name, type, servt, visits)) {
			String algorithm = "Chow";
			mClosedChow.solve();
			models.put(algorithm, mClosedChow);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedBardSchweitzer mClosedBS = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		if (mClosedBS.input(name, type, servt, visits)) {
			String algorithm = "Bard-Schweitzer";
			mClosedBS.solve();
			models.put(algorithm, mClosedBS);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(classes, stations, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			String algorithm = "AQL";
			mClosedAQL.solve();
			models.put(algorithm, mClosedAQL);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Linearizer";
			mClosedLinearizer.solve();
			models.put(algorithm, mClosedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedOptimisedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		if (mClosedOptimisedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Optimised Linearizer";
			mClosedOptimisedLinearizer.solve();
			models.put(algorithm, mClosedOptimisedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		System.out.println("Elapsed times (s): ");
		for (String algorithm : elapsedTimes.keySet()) {
			System.out.println("  " + algorithm + " - " + elapsedTimes.get(algorithm));
		}
		
		System.out.println(toString(stations, classes, models));
		
		/*
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(2, 2);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			mClosedMVA.solve();
			System.out.println(mClosedMVA);
			System.out.println();
			System.out.println("End Multi Closed LI - MVA");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(2, 2, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			mClosedAQL.solve();
			System.out.println(mClosedAQL);
			System.out.println();
			System.out.println("End Multi Closed LI - AQL");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(2, 2, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			mClosedLinearizer.solve();
			System.out.println(mClosedLinearizer);
			System.out.println();
			System.out.println("End Multi Closed LI - Linearizer");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}
		*/
	}

	public static void testMultiClosedLI_big() {

		System.out.println("Closed system with 4 classes");
		long start = System.currentTimeMillis();

		String[] name = { "Station1", "Station2" };
		int[] type = { Solver.LI, Solver.LI, };
		double[][][] servt = new double[2][4][1];

		int[] pop = new int[4];
		pop[0] = 100;
		pop[1] = 100;
		pop[2] = 100;
		pop[3] = 100;

		//station 1
		servt[0][0][0] = 10;
		servt[0][1][0] = 5;
		servt[0][2][0] = 10;
		servt[0][3][0] = 5;

		//station 2
		servt[1][0][0] = 5;
		servt[1][1][0] = 9;
		servt[1][2][0] = 4;
		servt[1][3][0] = 5;

		double[][] visits = new double[2][4];

		//station 1
		visits[0][0] = 1;
		visits[0][1] = 1;
		visits[0][2] = 1;
		visits[0][3] = 1;

		//station 2
		visits[1][0] = 1;
		visits[1][1] = 1;
		visits[1][2] = 1;
		visits[1][3] = 1;

		Map<String, SolverMulti> models = new LinkedHashMap<String, SolverMulti>();
		Map<String, Double> elapsedTimes = new LinkedHashMap<String, Double>();
		
		int stations = 2;
		int classes = 4;
		
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(classes, stations);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			String algorithm = "MVA";
			mClosedMVA.solve();
			models.put(algorithm, mClosedMVA);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedChow mClosedChow = new SolverMultiClosedChow(classes, stations, pop);
		if (mClosedChow.input(name, type, servt, visits)) {
			String algorithm = "Chow";
			mClosedChow.solve();
			models.put(algorithm, mClosedChow);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedBardSchweitzer mClosedBS = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		if (mClosedBS.input(name, type, servt, visits)) {
			String algorithm = "Bard-Schweitzer";
			mClosedBS.solve();
			models.put(algorithm, mClosedBS);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(classes, stations, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			String algorithm = "AQL";
			mClosedAQL.solve();
			models.put(algorithm, mClosedAQL);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Linearizer";
			mClosedLinearizer.solve();
			models.put(algorithm, mClosedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedOptimisedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		if (mClosedOptimisedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Optimised Linearizer";
			mClosedOptimisedLinearizer.solve();
			models.put(algorithm, mClosedOptimisedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		System.out.println("Elapsed times (s): ");
		for (String algorithm : elapsedTimes.keySet()) {
			System.out.println("  " + algorithm + " - " + elapsedTimes.get(algorithm));
		}
		
		System.out.println(toString(stations, classes, models));

		/*
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(4, 2);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			mClosedMVA.solve();
			System.out.println(mClosedMVA);
			System.out.println();
			System.out.println("End Multi Closed LI - MVA");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(4, 2, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			mClosedAQL.solve();
			System.out.println(mClosedAQL);
			System.out.println();
			System.out.println("End Multi Closed LI - AQL");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(4, 2, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			mClosedLinearizer.solve();
			System.out.println(mClosedLinearizer);
			System.out.println();
			System.out.println("End Multi Closed LI - Linearizer");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}
		*/
	}

	public static void testLinearizerExample() {

		System.out.println("Closed system with 2 classes - Linearizer example from research paper");
		long start = System.currentTimeMillis();

		String[] name = { "Station1", "Station2" };
		int[] type = { Solver.DELAY, Solver.LI, };
		double[][][] servt = new double[2][2][1];

		int[] pop = new int[2];
		pop[0] = 8;
		pop[1] = 1;

		//station 1
		servt[0][0][0] = 8;
		servt[0][1][0] = 0;

		//station 2
		servt[1][0][0] = 1;
		servt[1][1][0] = 1;

		double[][] visits = new double[2][2];

		//station 1
		visits[0][0] = 1;
		visits[0][1] = 0;

		//station 2
		visits[1][0] = 1;
		visits[1][1] = 1;

		Map<String, SolverMulti> models = new LinkedHashMap<String, SolverMulti>();
		Map<String, Double> elapsedTimes = new LinkedHashMap<String, Double>();
		
		int stations = 2;
		int classes = 2;
		
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(classes, stations);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			String algorithm = "MVA";
			mClosedMVA.solve();
			models.put(algorithm, mClosedMVA);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedChow mClosedChow = new SolverMultiClosedChow(classes, stations, pop);
		if (mClosedChow.input(name, type, servt, visits)) {
			String algorithm = "Chow";
			mClosedChow.solve();
			models.put(algorithm, mClosedChow);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedBardSchweitzer mClosedBS = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		if (mClosedBS.input(name, type, servt, visits)) {
			String algorithm = "Bard-Schweitzer";
			mClosedBS.solve();
			models.put(algorithm, mClosedBS);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(classes, stations, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			String algorithm = "AQL";
			mClosedAQL.solve();
			models.put(algorithm, mClosedAQL);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Linearizer";
			mClosedLinearizer.solve();
			models.put(algorithm, mClosedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedOptimisedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		if (mClosedOptimisedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Optimised Linearizer";
			mClosedOptimisedLinearizer.solve();
			models.put(algorithm, mClosedOptimisedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		System.out.println("Elapsed times (s): ");
		for (String algorithm : elapsedTimes.keySet()) {
			System.out.println("  " + algorithm + " - " + elapsedTimes.get(algorithm));
		}
		
		System.out.println(toString(stations, classes, models));
		
		/*
		SolverMultiClosedMVA mClosedMVA = new SolverMultiClosedMVA(2, 2);
		if (mClosedMVA.input(name, type, servt, visits, pop)) {
			mClosedMVA.solve();
			System.out.println(mClosedMVA);
			System.out.println();
			System.out.println("End Multi Closed LI - MVA");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(2, 2, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			mClosedAQL.solve();
			System.out.println(mClosedAQL);
			System.out.println();
			System.out.println("End Multi Closed LI - AQL");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(2, 2, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			mClosedLinearizer.solve();
			System.out.println(mClosedLinearizer);
			System.out.println();
			System.out.println("End Multi Closed LI - Linearizer");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}
		*/
	}

	public static void testMultiClosedLI_huge() {

		System.out.println("Closed system with 6 classes");
		long start = System.currentTimeMillis();

		String[] name = new String[25];
		int[] type = new int[25];
        for (int i=0; i<25; i++) {
            name[i] = "Station"+i;
            type[i] = Solver.LI;
        }
		double[][][] servt = new double[25][6][1];
		double[][] visits = new double[25][6];

		int[] pop = new int[6];
        for (int r=0; r<6; r++) {
            pop[r] = 1000;
        }

        for (int m=0; m<12; m++) {
            servt[m][1][0] = 17.0;
            visits[m][1] = 1.0;
            servt[m][2][0] = 4.0;
            visits[m][2] = 1.0;
            servt[m][3][0] = 15.0;
            visits[m][3] = 1.0;
            servt[m][4][0] = 6.0;
            visits[m][4] = 1.0;
            servt[m][5][0] = 20.0;
            visits[m][5] = 1.0;
            servt[m][0][0] = 2.0;
            visits[m][0] = 1.0;
        }

        for (int m=12; m<17; m++) {
            servt[m][1][0] = 16.0;
            visits[m][1] = 1.0;
            servt[m][2][0] = 7.0;
            visits[m][2] = 1.0;
            servt[m][3][0] = 16.0;
            visits[m][3] = 1.0;
            servt[m][4][0] = 17.0;
            visits[m][4] = 1.0;
            servt[m][5][0] = 5.0;
            visits[m][5] = 1.0;
            servt[m][0][0] = 17.0;
            visits[m][0] = 1.0;
        }

        for (int m=17; m<25; m++) {
            servt[m][1][0] = 2.0;
            visits[m][1] = 1.0;
            servt[m][2][0] = 18.0;
            visits[m][2] = 1.0;
            servt[m][3][0] = 19.0;
            visits[m][3] = 1.0;
            servt[m][4][0] = 8.0;
            visits[m][4] = 1.0;
            servt[m][5][0] = 18.0;
            visits[m][5] = 1.0;
            servt[m][0][0] = 12.0;
            visits[m][0] = 1.0;
        }
                
		start = System.currentTimeMillis();
		
		Map<String, SolverMulti> models = new LinkedHashMap<String, SolverMulti>();
		Map<String, Double> elapsedTimes = new LinkedHashMap<String, Double>();
		
		int stations = 25;
		int classes = 6;
		
		SolverMultiClosedChow mClosedChow = new SolverMultiClosedChow(classes, stations, pop);
		if (mClosedChow.input(name, type, servt, visits)) {
			String algorithm = "Chow";
			mClosedChow.solve();
			models.put(algorithm, mClosedChow);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedBardSchweitzer mClosedBS = new SolverMultiClosedBardSchweitzer(classes, stations, pop);
		if (mClosedBS.input(name, type, servt, visits)) {
			String algorithm = "Bard-Schweitzer";
			mClosedBS.solve();
			models.put(algorithm, mClosedBS);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(classes, stations, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			String algorithm = "AQL";
			mClosedAQL.solve();
			models.put(algorithm, mClosedAQL);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Linearizer";
			mClosedLinearizer.solve();
			models.put(algorithm, mClosedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedOptimisedLinearizer = new SolverMultiClosedLinearizer(classes, stations, pop, true);
		if (mClosedOptimisedLinearizer.input(name, type, servt, visits)) {
			String algorithm = "Optimised Linearizer";
			mClosedOptimisedLinearizer.solve();
			models.put(algorithm, mClosedOptimisedLinearizer);
			long stop = System.currentTimeMillis();
			elapsedTimes.put(algorithm, ((stop - start)/(double)1000));
		} else {
			System.out.println("Wrong input!!");
		}
		
		System.out.println("Elapsed times (s): ");
		for (String algorithm : elapsedTimes.keySet()) {
			System.out.println("  " + algorithm + " - " + elapsedTimes.get(algorithm));
		}
		
		System.out.println(toString(stations, classes, models));
		
		/*
		SolverMultiClosedAQL mClosedAQL = new SolverMultiClosedAQL(6, 25, pop);
		if (mClosedAQL.input(name, type, servt, visits)) {
			mClosedAQL.solve();
			System.out.println(mClosedAQL);
			System.out.println();
			System.out.println("End Multi Closed LI - AQL");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}

		start = System.currentTimeMillis();
		SolverMultiClosedLinearizer mClosedLinearizer = new SolverMultiClosedLinearizer(6, 25, pop, false);
		if (mClosedLinearizer.input(name, type, servt, visits)) {
			mClosedLinearizer.solve();
			System.out.println(mClosedLinearizer);
			System.out.println();
			System.out.println("End Multi Closed LI - Linearizer");
			long stop = System.currentTimeMillis();
			System.out.print("Elapsed time (s): " + ((stop - start)/(double)1000));
			System.out.println();

		} else {
			System.out.println("Wrong input!!");
		}
		*/
	}

	public static String toString(int stations, int classes, Map<String, SolverMulti> models) {
		StringBuffer buf = new StringBuffer();
		buf.append("\n------------------------------------");
		buf.append("\nAnalysis with MVA Multiclass");

		buf.append("\n\n");
		buf.append("Queue Length (Q)");
		buf.append("\n------------------------------------\n");
		for (String algorithm : models.keySet()) {
			double[][] queueLen = models.get(algorithm).getQueueLen();
			
			buf.append("Algorithm: " + algorithm + "\n");
			for (int m = 0; m < stations; m++) {
				buf.append("[   ");
				for (int c = 0; c < classes; c++) {
					buf.append(Printer.print(queueLen[m][c], 6) + "   ");
				}
				buf.append("]\n");
			}
		}

		buf.append("\n\n");
		buf.append("Residence Time (R)");
		buf.append("\n------------------------------------\n");
		for (String algorithm : models.keySet()) {
			double[][] residenceTime = models.get(algorithm).getResTime();
			
			buf.append("Algorithm: " + algorithm + "\n");
			for (int m = 0; m < stations; m++) {
				buf.append("[   ");
				for (int c = 0; c < classes; c++) {
					buf.append(Printer.print(residenceTime[m][c], 6) + "   ");
				}
				buf.append("]\n");
			}
		}

		buf.append("\n\n");
		buf.append("Throughput (X)");
		buf.append("\n------------------------------------\n");
		for (String algorithm : models.keySet()) {
			double[][] throughput = models.get(algorithm).getThroughput();
			
			buf.append("Algorithm: " + algorithm + "\n");
			for (int m = 0; m < stations; m++) {
				buf.append("[   ");
				for (int c = 0; c < classes; c++) {
					buf.append(Printer.print(throughput[m][c], 6) + "   ");
				}
				buf.append("]\n");
			}
		}

		buf.append("\n\n");
		buf.append("Utilization (U)");
		buf.append("\n------------------------------------\n");
		for (String algorithm : models.keySet()) {
			double[][] utilization = models.get(algorithm).getUtilization();
			
			buf.append("Algorithm: " + algorithm + "\n");
			for (int m = 0; m < stations; m++) {
				buf.append("[   ");
				for (int c = 0; c < classes; c++) {
					buf.append(Printer.print(utilization[m][c], 6) + "   ");
				}
				buf.append("]\n");
			}
		}

		for (int i = 0; i < stations; i++) {
			buf.append("\n\nStation " + i + ": aggregated values");
			for (String algorithm : models.keySet()) {
				double[] scUtilization = models.get(algorithm).getAggrUtilization();
				double[] scThroughput = models.get(algorithm).getAggrThroughput();
				double[] scQueueLen = models.get(algorithm).getAggrQueueLen();
				double[] scResidTime = models.get(algorithm).getAggrResTime();
				
				buf.append("\nAlgorithm: " + algorithm);
				buf.append("\n  throughput       : " + scThroughput[i]);
				buf.append("\n  utilization      : " + scUtilization[i]);
				buf.append("\n  queue length     : " + scQueueLen[i]);
				buf.append("\n  residence time   : " + scResidTime[i]);
			}
		}
		for (int j = 0; j < classes; j++) {
			buf.append("\n\nClass " + j + ": aggregated values");
			for (String algorithm : models.keySet()) {
				double[] clsThroughput = models.get(algorithm).getClsThroughput();
				double[] clsRespTime = models.get(algorithm).getClsResTime();
				
				buf.append("\nAlgorithm: " + algorithm);
				buf.append("\n  response time       : " + clsRespTime[j]);
				buf.append("\n  throughput          : " + clsThroughput[j]);
			}
		}

		buf.append("\n\nSystem aggregate values");
		for (String algorithm : models.keySet()) {
			double sysResponseTime = models.get(algorithm).getTotResTime();
			double sysThroughput = models.get(algorithm).getTotThroughput();
			double sysNumJobs = 0;
			for (int k = 0; k < stations; k++) {
				sysNumJobs += models.get(algorithm).getAggrQueueLen(k);
			}
			
			buf.append("\nAlgorithm: " + algorithm);
			buf.append("\n  System Response Time    : " + sysResponseTime);
			buf.append("\n  System Throughput       : " + sysThroughput);
			buf.append("\n  System Number of jobs   : " + sysNumJobs);
		}

		return buf.toString();
	}
	
}
