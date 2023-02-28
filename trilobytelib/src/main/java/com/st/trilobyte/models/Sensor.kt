package com.st.trilobyte.models

import java.io.Serializable

@Suppress("SpellCheckingInspection")
data class Sensor(
        var id: String = "",
        var description: String = "",
        var icon: String = "",
        var output: String = "",
        var outputs: List<String> = listOf(),
        var model: String = "",
        var notes: String = "",
        var dataType: String = "",
        var um: String = "",
        var fullScaleUm: String = "",
        var datasheetLink: String = "",
        var fullScales: List<Int>? = null,
        var powerModes: List<PowerMode>? = null,
        var acquisitionTime: Double? = null,
        var samplingFrequencies: List<Int>? = null,
        var bleMaxOdr: Double? = null,
        var board_compatibility : List<String>,
        var configuration: SensorConfiguration = SensorConfiguration()) : Serializable {

    override fun equals(other: Any?): Boolean {
        return other is Sensor && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun hasSettings(): Boolean {

        if (configuration.regConfig != null) {
            return true
        }

        if (fullScales != null && !fullScales!!.isEmpty()) {
            return true
        }

        return powerModes != null && !powerModes!!.isEmpty()
    }

    fun hasSameConfiguration(otherConfiguration: SensorConfiguration): Boolean {
        return configuration == otherConfiguration
    }
}
