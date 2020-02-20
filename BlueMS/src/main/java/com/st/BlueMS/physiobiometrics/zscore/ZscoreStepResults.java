package com.st.BlueMS.physiobiometrics.zscore;


public class ZscoreStepResults {
    public double timestamp;
    public double minH2TdegreesPerSecond;
    public double maxHSdegreesPerSecond;
    public boolean goodstep;
    public boolean badstep;

    ZscoreStepResults () {
        timestamp = 0;
        goodstep = false;
        badstep = false;
        minH2TdegreesPerSecond = 0;
        maxHSdegreesPerSecond = 0;
    }
}

