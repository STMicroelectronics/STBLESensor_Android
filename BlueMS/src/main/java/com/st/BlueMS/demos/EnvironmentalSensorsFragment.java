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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureLuminosity;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.List;

/**
 * Show the Temperature, Humidity, lux and barometer values. Each value will have also its icon
 * that will change in base of the feature value
 */
@DemoDescriptionAnnotation(name="Environmental",iconRes=R.drawable.demo_environmental_sensor,
    requareOneOf = {FeatureHumidity.class,FeatureLuminosity.class,FeaturePressure.class,
            FeatureTemperature.class})
public class EnvironmentalSensorsFragment extends DemoFragment {

    /**
     * format used for print the different environmental values
     */
    private final static String TEMP_FORMAT = "%.1f [%s]";
    private final static String HUM_FORMAT ="%.1f [%s]";
    private final static String PRES_FORMAT="%.2f [%s]";
    private final static String LUX_FORMAT="%.1f [%s]";


    /**
     * get the string to show containing a value for each line
     * @param format format used for print the value and the unit
     * @param unit unit to use
     * @param values array with the value to print
     * @return string showing all the value, one for each line
     */
    private static String getDisplayString(final String format,final String unit,
                                           float values[]){
        StringBuilder sb = new StringBuilder();
        for(int i =0;i<values.length-1;i++){
            sb.append(String.format(format,values[i],unit));
            sb.append('\n');
        }//for
        sb.append(String.format(format,values[values.length-1],unit));
        return sb.toString();
    }


    private interface ExtractDataFunction{
        float getData(Feature.Sample s);
    }

    /**
     * get a sample for each feature and extract the float data from it
     * @param features list of feature
     * @param extractDataFunction object that will extract the data from the sample
     * @return list of values inside the feature
     */
    private static float[] extractData(final List<? extends Feature> features,
                                       final ExtractDataFunction extractDataFunction){
        int nFeature = features.size();
        float data[] = new float[nFeature];
        for(int i=0;i<nFeature;i++){
            data[i] = extractDataFunction.getData(features.get(i).getSample());
        }//for
        return data;
    }//extractData

    /**
     * function for clamp a value between the value min and max
     *
     * @param val value to clamp
     * @param min minimum value
     * @param max max value
     * @return min if val < val, max if val> max val otherwise
     */
    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    ////////////////////HUMIDITY///////////////////////////////////////////////////////////////////
    /**
     * feature where we will read the humidity_icon value
     */
    private List<FeatureHumidity> mHumidity;
    /**
     * label where we will show the humidity_icon value
     */
    private TextView mHumidityText;
    /**
     * humidity_icon icons, it became transparent in function of the humidity_icon
     */
    private ImageView mHumidityImage;
    /**
     * max allowed humidity_icon value for this application
     */
    private final static float HUMIDITY_MAX_VAL = 100.0f;
    /**
     * min allowed humidity_icon value for this application
     */
    private final static float HUMIDITY_MIN_VAL = 0.0f;

    /**
     * object that extract the humidity_icon from a feature sample
     */
    private final static ExtractDataFunction sExtractDataHum  = new ExtractDataFunction(){
        public float getData(Feature.Sample s){
            return FeatureHumidity.getHumidity(s);
        }//getData
    };

    /**
     * listener for the humidity_icon feature, it will update the humidity_icon value and change the alpha
     * value for the {@code mHumidityImage} image
     */
    private class HumidityListener implements Feature.FeatureListener {

        private Drawable image;

