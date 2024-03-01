package com.st.cloud_azure_iot_central.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AzureCloudDevice(
    @SerialName("id")
    val id: String,
    @SerialName("etag")
    val etag: String? = null,
    @SerialName("displayName")
    val displayName: String,
    @SerialName("simulated")
    val simulated: Boolean = false,
    @SerialName("provisioned")
    val provisioned: Boolean? = null,

    @SerialName("template")
    val template: String,

    @SerialName("templateModel")
    var templateModel: CloudTemplateRetrieved?=null,

    @SerialName("enabled")
    var enabled: Boolean = true,

    @SerialName("selected")
    var selected: Boolean = false,

    @SerialName("credentials")
    var credentials: CloudDeviceCredentials? = null,

    @SerialName("macAdd")
    var macAdd: String?=null,

    @SerialName("connectedCloudAppUrl")
    var connectedCloudAppUrl: String?=null
)

@Serializable
data class CloudDeviceCredentials(
    @SerialName("idScope")
    val idScope: String? = null,
    @SerialName("symmetricKey")
    val symmetricKey: CloudSymmetricKey? = null
)

@Serializable
data class CloudSymmetricKey(
    @SerialName("primaryKey")
    val primaryKey: String? = null,
    @SerialName("secondaryKey")
    val secondaryKey: String? = null
)

@Serializable
data class CloudDevicesList(
    @SerialName("value")
    var list: MutableList<AzureCloudDevice>
)