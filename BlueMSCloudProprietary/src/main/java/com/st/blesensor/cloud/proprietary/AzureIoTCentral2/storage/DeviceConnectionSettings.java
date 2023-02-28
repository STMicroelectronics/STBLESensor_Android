package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.annotation.NonNull;

@Entity(primaryKeys = {"deviceMac","deviceName"},
        tableName = "DevicesParameters")
public class DeviceConnectionSettings {

    @NonNull
    @ColumnInfo(name = "deviceMac")
    public final String deviceMac;
    @NonNull
    @ColumnInfo(name = "deviceName")
    public final String deviceName;

    @NonNull
    @ColumnInfo(name = "scopeId")
    public final String scopeId;

    @NonNull
    @ColumnInfo(name = "appName")
    public final String appName;

    @NonNull
    @ColumnInfo(name = "appKey")
    public final String appKey;

    @NonNull
    @ColumnInfo(name = "deviceId")
    public final String deviceId;


    public DeviceConnectionSettings(@NonNull String deviceMac,
                                    @NonNull String deviceName,
                                    @NonNull String scopeId,
                                    @NonNull String appName,
                                    @NonNull String appKey,
                                    @NonNull String deviceId) {
        this.deviceMac = deviceMac;
        this.deviceName = deviceName;
        this.scopeId = scopeId;
        this.appName = appName;
        this.appKey = appKey;
        this.deviceId = deviceId;
    }
}
