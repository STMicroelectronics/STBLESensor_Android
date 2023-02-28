package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class SensorDescriptor (
    //NOTE here there may be parameters in the future
    @SerializedName("subSensorDescriptor") var subSensorDescriptors: List<SubSensorDescriptor>
)