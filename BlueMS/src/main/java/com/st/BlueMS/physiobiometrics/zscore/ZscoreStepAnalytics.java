package com.st.BlueMS.physiobiometrics.zscore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ZscoreStepAnalytics {
	ZscoreStepCalculations sc;

	private Double findDataInStep (List<Double> stepFilterList,
								   List<Double> markers, int i,  boolean greater) {
		int findIndex = i+1;
		boolean found = false;
		while (findIndex < stepFilterList.size() && stepFilterList.get(findIndex) == 0.0 && !found) {
			// find max
			if (greater && markers.get(findIndex) > 0) {
				return markers.get(findIndex);
				// else find min
			} else if (!greater && markers.get(findIndex) < 0) {
				return markers.get(findIndex);
			} else {
				findIndex++;
			}
		}
		return 0.0;
	}

	public void printStepCalculations () {
		System.out.println("fname = "+sc.fname);
		System.out.println("freqHZ = "+sc.freqHZ);
		System.out.println("footSwingThreshold = "+sc.footSwingThreshold);
		System.out.println("stepThreshold = "+sc.stepThreshold);
		System.out.println("signal lag " + sc.lag);
		System.out.println("signal threshold " + sc.threshold);
		System.out.println("signal influence " + sc.influence);
		System.out.println("timeOn " + sc.timeOn);
		System.out.println("timeWalk= "+sc.timeWalk);
		System.out.println("startWalking= "+sc.startWalking);
		System.out.println("stopWalking= "+sc.stopWalking);
		System.out.println("nSteps= "+ sc.nSteps);
		System.out.println("ngood= "+sc.ngood);
		System.out.println("nbad= "+ sc.nbad);
		System.out.println("pcgood= "+ sc.pcgood);
		System.out.println("pcbad= " + sc.pcbad);
		System.out.println("stepmeantime= "+sc.stepmeantime);
		System.out.println("cadmean= "+sc.cadmean);
		System.out.println("HeelStrikeAV= "+sc.HeelStrikeAV);
		System.out.println("HeelStrikeAVSTD= "+sc.HeelStrikeAVSTD);
		System.out.println("HeelStrikeAVCV= "+sc.HeelStrikeAVCV);
		System.out.println("FootSwingAV= "+sc.FootSwingAV);
		System.out.println("FootSwingAVSTD= "+sc.FootSwingAVSTD);
		System.out.println("FootSwingAVCV= "+sc.FootSwingAVCV);
	}



	public ZscoreStepCalculations signalAnalytics(ArrayList<Double> data, List<Integer> signalsList,
												  List<Double> stepFilterList, List<Double> heelPeakList, List<Double> goodStepFilterList,
												  List<Double> toePeakList, int samples, int freqHZ, int lag, Double stepThreshold) {

		sc = new ZscoreStepCalculations();
		DecimalFormat df2 = new DecimalFormat("#.##");
		sc.freqHZ = freqHZ;
		sc.rate = 1 / (double) freqHZ;
		double rateMilliseconds = sc.rate * 1000;
		double totalH2TAngularVelocity = 0;
		double totalHSAngularVelocity = 0;
		boolean firststep = true;
		double timestamp;

		List<ZscoreStepResults> allStepResults = new ArrayList<ZscoreStepResults>();

		for (int i = lag; i < data.size(); i++) {

			// find the start of walking
			if (stepFilterList.get(i) > 0) {
				ZscoreStepResults stepResults = new ZscoreStepResults();
				stepResults.timestamp = i;
				// process step
				sc.nSteps++;
				if (firststep) {
					sc.startWalking = i;
					firststep = false;
				}
				sc.stopWalking = i;

				Double minToe = findDataInStep (stepFilterList, toePeakList, i, false); // TODO CALC EVEN IF < THRESHOLD
				totalH2TAngularVelocity += minToe;
				stepResults.minH2TdegreesPerSecond = minToe;

				Double maxStep = findDataInStep (stepFilterList, heelPeakList, i, true);
				totalHSAngularVelocity += maxStep;
				stepResults.maxHSdegreesPerSecond = maxStep;

				if (minToe < stepThreshold) {
					sc.ngood++;
					stepResults.goodstep = true;
				} else {
					sc.nbad++;
					stepResults.badstep = true;
				}
				allStepResults.add(stepResults);
			}
		}
		int i = 1;
		boolean goodStep;
		for (ZscoreStepResults s : allStepResults) {
			goodStep = (s.minH2TdegreesPerSecond <= stepThreshold);
			System.out.println(i++ + "\t"+ s.timestamp + "\t"+ s.minH2TdegreesPerSecond + "\t"+ s.maxHSdegreesPerSecond + "\t" + goodStep);
		}
		sc.timeOn = (double) data.size() * sc.rate;
		sc.timeWalk = (sc.stopWalking - sc.startWalking) * sc.rate;
		sc.totalwalkingtime = sc.timeWalk;
		if (sc.nSteps > 0) {
			sc.stepmeantime = sc.timeWalk / (double) (sc.nSteps-1);
			sc.cadmean = 120 / sc.stepmeantime;
			sc.pcbad =  (double) sc.nbad/ (double) sc.nSteps;
			sc.pcgood =  (double) sc.ngood/ (double) sc.nSteps;
			sc.FootSwingAV = totalHSAngularVelocity / (double) sc.nSteps;
			sc.HeelStrikeAV = totalH2TAngularVelocity / (double) sc.nSteps;
			int index = 0;
			while (index < allStepResults.size()) {
				ZscoreStepResults step = allStepResults.get(index);
				double differenceH2T = step.minH2TdegreesPerSecond - sc.HeelStrikeAV;
				sc.HeelStrikeAVSTD += Math.pow(differenceH2T, 2);

				double differenceHS = step.maxHSdegreesPerSecond - sc.FootSwingAV;
				sc.FootSwingAVSTD += Math.pow(differenceHS, 2);

				index++;
			}
			sc.HeelStrikeAVSTD = sc.HeelStrikeAVSTD/(double) sc.nSteps;
			sc.HeelStrikeAVSTD = Math.sqrt(sc.HeelStrikeAVSTD);
			sc.HeelStrikeAVCV =  sc.HeelStrikeAVSTD/sc.HeelStrikeAV;

			sc.FootSwingAVSTD = sc.FootSwingAVSTD/(double) sc.nSteps;
			sc.FootSwingAVSTD = Math.sqrt(sc.FootSwingAVSTD);
			sc.FootSwingAVCV =  sc.FootSwingAVSTD/sc.FootSwingAV;
		}
		return sc;
	}
}
