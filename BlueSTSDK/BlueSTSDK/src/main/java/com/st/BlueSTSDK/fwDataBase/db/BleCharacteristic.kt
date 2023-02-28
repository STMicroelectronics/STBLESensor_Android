package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class BleCharacteristic(
    @SerializedName("name")
    val name: String,
    @SerializedName("uuid")
    val uuid: String,
    @SerializedName("uuid_type")
    val uuid_type: String?,
    @SerializedName("dtmi_name")
    val dtmi_name: String?,
    @SerializedName("format_notify")
    val format_notify: List<BleCharacteristicFormat>?,
    @SerializedName("format_write")
   val format_write: List<BleCharacteristicFormat>?
)
