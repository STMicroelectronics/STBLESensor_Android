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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.GLCubeRender;
import com.st.BlueMS.demos.util.HidableTextView;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureAutoConfigurable;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeatureProximity;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.concurrent.atomic.AtomicLong;

/**
 * fragment that show the feature and proximity feature
 * <p>
 * will show a cube that moves according to the quaternion computed by the mems sensor fusion
 * engine inside the board
 * if present the data from the proximity sensor will be used for change the cube size
 * </p>
 */
@DemoDescriptionAnnotation(name="Mems Sensor Fusion",iconRes=R.drawable.demo_sensors_fusion,
    requareOneOf = {FeatureMemsSensorFusion.class,FeatureMemsSensorFusionCompact.class})
public class MemsSensorFusionFragment extends DemoFragment {
    private final static String TAG = MemsSensorFusionFragment.class.getCanonicalName();

    /**
     * initial size of the cube
     */
    private final static float INITIAL_CUBE_SCALE = 1.0f;

    /**
     * text that is hide if clicked, that contains the screen refresh rate
     */
    private HidableTextView mFrameRateText;
    /**
     * text that is hide if clicked, that contains the number of quaternions that we receive in a
     * second
     */
    private HidableTextView mQuaternionRateText;

    /**
     * opengl surface used for paint the cube
     */
    private GLSurfaceView mGlSurface;
    /**
     * renderer used for paint the cube
     */
    private GLCubeRender mGlRenderer;

    private boolean mShowResetDialog;
    private boolean mShowCalibrateDialog;

    /**
     * true if the node is calibrated, false otherwise
     */
    private boolean mCalibState = false;

    /**
     * task that will change the button image in function of the calibration status
     */
    private Runnable mChangeCalibrationButtonState = new Runnable() {
        @Override
        public void run() {
            final Resources r = getResources();
            if (mCalibState)
                mCalibrationImage.setImageDrawable(r.getDrawable(R.drawable.calibrated));
            else
                mCalibrationImage.setImageDrawable(r.getDrawable(R.drawable.uncalibrated));
        }//run
    };

    /**
     * button used for reset the cube position
     */
    private Button mResetButton;
    /**
     * image used for tell if the system is calibrated or not
     */
    private ImageButton mCalibrationImage;
    /**
     * feature where we read the cube position
     */
    private FeatureAutoConfigurable mSensorFusion;
    private View mRootLayout;

    /**
     * this listener will count how mach sample we receive and will show the average number of
     * sample that we receive in a second
     */
    private class SensorFusionCountRateListener implements
            FeatureAutoConfigurable.FeatureAutoConfigurationListener {

        /**
         * time of when we receive the first sample
         */
        private long mFistQuaternionTime = -1;
        /**
         * number of sample that we received
         */
        private AtomicLong mNQuaternion = new AtomicLong(0);

        void resetQuaternionRate() {
            mFistQuaternionTime = -1;
            mNQuaternion.set(0);
        }

        @Override
        public void onAutoConfigurationStatusChanged(FeatureAutoConfigurable f, int status) {
            setCalibrationStatus(f.isConfigured());
        }

        @Override
        public void onUpdate(Feature f,Feature.Sample data) {
            if (mFistQuaternionTime < 0)
                mFistQuaternionTime = System.currentTimeMillis();
            //+1 for avoid division by 0 the first time that we initialize mFistQuaternionTime
            long averageQuaternionRate = (mNQuaternion.incrementAndGet() * 1000) /
                    (System.currentTimeMillis() - mFistQuaternionTime + 1);

            //update the cube rotation
            mGlRenderer.setRotation(FeatureMemsSensorFusionCompact.getQi(data),
                    FeatureMemsSensorFusionCompact.getQj(data),
                    FeatureMemsSensorFusionCompact.getQk(data),
                    FeatureMemsSensorFusionCompact.getQs(data));

            final String rateText = String.format("Frame Rate :%03d fps",
                    mGlRenderer.getRenderingRate());

            final String quaternionText = String.format("Quaternion Rate: %03d",
                    averageQuaternionRate);
            updateGui(new Runnable() {
                @Override
                public void run() {
                    try {
                        mFrameRateText.setText(rateText);
                        mQuaternionRateText.setText(quaternionText);
                    } catch (NullPointerException e) {
                        //this exception can happen when the task is run after the fragment is
                        // destroyed
                    }//try -catch
                }//run
            });
        }//onUpdate

        @Override
        public void onAutoConfigurationStarting(FeatureAutoConfigurable f) {

        }

        @Override
        public void onConfigurationFinished(FeatureAutoConfigurable f, int status) {

        }

    }

    /**
     * class that update the cube position using the data from the sensor,
     * and change the calibration status of the system
     */
    private SensorFusionCountRateListener mSensorFusionListener = new SensorFusionCountRateListener();

