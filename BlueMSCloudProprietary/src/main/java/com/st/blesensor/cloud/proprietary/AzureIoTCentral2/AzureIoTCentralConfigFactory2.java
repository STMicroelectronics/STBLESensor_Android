package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.ViewGroup;
import com.st.BlueSTSDK.Node;
import com.st.blesensor.cloud.CloudIotClientConfigurationFactory;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.blesensor.cloud.util.MqttClientUtil;

public class AzureIoTCentralConfigFactory2 implements CloudIotClientConfigurationFactory {
    private static final String CLOUD_NAME = "Azure IoT Central - SensorTile.Box";
    private static final String CONFIG_FRAGMENT_TAG  = AzureIoTCentralConfigFactory2.class.getCanonicalName()+".CONFIG_FRAGMENT";


    @Override
    public void attachParameterConfiguration(@NonNull FragmentManager fm, @NonNull ViewGroup root, @Nullable String id_mcu,@Nullable String fw_version) {

        //check if a fragment is already attach, and remove it to attach the new one
        Fragment mConfigFragment = (IoTCentralLoginFragment)
                fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);

        FragmentTransaction transaction = fm.beginTransaction();
        if(mConfigFragment==null) {
            IoTCentralLoginFragment newFragment = new IoTCentralLoginFragment();
            transaction.add(root.getId(), newFragment, CONFIG_FRAGMENT_TAG);
            transaction.commitNow();
        }
    }

    @Override
    public void detachParameterConfiguration(@NonNull FragmentManager fm, @NonNull ViewGroup root) {
        Fragment configFragment = fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);
        if(configFragment!=null){
            fm.beginTransaction().remove(configFragment).commit();
        }
    }

    @Override
    public void loadDefaultParameters(@NonNull FragmentManager fm,@Nullable Node n) {
        IoTCentralLoginFragment mConfigFragment = (IoTCentralLoginFragment)
                fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);
        if(n!=null && mConfigFragment!=null){
            mConfigFragment.setDeviceInfo(n.getTag(),getIotCentralDeviceName(n));
        }
    }

    private static String getIotCentralDeviceName(Node n){
        return MqttClientUtil.getDefaultCloudDeviceName(n).replace("_","");
    }

    @Override
    public String getName() {
        return CLOUD_NAME;
    }

    @Override
    public CloudIotClientConnectionFactory getConnectionFactory(@NonNull FragmentManager fm) throws IllegalArgumentException {
        IoTCentralLoginFragment mConfigFragment = (IoTCentralLoginFragment)
                fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);
        AzureIotCentralFactory2.ConnectionParameters param = mConfigFragment.getConnectionParam();
        if(param == null){
            throw new IllegalArgumentException("Register the device");
        }
        return new AzureIotCentralFactory2(param);
    }
}
