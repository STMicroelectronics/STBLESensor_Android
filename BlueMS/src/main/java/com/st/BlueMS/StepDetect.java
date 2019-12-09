package com.st.BlueMS;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StepDetect {
    public double[] filter_b = { 0.0096, 0.0287, 0.0287, 0.0096 };
    public double[] filter_a = { 1, -2.0268, 1.4741, -0.3707 };
    public double[][] rotMat = {{1,0,0},{0,1,0},{0,0,1}};

    public double divisionThreshold = 50;
    public double classBoundary = -109.8;
    public boolean toggleDetect = false;
    public double latestTime;
    public double latestTimeFreeze;
    public double zGyroValueFreeze;

    public int wait1 = 0;
    public int wait2 = 0;
    public double phaseTime;
    private int array_size = 4;

    public double[]  zGyroArray;
    public double[] zGyroArrayFilt;

    public boolean plotToggle;
    public boolean soundToggle;
    public boolean loggingToggle;

    public static String mFileName = "ht2";
    //public File rootDirectory = Environment.getExternalStorageDirectory();
    public double[] heelStrikeLogArray;

    public StepDetect() {
        zGyroArray = new double[] {0,0,0,0};
        zGyroArrayFilt = new double[] {0,0,0,0};
    }

    public double[] filter(double timestamp, double xGyroValue,double yGyroValue, double zGyroValue) {

        double[][] gyroArray = {{xGyroValue},{yGyroValue},{zGyroValue}};

        double[][] gyroFix = multiplyByMatrix(rotMat,gyroArray);

        zGyroValue = gyroFix[2][0];

        System.out.println(zGyroValue);

        latestTimeFreeze = timestamp/1000;

        zGyroArray = shiftArray(zGyroArray);
        zGyroArrayFilt = shiftArray(zGyroArrayFilt);

        zGyroArray[0] = zGyroValue;

        zGyroArrayFilt[0] = (dotProd(zGyroArray, filter_b) - dotProd(
                Arrays.copyOfRange(zGyroArrayFilt, 1,
                        array_size), Arrays.copyOfRange(
                        filter_a, 1, array_size)))
                / filter_a[0];
        return zGyroArrayFilt;
    }

    public StepResults detectStep(double[] zGyroArrayFilt) {
        StepResults stepResults = new StepResults();

        if (wait1 == 0 && wait2 == 0) {
            if (toggleDetect == false) {
                if (zGyroArrayFilt[1] > zGyroArrayFilt[2]
                        && zGyroArrayFilt[1] > zGyroArrayFilt[0]
                        && zGyroArrayFilt[1] >= divisionThreshold) {
                    toggleDetect = true;
                    wait1 = 7;
                    phaseTime = latestTimeFreeze;

                }
            } else {
                if (zGyroArrayFilt[1] < 100
                        && zGyroArrayFilt[1] < zGyroArrayFilt[2]
                        && zGyroArrayFilt[1] < zGyroArrayFilt[0]) {
                    toggleDetect = false;
                    wait2 = 20;
                    if (zGyroArrayFilt[1] < classBoundary) {
                        toggleDetect = false;
                        stepResults.degreesPerSecond = zGyroArrayFilt[1];
                        stepResults.goodstep = true;
                        if (soundToggle) {
                            //soundPool.play(sound, 1.0f,1.0f, 0, 0, 1.0f);
                        }
                    } else {
                        stepResults.degreesPerSecond = zGyroArrayFilt[1];
                        stepResults.badstep = true;
                    }
                } else if (latestTimeFreeze - phaseTime > 0.78) {
                    toggleDetect = false;
                }
            }
        } else {
            if (wait1 != 0) {
                wait1 = wait1 - 1;
            }

            if (wait2 != 0) {
                wait2 = wait2 - 1;
            }
        }

        stepResults.toggleDetect = toggleDetect;
        stepResults.latestTimeFreeze = latestTimeFreeze;

        return stepResults;
    }

    public String stepResults (List<StepResults> allStepResults,
                               List<StepResults> goodstepResults, List<StepResults> badstepResults, int samples ) {
        int goodsteps = goodstepResults.size();
        int badsteps = badstepResults.size();
        int nSteps = goodsteps + badsteps;
        System.out.println("steps = "+ nSteps);
        System.out.println("good steps : " + goodsteps);
        System.out.println("bad steps : " + badsteps);
        double percent =  (double) goodsteps/ (double) nSteps * 100;
        System.out.println("goodStepPercent : " + percent);
        percent =  (double) badsteps/(double) nSteps * 100;
        System.out.println("badStepPercent : " + percent);
        double totalTime = (double) (samples * 20) / 1000;
        System.out.println("totalTime : " + totalTime);

        double stepwalkingtime = 0;
        double totalwalkingtime = 0;
        double startWalking = 0;
        double totalAngularVelocity = 0;
        int index = 0;
        while (index <  allStepResults.size()) {
            StepResults step =  allStepResults.get(index);
            if (index > 0) {
                stepwalkingtime = step.timestamp - startWalking;
            }
            startWalking = step.timestamp;
            totalwalkingtime += stepwalkingtime;

            //TODO calculate from raw data not filtered!
            totalAngularVelocity += step.degreesPerSecond;
            index++;
        }
        totalwalkingtime = totalwalkingtime / 1000;
        double avgstep = totalwalkingtime / (double) nSteps;
        double avgCadence = 120 / avgstep;

        double meanAngularVelocity = totalAngularVelocity / (double) nSteps;
        double stdAngularVelocity = 0;
        index = 0;
        while (index <  allStepResults.size()) {
            StepResults step =  allStepResults.get(index);
            stdAngularVelocity += Math.pow(step.degreesPerSecond - meanAngularVelocity, 2);
            index++;
        }

        stdAngularVelocity = Math.sqrt(stdAngularVelocity/(double) nSteps);

        System.out.println("walkingTime : " + totalwalkingtime);
        System.out.println("avgCadence : " + avgCadence + " 2 x steps / min (avg step : = " + avgstep + " s )");
        System.out.println("avgAngularVelocity : " + meanAngularVelocity);
        System.out.println("stdAngularVelocity : " + stdAngularVelocity);

        return (" steps summary");
    }

    public double[] shiftArray(double[] aOld) {
        double[] aNew = new double[aOld.length];
        for (int i = 1; i < aOld.length; i++) {
            aNew[i] = aOld[i - 1];
        }
        return aNew;
    }

    public double dotProd(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "The dimensions have to be equal!");
        }
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public double[][] multiplyByMatrix(double[][] m1, double[][] m2) {
        int m1ColLength = m1[0].length; // m1 columns length
        int m2RowLength = m2.length;    // m2 rows length
        if(m1ColLength != m2RowLength) return null; // matrix multiplication is not possible
        int mRRowLength = m1.length;    // m result rows length
        int mRColLength = m2[0].length; // m result columns length
        double[][] mResult = new double[mRRowLength][mRColLength];
        for(int i = 0; i < mRRowLength; i++) {         // rows from m1
            for(int j = 0; j < mRColLength; j++) {     // columns from m2
                for(int k = 0; k < m1ColLength; k++) { // columns from m1
                    mResult[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }
        return mResult;
    }

    public static double calibrationProcessing(double[] array) {
        int j = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != 0) {
                array[j++] = array[i];
            }

        }
        double[] newArray = new double[j];
        System.arraycopy(array, 0, newArray, 0, j);

        double arrayMean = mean(newArray);

        return arrayMean;
    }

    public static double mean(double[] m) {
        double sum = 0;
        for (int i = 0; i < m.length; i++) {
            sum += m[i];
        }
        return sum / m.length;
    }

    public static double[] normalise(double[] a) {
        double aNorm = Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
        double[] aOut = { a[0] / aNorm, a[1] / aNorm, a[2] / aNorm };
        return aOut;
    }

    public void appendLog(double[] writeArray)
    {
        File logFile = new File(mFileName + "-output.dat");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i<writeArray.length;i++){
                stringBuffer.append(Double.toString(writeArray[i]));
                stringBuffer.append("\t");

            }
            buf.append(stringBuffer.toString());
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<String[]> readCSV(Context context, String fileName) {
        List<String[]> rows = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String csvSplitBy = ",";

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] row = line.split(csvSplitBy);
                rows.add(row);
            }
            return rows;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String[]> readCSV(InputStream is) {
        List<String[]> rows = new ArrayList<>();
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String csvSplitBy = ",";

            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] row = line.split(csvSplitBy);
                rows.add(row);
            }
            return rows;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
