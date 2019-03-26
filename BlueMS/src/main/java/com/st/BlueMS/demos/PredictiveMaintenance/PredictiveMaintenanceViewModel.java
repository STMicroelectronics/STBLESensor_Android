package com.st.BlueMS.demos.PredictiveMaintenance;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictive;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictiveAccelerationStatus;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictiveFrequencyDomainStatus;
import com.st.BlueSTSDK.Features.predictive.FeaturePredictiveSpeedStatus;
import com.st.BlueSTSDK.Node;

import static com.st.BlueMS.demos.PredictiveMaintenance.PredictiveStatusView.ViewStatus;

public class PredictiveMaintenanceViewModel extends ViewModel {

    private MutableLiveData<ViewStatus> mSpeedStatus = new MutableLiveData<>();
    private MutableLiveData<ViewStatus> mAccStatus = new MutableLiveData<>();
    private MutableLiveData<ViewStatus> mFrequencyStatus = new MutableLiveData<>();

    private MutableLiveData<Boolean> mSpeedStatusVisibility = new MutableLiveData<>();
    private MutableLiveData<Boolean> mAccStatusVisibility = new MutableLiveData<>();
    private MutableLiveData<Boolean> mFrequencyStatusVisibility = new MutableLiveData<>();

    public PredictiveMaintenanceViewModel(){
        ViewStatus unknown = new PredictiveStatusView.ViewStatus(
                FeaturePredictive.Status.UNKNOWN,
                FeaturePredictive.Status.UNKNOWN,
                FeaturePredictive.Status.UNKNOWN);
        mSpeedStatus.setValue(unknown);
        mAccStatus.setValue(unknown);
        mFrequencyStatus.setValue(unknown);

    }

    public LiveData<ViewStatus> getAccStatus(){ return mAccStatus;}
    public LiveData<ViewStatus> getSpeedStatus(){ return mSpeedStatus;}
    public LiveData<ViewStatus> getFrequencyStatus(){ return mFrequencyStatus;}

    public LiveData<Boolean> getAccStatusVisibility(){ return mAccStatusVisibility;}
    public LiveData<Boolean> getSpeedStatusVisibility(){ return mSpeedStatusVisibility;}
    public LiveData<Boolean> getFrequencyStatusVisibility(){ return mFrequencyStatusVisibility;}


    private Feature.FeatureListener mTimeSpeedListener = (f, sample) -> {
        ViewStatus timeStatus = new ViewStatus(
                FeaturePredictiveSpeedStatus.getStatusX(sample),
                FeaturePredictiveSpeedStatus.getStatusY(sample),
                FeaturePredictiveSpeedStatus.getStatusZ(sample),
                new ViewStatus.Point(FeaturePredictiveSpeedStatus.getSpeedX(sample)),
                new ViewStatus.Point(FeaturePredictiveSpeedStatus.getSpeedY(sample)),
                new ViewStatus.Point(FeaturePredictiveSpeedStatus.getSpeedZ(sample))
        );

        mSpeedStatus.postValue(timeStatus);
    };

    private Feature.FeatureListener mFrequencyDomainListener = (f, sample) -> {
        ViewStatus newStatus = new ViewStatus(
                FeaturePredictiveFrequencyDomainStatus.getStatusX(sample),
                FeaturePredictiveFrequencyDomainStatus.getStatusY(sample),
                FeaturePredictiveFrequencyDomainStatus.getStatusZ(sample),
                new ViewStatus.Point(
                        FeaturePredictiveFrequencyDomainStatus.getWorstXFrequency(sample),
                        FeaturePredictiveFrequencyDomainStatus.getWorstXValue(sample)
                ),
                new ViewStatus.Point(
                        FeaturePredictiveFrequencyDomainStatus.getWorstYFrequency(sample),
                        FeaturePredictiveFrequencyDomainStatus.getWorstYValue(sample)
                ),
                new ViewStatus.Point(
                        FeaturePredictiveFrequencyDomainStatus.getWorstZFrequency(sample),
                        FeaturePredictiveFrequencyDomainStatus.getWorstZValue(sample)
                )
        );

        mFrequencyStatus.postValue(newStatus);
    };

    private Feature.FeatureListener mTimeAccListener = (f, sample) -> {
        ViewStatus newStatus = new ViewStatus(
                FeaturePredictiveAccelerationStatus.getStatusX(sample),
                FeaturePredictiveAccelerationStatus.getStatusY(sample),
                FeaturePredictiveAccelerationStatus.getStatusZ(sample),
                new ViewStatus.Point(
                        FeaturePredictiveAccelerationStatus.getAccX(sample)
                ),
                new ViewStatus.Point(
                        FeaturePredictiveAccelerationStatus.getAccY(sample)
                ),
                new ViewStatus.Point(
                        FeaturePredictiveAccelerationStatus.getAccZ(sample)
                )
        );

        mAccStatus.postValue(newStatus);
    };

    void enableNotification(@NonNull Node node){
        Feature predictiveMaintenanceTimeSpeedStatus = node.getFeature(FeaturePredictiveSpeedStatus.class);
        if(predictiveMaintenanceTimeSpeedStatus !=null){
            mSpeedStatusVisibility.postValue(true);
            predictiveMaintenanceTimeSpeedStatus.addFeatureListener(mTimeSpeedListener);
            predictiveMaintenanceTimeSpeedStatus.enableNotification();
        }

        Feature predictiveMaintenanceFrequencyStatus = node.getFeature(FeaturePredictiveFrequencyDomainStatus.class);
        if(predictiveMaintenanceFrequencyStatus !=null){
            mFrequencyStatusVisibility.postValue(true);
            predictiveMaintenanceFrequencyStatus.addFeatureListener(mFrequencyDomainListener);
            predictiveMaintenanceFrequencyStatus.enableNotification();
        }

        Feature predictiveMaintenanceAccStatus = node.getFeature(FeaturePredictiveAccelerationStatus.class);
        if(predictiveMaintenanceAccStatus !=null){
            mAccStatusVisibility.postValue(true);
            predictiveMaintenanceAccStatus.addFeatureListener(mTimeAccListener);
            predictiveMaintenanceAccStatus.enableNotification();
        }
    }


    void disableNotification(@NonNull Node node){
        Feature predictiveMaintenanceTimeSpeedStatus = node.getFeature(FeaturePredictiveSpeedStatus.class);
        if(predictiveMaintenanceTimeSpeedStatus !=null){
            mSpeedStatusVisibility.postValue(true);
            predictiveMaintenanceTimeSpeedStatus.removeFeatureListener(mTimeSpeedListener);
            predictiveMaintenanceTimeSpeedStatus.disableNotification();
        }

        Feature predictiveMaintenanceFrequencyStatus = node.getFeature(FeaturePredictiveFrequencyDomainStatus.class);
        if(predictiveMaintenanceFrequencyStatus !=null){
            predictiveMaintenanceFrequencyStatus.removeFeatureListener(mFrequencyDomainListener);
            predictiveMaintenanceFrequencyStatus.disableNotification();
        }

        Feature predictiveMaintenanceTimeAccStatus = node.getFeature(FeaturePredictiveAccelerationStatus.class);
        if(predictiveMaintenanceTimeAccStatus !=null){
            predictiveMaintenanceTimeAccStatus.removeFeatureListener(mTimeAccListener);
            predictiveMaintenanceTimeAccStatus.disableNotification();
        }
    }

}
