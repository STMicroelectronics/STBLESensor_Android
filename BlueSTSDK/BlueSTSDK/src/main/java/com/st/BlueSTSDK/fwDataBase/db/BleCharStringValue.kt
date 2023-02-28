package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class BleCharStringValue(
    @SerializedName("display_name")
    val display_name: String,
    @SerializedName("value")
    val value: Int
)
