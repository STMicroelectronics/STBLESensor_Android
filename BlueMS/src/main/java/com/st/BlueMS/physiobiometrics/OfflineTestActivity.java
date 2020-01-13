/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.BlueMS.physiobiometrics;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.st.BlueMS.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity that display all the demo available for the node
 */
public class OfflineTestActivity extends AppCompatActivity {

    private static final int matlabFormat = 0;
    private static final int androidFormat = 1;
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int WRITE_REQUEST_CODE = 101;
    private static final int SDCARD_PERMISSION = 1,
            FOLDER_PICKER_CODE = 2,
            FILE_PICKER_CODE = 3;
    protected SeekBar mThreshold;
    ToneGenerator toneGen1;
    //private Context thiscontext;
    private ContentResolver contentResolver;
    private TextView mH2tstatus;
    // UI management
    private RadioButton mBeepChecked;
    private boolean isBeepChecked;
    private RadioButton mCaptureToFileChecked;
    private boolean isCaptureToFileChecked;
    private RadioButton mSimulateChecked;
    private boolean isSimulateChecked;
    private TextView mThresholdVal;
    private double goodStepThreshold;
    private Button processFileButton;
    private Spinner spinnerFileFormat;
    private int fileformat;
    private TextView mFolderLocation;
    /***************
     * inertial measurement XYZ orientation is dependent on the Hardware chip orientation
     * when laid flat:
     * Z is up/down
     * Y is forward to backward
     * X is left to right
     * the heel2toe is oriented on the side, so X,Y,Z will interchange
     * we will do this manually to start, and detect it later....
     */
    private Spinner spinnerX;
    private Spinner spinnerY;
    private Spinner spinnerZ;
    private int Xcoord = X;
    private int Ycoord = Y;
    private int Zcoord = Z;
    private int frequency;
    private Spinner spinnerFrequency;
    private OutputStream outputStream;
    private boolean captureReady;
    private String dataFilename;
    private TextView folder;
    private boolean folderIsSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_h2tfile_feature);

        mH2tstatus = findViewById(R.id.h2tstatus);
        mH2tstatus.setText("Ready for Walk-Well file analysis. Select the proper file format and then chosee a file (Process File) ");

        mBeepChecked = (RadioButton) findViewById(R.id.beepGoodStep);
        isBeepChecked = false;
        mBeepChecked.setOnClickListener(new BeepCheckedListener());

