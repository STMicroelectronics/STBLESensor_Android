package com.st.ui.utils

import com.st.ui.R

fun getBlueStBoardImages(boardType: String): Int {
    return BLUE_ST_SDK_V2_BOARD_IMAGES[boardType] ?: R.drawable.real_board_generic
}

fun getBlueStBoardTypeImages(boardType: String): Int {
    return BLUE_ST_SDK_V2_BOARD_TYPE_IMAGES[boardType] ?: R.drawable.real_board_generic
}

private val BLUE_ST_SDK_V2_BOARD_TYPE_IMAGES = mapOf(
    "STEVAL_WESU1" to R.drawable.board_steval_wesu1,
    "SENSOR_TILE" to R.drawable.board_sensor_tile,
    "SENSOR_TILE_BOX" to R.drawable.board_sensor_tile,
    "STEVAL_STWINKIT1" to R.drawable.board_sensor_tile,
    "STEVAL_STWINKT1B" to R.drawable.board_sensor_tile,
    "SENSOR_TILE_BOX_PRO" to R.drawable.board_sensor_tile,
    "STWIN_BOX" to R.drawable.board_sensor_tile,
    "BLUE_COIN" to R.drawable.board_bluecoin,
    "STEVAL_IDB008VX" to R.drawable.board_bluenrg,
    "STEVAL_BCN002V1" to R.drawable.board_bluenrg,
    "NUCLEO" to R.drawable.board_nucleo,
    "NUCLEO_F401RE" to R.drawable.board_nucleo,
    "NUCLEO_L053R8" to R.drawable.board_nucleo,
    "NUCLEO_L476RG" to R.drawable.board_nucleo,
    "NUCLEO_F446RE" to R.drawable.board_nucleo,
    "DISCOVERY_IOT01A" to R.drawable.board_stm32,
    "B_L475E_IOT01A" to R.drawable.board_stm32,
    "B_U585I_IOT02A" to R.drawable.board_stm32,
    "PROTEUS" to R.drawable.board_stm32,
    "ASTRA1" to R.drawable.board_stm32,
    "STDES_CBMLORABLE" to R.drawable.board_stm32,
    "WB_BOARD" to R.drawable.board_generic,
    "WBA_BOARD" to R.drawable.board_generic,
    "GENERIC" to R.drawable.board_generic,
    "NUCLEO_WB09KE" to R.drawable.board_generic
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
    "NUCLEO_F446RE" to R.drawable.real_board_nucleo,
    "DISCOVERY_IOT01A" to R.drawable.real_board_b_l475e_iot01bx,
    "WB_BOARD" to R.drawable.real_board_pnucleo_wb55,
    "WBA_BOARD" to R.drawable.real_board_wba,
    "PROTEUS" to R.drawable.real_board_proteus,
    "ASTRA1" to R.drawable.real_board_astra,
    "STDES_CBMLORABLE" to R.drawable.real_board_stysys_sbu06,
    "SENSOR_TILE_BOX_PRO" to R.drawable.real_board_sensortilebox_pro,
    "STWIN_BOX" to R.drawable.real_board_stwinbx1,
    "GENERIC" to R.drawable.real_board_generic,
    "B_L475E_IOT01A" to R.drawable.real_board_generic,
    "B_U585I_IOT02A" to R.drawable.real_board_generic,
    "NUCLEO_WB09KE" to R.drawable.real_board_generic
)
