package com.st.cloud_azure_iot_central.model

import com.st.blue_sdk.board_catalog.models.CloudApp
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class CloudAppConfigured(
    @SerialName("cloudApp")
    val cloudApp: CloudApp,
    @SerialName("authorizationKey")
    var authorizationKey: String? = null,
    @SerialName("apiToken")
    var apiToken: CloudAPIToken? = null,
    @SerialName("appIndex")
    var appIndex: Int = 0
) {
    //is the token expired?
    @Transient
    var apiTokenExpired: Boolean = false
}