package com.st.textual_monitor

import com.st.blue_sdk.board_catalog.models.BleCharacteristic
import com.st.blue_sdk.features.Feature

data class GenericTextualFeature(
    val name: String,
    val description: String,
    val feature: Feature<*>,
    var bleCharDesc: BleCharacteristic? = null
)
