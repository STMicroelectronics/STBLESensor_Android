package com.st.blesensor.cloud.stazure.communication

import com.st.blesensor.cloud.stazure.storage.RegisteredDevice

internal sealed class RegistrationResult{
    data class Success(val device:RegisteredDevice):RegistrationResult()
    object AccessForbidden:RegistrationResult()
}

internal interface DeviceManager {

    suspend fun registerDevice(deviceId:String,deviceName:String) : RegistrationResult
}