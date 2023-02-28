package com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class CloudContentCapabilityModel (
    @SerializedName("@id")
    val id: String,
    @SerializedName("@type")
    val type: Any,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("name")
    val name: String,
    @JsonAdapter(CustomAdapterCloudSchemaModel::class)
    @SerializedName("schema")
    val schema: CloudSchemaModel? = null
)

internal class CustomAdapterCloudSchemaModel : JsonDeserializer<CloudSchemaModel>,
    JsonSerializer<CloudSchemaModel> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CloudSchemaModel? {
        var result:CloudSchemaModel?=null
        if (json!!.isJsonObject) {
            if (context != null) {
                result = context.deserialize(json, CloudSchemaModel::class.java)
            }
        } else {
            result = CloudSchemaModel(type=json.asString)
        }
        return result
    }

    override fun serialize(
        src: CloudSchemaModel?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement?{
        if (src != null) {
            if (src.type == "Object" || src.type == "Interface" || src.type == "Property") {
                if (context != null) {
                    return context.serialize(src)
                }
            } else {
                if (context != null) {
                    return context.serialize(src.type)
                }
            }
        }
        return null
    }

}