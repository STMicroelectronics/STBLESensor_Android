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

package com.st.BlueMS.demos.Cloud.IBMWatson;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Cloud.util.JSONSampleSerializer;
import com.st.BlueMS.demos.Cloud.MqttClientConnectionFactory;
import com.st.BlueMS.demos.Cloud.util.MqttClientUtil;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Object for create and open a connection to the IBM Watson Iot/BlueMX service
 */
public class IBMWatsonFactory implements MqttClientConnectionFactory {

    private static final String BLUEMX_URL=
            "ssl://%s.messaging.internetofthings.ibmcloud.com:8883";
    private static final Uri BLUEMX_PAGE_DATA = Uri.parse("https://play.internetofthings.ibmcloud.com/dashboard/");

    public static String getDeviceId(String mOrganization,String nodeType ,String deviceName){
        Log.d("BlueMX", "getDeviceId: "+"d:"+mOrganization+":"+nodeType+':'+deviceName);
        return "d:"+mOrganization+":"+nodeType+':'+deviceName;
    }

    private String mAuthKey;
    private String mDeviceId;
    private String mOrganization;
    private String mDeviceType;

    public IBMWatsonFactory(String organization,String authKey, String boardType,String boardId) throws IllegalArgumentException{

        if(organization==null || organization.isEmpty())
            throw new IllegalArgumentException("Organization can not be empty");

        if(authKey==null || authKey.isEmpty())
            throw new IllegalArgumentException("authKey can not be empty");

        if(boardType==null || boardType.isEmpty())
            throw new IllegalArgumentException("boardType can not be empty");

        if(boardId==null || boardId.isEmpty())
            throw new IllegalArgumentException("boardId can not be empty");

        mOrganization = organization;
        mAuthKey=authKey;
        mDeviceType=boardType;
        mDeviceId=boardId;
        /*
        mDeviceId="bluemsTest";
        mDeviceType="stm32_nucleo";
        mAuthKey = "qwerty1234";
        mOrganization = "play";*/
    }

    @Override
    public IMqttToken connect(Context ctx,IMqttAsyncClient client,
                              IMqttActionListener connectionListener)
            throws MqttException, IOException, GeneralSecurityException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("use-token-auth");
        options.setPassword(mAuthKey.toCharArray());
        options.setSocketFactory(MqttClientUtil.createSSLSocketFactory(ctx, R.raw.bluemx_cloud));

        return client.connect(options,ctx, connectionListener);
    }

    @Override
    public MqttAndroidClient createClient(Context ctx) {
        Log.d("BlueMX", "URL: "+String.format(BLUEMX_URL,mOrganization));

        return new MqttAndroidClient(ctx, String.format(BLUEMX_URL,mOrganization),
                getDeviceId(mOrganization,mDeviceType,mDeviceId));
    }

    @Override
    public Feature.FeatureListener getFeatureListener(IMqttAsyncClient broker){
        return new IBMWatsonMqttFeatureListener(broker);
    }

    @Override
    public @Nullable Uri getDataPage() {
        return BLUEMX_PAGE_DATA;
    }

    @Override
    public boolean supportFeature(Feature f) {
        //all the feature are supported
        return true;
    }

    @Override
    public boolean enableCloudFwUpgrade(Node node, IMqttAsyncClient mqttConnection, FwUpgradeAvailableCallback callback) {
        return false;
    }


    /**
     * class that publish on all the sample to the cloud using the mqtt protocol
     */
    public static class IBMWatsonMqttFeatureListener implements Feature.FeatureListener {

        private IMqttAsyncClient mBroker;

        /**
         * build an object that will publish all the update to the cloud
         * @param client object where publish the data
         */
        public IBMWatsonMqttFeatureListener(IMqttAsyncClient client) {
            mBroker = client;
        }

        private static String getPublishTopic(Feature f) {
            String featureName = f.getName().replace(' ', '_');
            return "iot-2/evt/" + featureName + "/fmt/json";

        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            try {
                JSONObject obj = JSONSampleSerializer.serialize(sample);
                JSONObject quikStartObj = new JSONObject();
                quikStartObj.put("d", obj);
                MqttMessage msg = new MqttMessage(quikStartObj.toString().getBytes());
                msg.setQos(0);
                if (mBroker.isConnected())
                    mBroker.publish(getPublishTopic(f), msg);
            } catch (JSONException | MqttException | IllegalArgumentException e) {
                Log.e(getClass().getName(), "Error Logging the sample: " +
                        sample + "\nError:" + e.getMessage());
            }

        }
    }

}
