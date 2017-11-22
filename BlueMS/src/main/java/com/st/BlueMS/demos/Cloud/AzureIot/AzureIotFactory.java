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

package com.st.BlueMS.demos.Cloud.AzureIot;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.st.BlueMS.demos.Cloud.AzureIot.util.ConnectionParameters;
import com.st.BlueMS.demos.Cloud.AzureIot.util.Signature;
import com.st.BlueMS.demos.Cloud.MqttClientConnectionFactory;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AzureIotFactory implements MqttClientConnectionFactory {
    //https://github.com/Microsoft/azure-docs/blob/master/articles/iot-hub/iot-hub-mqtt-support.md
    private static long getCurrentUnixTime(){
        return System.currentTimeMillis()/1000L;
    }

    private static final long TOKEN_VALIDITY_S = 60*60*24; // 1 day

    private final static String MQTT_BROKER_HOSTNANE_FORMAT = "ssl://%s:8883";

    private final static String MQTT_USER_FORMAT = "%s/%s/api-version=2016-11-14";
    private final static String MQTT_PASSWORD_RESOURCE_URI_FORMAT="%s%%2Fdevices%%2F%s";
    private final static String MQTT_PASSWORD_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s";


    private ConnectionParameters mParam;

    public AzureIotFactory(ConnectionParameters params) {
        this.mParam = params;
    }


    @Override
    public MqttAndroidClient createClient(Context ctx) {
        return  new MqttAndroidClient(ctx,getMqttHost(),mParam.deviceId);
    }

    private String getMqttHost(){
        return String.format(MQTT_BROKER_HOSTNANE_FORMAT,mParam.hostName);
    }

    @Override
    public IMqttToken connect(final Context ctx, final IMqttAsyncClient client,
                              final IMqttActionListener connectionListener)
            throws MqttException, IOException, GeneralSecurityException {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(getMqttUser());
        options.setPassword(getMqttPassword().toCharArray());

        return client.connect(options, ctx, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                connectionListener.onSuccess(asyncActionToken);
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                connectionListener.onFailure(asyncActionToken,exception);
            }
        });
    }

    private String getMqttUser(){
        return String.format(MQTT_USER_FORMAT,mParam.hostName,mParam.deviceId);
    }

    private String getMqttPassword(){
        String uri = String.format(MQTT_PASSWORD_RESOURCE_URI_FORMAT,mParam.hostName,mParam.deviceId);
        long expire = getCurrentUnixTime()+ TOKEN_VALIDITY_S;

        Signature sig = new Signature(uri,expire,mParam.sharedAccessKey);
        return String.format(MQTT_PASSWORD_FORMAT,uri,sig.toString(),expire);
    }

    @Override
    public Feature.FeatureListener getFeatureListener(IMqttAsyncClient broker) {
        return new AzureIotMqttLogger(broker,mParam.deviceId);
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

    @Override
    public boolean enableCloudFwUpgrade(Node node, IMqttAsyncClient cloudConnection, FwUpgradeAvailableCallback callback) {
        return false;
    }

    /**
     * log each sample as a json object:
     * {
     *     id: $deviceId
     *     ts: $timestamp
     *     $featureName: value
     *     or
     *     $featureName: {
     *         $fieldName:$value
     *         $fieldName:$value
     *     }
     * }
     */
    private static class AzureIotMqttLogger implements Feature.FeatureListener{

        private IMqttAsyncClient mBroker;
        private String mDeviceId;

        private static final DateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                Locale.US);

        /**
         * build an object that will publish all the update to the cloud
         * @param client object where publish the data
         */
        AzureIotMqttLogger(IMqttAsyncClient client, String deviceId) {
            mBroker = client;
            mDeviceId=deviceId;
        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            try {
                JSONObject obj = prepareMessage(f,sample);
                MqttMessage msg = new MqttMessage(obj.toString().getBytes());
                msg.setQos(0);
                if (mBroker.isConnected())
                    mBroker.publish(getPublishTopic(mDeviceId), msg);

            } catch (JSONException | MqttException e) {
                Log.e(getClass().getName(), "Error Logging the sample: " +
                        sample + "\nError:" + e.getMessage());
            } catch (IllegalArgumentException e){
                //thrown when isconnected is called in an invalid handle -> we are disconnected
            }

        }

        private JSONObject prepareMessage(Feature f, Feature.Sample sample) throws JSONException {
            JSONObject obj=new JSONObject();
            obj.put("id",mDeviceId);
            obj.put("ts",sDataFormat.format(new Date(sample.notificationTime)));
            appendSampleData(obj,f.getName(),sample);
            return obj;
        }

        private static String getPublishTopic(String mDeviceId) {
            return "devices/"+mDeviceId+"/messages/events/";
        }

        /**
         * add the feature sample data to the json object
         * @param obj object where add the data
         * @param featureName feature that generate the data
         * @param sample feature data
         * @throws JSONException if some error happen during the json creation
         */
        private void appendSampleData(JSONObject obj, String featureName, Feature.Sample sample) throws  JSONException{
            Field[] desc = sample.dataDesc;
            if(desc.length==1){
                obj.put(featureName,sample.data[0]);
            }else{
                JSONObject data = new JSONObject();
                for(int i=0; i<desc.length;i++){
                    data.put(desc[i].getName(),sample.data[i]);
                }
                obj.put(featureName,data);
            }//if-else
        }

    }//AzureIotMqttLogger
}
