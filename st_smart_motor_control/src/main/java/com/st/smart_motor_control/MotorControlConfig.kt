package com.st.smart_motor_control

import androidx.compose.runtime.Composable

object  MotorControlConfig {
    var nodeId: String? = null

    var tags: List<String> = emptyList()

    var motorControlTabBar: (@Composable (title: String, isLoading: Boolean) -> Unit)? = null

    var showStopDialog: Boolean = false

    var isLogging: Boolean = false

    var datalogNameFormat: String? = null
}