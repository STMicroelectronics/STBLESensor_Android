package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class SensorStatus (
    //NOTE here there may be parameters in the future
    @SerializedName("subSensorStatus") var subSensorStatusList : List<SubSensorStatus>,
    @SerializedName("paramsLocked") var paramsLocked: Boolean = false
)
{
    fun getSubSensorStatus(subSensorId: Int): SubSensorStatus? {
        return subSensorStatusList.getOrNull(subSensorId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorStatus

        if (subSensorStatusList != other.subSensorStatusList) return false
        if (paramsLocked != other.paramsLocked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = subSensorStatusList.hashCode()
        result = 31 * result + paramsLocked.hashCode()
        return result
    }
}

