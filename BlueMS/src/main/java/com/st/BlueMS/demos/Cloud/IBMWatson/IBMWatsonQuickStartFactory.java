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

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Cloud.MqttClientConnectionFactory;
import com.st.BlueMS.demos.Cloud.util.MqttClientUtil;
import com.st.BlueSTSDK.Feature;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

/**
 * Object for create and open a connection to the IBM Watson Iot/BlueMX service using the
 * quickstart page
 */
public class IBMWatsonQuickStartFactory implements MqttClientConnectionFactory {

    private static final long CLOUD_DATA_NOTIFICATION_PERIOD_MS = 1000;

    //address where send the datas
    private static final String QUICKSTART_URL="ssl://quickstart.messaging.internetofthings.ibmcloud.com:8883";
    //private static final String QUICKSTART_URL="tcp://quickstart.messaging.internetofthings.ibmcloud.com:1883";

    //page where see the data send to the cloud
    private static final String QUICKSTART_PAGE_DATA =
            "https://quickstart.internetofthings.ibmcloud.com/#/device/%s/sensor/";

    /**
     * create a device id needed for upload the data
     * @param nodeType ble node name
     * @param deviceName ble node type
     * @return string to use as device id for open the connection
     */
    public static String getDeviceId(String nodeType,String deviceName){
        return "d:quickstart:"+nodeType+':'+deviceName;
    }

    private String mDeviceName;
    private String mDeviceType;

    public IBMWatsonQuickStartFactory(String type,String id) throws IllegalArgumentException{
        if(id==null || id.isEmpty())
            throw new IllegalArgumentException("id can not be empty");
        if(type==null || type.isEmpty())
            throw new IllegalArgumentException("type name can not be empty");

        mDeviceName =id;
        mDeviceType=type;
    }

    @Override
    public IMqttToken connect(Context ctx,MqttAndroidClient client,
                             IMqttActionListener connectionListener)
            throws MqttException, IOException, GeneralSecurityException {

        //no authentication is needed
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("use-token-auth");
        options.setPassword(new char[0]);
        options.setSocketFactory(MqttClientUtil.createSSLSocketFactory(ctx,R.raw.bluemx_cloud));

        return client.connect(options,ctx, connectionListener);
    }

    @Override
    public MqttAndroidClient createClient(Context ctx) {
        return new MqttAndroidClient(ctx, QUICKSTART_URL,
                getDeviceId(mDeviceType, mDeviceName));
    }

    @Override
    public Feature.FeatureListener getFeatureListener(MqttAndroidClient broker){
        return new IBMWatsonQuickStartMqttFeatureListener(broker,CLOUD_DATA_NOTIFICATION_PERIOD_MS);
    }

    @Override
    public @Nullable Uri getDataPage() {
        return Uri.parse(String.format(QUICKSTART_PAGE_DATA, mDeviceName));
    }

    @Override
    public boolean supportFeature(Feature f) {
        return true;
    }


    /**
     * Create a json object that is accepted by the quickstart page to display the data
     */
    public static class IBMWatsonQuickStartMqttFeatureListener extends IBMWatsonFactory.IBMWatsonMqttFeatureListener {

        private Map<Feature,Long> mLastCloudUpdate=new HashMap<>();
        private long mUpdateRateMs;

        /**
         * build and object for send an update to the cloud each updateRateMs milliseconds
         * @param client object to use for publish the data
         * @param updateRateMs minimum time between 2 published samples
         */
        public IBMWatsonQuickStartMqttFeatureListener(MqttAndroidClient client, long updateRateMs){
            super(client);
            mUpdateRateMs=updateRateMs;
        }

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if(!featureNeedCloudUpdate(f,sample.notificationTime))
                return;
            //otherwise send the data
            super.onUpdate(f,sample);
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
    }
}
