package com.st.BlueSTSDK.Features.highSpeedDataLog.communication

import android.util.Log
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Device
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Response
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.SensorType
import java.lang.reflect.Type
import java.util.*

class DeviceParser {

    companion object {

        private val gsonEncDec = GsonBuilder()
                .registerTypeAdapter(SensorType::class.java,SensorTypeSerializer)
                .create()


        @JvmStatic
        fun getJsonObj(rawData:ByteArray):JsonObject?{
            val commandString = rawData.toString(Charsets.UTF_8).dropLast(1)
            return try {
                gsonEncDec.fromJson(commandString,JsonObject::class.java)
            }catch (e: JsonSyntaxException){
                Log.e("HSD DeviceParser","error parsing the response: $e")
                Log.e("HSD DeviceParser",commandString)
                null
            }
        }

        fun toJsonStr(sensors:List<Sensor>):String{
            return gsonEncDec.toJsonTree(sensors).toString()
        }

        fun extractSensors(json:String):List<Sensor>?{
            return try {
                val listType: Type = object : TypeToken<List<Sensor>?>() {}.type
                gsonEncDec.fromJson(json,listType)
            }catch (e: JsonSyntaxException){
                null
            }
        }

        //NOTE -- create JSON String from Command Classes
        @JvmStatic
        fun createHSDCommandJSON(command:HSDCmd):String{
            return gsonEncDec.toJson(command)
        }

        @JvmStatic
        fun extractDevice(obj:JsonObject?): Device?{
            obj ?: return null
            if(obj.has("device")){
                return try {
                    gsonEncDec.fromJson(obj,
                            Response::class.java).device
                }catch (e: JsonSyntaxException){
                    Log.e("HSD DeviceParser","error parsing the DeviceStatus: $e")
                    Log.e("HSD DeviceParser",obj.toString())
                    null
                }
            }
            if(obj.has("deviceInfo")){
                return try {
                    gsonEncDec.fromJson(obj,
                            Device::class.java)
                }catch (e: JsonSyntaxException){
                    Log.e("HSD DeviceParser","error parsing the DeviceStatus: $e")
                    Log.e("HSD DeviceParser",obj.toString())
                    null
                }
            }
            if(obj.has(("tagConfig"))){
                return try {
                    gsonEncDec.fromJson(obj,
                            Device::class.java)
                }catch (e: JsonSyntaxException){
                    Log.e("HSD DeviceParser","error parsing the DeviceStatus: $e")
                    Log.e("HSD DeviceParser",obj.toString())
                    null
                }
            }
            return null
        }

        @JvmStatic
        fun extractDeviceStatus(obj:JsonObject?): DeviceStatus?{
            obj ?: return null
            return try {
                gsonEncDec.fromJson(obj,
                        DeviceStatus::class.java)
            }catch (e: JsonSyntaxException){
                Log.e("HSD DeviceParser","error parsing the DeviceStatus: $e")
                Log.e("HSD DeviceParser",obj.toString())
                null
            }
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