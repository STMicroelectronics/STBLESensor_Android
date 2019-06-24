package com.st.blesensor.cloud.AzureIoTCentral;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;

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
    public CloutIotClient createClient(Context ctx) {
        return new IoTCentralClient(mScopeId,mDeviceId,mMasterKey);
    }

    @Override
    public boolean connect(Context ctx, CloutIotClient client, ConnectionListener connectionListener) {

        if(! isCorrectClient(client))
            return false;
        MqttClientUtil.enableTls12(ctx);
        ((IoTCentralClient) client).connect(connectionListener);

        return true;
    }

    @Override
    public Feature.FeatureListener getFeatureListener(CloutIotClient client, long minUpdateIntervalMs) {
        if(! isCorrectClient(client))
            return null;
        return new AzureIoTCentralFeatureListener((IoTCentralClient) client,minUpdateIntervalMs);
    }

    @Override
    public void disconnect(CloutIotClient client) throws Exception {
        if(! isCorrectClient(client))
            return;

        ((IoTCentralClient) client).disconnect();
    }

    @Override
    public void destroy(CloutIotClient client) {
        if(! isCorrectClient(client))
            return;
        ((IoTCentralClient) client).destroy();
    }

    @Override
    public boolean isConnected(CloutIotClient client) {

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
    public boolean supportFeature(Feature f) {
        return AzureIoTCentralFeatureListener.isSupportedFeature(f);
    }

    @Override
    public boolean enableCloudFwUpgrade(Node node, CloutIotClient cloudConnection, FwUpgradeAvailableCallback callback) {
        if(!isCorrectClient(cloudConnection))
            return false;

        ((IoTCentralClient) cloudConnection).onFwUpgradeCommand(callback);

        return true;
    }

}
