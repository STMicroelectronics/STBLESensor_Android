package com.st.flow_demo.models

import com.st.flow_demo.serializer.PropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable (with =  PropertySerializer::class)
data class Property(
    @SerialName(value = "label")
    var label: String,
    @SerialName(value = "type")
    var type: PropertyType,
    @SerialName(value = "value")
    var value: Any,
    @SerialName(value = "enumValues")
    var enumValues: List<PropertyEnumValue>?=null) {
    override fun toString(): String {
        return "Property{" +
                "label='" + label + '\'' +
                ", type=" + type +
                ", value=" + value +
                '}'
    }
}

enum class PropertyType {
    FLOAT, INT, ENUM, STRING, BOOL
}

