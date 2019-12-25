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
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.PlotFeatureFragment;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureAccelerationNorm;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureGyroscopeNorm;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMagnetometerNorm;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
/**
 * Fragment that plot the feature data in an xy plot
 */
@DemoDescriptionAnnotation(name="Heel2toe detector",iconRes=R.drawable.demo_charts,
        requareOneOf = {FeatureAcceleration.class,
                FeatureGyroscope.class,
                FeatureMagnetometer.class,
                FeatureAccelerationNorm.class,
                FeatureMagnetometerNorm.class,
                FeatureGyroscopeNorm.class
        })
public class H2TFeatureFragment extends BaseDemoFragment implements View.OnClickListener  {

    private Context thiscontext;
    private ContentResolver contentResolver;
    private boolean mIsPlotting;
    private ImageButton mStartPlotButton;

    /** domain axis label */
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
    private boolean isSimulateChecked;
    protected SeekBar mThreshold;
    private TextView mThresholdVal;
    private int goodStepThreshold;

    private SeekBar mMaxTimeBar;
    protected TextView mMaxtime;
    private TextView mCcounttime;

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
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private int Xcoord = X;
    private int Ycoord = Y;
    private int Zcoord = Z;

    private int frequency;
    private Spinner spinnerFrequency;

    private OutputStream outputStream;
    private boolean captureReady;
    private String dataFilename;

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
    private static final int SDCARD_PERMISSION = 1,
            FOLDER_PICKER_CODE = 2,
            FILE_PICKER_CODE = 3;

    private TextView folder;
    private boolean folderIsSet;

