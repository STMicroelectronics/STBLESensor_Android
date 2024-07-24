package com.st.pnpl.util

import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd

enum class PnPLTypeOfCommand{
    Command,
    Set,
    Log,
    Status
}

data class SetCommandPnPLRequest(
    val typeOfCommand: PnPLTypeOfCommand,
    val pnpLCommand: PnPLCmd,
    val askTheStatus: Boolean = true
)