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

import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;

import java.util.Arrays;

public class OTAControlFeature extends Feature {

    private static final String FEATURE_NAME = "OTA Control";
    private static final Field[] DATA_DESC = new Field[0];

    private static final byte STOP_COMMAND = 0x00;
    private static final byte START_M0_COMMAND = 0x01;
    private static final byte START_M4_COMMAND = 0x02;
    private static final byte UPLOAD_FINISHED_COMMAND = 0x07;
    private static final byte CANCEL_COMMAND = 0x08;

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public OTAControlFeature(Node n) {
        super(FEATURE_NAME, n, DATA_DESC,false);
    }

    public void startUpload(@FirmwareType int type, long address){
        byte command[] = NumberConversion.BigEndian.uint32ToBytes(address);
        byte first = type == FirmwareType.BLE_FW ? START_M0_COMMAND : START_M4_COMMAND;
        if(getParentNode().getType() == Node.Type.WBA_BOARD) {
            byte[] adaptedCommand = new byte[5];
            adaptedCommand[0] = first;
            System.arraycopy(command, 0, adaptedCommand, 1, 4);
            Log.d("startUpload", "startUpload: "+ Arrays.toString(adaptedCommand));
            writeData(adaptedCommand);
        } else { // WB or other
            command[0] = first;
            Log.d("startUpload", "startUpload: "+ Arrays.toString(command));
            writeData(command);
        }
    }

    public void uploadFinished(Runnable onMessageWrite){
        Log.d("FastFota", "sending upload finished");
        writeData(new byte[]{UPLOAD_FINISHED_COMMAND},onMessageWrite);
    }

    public void cancelUpload(){
        writeData(new byte[]{CANCEL_COMMAND});
    }
    public void stopUpload(){
        writeData(new byte[]{STOP_COMMAND});
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        return new ExtractResult(null,0);
    }
}