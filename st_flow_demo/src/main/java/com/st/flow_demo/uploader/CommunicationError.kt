package com.st.flow_demo.uploader

enum class CommunicationError(val code: Int) {
    FW_VERSION_ERROR(0),
    PARSING_ERROR(1),
    ERROR_MISSING_SD(2),
    ERROR_USB_NOT_CONNECTED(3),
    ERROR_MISSING_SD_FILE(4),
    SD_IO_ERROR(5),
    TIMEOUT_ERROR(6),
    GENERIC_ERROR(7),
    APP_VERSION_ERROR(8),
    FLOW_COMPATIBILITY_ERROR(9),
    FLOW_NO_ERROR(10),
    FLOW_RECEIVED(11),
    FLOW_RECEIVED_AND_PARSED(12),
    FLOW_ERROR_UNKNOWN(13);

    companion object {
        fun getCommunicationError(code: Int) = when(code) {
            0 -> FW_VERSION_ERROR
            1 -> PARSING_ERROR
            2 -> ERROR_MISSING_SD
            3 -> ERROR_USB_NOT_CONNECTED
            4 -> ERROR_MISSING_SD_FILE
            5 -> SD_IO_ERROR
            6 -> TIMEOUT_ERROR
            7 -> GENERIC_ERROR
            8 -> APP_VERSION_ERROR
            9 -> FLOW_COMPATIBILITY_ERROR
            10 -> FLOW_NO_ERROR
            11 -> FLOW_RECEIVED
            12 -> FLOW_RECEIVED_AND_PARSED
            else -> FLOW_ERROR_UNKNOWN
        }
    }
}