    ToneGenerator toneGen1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_h2t_feature, container, false);

        mStartPlotButton = root.findViewById(R.id.startPlotButton);
        mStartPlotButton.setOnClickListener(new ProcessListener());
        mStartPlotButton.setEnabled(false);

        mAccelData = root.findViewById(R.id.accelData);
        mAccelData.setText("Aceleration data");
        mGyroData = root.findViewById(R.id.gyroData);
        mGyroData.setText("GyroScope data");
        Resources res = getResources();
        mH2tstatus = root.findViewById(R.id.h2tstatus);
        mH2tstatus.setText("Ready for Walk-Well analysis. Press start button then walk. Process file to simulate. ");

        mBeepChecked = (RadioButton) root.findViewById(R.id.beepGoodStep);
        isBeepChecked = false;
        mBeepChecked.setOnClickListener(new BeepCheckedListener());

        mCaptureToFileChecked = (RadioButton) root.findViewById(R.id.captureToFile);
        isCaptureToFileChecked = false;
        mCaptureToFileChecked.setOnClickListener(new CaptureCheckedListener());

        spinnerX  = (Spinner) root.findViewById(R.id.spinnerX);
        spinnerX.setSelection(X);
        spinnerX.setOnItemSelectedListener(new SpinnerXListener());
        spinnerY  = (Spinner) root.findViewById(R.id.spinnerY);
        spinnerY.setSelection(Y);
        spinnerY.setOnItemSelectedListener(new SpinnerYListener());
        spinnerZ  = (Spinner) root.findViewById(R.id.spinnerZ);
        spinnerZ.setSelection(Z);
        spinnerZ.setOnItemSelectedListener(new SpinnerZListener());

        frequency = 50;
        spinnerFrequency  = (Spinner) root.findViewById(R.id.frequency);
        spinnerFrequency.setSelection(0);
        spinnerFrequency.setOnItemSelectedListener(new SpinnerFrequencyListener());

        goodStepThreshold = -109; // default value from matlab
        mThresholdVal = root.findViewById(R.id.thresholdVal);
        mThresholdVal.setText("Threshold: " + goodStepThreshold + " d/s");
        mThreshold =(SeekBar) root.findViewById(R.id.thresholdBar);
        mThreshold.setProgress(-goodStepThreshold);
        mThreshold.setOnSeekBarChangeListener(new ThresholdListener());

        mCcounttime= root.findViewById(R.id.counttime);
        mCcounttime.setText("0");
        mMaxtime = root.findViewById(R.id.maxtime);
        mMaxtime.setText("Max: "+ String.valueOf(maxSessionSeconds)+" s");
        mMaxTimeBar =(SeekBar) root.findViewById(R.id.MaxTimeBar);
        mMaxTimeBar.setProgress(maxSessionSeconds);
        mMaxTimeBar.setOnSeekBarChangeListener(new MaxTimeListener());

        folder = root.findViewById(R.id.folderLocation);
        folder.setText("set folder location for capture");
        folderIsSet = false;

        // setup the step detector
        stepDetect = new StepDetect();

        captureReady = false;
        outputStream = null;

        this.thiscontext = container.getContext();
        this.contentResolver = thiscontext.getContentResolver();
        toneGen1 = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);

        return root;
    }

    public class RemindTask extends TimerTask {
        public void run() {
            mH2tstatus.setText("Timeout. Processing data");
            timer.cancel(); //Terminate the timer thread
            mIsPlotting = false;
            stopH2tFeature();
            setButtonStartStatus();
        }
    }

    private class H2TgyroListener implements Feature.FeatureListener {

        @Override
        public void onUpdate(final Feature f,Feature.Sample sample) {
            long timestamp = sample.timestamp;
            final String dataString = f.toString();

            //zGyroArrayFilt = stepDetect.filter(sample.timestamp,
            //        (double) sample.data[0], (double) sample.data[1], (double) sample.data[2]);
            //StepResults stepResults = stepDetect.detectStep(zGyroArrayFilt);

            updateGui(() -> {
                try {
                    mGyroData.setText(dataString);
                } catch (NullPointerException e) {
                    //this exception can happen when the task is run after the fragment is
                    // destroyed
                }
            });
        }//onUpdate
    }
    private class H2TaccelListener implements Feature.FeatureListener {

        @Override
        public void onUpdate(final Feature f,Feature.Sample sample) {
            long timestamp = sample.timestamp;
            final String dataString = f.toString();
            updateGui(() -> {
                try {
                    mAccelData.setText(dataString);
                } catch (NullPointerException e) {
                    //this exception can happen when the task is run after the fragment is
                    // destroyed
                }
            });
        }//onUpdate
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


    private class ThresholdListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            goodStepThreshold = -progress;
            mThresholdVal.setText("Threshold: " + goodStepThreshold + " d/s");
        }
        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    }

   private class MaxTimeListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            maxSessionSeconds = progress;
            mMaxtime.setText("Max: "+ String.valueOf(maxSessionSeconds)+" s");
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

    private class ProcessListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mIsPlotting) {
                stopH2tFeature();
                setButtonStartStatus();
                h2tSummary();
            } else {
                startH2tFeature(); // TED
                setButtonStopStatus();
            }//if-else
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
    /*
     * free the element gui for permit to the gc to free it before recreate the fragment
     */
    @Override
    public void onDestroyView (){
        mStartPlotButton=null;
        super.onDestroyView();
    }

    /**
     * start Heel2toe processing  for feature data and enable the feature
     *
     */
    public void startH2tFeature() {
        Node node = getNode();
        if(node==null)
            return;

        mH2TgyroFeature = node.getFeatures(FeatureGyroscope.class);
        if(!mH2TgyroFeature.isEmpty()) {
            //View.OnClickListener forceUpdate = new ForceUpdateFeature(mHumidity);
            mH2TgyroFeatureListener = new H2TgyroListener();
            //mHumidityImage.setOnClickListener(forceUpdate);
            for (Feature f : mH2TgyroFeature) {
                f.addFeatureListener(mH2TgyroFeatureListener);
                node.enableNotification(f);
            }//for
        }

        mH2TaccelFeature = node.getFeatures(FeatureAcceleration.class);
        if(!mH2TaccelFeature.isEmpty()) {
            //View.OnClickListener forceUpdate = new ForceUpdateFeature(mHumidity);
            mH2TaccelFeatureListener = new H2TaccelListener();
            //mHumidityImage.setOnClickListener(forceUpdate);
            for (Feature f : mH2TaccelFeature) {
                f.addFeatureListener(mH2TaccelFeatureListener);
                node.enableNotification(f);
            }//for
        }
        mIsPlotting = true;
        mH2tstatus.setText("Step Detection in Progress");

        timer = new Timer();
        int maxSessionMilliSeconds = maxSessionSeconds * 1000;
        timer.schedule(new RemindTask(), maxSessionMilliSeconds);

        countdown = new CountDownTimer(maxSessionMilliSeconds,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mCcounttime.setText(String.valueOf(maxSessionSeconds-counter));
                counter++;
            }
            @Override
            public void onFinish() {
                counter = 0;
                mCcounttime.setText("Finished");
            }
        }.start();
    }

    /**
     * start Heel2toe processing  for feature data and enable the feature
     */
    public void stopH2tFeature() {
        Node node = getNode();
        if(node==null)
            return;

        if(mH2TgyroFeature != null && !mH2TgyroFeature.isEmpty()) {
            for (Feature f : mH2TgyroFeature) {
                f.removeFeatureListener(mH2TgyroFeatureListener);
                node.disableNotification(f);
            }//for
        }

        if(mH2TaccelFeature != null && !mH2TaccelFeature.isEmpty()) {
            for (Feature f : mH2TaccelFeature) {
                f.removeFeatureListener(mH2TaccelFeatureListener);
                node.disableNotification(f);
            }//for
        }
        mIsPlotting = false;
    }

    private void setButtonStopStatus(){
        mStartPlotButton.setImageResource(R.drawable.ic_stop);
        mStartPlotButton.setContentDescription("Start");
    }

    private void setButtonStartStatus(){
        mStartPlotButton.setImageResource(R.drawable.ic_play_arrow);
        mStartPlotButton.setContentDescription("Stop");
    }

    private void h2tSummary() {
        mH2tstatus.setText("Step Detection Complete. You are a good walker!!!");
    }

    /**
     * call when the user click on the button, will start/ai_log_stop plotting the data for the selected
     * feature
     * @param v clicked item (not used)
     */
    @Override
    public void onClick(View v) {}

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

        super.onActivityResult(requestCode, resultCode, intent);

        if (intent != null && intent.getData() != null) {
            Uri content_describer = intent.getData();
            if (requestCode == WRITE_REQUEST_CODE) {
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
            }
        }
    }

    /**
     * after a screen rotation the gui item are recreated so we have to restore the status
     * as before the the rotation, this method is called only if we rotate the screen when we are
     * plotting something
     */
    private void restoreGui(){
        //restore the plot
        //restoreChart();
        //we are plotting something -> change the button label
        setButtonStopStatus();
    }

    private static List<Class<? extends Feature>> getSupportedFeatures(){
        Class<? extends Feature>[] temp =
                PlotFeatureFragment.class.getAnnotation(DemoDescriptionAnnotation.class).requareOneOf();

        return Arrays.asList(temp);
    }

    /**
     * we enable the button for start plotting the data
     * @param node node where the notification will be enabled
     */
    @Override
    protected void enableNeededNotification(@NonNull final Node node) {
        //run
        updateGui(() -> {
            mStartPlotButton.setEnabled(true);
        });
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node){

    }
}
