package com.st.trilobyte.models.board.serializer

import com.google.gson.*
import com.st.trilobyte.models.*
import java.lang.reflect.Type

object BoardFlowSerializer : JsonSerializer<Flow> {
    override fun serialize(src: Flow, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {

        val obj = JsonObject()

        obj.addProperty("id", src.id)

        val sensorArray = JsonArray()
        for ((id) in src.sensors) {
            sensorArray.add(id)
        }
        obj.add("sensors", sensorArray)

        val flowArray = JsonArray()
        for (flow in src.flows) {
            flowArray.add(flow.id)
        }
        obj.add("flows", flowArray)

        val functionArray = JsonArray()
        if (src.functions != null) {
            for (function in src.functions) {

                val functionObj = JsonObject()
                functionObj.addProperty("id", function.id)

                val propertiesArray = JsonArray()
                if (function.hasSettings) {
                    addPropertiesToJsonObject(function.properties, propertiesArray)
                }

                functionObj.add("values", propertiesArray)
                functionArray.add(functionObj)
            }
        }

        obj.add("functions", functionArray)

        val outputArray = JsonArray()
        if (src.outputs != null) {
            for (output in src.outputs) {

                val outputObj = JsonObject()
                outputObj.addProperty("id", output.id)

                val propertiesArray = JsonArray()
                if (output.hasSettings) {
                    addPropertiesToJsonObject(output.properties!!, propertiesArray)
                }

                outputObj.add("values", propertiesArray)
                outputArray.add(outputObj)
            }
        }

        obj.add("outputs", outputArray)
        return obj
    }

    private fun addPropertiesToJsonObject(properties: List<Property<*>>, propertiesArray: JsonArray) {
        for (functionProperty in properties) {

            val obj = JsonObject()
            obj.addProperty("label", functionProperty.label)

            when (functionProperty.type) {
                Property.PropertyType.FLOAT, Property.PropertyType.INT -> {
                    val numberProperty = functionProperty as NumberProperty
                    obj.addProperty("value", numberProperty.value)
                }
                Property.PropertyType.ENUM -> {
                    val enumProperty = functionProperty as EnumProperty
                    obj.addProperty("value", enumProperty.value)
                }
                Property.PropertyType.STRING -> {
                    val stringProperty = functionProperty as StringProperty
                    obj.addProperty("value", stringProperty.value)
                }
                Property.PropertyType.BOOL -> {
                    val boolProperty = functionProperty as BoolProperty
                    obj.addProperty("value", boolProperty.value)
                }
            }

            propertiesArray.add(obj)
        }
    }
}