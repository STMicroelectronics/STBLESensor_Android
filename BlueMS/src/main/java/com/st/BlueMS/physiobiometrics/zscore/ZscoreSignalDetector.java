/* uncomment for android */
package com.st.BlueMS.physiobiometrics.zscore;
import com.st.BlueMS.physiobiometrics.Sound;
/**/

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ZscoreSignalDetector {
    /* uncomment for android */
    Sound soundMgr;
    int beepSound;
    /* */
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
    List<Double> HeelStrikeValley;
    List<Double> maxFootSwings;
    List<Double> heelOffPower;
    List<Double> startSwing;

    Double lastPeakStatus;
    Double stepThreshold;
    Double goodStepThreshold;
    Double maxFootSwing;
    Double minHeelValley;
    Double heelOffPowerMin;
    int lastSignal;
    int stateCount;

    boolean foundStepStart;
    boolean lookForNextStep;
    boolean foundToeDown;
    boolean inToeDown;
    boolean inStep;
    boolean firstBelow;
    boolean firstBeep;
    boolean unknownData;

    public enum StepState {
        LOOKING_FOR_STEP, FOOT_SWING, POTENTIAL_HEEL_STRIKE, HEEL_STRIKE, FLAT_FOOT, HEEL_OFF,
    }

    StepState thisStepState;

    // CONSTANTS
    static double HEEL_STRIKE_VALLEY_INDICATOR = -100.0;
    static double BEEP_INDICATOR = -600.0;
    static int MAX_TIME = 100;
    static int MIN_NUM_PEAKS_FOR_HEEL_STRIKE = 3;
    static int SKIP_FLAT_FOOT_SAMPLES = 15;
    static int SKIP_HEEL_OFF_SAMPLES = 5;
    static double MIN_DEGREES_SEC_FOR_PEAK = 25.0;
    int inStepTimeout;

    /*
     * Heel Strike (HS); loading response phase or Flat Foot (FF); heel lifting or
     * Heel-Off (HO); initial Swing Phase (SP) or Toe-Off (TO)
     *
     *
     */

    public ZscoreSignalDetector(int lag, Double threshold, Double influence, int dataSize,
                                Double stepThreshold,
                                //Double goodStepThreshold) {
		/* uncomment for android */
		Double goodStepThreshold, Sound soundMgr,int beepSound) {
		this.soundMgr = soundMgr;
		this.beepSound = beepSound;
		/* */
        this.lag = lag;
        this.threshold = threshold;
        this.influence = influence;
        this.stepThreshold = stepThreshold;
        this.goodStepThreshold = goodStepThreshold;
        lastSignal = 0;

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
        HeelStrikeValley = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        beep = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        heelOffPower = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        startSwing = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));
        maxFootSwings = new ArrayList<Double>(Collections.nCopies(dataSize, 0.0));

        foundStepStart = false;
        foundToeDown = false;
        lookForNextStep = true;
        inStep = false;
        inToeDown = false;
        maxFootSwing = 0.0;
        minHeelValley = 0.0;
        heelOffPowerMin = 0.0;

        thisStepState = StepState.LOOKING_FOR_STEP;
        inStepTimeout = 0;

        // init avgFilter and stdFilter

        double mean = myStats.getMean();
        avgFilter.set(lag - 1, mean);
        stdFilter.set(lag - 1, Math.sqrt(myStats.getVariance(mean))); // getStandardDeviation() uses sample variance
    }

    public StepState doStepDetect(Double point, int i, int signal, StepState stepState, boolean debug) {
        DecimalFormat df = new DecimalFormat("#0.000");
        /*
         * if (debug) { System.out.println(stepState.name() + " signal: " +
         * df.format(signal) + " point: " + df.format(point)); }
         */
        switch (stepState) {
            case LOOKING_FOR_STEP:
                firstBelow = true;
                firstBeep = true;

                // we need to capture the 1st 0 crossing
                if ((signal > 0) && (point >= 0) &&
                        (startSwing.get(i - 1) == 0) &&
                        (startSwing.get(i - 2) == 0) &&
                        (startSwing.get(i - 3) == 0)) {
                    startSwing.set(i, point );
                }

                if (signal > 0 && point > stepThreshold) {
                    stepFilter.set(i, stepThreshold);
                    maxFootSwing = point;
                    stepState = StepState.FOOT_SWING;
                }
                break;

            case FOOT_SWING:
                // do we still have a PEAK?
                if (signal > 0) {
                    if (point > maxFootSwing) {
                        maxFootSwing = point;
                    }
                    // Potential valley. we may have a HEEL_STRIKE
                } else {
                    if (point < 0) {
                        if (signal < 0) {
                            stepState = StepState.HEEL_STRIKE;
                        } else {
                            stepState = StepState.POTENTIAL_HEEL_STRIKE;
                        }
                    }
                }
                break;

            // is this real or a false peak?
            case POTENTIAL_HEEL_STRIKE:
                // if false peak, return to FOOT_SWING
                if (signal >= 0 && point > 0) {
                    stepState = StepState.FOOT_SWING;
                } else {
                    stepState = StepState.HEEL_STRIKE;
                    minHeelValley = point;
                    goodStepFilter.set(i, HEEL_STRIKE_VALLEY_INDICATOR);
                }
                break;

            case HEEL_STRIKE:
                if (firstBelow) {
                    firstBelow = false;
                    maxFootSwings.set(i, maxFootSwing);
                    if (debug) {
                        System.out.println("FOOT SWING" + " timestamp: " + df.format(i) +
                                " maxFootSwing: " + df.format(maxFootSwing));
                    }
                    maxFootSwing = 0.0;
                }
                // do we still have a HEEL STRIKE?
                if (signal < 0) {
                    if (point < minHeelValley) {
                        goodStepFilter.set(i, HEEL_STRIKE_VALLEY_INDICATOR);
                        minHeelValley = point;
                    }
                    if (point < goodStepThreshold && firstBeep) {
                        beep.set(i, point);
                        firstBeep = false;
					/* uncomment for android */
					 if (beepSound > 0)
                         soundMgr.playSound(beepSound);
                    /* */
                        if (debug) {
                            System.out.println("BEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEP");
                        }
                    }
                    // no. then go back and look for a step
                    // TODO transition the reset of gait
                } else {
                    HeelStrikeValley.set(i, minHeelValley);
                    stepState = StepState.FLAT_FOOT;
                    stateCount = SKIP_FLAT_FOOT_SAMPLES;
                    if (debug) {
                        System.out.println("HEEL STRIKE" + " timestamp: " + df.format(i) +
                                " minHeelValley: " + df.format(minHeelValley));
                    }
                    minHeelValley = 0.0;
                }
                break;
            case FLAT_FOOT:
                stateCount--;
                if (signal < 0) {
                    stepState = StepState.HEEL_OFF;
                    stateCount = SKIP_HEEL_OFF_SAMPLES;
                }
                break;
            case HEEL_OFF:
                stateCount--;
                if (point < heelOffPowerMin) {
                    heelOffPowerMin = point;
                }
                if (signal > 0) {
                    heelOffPower.set(i, heelOffPowerMin);
                    heelOffPowerMin = point;
                    stepState = StepState.LOOKING_FOR_STEP;
                }
                break;
        }

        return stepState;
    }

    // TODO in-step timeout - is it needed? there will always be signal transitions.
    public int doSignal(Double point, int i) {
        // if the distance between the current value and average is enough standard
        // deviations (threshold) away
        if (Math.abs(point) > MIN_DEGREES_SEC_FOR_PEAK
                && Math.abs((point - avgFilter.get(i - 1))) > threshold * stdFilter.get(i - 1)) {

            // this is a signal (i.e. peak), determine if it is a positive or negative
            // signal
            if (point > avgFilter.get(i - 1)) {
                signals.set(i, stepThreshold.intValue());
            } else {
                signals.set(i, goodStepThreshold.intValue());
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

        // fix from ahmed data June 8, 2020
        if ((point > MIN_DEGREES_SEC_FOR_PEAK) & (signals.get(i - 1) == 1)) {
            signals.set(i, -1);
        }

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
        returnMap.put("startSwing", startSwing);
        returnMap.put("HeelStrikeValley", HeelStrikeValley);
        returnMap.put("maxFootSwings", maxFootSwings);
        returnMap.put("beep", beep);

        return returnMap;

    } // end

    public HashMap<String, List> analyzeDataForH2T(List<Double> data) {

        // loop input starting at end of rolling window
        for (int i = lag; i < data.size(); i++) {
            int signal = doSignal(data.get(i), i);
            thisStepState = doStepDetect(data.get(i), i, signal, thisStepState, false);
        }

        HashMap<String, List> returnMap = new HashMap<String, List>();
        returnMap.put("signals", signals);
        returnMap.put("filteredData", filteredData);
        returnMap.put("avgFilter", avgFilter);
        returnMap.put("stdFilter", stdFilter);
        returnMap.put("goodStepFilter", goodStepFilter);
        returnMap.put("stepFilter", stepFilter);
        returnMap.put("startSwing", startSwing);
        returnMap.put("HeelStrikeValley", HeelStrikeValley);
        returnMap.put("maxFootSwings", maxFootSwings);
        returnMap.put("beep", beep);

        return returnMap;

    } // end
}