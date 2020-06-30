package com.st.BlueMS.physiobiometrics.zscore;

public class ZscoreStepCalculations {
    String fname;
    int freqHZ; //sampling frequency
    double rate;
    double footSwingThreshold; //threshold to be considered the start of a step
    double stepThreshold; //threshold for a good step
    int lag; // signal algorithm
    double threshold; // signal algorithm
    double influence; // signal algorithm
    // results
    int ngood;
    int nbad;
    int nSteps;
    double timeOn;
    double pcgood;
    double pcbad;
    double timeWalk;
    double totalwalkingtime;
    double startWalking;
    double stopWalking;
    double stepmeantime;
    double cadmean;
    double HeelStrikeAV;
    double HeelStrikeAVSTD;
    double HeelStrikeAVCV;
    double FootSwingAV;
    double FootSwingAVSTD;
    double FootSwingAVCV;

    ZscoreStepCalculations() {
        fname = "";
        freqHZ =0;
        footSwingThreshold = 0;
        stepThreshold = 0;
        rate =0;
        ngood=0;
        nbad=0;
        nSteps=0;
        timeOn=0;
        pcgood=0;
        pcbad=0;
        timeWalk=0;
        totalwalkingtime=0;
        startWalking=0;
        stopWalking=0;
        stepmeantime=0;
        cadmean=0;
        HeelStrikeAV=0;
        HeelStrikeAVSTD=0;
        HeelStrikeAVCV=0;
        FootSwingAV=0;
        FootSwingAVSTD=0;
        FootSwingAVCV=0;
    }


}
