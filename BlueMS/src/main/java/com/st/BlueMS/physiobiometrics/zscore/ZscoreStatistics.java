package com.st.BlueMS.physiobiometrics.zscore;

import java.util.ArrayList;

public class ZscoreStatistics {
	
	int lag;
	double[] statsArray;
	
	ZscoreStatistics(int lag) {
		this.lag = lag;
		statsArray = new double[lag];
		this.initLagArray(lag);
	}
	
	private void initLagArray(int lag) {
		 for (int i = 0; i < lag; i++) {
			 statsArray[i] = 0.0;
	        }
	}
	
	public void setLagArray(ArrayList<Double> filterdata, int currentSampleIndex) {
		 int i = 0;
		 for (int j = currentSampleIndex - lag; j < currentSampleIndex; j++) {
			 statsArray[i++] = filterdata.get(j);
         }
	}
	
	
	public  double getMean() {
        double sum = 0;
        for (int i = 0; i < lag; i++) {
            sum += statsArray[i];
        }
        return sum / lag;
    }
	
	public double getVariance(double mean) {
        double sum = 0;
        for (int i = 0; i < lag; i++) {
            sum += Math.pow(statsArray[i] - mean, 2);
        }
        return sum / lag;
    }

}
