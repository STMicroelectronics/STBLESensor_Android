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
package com.st.STM32WB.fwUpgrade.feature;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.Utils.UUIDToFeatureMap;
import com.st.BlueSTSDK.fwDataBase.ReadBoardFirmwareDataBase;
import com.st.BlueSTSDK.fwDataBase.db.BoardFirmware;
import com.st.BlueSTSDK.fwDataBase.db.BoardFotaType;

import java.util.UUID;

public class STM32OTASupport{

    public static final byte OTA_NODE_ID = (byte)0x86;

    public static boolean isOTANode(@NonNull Node n, @NonNull Context ctx){
        boolean retValue=false;
        if(n.getTypeId() ==  OTA_NODE_ID) {
            retValue = true;
        } else if(n.getProtocolVersion()==0x02) {
            //we need to associate a fw model to the current node
            ReadBoardFirmwareDataBase firmwareDB = new ReadBoardFirmwareDataBase(ctx);

            byte[] optBytes = NumberConversion.BigEndian.uint32ToBytes(n.getAdvertiseOptionBytes());
            short optBytesUnsigned[] = new short[4];
            optBytesUnsigned[0] = NumberConversion.byteToUInt8(optBytes,0);
            optBytesUnsigned[1] = NumberConversion.byteToUInt8(optBytes,1);
            optBytesUnsigned[2] = NumberConversion.byteToUInt8(optBytes,2);
            optBytesUnsigned[3] = NumberConversion.byteToUInt8(optBytes,3);

            BoardFirmware fw_details = firmwareDB.getFwDetailsNode( (short)(n.getTypeId() &  0xFF),
                    optBytesUnsigned[0],
                    optBytesUnsigned[1]);
            if(fw_details!=null) {
                n.setFwDetails(fw_details);
                if(fw_details.getFota().getType()!=null) {
                    if (fw_details.getFota().getType() == BoardFotaType.wb_ready) {
                        retValue = true;
                    }
                }
            }
        }

        return retValue;
    }

    public static UUIDToFeatureMap getOTAFeatures(){
        UUIDToFeatureMap featureMap = new UUIDToFeatureMap();
        featureMap.put(UUID.fromString("0000fe11-8e22-4541-9d4c-21edae82ed19"), RebootOTAModeFeature.class);
        featureMap.put(UUID.fromString("0000fe22-8e22-4541-9d4c-21edae82ed19"), OTAControlFeature.class);
        featureMap.put(UUID.fromString("0000fe23-8e22-4541-9d4c-21edae82ed19"), OTABoardWillRebootFeature.class);
        featureMap.put(UUID.fromString("0000fe24-8e22-4541-9d4c-21edae82ed19"), OTAFileUpload.class);
        return featureMap;
    }

    /**
     * after the board reboot it will have the current address +1
     * @param node current node
     * @return node address/tag that the board will have when it is in ota mode
     */
    public static String getOtaAddressForNode(Node node){
        String currentAddress = node.getTag();

        int lastDigit = Short.valueOf(currentAddress.substring(currentAddress.length()-2),16);

        lastDigit = lastDigit + 1;
        return currentAddress.substring(0,currentAddress.length()-2) +
                //String.format("%X",(byte)lastDigit); //"80:00:12:34:56:01" +1 generates "80:00:12:34:56:2"
                String.format("%02X",(byte)lastDigit); //"80:00:12:34:56:01" +1 generates "80:00:12:34:56:02"
    }

}
