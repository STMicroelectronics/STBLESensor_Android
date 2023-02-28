package com.st.blesensor.cloud.stazure.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface RegisteredDeviceDao {

    @Query("SELECT * FROM RegisteredDevice WHERE id = :id")
    suspend fun getRegisterDevice(id:String): RegisteredDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(device: RegisteredDevice)

}