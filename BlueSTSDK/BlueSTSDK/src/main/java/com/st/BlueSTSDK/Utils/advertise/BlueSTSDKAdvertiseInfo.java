/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentatio
 *      n
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.Utils.advertise;

import com.st.BlueSTSDK.Node.Type;

public class BlueSTSDKAdvertiseInfo implements BleAdvertiseInfo {

    /**
     * device name
     */
    private final String mName;
    /**
     * device tx power
     */
    private final byte mTxPower;
    /**
     * device mac address
     */
    private final String mAddress;

    /**
     * - for BlueSTSDK V1 -> bit map that tell us the available features
     * - for BlueSTSDK V2 -> Option bytes
     */
    private final long mOptByte_FeatureMap;
    /**
     * device id
     */
    private final byte mDeviceId;
    /**
     * Device ProtocolVersion (it is an unsigned char )
     */
    private final short mProtocolVersion;

    /**
     * board type -> is a super class of the device id
     */
    private final Type mBoardType;


    /**
     * board is in sleeping state
     */
    private final boolean mBoardSleeping;

    /**
     * board has general purpose info
     */
    private final boolean mHasGeneralPurpose;

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public byte getTxPower() {
        return mTxPower;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public long getFeatureMap() {
        if(mProtocolVersion==1) {
            return mOptByte_FeatureMap;
        } else {
            return 0;
        }
    }

    @Override
    public long getOptionBytes() {
        if(mProtocolVersion==1) {
            return 0;
        } else {
            return mOptByte_FeatureMap;
        }
    }

    @Override
    public byte getDeviceId() {
        return mDeviceId;
    }

    @Override
    public short getProtocolVersion() {
        return mProtocolVersion;
    }

    @Override
    public Type getBoardType() {
        return mBoardType;
    }

    @Override
    public boolean isBoardSleeping() {
        return mBoardSleeping;
    }

    @Override
    public boolean isHasGeneralPurpose() {
        return mHasGeneralPurpose;
    }

    public BlueSTSDKAdvertiseInfo(String mName, byte mTxPower, String mAddress, long mOptByte_FeatureMap, byte mDeviceId, short mProtocolVersion, Type mBoardType, boolean mBoardSleeping, boolean mHasGeneralPurpose) {
        this.mName = mName;
        this.mTxPower = mTxPower;
        this.mAddress = mAddress;
        this.mOptByte_FeatureMap = mOptByte_FeatureMap;
        this.mDeviceId = mDeviceId;
        this.mProtocolVersion = mProtocolVersion;
        this.mBoardType = mBoardType;
        this.mBoardSleeping = mBoardSleeping;
        this.mHasGeneralPurpose = mHasGeneralPurpose;
    }


}
