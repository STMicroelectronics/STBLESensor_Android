package com.st.BlueMS.physiobiometrics.zscore;

public class ZscoreStepResults {
    public double timestamp;
    public double minHeelStrike;
    public double maxFootSwing;
    public boolean goodstep;
    public boolean badstep;

    ZscoreStepResults () {
        timestamp = 0;
        goodstep = false;
        badstep = false;
        minHeelStrike = 0;
        maxFootSwing = 0;
    }
}

