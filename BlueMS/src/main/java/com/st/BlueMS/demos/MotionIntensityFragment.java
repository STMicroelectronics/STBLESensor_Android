/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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

import android.animation.AnimatorInflater;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureMotionIntensity;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

@DemoDescriptionAnnotation(name="Motion Intensity", requareAll = {FeatureMotionIntensity.class},
        iconRes = com.st.BlueMS.R.drawable.activity_demo_icon)
public class MotionIntensityFragment extends BaseDemoFragment {

    private static final String LAST_VALUE = MotionIntensityFragment.class.getCanonicalName()
            +".MotionValue";

    private Feature mMotionIntensityFeature;

    private String mIntensityValueFormat;
    private ImageView mIntensityNeedle;
    private TypedArray mNeedleOffset;
    private TextView mIntensityValue;

    private ValueAnimator mRotationAnim;

    private byte mLastValue =0;
    /**
     * rotate the indicator needle and set the text
     * @param value intensity value to show
     */
    private void setGuiForIntensityValue(byte value){
        if(value == mLastValue)
            return;
        mLastValue =value;
        final float rotationOffset = mNeedleOffset.getFloat(value,0.0f);
        final String valueStr = String.format(mIntensityValueFormat,value);

        updateGui(() -> {
            mIntensityValue.setText(valueStr);

            if(mRotationAnim.isRunning())
                mRotationAnim.pause();

            float currentPosition = mIntensityNeedle.getRotation();
            mRotationAnim.setFloatValues(currentPosition,rotationOffset);
            mRotationAnim.start();
        });
    }


    private Feature.FeatureListener mMotionListener = (f, sample) -> {
        byte value = FeatureMotionIntensity.getMotionIntensity(sample);
        //update the gui only if it is a valid value
        if(value>= FeatureMotionIntensity.DATA_MIN && value<= FeatureMotionIntensity.DATA_MAX)
            setGuiForIntensityValue(value);
    };

    public MotionIntensityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_motion_intensity, container, false);

        mIntensityValue = root.findViewById(R.id.motionId_intensityValue);
        mIntensityNeedle = root.findViewById(R.id.motionId_needleImage);

        Resources res = root.getResources();
        mNeedleOffset = res.obtainTypedArray(R.array.motionId_angleOffset);
        mIntensityValueFormat = res.getString(R.string.motionId_valueTextFormat);

        mRotationAnim = (ValueAnimator) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.needle_rotation);
        mRotationAnim.setTarget(mIntensityNeedle);


        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(LAST_VALUE)) {
                setGuiForIntensityValue(savedInstanceState.getByte(LAST_VALUE));
            }// if contains key
        }// if !=null

        return root;
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mMotionIntensityFeature = node.getFeature(FeatureMotionIntensity.class);
        if(mMotionIntensityFeature==null)
            return;

        mMotionIntensityFeature.addFeatureListener(mMotionListener);
        node.enableNotification(mMotionIntensityFeature);

    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mMotionIntensityFeature==null)
            return;

        mMotionIntensityFeature.removeFeatureListener(mMotionListener);
        node.disableNotification(mMotionIntensityFeature);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putByte(LAST_VALUE, mLastValue);
    }//onSaveInstanceState

}
