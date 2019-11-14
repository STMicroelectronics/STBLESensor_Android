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

package com.st.blesensor.cloud.IBMWatson;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.Nullable;
import android.util.Log;

import com.st.blesensor.cloud.R;
import com.st.blesensor.cloud.util.JSONSampleSerializer;
import com.st.blesensor.cloud.util.MqttClientConnectionFactory;
import com.st.blesensor.cloud.util.MqttClientUtil;
import com.st.blesensor.cloud.util.SubSamplingFeatureListener;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Object for create and open a connection to the IBM Watson Iot/BlueMX service
 */
public class IBMWatsonFactory extends MqttClientConnectionFactory {

    private static final String BLUEMX_URL=
            "ssl://%s.messaging.internetofthings.ibmcloud.com:8883";
    private static final String BLUEMX_PAGE_DATA_FORMAT = "https://%s.internetofthings.ibmcloud.com/dashboard/";

    private static String getDeviceId(String mOrganization,String nodeType ,String deviceName){
        //Log.d("BlueMX", "getDeviceId: "+"d:"+mOrganization+":"+nodeType+':'+deviceName);
        return "d:"+mOrganization+":"+nodeType+':'+deviceName;
    }

    private String mAuthKey;
    private String mDeviceId;
    private String mOrganization;
    private String mDeviceType;

    IBMWatsonFactory(String organization,String authKey, String boardType,String boardId) throws IllegalArgumentException{

        if(organization==null || organization.isEmpty())
            throw new IllegalArgumentException("Organization ID can not be empty");

        if(authKey==null || authKey.isEmpty())
            throw new IllegalArgumentException("Authentication Token can not be empty");

        if(!IBMWatsonUtil.isValidString(boardType))
            throw new IllegalArgumentException("Invalid Board Type");

        if(!IBMWatsonUtil.isValidString(boardId))
            throw new IllegalArgumentException("Invalid Board ID");

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
    public boolean connect(Context ctx, CloutIotClient connection,
                           ConnectionListener connectionListener)
            throws Exception {

        IMqttAsyncClient client = extractMqttClient(connection);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("use-token-auth");
        options.setPassword(mAuthKey.toCharArray());
        options.setSocketFactory(MqttClientUtil.createSSLSocketFactory(ctx, R.raw.bluemx_cloud));

        return client.connect(options,ctx, buildMqttListener(connectionListener))!=null;
    }

    @Override
    public CloutIotClient createClient(Context ctx) {
        return new MqttClient(
                new MqttAndroidClient(ctx, String.format(BLUEMX_URL,mOrganization),
                getDeviceId(mOrganization,mDeviceType,mDeviceId))
        );
    }

    @Override
    public Feature.FeatureListener getFeatureListener(CloutIotClient broker,long minUpdateIntervalMs){
        return new IBMWatsonMqttFeatureListener(extractMqttClient(broker), minUpdateIntervalMs);
    }

    @Override
    public @Nullable Uri getDataPage() {
        return Uri.parse(String.format(BLUEMX_PAGE_DATA_FORMAT,mOrganization));
    }

    @Override
    public boolean supportFeature(Feature f) {
        return MqttClientUtil.isSupportedFeature(f);
    }

    @Override
    public boolean enableCloudFwUpgrade(Node node, CloutIotClient iotConnection, FwUpgradeAvailableCallback callback) {
        return false;
    }


    /**
     * class that publish on all the sample to the cloud using the mqtt protocol
     */
    public static class IBMWatsonMqttFeatureListener extends SubSamplingFeatureListener {

        private IMqttAsyncClient mBroker;

        /**
         * build an object that will publish all the update to the cloud
         * @param client object where publish the data
         */
        public IBMWatsonMqttFeatureListener(IMqttAsyncClient client,long minUpdateInterval) {
            super(minUpdateInterval);
            mBroker = client;
        }

        private static String getPublishTopic(Feature f) {
            return MqttClientUtil.sanitizeTopicName("iot-2/evt/" + f.getName() + "/fmt/json");
        }

        @Override
        public void onNewDataUpdate(Feature f, Feature.Sample sample) {
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
