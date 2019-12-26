package com.st.BlueMS.physiobiometrics;


public class InertialMeasurement {
    public long sample;
    public long timestamp;
    public double x;
    public double y;
    public double z;

    InertialMeasurement(long sample, long timestamp, double x, double y, double z) {
        this.sample = sample;
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}

