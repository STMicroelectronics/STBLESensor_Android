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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureProximityGesture;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;


@DemoDescriptionAnnotation(name="Proximity Gesture", requareAll = {FeatureProximityGesture.class} ,
        iconRes = R.drawable.proximity_gesture_demo_icon)
public class ProximityGestureRecognitionFragment extends DemoFragment {

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
     * feature where we will read the position
     */
    private Feature mGestureRecognition;

    /**
     * image link to the tap event
     */
    private ImageView mTapImage;

    /**
     * image link to the left event
     */
    private ImageView mLeftImage;

    /**
     * image link to the right event
     */
    private ImageView mRightImage;

    /**
     * current selected image
     */
    private ImageView mCurrentSelectedImage=null;

    /**
     * animation for scale down the image
     */
    private Animation mScaleDownAnim;

    /**
     * animation for move left an image
     */
    private Animation mMoveLeftAnim;

    /**
     * animation for move right an image
     */
    private Animation mMoveRightAnim;

    /**
     * listener that will animate selected image
     */
    private Feature.FeatureListener mActivityListener = new  Feature.FeatureListener () {

        @Override
        public void onUpdate(Feature f,Feature.Sample sample) {
            final FeatureProximityGesture.Gesture gesture = FeatureProximityGesture.getGesture(sample);
            updateGui(new Runnable() {
                @Override
                public void run() {
                    animateGesture(gesture);
                }//run
            });

        }//on update
    };

    /**
     * return the animation to apply to the gesture image
     * @param gesture gesture detected by the node
     * @return animation to apply
     */
    private Animation getGestureAnimation(FeatureProximityGesture.Gesture gesture){
        switch(gesture) {
            case TAP:
                return mScaleDownAnim;
            case LEFT:
                return mMoveLeftAnim;
            case RIGHT:
                return mMoveRightAnim;
            default:
                return null;
        }//switch
    }//getGestureImage

    /**
     * return the image link with the gesture
     * @param gesture gesture detected by the node
     * @return image to select
     */
    private @Nullable ImageView getGestureImage(FeatureProximityGesture.Gesture gesture){
        switch(gesture) {
            case TAP:
                return mTapImage;
            case LEFT:
                return mLeftImage;
            case RIGHT:
                return mRightImage;
            default:
                return null;
        }//switch
    }//getGestureImage

    /**
     * animate the image link with the gesture
     * @param gesture gesture to animate
     */
    private void animateGesture(FeatureProximityGesture.Gesture gesture){
        ImageView gestureImage = getGestureImage(gesture);
        if(mCurrentSelectedImage!=null){
            mCurrentSelectedImage.setColorFilter(sToGrayScale);
        }//if
        mCurrentSelectedImage=gestureImage;
        if(mCurrentSelectedImage!=null){
            mCurrentSelectedImage.setColorFilter(null);
            mCurrentSelectedImage.startAnimation(getGestureAnimation(gesture));
        }//if
    } //animateGesture


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_gesture_demo, container, false);

        mTapImage = (ImageView) root.findViewById(R.id.tapImage);
        mTapImage.setColorFilter(sToGrayScale);
        mLeftImage = (ImageView) root.findViewById(R.id.leftImage);
        mLeftImage.setColorFilter(sToGrayScale);
        mRightImage = (ImageView) root.findViewById(R.id.rightImage);
        mRightImage.setColorFilter(sToGrayScale);

        mScaleDownAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.scale_down);
        mMoveLeftAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.move_left);
        mMoveRightAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.move_right);

        return root;
    }//onCreateView

    @Override
    protected void enableNeededNotification(@NonNull Node node) {

        mGestureRecognition = node.getFeature(FeatureProximityGesture.class);
        if (mGestureRecognition != null) {
            mGestureRecognition.addFeatureListener(mActivityListener);
            node.enableNotification(mGestureRecognition);
        }
    }//enableNeededNotification

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        mGestureRecognition = node.getFeature(FeatureProximityGesture.class);
        if (mGestureRecognition != null) {
            mGestureRecognition.removeFeatureListener(mActivityListener);
            node.disableNotification(mGestureRecognition);
        }//if
    }//disableNeedNotification

}
