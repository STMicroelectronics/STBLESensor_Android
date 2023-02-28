/* Copyright (c) 2017  STMicroelectronics – All rights reserved
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
package com.st.BlueNRG.fwUpgrade.feature;


import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class ImageFeature extends DeviceTimestampFeature {
    private static final String FEATURE_NAME = "MemoryInfo";
    /** name of the exported data */
    private static final String[] FEATURE_DATA_NAME = {"Flash_LB", "Flash_UB", "ProtocolVerMajor","ProtocolVerMinor"};
    /** max value of one component*/
    private static final long DATA_MAX = 0xFFFFFFFF;
    /** min value of one component*/
    private static final long DATA_MIN = 0;

    private static final int FLASH_LB_INDEX = 0;
    private static final int FLASH_UB_INDEX = 1;
    private static final int PROTOCOL_VAR_MAJOR_INDEX = 2;
    private static final int PROTOCOL_VER_MINOR_INDEX = 3;

    public ImageFeature(Node n){
        super(FEATURE_NAME,n,new Field[]{
                new Field(FEATURE_DATA_NAME[FLASH_LB_INDEX],null, Field.Type.UInt32,DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[FLASH_UB_INDEX],null, Field.Type.UInt32,DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[PROTOCOL_VAR_MAJOR_INDEX],null, Field.Type.UInt8,255,DATA_MIN),
                new Field(FEATURE_DATA_NAME[PROTOCOL_VER_MINOR_INDEX],null, Field.Type.UInt8,255,DATA_MIN)
        });
    }

    public static long getFlashLowerBound(Sample s){
        if(hasValidIndex(s, FLASH_LB_INDEX))
            return s.data[FLASH_LB_INDEX].longValue();
        //else
        return DATA_MAX;
    }

    public static long getFlashUpperBound(Sample s){
        if(hasValidIndex(s,FLASH_UB_INDEX))
            return s.data[FLASH_UB_INDEX].longValue();
        //else
        return DATA_MIN;
    }

    public @Nullable FwVersion getProtocolVer(Sample s){
        if(hasValidIndex(s,PROTOCOL_VAR_MAJOR_INDEX)&& hasValidIndex(s,PROTOCOL_VER_MINOR_INDEX)){
            return new FwVersion(s.data[PROTOCOL_VAR_MAJOR_INDEX].byteValue(),
                    s.data[PROTOCOL_VER_MINOR_INDEX].byteValue(),0);
        }
        return null;
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        final int availableData = data.length - dataOffset;
        int readData = 8;
        if ( availableData < readData)
            throw new IllegalArgumentException("There are byte available to read");

        long flash_LB = NumberConversion.BigEndian.bytesToUInt32(data,dataOffset);
        long flash_UB = NumberConversion.BigEndian.bytesToUInt32(data,dataOffset+4);

        int versionMajour = 1, versionMinor = 0;
        if(availableData >=9){
            versionMajour = data[dataOffset+8] / 16;
            versionMinor = data[dataOffset+8] % 16;
            readData++;
        }

        return new ExtractResult(
                new Sample(new Number[]{flash_LB, flash_UB,versionMajour,versionMinor},
                getFieldsDesc()), readData);

    }
}
