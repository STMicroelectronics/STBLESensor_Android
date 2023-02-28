package com.st.BlueSTSDK.Features.ExtConfiguration

import android.util.Log
import com.google.gson.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import java.lang.reflect.Type
import java.util.*

class ExtConfigParser {

    companion object {

        private val gsonEncDec = GsonBuilder()
                .registerTypeAdapter(SensorType::class.java, SensorTypeSerializer)
                .create()

        //create JSON String from Command Classes
        @JvmStatic
        fun createExtConfigCommandJSON(obj: Any):String{
            return gsonEncDec.toJson(obj)
        }

        @JvmStatic
        fun createExtConfigArgumentJSON(obj: Any): JsonElement {
            return gsonEncDec.toJsonTree(obj)
        }

        @JvmStatic
        fun getJsonObj(rawData:ByteArray):JsonObject?{
            val commandString = rawData.toString(Charsets.UTF_8).dropLast(1)
            return try {
                gsonEncDec.fromJson(commandString,JsonObject::class.java)
            }catch (e: JsonSyntaxException){
                Log.e("ExtConfigParser","error parsing the response: $e")
                Log.e("ExtConfigParser",commandString)
                null
            }
        }

        @JvmStatic
        fun extractCommandList(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Commands")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).CommandList
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Commands: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandInfo(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Info")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).info
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Info: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandError(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Error")) {
                return try {
                    gsonEncDec.fromJson(obj,
                        ExtConfigCommandAnswers::class.java).error
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Error: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandHelp(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Help")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).help
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Help: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandCertificate(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("Certificate")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).certificate
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the Certificate: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandVersionFw(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("VersionFw")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).versionFw
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the VersionFw: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandSTM32UID(obj: JsonObject?): String? {
            obj ?: return null
            if (obj.has("UID")) {
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).stm32UID
                } catch (e: JsonSyntaxException) {
                    Log.e("ExtConfigParser", "error parsing the STM32_UID: $e")
                    Log.e("HExtConfigParser", obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCommandPowerStatus(obj: JsonObject?): String? {
            obj ?: return null
            if(obj.has("PowerStatus")){
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).powerStatus
                }catch (e: JsonSyntaxException){
                    Log.e("ExtConfigParser","error parsing the PowerStatus: $e")
                    Log.e("HExtConfigParser",obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractCustomCommandList(obj: JsonObject?): List<CustomCommand>? {
            obj ?: return null
            if(obj.has("CustomCommands")){
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).CustomCommandList
                }catch (e: JsonSyntaxException){
                    Log.e("ExtConfigParser","error parsing the listCustomCommand: $e")
                    Log.e("HExtConfigParser",obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractBanksStatus(obj: JsonObject?): BanksStatus? {
            obj ?: return null
            if(obj.has("BankStatus")){
                return try {
                    gsonEncDec.fromJson(obj,
                        ExtConfigCommandAnswers::class.java).banksStatus
                }catch (e: JsonSyntaxException){
                    Log.e("ExtConfigParser","error parsing the BankStatus: $e")
                    Log.e("HExtConfigParser",obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractReadSensorCommand(obj: JsonObject?): List<Sensor>? {
            obj ?: return null
            if(obj.has("sensor")){
                return try {
                    gsonEncDec.fromJson(obj,
                            ExtConfigCommandAnswers::class.java).sensor
                }catch (e: JsonSyntaxException){
                    Log.e("ExtConfigParser","error parsing the listCustomCommand: $e")
                    Log.e("HExtConfigParser",obj.toString())
                    null
                }
            }
            return null
        }
    }
}

private object SensorTypeSerializer : JsonSerializer<SensorType>,JsonDeserializer<SensorType>{
    override fun serialize(src: SensorType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return when(src){
            SensorType.Accelerometer -> JsonPrimitive("ACC")
            SensorType.Magnetometer -> JsonPrimitive("MAG")
            SensorType.Gyroscope -> JsonPrimitive("GYRO")
            SensorType.Temperature -> JsonPrimitive("TEMP")
            SensorType.Humidity -> JsonPrimitive("HUM")
            SensorType.Pressure -> JsonPrimitive("PRESS")
            SensorType.Microphone -> JsonPrimitive("MIC")
            SensorType.MLC -> JsonPrimitive("MLC")
            SensorType.CLASS -> JsonPrimitive("CLASS")
            SensorType.STREDL -> JsonPrimitive("STREDL")
            null,SensorType.Unknown -> JsonPrimitive("")
        }
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): SensorType {
        val str = json?.asString ?: return SensorType.Unknown
        return when (str.uppercase(Locale.getDefault())){
            "ACC" -> SensorType.Accelerometer
            "MAG" -> SensorType.Magnetometer
            "GYRO" -> SensorType.Gyroscope
            "TEMP" -> SensorType.Temperature
            "HUM" -> SensorType.Humidity
            "PRESS" -> SensorType.Pressure
            "MIC" -> SensorType.Microphone
            "MLC" -> SensorType.MLC
            "CLASS" -> SensorType.CLASS
            "STREDL" -> SensorType.STREDL
            else -> SensorType.Unknown
        }
    }

}