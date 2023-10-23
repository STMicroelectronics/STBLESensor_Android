package com.st.flow_demo.uploader

import com.st.flow_demo.models.PropertyType
import com.st.flow_demo.serializer.BoardPropertySerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = BoardPropertySerializer::class)
data class BoardPropertyElement(
    @SerialName(value = "label")
    var label: String,
    @SerialName(value = "value")
    var value: Any,
    @kotlinx.serialization.Transient
    var type: PropertyType? = null
)