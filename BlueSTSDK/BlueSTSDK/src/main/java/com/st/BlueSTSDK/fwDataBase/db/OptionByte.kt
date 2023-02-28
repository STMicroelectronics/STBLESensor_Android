package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class OptionByte(
    @SerializedName("format")
    val format: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("negative_offset")
    val negative_offset: Int,
    @SerializedName("scale_factor")
    val scale_factor: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("string_values")
    val string_values: List<OptionByteEnumType>,
    @SerializedName("icon_values")
    val icon_values: List<OptionByteEnumType>,
    @SerializedName("escape_value")
    val escape_value: Int?,
    @SerializedName("escape_message")
    val escape_message: String
)