package com.st.BlueMS.demos.fftAmpitude;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureMotorTimeParameter;
import com.st.BlueSTSDK.Node;

public class TimeDomainDataViewModel extends ViewModel {


    static class TimeDomainStats{
        final float accPeak;
        final float rmsSpeed;

        TimeDomainStats(float accPeak, float rmsSpeed) {
            this.accPeak = accPeak;
            this.rmsSpeed = rmsSpeed;
        }
    }

    private Feature mTimeDomainData;
    private MutableLiveData<TimeDomainStats> mXStats = new MutableLiveData<>();
    private MutableLiveData<TimeDomainStats> mYStats = new MutableLiveData<>();
    private MutableLiveData<TimeDomainStats> mZStats = new MutableLiveData<>();

    LiveData<TimeDomainStats> getXComponentStats(){
        return mXStats;
    }

    LiveData<TimeDomainStats> getYComponentStats(){
        return mYStats;
    }

    LiveData<TimeDomainStats> getZComponentStats(){
        return mZStats;
    }

    private Feature.FeatureListener mTimeDomainListener = (f, sample) -> {
        float acc = FeatureMotorTimeParameter.getAccPeakX(sample);
        float speed = FeatureMotorTimeParameter.getRMSSpeedX(sample);
        if( !Float.isNaN(acc) && !Float.isNaN(speed)){
            mXStats.postValue(new TimeDomainStats(acc,speed));
        }

        acc = FeatureMotorTimeParameter.getAccPeakY(sample);
        speed = FeatureMotorTimeParameter.getRMSSpeedY(sample);
        if( !Float.isNaN(acc) && !Float.isNaN(speed)){
            mYStats.postValue(new TimeDomainStats(acc,speed));
        }

        acc = FeatureMotorTimeParameter.getAccPeakZ(sample);
        speed = FeatureMotorTimeParameter.getRMSSpeedZ(sample);
        if( !Float.isNaN(acc) && !Float.isNaN(speed)){
            mZStats.postValue(new TimeDomainStats(acc,speed));
        }
    };


    void startListenDataFrom(@NonNull Node node){

        mTimeDomainData = node.getFeature(FeatureMotorTimeParameter.class);
        if(mTimeDomainData!=null) {
            mTimeDomainData.addFeatureListener(mTimeDomainListener);
            mTimeDomainData.enableNotification();
        }
    }

    void stopListenDataFrom(){

        if(mTimeDomainData!=null) {
            mTimeDomainData.addFeatureListener(mTimeDomainListener);
            mTimeDomainData.disableNotification();
            mTimeDomainData = null;
        }
    }


}
