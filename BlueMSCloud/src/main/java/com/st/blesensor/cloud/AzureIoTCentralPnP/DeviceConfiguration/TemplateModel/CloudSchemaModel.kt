package com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel

import com.google.gson.annotations.SerializedName
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudContentSchemaModel

data class CloudSchemaModel (
    @SerializedName("@id")
    val id: String? = null,
    @SerializedName("@type")
    val type: String? = null,
    @SerializedName("displayName")
    val displayName: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("contents")
    val contents: List<CloudContentSchemaModel>? = null
)