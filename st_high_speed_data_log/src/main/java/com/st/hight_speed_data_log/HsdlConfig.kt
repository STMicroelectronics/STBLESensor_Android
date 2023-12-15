package com.st.hight_speed_data_log

import androidx.compose.runtime.Composable

object HsdlConfig {
    var nodeId: String? = null

    var tags: List<String> = emptyList()

    var hsdlTabBar: (@Composable (title: String, isLoading: Boolean) -> Unit)? = null

    var showStopDialog: Boolean = false

    var isLogging: Boolean = false

    var datalogNameFormat: String? = null
}
