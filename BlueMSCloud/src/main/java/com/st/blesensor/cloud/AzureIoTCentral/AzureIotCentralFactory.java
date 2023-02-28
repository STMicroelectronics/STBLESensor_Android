/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.blesensor.cloud.AzureIoTCentral;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.blesensor.cloud.util.MqttClientUtil;

public class AzureIotCentralFactory implements CloudIotClientConnectionFactory {

    private final String mScopeId;
    private final String mDeviceId;
    private final String mMasterKey;

    public AzureIotCentralFactory(String scopeId, String deviceId, String masterKey) {
        mMasterKey = masterKey;
        mScopeId = scopeId;
        mDeviceId = deviceId;
    }

    private static boolean isCorrectClient(CloutIotClient client){
        return client instanceof IoTCentralClient;
    }

    @Override
    public CloutIotClient createClient(@NonNull Context ctx) {
        return new IoTCentralClient(mScopeId,mDeviceId,mMasterKey);
    }

    @Override
    public boolean connect(@NonNull Context ctx, @NonNull CloutIotClient client, @NonNull ConnectionListener connectionListener) {

        if(! isCorrectClient(client))
            return false;
        MqttClientUtil.enableTls12(ctx);
        ((IoTCentralClient) client).connect(connectionListener);

        return true;
    }

    @Override
    public Feature.FeatureListener getFeatureListener(@NonNull CloutIotClient client, long minUpdateIntervalMs) {
        if(! isCorrectClient(client))
            return null;
        return new AzureIoTCentralFeatureListener((IoTCentralClient) client,minUpdateIntervalMs);
    }

    @Override
    public void disconnect(@NonNull CloutIotClient client) throws Exception {
        if(! isCorrectClient(client))
            return;

        ((IoTCentralClient) client).disconnect();
    }

    @Override
    public void destroy(@NonNull CloutIotClient client) {
        if(! isCorrectClient(client))
            return;
        ((IoTCentralClient) client).destroy();
    }

    @Override
    public boolean isConnected(@NonNull CloutIotClient client) {

        if(! isCorrectClient(client))
            return false;

        return ((IoTCentralClient) client).isConnected();
    }

    @Nullable
    @Override
    public Uri getDataPage() {
        return Uri.parse("https://apps.azureiotcentral.com");
    }

    @Override
    public boolean supportFeature(@NonNull Feature f) {
        return AzureIoTCentralFeatureListener.isSupportedFeature(f);
    }

    @Override
    public boolean enableCloudFwUpgrade(@NonNull Node node, @NonNull CloutIotClient cloudConnection, @NonNull FwUpgradeAvailableCallback callback) {
        if(!isCorrectClient(cloudConnection))
            return false;

        ((IoTCentralClient) cloudConnection).onFwUpgradeCommand(callback);

        return true;
    }

}
