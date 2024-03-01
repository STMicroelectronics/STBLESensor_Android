package com.st.flow_demo.uploader

import com.st.blue_sdk.utils.NumberConversion
import java.util.Calendar

class FlowUploaderHelper {

    companion object {
//        private val TAG = FlowUploaderHelper::class.java.simpleName
//        const val TIMEOUT_MS = 10000L
        const val CHARACTERISTIC_SIZE = 20
        const val SEND_FLOW_REQUEST = "SF"
        const val SEND_FLOW_RESPONSE = "Flow_Req_Received"
        const val SEND_PARSING_FLOW_RESPONSE = "Parsing_flow"
        const val FLOW_PARSED_MESSAGE_OK = "Flow_parse_ok"
        const val FLOW_ERROR_MESSAGE = "Error:"
    }
}

fun startFlowMessage(flowLength:Int):ByteArray{
    val message = ByteArray(10)
    System.arraycopy(
        FlowUploaderHelper.SEND_FLOW_REQUEST.toByteArray(),0,message,0,
        FlowUploaderHelper.SEND_FLOW_REQUEST.length)

    val lengthPayload = NumberConversion.BigEndian.int32ToBytes(flowLength)
    System.arraycopy(lengthPayload, 0, message, FlowUploaderHelper.SEND_FLOW_REQUEST.length, lengthPayload.size)
    val nowS = getSecondsFrom1970()
    val dataPayload = NumberConversion.BigEndian.uint32ToBytes(nowS)
    System.arraycopy(dataPayload, 0, message, FlowUploaderHelper.SEND_FLOW_REQUEST.length + lengthPayload.size, dataPayload.size)
    return message
}


private fun getSecondsFrom1970():Long{
    val calendar = Calendar.getInstance()
    val timeZoneOffsetMS = (calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET))
    return (calendar.timeInMillis + timeZoneOffsetMS)/1000
}


