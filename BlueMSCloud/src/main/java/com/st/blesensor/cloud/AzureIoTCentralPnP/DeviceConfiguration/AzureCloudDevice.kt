package com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration

import com.google.gson.annotations.SerializedName
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel.CloudTemplateRetrieved

data class AzureCloudDevice(
    @SerializedName("id")
    val id: String,
    @SerializedName("etag")
    val etag: String? = null,
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("simulated")
    val simulated: Boolean = false,
    @SerializedName("provisioned")
    val provisioned: Boolean? = null,

    @SerializedName("template")
    val template: String,

    @SerializedName("templateModel")
    var templateModel: CloudTemplateRetrieved?=null,

    @SerializedName("enabled")
    val enabled: Boolean = true,

    @SerializedName("credentials")
    var credentials: CloudDeviceCredentials? = null,

    @SerializedName("macAdd")
    var macAdd: String?=null,

    @SerializedName("connectedCloudAppUrl")
    var connectedCloudAppUrl: String?=null
)

data class CloudDeviceCredentials(
    @SerializedName("idScope")
    val idScope: String? = null,
    @SerializedName("symmetricKey")
    val symmetricKey: CloudSymmetricKey? = null
)

data class CloudSymmetricKey(
    @SerializedName("primaryKey")
    val primaryKey: String? = null,
    @SerializedName("secondaryKey")
    val secondaryKey: String? = null
)

data class CloudDevicesList(
    @SerializedName("value")
    val list: MutableList<AzureCloudDevice>
)