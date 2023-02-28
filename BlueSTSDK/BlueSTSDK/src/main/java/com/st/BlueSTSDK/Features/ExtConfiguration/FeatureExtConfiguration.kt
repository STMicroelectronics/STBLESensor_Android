package com.st.BlueSTSDK.Features.ExtConfiguration

import android.util.Log
import com.google.gson.JsonObject
import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.Field
import com.st.BlueSTSDK.Features.highSpeedDataLog.communication.DeviceModel.Sensor
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.STL2TransportProtocol

class FeatureExtConfiguration constructor(n: Node) :
        Feature(FEATURE_NAME, n, arrayOf(FEATURE_EXT_CONF),false) {

    private var mSTWINTransportDecoder = STL2TransportProtocol()

    fun writeCommand(Command: ByteArray?) {
        parentNode.writeFeatureData(this, Command)
    }

    // API for writing one command without any argument
    fun writeCommandWithoutArgument(commandName: String) {
        var command = ExtConfigCommands(command = commandName)
        sendCommand(command)
    }

    // API for writing one command with one Json Argument
    fun writeCommandSetArgumentJSON(commandName: String, obj: Any, onSendComplete: Runnable? = null) {
        var objElement = ExtConfigParser.createExtConfigArgumentJSON(obj)
        var command = ExtConfigCommands(command = commandName, argJsonElement = objElement)
        sendCommand(command,onSendComplete)
    }

    // API for writing one command with one String Argument
    fun writeCommandSetArgumentString(commandName: String, obj: String) {
        var command = ExtConfigCommands(command = commandName, argString = obj)
        sendCommand(command)
    }


    // API for writing one command with one Number Argument
    fun writeCommandSetArgumentNumber(commandName: String, number: Int) {
        var command = ExtConfigCommands(command = commandName, argNumber = number)
        sendCommand(command)
    }

    private fun sendWrite(bytesToSend: ByteArray, onSendComplete: Runnable?) {
        var byteSend = 0
        while (bytesToSend.size - byteSend > 20) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, byteSend + 20))
            byteSend += 20
        }
        if (byteSend != bytesToSend.size) {
            parentNode.writeFeatureData(this, bytesToSend.copyOfRange(byteSend, bytesToSend.size), onSendComplete)
        } //if
    }

    fun getString(sample: Sample): String? {
        val data = ByteArray(sample.data.size)
        for (i in sample.data.indices) {
            data[i] = sample.data[i].toByte()
        }
        return String(data)
    }

    /**
     * extract the Information from Extended Configuration Feature
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes (20) and data extracted (the audio information, the 40 shorts array)
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    override fun extractData(timestamp: Long, data: ByteArray, dataOffset: Int): ExtractResult? {
        val commandFrame = mSTWINTransportDecoder.decapsulate(data)
        if (commandFrame != null) {
            // for example we have here: {"Commands" : "setWifi,setName,setPin,dfu,off"}
            Log.i("ext FeatureExt", String(commandFrame))
            // we need to extract the commands string
            val responseObj = ExtConfigParser.getJsonObj(commandFrame)
            //commandResponse should contains only for example "setWifi,setName,setPin,dfu,off"
            //val commandResponse = ExtConfigParser.extractCommandResult(responseObj)
            //Log.i("json FeatureExt",commandResponse)
            val commandData = CommandSample(
                    responseObj,
                    //ExtConfigParser.extractCommandResult(responseObj)
            )
            return ExtractResult(commandData, data.size)
        }
        return ExtractResult(null, data.size)
    }

    class CommandSample(val command: JsonObject?) :
            Sample(emptyArray(), arrayOf(FEATURE_EXT_CONF))

    private fun sendCommand(command: ExtConfigCommands, onSendComplete: Runnable? = null){
        sendWrite(mSTWINTransportDecoder.encapsulate(ExtConfigParser.createExtConfigCommandJSON(command)), onSendComplete)
    }

    companion object {
        private const val FEATURE_NAME = "ExtConfig"
        private const val FEATURE_DATA_NAME = "Configuration"
        private val FEATURE_EXT_CONF = Field(FEATURE_DATA_NAME, null, Field.Type.ByteArray, Byte.MAX_VALUE, Byte.MIN_VALUE)


        const val TAG = "ExtConfViewModel"
        const val READ_COMMANDS = "ReadCommand"
        const val READ_CERTIFICATE = "ReadCert"
        const val READ_UID = "UID"
        const val READ_VERSION_FW = "VersionFw"
        const val READ_INFO = "Info"
        const val READ_HELP = "Help"
        const val READ_POWER_STATUS = "PowerStatus"
        const val CHANGE_PIN = "ChangePIN"
        const val CLEAR_DB = "ClearDB"
        const val SET_DFU = "DFU"
        const val POWER_OFF = "Off"
        const val BANKS_STATUS = "ReadBanksFwId"
        const val BANKS_SWAP = "BanksSwap"
        const val SET_TIME = "SetTime"
        const val SET_DATE = "SetDate"
        const val SET_WIFI = "SetWiFi"
        const val READ_SENSORS = "ReadSensorsConfig"
        const val SET_SENSORS = "SetSensorsConfig"
        const val SET_NAME = "SetName"
        const val SET_CERTIFICATE = "SetCert"
        const val READ_CUSTOM_COMMANDS = "ReadCustomCommand"

        const val FLASH_FW_ID_NOT_VALID = "0xFFFF"

        fun resultCommandList(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandList(responseObj)
        }

        fun resultCommandCertificate(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandCertificate(responseObj)
        }

        fun resultCommandHelp(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandHelp(responseObj)
        }

        fun resultCommandInfo(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandInfo(responseObj)
        }

        fun resultCommandError(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandError(responseObj)
        }

        fun resultCommandPowerStatus(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandPowerStatus(responseObj)
        }

        fun resultCommandSTM32UID(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandSTM32UID(responseObj)
        }

        fun resultCommandVersionFw(responseObj: JsonObject?): String? {
            return ExtConfigParser.extractCommandVersionFw(responseObj)
        }

        fun resultCustomCommandList(responseObj: JsonObject?): List<CustomCommand>? {
            return ExtConfigParser.extractCustomCommandList(responseObj)
        }

        fun resultCommandReadBanksStatus(responseObj: JsonObject?) : BanksStatus? {
            return ExtConfigParser.extractBanksStatus(responseObj)
        }

        fun resultReadSensorCommand(responseObj: JsonObject?): List<Sensor>? {
            return ExtConfigParser.extractReadSensorCommand(responseObj)
        }

    }
}
