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

package com.st.blesensor.cloud.proprietary.STAwsIot;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureProximity;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Node;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.blesensor.cloud.util.SubSamplingFeatureListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.Arrays;


class STAwsIotMqttFactory implements CloudIotClientConnectionFactory {

    private final static String mEndpoint ="a1dyrwwtjpwo0n-ats.iot.us-east-1.amazonaws.com";

    private static class AwsConnection implements CloutIotClient{

        public final AWSIotMqttManager connection;
        public boolean isConnected=false;

        AwsConnection(AWSIotMqttManager manager){
            connection = manager;
        }
    }

    private static @Nullable AwsConnection extractConnection(CloutIotClient client){
        if(!(client instanceof AwsConnection))
            return null;
        return ((AwsConnection) client);
    }

    private String mClientId;
    private Uri mDeviceCertificatePrivateKey;

    STAwsIotMqttFactory(@Nullable String clientId, @Nullable Uri deviceCertPrivateKey){


        if(clientId==null || clientId.isEmpty())
            throw new IllegalArgumentException("Client Id Must Not be empty");

        if(deviceCertPrivateKey==null)
            throw new IllegalArgumentException("Invalid certificate private key path");

        mClientId                    = clientId;
        mDeviceCertificatePrivateKey = deviceCertPrivateKey;
    }


    @Override
    public CloutIotClient createClient(@NonNull Context ctx) {

        AWSIotMqttManager manager = new AWSIotMqttManager(mClientId,mEndpoint);
        manager.setAutoReconnect(false);
        return new AwsConnection(manager);
    }

    @Override
    public boolean connect(@NonNull Context ctx, @NonNull CloutIotClient client, @NonNull final ConnectionListener connectionListener) throws Exception {

        if(client instanceof AwsConnection) {
            new LoadKeyStoreAndConnect(ctx, (AwsConnection)client, mDeviceCertificatePrivateKey, connectionListener).execute();
            return true;
        }

        return false;
    }

    @Override
    public void disconnect(@NonNull CloutIotClient client) throws Exception {
        if(client instanceof AwsConnection) {
            AwsConnection connection = (AwsConnection)client;
            connection.connection.disconnect();
            connection.isConnected=false;
        }
    }


    private static class LoadKeyStoreAndConnect extends AsyncTask<Void,Void,KeyStore>{

        // Filename of KeyStore file on the filesystem
        private static final String KEYSTORE_NAME = "iot_keystore";
        // Password for the private key in the KeyStore
        private static final String KEYSTORE_PASSWORD = "password";
        // Certificate and key aliases in the KeyStore
        private static final String CERTIFICATE_ID = "default";

        private Context mContext;
        private final AwsConnection mConnection;
        private final Uri mCertificatePrivateKeyFile;
        private final ConnectionListener mListener;

        private String getKeystoreLocation(){
            return mContext.getFilesDir().getAbsolutePath();
        }

        LoadKeyStoreAndConnect(Context c,AwsConnection connection, Uri certificatePrivateKeyFile, ConnectionListener listener){
            mContext = c;

            mConnection = connection;
            this.mCertificatePrivateKeyFile = certificatePrivateKeyFile;
            this.mListener = listener;
        }

        private String loadFile(Uri uri) throws IOException {
            InputStream in = mContext.getContentResolver().openInputStream(uri);
            if (in == null)
               return  null;
           ByteArrayOutputStream result = new ByteArrayOutputStream();
           byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                result.write(buffer, 0, length);
           }
            return result.toString(StandardCharsets.UTF_8.name());
        }

        private void removeOldKeyStore(String path){
            if(AWSIotKeystoreHelper.isKeystorePresent(path,KEYSTORE_NAME))
                AWSIotKeystoreHelper.deleteKeystoreAlias(CERTIFICATE_ID,path,KEYSTORE_NAME,KEYSTORE_PASSWORD);
        }

