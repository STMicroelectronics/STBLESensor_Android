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
package com.st.BlueMS.demos.fitnessActivity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureFitnessActivity;
import com.st.BlueSTSDK.Node;

public class FitnessActivityViewModel extends ViewModel {

    private static final FeatureFitnessActivity.ActivityType DEFAULT_ACTIVITY = FeatureFitnessActivity.ActivityType.BICEP_CURL;

    private @Nullable
    FeatureFitnessActivity mFeature;

    private MutableLiveData<FeatureFitnessActivity.ActivityType> mCurrentActivity = new MutableLiveData<>();
    private MutableLiveData<Integer> mCurrentCounter = new MutableLiveData<>();

    public FitnessActivityViewModel(){
        mCurrentActivity.setValue(DEFAULT_ACTIVITY);
    }

    private Feature.FeatureListener mFeatureListener = (f, sample) -> {
        FeatureFitnessActivity.ActivityType activityType = FeatureFitnessActivity.getActivity(sample);
        int counter = FeatureFitnessActivity.getActivityCount(sample);
        mCurrentActivity.postValue(activityType);
        mCurrentCounter.postValue(counter);
    };

    LiveData<FeatureFitnessActivity.ActivityType> getCurrentActivity(){
        return mCurrentActivity;
    }


    LiveData<Integer> getCurrentCounter(){
        return mCurrentCounter;
    }


    void setActivity(FeatureFitnessActivity.ActivityType activity){
        if(mFeature==null)
            return;
        mFeature.enableActivity(activity);
    }


    void startListenDataFrom(@NonNull Node node){

        mFeature = node.getFeature(FeatureFitnessActivity.class);
        if(mFeature!=null){
            mFeature.addFeatureListener(mFeatureListener);
            mFeature.enableActivity(mCurrentActivity.getValue());
            mFeature.enableNotification();
        }

    }


    void stopListenDataFrom(@NonNull Node node){
        if(mFeature!=null){
            mFeature.disableNotification();
            mFeature.removeFeatureListener(mFeatureListener);
        }
        mFeature = null;
    }

}