    private void enableSensorFusion(Node node){
        if (mSensorFusion != null) {
            mSensorFusionListener.resetQuaternionRate();
            mSensorFusion.addFeatureListener(mSensorFusionListener);
            node.enableNotification(mSensorFusion);
            setCalibrationStatus(mSensorFusion.isConfigured());
            //we force to run the mChangeCalibrationButtonState, since if we return in this
            // fragment we can have the same status (true) but the button is reset to the default
            // image (false state -> not calibrated)
            updateGui(mChangeCalibrationButtonState);
        } else
            showActivityToast(R.string.memsSensorFusionNotFound);
    }

    private void disableSensorFusion(@NonNull  Node node){
        if (mSensorFusion != null) {
            mSensorFusion.removeFeatureListener(mSensorFusionListener);
            node.disableNotification(mSensorFusion);
        }
    }


    ///////////////////////////PROXIMITY//////////////////////////////////
    /**
     * button for enable/disable the proximity feature, it is show only when the feature is
     * present
     */
    private ToggleButton mProximityButton;
    /**
     * feature where read the proximity value
     */
    private Feature mProximity;

    /**
     * class that will update the cube size in function of the proximity values,
     * when out of range the cube will be reset to the original size
     */
    private Feature.FeatureListener mSensorProximity = new Feature.FeatureListener() {
        private static final float PROXIMITY_SCALE_FACTOR = 1.0f /
                (FeatureProximity.DATA_MAX - FeatureProximity.DATA_MIN);

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            int proximity = FeatureProximity.getProximityDistance(sample);
            if (proximity == FeatureProximity.OUT_OF_RANGE_VALUE)
                mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE);
            else
                mGlRenderer.setScaleCube(((proximity - FeatureProximity.DATA_MIN) *
                        PROXIMITY_SCALE_FACTOR));
        }//onUpdate
    };


    private void enableProximity(Node node){
        if (mProximity != null) {
            /**
             * proximity sensor is present, show the button and attach the listener for
             * enable/disable the sensor reading
             */
            mProximityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE);
                    if (node.isEnableNotification(mProximity))
                        node.disableNotification(mProximity);
                    else
                        node.enableNotification(mProximity);
                }//onClick
            });
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mProximityButton.setVisibility(View.VISIBLE);
                }
            });
            mProximity.addFeatureListener(mSensorProximity);
            if(mProximityButton.isChecked()) {
                node.enableNotification(mProximity);
            }
        } else {
            //    showActivityToast(R.string.proximityNotFound);
        }
    }

    private void disableProximity(@NonNull Node node) {
        if (mProximity != null) {
            mProximity.removeFeatureListener(mSensorProximity);
            node.disableNotification(mProximity);
        }
    }
    /////////////////////// END PROXIMITY ////////////////////////////////////

    ////////////////////////////FREE FALL///////////////////////////////////////////
    private FeatureAccelerationEvent mFreeFallEvent;
    private Feature.FeatureListener mFreeFallListener;

    /**
     * Show a toast and vibrate the mobile when a free fall event is detected
     */
    private class FreeFallListener implements Feature.FeatureListener{

        private Vibrator mVibratorManager;
        private int mVibrationTime;
        private long mLastNotificationTime;
        private long minEventDelay;

        FreeFallListener(Context c){
            minEventDelay =c.getResources().getInteger(R.integer.wesu_motion_fx_free_fall_notification_delay);
            mVibrationTime =c.getResources().getInteger(R.integer.wesu_motion_fx_free_fall_vibration_duration);
            mVibratorManager = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if(!FeatureAccelerationEvent.hasAccelerationEvent(sample,
                    FeatureAccelerationEvent.FREE_FALL))
                return;

            if (sample.notificationTime - mLastNotificationTime > minEventDelay) {
                mLastNotificationTime =sample.notificationTime;

                mVibratorManager.vibrate(mVibrationTime);
                updateGui(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(mRootLayout, R.string.wesu_motion_fx_freeFallDetected,
                                Snackbar.LENGTH_SHORT).show();
                    }//run
                });// upadate Gui
            }//if
        }//onUpdate
    }//FreeFallListener

    private void enableFreeFall(Node node){
        mFreeFallEvent = node.getFeature(FeatureAccelerationEvent.class);
        if(mFreeFallEvent!=null){
            mFreeFallListener = new FreeFallListener(getActivity());
            mFreeFallEvent.addFeatureListener(mFreeFallListener);
            mFreeFallEvent.detectEvent(FeatureAccelerationEvent.DetectableEvent.FREE_FALL,true);
            node.enableNotification(mFreeFallEvent);
        }
    }

    private void disableFreeFall(@NonNull Node node) {
        if(mFreeFallEvent!=null){
            mFreeFallEvent.removeFeatureListener(mFreeFallListener);
            mFreeFallEvent.detectEvent(FeatureAccelerationEvent.DetectableEvent.FREE_FALL,false);
            node.disableNotification(mFreeFallEvent);
        }//if
    }

    //////////////////////////////END FREE FALL////////////////////////////

    public MemsSensorFusionFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Node node = getNode();
        if (node != null) {
            mSensorFusion = node.getFeature(FeatureMemsSensorFusionCompact.class);
            if (mSensorFusion == null) {
                mSensorFusion = node.getFeature(FeatureMemsSensorFusion.class);
            }
            mProximity = node.getFeature(FeatureProximity.class);
        }
    }

    /**
     * get the fragment background color, to be used for fill the opengl background
     * <p>If the background is not a color we return white</p>
     *
     * @return fragment background color
     */
    private int getBgColor() {
        TypedValue a = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color
            return a.data;
        } else {
            // windowBackground is not a color, probably a drawable
            return Color.WHITE;
        }//if else
    }

    /**
     * we create the renderer hire for start early early the texture reading
     *
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mShowCalibrateDialog=false;
        mShowResetDialog=false;

        //avoid to recreate the cube each time the user rotate the cube
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_mems_sensor_fusion, container, false);
        mRootLayout = root.findViewById(R.id.memsSensorFusionRootLayout);
        mFrameRateText = (HidableTextView) root.findViewById(R.id.quaternionRateText);
        mQuaternionRateText = (HidableTextView) root.findViewById(R.id.renderingRateText);

        mCalibrationImage = (ImageButton) root.findViewById(R.id.calibrationImage);

        /**
         * when the user will click on the button we will request to start the calibration
         * procedure
         */
        mCalibrationImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               onCalibrateButtonClicked();
            }//onClick
        });


        mResetButton = (Button) root.findViewById(R.id.resetButton);
        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onResetPositionButtonClicked();
            }//onClick
        });


        mGlSurface = (GLSurfaceView) root.findViewById(R.id.glSurface);
        // Request an OpenGL ES 2.0 compatible context.
        mGlSurface.setEGLContextClientVersion(2);
        mGlRenderer = new GLCubeRender(getActivity(), getBgColor());
        mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE);
        mGlSurface.setRenderer(mGlRenderer);

        mProximityButton = (ToggleButton) root.findViewById(R.id.proximityButton);

        return root;
    }

    @Override
    protected void enableNeededNotification(@NonNull final Node node) {

        enableSensorFusion(node);
        enableProximity(node);
        enableFreeFall(node);

    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        disableSensorFusion(node);
        disableProximity(node);
        disableFreeFall(node);
    }

    @Override
    public void onResume() {
        super.onResume();
        mGlSurface.onResume();
        mGlRenderer.resetCube();
    }


    @Override
    public void onPause() {
        mGlSurface.onPause();
        super.onPause();
    }

    @Override
    public void onDestroyView (){
        mFrameRateText=null;
        mQuaternionRateText=null;
        mGlSurface=null;
        mCalibrationImage=null;
        mResetButton=null;
        mProximityButton=null;
        super.onDestroyView();
    }


    private void resetPosition(){
        if (mGlRenderer != null) {
            mGlRenderer.resetCube();
        }//if
    }

    private Dialog buildResetInfoDialog(@StringRes int messageId, @DrawableRes int imageId){

        Activity activity = getActivity();
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.memsSensorFusionInfoTitle);

        View view = activity.getLayoutInflater().inflate(R.layout
                .dialog_reset_sensor_fusion, null);

        TextView message = (TextView) view.findViewById(R.id.dialog_reset_message);
        ImageView image = (ImageView) view.findViewById(R.id.dialog_reset_image);

        message.setText(messageId);
        image.setImageResource(imageId);

        dialog.setView(view);

        dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                resetPosition();
            }
        });
        return dialog.create();
    }

    private @Nullable Dialog buildResetInfoDialog(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_STEVAL_WESU1,
                        R.drawable.steval_wesu1_reset_position);
            case SENSOR_TILE:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_nucleo,
                    R.drawable.tile_reset_position);
            case NUCLEO:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_nucleo,
                        R.drawable.nucleo_reset_position);
            case BLUE_COIN:
            case GENERIC:
            default:
                return null;
        }
    }

    private void onResetPositionButtonClicked(){
        if(!mShowResetDialog) {
            Node node = getNode();
            if(node == null)
                return;
            Dialog dialog = buildResetInfoDialog(node.getType());
            if(dialog!=null)
                dialog.show();
            mShowResetDialog=true;
        }else
            resetPosition();
    }

    protected void calibrateSensorFusion() {
        if (mSensorFusion != null) {
            mSensorFusion.startAutoConfiguration();
            setCalibrationStatus(mSensorFusion.isConfigured());
        }//if
    }

    private Dialog buildCalibrationInfoDialog(){
        Activity activity = getActivity();
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.memsSensorFusionInfoTitle);
        dialog.setView(activity.getLayoutInflater().inflate(R.layout
                .dialog_calibration_sensor_fusion,null));
        dialog.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                calibrateSensorFusion();
            }
        });
        return dialog.create();
    }

    private void onCalibrateButtonClicked(){
        if(!mShowCalibrateDialog){
            buildCalibrationInfoDialog().show();
            mShowCalibrateDialog=true;
        }else
            calibrateSensorFusion();
    }

    /**
     * change the calibration status, and update the button image
     *
     * @param calibState new calibration status
     */
    protected void setCalibrationStatus(boolean calibState) {
        if (mCalibState != calibState) {
            mCalibState = calibState;
            updateGui(mChangeCalibrationButtonState);
        }//if
    }//setState

}
