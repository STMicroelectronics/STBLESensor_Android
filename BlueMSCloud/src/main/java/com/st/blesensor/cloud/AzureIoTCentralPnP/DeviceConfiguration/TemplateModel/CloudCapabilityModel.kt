package com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel

import com.google.gson.annotations.SerializedName

data class CloudCapabilityModel (
    @SerializedName("@id")
    val id: String,
    @SerializedName("@type")
    val type: String,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("contents")
    val contents: List<CloudContentCapabilityModel>
)