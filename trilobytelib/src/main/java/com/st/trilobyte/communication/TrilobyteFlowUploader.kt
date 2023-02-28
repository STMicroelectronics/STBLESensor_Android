package com.st.trilobyte.communication

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.st.BlueSTSDK.Debug
import com.st.BlueSTSDK.Node
import com.st.BlueSTSDK.Utils.NumberConversion
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole
import com.st.trilobyte.helper.removeTerminatorCharacters
import java.util.*

class TrilobyteFlowUploader {

    val sb = StringBuffer()

    enum class CommunicationError(val code: Int) {
        FW_VERSION_ERROR(0),
        PARSING_ERROR(1),
        ERROR_MISSING_SD(2),
        ERROR_USB_NOT_CONNECTED(3),
        ERROR_MISSING_SD_FILE(4),
        SD_IO_ERROR(5),
        TIMEOUT_ERROR(6),
        GENERIC_ERROR(7),
        APP_VERSION_ERROR(8)
    }

    companion object {

        private val TAG = TrilobyteFlowUploader::class.java.simpleName

        private const val TIMEOUT_MS = 5000L

        private const val CHARACTERISTIC_SIZE = 20

        private const val SEND_FLOW_REQUEST = "SF"

        private const val SEND_FLOW_RESPONSE = "Flow_Req_Received"

        private const val FLOW_PARSED_MESSAGE_OK = "Flow_parse_ok"

        private const val FLOW_ERROR_MESSAGE = "Error:"
    }

    private val mTimeoutHandler: Handler

    init {
        mTimeoutHandler = Handler(Looper.getMainLooper())
    }

    fun checkFwVersion(node: Node?, versionListener: FwVersionConsole.FwVersionCallback) {

        if (node == null || node.debug == null) {
            versionListener.onVersionRead(null, FirmwareType.BOARD_FW, null)
            return
        }

        val fwVersionConsole = FwVersionConsole.getFwVersionConsole(node)
        fwVersionConsole?.let {
            it.setLicenseConsoleListener(versionListener)
            it.readVersion(FirmwareType.BOARD_FW)
        } ?: run { versionListener.onVersionRead(null, FirmwareType.BOARD_FW, null) }
    }

    fun uploadFlow(node: Node?, payload: ByteArray, listener: FlowUploadListener) {
        val console =  node?.debug
        if (console == null) {
            listener.onError(CommunicationError.GENERIC_ERROR.code)
            return
        }

        val uploader = FlowUploader(console,payload,listener)

        uploader.startUpload()
    }

       // interface
    interface FlowUploadListener {
        fun onSuccess()

        fun onError(errorCode: Int)
    }


    inner class FlowUploader(
            private val console:Debug,
            private val data:ByteArray,
            private val flowListener: FlowUploadListener) : Debug.DebugOutputListener {

        private val mTimeoutRunnable = Runnable { notifyTransmissionError(CommunicationError.TIMEOUT_ERROR.code) }
        private var mSentChar = 0

        private fun getSecondsFrom1970():Long{
            val calendar = Calendar.getInstance()
            val timeZoneOffsetMS = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET))
            return (calendar.timeInMillis + timeZoneOffsetMS)/1000
        }

        private fun startMessage(flowLength:Int):ByteArray{
            val message = ByteArray(10)
            System.arraycopy(SEND_FLOW_REQUEST.toByteArray(),0,message,0,TrilobyteFlowUploader.SEND_FLOW_REQUEST.length)

            val lengthPayload = NumberConversion.BigEndian.int32ToBytes(flowLength)
            System.arraycopy(lengthPayload, 0, message, TrilobyteFlowUploader.SEND_FLOW_REQUEST.length, lengthPayload.size)
            val nowS = getSecondsFrom1970()
            val dataPayload = NumberConversion.BigEndian.uint32ToBytes(nowS)
            System.arraycopy(dataPayload, 0, message, TrilobyteFlowUploader.SEND_FLOW_REQUEST.length + lengthPayload.size, dataPayload.size)
            //Log.d(TAG, "onStdOutReceived lenFlow=${flowLength} ${lengthPayload.size}  ${dataPayload.size}")
            return message
        }

        fun startUpload(){
            console.addDebugOutputListener(this)
            val startMessage = startMessage(data.size)
            console.write(startMessage)
            mTimeoutHandler.postDelayed(mTimeoutRunnable, TIMEOUT_MS)
        }

        override fun onStdOutReceived(console: Debug, message: String) {
            Log.d(TAG, "onStdOutReceived: $message")

            val escapedMessage = message.apply {
                replace("\n", "")
                replace("\r", "")
            }

            when {
                escapedMessage.startsWith(SEND_FLOW_RESPONSE) -> {
                    sendNextMessage(0)
                }
                escapedMessage.startsWith(FLOW_PARSED_MESSAGE_OK) -> {
                    mTimeoutHandler.removeCallbacks(mTimeoutRunnable)
                    flowListener.onSuccess()
                    console.removeDebugOutputListener(this)
                }
                escapedMessage.startsWith(FLOW_ERROR_MESSAGE) -> handleBoardError(message)
                else -> {
                    sendNextMessage(message.length)
                }
            }
        }

        private fun sendNextMessage(length: Int) {
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable)
            mSentChar+=length
            if(mSentChar >= data.size){
                //re add the callback to wait the flow load response from the fw
                mTimeoutHandler.postDelayed(mTimeoutRunnable, TIMEOUT_MS)
                return
            }
            val lastChar = minOf(mSentChar + CHARACTERISTIC_SIZE,data.size)
            val lenDataToSend = lastChar - mSentChar
            val dataToSend = ByteArray(lenDataToSend)
            data.copyInto(dataToSend, 0, mSentChar, lastChar)

            console.write(dataToSend)
            mTimeoutHandler.postDelayed(mTimeoutRunnable, TIMEOUT_MS)
        }

        override fun onStdErrReceived(debug: Debug, message: String) {
            Log.d(TAG, "onStdErrReceived: $message")
            notifyTransmissionError(CommunicationError.GENERIC_ERROR.code)
        }

        override fun onStdInSent(debug: Debug, message: String, writeResult: Boolean) {
            Log.d(TAG, "onStdInSent: $message")
            sb.append(message)
            if (!writeResult) {
                notifyTransmissionError(CommunicationError.GENERIC_ERROR.code)
                return
            }
        }

        private fun handleBoardError(errorMessage: String) {

            var errorCode = CommunicationError.GENERIC_ERROR.code

            try {
                val parsedError = errorMessage.removeTerminatorCharacters()
                        .substring(errorMessage.indexOf(":") + 1)
                errorCode = Integer.parseInt(parsedError)
            } catch (ignored: Exception) {
            } finally {
                notifyTransmissionError(errorCode)
            }
        }

        private fun notifyTransmissionError(errorCode: Int) {
            Log.d(TAG, "messageSent: $sb")
            mTimeoutHandler.removeCallbacks(mTimeoutRunnable)
            console.removeDebugOutputListener(this)
            flowListener.onError(errorCode)
        }

    }

}
