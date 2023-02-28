package com.st.blesensor.cloud.AzureIoTCentralPnP

import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.fwDataBase.db.CloudApp
import com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.AzureCloudDevice
import java.util.*

data class CloudAppConfigured(
    @SerializedName("cloudApp")
    val cloudApp: CloudApp,
    @SerializedName("configurationDone")
    var configurationDone: Boolean=false,
    @SerializedName("authorizationKey")
    var authorizationKey:String?=null,
    @SerializedName("apiToken")
    var apiToken: CloudAPIToken?=null,
    @SerializedName("mcu_id")
    var mcu_id:String?=null
)

data class CloudAPIToken(
    @SerializedName("id")
    val id: String,
    @SerializedName("roles")
    val roles: List<CloudAPIRole>,
    @SerializedName("expiry")
    val expire: Date
)

data class CloudAPIRole(
    @SerializedName("role")
    val role: String
)