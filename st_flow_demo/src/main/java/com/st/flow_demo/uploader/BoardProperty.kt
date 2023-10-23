package com.st.flow_demo.uploader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardProperty(
    @SerialName(value = "id")
    var id: String,
    @SerialName(value = "values")
    var values: List<BoardPropertyElement>
)