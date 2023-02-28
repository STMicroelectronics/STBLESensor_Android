package com.st.BlueSTSDK.Features.ExtConfiguration

import com.google.gson.annotations.SerializedName
data class CustomCommand(
        @SerializedName("Name")
        val name: String,
        @SerializedName("Type")
        val type: String,
        @SerializedName("DefaultValue")
        val default_value: Int?=null,
        @SerializedName("Min")
        val min: Int = 0,
        @SerializedName("Max")
        val max: Int = 1,
        @SerializedName("Desc")
        val description: String?,
        @SerializedName("StringValues")
        val stringValues: List<String>,
        @SerializedName("IntegerValues")
        val integerValues: List<Int>

)