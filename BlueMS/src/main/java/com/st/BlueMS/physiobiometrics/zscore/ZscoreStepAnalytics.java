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
		DecimalFormat df2 = new DecimalFormat("#.##");
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
		System.out.println("pcgood= "+ df2.format(sc.pcgood));
		System.out.println("pcbad= " + df2.format(sc.pcbad));
		System.out.println("stepmeantime= "+ df2.format(sc.stepmeantime));
		System.out.println("cadmean= "+ df2.format(sc.cadmean));
		System.out.println("HeelStrikeAV= "+ df2.format(sc.HeelStrikeAV));
		System.out.println("HeelStrikeAVSTD= "+ df2.format(sc.HeelStrikeAVSTD));
		System.out.println("HeelStrikeAVCV= "+ df2.format(sc.HeelStrikeAVCV));
		System.out.println("FootSwingAV= "+ df2.format(sc.FootSwingAV));
		System.out.println("FootSwingAVSTD= "+ df2.format(sc.FootSwingAVSTD));
		System.out.println("FootSwingAVCV= "+ df2.format(sc.FootSwingAVCV));
	}

	public void printStepResults () {
		DecimalFormat df2 = new DecimalFormat("#.##");
		boolean goodStep;
		int i = 0;
		for (ZscoreStepResults s : sc.allStepResults) {
			goodStep = (s.minHeelStrike <= sc.stepThreshold);
			System.out.println(i++ + "\t"+  df2.format(s.timestamp) + "\t"+ df2.format(s.minHeelStrike) + "\t"+ df2.format(s.maxFootSwing) + "\t" + goodStep);
		}
	}

	public ZscoreStepCalculations signalAnalytics(ArrayList<Double> data, List<Integer> signalsList,
												  List<Double> stepFilterList, List<Double> FootSwingPeak, List<Double> goodStepFilterList,
												  List<Double> HeelStrikeValley, int samples, int freqHZ, int lag, Double stepThreshold) {

		sc = new ZscoreStepCalculations();
		sc.freqHZ = freqHZ;
		sc.rate = 1 / (double) freqHZ;
		double totalHeelStrikeAngularVelocity = 0;
		double totalFootSwingAngularVelocity = 0;
		boolean firststep = true;

		sc.allStepResults = new ArrayList<ZscoreStepResults>();

		for (int i = lag; i < data.size(); i++) {

			// find the start of walking
			if (stepFilterList.get(i) > 0) {

				Double minHeelStrike = findDataInStep(stepFilterList, HeelStrikeValley, i, false); // TODO CALC EVEN IF
				// < THRESHOLD
				Double maxFootSwing = findDataInStep(stepFilterList, FootSwingPeak, i, true);

				if (minHeelStrike < 0) {
					ZscoreStepResults stepResults = new ZscoreStepResults();
					stepResults.timestamp = i;
					// process step
					sc.nSteps++;
					if (firststep) {
						sc.startWalking = i;
						firststep = false;
					}
					sc.stopWalking = i;

					totalHeelStrikeAngularVelocity += minHeelStrike;
					stepResults.minHeelStrike = minHeelStrike;
					totalFootSwingAngularVelocity += maxFootSwing;
					stepResults.maxFootSwing = maxFootSwing;

					if (minHeelStrike < stepThreshold) {
						sc.ngood++;
						stepResults.goodstep = true;
					} else {
						sc.nbad++;
						stepResults.badstep = true;
					}
					sc.allStepResults.add(stepResults);
				}
			}
		}
		sc.timeOn = (double) data.size() * sc.rate;
		sc.timeWalk = (sc.stopWalking - sc.startWalking) * sc.rate;
		sc.totalwalkingtime = sc.timeWalk;
		if (sc.nSteps > 0) {
			sc.stepmeantime = sc.timeWalk / (double) (sc.nSteps - 1);
			sc.cadmean = 120 / sc.stepmeantime;
			sc.pcbad = (double) sc.nbad / (double) sc.nSteps;
			sc.pcgood = (double) sc.ngood / (double) sc.nSteps;
			sc.FootSwingAV = totalFootSwingAngularVelocity / (double) sc.nSteps;
			sc.HeelStrikeAV = totalHeelStrikeAngularVelocity / (double) sc.nSteps;
			int index = 0;
			while (index < sc.allStepResults.size()) {
				ZscoreStepResults step = sc.allStepResults.get(index);
				double differenceHeelStrike = step.minHeelStrike - sc.HeelStrikeAV;
				sc.HeelStrikeAVSTD += Math.pow(differenceHeelStrike, 2);

				double differenceFootSwing = step.maxFootSwing - sc.FootSwingAV;
				sc.FootSwingAVSTD += Math.pow(differenceFootSwing, 2);

				index++;
			}
			sc.HeelStrikeAVSTD = sc.HeelStrikeAVSTD / (double) sc.nSteps;
			sc.HeelStrikeAVSTD = Math.sqrt(sc.HeelStrikeAVSTD);
			sc.HeelStrikeAVCV = sc.HeelStrikeAVSTD / sc.HeelStrikeAV;

			sc.FootSwingAVSTD = sc.FootSwingAVSTD / (double) sc.nSteps;
			sc.FootSwingAVSTD = Math.sqrt(sc.FootSwingAVSTD);
			sc.FootSwingAVCV = sc.FootSwingAVSTD / sc.FootSwingAV;
		}
		return sc;
	}
}
