package com.st.BlueSTSDK.Features.highSpeedDataLog.communication

import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorStatus

data class DeviceStatus (
    @SerializedName("type") val type: String?,
    @SerializedName("isLogging") val isSDLogging: Boolean?,
    @SerializedName("isSDInserted") val isSDCardInserted: Boolean?,
    @SerializedName("cpuUsage") val cpuUsage: Double?,
    @SerializedName("batteryVoltage") val batteryVoltage: Double?,
    @SerializedName("batteryLevel") val batteryLevel: Double?,
    @SerializedName("ssid") val ssid: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("ip") val ip: String?,
    @SerializedName("sensorId") val sensorId: Int?,
    @SerializedName("sensorStatus") val sensorStatus: SensorStatus?
)

class SensorStatusWId (
    val sensorId: Int?,
    val sensorStatus: SensorStatus?
)