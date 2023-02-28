package com.st.trilobyte.models

import java.io.Serializable

data class SensorFilter(
        var sensorId: String = "",
        var board_compatibility : List<String>,
        var values: List<FilterHolder> = ArrayList()
) : Serializable