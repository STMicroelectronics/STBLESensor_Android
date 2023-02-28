package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class BleCharacteristicProperty(
    @SerializedName("length")
    val length: Int,
    @SerializedName("format")
    val format: List<BleCharacteristicFormat>
)
