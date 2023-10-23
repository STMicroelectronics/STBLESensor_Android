package com.st.flow_demo.serializer

import com.st.flow_demo.models.Flow
import com.st.flow_demo.uploader.BoardProperty
import com.st.flow_demo.uploader.BoardPropertyElement
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.listSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure

class BoardFlowSerializer : KSerializer<Flow> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor= buildClassSerialDescriptor("Flow")
    {
        element("id", serialDescriptor<String>())
        element("sensors",listSerialDescriptor<String>())
        element("flows",listSerialDescriptor<String>())
        element("functions",listSerialDescriptor<BoardProperty>())
        element("outputs",listSerialDescriptor<BoardProperty>())
    }

    override fun deserialize(decoder: Decoder): Flow {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Flow) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id)

            val sensorIdsList = mutableListOf<String>()
            for (sensor in value.sensors) {
                sensorIdsList.add(sensor.id)
            }

            encodeSerializableElement(
                descriptor,
                1,
                ListSerializer(String.serializer()),
                sensorIdsList.toList()
            )


            val flowIdsList = mutableListOf<String>()
            for (flow in value.flows) {
                flowIdsList.add(flow.id)
            }

            encodeSerializableElement(
                descriptor,
                2,
                ListSerializer(String.serializer()),
                flowIdsList.toList()
            )


            if (value.functions.isNotEmpty()) {
                val functionsList = mutableListOf<BoardProperty>()
                for (function in value.functions) {
                    val boardProperty =
                        if (function.hasSettings) {
                            val valueList = mutableListOf<BoardPropertyElement>()
                            for (property in function.properties) {
                                valueList.add(
                                    BoardPropertyElement(
                                        label = property.label,
                                        value = property.value,
                                        type = property.type
                                    )
                                )
                            }
                            BoardProperty(id = function.id, values = valueList.toList())
                        } else {
                            BoardProperty(id = function.id, values = emptyList())
                        }
                    functionsList.add(boardProperty)
                }
                encodeSerializableElement(
                    descriptor,
                    3,
                    ListSerializer(BoardProperty.serializer()),
                    functionsList.toList()
                )
            }

            if (value.outputs.isNotEmpty()) {
                val outputsList = mutableListOf<BoardProperty>()
                for (output in value.outputs) {
                    val boardProperty =
                        if (output.hasSettings) {
                            val valueList = mutableListOf<BoardPropertyElement>()
                            for (property in output.properties!!) {
                                valueList.add(
                                    BoardPropertyElement(
                                        label = property.label,
                                        value = property.value,
                                        type = property.type
                                    )
                                )
                            }
                            BoardProperty(id = output.id, values = valueList.toList())
                        } else {
                            BoardProperty(id = output.id, values = emptyList())
                        }
                    outputsList.add(boardProperty)
                }
                encodeSerializableElement(
                    descriptor,
                    4,
                    ListSerializer(BoardProperty.serializer()),
                    outputsList.toList()
                )
            }
        }
    }
}

