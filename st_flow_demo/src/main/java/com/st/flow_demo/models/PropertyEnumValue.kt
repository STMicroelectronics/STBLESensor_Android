package com.st.flow_demo.models
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PropertyEnumValue(
    @SerialName(value = "label")
    var label: String,
    @SerialName(value = "value")
    var value: Int
)