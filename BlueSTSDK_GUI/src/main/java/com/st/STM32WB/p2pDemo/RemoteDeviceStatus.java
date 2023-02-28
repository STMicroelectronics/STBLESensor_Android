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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Class containing the remote device status
 */
class RemoteDeviceStatus implements Parcelable{

    /**
     * name of the remote device
     */
    Peer2PeerDemoConfiguration.DeviceID id;

    /**
     * true if the light is on, false otherwise, default value is false
     */
    boolean ledStatus;

    /**
     * true if the button was pressed, false otherwise, default value is false
     */
    boolean buttonStatus;

    RemoteDeviceStatus(@NonNull Peer2PeerDemoConfiguration.DeviceID id) {
        this.id = id;
        ledStatus=false;
        buttonStatus=false;
    }

    RemoteDeviceStatus(RemoteDeviceStatus status){
        id = status.id;
        ledStatus = status.ledStatus;
        buttonStatus = status.buttonStatus;
    }

    private RemoteDeviceStatus(Parcel in) {
        id = (Peer2PeerDemoConfiguration.DeviceID) in.readSerializable();
        ledStatus = in.readByte() != 0;
        buttonStatus = in.readByte() != 0;
    }

    public static final Creator<RemoteDeviceStatus> CREATOR = new Creator<RemoteDeviceStatus>() {
        @Override
        public RemoteDeviceStatus createFromParcel(Parcel in) {
            return new RemoteDeviceStatus(in);
        }

        @Override
        public RemoteDeviceStatus[] newArray(int size) {
            return new RemoteDeviceStatus[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteDeviceStatus status = (RemoteDeviceStatus) o;

        return ledStatus == status.ledStatus &&
                buttonStatus == status.buttonStatus &&
                id == status.id;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ledStatus ? 1 : 0);
        result = 31 * result + (buttonStatus ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(id);
        dest.writeByte((byte) (ledStatus ? 1 : 0));
        dest.writeByte((byte) (buttonStatus ? 1 : 0));
    }
}