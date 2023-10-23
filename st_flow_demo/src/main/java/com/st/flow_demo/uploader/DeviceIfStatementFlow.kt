package com.st.flow_demo.uploader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceIfStatementFlow(
    @SerialName(value = "expression")
    var expression: DeviceFlow,
    @SerialName(value = "statements")
    var statements: List<DeviceFlow>
)