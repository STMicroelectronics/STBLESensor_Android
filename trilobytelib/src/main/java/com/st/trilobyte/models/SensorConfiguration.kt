package com.st.trilobyte.models

import java.io.Serializable

data class SensorConfiguration(
        var odr: Double? = null,
        var oneShotTime: Double? = null,
        var powerMode: PowerMode.Mode? = null,
        var acquisitionTime: Double? = null,
        var regConfig: String? = null,
        var ucfFilename: String? = null,
        var mlcLabels: String? = null,
        var fsmLabels: String? = null,
        var filters: FilterConfiguration = FilterConfiguration(),
        var fullScale: Int? = null) : Serializable


var SensorConfiguration.acquisitionTimeMin:Double?
        get() { return this.acquisitionTime?.div(60) }
        set(value) { this.acquisitionTime = value?.times(60) }