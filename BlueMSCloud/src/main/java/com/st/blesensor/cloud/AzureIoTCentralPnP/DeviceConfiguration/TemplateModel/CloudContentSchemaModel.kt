package com.st.blesensor.cloud.AzureIoTCentralPnP.DeviceConfiguration.TemplateModel

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class CloudContentSchemaModel (
    @SerializedName("@id")
    val id: String,
    //This could be a String or a list of String...
    // example Battery -> Telemetry or Temperature -> [Telemetry Temperature]
    @JsonAdapter(CustomAdapterString::class)
    @SerializedName("@type")
    val type: List<String?>?,
    @SerializedName("displayName")
    val displayName: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("comment")
    val comment: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("writable")
    val writable:Boolean?,
    //This could be a String (double for example) or one object for example:
//    "schema": {
//        "@type": "Object",
//        "description": "Single acceleration sample. I.e.: {a_x, a_y, a_z}.",
//        "displayName": "Single acceleration sample.",
//        "fields": [
//        {
//            "name": "a_x",
//            "schema": "double"
//        },
//        {
//            "name": "a_y",
//            "schema": "double"
//        },
//        {
//            "name": "a_z",
//            "schema": "double"
//        }
//        ]
//    }
    @JsonAdapter(CustomAdapterSchemaModel::class)
    @SerializedName("schema")
    val schema: SchemaModel?
)

data class SchemaModel(
    @SerializedName("@type")
    val type: String?,
    @SerializedName("description")
    val description: String?=null,
    @SerializedName("displayName")
    val displayName: String?=null,
    @SerializedName("fields")
    val fields: List<FieldModel?>?=null,

)

data class FieldModel(
    @SerializedName("displayName")
    val displayName: String?=null,
    @SerializedName("name")
    val name: String?=null,
    //This could be a String (double for example) or one enumerative for example:
//    "schema": {
//        "@id": "dtmi:proteusPoc:fpaipdmwbsoc:neai_ad:schema:phase:schema;1",
//        "@type": "Enum",
//        "displayName": {
//            "en": "Enum"
//        },
//        "enumValues": [
//        {
//            "@id": "dtmi:proteusPoc:fpaipdmwbsoc:neai_ad:schema:phase:schema:idle;1",
//            "displayName": {
//            "en": "IDLE"
//        },
//            "enumValue": 0,
//            "name": "idle"
//        },
//        {
//            "@id": "dtmi:proteusPoc:fpaipdmwbsoc:neai_ad:schema:phase:schema:learning;1",
//            "displayName": {
//            "en": "LEARNING"
//        },
//            "enumValue": 1,
//            "name": "learning"
//        }
//        ],
//        "valueSchema": "integer"
//    }
    @JsonAdapter(CustomAdapterSchemaModel::class)
    @SerializedName("schema")
    val schema: SchemaModel?
)

internal class CustomAdapterString : JsonDeserializer<List<String>>,JsonSerializer<List<String>> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<String>? {
        var result : List<String>?=null

        if (json!!.isJsonArray) {
            if (context != null) {
                result = context.deserialize(json, typeOfT)
            }
        } else {
            result = ArrayList()
            if (context != null) {
                result.add(context.deserialize(json, String::class.java) as String)
            }
        }
        return result
    }

    override fun serialize(
        src: List<String>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement? {
        if (src != null) {
            if (src.size == 1) {
                if (context != null) {
                    return context.serialize(src[0])
                }
            } else {
                if (context != null) {
                    return context.serialize(src)
                }
            }
        }
        return null
     }
}

internal class CustomAdapterSchemaModel : JsonDeserializer<SchemaModel>,JsonSerializer<SchemaModel> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): SchemaModel? {
        var result:SchemaModel?=null
        if (json!!.isJsonObject) {
            if (context != null) {
                result = context.deserialize(json, SchemaModel::class.java)
            }
        } else {
            result = SchemaModel(type=json.asString)
        }
        return result
    }

    override fun serialize(
        src: SchemaModel?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement?{
        if (src != null) {
            if (src.type == "Object") {
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
