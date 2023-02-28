package com.st.trilobyte.models

import java.io.Serializable

data class Output(
        var id: String = "",
        var board_compatibility : List<String>,
        var description: String = "",
        var icon: String = "",
        var properties: List<Property<*>>? = null) : Serializable {

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
