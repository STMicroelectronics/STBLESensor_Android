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

package com.st.blesensor.cloud.GenericMqtt;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.Log;

import com.st.blesensor.cloud.util.MqttClientConnectionFactory;
import com.st.blesensor.cloud.util.MqttClientUtil;
import com.st.blesensor.cloud.util.SubSamplingFeatureListener;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * create a connection to a generic mqtt broker
 */
class GenericMqttFactory extends MqttClientConnectionFactory {

    /**
     * brocker url: protocol://url:port
     */
    private String mBroker;
    private @Nullable String mUser;
    private @Nullable String mPassword;
    private String mClientId;

    /**
     * cretate a mqtt connection
     * @param url broker url: protocol://address
     * @param port broker port
     * @param clientId mqtt id (can be empty/null)
     * @param user connection user (can be empty/null)
     * @param password connection password (can be empty/null)
     */
    GenericMqttFactory(String url, String port, @Nullable String clientId,
                              @Nullable String user, @Nullable String password){
        mBroker = url+":"+port;
        mClientId = clientId==null ? "" : clientId;
        mUser = user;
        mPassword = password;
    }


    @Override
    public CloutIotClient createClient(Context ctx) {
        return new MqttClient(
             new MqttAndroidClient(ctx, mBroker,mClientId)
        );
    }

    @Override
    public boolean connect(Context ctx, CloutIotClient connection,
                           ConnectionListener connectionListener)
            throws Exception {

        IMqttAsyncClient client = extractMqttClient(connection);

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keystore);


        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        if(mUser!=null && !mUser.isEmpty()) {
            connOpts.setUserName(mUser);
        }
        if(mPassword!=null && !mPassword.isEmpty()) {
            connOpts.setPassword(mPassword.toCharArray());
        }

        return client.connect(connOpts,null,buildMqttListener(connectionListener))!=null;
    }

    @Override
    public Feature.FeatureListener getFeatureListener(CloutIotClient broker,long minUpdateIntervalMs) {
        return new GenericMqttFeatureListener(mClientId,extractMqttClient(broker), minUpdateIntervalMs);
    }

    @Nullable
    @Override
    public Uri getDataPage() {
        return null;
    }

    @Override
    public boolean supportFeature(Feature f) {
        return MqttClientUtil.isSupportedFeature(f);
    }

    @Override
    public boolean enableCloudFwUpgrade(Node node, CloutIotClient mqttConnection, FwUpgradeAvailableCallback callback) {
        return false;
    }

    /**
     * feature listener that will send all the update to the mqtt broker.
     * each update is published as a string in the topic:
     * ClientId/FeatureName/FieldName
     */
    private static class GenericMqttFeatureListener extends SubSamplingFeatureListener {

        private IMqttAsyncClient mBroker;
        private String mClientId;

        /**
         * build an object that will publish all the update to the cloud
         * @param clientId name of the device that generate the data
         * @param client object where publish the data
         */
        GenericMqttFeatureListener(String clientId,IMqttAsyncClient client,long minUpdateInterval) {
            super(minUpdateInterval);
            mBroker = client;
            mClientId=clientId;
        }



        @Override
        public void onNewDataUpdate(Feature f, Feature.Sample sample) {
            Field fields[] = sample.dataDesc;
            Number data[] = sample.data;
            try {
                for(int i =0; i<data.length ; i++){
                    MqttMessage msg = new MqttMessage(data[i].toString().getBytes());
                    msg.setQos(0);
                    if (mBroker.isConnected()) {
                        String topic = MqttClientUtil.getPublishTopic(mClientId,f.getName(),
                                fields[i].getName());
                        mBroker.publish(topic, msg);
                    }
                }

            } catch (MqttException | IllegalArgumentException e) {
                Log.e(getClass().getName(), "Error Logging the sample: " +
                        sample + "\nError:" + e.getMessage());
            }//try catch
        }


    }//

}
