package com.st.cloud_azure_iot_central.model

import com.st.cloud_azure_iot_central.model.serializer.CloudContentCapabilityModelSerializer
import com.st.cloud_azure_iot_central.model.serializer.StringToArrayTypeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable(with = CloudContentCapabilityModelSerializer::class)
sealed class CloudContentCapabilityModel {
    @Serializable
    @SerialName("schema_obj")
    data class CloudContentCapabilityModelObj (
        @SerialName("@id")
        val id: String?=null,
        @SerialName("@type")
        @Serializable(with = StringToArrayTypeSerializer::class)
        val type: List<String>,
        @SerialName("displayName")
        val displayName: String?=null,
        @SerialName("name")
        val name: String,
        @SerialName("schema")
        val schema: CloudSchemaModel? = null
    ): CloudContentCapabilityModel()

    @Serializable
    @SerialName("schema_string")
    data class CloudContentCapabilityModelString (
        @SerialName("@id")
        val id: String?=null,
        @SerialName("@type")
        @Serializable(with = StringToArrayTypeSerializer::class)
        val type: List<String>,
        @SerialName("displayName")
        val displayName: String?=null,
        @SerialName("name")
        val name: String,
        @SerialName("schema")
        val schema: String? = null
    ): CloudContentCapabilityModel()
}