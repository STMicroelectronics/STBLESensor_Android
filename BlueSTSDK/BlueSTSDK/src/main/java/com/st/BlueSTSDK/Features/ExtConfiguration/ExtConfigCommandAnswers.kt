package com.st.BlueSTSDK.Features.ExtConfiguration

import com.google.gson.annotations.SerializedName
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor

// data class for reading the Configuration Commands result
data class ExtConfigCommandAnswers (
        @SerializedName("Commands")
        val CommandList: String?,
        @SerializedName("Info")
        val info: String?,
        @SerializedName("Help")
        val help: String?,
        @SerializedName("Certificate")
        val certificate: String?,
        @SerializedName("VersionFw")
        val versionFw: String?,
        @SerializedName("UID")
        val stm32UID: String?,
        @SerializedName("PowerStatus")
        val powerStatus: String?,
        @SerializedName("CustomCommands")
        val CustomCommandList: List<CustomCommand>?,
        @SerializedName("sensor")
        val sensor: List<Sensor>?,
        @SerializedName("Error")
        val error: String?,
        @SerializedName("BankStatus")
        val banksStatus: BanksStatus?
)