/*        mCaptureToFileChecked = (RadioButton) findViewById(R.id.captureToFile);
        isCaptureToFileChecked = false;
        mCaptureToFileChecked.setOnClickListener(new CaptureCheckedListener());*/

        mSimulateChecked = (RadioButton) findViewById(R.id.simulate);
        isSimulateChecked = false;
        mSimulateChecked.setOnClickListener(new SimulateCheckedListener());

        spinnerFileFormat  = (Spinner) findViewById(R.id.fileformat);
        spinnerFileFormat.setSelection(matlabFormat);
        spinnerFileFormat.setOnItemSelectedListener(new SpinnerFileFormatListener());
        fileformat = matlabFormat;

        spinnerX  = (Spinner) findViewById(R.id.spinnerX);
        spinnerX.setSelection(X);
        spinnerX.setOnItemSelectedListener(new SpinnerXListener());
        spinnerY  = (Spinner) findViewById(R.id.spinnerY);
        spinnerY.setSelection(Y);
        spinnerY.setOnItemSelectedListener(new SpinnerYListener());
        spinnerZ  = (Spinner) findViewById(R.id.spinnerZ);
        spinnerZ.setSelection(Z);
        spinnerZ.setOnItemSelectedListener(new SpinnerZListener());

        frequency = 50;
        spinnerFrequency  = (Spinner) findViewById(R.id.frequency);
        spinnerFrequency.setSelection(0);
        spinnerFrequency.setOnItemSelectedListener(new SpinnerFrequencyListener());

        goodStepThreshold = -109.8; // default value from matlab
        mThresholdVal = findViewById(R.id.thresholdVal);
        mThresholdVal.setText("Threshold: " + goodStepThreshold + " d/s");
        mThreshold =(SeekBar) findViewById(R.id.thresholdBar);
        mThreshold.setProgress((int) -goodStepThreshold);
        mThreshold.setOnSeekBarChangeListener(new ThresholdListener());

        mFolderLocation = findViewById(R.id.folderLocation);
        dataFilename = "unknown format";
        mFolderLocation.setText("choose a file to process (matlab or Heel2Toe) ...");
        folderIsSet = false;

        processFileButton = findViewById(R.id.processfileButton);
        processFileButton.setOnClickListener(new ProcessFileListener());

        //this.thiscontext = container.getContext();
        this.contentResolver = getContentResolver();
        captureReady = false;
        outputStream = null;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent,requestCode);
        String csvFile = null;
        if (intent != null && intent.getData() != null) {
            csvFile =  intent.getData().getPath();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //  Handle activity result here
        StepDetect stepDetect = new StepDetect();
        double[] zGyroArrayFilt;
        List<String[]> inertialMeasurements;
        List<StepResults> allStepResults = new ArrayList<StepResults>();
        List<StepResults> goodstepResults = new ArrayList<StepResults>();
        List<StepResults> badstepResults = new ArrayList<StepResults>();
        int sample = 0;
        double ms = 0;

        /***********
         * first setup X, Y , and Z lookup table based on GUI selections;
         *  in the matlab file, Gyroscope XYZ coordinates are at 0, 7, 4 respectively
         *   GyroscopeX_ds = Double.parseDouble(sArray[0]);
         *   GyroscopeY_ds = Double.parseDouble(sArray[7]);
         *   GyroscopeZ_ds = Double.parseDouble(sArray[4]);
         *                   *
         */
        int[] xyz_gyro = {0, 7, 4};
        String[] xyz = {"X", "Y", "Z"};
        int xGyroIndex = xyz_gyro[Xcoord];
        int yGyroIndex = xyz_gyro[Ycoord];
        int zGyroIndex = xyz_gyro[Zcoord];
        double GyroscopeX_ds = 0;
        double GyroscopeY_ds = 0;
        double GyroscopeZ_ds = 0;
        StepResults stepResults = new StepResults();
        FileProcess fileProcess = new FileProcess();
        String errorMsg = "";

        int WRITE_REQUEST_CODE = 101;

        super.onActivityResult(requestCode, resultCode, intent);

        if (intent != null && intent.getData() != null) {
            Uri content_describer = intent.getData();
            /*
            if (requestCode == FOLDER_PICKER_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    // String folderLocation = "Selected Folder: "+ intent.getExtras().getString("data");
                    folder.setText(intent.getData().getPath());
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    folder.setText("Cancelled");
                }
            } else if (requestCode == WRITE_REQUEST_CODE) {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        try {
                            outputStream = contentResolver.openOutputStream(content_describer);
                            captureReady = true;
                        } catch (IOException e) {
                            mH2tstatus.setText("Error opening file");
                            closeCaptureStream();
                        }
                        folder.setText(dataFilename);
                        break;
                    case Activity.RESULT_CANCELED:
                        mH2tstatus.setText("Canceled");
                        folder.setText("");
                        closeCaptureStream();
                        break;
                }
            } else {

             */
            try {
                InputStream inputStream = this.contentResolver.openInputStream(content_describer);
                dataFilename = queryName(this.contentResolver, content_describer);
                if (fileformat == matlabFormat) {
                    inertialMeasurements = fileProcess.readCSV(inputStream);
                    if (inertialMeasurements.isEmpty()) {
                        errorMsg = "Error. Input file is not shimmer matlab format";
                    }
                } else if (fileformat == androidFormat) {
                    inertialMeasurements = fileProcess.readAndroidFileFormat(inputStream);
                    if (inertialMeasurements.isEmpty()) {
                        errorMsg = "Error. Input file was not produced by this app";
                    }
                } else {
                    inertialMeasurements = null;
                    errorMsg = "Error. unknown inut file format";
                }
            } catch (IOException e) {
                inertialMeasurements = null;
                errorMsg = "Error processing input file";
                e.printStackTrace();
                // print an error message
                return;
            }
            if (!inertialMeasurements.isEmpty()) {
                for (String[] sArray : inertialMeasurements) {
                    ms = sample * 20;
                    boolean numeric = true;
                    try {
                        GyroscopeX_ds = Double.parseDouble(sArray[xGyroIndex]);
                        GyroscopeY_ds = Double.parseDouble(sArray[yGyroIndex]);
                        GyroscopeZ_ds = Double.parseDouble(sArray[zGyroIndex]);
                    } catch (NumberFormatException e) {
                        numeric = false;
                        System.out.println("header");
                    }
                    if (numeric) {
                        System.out.print("GyroscopeX_ds : " + GyroscopeX_ds + " GyroscopeY_ds : " +
                                GyroscopeY_ds + " GyroscopeZ_ds : " + GyroscopeZ_ds);
                        zGyroArrayFilt = stepDetect.filter(ms, GyroscopeX_ds, GyroscopeY_ds, GyroscopeZ_ds);
                        stepResults = stepDetect.detectStep(zGyroArrayFilt, goodStepThreshold);
                        stepResults.timestamp = ms;
                        allStepResults.add(stepResults);
                        if (stepResults.goodstep) {
                            if (isBeepChecked) {
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                            }
                            goodstepResults.add(stepResults);
                        } else if (stepResults.badstep) {
                            badstepResults.add(stepResults);
                        }
                        System.out.println();
                        sample++;

                        // if simulate, then wait 20 ms to simulate real signal
                        if (isSimulateChecked) {
                            try {
                                Thread.sleep(20);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                        }
                    }
                }

                String results = stepDetect.stepResults(allStepResults, goodstepResults, badstepResults, sample, 50);

                results = "Sampling frequency: " + frequency + " Hertz" +
                        System.getProperty("line.separator") +
                        "Gyroscope XYZ X: " + xyz[Xcoord] + " Y: " + xyz[Ycoord] + " Z: " + xyz[Zcoord] +
                        System.getProperty("line.separator") +
                        "Threshold: " + goodStepThreshold + " d/s" +
                        System.getProperty("line.separator") +
                        results;
                mH2tstatus.setText(results);
/*                    if (captureReady) {
                        if (!fileProcess.writeResults(results, outputStream, inertialMeasurements)) {
                            mH2tstatus.setText(results + System.getProperty("line.separator") + "ERROR WRITING FILE");
                        }
                        closeCaptureStream();
                    }*/
            } else {
                mH2tstatus.setText(errorMsg);
            }
            mFolderLocation.setText(dataFilename);
        }
    }

    private class SpinnerXListener implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            Xcoord = position;
        }
        public void onNothingSelected(AdapterView<?> parentView) {
        }
    }

    private class SpinnerYListener implements Spinner.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            Ycoord = position;
        }
        public void onNothingSelected(AdapterView<?> parentView) {
        }
    }

    private class SpinnerZListener implements Spinner.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            Zcoord = position;
        }
        public void onNothingSelected(AdapterView<?> parentView) {
        }
    }

    private class SpinnerFrequencyListener implements Spinner.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            int[] freqs = {50,40,25};
            frequency = freqs[position];
        }
        public void onNothingSelected(AdapterView<?> parentView) {
        }
    }

    private class SpinnerFileFormatListener implements Spinner.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            fileformat = position;
        }
        public void onNothingSelected(AdapterView<?> parentView) {
        }
    }

    private class ThresholdListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            goodStepThreshold = -progress;
            mThresholdVal.setText("Threshold: " + goodStepThreshold + " d/s");
        }
        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    /*
    private class CaptureCheckedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isCaptureToFileChecked) {
                closeCaptureStream();
            } else {
                mCaptureToFileChecked.setChecked(true);
                isCaptureToFileChecked = true;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                // filter to only show openable items.
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Create a file with the requested Mime type
                intent.setType("text/csv");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date today = Calendar.getInstance().getTime();
                dataFilename = dateFormat.format(today)+".csv";
                intent.putExtra(Intent.EXTRA_TITLE, dataFilename);
                startActivityForResult(intent, WRITE_REQUEST_CODE);
            }
        }
    }

     */

    private class BeepCheckedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isBeepChecked) {
                mBeepChecked.setChecked(false);
                isBeepChecked = false;
            } else {
                mBeepChecked.setChecked(true);
                isBeepChecked = true;
                toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            }
        }
    }

    /*
    private class FolderListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            startActivityForResult(intent, FOLDER_PICKER_CODE);
        }
    } */

    private class SimulateCheckedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isSimulateChecked) {
                mSimulateChecked.setChecked(false);
                isSimulateChecked = false;
                mH2tstatus.setText("simulation cleared. we will process as fast as possible");
            } else {
                mSimulateChecked.setChecked(true);
                isSimulateChecked = true;
                mBeepChecked.setChecked(true);
                isBeepChecked = true;
                toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                mH2tstatus.setText("warning! simulation takes a long time! 20 millisecond pause between each sample to simulate live data capture ");
            }
        }
    }

    /*
    private  void closeCaptureStream() {
        if (captureReady) {
            try {
                if (outputStream != null) {
                    outputStream.close();
                    outputStream = null;
                }

            } catch (IOException e) {
                mH2tstatus.setText("Error closing output stream");
            }
        }
        captureReady = false;
        mCaptureToFileChecked.setChecked(false);
        isCaptureToFileChecked = false;
    }
    */

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    private class ProcessFileListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/csv");
            intent = Intent.createChooser(intent, "Choose a file");
            startActivityForResult(intent, 1);
        }
    }
}
