package com.st.trilobyte.models

import java.io.Serializable

const val FFT_FUNCTION_ID = "F2"

const val SENSOR_FUSION_QUATERNION_FUNCTION_ID = "F3"

const val SENSOR_FUSION_EULER_FUNCTION_ID = "F4"

const val SENSOR_FUSION_PEDOMETER_FUNCTION_ID = "F5"

const val HARD_IRON_COMP_FUNCTION_ID = "F6"

internal data class Function(val id: String,
                    var board_compatibility : List<String>,
                    val description: String,
                    val inputs: List<String> = listOf(),
                    val mandatoryInputs: List<List<String>> = listOf(),
                    val outputs: List<String> = listOf(),
                    val parametersCount: Int,
                    val maxRepeatCount: Int? = null,
                    var properties: List<Property<*>>) : Serializable {

    val isFFT: Boolean
        get() = when (id) {
            FFT_FUNCTION_ID -> true
            else -> false
        }
    val isFFTCompare: Boolean
        get() = when (id) {
            "F25" -> true
            "F26" -> true
            else -> false
        }

    val isThresholdCompare:Boolean
        get() = when(id){
            "L1" -> true
            else -> false
        }

    val hasSettings: Boolean
        get() = properties.isNotEmpty()
}