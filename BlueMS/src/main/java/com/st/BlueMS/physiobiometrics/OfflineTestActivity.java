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
import android.content.Context;
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
import android.widget.TableLayout;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.physiobiometrics.shimmer.StepAnalytics;
import com.st.BlueMS.physiobiometrics.shimmer.StepAnalyticsDisplay;
import com.st.BlueMS.physiobiometrics.shimmer.StepCalculations;
import com.st.BlueMS.physiobiometrics.shimmer.StepDetect;
import com.st.BlueMS.physiobiometrics.shimmer.StepResults;
import com.st.BlueMS.physiobiometrics.zscore.ZscoreSignalDetector;
import com.st.BlueMS.physiobiometrics.zscore.ZscoreStepAnalytics;
import com.st.BlueMS.physiobiometrics.zscore.ZscoreStepAnalyticsDisplay;
import com.st.BlueMS.physiobiometrics.zscore.ZscoreStepCalculations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Activity that display all the demo available for the node
 */
public class OfflineTestActivity extends AppCompatActivity {

    private static final int physioBioMetricsRFormat = 0;
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

    private Button processFileButton;
    private Button firebaseButton;
    private Spinner spinnerFileFormat;
    private int fileformat;
    private TextView mFolderLocation;

    private Context thiscontext;
    private TableLayout mResultsTable;
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

    // signal detector
    private int zScorelag = 5;
    private double zScoreThreshold = 2;
    private double zScoreInfluence = 0.1;

    // step detector
    private static int MAX_DATA_SIZE = 50000; // 16 minutes
    private double footSwingThreshold = 100.0;
    private double goodStepThreshold = -150.0;

    private ZscoreSignalDetector zscoreSignalDetector;
    private ZscoreSignalDetector.StepState thisStepState;
    private ArrayList<Double> dataH2t;

    Sound soundMgr;
    int beepSound;
    int startMeasureSound;
    int stopMeasureSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_h2tfile_feature);

        //console.log("Auth: "+buf);
        mH2tstatus = findViewById(R.id.h2tstatus);
        mH2tstatus.setText("Ready for Walk-Well file analysis. Select the proper file format and then chosee a file (Process File) ");

/*        mBeepChecked = (RadioButton) findViewById(R.id.beepGoodStep);
        isBeepChecked = false;
        mBeepChecked.setOnClickListener(new BeepCheckedListener());*/

/*        mCaptureToFileChecked = (RadioButton) findViewById(R.id.captureToFile);
        isCaptureToFileChecked = false;
        mCaptureToFileChecked.setOnClickListener(new CaptureCheckedListener());*/

