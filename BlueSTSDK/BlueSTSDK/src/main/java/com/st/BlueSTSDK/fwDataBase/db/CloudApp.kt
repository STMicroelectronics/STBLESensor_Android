package com.st.BlueSTSDK.fwDataBase.db

import com.google.gson.annotations.SerializedName

data class CloudApp(
    @SerializedName("description")
    val description: String?=null,
    @SerializedName("dtmi")
    val dtmi: String?,
    @SerializedName("name")
    var name: String,
    @SerializedName("shareable_link")
    var shareable_link: String?,
    @SerializedName("url")
    var url: String
)