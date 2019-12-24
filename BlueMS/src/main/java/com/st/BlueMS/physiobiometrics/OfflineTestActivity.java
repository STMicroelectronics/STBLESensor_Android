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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;

/**
 * Activity that display all the demo available for the node
 */
public class OfflineTestActivity extends AppCompatActivity {

    //private Context thiscontext;
    private ContentResolver contentResolver;
    private boolean mIsPlotting;
    private XYPlot mChart;
    private ImageButton mStartPlotButton;

    /** domain axis label */
    private String mXAxisLabel;
    private TextView mH2tstatus;

    // timers fo rH2t session
    private Timer timer;
    private CountDownTimer countdown;
    protected int maxSessionSeconds = 10;
    private  int counter;

    // UI management
    private RadioButton mBeepChecked;
    private boolean isBeepChecked;
    private RadioButton mCaptureToFileChecked;
    private boolean isCaptureToFileChecked;
    private RadioButton mSimulateChecked;
    private boolean isSimulateChecked;
    protected SeekBar mThreshold;
    private TextView mThresholdVal;
    private int goodStepThreshold;
    private Button processFileButton;
    private Button folderButton;

    private SeekBar mMaxTimeBar;
    protected TextView mMaxtime;
    private TextView mCcounttime;
    private int stopwatch;

    // step detection
    private StepDetect stepDetect;
    private double[] zGyroArrayFilt;
    private List<StepResults> allStepResults = new ArrayList<StepResults>();
    private List<StepResults> goodstepResults = new ArrayList<StepResults>();
    private List<StepResults> badstepResults = new ArrayList<StepResults>();

    private OutputStream outputStream;
    private BufferedWriter bw;
    private boolean captureReady;
    private String dataFilename;

    // TED for H2t sampling
    List<Feature> h2tFeatures = null;
    private List<FeatureGyroscope> mH2TgyroFeature;
    private  Feature.FeatureListener mH2TgyroFeatureListener;

    private List<FeatureAcceleration>  mH2TaccelFeature;
    private  Feature.FeatureListener mH2TaccelFeatureListener;

    private static final int WRITE_REQUEST_CODE = 101;

    private static final int SDCARD_PERMISSION = 1,
            FOLDER_PICKER_CODE = 2,
            FILE_PICKER_CODE = 3;

    private TextView folder;
    private boolean folderIsSet;

    ToneGenerator toneGen1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_h2tfile_feature);

        mH2tstatus = findViewById(R.id.h2tstatus);
        mH2tstatus.setText("Ready for Walk-Well analysis. Press start button then walk. Process file to simulate. ");

        mBeepChecked = (RadioButton) findViewById(R.id.beepGoodStep);
        isBeepChecked = false;
        mBeepChecked.setOnClickListener(new BeepCheckedListener());

        mCaptureToFileChecked = (RadioButton) findViewById(R.id.captureToFile);
        isCaptureToFileChecked = false;
        mCaptureToFileChecked.setOnClickListener(new CaptureCheckedListener());

        mSimulateChecked = (RadioButton) findViewById(R.id.simulate);
        isSimulateChecked = false;
        mSimulateChecked.setOnClickListener(new SimulateCheckedListener());

        goodStepThreshold = -109; // default value from matlab
        mThresholdVal = findViewById(R.id.thresholdVal);
        mThresholdVal.setText("Threshold: " + goodStepThreshold + " d/s");
        mThreshold =(SeekBar) findViewById(R.id.thresholdBar);
        mThreshold.setProgress(-goodStepThreshold);
        mThreshold.setOnSeekBarChangeListener(new ThresholdListener());

        processFileButton = findViewById(R.id.processfileButton);
        processFileButton.setOnClickListener(new ProcessFileListener());

        folder = findViewById(R.id.folderLocation);
        folder.setText("set folder location for capture");
        folderIsSet = false;

        // setup the step detector
        stepDetect = new StepDetect();

        //this.thiscontext = container.getContext();
        this.contentResolver = getContentResolver();
        captureReady = false;
        outputStream = null;
        bw = null;
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

    /*
    private class FolderListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.putExtra("android.content.extra.SHOW_ADVANCED", true);
            startActivityForResult(intent, FOLDER_PICKER_CODE);
        }
    } */

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent,requestCode);
        String csvFile = null;
        if (intent != null && intent.getData() != null) {
            csvFile =  intent.getData().getPath();
        }
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        //  Handle activity result here
        List<String[]> inertialMeasurements;
        int sample = 0;
        double ms = 0;
        double GyroscopeX_ds =0;
        double GyroscopeY_ds =0;
        double GyroscopeZ_ds =0;

        int WRITE_REQUEST_CODE = 101;

        super.onActivityResult(requestCode, resultCode, intent);

        if (intent != null && intent.getData() != null) {
            Uri content_describer = intent.getData();
            if (requestCode == FOLDER_PICKER_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                   // String folderLocation = "Selected Folder: "+ intent.getExtras().getString("data");
                    folder.setText( intent.getData().getPath());
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
                try {
                    InputStream inputStream = this.contentResolver.openInputStream(content_describer);
                    inertialMeasurements = stepDetect.readCSV(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    // print an error message
                    return;
                }
                for (String[] sArray : inertialMeasurements) {
                    ms = sample*20;
                    boolean numeric = true;
                    try {
                        GyroscopeX_ds = Double.parseDouble(sArray[0]);
                        GyroscopeY_ds = Double.parseDouble(sArray[7]);
                        GyroscopeZ_ds = Double.parseDouble(sArray[4]);
                    } catch (NumberFormatException e) {
                        numeric = false;
                        System.out.println("header");
                    }
                    if (numeric) {
                        System.out.print("GyroscopeX_ds : " + GyroscopeX_ds + " GyroscopeY_ds : " +
                                GyroscopeY_ds + " GyroscopeZ_ds : " + GyroscopeZ_ds);
                        zGyroArrayFilt = stepDetect.filter(ms, GyroscopeX_ds, GyroscopeY_ds, GyroscopeZ_ds);
                        StepResults stepResults = stepDetect.detectStep(zGyroArrayFilt);
                        stepResults.timestamp = ms;
                        allStepResults.add(stepResults);
                        if (stepResults.goodstep && isBeepChecked) {
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
                            try {
                                Thread.sleep(20);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            goodstepResults.add(stepResults);
                        }
                        if (stepResults.badstep) {
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

                String results = stepDetect.stepResults(allStepResults,goodstepResults,badstepResults,sample,50);
                results = dataFilename + System.getProperty("line.separator") + results;
                mH2tstatus.setText(results);
                if (captureReady) {
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
                        mH2tstatus.setText("Error writing file");
                    }
                }
                closeCaptureStream();
            }
        }
    }
}
