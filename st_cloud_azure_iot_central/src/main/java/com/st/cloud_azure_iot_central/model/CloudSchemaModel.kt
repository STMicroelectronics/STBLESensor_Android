package com.st.cloud_azure_iot_central.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudSchemaModel (
    @SerialName("@id")
    val id: String? = null,
    @SerialName("@type")
    val type: String? = null,
    @SerialName("displayName")
    val displayName: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("contents")
    val contents: List<CloudContentSchemaModel>? = null
)