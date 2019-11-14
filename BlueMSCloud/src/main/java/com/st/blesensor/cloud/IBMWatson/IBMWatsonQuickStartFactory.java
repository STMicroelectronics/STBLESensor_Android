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

import com.st.blesensor.cloud.R;
import com.st.blesensor.cloud.util.MqttClientConnectionFactory;
import com.st.blesensor.cloud.util.MqttClientUtil;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * Object for create and open a connection to the IBM Watson Iot/BlueMX service using the
 * quickstart page
 */
public class IBMWatsonQuickStartFactory extends MqttClientConnectionFactory {

    //address where send the data
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
    private static String getDeviceId(String nodeType, String deviceName){
        return "d:quickstart:"+nodeType+':'+deviceName;
    }

    private String mDeviceName;
    private String mDeviceType;

    IBMWatsonQuickStartFactory(String type, String id) throws IllegalArgumentException{
        if(!IBMWatsonUtil.isValidString(id))
            throw new IllegalArgumentException("Invalid Device ID");
        if(!IBMWatsonUtil.isValidString(type))
            throw new IllegalArgumentException("Invalid Device Type");

        mDeviceName =id;
        mDeviceType=type;
    }

    @Override
    public boolean connect(Context ctx, CloutIotClient connection,
                           ConnectionListener connectionListener)
            throws Exception {

        IMqttAsyncClient client = extractMqttClient(connection);

        //no authentication is needed
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName("use-token-auth");
        options.setPassword(new char[0]);
        options.setSocketFactory(MqttClientUtil.createSSLSocketFactory(ctx, R.raw.bluemx_cloud));

        return client.connect(options,ctx, buildMqttListener(connectionListener))!=null;
    }

    @Override
    public CloutIotClient createClient(Context ctx) {
        return new MqttClient(
            new MqttAndroidClient(ctx, QUICKSTART_URL,
                    getDeviceId(mDeviceType, mDeviceName))
        );
    }


    @Override
    public Feature.FeatureListener getFeatureListener(CloutIotClient broker,long minUpdateIntervalMs){
        return new IBMWatsonFactory.IBMWatsonMqttFeatureListener(extractMqttClient(broker),
                minUpdateIntervalMs);
    }

    @Override
    public @Nullable Uri getDataPage() {
        return Uri.parse(String.format(QUICKSTART_PAGE_DATA, mDeviceName));
    }

    @Override
    public boolean supportFeature(Feature f) {
        return MqttClientUtil.isSupportedFeature(f);
    }

    @Override
    public boolean enableCloudFwUpgrade(Node node, CloutIotClient mqttConnection,
                                        FwUpgradeAvailableCallback callback) {
        return false;
    }

}
