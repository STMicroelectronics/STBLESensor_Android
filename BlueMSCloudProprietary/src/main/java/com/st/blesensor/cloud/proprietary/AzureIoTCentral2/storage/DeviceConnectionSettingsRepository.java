package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceConnectionSettingsRepository {

    private static ExecutorService DB_THREAD = Executors.newCachedThreadPool();

    private DeviceConnectionSettingsDao connectionSettingsDao;

    public DeviceConnectionSettingsRepository(Application appContext){
        connectionSettingsDao = DeviceConnectionSettingsDB.getDatabase(appContext).getConnectionSettings();
    }

    public LiveData<DeviceConnectionSettings> getSettingsFor(@NonNull String deviceMac,
                                                                   @NonNull String deviceName) {
        return connectionSettingsDao.getForDevice(deviceMac,deviceName);
    }

    public void add(DeviceConnectionSettings settings){
        DB_THREAD.execute(()-> connectionSettingsDao.add(settings));
    }

    public void remove(DeviceConnectionSettings settings){
        DB_THREAD.execute(()-> connectionSettingsDao.delete(settings));
    }

}
