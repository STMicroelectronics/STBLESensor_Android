package com.st.cloud_azure_iot_central.model.serializer

import com.st.cloud_azure_iot_central.model.CloudContentCapabilityModel
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object CloudContentCapabilityModelSerializer : JsonContentPolymorphicSerializer<CloudContentCapabilityModel>(
    CloudContentCapabilityModel::class
) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<CloudContentCapabilityModel> {

        val jsonObject = element.jsonObject

        return if (jsonObject["schema"] is JsonObject) {
            CloudContentCapabilityModel.CloudContentCapabilityModelObj.serializer()
        } else {
            CloudContentCapabilityModel.CloudContentCapabilityModelString.serializer()
        }
    }
}