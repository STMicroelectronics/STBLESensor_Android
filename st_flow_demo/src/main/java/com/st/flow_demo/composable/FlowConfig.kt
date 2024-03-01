package com.st.flow_demo.composable

import androidx.compose.runtime.Composable

object FlowConfig {
    var FlowTabBar: (@Composable (title: String) -> Unit)? = null
}