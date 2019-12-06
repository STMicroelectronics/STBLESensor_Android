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

package com.st.BlueMS.demos;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Button;

import com.androidplot.xy.XYPlot;
import com.st.BlueMS.R;
import com.st.BlueMS.StepDetect;
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

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
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
public class H2TFeatureFragment extends BaseDemoFragment implements View.OnClickListener {

    private Context thiscontext;
    private boolean mIsPlotting;
    private XYPlot mChart;
    private ImageButton mStartPlotButton;

    /** domain axis label */
    private String mXAxisLabel;
    private TextView mAccelData;
    private TextView mGyroData;
    private TextView mH2tstatus;
    private TextView counttime;

    // timers fo rH2t session
    private Timer timer;
    private CountDownTimer countdown;
    private int maxSessionSeconds = 10;
    private int maxSessionMilliSeconds = maxSessionSeconds*1000;
    private  int counter;

    // step detection
    private StepDetect stepDetect;
    private double[] zGyroArrayFilt;

    private Button processFileButton;

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

    // TED for H2t sampling
    List<Feature> h2tFeatures = null;
    private List<FeatureGyroscope> mH2TgyroFeature;
    private  Feature.FeatureListener mH2TgyroFeatureListener;

    private List<FeatureAcceleration>  mH2TaccelFeature;
    private  Feature.FeatureListener mH2TaccelFeatureListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_h2t_feature, container, false);

        mStartPlotButton = root.findViewById(R.id.startPlotButton);
        mStartPlotButton.setOnClickListener(this);
        mStartPlotButton.setEnabled(false);

        mAccelData = root.findViewById(R.id.accelData);
        mAccelData.setText("Aceleration data");
        mGyroData = root.findViewById(R.id.gyroData);
        mGyroData.setText("GyroScope data");
        Resources res = getResources();
        mH2tstatus = root.findViewById(R.id.h2tstatus);
        mH2tstatus.setText("No steps detected");
        counttime= root.findViewById(R.id.counttime);
        counttime.setText("Idle. Max session = "+ String.valueOf(maxSessionSeconds)+" seconds");
        mXAxisLabel = "time (ms)";

        processFileButton = root.findViewById(R.id.processfileButton);
        processFileButton.setOnClickListener(this);

        // setup the step detector
        stepDetect = new StepDetect();

        thiscontext = container.getContext();

        return root;
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
     * @param f feature to plot
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
        timer.schedule(new RemindTask(), maxSessionMilliSeconds);

        countdown = new CountDownTimer(maxSessionMilliSeconds,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                counttime.setText(String.valueOf(maxSessionSeconds-counter));
                counter++;
            }
            @Override
            public void onFinish() {
                counter = 0;
                counttime.setText("Finished");
            }
        }.start();
    }

    /**
     * start Heel2toe processing  for feature data and enable the feature
     * @param f feature to plot
     */
    public void stopH2tFeature() {
        Node node = getNode();
        if(node==null)
            return;

        if(mH2TgyroFeature != null && !mH2TgyroFeature.isEmpty()) {
            //View.OnClickListener forceUpdate = new ForceUpdateFeature(mHumidity);
            //mHumidityImage.setOnClickListener(forceUpdate);
            for (Feature f : mH2TgyroFeature) {
                f.removeFeatureListener(mH2TgyroFeatureListener);
                node.disableNotification(f);
            }//for
        }

        if(mH2TaccelFeature != null && !mH2TaccelFeature.isEmpty()) {
            //View.OnClickListener forceUpdate = new ForceUpdateFeature(mHumidity);
            //mHumidityImage.setOnClickListener(forceUpdate);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startPlotButton:
                if (mIsPlotting) {
                    stopH2tFeature();
                    setButtonStartStatus();
                    h2tSummary();
                } else {
                    startH2tFeature(); // TED
                    setButtonStopStatus();
                }//if-else
                break;

            case R.id.processfileButton:
                List<String[]> csvData = stepDetect.readCSV(thiscontext, "data.csv");
                int x = 1;
                break;

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
