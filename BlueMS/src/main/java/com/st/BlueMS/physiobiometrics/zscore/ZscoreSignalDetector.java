package com.st.BlueMS.physiobiometrics.zscore;
import com.st.BlueMS.physiobiometrics.Sound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ZscoreSignalDetector {
	int lag; 
	Double threshold; 
	Double influence;
	ZscoreStatistics myStats;
    // the results (peaks, 1 or -1) of our algorithm
    List<Integer> signals;

    // filter out the signals (peaks) from our original list (using influence arg)
    List<Double> filteredData;

    // the current average of the rolling window
    List<Double> avgFilter;

    // the current standard deviation of the rolling window
    List<Double> stdFilter;
    
    List<Double> goodStepFilter;

    List<Double> beep;
    
    List<Double> stepFilter;
    List<Double> heelPeak;
    List<Double> toePeak;    
    Double lastPeakStatus;
    Double stepThreshold;
    Double toeThreshold;
    Double flatFootValleyIndicator;
    Double maxStep;
    Double minToe;    
    int lastSignal;
    
    boolean foundStepStart;
    boolean lookForNextStep;
    boolean foundToeDown;
    boolean inToeDown;
    boolean inStep;
    Sound soundMgr;
    int beepSound;
    
    public enum StepState {
    	LOOKING_FOR_STEP,
    	HEEL_STRIKE,
    	FLAT_FOOT,
    	HEEL_OFF,
    	SWING_PHASE,  	
    }
    
    StepState thisStepState;
    
    static 	int MAX_TIME = 100;
    int inStepTimeout; 
    
    /*
     *  Heel Strike (HS);
     *  loading response phase or Flat Foot (FF);
     *  heel lifting or Heel-Off (HO);  
     *  initial Swing Phase (SP) or Toe-Off (TO)
     *  
     *  
     */
    
	
	public ZscoreSignalDetector(int lag, Double threshold, Double influence, int dataSize, Double stepThreshold, Double toeThreshold,
                         Sound soundMgr,int beepSound) {
		this.lag = lag;
		this.threshold = threshold;
		this.influence = influence;
		this.stepThreshold = stepThreshold;
		this.toeThreshold = toeThreshold;
		this.soundMgr = soundMgr;
		this.beepSound = beepSound;
		lastSignal = 0;
        this.flatFootValleyIndicator = -100.0;
		
		myStats = new ZscoreStatistics(lag);
        // the results (peaks, 1 or -1) of our algorithm
        signals = new ArrayList<Integer>(Collections.nCopies(dataSize, 0));

        // filter out the signals (peaks) from our original list (using influence arg)
        filteredData = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));

        // the current average of the rolling window
        avgFilter = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));

        // the current standard deviation of the rolling window
        stdFilter = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        
        goodStepFilter = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        stepFilter = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        heelPeak = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        toePeak = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        beep = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        
        foundStepStart = false;
        foundToeDown = false;
        lookForNextStep = true;
        inStep = false;
        inToeDown = false;
        maxStep = 0.0;
        minToe = 0.0;
        
        thisStepState = StepState.LOOKING_FOR_STEP;
        inStepTimeout = 0;
        
        // init avgFilter and stdFilter
        
        double mean = myStats.getMean();
        avgFilter.set(lag - 1, mean);
        stdFilter.set(lag - 1, Math.sqrt(myStats.getVariance(mean))); // getStandardDeviation() uses sample variance
	}
	
	
	
    public HashMap<String, List> analyzeDataForSignals(List<Double> data) {

        // loop input starting at end of rolling window
        for (int i = lag; i < data.size(); i++) {

            // if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs((data.get(i) - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {

                // this is a signal (i.e. peak), determine if it is a positive or negative signal
                if (data.get(i) > avgFilter.get(i - 1)) {
                    signals.set(i, 1);
                } else {
                    signals.set(i, -1);
                }

                // filter this signal out using influence
                filteredData.set(i, (influence * data.get(i)) + ((1 - influence) * filteredData.get(i - 1)));
            } else {
                // ensure this signal remains a zero
                signals.set(i, 0);
                // ensure this value is not filtered
                filteredData.set(i, data.get(i));
            }

            // update rolling average and deviation
            
            myStats.setLagArray((ArrayList<Double>) filteredData, i);
            double mean = myStats.getMean();
            avgFilter.set(i, mean);
            stdFilter.set(i, Math.sqrt(myStats.getVariance(mean))); // getStandardDeviation() uses sample variance
        }

        HashMap<String, List> returnMap = new HashMap<String, List>();
        returnMap.put("signals", signals);
        returnMap.put("filteredData", filteredData);
        returnMap.put("avgFilter", avgFilter);
        returnMap.put("stdFilter", stdFilter);

        return returnMap;
 
    } // end
    
    public StepState doStepDetect (Double point, int i, int signal, StepState stepState)   {
    	
    	switch (stepState) {
    	case LOOKING_FOR_STEP:
    		if (signal > 0 && point > stepThreshold) {
    			stepFilter.set(i, stepThreshold);
    			maxStep = point;
    			stepState = StepState.HEEL_STRIKE;
    		}
    		break;
    	case HEEL_STRIKE:
    		// do we still have a PEAK?
    		if (signal >= 0 ) {
    			if (point > maxStep) {
                 	maxStep = point;
                 }
    		// no. then we calculate max and transition to FLAT-FOOT	
    		} else {
    			heelPeak.set(i, maxStep);
        		maxStep = 0.0;
        		minToe = point;
        		goodStepFilter.set(i, flatFootValleyIndicator);
        		stepState = StepState.FLAT_FOOT;
        		if (point < toeThreshold) {
        		    beep.set(i,point);
        		    if (beepSound > 0)
        		        soundMgr.playSound(beepSound);
                }
    		}		
    		break;
    	case FLAT_FOOT:
    		// do we still have a FLAT FOOT?
    		if (signal < 0 ) {
    			if (point < minToe) {
    				goodStepFilter.set(i, flatFootValleyIndicator);
    				minToe = point;
                 }
                if (point < toeThreshold) {
                    beep.set(i,point);
                    soundMgr.playSound(beepSound);
                }
    		// no. then go back and look for a step
    		// TODO transition the reset of gait
    		} else {
    			toePeak.set(i, minToe);
    			minToe = 0.0;
        		stepState = StepState.LOOKING_FOR_STEP;
    		}		
    		break;
    	case HEEL_OFF:
    		break;
    	case SWING_PHASE:
    		break;
    	}
    	
    	return stepState;
    }
    
    
    // TODO  in-step timeout - is it needed? there will always be signal transitions.
    public int doSignal (Double point, int i)   {
    	 // if the distance between the current value and average is enough standard deviations (threshold) away
        if (Math.abs((point - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {

            // this is a signal (i.e. peak), determine if it is a positive or negative signal
            if (point > avgFilter.get(i - 1)) {
                signals.set(i, stepThreshold.intValue());        
            } else {
                signals.set(i, toeThreshold.intValue());
            }

            // filter this signal out using influence
            filteredData.set(i, (influence * point) + ((1 - influence) * filteredData.get(i - 1)));
        } else {
            // ensure this signal remains a zero
            signals.set(i, 0);
            // ensure this value is not filtered
            filteredData.set(i, point);
        } 
        // update rolling average and deviation
        
        myStats.setLagArray((ArrayList<Double>) filteredData, i);
        double mymean = myStats.getMean();
        avgFilter.set(i, mymean);
        stdFilter.set(i, Math.sqrt(myStats.getVariance(mymean))); // getStandardDeviation() uses sample variance
        
        return signals.get(i);
    }

    public HashMap<String, List> getDataForH2T() {

        HashMap<String, List> returnMap = new HashMap<String, List>();
        returnMap.put("signals", signals);
        returnMap.put("filteredData", filteredData);
        returnMap.put("avgFilter", avgFilter);
        returnMap.put("stdFilter", stdFilter);
        returnMap.put("goodStepFilter", goodStepFilter);
        returnMap.put("stepFilter", stepFilter);
        returnMap.put("heelPeak", heelPeak);
        returnMap.put("toePeak", toePeak);
        returnMap.put("beep", beep);

        return returnMap;

    } // end


    public HashMap<String, List> analyzeDataForH2T(List<Double> data) {
    	
        // loop input starting at end of rolling window
        for (int i = lag; i < data.size(); i++) {     	
        	int signal = doSignal(data.get(i), i);
        	thisStepState = doStepDetect(data.get(i), i, signal, thisStepState);
        }

        HashMap<String, List> returnMap = new HashMap<String, List>();
        returnMap.put("signals", signals);
        returnMap.put("filteredData", filteredData);
        returnMap.put("avgFilter", avgFilter);
        returnMap.put("stdFilter", stdFilter);
        returnMap.put("goodStepFilter", goodStepFilter);
        returnMap.put("stepFilter", stepFilter);
        returnMap.put("heelPeak", heelPeak);
        returnMap.put("toePeak", toePeak);
        returnMap.put("beep", beep);

        return returnMap;

    } // end
}