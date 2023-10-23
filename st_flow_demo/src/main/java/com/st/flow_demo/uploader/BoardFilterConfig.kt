package com.st.flow_demo.uploader

import com.st.blue_sdk.board_catalog.models.CutOff
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardFilterConfig (
    @SerialName(value = "lowPass")
    val lowPass: CutOff?=null,
    @SerialName(value = "highPass")
    val highPass: CutOff?=null
)