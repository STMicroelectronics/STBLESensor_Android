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

package com.st.BlueMS.demos.Cloud.util;

import android.content.Context;
import android.support.annotation.RawRes;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Features.FeatureDirectionOfArrival;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureLuminosity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeatureMicLevel;
import com.st.BlueSTSDK.Features.FeatureMotionIntensity;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureProximity;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import com.google.android.gms.security.ProviderInstaller;


public class MqttClientUtil {

    private final static String SSL_PROTOCOL =  "TLSv1.2";

    public static String getDefaultCloudDeviceName(Node n){
        return n.getTag().replace(':','_');
    }

    public static SocketFactory createSSLSocketFactory(Context context, @RawRes int certificateID)
        throws IOException,GeneralSecurityException {
        try {
            ProviderInstaller.installIfNeeded(context);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Log.e(MqttClientUtil.class.getName(),"ProviderInstaller error: "+e);
        }

        KeyStore ks = KeyStore.getInstance("bks");
        ks.load(context.getResources().openRawResource(certificateID), "password".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        tmf.init(ks);
        TrustManager[] tm = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance(SSL_PROTOCOL);
        sslContext.init(null, tm, null);
        return sslContext.getSocketFactory();
    }

    public static String getPublishTopic(String clientId,String featureName, String fieldName) {
        return (clientId+"/"+featureName+"/"+fieldName).toLowerCase()
                .replace(' ', '_')
                .replace("(","")
                .replace(")","");
    }

    public static boolean isSupportedFeature(Feature f){
        return SUPPORTED_FEATURES.contains(f.getClass());
    }

    private static final List<Class<? extends Feature>> SUPPORTED_FEATURES = Arrays.asList(
            FeatureAcceleration.class,
            FeatureAccelerationEvent.class,
            FeatureActivity.class,
            FeatureCompass.class,
            FeatureDirectionOfArrival.class,
            FeatureGyroscope.class,
            FeatureHumidity.class,
            FeatureLuminosity.class,
            FeatureMagnetometer.class,
            FeatureMemsSensorFusionCompact.class,
            FeatureMemsSensorFusion.class,
            FeatureMicLevel.class,
            FeatureMotionIntensity.class,
            FeaturePedometer.class,
            FeatureProximity.class,
            FeaturePressure.class,
            FeatureTemperature.class
    );

}
