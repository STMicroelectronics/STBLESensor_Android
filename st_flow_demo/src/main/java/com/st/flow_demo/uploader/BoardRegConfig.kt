package com.st.flow_demo.uploader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardRegConfig(
    @SerialName(value = "regConfig")
    var regConfig: String,
    @SerialName(value = "mlcLabels")
    var mlcLabels: String?=null,
    @SerialName(value = "fsmLabels")
    var fsmLabels: String?=null,
)