/*        mSimulateChecked = (RadioButton) findViewById(R.id.simulate);
        isSimulateChecked = false;
        mSimulateChecked.setOnClickListener(new SimulateCheckedListener());*/

        spinnerFileFormat = (Spinner) findViewById(R.id.fileformat);
        spinnerFileFormat.setSelection(physioBioMetricsRFormat);
        spinnerFileFormat.setOnItemSelectedListener(new SpinnerFileFormatListener());
        fileformat = physioBioMetricsRFormat;

        /*
        spinnerX  = (Spinner) findViewById(R.id.spinnerX);
        spinnerX.setSelection(X);
        spinnerX.setOnItemSelectedListener(new SpinnerXListener());
        spinnerY  = (Spinner) findViewById(R.id.spinnerY);
        spinnerY.setSelection(Y);
        spinnerY.setOnItemSelectedListener(new SpinnerYListener());
        spinnerZ  = (Spinner) findViewById(R.id.spinnerZ);
        spinnerZ.setSelection(Z);
        spinnerZ.setOnItemSelectedListener(new SpinnerZListener());

         */

        frequency = 50;
        spinnerFrequency = (Spinner) findViewById(R.id.frequency);
        spinnerFrequency.setSelection(0);
        spinnerFrequency.setOnItemSelectedListener(new SpinnerFrequencyListener());

        mThresholdVal = findViewById(R.id.thresholdVal);
        mThresholdVal.setText("Threshold: " + goodStepThreshold + " d/s");
        mThreshold = (SeekBar) findViewById(R.id.thresholdBar);
        mThreshold.setProgress((int) -goodStepThreshold);
        mThreshold.setOnSeekBarChangeListener(new ThresholdListener());

        mFolderLocation = findViewById(R.id.folderLocation);
        dataFilename = "unknown format";
        mFolderLocation.setText("choose a file to process (PhysioBioMetrics R  or Mobile app format) ...");
        folderIsSet = false;

        processFileButton = findViewById(R.id.processfileButton);
        processFileButton.setOnClickListener(new ProcessFileListener());

        firebaseButton = findViewById(R.id.firebase);
        firebaseButton.setOnClickListener(new FirebaseButtonListener());

        //this.thiscontext = container.getContext();
        this.contentResolver = getContentResolver();
        captureReady = false;
        outputStream = null;

        mResultsTable = (TableLayout) findViewById(R.id.resulttable);

        Sound soundMgr = new Sound();
        soundMgr.createNewSoundPool();
        startMeasureSound = soundMgr.loadSoundID(this, R.raw.startsoundbeep1);
        stopMeasureSound = soundMgr.loadSoundID(this, R.raw.alarmclock1);
        beepSound = soundMgr.loadSoundID(this, R.raw.shimmerbeep1);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {

        this.zscoreSignalDetector = new ZscoreSignalDetector(zScorelag, zScoreThreshold,
                zScoreInfluence, MAX_DATA_SIZE, footSwingThreshold, goodStepThreshold, soundMgr, -1);
        thisStepState = ZscoreSignalDetector.StepState.LOOKING_FOR_STEP;

        super.startActivityForResult(intent, requestCode);
        String csvFile = null;
        if (intent != null && intent.getData() != null) {
            csvFile = intent.getData().getPath();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //  Handle activity result here
        double[] zGyroArrayFilt;
        List<String[]> inertialMeasurements;
        List<StepResults> allStepResults = new ArrayList<StepResults>();
        List<StepResults> goodstepResults = new ArrayList<StepResults>();
        List<StepResults> badstepResults = new ArrayList<StepResults>();
        int sample = zScorelag;
        dataH2t = new ArrayList<Double>();

        /***********
         * first setup X, Y , and Z lookup table based on GUI selections;
         *  in the PhysioBioMetrics file, Gyroscope XYZ coordinates are at 0, 6,5 respectively
         *   GyroscopeX_ds = Double.parseDouble(sArray[0]);
         *   GyroscopeY_ds = Double.parseDouble(sArray[7]);
         *   GyroscopeZ_ds = Double.parseDouble(sArray[4]);
         *                   *
         */
        //int[] xyz_gyro = {0, 7, 4};
        int[] xyz_gyro = {0, 6, 5};

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
            try {
                InputStream inputStream = this.contentResolver.openInputStream(content_describer);
                dataFilename = queryName(this.contentResolver, content_describer);
                if (fileformat == physioBioMetricsRFormat) {
                    zGyroIndex = fileProcess.findZindex(inputStream);
                    if (zGyroIndex == -1) {
                        errorMsg = "Error. Input file was not produced by this application";
                    }
                    else {
                        errorMsg =("FOUND zINDEX IN FILE = " + zGyroIndex);
                    }
                    inertialMeasurements = fileProcess.readCSV(inputStream);

                    if (inertialMeasurements.isEmpty()) {
                        errorMsg = "Error. Input file is not physioBioMetrics R Format format";
                    }
                } else if (fileformat == androidFormat) {
                    zGyroIndex = 4;
                    inertialMeasurements = fileProcess.readAndroidFileFormat(inputStream);
                    if (inertialMeasurements.isEmpty()) {
                        errorMsg = "Error. Input file was not produced by this application";
                    }
                } else {
                    inertialMeasurements = null;
                    errorMsg = "Error. unknown input file format";
                }
            } catch (IOException e) {
                inertialMeasurements = null;
                errorMsg = "Error processing input file";
                e.printStackTrace();
                // print an error message
                return;
            }

            // get all values so reading file does not affect realtime measurments
            if (!inertialMeasurements.isEmpty()) {
                ArrayList<Double> zvals = new ArrayList<Double>();
                for (String[] sArray : inertialMeasurements) {
                    try {
                        GyroscopeZ_ds = Double.parseDouble(sArray[zGyroIndex]);
                        zvals.add(GyroscopeZ_ds);
                    } catch (NumberFormatException e) {
                        System.out.println("error. not numeric");
                    }
                }
                // process zvals
                long timeNow = System.currentTimeMillis();
                for (Double zval : zvals) {
                    //ms = sample * 20;
                    //System.out.print("GyroscopeX_ds : " + GyroscopeX_ds + " GyroscopeY_ds : " +
                    //        GyroscopeY_ds + " GyroscopeZ_ds : " + GyroscopeZ_ds);

                    int signal = this.zscoreSignalDetector.doSignal(zval, sample);
                    thisStepState = this.zscoreSignalDetector.doStepDetect(zval, sample, signal,
                            thisStepState, false);
                    dataH2t.add(zval);
                    sample++;
                }
                long totalTime = System.currentTimeMillis() - timeNow;
                System.out.println("time: " + totalTime);

                HashMap<String, List> resultsMap = this.zscoreSignalDetector.getDataForH2T();
                List<Integer> signalsList = resultsMap.get("signals");
                List<Double> filteredDataList = resultsMap.get("filteredData");
                List<Double> avgFilterList = resultsMap.get("avgFilter");
                List<Double> stdFilterList = resultsMap.get("stdFilter");
                List<Double> goodStepFilterList = resultsMap.get("goodStepFilter");
                List<Double> stepFilterList = resultsMap.get("stepFilter");
                List<Double> HeelStrikeValley = resultsMap.get("HeelStrikeValley");
                List<Double> maxFootSwings = resultsMap.get("maxFootSwings");
                List<Double> beepList = resultsMap.get("beep");

                ZscoreStepAnalytics stepAnalytics = new ZscoreStepAnalytics();
                ZscoreStepCalculations stepCalculations = stepAnalytics.signalAnalytics(dataH2t, signalsList,
                        stepFilterList, maxFootSwings, goodStepFilterList, HeelStrikeValley,
                        20, 50,zScorelag,goodStepThreshold);

                stepAnalytics.printStepResults();
                stepAnalytics.printStepCalculations();

                ZscoreStepAnalyticsDisplay zscoreStepAnalyticsDisplay = new ZscoreStepAnalyticsDisplay();
                zscoreStepAnalyticsDisplay.results(this, mResultsTable, stepCalculations,
                        goodStepThreshold,  dataH2t.size(), dataFilename);

                mH2tstatus.setText("");

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
            int[] freqs = {50, 40, 25};
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

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }


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
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
            }
        }
    }

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
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                mH2tstatus.setText("warning! simulation takes a long time! 20 millisecond pause between each sample to simulate live data capture ");
            }
        }
    }


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
    private class FirebaseButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            mH2tstatus.setText("firebase button");
        }
    }
}
