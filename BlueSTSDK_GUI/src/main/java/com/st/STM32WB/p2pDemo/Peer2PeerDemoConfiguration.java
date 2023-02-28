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
package com.st.STM32WB.p2pDemo;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.UUIDToFeatureMap;
import com.st.BlueSTSDK.fwDataBase.db.BoardFotaType;
import com.st.STM32WB.p2pDemo.feature.FeatureControlLed;
import com.st.STM32WB.p2pDemo.feature.FeatureNetworkStatus;
import com.st.STM32WB.p2pDemo.feature.FeatureSwitchStatus;
import com.st.STM32WB.p2pDemo.feature.FeatureProtocolRadioReboot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Class containing the settings and common structure for the Peer2Peer (P2P) stm32wb demo
 */
public class Peer2PeerDemoConfiguration {

    private static final Map<Byte,DeviceID> BOARDID_TO_DEVICEID;


     static {
        BOARDID_TO_DEVICEID = new HashMap<>();
        BOARDID_TO_DEVICEID.put((byte)0x83,DeviceID.DEVICE_1);
        BOARDID_TO_DEVICEID.put((byte)0x84,DeviceID.DEVICE_2);
        BOARDID_TO_DEVICEID.put((byte)0x87,DeviceID.DEVICE_3);
        BOARDID_TO_DEVICEID.put((byte)0x88,DeviceID.DEVICE_4);
        BOARDID_TO_DEVICEID.put((byte)0x89,DeviceID.DEVICE_5);
        BOARDID_TO_DEVICEID.put((byte)0x8A,DeviceID.DEVICE_6);
    }

    /**
     * id used for the router node
     */
    public static final Set<Byte> WB_DEVICE_NODE_IDS =BOARDID_TO_DEVICEID.keySet();

    /**
     * id used for the router node
     */
    public static final byte WB_ROUTER_NODE_ID =(byte)0x85;

    /**
     * tell if the node is a valid node for the P2P demo
     * @param node node to test
     * @return true if the node is manage by this demo
     */
    public static boolean isValidNode(@NonNull Node node){
        return isValidDeviceNode(node) || isValidRouterNode(node);
    }

    public static boolean isValidDeviceNode(@NonNull Node node){
        byte nodeId = node.getTypeId();
        boolean valid=false;
        if(node.getType() == Node.Type.WB_BOARD) {
            if(WB_DEVICE_NODE_IDS.contains(nodeId)) {
                valid=true;
            }
        } else if(node.getType() == Node.Type.WBA_BOARD) {
            valid=true;
        } else {
            //For allowing Proteus and Polaris to use also the WB Functionalities..
            if(node.getProtocolVersion()==0x02){
                if(node.getFwDetails()!=null) {
                    if(node.getFwDetails().getFota().getType()!=null) {
                        if (node.getFwDetails().getFota().getType() == BoardFotaType.wb_mode) {
                            valid = true;
                        }
                    }
                }
            }
        }
        return valid;
    }

    public static boolean isValidRouterNode(@NonNull Node node){
        byte nodeId = node.getTypeId();
        return node.getType() == Node.Type.WB_BOARD &&
                (WB_ROUTER_NODE_ID == nodeId);
    }

    /**
     * map the characteristics and the feature used by this demo
     * @return map containing the characteristics and feature used by this demo
     */
    public static UUIDToFeatureMap getCharacteristicMapping(){
        UUIDToFeatureMap temp = new UUIDToFeatureMap();
        temp.put(UUID.fromString("0000fe41-8e22-4541-9d4c-21edae82ed19"),
                Arrays.asList(FeatureControlLed.class, FeatureProtocolRadioReboot.class));
        temp.put(UUID.fromString("0000fe42-8e22-4541-9d4c-21edae82ed19"), FeatureSwitchStatus.class);
        temp.put(UUID.fromString("0000fe51-8e22-4541-9d4c-21edae82ed19"), FeatureNetworkStatus.class);
        return temp;
    }

    /**
     * enum containing the different device id
     */
    public enum DeviceID {

        /** The End Device 1 */
        DEVICE_1((byte) 0x01),
        /** The End Device 2 */
        DEVICE_2((byte) 0x02),
        /** The End Device 3 */
        DEVICE_3((byte) 0x03),
        /** The End Device 4 */
        DEVICE_4((byte) 0x04),
        /** The End Device 5 */
        DEVICE_5((byte) 0x05),
        /** The End Device 6 */
        DEVICE_6((byte) 0x06),
        /** all the devices  */
        ALL((byte) 0x00),
        /** Invalid value */
        UNKNOWN((byte) 0xFF);

        private byte deviceId;

        DeviceID(byte id){
            deviceId = id;
        }

        public byte getId() {
            return deviceId;
        }

        public static DeviceID fromBoardId(byte id){
            return BOARDID_TO_DEVICEID.get(id);
        }

    }//DeviceSelection

}
