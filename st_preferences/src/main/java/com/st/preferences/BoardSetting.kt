package com.st.preferences

import android.util.Log
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.lang.Exception

@Serializable
data class BoardSetting(
    @SerialName("nodeId")
    val nodeId: String,
    @SerialName("boardTypeName")
    val boardTypeName: String,
    @SerialName("customName")
    var customName: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardSetting

        return nodeId == other.nodeId
    }

    override fun hashCode(): Int {
        return nodeId.hashCode()
    }
}

fun String.toBoardSetting(): List<BoardSetting> =
    try {

        val jsonDec = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }

        jsonDec.decodeFromString<List<BoardSetting>>(this)

    } catch (e: Exception) {
        Log.d("StPreferencesImpl", e.stackTraceToString())
        emptyList()
    }
