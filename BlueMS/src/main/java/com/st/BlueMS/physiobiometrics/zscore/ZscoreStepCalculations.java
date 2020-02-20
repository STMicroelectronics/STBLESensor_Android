package com.st.BlueMS.physiobiometrics.zscore;

public class ZscoreStepCalculations {
    int frequency;
    double rate;
    int goodsteps;
    int badsteps;
    int nSteps;
    double totalTime;
    double goodpercent;
    double badpercent;
    double stepwalkingtime;
    double totalwalkingtime;
    double startWalking;
    double stopWalking;
    double avgstep;
    double avgCadence;
    double meanH2TAngularVelocity;
    double stdH2TAngularVelocity;
    double H2TcoefVariation;
    double meanHSAngularVelocity;
    double stdHSAngularVelocity;
    double HScoefVariation;

    ZscoreStepCalculations() {
        frequency =0;
        rate =0;
        goodsteps=0;
        badsteps=0;
        nSteps=0;
        totalTime=0;
        goodpercent=0;
        badpercent=0;
        stepwalkingtime=0;
        totalwalkingtime=0;
        startWalking=0;
        stopWalking=0;
        avgstep=0;
        avgCadence=0;
        meanH2TAngularVelocity=0;
        stdH2TAngularVelocity=0;
        H2TcoefVariation=0;
        meanHSAngularVelocity=0;
        stdHSAngularVelocity=0;
        HScoefVariation=0;
    }


}
