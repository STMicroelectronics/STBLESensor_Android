package com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel

import com.google.gson.annotations.SerializedName

data class Device (
    @SerializedName("deviceInfo") var deviceInfo: DeviceInfo?,
    @SerializedName("sensor") var sensors: List<Sensor>?,
    @SerializedName("tagConfig") var tags: TagConfig?
    ) {

    fun getTag(
        id: Int,
        isSW: Boolean
    ): Tag? {
        val tagsConf = tags ?: return null
        if (isSW) {
            for (tag in tagsConf.softwareTags) {
                if (tag.id == id) return tag
            }
        } else {
            for (tag in tagsConf.hardwareTags) {
                if (tag.id == id) return tag
            }
        }
        return null
    }

    fun updateTagLabel(tagId: Int, isSW: Boolean, label: String?) {
        if (tags != null) {
            if (isSW) {
                for (tag in tags!!.softwareTags) {
                    if (tag.id == tagId) {
                        tag.label = label!!
                    }
                }
            } else {
                for (tag in tags!!.hardwareTags) {
                    if (tag.id == tagId) {
                        tag.label = label!!
                    }
                }
            }
        }
    }

    fun enableTag(tagId: Int, isSW: Boolean, isEnabled: Boolean) {
        if (tags != null) {
            if (isSW) {
                for (tag in tags!!.softwareTags) {
                    if (tag.id == tagId) {
                        tag.isEnabled = isEnabled
                    }
                }
            } else {
                for (tag in tags!!.hardwareTags) {
                    if (tag.id == tagId) {
                        tag.isEnabled = isEnabled
                    }
                }
            }
        }
    }

    fun getSensorOfType(type:SensorType):SensorCoordinate?{
        sensors?.forEach { s ->
            val subSensor = s.sensorDescriptor.subSensorDescriptors.find { it.sensorType == type  }
            if(subSensor!=null){
                return SensorCoordinate(s.id,subSensor.id)
            }
        }
        return null
    }
}

data class SensorCoordinate(val sensorID:Int, val subSensorID:Int)