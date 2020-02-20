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
    	System.out.println("frequency = "+sc.frequency);
    	System.out.println("rate = "+sc.rate);
    	System.out.println("goodsteps= "+sc.goodsteps);
    	System.out.println("badsteps= "+ sc.badsteps);
    	System.out.println("nSteps= "+ sc.nSteps);
    	System.out.println("totalTime " + sc.totalTime);
    	System.out.println("goodpercent= "+ sc.goodpercent);
    	System.out.println("badpercent= " + sc.badpercent);
    	System.out.println("stepwalkingtime= "+sc.stepwalkingtime);
    	System.out.println("totalwalkingtime= "+sc.totalwalkingtime);
    	System.out.println("startWalking= "+sc.startWalking);
    	System.out.println("stopWalking= "+sc.stopWalking);
    	
    	System.out.println("avgstep= "+sc.avgstep);
    	System.out.println("avgCadence= "+sc.avgCadence);
    	System.out.println("meanH2TAngularVelocity= "+sc.meanH2TAngularVelocity);
    	System.out.println("stdH2TAngularVelocity= "+sc.stdH2TAngularVelocity);
    	System.out.println("H2TcoefVariation= "+sc.H2TcoefVariation);
    	System.out.println("meanHSAngularVelocity= "+sc.meanHSAngularVelocity);
    	System.out.println("stdHSAngularVelocity= "+sc.stdHSAngularVelocity);
    	System.out.println("HScoefVariation= "+sc.HScoefVariation);
    }
    
    

    public ZscoreStepCalculations signalAnalytics(ArrayList<Double> data, List<Integer> signalsList, 
    		List<Double> stepFilterList, List<Double> heelPeakList, List<Double> goodStepFilterList, 
    		List<Double> toePeakList, int samples, int frequency, int lag, Double stepThreshold) {
    	
    	sc = new ZscoreStepCalculations();
        DecimalFormat df2 = new DecimalFormat("#.##");
        sc.frequency = frequency;
        sc.rate = 1 / (double) frequency;
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
        			sc.goodsteps++;
        			stepResults.goodstep = true;
        		} else {
        			sc.badsteps++;
        			stepResults.badstep = true;
        		}	
        		allStepResults.add(stepResults);
        	}
        }       
        sc.totalTime = (double) data.size() * sc.rate;
        sc.stepwalkingtime = (sc.stopWalking - sc.startWalking) * sc.rate;
        sc.totalwalkingtime = sc.stepwalkingtime;
        if (sc.nSteps > 0) {
            sc.avgstep = sc.stepwalkingtime / (double) (sc.nSteps-1);
            sc.avgCadence = 120 / sc.avgstep;
            sc.badpercent =  (double) sc.badsteps/ (double) sc.nSteps;
            sc.goodpercent =  (double) sc.goodsteps/ (double) sc.nSteps;
            sc.meanHSAngularVelocity = totalHSAngularVelocity / (double) sc.nSteps;
            sc.meanH2TAngularVelocity = totalH2TAngularVelocity / (double) sc.nSteps;
            int index = 0;
            while (index < allStepResults.size()) {
                ZscoreStepResults step = allStepResults.get(index);
                double differenceH2T = step.minH2TdegreesPerSecond - sc.meanH2TAngularVelocity;
                sc.stdH2TAngularVelocity += Math.pow(differenceH2T, 2);
                
                double differenceHS = step.maxHSdegreesPerSecond - sc.meanHSAngularVelocity;
                sc.stdHSAngularVelocity += Math.pow(differenceHS, 2);
                
                index++;
            }
            sc.stdH2TAngularVelocity = sc.stdH2TAngularVelocity/(double) sc.nSteps;
            sc.stdH2TAngularVelocity = Math.sqrt(sc.stdH2TAngularVelocity);
            sc.H2TcoefVariation =  sc.stdH2TAngularVelocity/sc.meanH2TAngularVelocity;
            
            sc.stdHSAngularVelocity = sc.stdHSAngularVelocity/(double) sc.nSteps;
            sc.stdHSAngularVelocity = Math.sqrt(sc.stdHSAngularVelocity);
            sc.HScoefVariation =  sc.stdHSAngularVelocity/sc.meanHSAngularVelocity;
        }
        return sc;
    }
}
