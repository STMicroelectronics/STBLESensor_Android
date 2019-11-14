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

package com.st.BlueMS.demos.memsSensorFusion.calibration;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAutoConfigurable;

/**
 * presenter implementation
 */
public class CalibrationPresenter implements CalibrationContract.Presenter,
        FeatureAutoConfigurable.FeatureAutoConfigurationListener {

    /**
     * feature to calibrate
     */
    private FeatureAutoConfigurable mFeature;

    /**
     * view to change
     */
    private CalibrationContract.View mView;

    @Override
    public void manage(@NonNull CalibrationContract.View v,@NonNull FeatureAutoConfigurable feature) {
        mView = v;
        mFeature = feature;
        feature.addFeatureListener(this);
        if(!mFeature.isConfigured()) {
            mFeature.startAutoConfiguration();
        }else
            mView.setCalibrationButtonState(true);
    }

    @Override
    public void startCalibration() {
        if(mFeature!=null)
            mFeature.startAutoConfiguration();
    }

    @Override
    public void unManageFeature() {
        if(mFeature!=null){
            mFeature.removeFeatureListener(this);
        }
        mView=null;
    }

    /**
     * move the view in a calibrate state
     */
    private void setCalibratedState(){
        if(mView!=null) {
            mView.hideCalibrationDialog();
            mView.setCalibrationButtonState(true);
        }
    }

    /**
     * move the view in a uncalibrate state
     */
    private void setUnCalibratedState(){
        if(mView!=null) {
            mView.showCalibrationDialog();
            mView.setCalibrationButtonState(false);
        }
    }

    ///// FeatureAutoConfigurationListener//////

    @Override
    public void onAutoConfigurationStarting(FeatureAutoConfigurable f) {
        setUnCalibratedState();
    }

    @Override
    public void onConfigurationFinished(FeatureAutoConfigurable f, int status) {
        setCalibratedState();
    }

    @Override
    public void onAutoConfigurationStatusChanged(FeatureAutoConfigurable f, int status) {
        if(status == 0 ){ // uncalibrated
            setUnCalibratedState();
        }else if (status == 100){ //fully calibrated
            setCalibratedState();
        }
    }

    @Override
    public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) { }
}
