package com.st.BlueMS.demos.fftAmpitude;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureFFTAmplitude;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.List;

public class FFTDataViewModel extends ViewModel {

    static class FFTPoint{
        final float frequency;
        final float amplitude;

        FFTPoint(float frequency, float amplitude) {
            this.frequency = frequency;
            this.amplitude = amplitude;
        }
    }

    private  static int findIndexMax(float data[]){
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
                float compData[] = data.get(i);
                int maxIndex = findIndexMax(compData);
                maxs.add(new FFTPoint(freqSteps*maxIndex,compData[maxIndex]));
            }
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

    public LiveData<List<float[]>> getFftData(){
        return mFftData;
    }

    public LiveData<Byte> getLoadingStatus(){
        return mLoadingStatus;
    }

    public LiveData<Float> getFrequencyStep(){
        return mFrequencyStep;
    }

    LiveData<List<FFTPoint>> getFFTMax(){
        return mFftMax;
    }

    private Feature.FeatureListener mFFTListener = (f, sample) -> {
        if(FeatureFFTAmplitude.isComplete(sample)){
            List<float[]> data = FeatureFFTAmplitude.getComponents(sample);
            float deltaX = FeatureFFTAmplitude.getFreqStep(sample);
            mFrequencyStep.postValue(deltaX);
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
