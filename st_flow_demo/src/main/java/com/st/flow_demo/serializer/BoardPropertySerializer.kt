package com.st.flow_demo.serializer

import com.st.flow_demo.models.PropertyType
import com.st.flow_demo.uploader.BoardPropertyElement
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

class BoardPropertySerializer : KSerializer<BoardPropertyElement> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BoardProperty") {
        element("label", serialDescriptor<String>())
        element("value", buildClassSerialDescriptor("Any"))
    }


    override fun deserialize(decoder: Decoder): BoardPropertyElement {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: BoardPropertyElement) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.label)
            when (value.type!!) {
                PropertyType.FLOAT -> encodeFloatElement(descriptor, 1, value.value as Float)
                PropertyType.INT -> encodeIntElement(descriptor, 1, value.value as Int)
                PropertyType.ENUM -> encodeIntElement(descriptor, 1, value.value as Int)
                PropertyType.STRING -> encodeStringElement(descriptor, 1, value.value as String)
                PropertyType.BOOL -> encodeBooleanElement(descriptor, 1, value.value as Boolean)
            }
        }
    }
}