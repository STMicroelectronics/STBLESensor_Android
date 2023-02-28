package com.st.blesensor.cloud.proprietary.AzureIoTCentral2.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.annotation.NonNull;

import java.util.List;

@Dao
public interface DeviceConnectionSettingsDao {

        @Query("SELECT * FROM DevicesParameters")
        LiveData<List<DeviceConnectionSettings>> getAll();

        @Query("SELECT * FROM DevicesParameters WHERE deviceMac = :mac AND deviceName = :name")
        LiveData<DeviceConnectionSettings> getForDevice(@NonNull String mac,@NonNull String name);

        @Delete
        void delete(DeviceConnectionSettings settings);

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void add(DeviceConnectionSettings ... settings);


}
