package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.blesensor.cloud.util.MqttClientUtil;

public class AzureIotCentralFactory2 implements CloudIotClientConnectionFactory {
    private static final String APP_URL_FORMAT = "https://%s.azureiotcentral.com";
    static class ConnectionParameters{
        final String appName;
        final IoTCentralClient2.Parameters param;
        public ConnectionParameters(String appName, IoTCentralClient2.Parameters param) {
            this.appName = appName;
            this.param = param;
        }
    }

    private static @Nullable Uri buildAppUriFromName(String appName){
        if(appName == null)
            return  null;
        return Uri.parse(String.format(APP_URL_FORMAT,appName));
    }

    private final ConnectionParameters mParam;

    public AzureIotCentralFactory2(@NonNull ConnectionParameters param) {
        mParam = param;
    }

    private static boolean isCorrectClient(CloutIotClient client){
        return client instanceof IoTCentralClient2;
    }

    @Override
    public CloutIotClient createClient(@NonNull Context ctx) {
        return new IoTCentralClient2(mParam.param);
    }

    @Override
    public boolean connect(@NonNull Context ctx, @NonNull CloutIotClient client, @NonNull ConnectionListener connectionListener) {

        if(! isCorrectClient(client))
            return false;
        MqttClientUtil.enableTls12(ctx);
        ((IoTCentralClient2) client).connect(connectionListener);

        return true;
    }

    @Override
    public Feature.FeatureListener getFeatureListener(@NonNull CloutIotClient client, long minUpdateIntervalMs) {
        if(! isCorrectClient(client))
            return null;
        return new AzureIoTCentralFeatureListener2((IoTCentralClient2) client,minUpdateIntervalMs);
    }

    @Override
    public void disconnect(@NonNull CloutIotClient client) throws Exception {
        if(! isCorrectClient(client))
            return;

        ((IoTCentralClient2) client).disconnect();
    }

    @Override
    public void destroy(@NonNull CloutIotClient client) {
        if(! isCorrectClient(client))
            return;
        ((IoTCentralClient2) client).destroy();
    }

    @Override
    public boolean isConnected(@NonNull CloutIotClient client) {

        if(! isCorrectClient(client))
            return false;

        return ((IoTCentralClient2) client).isConnected();
    }

    @Nullable
    @Override
    public Uri getDataPage() {
        return buildAppUriFromName(mParam.appName);
    }

    @Override
    public boolean supportFeature(@NonNull Feature f) {
        return AzureIoTCentralFeatureListener2.isSupportedFeature(f);
    }

    @Override
    public boolean enableCloudFwUpgrade(@NonNull Node node, @NonNull CloutIotClient cloudConnection, @NonNull FwUpgradeAvailableCallback callback) {
        if(!isCorrectClient(cloudConnection))
            return false;

        ((IoTCentralClient2) cloudConnection).onFwUpgradeCommand(callback);

        return true;
    }

}
