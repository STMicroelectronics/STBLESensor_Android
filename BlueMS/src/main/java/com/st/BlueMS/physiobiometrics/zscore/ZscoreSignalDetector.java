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
    boolean firstBelow;
    int stateCount;

    public  enum StepState {
        LOOKING_FOR_STEP,
        HEEL_STRIKE,
        POTENTIAL_FLAT_FOOT,
        FLAT_FOOT,
        HEEL_OFF,
        SWING_PHASE,
    }

    StepState thisStepState;

    // CONSTANTS
    static 	int MAX_TIME = 100;
    static  int MIN_NUM_PEAKS_FOR_FLAT_FOOT = 3;
    static 	int SKIP_HEEL_OFF_SAMPLES = 15;
    static 	int SKIP_SWING_PHASE_SAMPLES = 5;
    static  double MIN_DEGREES_SEC_FOR_PEAK = 25.0;
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
        this.firstBelow = true;
		
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
            double z_i = data.get(i);
            // if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs(z_i) > MIN_DEGREES_SEC_FOR_PEAK &&
                    Math.abs((z_i - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {

                // this is a signal (i.e. peak), determine if it is a positive or negative signal
                if (z_i > avgFilter.get(i - 1)) {
                    signals.set(i, 1);
                } else {
                    signals.set(i, -1);
                }

                // filter this signal out using influence
                filteredData.set(i, (influence * z_i) + ((1 - influence) * filteredData.get(i - 1)));
            } else {
                // ensure this signal remains a zero
                signals.set(i, 0);
                // ensure this value is not filtered
                filteredData.set(i, z_i);
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
                this.firstBelow = true;
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
                    // Potential Peak.  we may have a FLAT-FOOT
                    // if signal negative for at least MIN_NUM_PEAKS_FOR_FLAT_FOOT
                    // if data is positive
                } else {
                    if (point > 0) {
                        stateCount = MIN_NUM_PEAKS_FOR_FLAT_FOOT;
                    } else {
                        stateCount = 1; // point has gone negative. definitely a heel_strike.
                    }
                    stepState = StepState.POTENTIAL_FLAT_FOOT;
                }
                break;
            // is this real or a false peak?
            case POTENTIAL_FLAT_FOOT:
                // if false peak, return to HEEL_STRIKE
                if (signal >= 0 ) {
                    stepState = StepState.HEEL_STRIKE;
                }
                stateCount--;
                if (stateCount == 0) {
                    heelPeak.set(i, maxStep);
                    maxStep = 0.0;
                    minToe = point;
                    goodStepFilter.set(i, flatFootValleyIndicator);
                    stepState = StepState.FLAT_FOOT;
                    if (point  < toeThreshold && this.firstBelow) {
                        if (beepSound > 0)
                            soundMgr.playSound(beepSound);
                        beep.set(i,-600.0);
                        this.firstBelow = false;
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
                    if (point < toeThreshold && this.firstBelow) {
                        if (beepSound > 0)
                            soundMgr.playSound(beepSound);
                        beep.set(i,point);
                        this.firstBelow = false;
                    }
                    // no. then go back and look for a step
                    // TODO transition the reset of gait
                } else {
                    toePeak.set(i, minToe);
                    minToe = 0.0;
                    stepState = StepState.HEEL_OFF;
                    stateCount = SKIP_HEEL_OFF_SAMPLES;
                }
                break;
            case HEEL_OFF:
                stateCount--;
                if (stateCount == 0) {
                    stepState = StepState.SWING_PHASE;
                    stateCount = SKIP_SWING_PHASE_SAMPLES;
                }
                break;
            case SWING_PHASE:
                stateCount--;
                if (stateCount == 0) {
                    stepState = StepState.LOOKING_FOR_STEP;
                }
                break;
        }
    	
    	return stepState;
    }
    
    
    // TODO  in-step timeout - is it needed? there will always be signal transitions.
    public int doSignal (Double point, int i)   {
    	 // if the distance between the current value and average is enough standard deviations (threshold) away
        if (Math.abs(point) > MIN_DEGREES_SEC_FOR_PEAK &&
                Math.abs((point - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {

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