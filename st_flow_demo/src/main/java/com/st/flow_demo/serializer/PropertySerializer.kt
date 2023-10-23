package com.st.flow_demo.serializer

import com.st.flow_demo.models.Property
import com.st.flow_demo.models.PropertyEnumValue
import com.st.flow_demo.models.PropertyType
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE

class PropertySerializer : KSerializer<Property> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Property") {
        element("label", serialDescriptor<String>())
        element("type", serialDescriptor<String>())
        element("value", buildClassSerialDescriptor("Any"))
        element("enumValues", buildClassSerialDescriptor("PropertyEnumValue"))
    }

    @ExperimentalSerializationApi
    override fun deserialize(decoder: Decoder): Property {
        val composite = decoder.beginStructure(descriptor)
        var label: String? = null
        var typeString: String? = null
        var value: Any? = null
        var enumValues: List<PropertyEnumValue>? = null
        while (true) {
            when (val index = composite.decodeElementIndex(descriptor)) {
                0 -> label = composite.decodeStringElement(descriptor, 0)
                1 -> typeString = composite.decodeStringElement(descriptor, 1)
                2 -> {
                    value =
                        when (typeString?.let { PropertyType.valueOf(it) }) {
                            PropertyType.FLOAT -> composite.decodeFloatElement(descriptor, 2)
                            PropertyType.INT -> composite.decodeIntElement(descriptor, 2)
                            PropertyType.ENUM -> composite.decodeIntElement(descriptor, 2)
                            //PropertyType.ENUM -> composite.decodeSerializableElement(descriptor,2,serializer<PropertyType>())
                            PropertyType.STRING -> composite.decodeStringElement(descriptor, 2)
                            PropertyType.BOOL -> composite.decodeBooleanElement(descriptor, 2)

                            else -> {
                                error("Unexpected typeString: $typeString")
                            }
                        }
                }

                3 -> enumValues = composite.decodeSerializableElement(
                    descriptor,
                    3,
                    serializer<List<PropertyEnumValue>?>()
                )

                DECODE_DONE -> break // Input is over
                else -> error("Unexpected index: $index")
            }
        }
        composite.endStructure(descriptor)
        require(label != null && typeString != null && value != null)
        return Property(
            label = label,
            type = PropertyType.valueOf(typeString),
            value = value,
            enumValues = enumValues
        )
    }

    override fun serialize(encoder: Encoder, value: Property) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.label)
            encodeStringElement(descriptor, 1, value.type.name)
            when (value.type) {
                PropertyType.FLOAT -> encodeFloatElement(descriptor, 2, value.value as Float)
                PropertyType.INT -> encodeIntElement(descriptor, 2, value.value as Int)
                PropertyType.ENUM -> encodeIntElement(descriptor, 2, value.value as Int)
                PropertyType.STRING -> encodeStringElement(descriptor, 2, value.value as String)
                PropertyType.BOOL -> encodeBooleanElement(descriptor, 2, value.value as Boolean)
            }
            value.enumValues?.let {
                encodeSerializableElement(descriptor, 3, serializer(), value.enumValues)
            }
        }
    }
}