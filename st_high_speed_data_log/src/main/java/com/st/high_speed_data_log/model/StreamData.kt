package com.st.high_speed_data_log.model

data class StreamData(
    val streamId: Int,
    val odr: Int = 1,
    val name: String = "",
    val uom: String = "",
    val max:Double?= null,
    val min: Double?=null,
    val data: List<StreamDataChannel> = emptyList()
)

data class StreamDataChannel(
    val data: List<Float> = emptyList()
)
