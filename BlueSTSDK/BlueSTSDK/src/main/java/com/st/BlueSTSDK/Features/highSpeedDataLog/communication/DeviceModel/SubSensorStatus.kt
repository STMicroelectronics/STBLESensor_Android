package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class SubSensorStatus(
        @SerializedName("isActive") var isActive : Boolean,
        @SerializedName("ODR") var odr : Double?,
        @SerializedName("ODRMeasured") val odrMeasured : Double?,
        @SerializedName("initialOffset") val initialOffset : Double?,
        @SerializedName("samplesPerTs") var samplesPerTs : Int,
        @SerializedName("FS") var fs : Double?,
        @SerializedName("sensitivity") val sensitivity : Double?,
        @SerializedName("usbDataPacketSize") val usbDataPacketSize : Int,
        @SerializedName("sdWriteBufferSize") val sdWriteBufferSize : Int,
        @SerializedName("wifiDataPacketSize") val wifiDataPacketSize : Int,
        @SerializedName("comChannelNumber") val comChannelNumber : Int,
        @SerializedName("ucfLoaded") var ucfLoaded : Boolean,
        //var paramsLocked: Boolean = false
)