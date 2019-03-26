package com.st.BlueMS.demos.fftAmpitude.settings;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v4.math.MathUtils;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;

public class FFTSettingsViewModel extends ViewModel {

    static final byte MIN_OVERLAP = 5;
    static final byte MAX_OVERLAP = 95;

    static final int MIN_TIME_ACQUISITION_MS = 500;
    static final int MAX_TIME_ACQUISITION_MS = 60000;

    private MutableLiveData<FFTSettings> mSettings = new MutableLiveData<>();
    private MutableLiveData<FFTSettings.WindowType> mWindowType = new MutableLiveData<>();
    private MutableLiveData<Short> mOdr = new MutableLiveData<>();
    private MutableLiveData<Short> mSize = new MutableLiveData<>();
    private MutableLiveData<Byte> mSensorFullScale = new MutableLiveData<>();
    private MutableLiveData<Byte> mSubRange = new MutableLiveData<>();
    private MutableLiveData<Byte> mOverlap = new MutableLiveData<>();
    private MutableLiveData<Integer> mTimeAcquisition = new MutableLiveData<>();
    private MutableLiveData<Boolean> mUpdateParamCorrectly = new MutableLiveData<>();

    private FFTSettingsConsole mConsole;

    void readSettingsFrom(Node node) {
        Debug console = node.getDebug();
        if(console!=null) {
            mConsole = new FFTSettingsConsole(console);
            mConsole.read(values -> {
                mSettings.postValue(values);
                if(values!=null){
                    mWindowType.postValue(values.winType);
                    mOdr.postValue(values.odr);
                    mSize.postValue(values.size);
                    mSensorFullScale.postValue(values.fullScale);
                    mSubRange.postValue(values.subRange);
                    mOverlap.postValue(values.overlap);
                    mTimeAcquisition.postValue(values.acquisitionTime_s);

                }
            });
        }
    }

    LiveData<FFTSettings> getSettings(){
        return mSettings;
    }

    LiveData<FFTSettings.WindowType> getWindowType(){
        return mWindowType;
    }
    LiveData<Short> getOdr(){
        return mOdr;
    }
    LiveData<Short> getSize(){
        return mSize;
    }
    LiveData<Byte> getSensorFullScale(){
        return mSensorFullScale;
    }
    LiveData<Byte> getSubRange(){
        return mSubRange;
    }
    LiveData<Byte> getOverlap(){
        return mOverlap;
    }
    LiveData<Boolean> getUpdateParamCorrectly(){
        return mUpdateParamCorrectly;
    }

    public void setSize(short newSize) {
        mSize.postValue(newSize);
    }

    void setNewWindow(FFTSettings.WindowType newType) {
        mWindowType.postValue(newType);
    }

    void setOdr(short newOdr) {
        mOdr.postValue(newOdr);
    }

    void setSensorFullScale(byte newValue) {
        Byte currentValue = mSensorFullScale.getValue();
        if(currentValue==null || currentValue!=newValue) {
            mSensorFullScale.postValue(newValue);
        }
    }

    void setSubRange(byte newValue) {
        Byte currentValue = mSubRange.getValue();
        if(currentValue==null || currentValue!=newValue) {
            mSubRange.postValue(newValue);
        }
    }

    void setOverlap(byte newValue) {
        newValue = (byte) MathUtils.clamp(newValue,MIN_OVERLAP,MAX_OVERLAP);
        Byte currentValue = mOverlap.getValue();
        if(currentValue==null || currentValue!=newValue) {
            mOverlap.postValue(newValue);
        }
    }

    void setAcquisitionTime(int newValue){
        newValue = MathUtils.clamp(newValue,
                MIN_TIME_ACQUISITION_MS,MAX_TIME_ACQUISITION_MS);
        Integer currentValue = mTimeAcquisition.getValue();
        if(currentValue==null || currentValue!=newValue)
            mTimeAcquisition.postValue(newValue);
    }

    LiveData<Integer> getTimeAcquisition() {
        return mTimeAcquisition;
    }

    void writeSettingsTo(Node node) {
        Debug console = node.getDebug();
        FFTSettings newSettings = null;
        try {
            newSettings = new FFTSettings(
                    mOdr.getValue(),
                    mSensorFullScale.getValue(),
                    mSize.getValue(),
                    mWindowType.getValue(),
                    mTimeAcquisition.getValue(),
                    mOverlap.getValue(),
                    mSubRange.getValue()
            );
        }catch (NullPointerException e){
            mUpdateParamCorrectly.postValue(false);
            return;
        }
        if(console!=null) {
            mConsole = new FFTSettingsConsole(console);
            mConsole.write(newSettings,
                    success -> mUpdateParamCorrectly.postValue(success));
        }
    }
}
