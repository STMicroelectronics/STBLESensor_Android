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

package com.st.BlueMS.demos.SDLog;


import com.st.BlueMS.preference.nucleo.NucleoConsole;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureSDLogging;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

class SDLogPresenter implements SDLogContract.Presenter, Feature.FeatureListener {
    private static final int DEFAULT_LOG_INTERVAL=1;
    private SDLogContract.View mView;
    private FeatureSDLogging mLogFeature;
    private boolean isLogging=false;

    private static final List<Class< ? extends Feature>> SUPPORTED_FEATURE = Arrays.asList(
            FeatureAcceleration.class,
            FeatureMagnetometer.class,
            FeatureGyroscope.class,
            FeatureTemperature.class,
            FeatureHumidity.class,
            FeaturePressure.class,
            FeatureMemsSensorFusionCompact.class);

    SDLogPresenter(SDLogContract.View view, FeatureSDLogging logging){
        mView = view;
        mLogFeature = logging;
    }


    @Override
    public void startDemo() {
        if(mLogFeature==null) {
            mView.displayDisableLoggingView();
            return;
        }
        //else
        mLogFeature.addFeatureListener(this);
        mView.setLogInterval(DEFAULT_LOG_INTERVAL);
        Node n = mLogFeature.getParentNode();
        n.enableNotification(mLogFeature);
        readFeature(mLogFeature);
    }

    private static void readFeature(Feature f){
        f.getParentNode().readFeature(f);
    }

    @Override
    public void stopDemo() {
        mLogFeature.removeFeatureListener(this);
        Node n = mLogFeature.getParentNode();
        n.disableNotification(mLogFeature);
    }

    private List<Feature> getAvailableFeatures() {
        Node node = mLogFeature.getParentNode();
        List<Feature> supportedFeature = new ArrayList<>(SUPPORTED_FEATURE.size());
        for (Class < ? extends Feature> supportedType : SUPPORTED_FEATURE){
            List<Feature> features = node.getFeatures((Class<Feature>) supportedType);
            if(!features.isEmpty()){
                supportedFeature.add(features.get(0));
            }
        }

        return supportedFeature;
    }

    private void stopLogging(){
        mLogFeature.stopLogging();
        isLogging=false;
        mView.displayStartLoggingView(getAvailableFeatures());
    }

    private void setNodeTime(){
        Node n = mLogFeature.getParentNode();
        Debug console = n.getDebug();
        if(console!=null){
            new NucleoConsole(console).setDateAndTime(new Date());
        }
    }

    private void startLogging(){
        setNodeTime();
        Set<Feature> selected = mView.getSelectedFeature();
        long interval = mView.getLogInterval();
        mLogFeature.startLogging(selected,interval);
        mView.displayStopLoggingView();
        isLogging=true;
    }

    @Override
    public void onStartStopLogPressed() {
        if(isLogging){
            stopLogging();
        }else{
            startLogging();
        }
    }

    @Override
    public void onUpdate(Feature f, Feature.Sample sample) {
        @FeatureSDLogging.LoggingStatus int status = FeatureSDLogging.getLoggingStatus(sample);
        switch (status) {
            case FeatureSDLogging.LOGGING_STARTED:
                isLogging=true;
                mView.displayStopLoggingView();
                break;
            case FeatureSDLogging.LOGGING_STOPPED:
                isLogging=false;
                mView.displayStartLoggingView(getAvailableFeatures());
                break;
            case FeatureSDLogging.LOGGING_IO_ERROR:
                isLogging=false;
                mView.displayIOErrorLoggingView();
                break;
            case FeatureSDLogging.LOGGING_NO_SD:
                isLogging=false;
                mView.displayNoSDCardErrorLoggingView();
                break;
        }
        mView.setLogInterval(FeatureSDLogging.getLogInterval(sample));
        mView.setSelectedFeature(FeatureSDLogging.getLoggedFeature(mLogFeature.getParentNode(),sample));
    }

}
