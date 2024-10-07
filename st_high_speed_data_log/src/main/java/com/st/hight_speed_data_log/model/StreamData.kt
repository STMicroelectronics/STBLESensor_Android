package com.st.hight_speed_data_log.model

data class StreamData(
    val streamId: Int,
    val odr: Int = 1,
    val name: String = "",
    val uom: String = "",
    val max:Double?= null,
    val min: Double?=null,
    val data: List<StreamDataChannel> = emptyList()
){
    val shortUom = when(uom.lowercase()){
        "celsius" -> "°C"
        "gauss" -> "G"
        else -> uom
    }
}

data class StreamDataChannel(
    val data: List<Float> = emptyList()
)
