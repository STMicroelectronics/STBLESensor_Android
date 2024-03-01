package com.st.cloud_azure_iot_central.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CloudAPIRole(
    @SerialName("role")
    val role: String
)
