package com.st.BlueSTSDK.Features.highSpeedDataLog.communication

import com.google.gson.annotations.SerializedName

data class WifSettings(
        @SerializedName("enable") val enable: Boolean?,
        @SerializedName("ssid") val ssid: String?,
        @SerializedName("password") val password: String?,
        @SerializedName("securityType") val securityType: String?
)