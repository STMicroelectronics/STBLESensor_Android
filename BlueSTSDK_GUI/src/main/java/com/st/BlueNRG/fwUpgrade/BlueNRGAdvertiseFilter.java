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
package com.st.BlueNRG.fwUpgrade;

import android.util.SparseArray;

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseParser;
import com.st.BlueSTSDK.Utils.advertise.BleAdvertiseInfo;

import java.util.Arrays;
import java.util.UUID;

import static com.st.BlueSTSDK.Utils.advertise.AdvertiseParser.split;

public class BlueNRGAdvertiseFilter implements AdvertiseFilter {

    private static final String DEFAULT_NAME = "BlueNRG OTA";
    private static final byte[] OTA_SERVICE_UUID =
            new byte[] {(byte)0x8a,(byte)0x97,(byte)0xf7,(byte)0xc0,(byte)0x85,(byte)0x06,(byte)0x11,
                    (byte)0xe3,(byte)0xba,(byte)0xa7,(byte)0x08,(byte)0x00,(byte)0x20,(byte)0x0c,(byte)0x9a,
                    (byte)0x66};


    public class BlueNRGAdvertiseInfo implements BleAdvertiseInfo {

        private String mName;
        private UUID mExportedService;

        public BlueNRGAdvertiseInfo(String name, UUID exportedService) {
            mName = name;
            mExportedService = exportedService;
        }

        public UUID getExportedService() {
            return mExportedService;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public byte getTxPower() {
            return 0;
        }

        @Override
        public String getAddress() {
            return null;
        }

        @Override
        public long getFeatureMap() {
            return 0;
        }

        @Override
        public long getOptionBytes() {
            return 0;
        }

        @Override
        public byte getDeviceId() {
            return 4;
        }

        @Override
        public short getProtocolVersion() {
            return 1;
        }

        @Override
        public Node.Type getBoardType() {
            return Node.Type.STEVAL_IDB008VX;
        }

        @Override
        public boolean isBoardSleeping() {
            return false;
        }

        @Override
        public boolean isHasGeneralPurpose() {
            return false;
        }
    }

    private String getDeviceName(SparseArray<byte[]>  advData){
        byte nameData[] = advData.get(AdvertiseParser.DEVICE_NAME_TYPE);
        if(nameData!=null && nameData.length>0)
            return  new String(nameData);
        return DEFAULT_NAME;
    }

    @Nullable
    @Override
    public BleAdvertiseInfo filter(byte[] advData) {
        SparseArray<byte[]> splitAdv = split(advData);
        byte[] exportedService = splitAdv.get(AdvertiseParser.INCOMPLETE_LIST_OF_128_UUID);
        if(exportedService!=null && Arrays.equals(OTA_SERVICE_UUID,exportedService)){
            return new BlueNRGAdvertiseInfo(getDeviceName(splitAdv),
                    UUID.nameUUIDFromBytes(OTA_SERVICE_UUID));
        }
        return null;

    }
}
