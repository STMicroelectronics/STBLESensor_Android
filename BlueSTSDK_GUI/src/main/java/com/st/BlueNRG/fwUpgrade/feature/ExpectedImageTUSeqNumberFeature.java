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
package com.st.BlueNRG.fwUpgrade.feature;

import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class ExpectedImageTUSeqNumberFeature extends DeviceTimestampFeature {

    private static final String FEATURE_NAME = "Start ack notification";
    /** name of the exported data */
    private static final String[] FEATURE_DATA_NAME = {"NextExpectedCharBlock", "ReadAck"};
    /** max value of one component*/
    private static final short DATA_MAX = (short) 0xFFFF;
    /** min value of one component*/
    private static final short DATA_MIN = 0;

    private static final int NEXT_EXPECTED_INDEX = 0;
    private static final int ERROR_ACK_INDEX = 1;

    public enum ErrorCode {
        FLASH_WRITE_FAILED,
        FLASH_VERIFY_FAILED,
        CHECK_SUM_ERROR,
        SEQUENCE_ERROR,
        NO_ERROR,
        UNKNOWN_ERROR;

        static ErrorCode buildErrorCode(byte ack) {
            switch (ack) {
                case (byte)0xFF:
                    return ErrorCode.FLASH_WRITE_FAILED;
                case (byte)0x3C:
                    return ErrorCode.FLASH_VERIFY_FAILED;
                case (byte)0x0F:
                    return ErrorCode.CHECK_SUM_ERROR;
                case (byte)0xF0:
                    return ErrorCode.SEQUENCE_ERROR;
                case (byte)0:
                    return ErrorCode.NO_ERROR;
                default:
                    return ErrorCode.UNKNOWN_ERROR;
            }
        }
    }//ErrorCode

    public ExpectedImageTUSeqNumberFeature(Node n){
        super(FEATURE_NAME,n,new Field[]{
                new Field(FEATURE_DATA_NAME[NEXT_EXPECTED_INDEX],null, Field.Type.UInt16,DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[ERROR_ACK_INDEX],null, Field.Type.UInt8,255,DATA_MIN)
        });
    }

    public static short getNextExpectedCharBlock(Sample s){
        if(hasValidIndex(s, NEXT_EXPECTED_INDEX))
            return s.data[NEXT_EXPECTED_INDEX].shortValue();
        //else
        return DATA_MAX;
    }

    public static ErrorCode getAck(Sample s){
        if(hasValidIndex(s, ERROR_ACK_INDEX)) {
            byte ack = s.data[ERROR_ACK_INDEX].byteValue();
            return ErrorCode.buildErrorCode(ack);
        }
        return ErrorCode.UNKNOWN_ERROR;
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        int numByte = 3;

        if (data.length - dataOffset < numByte)
            throw new IllegalArgumentException("There are byte available to read");

        int nextExpectedCharBlock = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset); // unsigned short is saved as int
        byte errorAck = data[dataOffset+2];

        return new ExtractResult(new Sample(new Number[]{nextExpectedCharBlock,errorAck},getFieldsDesc()),numByte);
    }
}
