package com.st.BlueSTSDK.Features.ExtConfiguration

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

//Data class for the Configuration Command
data class ExtConfigCommands (
        @SerializedName("command")
        val command: String,
        @SerializedName("argString")
        val argString: String?=null,
        @SerializedName("argNumber")
        val argNumber: Int?=null,
        @SerializedName("argJsonElement")
        val argJsonElement: JsonElement?=null
)