package com.st.flow_demo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Function(
    @SerialName(value = "id")
    val id: String,
    @SerialName(value = "board_compatibility")
    var board_compatibility: ArrayList<String> = ArrayList(),
    @SerialName(value = "description")
    val description: String,
    @SerialName(value = "inputs")
    val inputs: List<String> = listOf(),
    @SerialName(value = "mandatoryInputs")
    val mandatoryInputs: List<List<String>> = listOf(),
    @SerialName(value = "outputs")
    val outputs: List<String> = listOf(),
    @SerialName(value = "parametersCount")
    val parametersCount: Int,
    @SerialName(value = "maxRepeatCount")
    val maxRepeatCount: Int? = null,
    @SerialName(value = "properties")
    var properties: List<Property>
) {
    companion object {
        const val FFT_FUNCTION_ID = "F2"

        const val SENSOR_FUSION_QUATERNION_FUNCTION_ID = "F3"

        const val SENSOR_FUSION_EULER_FUNCTION_ID = "F4"

        const val SENSOR_FUSION_PEDOMETER_FUNCTION_ID = "F5"

        const val HARD_IRON_COMP_FUNCTION_ID = "F6"
    }

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
