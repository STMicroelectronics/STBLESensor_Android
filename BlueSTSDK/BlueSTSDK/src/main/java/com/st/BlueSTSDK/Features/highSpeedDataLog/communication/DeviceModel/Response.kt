package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class Response(
        @SerializedName("JSONVersion")
        val version:String,
        @SerializedName("device")
        val device: Device?
)