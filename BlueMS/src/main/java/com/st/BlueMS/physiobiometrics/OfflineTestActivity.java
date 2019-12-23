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
import java.util.ArrayList;
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
    private TextView mAccelData;
    private TextView mGyroData;
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

    private SeekBar mMaxTimeBar;
    protected TextView mMaxtime;
    private TextView mCcounttime;
    private int stopwatch;

    // step detection
    private StepDetect stepDetect;
    private double[] zGyroArrayFilt;
    List<StepResults> allStepResults = new ArrayList<StepResults>();
    List<StepResults> goodstepResults = new ArrayList<StepResults>();
    List<StepResults> badstepResults = new ArrayList<StepResults>();

    // TED for H2t sampling
    List<Feature> h2tFeatures = null;
    private List<FeatureGyroscope> mH2TgyroFeature;
    private  Feature.FeatureListener mH2TgyroFeatureListener;

    private List<FeatureAcceleration>  mH2TaccelFeature;
    private  Feature.FeatureListener mH2TaccelFeatureListener;

    private static final int WRITE_REQUEST_CODE = 101;

    ToneGenerator toneGen1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_h2tfile_feature);

        mAccelData = findViewById(R.id.accelData);
        mAccelData.setText("Aceleration data");
        mGyroData = findViewById(R.id.gyroData);
        mGyroData.setText("GyroScope data");

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

        // setup the step detector
        stepDetect = new StepDetect();

        //this.thiscontext = container.getContext();
        this.contentResolver = getContentResolver();
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

    private class CaptureCheckedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isCaptureToFileChecked) {
                mCaptureToFileChecked.setChecked(false);
                isCaptureToFileChecked = false;
            } else {
                mCaptureToFileChecked.setChecked(true);
                isCaptureToFileChecked = true;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                // filter to only show openable items.
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Create a file with the requested Mime type
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, "Neonankiti.txt");
                startActivityForResult(intent, WRITE_REQUEST_CODE);
            }
        }
    }
    private class SimulateCheckedListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (isSimulateChecked) {
                mSimulateChecked.setChecked(false);
                isSimulateChecked = false;
            } else {
                mSimulateChecked.setChecked(true);
                isSimulateChecked = true;
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

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent,requestCode);
        String csvFile = null;
        if (intent != null && intent.getData() != null) {
            csvFile =  intent.getData().getPath();
        }
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
            if (requestCode == WRITE_REQUEST_CODE) {
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        try {
                            OutputStream outputStream = contentResolver.openOutputStream(content_describer);
                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));
                            bw.write("bison is bision");
                            bw.flush();
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case Activity.RESULT_CANCELED:
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
                mH2tstatus.setText(results);
            }
        }
    }

}
