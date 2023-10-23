package com.st.flow_demo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Output(
    @SerialName(value = "id")
    var id: String = "",
    @SerialName(value = "board_compatibility")
    var board_compatibility: List<String>?=null,
    @SerialName(value = "description")
    var description: String = "",
    @SerialName(value = "icon")
    var icon: String = "",
    @SerialName(value = "properties")
    var properties: List<Property>? = null
) {

    companion object {
        const val OUTPUT_STREAM_TO_SD = "O1"
        const val OUTPUT_STREAM_TO_BT = "O3"
        const val OUTPUT_AS_INPUT_ID = "O4"
        const val OUTPUT_EXP_ID = "O5"
    }

    val hasSettings: Boolean
        get() = !properties.isNullOrEmpty()

    val canUpload: Boolean
        get() = when (id) {
            "O1" -> true
            "O2" -> true
            "O3" -> true
            "O4" -> false
            "O5" -> false
            else -> false
        }

    val isLogic: Boolean
        get() = when (id) {
            "O1" -> false
            "O2" -> false
            "O3" -> false
            "O4" -> true
            "O5" -> true
            else -> true
        }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Output

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
