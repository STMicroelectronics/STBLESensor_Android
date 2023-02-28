package com.st.blesensor.cloud.proprietary.AzureIoTCentral2;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage.DeviceConnectionSettings;
import com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage.DeviceConnectionSettingsRepository;

public class StoredConnectionViewModel extends AndroidViewModel {

    private DeviceConnectionSettingsRepository mRepository;

    public StoredConnectionViewModel(Application application) {
        super(application);
        mRepository = new DeviceConnectionSettingsRepository(application);
    }

    public void storeSettings(String deviceMac, String deviceName,
                              AzureIotCentralFactory2.ConnectionParameters params){
        mRepository.add(new DeviceConnectionSettings(deviceMac,
                deviceName,
                params.param.appScopeId,
                params.appName,
                params.param.appMasterKey,
                params.param.deviceId));
    }


    public LiveData<DeviceConnectionSettings> loadSettings(String deviceMac, String deviceName){
        return mRepository.getSettingsFor(deviceMac,deviceName);
    }

}