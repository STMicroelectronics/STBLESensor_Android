package com.st.BlueSTSDK.fwDataBase.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BoardCatalogDao {
    @Query("SELECT * FROM BlueSTSDKDataBase ORDER BY BleDeviceID DESC")
    suspend fun getDeviceFirmwares():List<BoardFirmware>

    @Query("DELETE FROM BlueSTSDKDataBase")
    suspend fun deleteAllEntries()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(firmwares: List<BoardFirmware>)

    @Query("DELETE FROM BlueSTSDKDataBase WHERE BleDeviceID = :device_id AND BleFirmwareID LIKE :ble_fw_id")
    suspend fun deleteFwForDevice(device_id:String,ble_fw_id: String)

    @Query("SELECT * FROM BlueSTSDKDataBase WHERE BleDeviceID = :device_id AND BleFirmwareID LIKE :ble_fw_id")
    suspend fun getFwForDevice(device_id:String,ble_fw_id: String): BoardFirmware?

    @Query("SELECT * FROM BlueSTSDKDataBase WHERE BleDeviceID = :device_id")
    suspend fun getFwCompatibleWhiteNode(device_id:String): List<BoardFirmware>?

    @Query("SELECT * FROM BlueSTSDKDataBase WHERE BleDeviceID = :device_id AND FwName LIKE :fw_name")
    suspend fun getAllFwForFwName(device_id:String,fw_name: String): List<BoardFirmware>?
}