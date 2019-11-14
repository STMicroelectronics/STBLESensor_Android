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
package com.st.blesensor.cloud.util;

import androidx.annotation.NonNull;

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
    public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
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
