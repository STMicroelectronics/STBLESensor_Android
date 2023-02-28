package com.st.trilobyte.models.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.st.trilobyte.models.*
import java.lang.reflect.Type
import java.util.*

object PropertyDeserializer : JsonDeserializer<List<Property<*>>> {
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): List<Property<*>> {
        val properties = ArrayList<Property<*>>()

        val array = json.asJsonArray

        for (i in 0 until array.size()) {
            val obj = array.get(i).asJsonObject
            val type = Property.PropertyType.valueOf(obj.get("type").asString)

            when (type) {
                Property.PropertyType.ENUM -> properties.add(context?.deserialize<Any>(obj, EnumProperty::class.java) as EnumProperty)
                Property.PropertyType.FLOAT, Property.PropertyType.INT -> properties.add(context?.deserialize<Any>(obj, NumberProperty::class.java) as NumberProperty)
                Property.PropertyType.STRING -> properties.add(context?.deserialize<Any>(obj, StringProperty::class.java) as StringProperty)
                Property.PropertyType.BOOL -> properties.add(context?.deserialize<Any>(obj, BoolProperty::class.java) as BoolProperty)
            }
        }

        return properties
    }
}