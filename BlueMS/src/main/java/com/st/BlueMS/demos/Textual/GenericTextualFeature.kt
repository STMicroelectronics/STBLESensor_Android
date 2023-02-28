package com.st.BlueMS.demos.Textual

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.fwDataBase.db.BleCharacteristic

data class GenericTextualFeature(
    val name: String,
    val description: String,
    val feature: Feature,
    var bleCharDesc: BleCharacteristic? = null
)
