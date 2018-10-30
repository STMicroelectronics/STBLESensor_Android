package com.st.BlueMS.demos.Cloud.util;

import com.st.BlueSTSDK.Feature;

import java.util.HashMap;
import java.util.Map;

public abstract class SubSamplingFeatureListener implements Feature.FeatureListener {

    private static final long DEFAULT_UPDATE_INTERVAL_MS = 5000;

    private final long mUpdateRateMs;
    private Map<Feature,Long> mLastCloudUpdate=new HashMap<>();

    /**
     * build and object for send an update to the cloud each updateRateMs milliseconds
     * @param updateRateMs minimum time between 2 published samples
     */
    public SubSamplingFeatureListener(long updateRateMs){
        mUpdateRateMs=updateRateMs;
    }

    public SubSamplingFeatureListener(){
        this(DEFAULT_UPDATE_INTERVAL_MS);
    }

    @Override
    public void onUpdate(Feature f, Feature.Sample sample) {
        if(!featureNeedCloudUpdate(f,sample.notificationTime))
            return;
        //otherwise send the data
        onNewDataUpdate(f,sample);
    }

    private boolean featureNeedCloudUpdate(Feature f, long notificationTime) {
        Long lastNotification = mLastCloudUpdate.get(f);
        //first notification or old value
        if(lastNotification==null ||
                ((notificationTime-lastNotification)>mUpdateRateMs)) {
            mLastCloudUpdate.put(f,notificationTime);
            return true;
        }
        return false;
    }

    public abstract void onNewDataUpdate(Feature f,Feature.Sample sample);
}
