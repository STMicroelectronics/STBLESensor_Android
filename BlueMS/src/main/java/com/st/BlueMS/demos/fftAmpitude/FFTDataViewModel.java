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
package com.st.BlueMS.demos.fftAmpitude;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureFFTAmplitude;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * view model containing the fft data received by the BLE
 */
public class FFTDataViewModel extends ViewModel {

    static class FFTPoint{
        final float frequency;
        final float amplitude;

        FFTPoint(float frequency, float amplitude) {
            this.frequency = frequency;
            this.amplitude = amplitude;
        }
    }

    private  static int findIndexMax(float[] data){
        float max = data[0];
        int index = 0;
        for (int i = 1; i < data.length; i++) {
            if(data[i]>max){
                max = data[i];
                index =i;
            }
        }
        return index;
    }

    private Feature mFFTFeature;

    /**
     * every time we have new data, we update also the max values
     */
    private Observer<List<float[]>> mUpdateMaxValues = new Observer<List<float[]>>() {

        @Override
        public void onChanged(@Nullable List<float[]> data) {
            Float freqSteps = mFrequencyStep.getValue();

            if(data==null || freqSteps==null){
                mFftMax.postValue(null);
                return;
            }

            final int nComponents = data.size();
            ArrayList<FFTPoint> maxs = new ArrayList<>(nComponents);

            for (int i = 0; i <nComponents ; i++) {
                float[] compData = data.get(i);
                if(compData!=null && compData.length>0) {
                    int maxIndex = findIndexMax(compData);
                    maxs.add(new FFTPoint(freqSteps * maxIndex, compData[maxIndex]));
                }
            }
            if(maxs.size()>0)
                mFftMax.postValue(maxs);
        }
    };

    private MutableLiveData<List<float[]>> mFftData = new MutableLiveData<>();
    private MutableLiveData<Float> mFrequencyStep = new MutableLiveData<>();
    private MutableLiveData<Byte> mLoadingStatus = new MutableLiveData<>();

    private MutableLiveData<List<FFTPoint>> mFftMax = new MutableLiveData<>();

    public FFTDataViewModel(){
        mFftData.observeForever(mUpdateMaxValues);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mFftData.removeObserver(mUpdateMaxValues);
    }

    LiveData<List<float[]>> getFftData(){
        return mFftData;
    }

    LiveData<Byte> getLoadingStatus(){
        return mLoadingStatus;
    }

    LiveData<Float> getFrequencyStep(){
        return mFrequencyStep;
    }

    LiveData<List<FFTPoint>> getFFTMax(){
        return mFftMax;
    }

    /**
     * set the data received by the ble to the live data
     */
    private Feature.FeatureListener mFFTListener = (f, sample) -> {
        if(FeatureFFTAmplitude.isComplete(sample)){
            List<float[]> data = FeatureFFTAmplitude.getComponents(sample);
            float deltaFreq = FeatureFFTAmplitude.getFreqStep(sample);
            mFrequencyStep.postValue(deltaFreq);
            mFftData.postValue(data);
        }else{
            mLoadingStatus.postValue(FeatureFFTAmplitude.getDataLoadPercentage(sample));
        }

    };

    void startListenDataFrom(@NonNull Node node){

        mLoadingStatus.setValue((byte)0);

        mFFTFeature = node.getFeature(FeatureFFTAmplitude.class);
        if(mFFTFeature!=null){
            mFFTFeature.addFeatureListener(mFFTListener);
            mFFTFeature.enableNotification();
        }

    }

    void stopListenDataFrom(){
        if(mFFTFeature!=null){
            mFFTFeature.removeFeatureListener(mFFTListener);
            mFFTFeature.disableNotification();
            mFFTFeature = null;
        }
    }
}
