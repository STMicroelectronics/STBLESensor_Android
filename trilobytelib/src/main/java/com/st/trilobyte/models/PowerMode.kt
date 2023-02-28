package com.st.trilobyte.models

import java.io.Serializable

@Suppress("SpellCheckingInspection")
data class PowerMode(
        var minCustomOdr: Double?,
        var mode: Mode = Mode.NONE,
        var label: String = "",
        var odrs: List<Double> = listOf()) : Serializable {

    enum class Mode(val id: Int) {
        NONE(0),
        LOW_NOISE(1),
        LOW_CURRENT(2),
        LOW_POWER(3),
        LOW_POWER_2(4),
        LOW_POWER_3(5),
        LOW_POWER_4(6),
        HIGH_PERFORMANCE(7),
        HIGH_RESOLUTION(8)
    }
}
