@file:UseSerializers(DateSerializer::class)

package com.st.cloud_azure_iot_central.model

import com.st.blue_sdk.board_catalog.api.serializers.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@Serializable
data class CloudAPIToken(
    @SerialName("id")
    val id: String,
    @SerialName("roles")
    val roles: List<CloudAPIRole>,
    @SerialName("expiry")
    val expire: Date
)