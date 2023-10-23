package com.st.flow_demo.models

import com.st.blue_sdk.board_catalog.models.PowerMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilterHolder(
    @SerialName(value = "id")
    val id: Int,
    @SerialName(value = "powerModes")
    val powerModes: List<PowerMode.Mode> = listOf(),
    @SerialName(value = "filters")
    val filters: List<Filter> = listOf()
)
