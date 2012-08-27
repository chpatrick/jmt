package jmt.analytical;

import java.io.File;

import org.xml.sax.SAXException;

import jmt.common.exception.SolverException;
import jmt.framework.data.ArrayUtils;
import jmt.framework.xml.XMLUtils;
import jmt.gui.exact.ExactModel;

public class Moment {
	private static final boolean DEBUG = true;
	
	private ExactModel model;
	
	public boolean loadModel(File xmlFile) throws SolverException {
		model = new ExactModel();
		try {
			XMLUtils xmlUtils = new XMLUtils();
			if (!model.loadDocument(xmlUtils.loadXML(xmlFile))) {
				fail("Error loading model from tempfile", null);
			}
			return true;
		} catch (SAXException e) {
			fail("XML parse error in tempfile", e);
		} catch (Exception e) {
			fail("Error loading model from tempfile", e);
		}
		return false;
	}
	
	private void fail(String message, Throwable t) throws SolverException {
		if (DEBUG && t != null) {
			t.printStackTrace();
		}
		StringBuffer s = new StringBuffer(message);
		if (t != null) {
			s.append("\n");
			s.append(t.toString());
		}

		throw new SolverException(s.toString(), t);
	}

	public double getBinomialMoment(int[] replicas) throws SolverException {
		int count = 0;
		for (int i = 0; i < replicas.length; i++) {
			if (replicas[i] > 0) {
				count++;
			}
		}
		if (model.getClasses() < 2) {
			return getBinomialMomentSingleClass(replicas, count);
		}
		double binomialMoment = 1;
		MomentModel momentModel = new MomentModel(model);
		SolverMulti solver = getMultiSolver(momentModel);
		solver.solve();
		
		for (int i = 0; i < replicas.length; i++) {
			if (replicas[i] > 0) {
				count--;
				binomialMoment *= solver.getAggrQueueLen(i)+1;
				for (int j = 2; j <= replicas[i]; j++) {
					momentModel.addStationReplica(i);
					solver = getMultiSolver(momentModel);
					solver.solve();
					binomialMoment *= (solver.getAggrQueueLen(i)+1);
				}
				if (count > 0) {
					momentModel.addStationReplica(i);
					solver = getMultiSolver(momentModel);
					solver.solve();
				}
			}
		}
		return binomialMoment;
	}
	
	private double getBinomialMomentSingleClass(int[] replicas, int count) throws SolverException {
		double binomialMoment = 1;
		MomentModel momentModel = new MomentModel(model);
		Solver solver = getSingleSolver(momentModel);
		solver.solve();
		
		for (int i = 0; i < replicas.length; i++) {
			if (replicas[i] > 0) {
				count--;
				binomialMoment *= solver.getQueueLen(i)+1;
				for (int j = 2; j <= replicas[i]; j++) {
					momentModel.addStationReplica(i);
					solver = getSingleSolver(momentModel);
					solver.solve();
					binomialMoment *= (solver.getQueueLen(i)+1);
				}
				if (count > 0) {
					momentModel.addStationReplica(i);
					solver = getSingleSolver(momentModel);
					solver.solve();
				}
			}
		}
		return binomialMoment;
	}
	
	private Solver getSingleSolver(MomentModel model) throws SolverException {
		Solver solver = new SolverSingleClosedMVA(model.classPopulation[0], model.stations);
		
		double[][] serviceTimes = new double[model.serviceTimes.length][model.serviceTimes[0][0].length];
		double[] visits = new double[model.visits.length];
		
		for (int i = 0; i < serviceTimes.length; i++) {
			for (int j = 0; j < serviceTimes[0].length; j++) {
				serviceTimes[i][j] = model.serviceTimes[i][0][j];
			}
			visits[i] = model.visits[i][0];
		}
		
		int[] stationTypes = new int[model.stationTypes.length];
		for (int i = 0; i < stationTypes.length; i++) {
			if (model.stationTypes[i] == ExactModel.STATION_DELAY) {
				stationTypes[i] = Solver.DELAY;
			}
			else if (model.stationTypes[i] == ExactModel.STATION_LD) {
				stationTypes[i] = Solver.LD;
			}
			else {
				stationTypes[i] = Solver.LI;
			}
		}
		
		if (!solver.input(model.stationNames, stationTypes, serviceTimes, visits)) {
			String algName = SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER.toString().replace(" ", "").replace("-", "");
			fail("Error initializing " + algName + "MultiSolver", null);
		}
		return solver;
	}
	
	private SolverMulti getMultiSolver(MomentModel model) throws SolverException {
		SolverMulti solver;
		
		int[] stationTypes = new int[model.stationTypes.length];
		for (int i = 0; i < stationTypes.length; i++) {
			if (model.stationTypes[i] == ExactModel.STATION_DELAY) {
				stationTypes[i] = Solver.DELAY;
			}
			else if (model.stationTypes[i] == ExactModel.STATION_LD) {
				stationTypes[i] = Solver.LD;
			}
			else {
				stationTypes[i] = Solver.LI;
			}
		}
		
		if (model.classes < 4 && model.stations < 4) {
			SolverMultiClosedMVA closedsolver = new SolverMultiClosedMVA(model.classes, model.stations);
			if (!closedsolver.input(model.stationNames, stationTypes, model.serviceTimes, model.visits, model.classPopulation)) {
				fail("Error initializing MVAMultiSolver", null);
			}
			solver = closedsolver;
		} else {
			SolverMultiClosedLinearizer closerSolver = new SolverMultiClosedLinearizer(model.classes, model.stations, model.classPopulation, true);
			closerSolver.setTolerance(model.tolerance);
			
			if (!closerSolver.input(model.stationNames, stationTypes, model.serviceTimes, model.visits)) {
				String algName = SolverAlgorithm.DESOUZA_MUNTZ_LINEARIZER.toString().replace(" ", "").replace("-", "");
				fail("Error initializing " + algName + "MultiSolver", null);
			}
			solver = closerSolver;
		}
		return solver;
	}
	
