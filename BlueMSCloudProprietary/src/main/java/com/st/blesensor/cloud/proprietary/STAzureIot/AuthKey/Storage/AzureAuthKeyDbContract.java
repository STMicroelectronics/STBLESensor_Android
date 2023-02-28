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

package com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.Storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.AuthToken;


class AzureAuthKeyDbContract {

    /* A entry in the license table */
    static class AuthTokenEntry extends AuthToken implements BaseColumns, Parcelable {

        static final String TABLE_NAME = "AuthToken";
        static final String COLUMN_NAME_DEVICE_ID = "DeviceId";
        static final String COLUMN_NAME_TOKEN = "Token";

        private long id=-1;

        AuthTokenEntry(long id,String deviceId,String token){
            this.id = id;
            setDeviceID(deviceId);
            setToken(token);
        }

        protected AuthTokenEntry(Parcel in) {
            id = in.readLong();
            setDeviceID(in.readString());
            setToken(in.readString());
        }

        public static final Creator<AuthTokenEntry> CREATOR = new Creator<AuthTokenEntry>() {
            @Override
            public AuthTokenEntry createFromParcel(Parcel in) {
                return new AuthTokenEntry(in);
            }

            @Override
            public AuthTokenEntry[] newArray(int size) {
                return new AuthTokenEntry[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(id);
            parcel.writeString(getDeviceID());
            parcel.writeString(getToken());
        }

        public void setId(long id) {
            this.id = id;
        }
    }

}
