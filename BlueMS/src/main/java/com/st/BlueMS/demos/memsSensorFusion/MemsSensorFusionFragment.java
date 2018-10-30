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

package com.st.BlueMS.demos.memsSensorFusion;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
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
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationContract;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationPresenter;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationView;
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
 * Fragment that show the feature and proximity feature
 * <p>
 * will show a cube that moves according to the quaternion computed by the mems sensor fusion
 * engine inside the board
 * if present the data from the proximity sensor will be used for change the cube size
 * </p>
 */
@DemoDescriptionAnnotation(name="Mems Sensor Fusion",iconRes=R.drawable.demo_sensors_fusion,
        requareOneOf = {FeatureMemsSensorFusion.class,FeatureMemsSensorFusionCompact.class})
public class MemsSensorFusionFragment extends DemoFragment implements CalibrationContract.View{
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

    /**
     * button used for reset the cube position
     */
    private Button mResetButton;
    /**
     * image used for tell if the system is calibrated or not
     */
    private ImageButton mCalibButton;
    /**
     * feature where we read the cube position
     */
    private FeatureAutoConfigurable mSensorFusion;
    private View mRootLayout;


    /**
     * this listener will count how mach sample we receive and will show the average number of
     * sample that we receive in a second
     */
    private class SensorFusionCountRateListener implements Feature.FeatureListener {

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

            final String rateText = getString(R.string.memsSensorFusion_frameRate,
                    mGlRenderer.getRenderingRate());

