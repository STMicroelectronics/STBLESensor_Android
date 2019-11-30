package com.st.BlueMS;


public class StepResults {
    public double timestamp;
    public boolean toggleDetect;
    public double latestTimeFreeze;
    public double degreesPerSecond;
    public boolean goodstep;
    public boolean badstep;

    StepResults () {
        timestamp = 0;
        toggleDetect = false;
        latestTimeFreeze = 0;
        degreesPerSecond = 0;
        goodstep = false;
        badstep = false;

    }
}

