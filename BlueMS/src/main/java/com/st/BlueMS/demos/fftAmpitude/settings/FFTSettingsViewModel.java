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
package com.st.BlueMS.demos.fftAmpitude.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.core.math.MathUtils;

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
