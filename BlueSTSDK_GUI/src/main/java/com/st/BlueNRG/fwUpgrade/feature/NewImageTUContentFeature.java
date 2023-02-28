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

import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

import com.st.BlueSTSDK.Utils.NumberConversion;

public class NewImageTUContentFeature extends DeviceTimestampFeature {

    private static final String FEATURE_NAME = "Write byte sequence";
    private static final int OTA_SUPPORT_INFO_SIZE = 4; // Sequence Number (2 bytes), NeedsAcks (1 byte), Checksum (1 byte)

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public NewImageTUContentFeature(Node n) {
        super(FEATURE_NAME,n,new Field[]{
                new Field("ExpectedWriteLength",null, Field.Type.UInt8,255,0)
        });
    }

    public static int getExpectedWriteLength(Sample s){
        if(hasValidIndex(s,0)){
            return s.data[0].intValue();
        }
        return -1;
    }

    private byte checkSum(byte message[], int start,int destPos){
        byte checksum = 0;
        for (int i=start; i<destPos;i++){
            checksum ^= message[i];
        }
        return checksum;
    }

    public void upload(Runnable onLastWriteSequenceDone,byte imageToSend[] ,byte OTA_ACK_EVERY,byte lastOTA_ACK_EVERY, int fw_image_packet_size, short SeqNum){
        int Write_Data_Len = fw_image_packet_size + OTA_SUPPORT_INFO_SIZE;// Set number of bytes sent on a single write without response
        byte payload[] = new byte[fw_image_packet_size];
        byte temp[];
        int end = (lastOTA_ACK_EVERY-(SeqNum+1)%OTA_ACK_EVERY)%lastOTA_ACK_EVERY+1; // ok????
        for (int i=0; i<end;i++){
            byte message[] = new byte[Write_Data_Len];
            int destPos = 0;
            byte needsAck;
            System.arraycopy(imageToSend,SeqNum*fw_image_packet_size,payload,0,fw_image_packet_size);

            // prepare message
            // checksum:1 byte + payload:fw_image_packet_size byte +  needsAck:1 byte + SeqNum:2 byte
            destPos +=1;//checksum
            int start = destPos;
            System.arraycopy(payload,0,message,destPos,payload.length);
            destPos +=fw_image_packet_size;//payload
            if(i==(end-1))
                needsAck = 1;
            else
                needsAck = 0;
            message[destPos] = needsAck;
            destPos +=1;//needsAck
            temp = NumberConversion.LittleEndian.uint16ToBytes(SeqNum);
            System.arraycopy(temp,0,message,destPos,temp.length);
            destPos +=2;//SeqNum
            byte checksum = checkSum(message,start,destPos);
            message[0] = checksum;

            if(needsAck == 0) {
                writeData(message);
                SeqNum++;
            }else{
                writeData(message,onLastWriteSequenceDone);
            }
        }
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        int numByte = 1; // at least 1
        if (data.length - dataOffset < numByte)
            throw new IllegalArgumentException("There are byte available to read");
        return new ExtractResult(new Sample(new Number[]{data.length},getFieldsDesc()),data.length);
    }
}
