package com.st.cloud_azure_iot_central.model.serializer

import com.st.cloud_azure_iot_central.model.CloudContentSchemaModel
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object CloudContentSchemaModelSerializer : JsonContentPolymorphicSerializer<CloudContentSchemaModel>(
    CloudContentSchemaModel::class,
) {
    override fun selectDeserializer(
        element: JsonElement,
    ): DeserializationStrategy<CloudContentSchemaModel> {

        val jsonObject = element.jsonObject

        return if (jsonObject["schema"] is JsonObject) {
            CloudContentSchemaModel.CloudContentSchemaModelObj.serializer()
        } else {
            CloudContentSchemaModel.CloudContentSchemaModelString.serializer()
        }
    }
}