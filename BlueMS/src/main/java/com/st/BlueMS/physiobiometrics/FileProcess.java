package com.st.BlueMS.physiobiometrics;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link android.app.Activity} for folder creation.
 */
public class FileProcess  {

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

    public List<String[]> readAndroidFileFormat(InputStream is) {
        List<String[]> rows = new ArrayList<>();
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String csvSplitBy = ",";

            br.readLine();

            boolean header = true;
            while ((line = br.readLine()) != null && header) {
                String[] row = line.split(csvSplitBy);
                for (String s : row) {
                    if (s.trim().compareTo("AccelerometerZ_ms2") == 0) {
                        header = false;
                    }
                }
            }
            br.readLine();

            if (header) {
                return null;
            }

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

    public boolean writeResults(String results, OutputStream outputStream, List<String[]> inertialMeasurements) {
        try {
            BufferedWriter rawData = new BufferedWriter(new OutputStreamWriter(outputStream));
            rawData.write(results);
            rawData.newLine();
            // we stay comaptible with the old matlab files
            rawData.write("GyroscopeX_ds,GyroscopeX_raw," +
                    "AccelerometerZ_ms2,AccelerometerZ_raw," +
                    "GyroscopeZ_ds,GyroscopeZ_raw," +
                    "GyroscopeY_raw,GyroscopeY_ds," +
                    "AccelerometerY_ms2,AccelerometerY_raw,AccelerometerX_ms2,AccelerometerX_raw," +
                    "Timestamp,Timestamp_ms\n");
            rawData.newLine();
            for (String[] sArray : inertialMeasurements) {
                int i = 0;
                for (String s : sArray) {
                    rawData.write(s);
                    if (i++ < sArray.length) {
                        rawData.write(",");
                    }
                }
                rawData.newLine();
            }
            rawData.close();

        }  catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writeResults(String results, OutputStream outputStream, long  sampleCount,
                                List<InertialMeasurement> gyroMeasurements,
                                List<InertialMeasurement> accelMeasurements) {
        try {
            BufferedWriter rawData = new BufferedWriter(new OutputStreamWriter(outputStream));
            rawData.write(results);
            rawData.newLine();
            // we stay comaptible with the old matlab files
            rawData.write(
                    "sample, timestamp,"+
                    "GyroscopeX_ds,GyroscopeY_ds,GyroscopeZ_ds," +
                    "AccelerometerX_ms2, AccelerometerY_ms2, AccelerometerZ_ms2\n");
            rawData.newLine();
            if (sampleCount != gyroMeasurements.size()) {
                return false;
            }
            if (sampleCount != accelMeasurements.size()) {
                return false;
            }
            int i = 0;
            for (InertialMeasurement g : gyroMeasurements) {
                InertialMeasurement a = accelMeasurements.get(i++);
                rawData.write(g.sample+","+g.timestamp + ","+
                        g.x+","+g.y+","+g.z+"," + a.x+","+a.y+","+a.z);
                rawData.newLine();
            }
            rawData.close();

        }  catch (IOException e) {
            return false;
        }
        return true;
    }
}