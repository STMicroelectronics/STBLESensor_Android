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
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link android.app.Activity} for folder creation.
 */


public class FileProcess  {

    public int findZindex (InputStream is) {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            String csvSplitBy = ",";

            line =  br.readLine();
            String[] headerRow = line.split(csvSplitBy);

            int zindex = -1;
            int index = 0;
            for (String header: headerRow) {
                if (header.contains("Gyro") && header.contains("Z") && header.contains("CAL")) {
                    zindex = index;
                    break;
                }
                index++;
            }
            return zindex;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
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

            int rnum = 0;
            while ((line = br.readLine()) != null) {
                String[] row = line.split(csvSplitBy);
                try {   // make sure the line has numbers beofre parsing
                    double val = Double.parseDouble(row[0]);
                    // only add row if numbers
                    rows.add(row);
                }
                catch (Exception e) {
                    System.out.print("row " + rnum + " r: " );
                    System.out.println(Arrays.toString(row));
                    System.out.println("Exception: " + e);
                }
                rnum++;
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

    public ArrayList<Double> GetZdata(List<String[]> im, int zindex) {
        ArrayList<Double> zdata = new  ArrayList<Double>();
        for (String[] sArray : im) {
            try {
                zdata.add(Double.parseDouble(sArray[zindex]));
            } catch (NumberFormatException e) {
                System.out.print(e.toString());
                System.out.print(zindex);
                e.printStackTrace();
                return null;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.print(e.toString());
                System.out.print(zindex);
                e.printStackTrace();
                return null;
            }
        }
        return zdata;
    }

    public FileStatus writeResults(String results, OutputStream outputStream, long  sampleCount,
                                List<InertialMeasurement> gyroMeasurements,
                                List<InertialMeasurement> accelMeasurements) {
        FileStatus fs = new FileStatus(true,"file write success");
        try {
            BufferedWriter rawData = new BufferedWriter(new OutputStreamWriter(outputStream));
            rawData.write(results);
            rawData.newLine();
            // we stay comaptible with the old matlab files
            rawData.write(
                    "sample, timestamp,"+
                    "GyroscopeX_ds,GyroscopeY_ds,GyroscopeZ_ds," +
                    "AccelerometerX_ms2, AccelerometerY_ms2, AccelerometerZ_ms2, step\n");
            rawData.newLine();

            int i = 0;

            while (i < accelMeasurements.size() && i < gyroMeasurements.size()) {
                InertialMeasurement g = gyroMeasurements.get(i);
                InertialMeasurement a = accelMeasurements.get(i++);
                if ((g == null) || (a == null)) {
                    fs.reason = "fileProcess warning. found a null object at sample: " + i + " of " + gyroMeasurements.size();
                    break;
                }
                rawData.write(g.sample+","+g.timestamp + ","+
                        g.x+","+g.y+","+g.z+"," + a.x+","+a.y+","+a.z+","+g.step);
                rawData.newLine();
            }
            rawData.close();

        }  catch (Exception e) {
            fs.reason = "fileProcess error: " + e.getMessage();
            fs.success = false;
        }
        return fs;
    }
}