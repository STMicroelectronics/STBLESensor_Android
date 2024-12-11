package com.st.high_speed_data_log

import androidx.compose.runtime.Composable

object HsdlConfig {
    var nodeId: String? = null

    var isVespucci: Boolean = false

    var tags: List<String> = emptyList()

    var hsdlTabBar: (@Composable (title: String, isLoading: Boolean, stopLoggingEnabled: Boolean, stopLogging: ()->Unit ) -> Unit)? = null

    var showStopDialog: Boolean = false

    var showResetDialog: Boolean = false

    var isLogging: Boolean = false

    var datalogNameFormat: String? = null
}
