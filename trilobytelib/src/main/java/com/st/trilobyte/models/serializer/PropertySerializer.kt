package com.st.trilobyte.models.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.st.trilobyte.models.*
import java.lang.reflect.Type

object PropertySerializer : JsonSerializer<Property<*>> {

    override fun serialize(src: Property<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        return when (src.type) {

            Property.PropertyType.ENUM -> context.serialize(src, EnumProperty::class.java)

            Property.PropertyType.FLOAT, Property.PropertyType.INT -> context.serialize(src, NumberProperty::class.java)

            Property.PropertyType.STRING -> context.serialize(src, StringProperty::class.java)

            Property.PropertyType.BOOL -> context.serialize(src, BoolProperty::class.java)

            else -> throw RuntimeException("Define property in deserializer")
        }
    }
}