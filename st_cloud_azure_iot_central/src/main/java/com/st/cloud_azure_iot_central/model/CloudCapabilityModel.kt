package com.st.cloud_azure_iot_central.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudCapabilityModel (
    @SerialName("@id")
    val id: String,
    @SerialName("@type")
    val type: String,
    @SerialName("displayName")
    val displayName: String?=null,
    @SerialName("contents")
    val contents: List<CloudContentCapabilityModel>
)