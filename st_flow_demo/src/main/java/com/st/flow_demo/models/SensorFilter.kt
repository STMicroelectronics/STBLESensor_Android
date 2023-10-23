package com.st.flow_demo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorFilter(
    @SerialName(value = "sensorId")
    var sensorId: String = "",
    @SerialName(value = "board_compatibility")
    var board_compatibility: ArrayList<String> = ArrayList(),
    @SerialName(value = "values")
    var values: List<FilterHolder> = ArrayList()
)
