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


import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureMemsGesture;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;


/**
 * Demo for show the gesture recognized by the mems sensors
 */
@DemoDescriptionAnnotation(name="Mems Gesture", requareAll = {FeatureMemsGesture.class},
        iconRes=R.drawable.demo_sensors_fusion)
public class MemsGestureRecognitionFragment extends DemoFragment {

    private static long AUTOMATIC_DESELECT_TIMEOUT_MS = 3000;

     /**
     * filter for convert a color image in a gray scale one
     */
    private static ColorMatrixColorFilter sToGrayScale;

    static{
        ColorMatrix temp = new ColorMatrix();
        temp.setSaturation(0.0f);
        sToGrayScale = new ColorMatrixColorFilter(temp);
    }//static initializer

    /**
     * image link to the tap event
     */
    private ImageView mPickUpImage;

    /**
     * image link to the left event
     */
    private ImageView mWakeUpImage;

    /**
     * image link to the right event
     */
    private ImageView mGlanceImage;

    /**
     *
     */
    private ImageView mCurrentImage;
    private long mCurrentTimestamp;

    private Animation mScaleDownAnim;

    /**
     * feature where we will read the position
     */
    private Feature mGestureRecognition;

    private Handler mDeselectImageTaskQueue;

    /**
     * listener that will animate selected image
     */
    private Feature.FeatureListener mActivityListener = new  Feature.FeatureListener () {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            final FeatureMemsGesture.Gesture gesture = FeatureMemsGesture.getGesture(sample);
            final long timeStamp = sample.timestamp;
            if(gesture == FeatureMemsGesture.Gesture.ERROR ||
                    gesture == FeatureMemsGesture.Gesture.UNKNOWN)
                return;
            updateGui(new Runnable() {
                @Override
                public void run() {
                    animateGesture(gesture);
                    mCurrentTimestamp=timeStamp;
                    mDeselectImageTaskQueue.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if(mCurrentTimestamp==timeStamp && mCurrentImage!=null){
                                        mCurrentImage.setColorFilter(sToGrayScale);
                                    }//if
                                }//run
                            },AUTOMATIC_DESELECT_TIMEOUT_MS);
                }//run
            });

        }//on update
    };

    public MemsGestureRecognitionFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mGestureRecognition = node.getFeature(FeatureMemsGesture.class);
        if (mGestureRecognition != null) {
            mGestureRecognition.addFeatureListener(mActivityListener);
            node.enableNotification(mGestureRecognition);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        mGestureRecognition = node.getFeature(FeatureMemsGesture.class);
        if (mGestureRecognition != null) {
            mGestureRecognition.removeFeatureListener(mActivityListener);
            node.disableNotification(mGestureRecognition);
        }//if
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_mems_gesture_recognition, container, false);

        mPickUpImage = (ImageView) root.findViewById(R.id.pickUpImage);
        mPickUpImage.setColorFilter(sToGrayScale);
        mWakeUpImage = (ImageView) root.findViewById(R.id.wakeUpImage);
        mWakeUpImage.setColorFilter(sToGrayScale);
        mGlanceImage = (ImageView) root.findViewById(R.id.glanceImage);
        mGlanceImage.setColorFilter(sToGrayScale);

        mScaleDownAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_down);

        mDeselectImageTaskQueue = new Handler(Looper.getMainLooper());
        mCurrentImage=null;

        return root;
    }


    /**
     * return the image link with the gesture
     * @param gesture gesture detected by the node
     * @return image to select
     */
    private @Nullable
    ImageView getGestureImage(FeatureMemsGesture.Gesture gesture){
        switch(gesture) {
            case GLANCE:
                return mGlanceImage;
            case WAKE_UP:
                return mWakeUpImage;
            case PICK_UP:
                return mPickUpImage;
            default:
                return null;
        }//switch
    }//getGestureImage

    /**
     * animate the image link with the gesture
     * @param gesture gesture to animate
     */
    @UiThread
    private void animateGesture(FeatureMemsGesture.Gesture gesture){

        ImageView gestureImage = getGestureImage(gesture);
        if(mCurrentImage!=null){
            mCurrentImage.setColorFilter(sToGrayScale);
        }//if
        mCurrentImage=gestureImage;
        if(mCurrentImage!=null){
            mCurrentImage.setColorFilter(null);
            mCurrentImage.startAnimation(mScaleDownAnim);
        }//if
    } //animateGesture

}
