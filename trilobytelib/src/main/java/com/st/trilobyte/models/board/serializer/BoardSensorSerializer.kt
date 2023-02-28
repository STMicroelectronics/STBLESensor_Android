package com.st.trilobyte.models.board.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.st.trilobyte.models.CutOff
import com.st.trilobyte.models.Sensor
import java.lang.reflect.Type

object BoardSensorSerializer : JsonSerializer<Sensor> {
    override fun serialize(src: Sensor, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val obj = JsonObject()

        obj.addProperty("id", src.id)

        if (src.hasSettings()) {
            if (src.configuration.powerMode != null) {
                obj.addProperty("powerMode", src.configuration.powerMode!!.id)

                if (src.configuration.oneShotTime != null || src.configuration.odr != null) {
                    val odrValue = if (src.configuration.oneShotTime != null)
                        src.configuration.oneShotTime else src.configuration.odr
                    obj.addProperty("odr", odrValue)
                }
            }

            if (src.configuration.fullScale != null) {
                obj.addProperty("fullScale", src.configuration.fullScale)
            }
        }

        src.configuration.acquisitionTime?.let {
            obj.addProperty("acquisitionTime", it)
        }

        src.configuration.regConfig?.let {
            val configurationObject = JsonObject()
            configurationObject.addProperty("regConfig", src.configuration.regConfig)
            src.configuration.mlcLabels?.let {
                configurationObject.addProperty("mlcLabels", src.configuration.mlcLabels)
            }
            src.configuration.fsmLabels?.let {
                configurationObject.addProperty("fsmLabels", src.configuration.fsmLabels)
            }
            obj.add("configuration", configurationObject)
        }

        val filterObject =  JsonObject()
        src.configuration.filters.lowPass?.let {
            addFilter(filterObject, "lowPass", it)
        }

        src.configuration.filters.highPass?.let {
            addFilter(filterObject, "highPass", it)
        }

        filterObject.let {
            obj.add("filter", filterObject)
        }

        return obj
    }

    private fun addFilter(jsonObject: JsonObject, propertyName: String, cutoff: CutOff?) {

        val filterObj = JsonObject()
        cutoff?.let {
            filterObj.addProperty("label", it.label)
            filterObj.addProperty("value", it.value)
        } ?: run { filterObj.addProperty("value", 0) }

        jsonObject.add(propertyName, filterObj)
    }
}