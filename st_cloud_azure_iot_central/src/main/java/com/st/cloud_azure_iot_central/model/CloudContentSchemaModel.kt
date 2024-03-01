package com.st.cloud_azure_iot_central.model

import com.st.cloud_azure_iot_central.model.serializer.CloudContentSchemaModelSerializer
import com.st.cloud_azure_iot_central.model.serializer.StringToArrayTypeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable(with = CloudContentSchemaModelSerializer::class)
sealed class CloudContentSchemaModel {
    @Serializable
    @SerialName("schema_obj")
    data class CloudContentSchemaModelObj (
        @SerialName("@id")
        val id: String?=null,
        @SerialName("@type")
        @Serializable(with = StringToArrayTypeSerializer::class)
        val type: List<String?>?=null,
        @SerialName("displayName")
        val displayName: String?=null,
        @SerialName("name")
        val name: String,
        @SerialName("comment")
        val comment: String?=null,
        @SerialName("description")
        val description: String?=null,
        @SerialName("unit")
        val unit: String?=null,
        @SerialName("writable")
        val writable:Boolean?=null,
        @SerialName("schema")
        val schema:SchemaModel?=null
    ): CloudContentSchemaModel()

    @Serializable
    @SerialName("schema_string")
    data class CloudContentSchemaModelString (
        @SerialName("@id")
        val id: String?=null,
        @SerialName("@type")
        @Serializable(with = StringToArrayTypeSerializer::class)
        val type: List<String?>?=null,
        @SerialName("displayName")
        val displayName: String?=null,
        @SerialName("name")
        val name: String,
        @SerialName("comment")
        val comment: String?=null,
        @SerialName("description")
        val description: String?=null,
        @SerialName("unit")
        val unit: String?=null,
        @SerialName("writable")
        val writable:Boolean?=null,
        @SerialName("schema")
        val schema:String?=null
    ): CloudContentSchemaModel()
}

@Serializable
data class SchemaModel(
    @SerialName("@type")
    val type: String?=null,
    @SerialName("description")
    val description: String?=null,
    @SerialName("displayName")
    val displayName: String?=null,
    @SerialName("fields")
    val fields: List<FieldModel?>?=null
)

@Serializable
data class FieldModel(
    @SerialName("displayName")
    val displayName: String?=null,
    @SerialName("name")
    val name: String?=null,
    @SerialName("schema")
    //val schema: SchemaModel?
    val schema: String?
)