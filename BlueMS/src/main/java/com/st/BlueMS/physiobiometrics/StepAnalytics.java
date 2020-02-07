package com.st.BlueMS.physiobiometrics;

import java.text.DecimalFormat;
import java.util.List;

public class StepAnalytics {

    public StepCalculations analytics(List<StepResults> allStepResults,
                            List<StepResults> goodstepResults, List<StepResults>
                                    badstepResults, int samples, int frequency) {

        StepCalculations sc = new StepCalculations();

        DecimalFormat df2 = new DecimalFormat("#.##");
        sc.frequency = frequency;
        sc.rate = 1 / (double) frequency;
        double rateMilliseconds = sc.rate * 1000;

        sc.goodsteps = goodstepResults.size();
        sc.badsteps = badstepResults.size();
        sc.nSteps = sc.goodsteps + sc.badsteps;
        sc.totalTime = (double) samples * sc.rate;
        sc.goodpercent = (double) sc.goodsteps / (double) sc.nSteps * 100;
        sc.badpercent = (double) sc.badsteps / (double) sc.nSteps * 100;
        sc.stepwalkingtime = 0;
        sc.totalwalkingtime = 0;
        sc.startWalking = 0;
        double totalAngularVelocity = 0;
        int index = 0;
        boolean firststep = true;

        double lastStepTime =0;
        while (index < allStepResults.size()) {
            StepResults step = allStepResults.get(index);
            if (step.goodstep || step.badstep) {
                if (index == 0) {
                    sc.startWalking = step.timestamp;
                    lastStepTime = sc.startWalking;
                }
                if (index > 0) {
                    sc.stepwalkingtime = step.timestamp - lastStepTime;
                    lastStepTime = step.timestamp;
                }
                sc.totalwalkingtime += sc.stepwalkingtime;

                //TODO calculate from raw data not filtered!
                totalAngularVelocity += step.degreesPerSecond;
                index++;
            }
        }
        sc.totalwalkingtime = sc.totalwalkingtime / 1000;
        if (sc.nSteps == 0) {
            sc.avgstep = 0;
            sc.avgCadence = 0;
            sc.meanAngularVelocity = 0;
            sc.stdAngularVelocity = 0;
            sc.coefVariation = 0;
        } else {
            sc.avgstep = sc.totalwalkingtime / (double) (sc.nSteps-1);
            sc.avgCadence = 120 / sc.avgstep;
            sc.meanAngularVelocity = totalAngularVelocity / (double) sc.nSteps;
            sc.stdAngularVelocity = 0;
            index = 0;
            while (index < allStepResults.size()) {
                StepResults step = allStepResults.get(index);
                double difference = step.degreesPerSecond - sc.meanAngularVelocity;
                sc.stdAngularVelocity += Math.pow(difference, 2);
                index++;
            }
            sc.stdAngularVelocity = sc.stdAngularVelocity/(double) sc.nSteps;
            sc.stdAngularVelocity = Math.sqrt(sc.stdAngularVelocity);
            sc.coefVariation =  sc.stdAngularVelocity/sc.meanAngularVelocity;
        }
        return sc;
    }
}
