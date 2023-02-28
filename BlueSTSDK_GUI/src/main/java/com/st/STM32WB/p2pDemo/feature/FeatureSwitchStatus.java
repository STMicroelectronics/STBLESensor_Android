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
package com.st.STM32WB.p2pDemo.feature;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;

/**
 * Class used to notify the button pressed
 */
public class FeatureSwitchStatus extends DeviceTimestampFeature {

    public static final String FEATURE_NAME = "SwitchInfo";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME[] = {"DeviceId","SwitchPressed"};
    public static final float DATA_MAX[] = {6,1};
    public static final float DATA_MIN[] = {0,0};

    /** Index for DEV ID Selection */
    public static int SWITCH_STATUS_DEV_ID_INDEX =0;
    /** Index for DEV ID Selection */
    public static int SWITCH_STATUS_BUTTON_ID_INDEX =1;

    /**
     * extract the Device ID
     * @param sample data read from the node
     * @return Device ID detected by the node
     */
    public static Peer2PeerDemoConfiguration.DeviceID getDeviceSelection(Sample sample){
        if(hasValidIndex(sample, SWITCH_STATUS_DEV_ID_INDEX)){
                int activityId = sample.data[SWITCH_STATUS_DEV_ID_INDEX].byteValue();
                switch (activityId){
                    case 0x01:
                        return Peer2PeerDemoConfiguration.DeviceID.DEVICE_1;
                    case 0x02:
                        return Peer2PeerDemoConfiguration.DeviceID.DEVICE_2;
                    case 0x03:
                        return Peer2PeerDemoConfiguration.DeviceID.DEVICE_3;
                    case 0x04:
                        return Peer2PeerDemoConfiguration.DeviceID.DEVICE_4;
                    case 0x05:
                        return Peer2PeerDemoConfiguration.DeviceID.DEVICE_5;
                    case 0x06:
                        return Peer2PeerDemoConfiguration.DeviceID.DEVICE_6;
                    case 0xFF:
                        return Peer2PeerDemoConfiguration.DeviceID.ALL;
                    default:
                        return Peer2PeerDemoConfiguration.DeviceID.UNKNOWN;
                }//switch
            }//if
        //if
        return Peer2PeerDemoConfiguration.DeviceID.UNKNOWN;
    }//getDeviceSelection

    /**
     * extract the switch state
     * @param sample data read from the node
     * @return true if the switch state is on
     */
    public static boolean isSwitchOn(Sample sample){
        if(hasValidIndex(sample, SWITCH_STATUS_BUTTON_ID_INDEX)) {
            int isPressed = sample.data[SWITCH_STATUS_BUTTON_ID_INDEX].byteValue();
            return isPressed == 0x01;
        }//if
        return false;
    }//getDeviceSelection


    /**
     * Build a button status feature
     * @param n node that will send data to this feature
     */
    public FeatureSwitchStatus(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[SWITCH_STATUS_DEV_ID_INDEX], FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX[SWITCH_STATUS_DEV_ID_INDEX],DATA_MIN[SWITCH_STATUS_DEV_ID_INDEX]),
                new Field(FEATURE_DATA_NAME[SWITCH_STATUS_BUTTON_ID_INDEX], FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX[SWITCH_STATUS_BUTTON_ID_INDEX],DATA_MIN[SWITCH_STATUS_BUTTON_ID_INDEX]),
        });
    }//FeatureSwitchStatus

    /**
     * read a byte with the  data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (2) and data extracted ()
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {

        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 bytes available to read");

        byte deviceId = data[dataOffset];
        byte buttonStatus = data[dataOffset+1];

        Sample temp = new Sample(new Number[]{deviceId,buttonStatus},getFieldsDesc());
        return new ExtractResult(temp,2);

    }//extractData

}
