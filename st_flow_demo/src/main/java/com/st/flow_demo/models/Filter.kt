package com.st.flow_demo.models

import com.st.blue_sdk.board_catalog.models.CutOff
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Filter(
    @SerialName(value = "odrs")
    val odrs: List<Double> = listOf(),
    @SerialName(value = "lowPass")
    val lowPass: List<CutOff> = listOf(),
    @SerialName(value = "highPass")
    val highPass: List<CutOff> = listOf()
)
