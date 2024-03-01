package com.st.cloud_azure_iot_central.composable

import androidx.compose.runtime.Composable

object CloudAzureConfig {
    var CloudTabBar: (@Composable (title: String) -> Unit)? = null
}