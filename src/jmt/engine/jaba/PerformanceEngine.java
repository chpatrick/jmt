package jmt.engine.jaba;

import java.util.ArrayList;
import java.util.Vector;

public class PerformanceEngine {

	private double[][][] serviceTimes;
	private Vector<Object> saturationSects;
	private String[] stationNames;

	public PerformanceEngine(double[][][] serviceTimes,
			Vector<Object> saturationSects, String[] stationNames) {
		this.serviceTimes = serviceTimes;
		this.saturationSects = saturationSects;
		this.stationNames = stationNames;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<DPoint>[] getUtilization() {
		ArrayList<DPoint>[] res = new ArrayList[serviceTimes.length];
		ArrayList<Double>[] coeff = calcNormalizedCoefficient();
		double[] previousEndPoints = calcFirstPreviousEndPoints();

		for (int i = 0; i < serviceTimes.length; i++) {
			res[i] = new ArrayList<DPoint>();
		}

		FinalSect2D sect;
		for (int i = 0; i < saturationSects.size(); i++) {
			sect = (FinalSect2D) saturationSects.get(i);
			if (sect.getBeta1() > sect.getBeta2()) { // it's a very strage
														// behaviour,
														// maybe there is a bug
														// in the engine sector.
				int saturatedRes = getSaturatedResource(sect);
				for (int j = 0; j < serviceTimes.length; j++) {
					if (j == saturatedRes) {
						res[j].add(new DPoint(sect.getBeta2(), 1));
						res[j].add(new DPoint(sect.getBeta1(), 1));
						previousEndPoints[j] = 1;
					} else {
						res[j].add(new DPoint(sect.getBeta2(),
								previousEndPoints[j]));
						double tmp = previousEndPoints[j]
								+ (sect.getBeta1() - sect.getBeta2())
								* coeff[saturatedRes].get(j);
						res[j].add(new DPoint(sect.getBeta1(), tmp));
						previousEndPoints[j] = tmp;
					}
				}
			} else {
				if (sect.countStation() > 1) {
					for (int j = 0; j < serviceTimes.length; j++) {
						res[j].add(new DPoint(sect.getBeta1(),
								previousEndPoints[j]));
						res[j].add(new DPoint(sect.getBeta2(),
								previousEndPoints[j]));
					}
				} else {
					int saturatedRes = getSaturatedResource(sect);
					for (int j = 0; j < serviceTimes.length; j++) {
						if (j == saturatedRes) {
							res[j].add(new DPoint(sect.getBeta1(), 1));
							res[j].add(new DPoint(sect.getBeta2(), 1));
							previousEndPoints[j] = 1;
						} else {
							res[j].add(new DPoint(sect.getBeta1(),
									previousEndPoints[j]));
							double tmp = previousEndPoints[j]
									+ (sect.getBeta2() - sect.getBeta1())
									* coeff[saturatedRes].get(j);
							res[j].add(new DPoint(sect.getBeta2(), tmp));
							previousEndPoints[j] = tmp;
						}
					}
				}
			}
		}
		return res;

	}

	private int getSaturatedResource(FinalSect2D sect) {
		for (int i = 0; i < stationNames.length; i++) {
			if (stationNames[i].equals(sect.getstation().get(0).getName()))
				return i;
		}
		return 0;
	}

	private double[] calcFirstPreviousEndPoints() {

		double endPoints[] = new double[serviceTimes.length];
		FinalSect2D sect = (FinalSect2D) saturationSects.get(0);

		int saturatedRes = getSaturatedResource(sect);

		for (int i = 0; i < serviceTimes.length; i++) {
			endPoints[i] = serviceTimes[i][1][0]
					/ serviceTimes[saturatedRes][1][0];
		}

		return endPoints;

	}

	private ArrayList<Double>[] calcNormalizedCoefficient() {

		ArrayList<Double> coeff[] = new ArrayList[serviceTimes.length];
		for (int i = 0; i < serviceTimes.length; i++) {
			coeff[i] = new ArrayList<Double>();
			for (int j = 0; j < serviceTimes.length; j++) {
				coeff[i].add(calcCoefficient(serviceTimes[j][1][0],
						serviceTimes[i][1][0], serviceTimes[j][0][0],
						serviceTimes[i][0][0]));
			}
		}
		return coeff;
	}

	private Double calcCoefficient(double sx, double sxBase, double dx,
			double dxBase) {
		return ((dx / dxBase) - (sx / sxBase));
	}

}