        public HumidityListener(Resources res){
            image = res.getDrawable(R.drawable.humidity_icon);
        }

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            String unit = mHumidity.get(0).getFieldsDesc()[0].getUnit();
            float data[] =extractData(mHumidity, sExtractDataHum);
            final String dataString = getDisplayString(HUM_FORMAT,unit,data);
            final int imageAlpha = (int) (100.0f *
                    (clamp(data[0], HUMIDITY_MIN_VAL, HUMIDITY_MAX_VAL) - HUMIDITY_MIN_VAL) /
                    (HUMIDITY_MAX_VAL - HUMIDITY_MIN_VAL));
            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {
                        mHumidityImage.setImageDrawable(image);
                        mHumidityImage.setImageAlpha(imageAlpha);
                        mHumidityText.setText(dataString);
                    } catch (NullPointerException e) {
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    }
                }
            });

        }//on update
    }
    //we cant initialize the listener here because we need to wait that the fragment is attached
    // to an activity
    private Feature.FeatureListener mHumidityListener;

    ////////////////////////////TEMPERATURE////////////////////////////////////////////////////////
    /**
     * feature where we can read the temperature value
     */
    private List<FeatureTemperature>  mTemperature;
    /**
     * label where we show the temperature value
     */
    private TextView mTemperatureText;
    /**
     * image where we will show the thermometer image
     */
    private ImageView mTemperatureImage;

    /**
     * object that extract the temperature from a feature sample
     */
    private final static ExtractDataFunction sExtractDataTemp  = new ExtractDataFunction(){
        public float getData(Feature.Sample s){
            return FeatureTemperature.getTemperature(s);
        }//getData
    };

    /**
     * listener for the humidity_icon feature, it will update the humidity_icon value and change the image
     * value for the {@code mTemperatureImage} image
     */
    private final Feature.FeatureListener mTemperatureListener = new Feature.FeatureListener() {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {

            String unit = mTemperature.get(0).getFieldsDesc()[0].getUnit();
            float data[] =extractData(mTemperature,sExtractDataTemp);
            final String dataString = getDisplayString(TEMP_FORMAT,unit,data);

            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {
                        mTemperatureText.setText(dataString);
                    }catch (NullPointerException e) {
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    }
                }
            });
        }//onUpdate
    };

    ////////////////////PRESSURE///////////////////////////////////////////////////////////////////
    /**
     * feature where we can read the pressure value
     */
    private List<FeaturePressure>  mPressure;
    /**
     * label where we show the pressure value
     */
    private TextView mPressureText;
    /**
     * image where we will show the barometer image
     */
    private ImageView mPressureImage;

    /**
     * object that extract the pressure from a feature sample
     */
    private final static ExtractDataFunction sExtractDataPres  = new ExtractDataFunction(){
        public float getData(Feature.Sample s){
            return FeaturePressure.getPressure(s);
        }//getData
    };

    /**
     * listener for the pressure feature, it will update the pressure value and change the image
     * value for the {@code mPressureImage} image
     */
    private final Feature.FeatureListener mPressureListener = new Feature.FeatureListener() {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            String unit = mPressure.get(0).getFieldsDesc()[0].getUnit();
            float data[] =extractData(mPressure, sExtractDataPres);
            final String dataString = getDisplayString(PRES_FORMAT, unit, data);
            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {
                        mPressureText.setText(dataString);
                    } catch (NullPointerException e) {
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    }//try-catch

                }
            });
        }
    };


    //////////////////////////////LUMINOSITY//////////////////////////////////////////////////////
    /**
     * feature where we read the luminosity value
     */
    private List<FeatureLuminosity>  mLuminosity;
    /**
     * label where write the luminosity value
     */
    private TextView mLuminosityText;
    /**
     * image where the show light bulb
     */
    private ImageView mLuminosityImage;

    /**
     * object that extract the luminosity from a feature sample
     */
    private final static ExtractDataFunction sExtractDataLux  = new ExtractDataFunction(){
        public float getData(Feature.Sample s){
            return FeatureLuminosity.getLuminosity(s);
        }//getData
    };

    /**
     * listener for the luminosity feature, it will update the luminosity value and change the image
     * value for the {@code mLuminosityImage} image
     */
    private final Feature.FeatureListener mLuminosityListener = new Feature.FeatureListener() {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {

            String unit = mLuminosity.get(0).getFieldsDesc()[0].getUnit();
            float data[] =extractData(mLuminosity, sExtractDataLux);
            final String dataString = getDisplayString(LUX_FORMAT, unit, data);

            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {

                        mLuminosityText.setText(dataString);

                    } catch (NullPointerException e) {
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    }
                }
            });
        }
    };

    public EnvironmentalSensorsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_eviromental_sensors, container, false);
        mHumidityText = (TextView) root.findViewById(R.id.humidityText);
        mHumidityImage = (ImageView) root.findViewById(R.id.humidityImage);


        mTemperatureText = (TextView) root.findViewById(R.id.thermometerText);
        mTemperatureImage = (ImageView) root.findViewById(R.id.thermometerImage);

        mPressureText = (TextView) root.findViewById(R.id.barometerText);
        mPressureImage = (ImageView) root.findViewById(R.id.barometerImage);

        mLuminosityText = (TextView) root.findViewById(R.id.luminosityText);
        mLuminosityImage = (ImageView) root.findViewById(R.id.luminosityImage);

        return root;
    }

    /**
     * enable the notification if the feature is not null + attach the listener for update the
     * gui when an update arrive + if you click the image we ask to read the value
     * if a feature is not available by the node a toast message is shown
     *
     * @param node node where the notification will be enabled
     */
    @Override
    protected void enableNeededNotification(@NonNull Node node) {

        mHumidity = node.getFeatures(FeatureHumidity.class);
        if(!mHumidity.isEmpty()) {
            View.OnClickListener forceUpdate = new ForceUpdateFeature(mHumidity);
            mHumidityListener = new HumidityListener(getResources());
            mHumidityImage.setOnClickListener(forceUpdate);
            for (Feature f : mHumidity) {
                f.addFeatureListener(mHumidityListener);
                node.enableNotification(f);
            }//for



        }else{
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mHumidityImage.setImageResource(R.drawable.humidity_missing);
                }
            });
        }

        mTemperature = node.getFeatures(FeatureTemperature.class);
        if(!mTemperature.isEmpty()) {
            View.OnClickListener forceUpdate = new ForceUpdateFeature(mTemperature);
            mTemperatureImage.setOnClickListener(forceUpdate);
            for (Feature f : mTemperature) {
                f.addFeatureListener(mTemperatureListener);
                node.enableNotification(f);
            }//for
        }else{
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mTemperatureImage.setImageResource(R.drawable.temperature_missing_icon);
                }
            });
        }

        mPressure = node.getFeatures(FeaturePressure.class);
        if(!mPressure.isEmpty()) {
            View.OnClickListener forceUpdate = new ForceUpdateFeature(mPressure);
            mPressureImage.setOnClickListener(forceUpdate);
            for (Feature f : mPressure) {
                f.addFeatureListener(mPressureListener);
                node.enableNotification(f);
            }//for
        }else{
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mPressureImage.setImageResource(R.drawable.pressure_missing_icon);
                }
            });
        }

        mLuminosity = node.getFeatures(FeatureLuminosity.class);
        if(!mLuminosity.isEmpty()) {
            View.OnClickListener forceUpdate = new ForceUpdateFeature(mLuminosity);
            mLuminosityImage.setOnClickListener(forceUpdate);
            for (Feature f : mLuminosity) {
                f.addFeatureListener(mLuminosityListener);
                node.enableNotification(f);
            }//for
        }else{
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mLuminosityImage.setImageResource(R.drawable.illuminance_missing);
                }
            });

        }

    }//enableNeededNotification


    /**
     * remove the listener and disable the notification
     *
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mHumidity!=null && !mHumidity.isEmpty()) {
            mHumidityImage.setOnClickListener(null);
            for (Feature f : mHumidity) {
                f.removeFeatureListener(mHumidityListener);
                node.disableNotification(f);
            }//for
        }

        if(mTemperature!=null && !mTemperature.isEmpty()) {
            mTemperatureImage.setOnClickListener(null);
            for (Feature f : mTemperature) {
                f.removeFeatureListener(mTemperatureListener);
                node.disableNotification(f);
            }//for
        }

        if(mPressure!=null && !mPressure.isEmpty()) {
            mPressureImage.setOnClickListener(null);
            for (Feature f : mPressure) {
                f.removeFeatureListener(mPressureListener);
                node.disableNotification(f);
            }//for
        }

        if(mLuminosity!=null && !mLuminosity.isEmpty()) {
            mLuminosityImage.setOnClickListener(null);
            for (Feature f : mLuminosity) {
                f.removeFeatureListener(mLuminosityListener);
                node.disableNotification(f);
            }//for
        }

    }//disableNeedNotification

    /**
     * simple callback class that will request to read the feature value when the user click on
     * the attached
     */
    static private class ForceUpdateFeature implements View.OnClickListener {

        /**
         * feature to read
         */
        private List<? extends Feature> mFeatures;

        /**
         * @param feature feature to read when the user click on a view
         */
        public ForceUpdateFeature(List<? extends Feature> feature) {
            mFeatures = feature;
        }

        /**
         * send the read request to the feature
         *
         * @param v clicked view
         */
        @Override
        public void onClick(View v) {
            for(Feature f: mFeatures) {
                Node node = f.getParentNode();
                if (node != null)
                    node.readFeature(f);
            }//if
        }//onClick

    }//ForceUpdateFeature
}