	public double getFirstPowerMoment(double firstBinomialMoment) {
		return firstBinomialMoment - 1;
	}
	
	public double getSecondPowerMoment(double firstPowerMoment, double secondBinomialMoment) {
		return factorial(2)*secondBinomialMoment - 3*firstPowerMoment - 2;
	}
	
	public double getThirdPowerMoment(double firstPowerMoment, double secondPowerMoment, double thirdBinomialMoment) {
		return factorial(3)*thirdBinomialMoment - 6*secondPowerMoment - 11*firstPowerMoment - 6;
	}
	
	public double getVariance(double firstPowerMoment, double secondPowerMoment) {
		return secondPowerMoment - Math.pow(firstPowerMoment, 2);
	}
	
	public double getStandardDeviation(double variance) {
		return Math.sqrt(variance);
	}
	
	private int factorial(int n) {
		int result = 1;
		for (int i = 2; i <= n; i++) {
			result *= i;
		}
		return result;
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("\nPlease pass an input model file as argument.");
			return;
		}
		try {
			Moment moment = new Moment();
			moment.loadModel(new File(args[0]));
			
			int stations = moment.model.getStations();
			for (int k = 0; k < stations; k++) {
				int[] replicas = new int[stations];
				for (int r = 0; r < stations; r++) {
					if (r == k) {
						replicas[r] = 1;
					} else {
						replicas[r] = 0;
					}
				}
				double firstBinomial = moment.getBinomialMoment(replicas);
				
				replicas[k] = 2;
				double secondBinomial = moment.getBinomialMoment(replicas);
				
				double firstPower = moment.getFirstPowerMoment(firstBinomial);
				double secondPower = moment.getSecondPowerMoment(firstPower, secondBinomial);
				double variance = moment.getVariance(firstPower, secondPower);
				
				System.out.println("\nMean queue length at " + moment.model.getStationNames()[k] + ": " + firstPower);
				System.out.println("Variance of queue length at " + moment.model.getStationNames()[k] + ": " + variance);
			}
		} catch (SolverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class MomentModel {
		int classes;
		int stations;
		int[] classPopulation;
		String[] stationNames;
		int[] stationTypes;
		double[][][] serviceTimes;
		double[][] visits;
		double tolerance;
		
		public MomentModel(ExactModel model) {
			this.classes = model.getClasses();
			this.stations = model.getStations();
			this.classPopulation = ArrayUtils.toInt(model.getClassData());
			this.stationNames = ArrayUtils.copy(model.getStationNames());
			this.stationTypes = ArrayUtils.copy(model.getStationTypes());
			this.serviceTimes = ArrayUtils.copy3(model.getServiceTimes());
			this.visits = ArrayUtils.copy2(model.getVisits());
			this.tolerance = model.getTolerance();
		}
		
		public MomentModel(MomentModel model) {
			this.classes = model.classes;
			this.stations = model.stations;
			this.classPopulation = ArrayUtils.copy(model.classPopulation);
			this.stationNames = ArrayUtils.copy(model.stationNames);
			this.stationTypes = ArrayUtils.copy(model.stationTypes);
			this.serviceTimes = ArrayUtils.copy3(model.serviceTimes);
			this.visits = ArrayUtils.copy2(model.visits);
			this.tolerance = model.tolerance;
		}
		
		public void addStationReplica(int index) {
			stations++;
			String[] newStationNames = new String[stations];
			int[] newStationTypes = new int[stations];
			for (int i = 0; i < stations; i++) {
				if (i < stations-1) {
					newStationNames[i] = stationNames[i];
					newStationTypes[i] = stationTypes[i];
				} else {
					newStationNames[i] = stationNames[index];
					newStationTypes[i] = stationTypes[index];
				}
			}
			stationNames = newStationNames;
			stationTypes = newStationTypes;
			
			double[][][] newServiceTimes = new double[stations][classes][serviceTimes[0][0].length];
			double[][] newVisits = new double[stations][classes];
			for (int i = 0; i < stations; i++) {
				for (int j = 0; j < classes; j++) {
					for (int k = 0; k < serviceTimes[0][0].length; k++) {
						if (i < stations-1) {
							newServiceTimes[i][j][k] = serviceTimes[i][j][k];
							newVisits[i][j] = visits[i][j];
						} else {
							newServiceTimes[i][j][k] = serviceTimes[index][j][k];
							newVisits[i][j] = visits[index][j];
						}
					}
				}
			}
			serviceTimes = newServiceTimes;
			visits = newVisits;
		}
	}
	
}
