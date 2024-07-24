package com.st.cloud_azure_iot_central.model.serializer

import com.st.cloud_azure_iot_central.model.FieldModel
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

object FieldModelSerializer : JsonContentPolymorphicSerializer<FieldModel>(
    FieldModel::class
) {
    override fun selectDeserializer(
        element: JsonElement
    ): DeserializationStrategy<FieldModel> {

        val jsonObject = element.jsonObject

        return if (jsonObject["schema"] is JsonObject) {
            FieldModel.FieldModelObj.serializer()
        } else {
            FieldModel.FieldModelString.serializer()
        }
    }
}