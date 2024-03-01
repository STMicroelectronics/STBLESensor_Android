package com.st.cloud_azure_iot_central.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudTemplateRetrieved (
    @SerialName("etag")
    val etag: String,
    @SerialName("displayName")
    val displayName: String?,
    @SerialName("capabilityModel")
    val capabilityModel: CloudCapabilityModel,
    @SerialName("@id")
    val id: String,
    @SerialName("@type")
    val type: List<String>,
    @SerialName("@context")
    val context: List<String>
)

@Serializable
data class CloudTemplatesList (
    @SerialName("value")
    val list: List<CloudTemplateRetrieved>
)