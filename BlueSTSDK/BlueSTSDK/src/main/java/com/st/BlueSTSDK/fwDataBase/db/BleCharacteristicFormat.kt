package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.Features.Field

data class BleCharacteristicFormat(
    @SerializedName("length")
    val length: Int,
    @SerializedName("optional")
    val optional: Boolean=false,
    @SerializedName("name")
    val name: String,
    @SerializedName("unit")
    val unit: String?,
    @SerializedName("type")
    val type: Field.Type?,
    @SerializedName("offset")
    val offset: Float,
    @SerializedName("scalefactor")
    val scalefactor: Float,
    @SerializedName("min")
    val min: Float?,
    @SerializedName("max")
    val max: Float?
)
