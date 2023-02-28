package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class Sensor (
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("sensorDescriptor") val sensorDescriptor: SensorDescriptor,
    @SerializedName("sensorStatus")val sensorStatus: SensorStatus
) : Comparable<Sensor>
{
    override fun compareTo(other: Sensor): Int = id - other.id

    fun getSubSensorStatusForId(id:Int):SubSensorStatus?{
        val index = sensorDescriptor.subSensorDescriptors.indexOfFirst { it.id == id }
        return if(index>=0){
            sensorStatus.subSensorStatusList[index]
        }else{
            null
        }
    }
}
