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

package com.st.blesensor.cloud.proprietary.STAzureIot;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.st.blesensor.cloud.AzureIot.AzureIotFactory;
import com.st.blesensor.cloud.AzureIot.util.ConnectionParameters;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.blesensor.cloud.util.SubSamplingFeatureListener;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;
import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.AuthToken;
import com.st.blesensor.cloud.proprietary.STAzureIot.FwUpgrade.DeviceTwinConnection;
import com.st.blesensor.cloud.proprietary.STAzureIot.FwUpgrade.UpgradableDevice;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * connect directly to a specific ST Azure Hub
 */
public class STAzureIotFactory extends AzureIotFactory {

    private final static String BASE_DATA_URL =("https://stm32ode.azurewebsites.net/Home/Index/");
    private final static String HUB_NAME ="STM32IoTHub.azure-devices.net";

    private String mDeviceId;

    STAzureIotFactory(AuthToken token) {
        super(new ConnectionParameters(HUB_NAME,token.getDeviceID(),token.getToken()));
        mDeviceId = token.getDeviceID();
    }

    @Override
    public Feature.FeatureListener getFeatureListener(@NonNull CloudIotClientConnectionFactory.CloutIotClient broker, long minUpdateIntervalMs) {
        return new STAzureFeatureListener(extractMqttClient(broker),mDeviceId, minUpdateIntervalMs);
    }

    @Nullable
    @Override
    public Uri getDataPage() {
        return Uri.parse(BASE_DATA_URL+mDeviceId);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Feature> SUPPORTED_FEATURE[] = new Class[]{
            FeatureAcceleration.class,
            FeatureGyroscope.class,
            FeatureTemperature.class,
            FeatureHumidity.class,
            FeaturePressure.class
    };

    @Override
    public boolean supportFeature(@NonNull Feature f) {
        return supportedDemoFeature(f);
    }

    private static boolean supportedDemoFeature(Feature f){
        //define a static version to use it inside the STAzureFeatureListener without capturing
        //a reference to the Factory
        return Arrays.asList(SUPPORTED_FEATURE).contains(f.getClass());
    }

    /**
     * Generate an mqtt message compatible with the Azure Iot demo
     * it works only for acceleration,gyroscope,temperature and humidity
     */
    private static class STAzureFeatureListener extends SubSamplingFeatureListener{

        private IMqttAsyncClient mBroker;
        private String mDeviceId;

        private static final DateFormat sDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                Locale.US);

        /**
         * build an object that will publish all the update to the cloud
         * @param client object where publish the data
         */
        STAzureFeatureListener(IMqttAsyncClient client, String deviceId, long minUpdateInterval) {
            super(minUpdateInterval);
            mBroker = client;
            mDeviceId=deviceId;
        }

        @Override
        public void onNewDataUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            //return if not supported
            if(!supportedDemoFeature(f))
                return;

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

            if(f instanceof FeatureAcceleration)
                appendAccelerationData(obj,sample);
            else if (f instanceof FeatureGyroscope)
                appendGyroscopeData(obj,sample);
            else if (f instanceof FeatureTemperature)
                appendTemperatureData(obj,sample);
            else if( f instanceof FeatureHumidity)
                appendHumidityData(obj,sample);
            else if( f instanceof FeaturePressure)
                appendPressureData(obj,sample);

            return obj;
        }

        private static String getPublishTopic(String mDeviceId) {
            return "devices/"+mDeviceId+"/messages/events/";
        }

        private void appendHumidityData(JSONObject obj, Feature.Sample sample) throws JSONException {
            obj.put("Humidity",FeatureHumidity.getHumidity(sample));
        }

        private void appendPressureData(JSONObject obj, Feature.Sample sample) throws JSONException {
            obj.put("Pressure",FeaturePressure.getPressure(sample));
        }

        private void appendTemperatureData(JSONObject obj, Feature.Sample sample) throws JSONException {
            obj.put("Temperature",FeatureTemperature.getTemperature(sample));
        }

        private void appendGyroscopeData(JSONObject obj, Feature.Sample sample) throws JSONException {
            obj.put("gyrX",FeatureGyroscope.getGyroX(sample));
            obj.put("gyrY",FeatureGyroscope.getGyroY(sample));
            obj.put("gyrZ",FeatureGyroscope.getGyroZ(sample));
        }

        private void appendAccelerationData(JSONObject obj, Feature.Sample sample) throws JSONException {
            obj.put("accX",FeatureAcceleration.getAccX(sample));
            obj.put("accY",FeatureAcceleration.getAccY(sample));
            obj.put("accZ",FeatureAcceleration.getAccZ(sample));
        }
    }

    @Override
    public boolean enableCloudFwUpgrade(@NonNull Node node, @NonNull final CloudIotClientConnectionFactory.CloutIotClient connection,
                                        @NonNull final CloudIotClientConnectionFactory.FwUpgradeAvailableCallback callback) {

        final IMqttAsyncClient cloudConnection = extractMqttClient(connection);

        FwVersionConsole console = FwVersionConsole.getFwVersionConsole(node);
        if(console==null) // no upgrade fw supported
            return false;

        console.setLicenseConsoleListener((console1, type, version) -> {
            //remove the listener from the console
            console1.setLicenseConsoleListener(null);

            if(version==null)
                return;
            //create and set up the device
            UpgradableDevice device = new UpgradableDevice(mDeviceId);
            device.setCurrentFwVersion((FwVersionBoard)version);
            //create the connection and update the device values
            DeviceTwinConnection twinConnection = new DeviceTwinConnection(cloudConnection);
            twinConnection.updateRemoteTwin(device);
            //register the callback when the fw upgrade is trigger
            device.addFwUpgradeAvailableCallback(twinConnection,callback);
        });
        //read the current fw version
        console.readVersion(FirmwareType.BOARD_FW);
        return true;
    }

}
