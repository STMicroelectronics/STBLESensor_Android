package com.st.BlueSTSDK.Features.PnPL

import android.util.Log
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.Field
import com.st.BlueSTSDK.Features.PnPL.PnPLParser.Companion.createPnPLCommandJSON
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.STL2TransportProtocol

class FeaturePnPL constructor(n: Node) :
        Feature(FEATURE_NAME, n, arrayOf(FEATURE_PNPL_CONFIG),false) {

    private var mSTL2TransportDecoder = STL2TransportProtocol()

    // API to create and send a PnPL GET Status command
    fun sendPnPLGetDeviceStatusCmd(command: PnPLGetDeviceStatusCmd, onSendComplete: Runnable?=null) = sendPnPLCommand(command, onSendComplete)

    // API to create and send a PnPL GET Component Status command
    fun sendPnPLGetComponentStatusCmd(command: PnPLGetComponentStatusCmd, onSendComplete: Runnable?=null) = sendPnPLCommand(command, onSendComplete)

    // API to send a PnPL SET Property command
    fun sendPnPLSetPropertyCmd(command: PnPLSetPropertyCmd, onSendComplete: Runnable?=null) = sendPnPLCommand(command, onSendComplete)

    // API to send a PnPL COMMAND command (with request and with fields)
    fun sendPnPLCommandCmd(compName: String, commandName: String, requestName: String, commandFields: Map<String,Any>, onSendComplete: Runnable?=null){
        val fields = mutableListOf<PnPLSendCommandField>()
        for ((k, v) in commandFields) {
            fields.add(PnPLSendCommandField(k,v))
        }
        val command = PnPLSendCommand(compName, commandName, requestName, fields)
        sendPnPLCommand(command, onSendComplete)
    }

    // API to send a PnPL COMMAND command (without request and with fields)
    fun sendPnPLCommandCmd(compName: String, commandName: String, commandFields: Map<String,Any>, onSendComplete: Runnable?=null){
        val fields = mutableListOf<PnPLSendCommandField>()
        for ((k, v) in commandFields) {
            fields.add(PnPLSendCommandField(k,v))
        }
        val command = PnPLSendCommand(compName, commandName, field_list =  fields)
        sendPnPLCommand(command, onSendComplete)
    }

    // API to send a PnPL COMMAND command (without request and fields)
    fun sendPnPLCommandCmd(compName: String, commandName: String, onSendComplete: Runnable?=null){
        val command = PnPLSendCommand(compName, commandName)
        sendPnPLCommand(command, onSendComplete)
    }

    fun sendPnPLCommand(command:PnPLCmd, onSendComplete: Runnable?=null){
        val cmdString = createPnPLCommandJSON(command)
        Log.d("FeaturePnPL","command: $cmdString")
        sendWrite(mSTL2TransportDecoder.encapsulate(cmdString),onSendComplete)
    }

    private fun sendWrite(bytesToSend: ByteArray, onSendComplete: Runnable?) {
        var byteSend = 0
        while (bytesToSend.size - byteSend > 20) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, byteSend + 20))
            byteSend += 20
        }
        if (byteSend != bytesToSend.size) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, bytesToSend.size),onSendComplete)
        }
    }

    //PnPL Commands (Responses from the Leaf Device) Parsing from received Samples
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult {
        val commandFrame = mSTL2TransportDecoder.decapsulate(data)
        if (commandFrame != null) {
            val responseObj = PnPLParser.getJsonObj(commandFrame)

            //Received PnPL commands parsing

            //Device Status Parsing
            responseObj?.get("devices")?.let {
                val devStatus = PnPLParser.extractPnPLDeviceStatus(responseObj)
                val commandData = PnPLDeviceStatusSample(devStatus)
                return ExtractResult(commandData, data.size)
            }

            //NOTE: Here this commands parser function would be expanded with eventual other future commands

            //Component Status Parsing (No specific known key in command (key=component name))
            if (responseObj != null) {
                return try {
                    //Component Status extraction
                    val componentStatus = PnPLParser.extractPnPLComponentStatus(responseObj)
                    val commandData = PnPLComponentStatusSample(componentStatus)
                    ExtractResult(commandData, data.size)
                } catch (e: ClassCastException) {
                    ExtractResult(null, data.size)
                }
            }
        }
        return ExtractResult(null, data.size)
    }

    //OLD
    private class PnPLDeviceStatusSample(val deviceStatus: PnPLDeviceStatus?) :
        Sample(emptyArray(), arrayOf(FEATURE_PNPL_CONFIG)) {
    }

    //OLD
    private class PnPLComponentStatusSample(val componentStatus: PnPLComponent?) :
        Sample(emptyArray(), arrayOf(FEATURE_PNPL_CONFIG)) {
    }

    companion object {
        private const val FEATURE_NAME = "PnPL"
        private const val FEATURE_DATA_NAME = "Vespucci Configuration & Control"
        private val FEATURE_PNPL_CONFIG = Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)

        //Device Status Parsing from received Samples
        fun getPnPLDeviceStatus(sample: Sample): PnPLDeviceStatus?{
            val devStatusSample = sample as? PnPLDeviceStatusSample ?: return null
            return devStatusSample.deviceStatus
        }

        //Component Status Parsing from received Samples
        fun getPnPLComponentStatus(sample: Sample): PnPLComponent?{
            val componentStatusSample = sample as? PnPLComponentStatusSample ?: return null
            return componentStatusSample.componentStatus
        }
    }
}