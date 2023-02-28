package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class SubSensorDescriptor(
    @SerializedName("id") val id : Int,
    @SerializedName("sensorType") val sensorType : SensorType,
    @SerializedName("dimensions") val dimensions : Int,
    @SerializedName("dimensionsLabel") val dimensionsLabel : List<String>,
    @SerializedName("unit") val unit : String?,
    @SerializedName("dataType") val dataType : String?,
    @SerializedName("FS") val fs : List<Double>?,
    @SerializedName("ODR") val odr : List<Double>?,
    @SerializedName("samplesPerTs") val samplesPerTs : SamplesPerTs
)

data class SamplesPerTs (
    @SerializedName("min") val min : Int,
    @SerializedName("max") val max : Int,
    @SerializedName("dataType") val dataType : String
)