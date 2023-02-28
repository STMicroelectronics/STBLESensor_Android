package com.st.BlueSTSDK.Features.ExtConfiguration

import com.google.gson.annotations.SerializedName

data class BanksStatus (
    @SerializedName("currentBank")
    val currentBank: Int,
    @SerializedName("fwId1")
    val fwId1: String,
    @SerializedName("fwId2")
    val fwId2: String
)