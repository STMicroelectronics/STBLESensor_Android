/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.BlueSTSDK.gui;

import androidx.annotation.DrawableRes;

import com.st.BlueSTSDK.Node;

public class NodeGui {

    public static @DrawableRes
    int getBoardTypeImage(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return R.drawable.board_steval_wesu1;
            case SENSOR_TILE:
            case SENSOR_TILE_BOX:
            case STEVAL_STWINKIT1:
            case STEVAL_STWINKT1B:
            case SENSOR_TILE_BOX_PRO:
            case STWIN_BOX:
                return R.drawable.board_sensor_tile;
            case BLUE_COIN:
                return R.drawable.board_bluecoin;
            case STEVAL_IDB008VX:
            case STEVAL_BCN002V1:
                return R.drawable.board_bluenrg;
            case NUCLEO:
            case NUCLEO_F401RE:
            case NUCLEO_L053R8:
            case NUCLEO_L476RG:
            case NUCLEO_F446RE:
                return R.drawable.board_nucleo;
            case DISCOVERY_IOT01A:
            case B_L475E_IOT01A:
            case B_U585I_IOT02A:
            case ASTRA1:
            case PROTEUS:
            case STDES_CBMLORABLE:
                return R.drawable.board_stm32;
            case WB_BOARD:
            case WBA_BOARD:
            case GENERIC:
            default:
                return R.drawable.board_generic;
        }
    }

    public static @DrawableRes
    int getRealBoardTypeImage(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return R.drawable.real_board_wesu;
            case SENSOR_TILE_BOX:
                return R.drawable.real_board_sensortilebox;
            case SENSOR_TILE:
                return R.drawable.real_board_sensortile;
            case STEVAL_STWINKIT1:
                return R.drawable.real_board_stwinkt1;
            case STEVAL_STWINKT1B:
                return R.drawable.real_board_stwinkt1b;
            case BLUE_COIN:
                return R.drawable.real_board_bluecoin;
            case STEVAL_IDB008VX:
            case STEVAL_BCN002V1:
                return R.drawable.board_bluenrg;
            case NUCLEO:
            case NUCLEO_F401RE:
            case NUCLEO_L053R8:
            case NUCLEO_L476RG:
            case NUCLEO_F446RE:
                return R.drawable.real_board_nucleo;
            case DISCOVERY_IOT01A:
                return R.drawable.real_board_b_l475e_iot01bx;
            case WB_BOARD:
                return R.drawable.real_board_pnucleo_wb55;
            case WBA_BOARD:
                return R.drawable.real_board_wba;
            case PROTEUS:
                return R.drawable.real_board_proteus;
            case ASTRA1:
                return R.drawable.real_board_astra;
            case STDES_CBMLORABLE:
                return R.drawable.real_board_stdes_cbmlorable;
            case SENSOR_TILE_BOX_PRO:
                return R.drawable.real_board_sensortilebox_pro;
            case STWIN_BOX:
                return R.drawable.real_board_stwinbx1;
            case GENERIC:
            case B_L475E_IOT01A:
            case B_U585I_IOT02A:
            default:
                return R.drawable.real_board_generic;
        }
    }

}
