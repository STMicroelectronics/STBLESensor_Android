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
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Features.standardCharacteristics.FeatureHeartRate;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(name="Heart Rate", requareAll = {FeatureHeartRate.class},
        iconRes = R.drawable.heart_rate_icon)
public class HearRateFragment extends DemoFragment {

    private Feature mHeartRateFeature;

    private Feature.FeatureListener mHeartRateListener = new Feature.FeatureListener() {
        private ColorMatrixColorFilter mToGrayScaleFilter;
        //public Feature.FeatureListener()
        {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            mToGrayScaleFilter = new ColorMatrixColorFilter(matrix);
        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            final int hearRate = FeatureHeartRate.getHeartRate(sample);
            final int energy = FeatureHeartRate.getEnergyExtended(sample);
            final float rrInterval = FeatureHeartRate.getRRInterval(sample);
            updateGui(new Runnable() {
                @Override
                public void run() {

                    if(hearRate<=0) {
                        mHeartRateLabel.setText("");
                        mHeartImage.setColorFilter(mToGrayScaleFilter);
                        return;
                    }

                    mHeartRateLabel.setText(String.format(mHeartRateFormat, hearRate, mHeartRateUnit));
                    mHeartImage.setColorFilter(null);
                    if(energy>0)
                        mEnergyExtendedLabel.setText(String.format(mEnergyExtendedFormat,
                                energy,mEnergyExtendedUnit));
                    if(!Float.isNaN(rrInterval))
                        mRRIntervalLabel.setText(String.format(mRRIntervalFormat,
                                rrInterval,mRRIntervalUnit));
                    if(!mPulseAnim.isRunning())
                        mPulseAnim.start();
                }//run
            });
        }//onUpdate
    };

    private TextView mHeartRateLabel;
    private String mHeartRateFormat;
    private String mHeartRateUnit;
    private TextView mEnergyExtendedLabel;
    private String mEnergyExtendedFormat;
    private String mEnergyExtendedUnit;
    private TextView mRRIntervalLabel;
    private String mRRIntervalFormat;
    private String mRRIntervalUnit;
    private ImageView mHeartImage;

    private AnimatorSet mPulseAnim;

    private void initDataFormat(Feature f){
        final Resources res = getResources();
        final Field data[] = f.getFieldsDesc();

        mHeartRateFormat = res.getString(R.string.heartRateDataFormat);
        mHeartRateUnit = data[FeatureHeartRate.HEART_RATE_INDEX].getUnit();

        mEnergyExtendedFormat = res.getString(R.string.energyExpendedDataFormat);
        mEnergyExtendedUnit = data[FeatureHeartRate.ENERGY_EXPENDED_INDEX].getUnit();

        mRRIntervalFormat = res.getString(R.string.rrIntervalDataFormat);
        mRRIntervalUnit = data[FeatureHeartRate.RR_INTERVAL_INDEX].getUnit();
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mHeartRateFeature = node.getFeature(FeatureHeartRate.class);
        if (mHeartRateFeature != null) {
            initDataFormat(mHeartRateFeature);
            mHeartRateFeature.addFeatureListener(mHeartRateListener);
            node.enableNotification(mHeartRateFeature);

        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mHeartRateFeature != null) {
            mHeartRateFeature.removeFeatureListener(mHeartRateListener);
            node.disableNotification(mHeartRateFeature);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_hear_rate, container, false);

        mHeartRateLabel = (TextView) root.findViewById(R.id.heartRateLabel);
        mRRIntervalLabel = (TextView) root.findViewById(R.id.rrIntervalLabel);
        mEnergyExtendedLabel = (TextView) root.findViewById(R.id.energyExtendedLabel);

        mHeartImage = (ImageView) root.findViewById(R.id.heartImage);
        mHeartImage.setOnClickListener(view -> {
            if(mHeartRateFeature!=null){
                Node node = getNode();
                if(node!=null)
                    node.readFeature(mHeartRateFeature);
            }
        });

        mPulseAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(),
                R.animator.pulse);
        mPulseAnim.setTarget(mHeartImage);

        return root;
    }

}
