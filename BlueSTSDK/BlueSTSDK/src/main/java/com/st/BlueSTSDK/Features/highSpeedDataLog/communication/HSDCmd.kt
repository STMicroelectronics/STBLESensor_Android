package com.st.BlueSTSDK.Features.highSpeedDataLog.communication

import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.DateFormat.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

abstract class HSDCmd(
        @SerializedName("command") var command: String,
        @SerializedName("start_time") var start_time: String?=null,
        @SerializedName("end_time") var end_time: String?=null
)

//NOTE HSD START/STOP commands
abstract class HSDControlCmd(command: String,start: String?=null,stop: String?=null) : HSDCmd(command = command,start_time= start,end_time= stop)

class HSDStartLoggingCmd(time: String) : HSDControlCmd(command = "START",start = time)

class HSDStopLoggingCmd(time: String) : HSDControlCmd(command = "STOP",stop = time)

class HSDSaveCmd : HSDControlCmd(command = "SAVE")

//NOTE HSD GET commands
abstract class HSDGetCmd(
        @SerializedName("request") var request: String
):HSDCmd(command = "GET")

class HSDGetDeviceCmd : HSDGetCmd(request = "device")

class HSDGetDeviceInfoCmd : HSDGetCmd(request = "deviceInfo")

class HSDGetDescriptorCmd(
        @SerializedName("sensorId") val address: Int
) : HSDGetCmd(request = "descriptor")

class HSDGetStatusCmd(
        @SerializedName("sensorId") val address: Int
) : HSDGetCmd(request = "status")

class HSDGetNetworkInfoCmd : HSDGetCmd(request = "network")

class HSDGetTagConfigCmd : HSDGetCmd(request = "tag_config")

class HSDGetLogStatusCmd : HSDGetCmd(request = "log_status")

data class HSDGetRegisterCmd(
        @SerializedName("address") val address: String?
): HSDGetCmd(request = "register")

//NOTE HSD SET commands
abstract class HSDSetCmd(@SerializedName("request") var request: String?) : HSDCmd(command = "SET")

////NOTE SET Device params
abstract class HSDSetDeviceCmd(request: String) : HSDSetCmd(request=request)

data class HSDSetDeviceAliasCmd(
        @SerializedName("alias") val alias: String
): HSDSetDeviceCmd(request = "deviceInfo")

data class HSDSetWiFiCmd(
        @SerializedName("ssid") val ssid: String?,
        @SerializedName("password") val password: String?,
        @SerializedName("enable") val enable: Boolean?
): HSDSetDeviceCmd(request = "network")

data class HSDSetSWTagCmd(
        @SerializedName("ID") val ID: Int,
        @SerializedName("enable") val enable: Boolean
): HSDSetDeviceCmd(request = "sw_tag")

data class HSDSetSWTagLabelCmd(
        @SerializedName("ID") val ID: Int,
        @SerializedName("label") val label: String
): HSDSetDeviceCmd(request = "sw_tag_label")

data class HSDSetHWTagCmd(
        @SerializedName("ID") val ID: Int,
        @SerializedName("enable") val enable: Boolean
): HSDSetDeviceCmd(request = "hw_tag")

data class HSDSetHWTagLabelCmd(
        @SerializedName("ID") val ID: Int,
        @SerializedName("label") val label: String
): HSDSetDeviceCmd(request = "hw_tag_label")

data class HSDSetAcquisitionInfoCmd(
        @SerializedName("name") val name: String?,
        @SerializedName("notes") val notes: String?
): HSDSetDeviceCmd(request = "acq_info")

////NOTE SET Sensors params
data class HSDSetSensorCmd(
        @SerializedName("sensorId") val sensorId: Int,
        @SerializedName("subSensorStatus") val subSensorStatus: List<SubSensorStatusParam>?
): HSDSetCmd(request = null)

data class HSDSetMLCSensorCmd(
        @SerializedName("sensorId") val sensorId: Int,
        @SerializedName("subSensorStatus") val subSensorStatus: List<MLCConfigParam>?
):HSDSetDeviceCmd(request = "mlc_config")

data class HSDSetSTREDLSensorCmd(
        @SerializedName("sensorId") val sensorId: Int,
        @SerializedName("subSensorStatus") val subSensorStatus: List<STREDLConfigParam>?
):HSDSetDeviceCmd(request = "stredl_config")

abstract class SubSensorStatusParam(
        @SerializedName("id") open val id: Int
)

data class IsActiveParam(
        @Transient override val id: Int,
        @SerializedName("isActive") val isActive: Boolean
) : SubSensorStatusParam(id)

data class ODRParam(
        @Transient override val id: Int,
        @SerializedName("ODR") val odr: Double
) : SubSensorStatusParam(id)

data class FSParam(
        @Transient override val id: Int,
        @SerializedName("FS") val fs: Double
) : SubSensorStatusParam(id)

data class SamplePerTSParam(
        @Transient override val id: Int,
        @SerializedName("samplesPerTs") val samplesPerTs: Int
) : SubSensorStatusParam(id)

data class MLCConfigParam(
        @Transient override val id: Int,
        @SerializedName("mlcConfigSize") val mlcConfigSize: Int,
        @SerializedName("mlcConfigData") val mlcConfigData: String
) : SubSensorStatusParam(id){
        companion object{
                fun fromUCFString(id:Int,ucfContent:String):MLCConfigParam{
                        val isSpace = "\\s+".toRegex()
                        val compactString = ucfContent.lineSequence()
                                .filter { isCommentLine(it) }
                                .map {it.replace(isSpace,"").drop(2)  }
                                .joinToString("")
                        return MLCConfigParam(id,compactString.length,compactString)
                }

                private fun isCommentLine(line:String):Boolean{
                        return !line.startsWith("--")
                }
        }
}

data class STREDLConfigParam(
        @Transient override val id: Int,
        @SerializedName("stredlConfigSize") val stredlConfigSize: Int,
        @SerializedName("stredlConfigData") val stredlConfigData: String
) : SubSensorStatusParam(id){
        companion object{
                fun fromUCFString(id:Int,ucfContent:String):STREDLConfigParam{
                        val isSpace = "\\s+".toRegex()
                        val isAc = "Ac".toRegex()
                        val isWAIT = "WAIT[0-9]+".toRegex()
                        val compactString = ucfContent.lineSequence()
                                .filter { isCommentLine(it) }
                                .map { it.replace(isSpace,"")}
                                .map { it.replace(isAc,"")}
                                .map { it.replace(isWAIT,"W"+it.drop(4)+"W")}
                                .joinToString("")
                        return STREDLConfigParam(id,compactString.length,compactString)
                }

                private fun isCommentLine(line:String):Boolean{
                        return !line.startsWith("--")
                }
        }
}

