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

package com.st.BlueMS.demos.Cloud.GenericMqtt;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.st.BlueMS.demos.Cloud.MqttClientConnectionFactory;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * create a connection to a generic mqtt broker
 */
public class GenericMqttFactory implements MqttClientConnectionFactory {

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
     * @param clientId client id (can be empty/null)
     * @param user connection user (can be empty/null)
     * @param password connection password (can be empty/null)
     */
    public GenericMqttFactory(String url, String port, @Nullable String clientId,
                              @Nullable String user, @Nullable String password){
        mBroker = url+":"+port;
        mClientId = clientId==null ? "" : clientId;
        mUser = user;
        mPassword = password;
    }


    @Override
    public MqttAndroidClient createClient(Context ctx) {
        return new MqttAndroidClient(ctx, mBroker,mClientId);
    }

    @Override
    public IMqttToken connect(Context ctx, MqttAndroidClient client, IMqttActionListener connectionListener)
            throws MqttException, IOException, GeneralSecurityException {


        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        if(mUser!=null && !mUser.isEmpty()) {
            connOpts.setUserName(mUser);
        }
        if(mPassword!=null && !mPassword.isEmpty()) {
            connOpts.setPassword(mPassword.toCharArray());
        }

        return client.connect(connOpts,connectionListener);
    }

    @Override
    public Feature.FeatureListener getFeatureListener(MqttAndroidClient broker) {
        return new GenericMqttFeatureListener(mClientId,broker);
    }

    @Nullable
    @Override
    public Uri getDataPage() {
        return null;
    }

    @Override
    public boolean supportFeature(Feature f) {
        return true;
    }

    /**
     * feature listener that will send all the update to the mqtt broker.
     * each update is published as a string in the topic:
     * ClientId/FeatureName/FieldName
     */
    public static class GenericMqttFeatureListener implements Feature.FeatureListener {

        private MqttAndroidClient mBroker;
        private String mClientId;

        /**
         * build an object that will publish all the update to the cloud
         * @param clientId name of the device that generate the data
         * @param client object where publish the data
         */
        public GenericMqttFeatureListener(String clientId,MqttAndroidClient client) {
            mBroker = client;
            mClientId=clientId;
        }

        private String getPublishTopic(Feature f,Field field) {
            return (mClientId+"/"+f.getName()+"/"+field.getName()).toLowerCase().replace(' ', '_');
        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            Field fields[] = sample.dataDesc;
            Number data[] = sample.data;
            try {
                for(int i =0; i<data.length ; i++){
                    MqttMessage msg = new MqttMessage(data[i].toString().getBytes());
                    if (mBroker.isConnected())
                        mBroker.publish(getPublishTopic(f,fields[i]), msg);
                }

            } catch (MqttException | IllegalArgumentException e) {
                Log.e(getClass().getName(), "Error Logging the sample: " +
                    sample + "\nError:" + e.getMessage());
            }//try catch
        }//onUpdate
    }//

}
