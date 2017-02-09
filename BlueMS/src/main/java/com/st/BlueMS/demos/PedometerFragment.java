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


import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(name="Pedometer", requareAll = {FeaturePedometer.class},iconRes = R
        .drawable.pedometer_demo_icon)
public class PedometerFragment extends DemoFragment {


    private String mStepCountTextFormat;
    private String mStepFrequencyTextFormat;

    private FeaturePedometer mPedometer;

    private TextView mStepsCount;
    private TextView mStepsFrequency;

    private boolean mFlipPosition;
    private AnimatorSet mFlipImageLeft;
    private AnimatorSet mFlipImageRight;

    private void animatePedometerIcon(){
        if(mFlipImageRight.isRunning() || mFlipImageLeft.isRunning())
            return;

        if(mFlipPosition)
            mFlipImageLeft.start();
        else
            mFlipImageRight.start();
        mFlipPosition=!mFlipPosition;
    }

    private Feature.FeatureListener mPedometerListener = new Feature.FeatureListener() {

        @Override
        public void onUpdate(Feature f,final Feature.Sample sample) {
            final String stepCountStr = String.format(mStepCountTextFormat,
                    FeaturePedometer.getSteps(sample));
            int frequency =FeaturePedometer.getFrequency(sample);
            final String stepFreqStr = frequency>0 ?
                    String.format(mStepFrequencyTextFormat,frequency,
                            FeaturePedometer.FEATURE_UNIT[1]) : "";
            updateGui(new Runnable() {
                @Override
                public void run() {
                    mStepsCount.setText(stepCountStr);
                    mStepsFrequency.setText(stepFreqStr);
                    animatePedometerIcon();
                }
            });
        }
    };

    public PedometerFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mPedometer = node.getFeature(FeaturePedometer.class);
        if (mPedometer != null) {
            mPedometer.addFeatureListener(mPedometerListener);
            node.enableNotification(mPedometer);
            //read the current state for initialize the string data
            node.readFeature(mPedometer);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mPedometer != null) {
            mPedometer.removeFeatureListener(mPedometerListener);
            node.disableNotification(mPedometer);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_pedometer_feature, container, false);

        mStepsCount = (TextView) root.findViewById(R.id.stepCount);
        mStepsFrequency = (TextView) root.findViewById(R.id.stepFrequency);
        ImageView pedometerImage = (ImageView) root.findViewById(R.id.stepImage);
        pedometerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPedometer!=null){
                    Node node = getNode();
                    if(node!=null)
                        node.readFeature(mPedometer);
                }
            }
        });

        mFlipImageLeft = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.flip_image_right);
        mFlipImageLeft.setTarget(pedometerImage);

        mFlipImageRight = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.flip_image_left);
        mFlipImageRight.setTarget(pedometerImage);

        final Resources res = getResources();
        mStepCountTextFormat = res.getString(R.string.stepCounterStringFormat);
        mStepFrequencyTextFormat = res.getString(R.string.stepFrequencyStringFormat);

        return root;
    }

}
