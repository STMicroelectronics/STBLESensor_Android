package com.st.BlueSTSDK.Features.highSpeedDataLog

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.Field
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.*
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceParser.Companion.createHSDCommandJSON
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.STL2TransportProtocol

class FeatureHSDataLogConfig constructor(n: Node) :
        Feature(FEATURE_NAME, n, arrayOf(STWINCONFIG_FIELD),false) {
    private var mSTWINTransportDecoder = STL2TransportProtocol()

    //NOTE -- Model Classes Parsing from received Samples
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val commandFrame = mSTWINTransportDecoder.decapsulate(data)
        if (commandFrame != null) {
//            val HSDval = commandFrame.decodeToString();
//            Log.i("HSDataLog:",HSDval);
            val responseObj = DeviceParser.getJsonObj(commandFrame)
            val commandData = ConfigSample(
                    DeviceParser.extractDevice(responseObj),
                    DeviceParser.extractDeviceStatus(responseObj)
            )
            return ExtractResult(commandData, data.size)
        }
        return ExtractResult(null, data.size)
    }

    private fun sendWrite(bytesToSend: ByteArray, onSendComplete: Runnable?) {
        var byteSend = 0
        while (bytesToSend.size - byteSend > 20) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, byteSend + 20))
            byteSend += 20
        }
        if (byteSend != bytesToSend.size) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, bytesToSend.size),onSendComplete)
        } //if
    }

    private class ConfigSample(val device: Device?, val status: DeviceStatus?) :
            Sample(emptyArray(), arrayOf(STWINCONFIG_FIELD))

    fun sendGetCmd(command: HSDGetCmd) = sendCommand(command)

    fun sendSetCmd(command: HSDSetCmd, onSendComplete: Runnable?=null) = sendCommand(command,onSendComplete)

    fun sendControlCmd(command: HSDControlCmd) = sendCommand(command)

    private fun sendCommand(command:HSDCmd, onSendComplete: Runnable?=null){
        sendWrite(mSTWINTransportDecoder.encapsulate(createHSDCommandJSON(command)),onSendComplete)
    }

    companion object {
        const val MIN_FW_V_MAJOR = 1
        const val MIN_FW_V_MINOR = 2
        const val MIN_FW_V_PATCH = 0
        const val LATEST_FW_NAME = "FP-SNS-DATALOG1"
        const val LATEST_FW_URL = "https://www.st.com/en/embedded-software/fp-sns-datalog1.html"
        private const val FEATURE_NAME = "HSDataLogConfig"
        private const val FEATURE_DATA_NAME = "ConfigJson"
        private val STWINCONFIG_FIELD = Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)

        fun getFWVersionString(): String{
            return "$MIN_FW_V_MAJOR.$MIN_FW_V_MINOR.$MIN_FW_V_PATCH"
        }

        fun isLogging(sample: Sample): Boolean? {
            return getDeviceStatus(sample)?.isSDLogging
        }

        fun isSDCardInserted(sample: Sample): Boolean? {
            return getDeviceStatus(sample)?.isSDCardInserted
        }

        fun getSensorStatusWId(sample: Sample): SensorStatusWId? {
            val sensorId = getDeviceStatus(sample)?.sensorId
            val sensorStatus = getDeviceStatus(sample)?.sensorStatus
            if(sensorId == null || sensorStatus == null)
                return null
            return SensorStatusWId(sensorId,sensorStatus)
        }

        //NOTE -- Model Classes Parsing from received Samples
        fun getDeviceConfig(sample: Sample): Device? {
            val hsdSample = sample as? ConfigSample ?: return null
            return hsdSample.device
        }

        fun getDeviceInfo(sample: Sample): DeviceInfo? {
            return getDeviceConfig(sample)?.deviceInfo
        }

        fun getDeviceTagConfig(sample: Sample): TagConfig? {
            return getDeviceConfig(sample)?.tags
        }

        fun getDeviceStatus(sample: Sample): DeviceStatus? {
            val hsdSample = sample as? ConfigSample ?: return null
            return hsdSample.status
        }
    }
}