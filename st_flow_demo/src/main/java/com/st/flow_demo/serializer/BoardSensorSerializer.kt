package com.st.flow_demo.serializer

import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.flow_demo.uploader.BoardFilterConfig
import com.st.flow_demo.uploader.BoardRegConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

class BoardSensorSerializer : KSerializer<Sensor> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Sensor")
    {
        element("id", serialDescriptor<String>())
        element("powerMode", serialDescriptor<Int>().nullable)
        element("odr", serialDescriptor<Double>().nullable)
        element("fullScale", serialDescriptor<Int>().nullable)
        element("acquisitionTime", serialDescriptor<Double>().nullable)
        element("configuration", buildClassSerialDescriptor("BoardRegConfig"))
        element("filter", buildClassSerialDescriptor( "BoardFilterConfig"))
    }

    override fun deserialize(decoder: Decoder): Sensor {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Sensor) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id)
            if(value.hasSettings()) {
                value.configuration!!.powerMode?.let {
                    encodeIntElement(descriptor, 1, value.configuration!!.powerMode!!.id)
                    if((value.configuration!!.oneShotTime!=null) || (value.configuration!!.odr!=null)) {
                        if(value.configuration!!.oneShotTime!=null) {
                            encodeDoubleElement(descriptor,2,value.configuration!!.oneShotTime!!)
                        } else {
                            encodeDoubleElement(descriptor,2,value.configuration!!.odr!!)
                        }
                    }
                }
                if(value.configuration!!.fullScale!=null) {
                    encodeIntElement(descriptor,3,value.configuration!!.fullScale!!)
                }
                if(value.configuration!!.acquisitionTime!=null) {
                    encodeDoubleElement(descriptor,4,value.configuration!!.acquisitionTime!!)
                }

                if(value.configuration!!.regConfig!=null) {
                    val config = BoardRegConfig(
                        regConfig = value.configuration!!.regConfig!!,
                        mlcLabels = value.configuration!!.mlcLabels,
                        fsmLabels = value.configuration!!.fsmLabels
                    )
                    encodeSerializableElement(descriptor,5, serializer(),config)
                }

                if(value.configuration!!.filters!=null) {
                    val filter = BoardFilterConfig(
                        lowPass = value.configuration!!.filters!!.lowPass,
                        highPass = value.configuration!!.filters!!.highPass
                    )
                    encodeSerializableElement(descriptor,6, serializer(),filter)
                }
            }
        }
    }
}
