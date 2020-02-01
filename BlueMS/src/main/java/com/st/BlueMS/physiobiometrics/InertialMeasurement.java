package com.st.BlueMS.physiobiometrics;


public class InertialMeasurement {
    public long sample;
    public long timestamp;
    public double x;
    public double y;
    public double z;
    public int step;  // good = 1, bad = -1;

    InertialMeasurement(long sample, long timestamp, double x, double y, double z, int step) {
        this.sample = sample;
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
        this.step = step;
    }
}

