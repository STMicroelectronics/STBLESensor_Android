package com.st.smart_motor_control.model

enum class MotorControlFault {
    None,
    Duration,
    OverVolt,
    UnderVolt,
    OverTemp,
    StartUp,
    SpeedFDBK,
    BreakIn,
    SwError,
    Unknown;

    fun getErrorStringFromCode() : String {
        return when(this) {
            None -> "No Error"
            Duration -> "FOC rate to high"
            OverVolt -> "Software over voltage"
            UnderVolt -> "Software under voltage"
            OverTemp -> "Software over temperature"
            StartUp -> "Startup failed"
            SpeedFDBK -> "Speed feedback"
            BreakIn -> "Emergency input (Over current)"
            SwError -> "Software Error"
            Unknown -> "Unknown Error"
        }
    }

    companion object {
        fun getErrorCodeFromValue(code: Int) : MotorControlFault{
            return when (code) {
                0 -> None
                1 -> Duration
                2 -> OverVolt
                3 -> UnderVolt
                4 -> OverTemp
                5 -> StartUp
                6 -> SpeedFDBK
                7 -> BreakIn
                8 -> SwError
                else -> Unknown
            }
        }
    }
}