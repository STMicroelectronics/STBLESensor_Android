package com.st.hight_speed_data_log

import androidx.compose.runtime.Composable

object HsdlConfig {
    var tags: List<String> = emptyList()

    var hsdlTabBar: (@Composable (title: String) -> Unit)? = null

    var showStopDialog: Boolean = false

    var isLogging: Boolean = false

}