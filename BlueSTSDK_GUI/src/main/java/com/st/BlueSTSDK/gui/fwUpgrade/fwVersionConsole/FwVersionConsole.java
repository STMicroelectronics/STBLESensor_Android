/*
 * Copyright (c) 2018  STMicroelectronics – All rights reserved
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
package com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole;

import androidx.annotation.Nullable;

import com.st.BlueNRG.fwUpgrade.FwVersionConsoleBlueNRG;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.STM32WB.fwUpgrade.FwVersionConsoleSTM32WB;

public abstract  class FwVersionConsole {

    /**
     * get an instance of this class that works with the node
     *
     * @param node node where upload the firmware
     * @return null if isn't possible upload the firmware in the node, or a class for do it
     */
    static public @Nullable
    FwVersionConsole getFwVersionConsole(Node node) {
        FwVersionConsole stm32wbConsole = FwVersionConsoleSTM32WB.buildForNode(node);
        if( stm32wbConsole!=null)
            return stm32wbConsole;

        FwVersionConsole blueNRGConsole = FwVersionConsoleBlueNRG.buildForNode(node);
        if( blueNRGConsole!=null && node.getType()!= Node.Type.STEVAL_BCN002V1)
            return blueNRGConsole;

        Debug debug = node.getDebug();
        if (debug == null)
            return null;
        switch (node.getType()) {
            case NUCLEO:
            case NUCLEO_F401RE:
            case NUCLEO_L053R8:
            case NUCLEO_L476RG:
            case NUCLEO_F446RE:
            case SENSOR_TILE:
            case BLUE_COIN:
            case STEVAL_BCN002V1:
            case SENSOR_TILE_BOX:
            case DISCOVERY_IOT01A:
            case STEVAL_STWINKIT1:
            case STEVAL_STWINKT1B:
            case B_L475E_IOT01A:
            case B_U585I_IOT02A:
            case ASTRA1:
            case SENSOR_TILE_BOX_PRO:
            case STWIN_BOX:
            case PROTEUS:
            case STDES_CBMLORABLE:
                return new FwVersionConsoleNucleo(debug);
        }
        return null;
    }

    /**
     * object where notify the command response
     */
    protected FwVersionCallback mCallback;

    /**
     * @param callback object where notify the command answer
     */
    protected FwVersionConsole(FwVersionCallback callback) {
        mCallback = callback;
    }

    /**
     * ask to the node the firmware version, the result will be notify using the method:
     * {@link FwVersionConsole.FwVersionCallback#onVersionRead(FwVersionConsole, int, FwVersion)}
     *
     * @param type version to read
     * @return true if the command is correctly send
     */
    abstract public boolean readVersion(@FirmwareType int type);


    /**
     * change the object where notify the commands answer
     *
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(FwVersionCallback callback) {
        mCallback = callback;
    }

    /**
     * Interface with the callback for the  command send by this class
     */
    public interface FwVersionCallback {


        /**
         * called when the node respond to the readVersion command
         *
         * @param console object where the readVersion was called
         * @param type    version read
         * @param version object with the version read, if some error happen it will be null
         */
        void onVersionRead(FwVersionConsole console, @FirmwareType int type, @Nullable FwVersion version);

    }

}