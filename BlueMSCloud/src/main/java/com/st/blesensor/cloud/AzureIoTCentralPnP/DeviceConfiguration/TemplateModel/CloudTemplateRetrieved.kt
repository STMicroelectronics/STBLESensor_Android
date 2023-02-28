package com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel

import com.google.gson.annotations.SerializedName

data class CloudTemplateRetrieved (
//    @SerializedName("etag")
//    val etag: String,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("capabilityModel")
    val capabilityModel: CloudCapabilityModel,
    @SerializedName("@id")
    val id: String,
    @SerializedName("@type")
    val type: List<String>,
    @SerializedName("@context")
    val context: List<String>
)

data class CloudTemplatesList (
    @SerializedName("value")
    val list: List<CloudTemplateRetrieved>
)