        @Override
        protected KeyStore doInBackground(Void... params) {
            try {
                String certPrivateKeyContent = loadFile(mCertificatePrivateKeyFile);

                String endCertificateString ="-----END CERTIFICATE-----";
                String endKeyString="-----END RSA PRIVATE KEY-----";

                int beginCertPos = certPrivateKeyContent.indexOf("-----BEGIN CERTIFICATE-----");
                int endCertPos = certPrivateKeyContent.indexOf(endCertificateString) + endCertificateString.length();
                int beginKeyPos = certPrivateKeyContent.indexOf("-----BEGIN RSA PRIVATE KEY-----");
                int endKeyPos = certPrivateKeyContent.indexOf(endKeyString) + endKeyString.length();
                String certContent = certPrivateKeyContent.substring(beginCertPos,endCertPos);
                String prvKeyContent = certPrivateKeyContent.substring(beginKeyPos,endKeyPos);

                String keystorePath = getKeystoreLocation();
                removeOldKeyStore(keystorePath);
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(CERTIFICATE_ID,
                        certContent,
                        prvKeyContent,
                        keystorePath, KEYSTORE_NAME, KEYSTORE_PASSWORD);

                // load keystore from file into memory to pass on
                // connection
                return AWSIotKeystoreHelper.getIotKeystore(CERTIFICATE_ID,
                        keystorePath, KEYSTORE_NAME,KEYSTORE_PASSWORD);

            } catch (IOException | IllegalArgumentException e) {
                mListener.onFailure(e);
                e.printStackTrace();
            //if the certificate file has invalid format an array of of band is throw
            } catch (ArrayIndexOutOfBoundsException e){
                IllegalArgumentException newExc = new IllegalArgumentException("Invalid certificate format");
                mListener.onFailure(newExc);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(KeyStore keyStore) {
            super.onPostExecute(keyStore);
            if(keyStore==null)
                return;
            mConnection.connection.connect(keyStore, (status, throwable) -> {
                if(throwable !=null){
                    mListener.onFailure(throwable);
                    return;
                }

                if(status == AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected) {
                    mConnection.isConnected=true;
                    mListener.onSuccess();
                }
            });
        }
    }

    @Override
    public void destroy(@NonNull CloutIotClient client) {  }

    @Override
    public boolean isConnected(@NonNull CloutIotClient client) {
        AwsConnection connection = extractConnection(client);
        return connection != null && connection.isConnected;
    }

    @Override
    public Feature.FeatureListener getFeatureListener(@NonNull CloutIotClient client, long minUpdateIntervalMs) {
        AwsConnection connection = extractConnection(client);
        if(connection!=null)
            return new AwsMqttFeatureListener(mClientId,connection, minUpdateIntervalMs);
        return null;
    }

    @Nullable
    @Override
    public Uri getDataPage() {
        return Uri.parse("http://st-dashboard-iot-v2.s3-website-us-east-1.amazonaws.com/");
    }

    private static Class<? extends Feature> SUPPORTED_FEATURE[] = new Class[]{
            FeatureAcceleration.class,
            FeatureGyroscope.class,
            FeatureMagnetometer.class,
            FeatureTemperature.class,
            FeatureHumidity.class,
            FeaturePressure.class,
            FeatureProximity.class
    };

    @Override
    public boolean supportFeature(@NonNull Feature f) {
        return Arrays.asList(SUPPORTED_FEATURE).contains(f.getClass());
    }

    @Override
    public boolean enableCloudFwUpgrade(@NonNull Node node, @NonNull CloutIotClient cloudConnection, @NonNull FwUpgradeAvailableCallback callback) {
        return false;
    }

    /**
     * feature listener that will send all the update to the mqtt broker.
     * each update is published as a string in the topic:
     * ClientId/FeatureName/FieldName
     */
    private static class AwsMqttFeatureListener extends SubSamplingFeatureListener {

        private AwsConnection mConnection;
        private String mClientId;

        /**
         * build an object that will publish all the update to the cloud
         * @param clientId name of the device that generate the data
         * @param client object where publish the data
         */
        AwsMqttFeatureListener(String clientId,AwsConnection client,long minUpdateInterval) {
            super(minUpdateInterval);
            mConnection = client;
            mClientId=clientId;
        }


        private JSONObject prepareMessage(Feature f, Feature.Sample sample) throws JSONException {
            JSONObject obj=new JSONObject();
            obj.put("Board_id",mClientId);

            if(f instanceof FeatureAcceleration) {
                obj.put("ACC-X", Math.round(FeatureAcceleration.getAccX(sample)));
                obj.put("ACC-Y", Math.round(FeatureAcceleration.getAccY(sample)));
                obj.put("ACC-Z", Math.round(FeatureAcceleration.getAccZ(sample)));
            }
            else if (f instanceof FeatureGyroscope) {
                obj.put("GYR-X", Math.round(FeatureGyroscope.getGyroX(sample)));
                obj.put("GYR-Y", Math.round(FeatureGyroscope.getGyroY(sample)));
                obj.put("GYR-Z", Math.round(FeatureGyroscope.getGyroZ(sample)));
            }
            else if (f instanceof FeatureMagnetometer) {
                obj.put("MAG-X", Math.round(FeatureMagnetometer.getMagX(sample)));
                obj.put("MAG-Y", Math.round(FeatureMagnetometer.getMagY(sample)));
                obj.put("MAG-Z", Math.round(FeatureMagnetometer.getMagZ(sample)));
            }
            else if (f instanceof FeatureTemperature)
               obj.put("Temperature",FeatureTemperature.getTemperature(sample));
            else if( f instanceof FeatureHumidity)
                obj.put("Humidity",FeatureHumidity.getHumidity(sample));
            else if( f instanceof FeaturePressure)
                obj.put("Pressure",FeaturePressure.getPressure(sample));
            else if( f instanceof FeatureProximity)
                obj.put("Proximity",FeatureProximity.getProximityDistance(sample));
            return obj;
        }


        @Override
        public void onNewDataUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            if(!mConnection.isConnected)
                return;
            final String topic = "telemetrydata/sensors";

            try {
                String jsonData = prepareMessage(f,sample).toString();
                mConnection.connection.publishString(jsonData,
                        topic,
                        AWSIotMqttQos.QOS0);
            }catch (AmazonClientException | JSONException e){
                e.printStackTrace();
            }
        }//onUpdate
    }//

}
