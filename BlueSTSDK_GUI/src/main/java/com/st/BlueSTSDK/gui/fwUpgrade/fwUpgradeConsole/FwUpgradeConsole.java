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
package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;


import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueNRG.fwUpgrade.FwUpgradeConsoleBlueNRG;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware;
import com.st.BlueSTSDK.fwDataBase.db.BoardFotaType;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;

import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.STM32WB.fwUpgrade.FwUpgradeConsoleSTM32WB;
import com.st.STM32WBA.FwUpgradeConsoleSTM32WBA;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface to implement for upload a file into the node, usually for upgrade the firmware.
 * it can handle the upgrade of the node firmware and the bluetooth firmware
 */
public abstract class FwUpgradeConsole {

    private static FwVersionBoard STBOX_NEW_FW_UPGRADE_PROTOCOL = new FwVersionBoard("SENSORTILE.BOX","L4R9",3,0,15);

    private static boolean stBoxHasNewFwUpgradeProtocol(@Nullable FwVersion version){
        if (version instanceof FwVersionBoard){
            return ((FwVersionBoard) version).getName().equals(STBOX_NEW_FW_UPGRADE_PROTOCOL.getName()) &&
                    version.compareTo(STBOX_NEW_FW_UPGRADE_PROTOCOL)>=0;
        }
        return false;
    }

    /**
     * get an instance of this class that works with the node
     * @param node node where upload the firmware
     * @return null if isn't possible upload the firmware in the node, or a class for do it
     */
    static public @Nullable FwUpgradeConsole getFwUpgradeConsole(@NonNull Context appCtx, @NonNull Node node, @Nullable FwVersion version){
        if(node.getType() == Node.Type.WBA_BOARD) {
            return FwUpgradeConsoleSTM32WBA.buildForNode(node);
        }
        FwUpgradeConsoleSTM32WB stm32wbConsole = FwUpgradeConsoleSTM32WB.buildForNode(node);
        if( stm32wbConsole!=null)
            return stm32wbConsole;
        FwUpgradeConsoleBlueNRG blueNrgConsole = FwUpgradeConsoleBlueNRG.buildForNode(node);
        if( blueNrgConsole!=null)
            return blueNrgConsole;

        Debug debug = node.getDebug();

        if(debug !=null) {
            Log.d("fwUpgrade","LastMTU Before Upgrade="+debug.getNode().getLastMtu());

            BoardFirmware fw_details = node.getFwDetails();

            switch (node.getType()) {
                case SENSOR_TILE_BOX:
                    if(stBoxHasNewFwUpgradeProtocol(version)){
                        //"Special" Fota for SensorTile.box official Fw
                        return new FwUpgradeConsoleBox(debug);
                    }else{
                        //Normal Fota
                        return new FwUpgradeConsoleNucleo(debug,fw_details);
                    }
                case NUCLEO:
                case NUCLEO_F401RE:
                case NUCLEO_L053R8:
                case NUCLEO_L476RG:
                case NUCLEO_F446RE:
                case SENSOR_TILE:
                case BLUE_COIN:
                case STEVAL_BCN002V1:
                case STEVAL_STWINKIT1:
                case STEVAL_STWINKT1B:
                case STWIN_BOX:
                case SENSOR_TILE_BOX_PRO:
                case DISCOVERY_IOT01A:
                    //Normal Fota
                    return new FwUpgradeConsoleNucleo(debug,fw_details);
            }
        }
        return  null;
    }


    /**
     * object where notify the command response
     */
    protected FwUpgradeCallback mCallback;

    /**
     *
     * @param callback object where notify the command answer
     */
    protected FwUpgradeConsole(FwUpgradeCallback callback) {
        mCallback = callback;
    }

    /**
     * upload the file into the node
     * @param type type of firmware that we are going to upload
     * @param fwFile file path
     * @param startingAddress memory address where load the firmware file
     * @return true if the command is correctly start
     */
    abstract public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFile, long startingAddress);

    public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFile){
        return loadFw(type,fwFile,0);
    }

    /**
     * change the object where notify the commands answer
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(FwUpgradeCallback callback) {
        mCallback = callback;
    }

    /**
     * Interface with the callback for the  command send by this class
     */
    public interface FwUpgradeCallback{

        /**
         * enum with the possible upload error
         */
        @IntDef({ERROR_CORRUPTED_FILE, ERROR_TRANSMISSION,ERROR_INVALID_FW_FILE,ERROR_WRONG_SDK_VERSION,ERROR_WRONG_SDK_VERSION_OR_ERROR_TRANSMISSION,ERROR_UNKNOWN,ERROR_NOT_READY_TO_RECEIVE})
        @Retention(RetentionPolicy.SOURCE)
        @interface UpgradeErrorType {}

        /**
         * error fired when the crc computed in the node isn't equal to the one computed on the
         * mobile.
         * this can happen when there are some error during the transmission
         */
        int ERROR_CORRUPTED_FILE = 0;

        /**
         * error fired when is not possible upload all the file
         */
        int ERROR_TRANSMISSION = 1;

        /**
         * error fired when is not possible open the file to upload
         */
        int ERROR_INVALID_FW_FILE=2;

        /**
         * the node firmware has a wrong version
         */
        int ERROR_WRONG_SDK_VERSION=3;

        int ERROR_WRONG_SDK_VERSION_OR_ERROR_TRANSMISSION=4;

        /**
         * unknown error
         */
        int ERROR_UNKNOWN=5;

        int ERROR_NOT_READY_TO_RECEIVE=6; // added for WBA ; when a wrong address is given, the board sends back a 0x03 not ready to receive file

        /**
         * called when the loadFw finish correctly
         * @param console object where loadFw was called
         * @param fwFile file upload to the node
         */
        void onLoadFwComplete(FwUpgradeConsole console, FwFileDescriptor fwFile);

        /**
         * called when the loadFw finish with an error
         * @param console object where loadFw was called
         * @param fwFile file upload fail
         * @param error error happen during the upload
         */
        void onLoadFwError(FwUpgradeConsole console, FwFileDescriptor fwFile,
                           @UpgradeErrorType int error);

        /**
         * function called for notify to the user that the uploading is running
         * @param console object where loadFw was called
         * @param fwFile file that we are uploading
         * @param loadBytes bytes loaded to the board
         */
        void onLoadFwProgressUpdate(FwUpgradeConsole console, FwFileDescriptor fwFile, long loadBytes);
    }

}
