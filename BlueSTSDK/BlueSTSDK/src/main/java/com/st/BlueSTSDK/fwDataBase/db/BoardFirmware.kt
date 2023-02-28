package com.st.BlueSTSDK.fwDataBase.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName

@Entity(primaryKeys = ["BleDeviceID","BleFirmwareID","FwName"], tableName = "BlueSTSDKDataBase")
@TypeConverters(BleCharacteristicDataConverter::class, OptionByteDataConverter::class,CloudAppDataConverter::class,FotaDetailsDataConverter::class)
data class BoardFirmware(
    @ColumnInfo(name = "BleDeviceID")
    @SerializedName("ble_dev_id")
    val ble_dev_id: String,
    @ColumnInfo(name = "BleFirmwareID")
    @SerializedName("ble_fw_id")
    val ble_fw_id: String,
    @ColumnInfo(name = "BoardName")
    @SerializedName("brd_name")
    val brd_name: String,
    @ColumnInfo(name = "FwVersion")
    @SerializedName("fw_version")
    val fw_version: String,
    @ColumnInfo(name = "FwName")
    @SerializedName("fw_name")
    val fw_name: String,
    @SerializedName("dtmi")
    val dtmi: String?,
    @ColumnInfo(name = "Cloud Applications List")
    @SerializedName("cloud_apps")
    val cloud_apps: List<CloudApp>,
    @ColumnInfo(name = "Characteristics")
    @SerializedName("characteristics")
    val characteristics: List<BleCharacteristic>,
    @ColumnInfo(name = "option_bytes")
    @SerializedName("option_bytes")
    val option_bytes: List<OptionByte>,
    @ColumnInfo(name = "fw_desc")
    @SerializedName("fw_desc")
    val fw_desc: String,
    @ColumnInfo(name = "Change Log")
    @SerializedName("changelog")
    val changelog: String?=null,
    @SerializedName("fota")
    var fota: FotaDetails,
)