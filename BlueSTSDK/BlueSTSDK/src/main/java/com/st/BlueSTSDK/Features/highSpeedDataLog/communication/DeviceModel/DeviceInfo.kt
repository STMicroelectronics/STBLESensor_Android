package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class DeviceInfo(
        @SerializedName("serialNumber") var serialNumber: String, //TODO update ongoing FW side (probably this will be in a sort of UUID format (with no spaces))
        @SerializedName("alias") var alias: String,
        @SerializedName("partNumber") var partNumber: String? = null,
        @SerializedName("URL") val URL: String? = null,
        @SerializedName("fwName") val fwName: String? = null,
        @SerializedName("fwVersion") val fwVersion: String? = null,
        @SerializedName("dataFileExt") val dataFileExt: String? = null,
        @SerializedName("dataFileFormat") val dataFileFormat: String? = null
)

