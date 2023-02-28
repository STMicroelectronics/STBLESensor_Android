package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class OptionByteEnumType(
    @SerializedName("type")
    val type: String,
    @SerializedName("display_name")
    val display_name: String,
    @SerializedName("comment")
    val comment: String,
    @SerializedName("value")
    val value: Int,
    @SerializedName("icon_code")
    val icon_code: Int
)