            final String quaternionText = getString(R.string.memsSensorFusion_quaternionRate,
                    averageQuaternionRate);
            //run
            updateGui(() -> {
                try {
                    mFrameRateText.setText(rateText);
                    mQuaternionRateText.setText(quaternionText);
                } catch (NullPointerException e) {
                    //this exception can happen when the task is run after the fragment is
                    // destroyed
                }//try -catch
            });
        }//onUpdate

    }

    private CalibrationContract.Presenter mCalibPresenter = new CalibrationPresenter();
    private CalibrationContract.View mCalibView;

    /**
     * class that update the cube position using the data from the sensor,
     * and change the calibration status of the system
     */
    private SensorFusionCountRateListener mSensorFusionListener = new SensorFusionCountRateListener();

    private void enableSensorFusion(@NonNull Node node){
        if(mSensorFusion!=null)
            return;

        mSensorFusion = getSensorFusion(node);

        if (mSensorFusion != null) {
            mSensorFusionListener.resetQuaternionRate();
            mSensorFusion.addFeatureListener(mSensorFusionListener);
            node.enableNotification(mSensorFusion);
        } else
            showActivityToast(R.string.memsSensorFusionNotFound);
    }

    private static @Nullable FeatureMemsSensorFusion getSensorFusion(@NonNull Node node){
        FeatureMemsSensorFusion sensorFusionFeature = node.getFeature(FeatureMemsSensorFusionCompact.class);
        if (sensorFusionFeature == null) {
            return node.getFeature(FeatureMemsSensorFusion.class);
        }
        return sensorFusionFeature;
    }

    private void disableSensorFusion(@NonNull  Node node){
        if (mSensorFusion != null) {
            mSensorFusion.removeFeatureListener(mSensorFusionListener);
            node.disableNotification(mSensorFusion);
            mSensorFusion=null;
        }
    }

    private void enableSensorFusionCalibration(){
        if(mSensorFusion!=null) {
            mCalibView = new CalibrationView(getFragmentManager(), mCalibButton);
            mCalibPresenter.manage(this, mSensorFusion);
        }
    }

    private void disableSensorFusionCalibration(){
        if(mSensorFusion!=null) {
            mCalibPresenter.unManageFeature();
        }
    }


    ///////////////////////////PROXIMITY//////////////////////////////////
    /**
     * button for enable/disable the proximity feature, it is show only when the feature is
     * present
     */
    private CompoundButton mProximityButton;
    /**
     * feature where read the proximity value
     */
    private Feature mProximity;

    /**
     * class that will update the cube size in function of the proximity values,
     * when out of range the cube will be reset to the original size
     */
    private Feature.FeatureListener mSensorProximity = new Feature.FeatureListener() {

        private final int MAX_DISTANCE = 200;
        private final float SCALE_FACTOR = 1.0f/MAX_DISTANCE;

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            int proximity = FeatureProximity.getProximityDistance(sample);
            if (proximity == FeatureProximity.OUT_OF_RANGE_VALUE)
                mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE);
            else {
                proximity = Math.min(proximity,MAX_DISTANCE);
                mGlRenderer.setScaleCube(proximity*SCALE_FACTOR);
            }
        }//onUpdate
    };


    private void enableProximity(@NonNull final Node node){

        mProximity = node.getFeature(FeatureProximity.class);
        if (mProximity != null) {
            /*
             * proximity sensor is present, show the button and attach the listener for
             * enable/disable the sensor reading
             */
            updateGui(() -> mProximityButton.setVisibility(View.VISIBLE));
            mProximity.addFeatureListener(mSensorProximity);
            if(mProximityButton.isChecked()) {
                node.enableNotification(mProximity);
            }
        }
    }

    private void disableProximity(@NonNull Node node) {
        if (mProximity != null) {
            mProximity.removeFeatureListener(mSensorProximity);
            node.disableNotification(mProximity);
            updateGui(() -> mProximityButton.setChecked(false));
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
                //run
                updateGui(() -> Snackbar.make(mRootLayout, R.string.wesu_motion_fx_freeFallDetected,
                        Snackbar.LENGTH_SHORT).show());// upadate Gui
            }//if
        }//onUpdate
    }//FreeFallListener

    private void enableFreeFall(@NonNull Node node){
        if(mFreeFallEvent!=null) //already enabled
            return;

        mFreeFallEvent = node.getFeature(FeatureAccelerationEvent.class);
        if(mFreeFallEvent!=null){
            mFreeFallListener = new FreeFallListener(requireActivity());
            mFreeFallEvent.detectEvent(FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT,false);
            mFreeFallEvent.detectEvent(FeatureAccelerationEvent.DetectableEvent.FREE_FALL,true);
            mFreeFallEvent.addFeatureListener(mFreeFallListener);
            node.enableNotification(mFreeFallEvent);
        }
    }

    private void disableFreeFall(@NonNull Node node) {
        if(mFreeFallEvent!=null){
            mFreeFallEvent.removeFeatureListener(mFreeFallListener);
            node.disableNotification(mFreeFallEvent);
            mFreeFallEvent=null;
        }//if
    }

    //////////////////////////////END FREE FALL////////////////////////////

    public MemsSensorFusionFragment() {
    }

    /**
     * get the fragment background color, to be used for fill the opengl background
     * <p>If the background is not a color we return white</p>
     *
     * @return fragment background color
     */
    private int getBgColor() {
        TypedValue a = new TypedValue();
        requireActivity().getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
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

        //avoid to recreate the cube each time the user rotate the cube
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_mems_sensor_fusion, container, false);
        mRootLayout = root.findViewById(R.id.memsSensorFusionRootLayout);
        mFrameRateText = root.findViewById(R.id.quaternionRateText);
        mQuaternionRateText = root.findViewById(R.id.renderingRateText);

        mCalibButton = root.findViewById(R.id.calibrationImage);
        /*
         * when the user will click on the button we will request to start the calibration
         * procedure
         */
        mCalibButton.setOnClickListener(v -> onStartCalibrationClicked());


        mResetButton = root.findViewById(R.id.resetButton);
        mResetButton.setOnClickListener(v -> onResetPositionButtonClicked());

        mProximityButton = root.findViewById(R.id.proximityButton);
        mProximityButton.setOnClickListener(v -> onProximityButtonClicked());

        mGlSurface = root.findViewById(R.id.glSurface);
        // Request an OpenGL ES 2.0 compatible context.
        mGlSurface.setEGLContextClientVersion(2);
        mGlRenderer = new GLCubeRender(getActivity(), getBgColor());
        mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE);
        mGlSurface.setRenderer(mGlRenderer);



        return root;
    }

    private void onProximityButtonClicked(){
        mGlRenderer.setScaleCube(INITIAL_CUBE_SCALE);
        Node node = getNode();
        if(mProximity==null || node==null ) //it is calibrating and the proximity is disabled
            return;

        if (node.isEnableNotification(mProximity))
            disableProximity(node);
        else
            enableProximity(node);
    }

    @Override
    protected void enableNeededNotification(@NonNull final Node node) {
        enableSensorFusion(node);
        enableProximity(node);
        enableFreeFall(node);
        enableSensorFusionCalibration();
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        disableSensorFusionCalibration();
        disableFreeFall(node);
        disableProximity(node);
        disableSensorFusion(node);
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
        mCalibButton =null;
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

        Activity activity = requireActivity();
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.memsSensorFusionInfoTitle);

        View view = activity.getLayoutInflater().inflate(R.layout
                .dialog_reset_sensor_fusion, null);

        TextView message = view.findViewById(R.id.dialog_reset_message);
        ImageView image = view.findViewById(R.id.dialog_reset_image);

        message.setText(messageId);
        image.setImageResource(imageId);

        dialog.setView(view);

        dialog.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> resetPosition());
        return dialog.create();
    }

    private @Nullable Dialog buildResetInfoDialog(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_STEVAL_WESU1,
                        R.drawable.steval_wesu1_reset_position);
            case SENSOR_TILE:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_nucleo,
                    R.drawable.ic_board_sensortile_bg);
            case NUCLEO:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_nucleo,
                        R.drawable.ic_board_nucleo_bg);
            case BLUE_COIN:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_nucleo,
                        R.drawable.ic_board_bluecoin_bg);
            case STEVAL_BCN002V1:
                return buildResetInfoDialog(R.string.memsSensorFusionDialogResetText_nucleo,
                        R.drawable.ic_board_bluenrgtile);
            case GENERIC:
            default:
                return null;
        }
    }

    private void onResetPositionButtonClicked(){
        Node node = getNode();
        if(node == null)
            return;
        Dialog dialog = buildResetInfoDialog(node.getType());
        if(dialog!=null)
            dialog.show();

    }


    /**
     * function called when the user request a new calibration
     */
    protected void onStartCalibrationClicked() {
        mCalibPresenter.startCalibration();
    }


    @Override
    public void showCalibrationDialog() {
        if(mCalibView!=null){
            mCalibView.showCalibrationDialog();
        }
    }

    @Override
    public void hideCalibrationDialog() {
        if(mCalibView!=null){
            mCalibView.hideCalibrationDialog();
        }
    }

    /**
     * when the calibration start, stop the free fall and proximity notification
     * @param isCalibrated true if the system is calibrated
     */
    @Override
    public void setCalibrationButtonState(boolean isCalibrated) {
        Node node = getNode();
        if(node!=null) {
            if (isCalibrated) {
                enableFreeFall(node);
                enableProximity(node);
            } else {
                disableFreeFall(node);
                disableProximity(node);
            }
        }
        if(mCalibView!=null){
            mCalibView.setCalibrationButtonState(isCalibrated);
        }
    }

}