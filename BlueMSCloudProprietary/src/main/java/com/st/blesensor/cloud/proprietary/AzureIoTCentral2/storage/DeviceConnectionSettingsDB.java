package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DeviceConnectionSettings.class},
        version = 1,
        exportSchema = false)
public abstract class DeviceConnectionSettingsDB extends RoomDatabase {

    public abstract DeviceConnectionSettingsDao getConnectionSettings();

    private static volatile DeviceConnectionSettingsDB INSTANCE;

    static DeviceConnectionSettingsDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (DeviceConnectionSettingsDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            DeviceConnectionSettingsDB.class, "DeviceConnectionSettingsDB")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
