package com.st.ui.utils

import com.st.ui.R

fun getBlueStBoardImages(boardType: String): Int {
    return BLUE_ST_SDK_V2_BOARD_IMAGES.getOrDefault(boardType,R.drawable.real_board_generic)
}

fun getBlueStBoardTypeImages(boardType: String): Int {
    return BLUE_ST_SDK_V2_BOARD_TYPE_IMAGES.getOrDefault(boardType,R.drawable.real_board_generic)
}

private val BLUE_ST_SDK_V2_BOARD_TYPE_IMAGES = mapOf(
    "STEVAL_WESU1" to R.drawable.board_steval_wesu1,
    "SENSOR_TILE" to R.drawable.board_sensor_tile,
    "SENSOR_TILE_BOX" to R.drawable.board_sensor_tile,
    "STEVAL_STWINKIT1" to R.drawable.board_sensor_tile,
    "STEVAL_STWINKT1B" to R.drawable.board_sensor_tile,
    "SENSOR_TILE_BOX_PRO" to R.drawable.board_sensor_tile,
    "SENSOR_TILE_BOX_PROB" to R.drawable.board_sensor_tile,
    "SENSOR_TILE_BOX_PROC" to R.drawable.board_sensor_tile,
    "STWIN_BOX" to R.drawable.board_sensor_tile,
    "STWIN_BOXB" to R.drawable.board_sensor_tile,
    "ROBKIT1" to R.drawable.board_robkit1,
    "BLUE_COIN" to R.drawable.board_bluecoin,
    "STEVAL_IDB008VX" to R.drawable.board_bluenrg,
    "STEVAL_BCN002V1" to R.drawable.board_bluenrg,
    "NUCLEO" to R.drawable.board_nucleo,
    "NUCLEO_F401RE" to R.drawable.board_nucleo,
    "NUCLEO_L053R8" to R.drawable.board_nucleo,
    "NUCLEO_L476RG" to R.drawable.board_nucleo,
    "NUCLEO_F446RE" to R.drawable.board_nucleo,
    "NUCLEO_U575ZIQ" to R.drawable.board_nucleo,
    "NUCLEO_U5A5ZJQ" to R.drawable.board_nucleo,
    "DISCOVERY_IOT01A" to R.drawable.board_stm32,
    "B_L475E_IOT01A" to R.drawable.board_stm32,
    "B_U585I_IOT02A" to R.drawable.board_stm32,
    "PROTEUS" to R.drawable.board_stm32,
    "ASTRA1" to R.drawable.board_stm32,
    "STDES_CBMLORABLE" to R.drawable.board_stm32,
    "WB55_NUCLEO_BOARD" to R.drawable.board_generic,
    "STM32WB5MM_DK" to R.drawable.board_generic,
    "WB55_USB_DONGLE_BOARD" to R.drawable.board_generic,
    "WB15CC_NUCLEO_BOARD" to R.drawable.board_generic,
    "B_WB1M_WPAN1" to R.drawable.board_generic,
    "WB55CG_NUCLEO_BOARD" to R.drawable.board_generic,
    "STM32WBA55G_DK1" to R.drawable.board_generic,
    "WBA65RI_NUCLEO_BOARD" to R.drawable.board_generic,
    "GENERIC" to R.drawable.board_generic,
    "WB0X_NUCLEO_BOARD" to R.drawable.board_generic,
    "WB05_NUCLEO_BOARD" to R.drawable.board_generic,
    "X_NUCLEO_67W61M" to R.drawable.board_generic,
    "B_WBA5M_WPAN" to R.drawable.board_generic,
    "STM32WBA65I_DK1" to R.drawable.board_generic
)

private val BLUE_ST_SDK_V2_BOARD_IMAGES = mapOf(
    "STEVAL_WESU1" to R.drawable.real_board_wesu,
    "SENSOR_TILE_BOX" to R.drawable.real_board_sensortilebox,
    "SENSOR_TILE" to R.drawable.real_board_sensortile,
    "STEVAL_STWINKIT1" to R.drawable.real_board_stwinkt1,
    "STEVAL_STWINKT1B" to R.drawable.real_board_stwinkt1b,
    "BLUE_COIN" to R.drawable.real_board_bluecoin,
    "STEVAL_IDB008VX" to R.drawable.board_bluenrg,
    "STEVAL_BCN002V1" to R.drawable.board_bluenrg,
    "NUCLEO" to R.drawable.real_board_nucleo,
    "NUCLEO_F401RE" to R.drawable.real_board_nucleo,
    "NUCLEO_L053R8" to R.drawable.real_board_nucleo,
    "NUCLEO_L476RG" to R.drawable.real_board_nucleo,
    "NUCLEO_U575ZIQ" to R.drawable.real_board_nucleo_u5,
    "NUCLEO_U5A5ZJQ" to R.drawable.real_board_nucleo_u5,
    "NUCLEO_F446RE" to R.drawable.real_board_nucleo,
    "DISCOVERY_IOT01A" to R.drawable.real_board_b_l475e_iot01bx,
    "WB55_NUCLEO_BOARD" to R.drawable.real_board_nucleo_wb55,
    "STM32WB5MM_DK" to R.drawable.real_board_discovery_kit_wb5m,
    "WB55_USB_DONGLE_BOARD" to R.drawable.real_board_usb_dongle_wb55,
    "WB15CC_NUCLEO_BOARD" to R.drawable.real_board_nucleo_wb15,
    "B_WB1M_WPAN1" to R.drawable.real_board_discovery_kit_wb1m,
    "WB55CG_NUCLEO_BOARD" to R.drawable.real_board_nucleo_wba5x,
    "STM32WBA55G_DK1" to R.drawable.real_board_discovery_kit_wba,
    "WBA65RI_NUCLEO_BOARD" to R.drawable.real_board_nucleo_wba6,
    "PROTEUS" to R.drawable.real_board_proteus,
    "ASTRA1" to R.drawable.real_board_astra,
    "STDES_CBMLORABLE" to R.drawable.real_board_stysys_sbu06,
    "SENSOR_TILE_BOX_PRO" to R.drawable.real_board_sensortilebox_pro,
    "SENSOR_TILE_BOX_PROB" to R.drawable.real_board_sensortilebox_pro,
    "SENSOR_TILE_BOX_PROC" to R.drawable.real_board_sensortilebox_pro,
    "STWIN_BOX" to R.drawable.real_board_stwinbx1,
    "STWIN_BOXB" to R.drawable.real_board_stwinbx1,
    "ROBKIT1" to R.drawable.real_board_robkit1,
    "GENERIC" to R.drawable.real_board_generic,
    "B_L475E_IOT01A" to R.drawable.real_board_generic,
    "B_U585I_IOT02A" to R.drawable.real_board_generic,
    "WB0X_NUCLEO_BOARD" to R.drawable.real_board_nucleo_wb0x,
    "WB05_NUCLEO_BOARD" to R.drawable.real_board_wb05kn1,
    "X_NUCLEO_67W61M" to R.drawable.real_board_nucleo_67w61m1,
    "B_WBA5M_WPAN" to R.drawable.real_board_wba5m_wpan,
    "STM32WBA65I_DK1" to R.drawable.real_board_wba651_dk1
)
