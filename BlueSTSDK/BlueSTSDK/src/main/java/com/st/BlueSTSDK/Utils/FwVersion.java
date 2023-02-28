/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
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
package com.st.BlueSTSDK.Utils;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

/**
 * Class that store a program version. The version is stored with 3 numbers, major version,
 * minor version and path version
 */
public class FwVersion implements Parcelable, Comparable<FwVersion>{

    protected int majorVersion;
    protected int minorVersion;
    protected int patchVersion;

    /**
     * create the default version with the value 1.0.0
     */
    public FwVersion(){
        this(1,0,0);
    }

    public FwVersion(int major,int minor,int patch){
        majorVersion=major;
        minorVersion=minor;
        patchVersion=patch;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    @Override
    public String toString() {
        return majorVersion+"."+minorVersion+"."+patchVersion;
    }

    //////////////////////Parcelable Interface ////////////////////////////////////

    public static final Creator<FwVersion> CREATOR = new Creator<FwVersion>() {
        @Override
        public FwVersion createFromParcel(Parcel in) {
            return new FwVersion(in);
        }

        @Override
        public FwVersion[] newArray(int size) {
            return new FwVersion[size];
        }
    };

    protected FwVersion(Parcel in) {
        majorVersion = in.readInt();
        minorVersion = in.readInt();
        patchVersion = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(majorVersion);
        parcel.writeInt(minorVersion);
        parcel.writeInt(patchVersion);
    }

    @Override
    public int compareTo(@NonNull FwVersion version) {
        int diff = majorVersion-version.majorVersion;
        if(diff!=0)
            return diff;
        diff = minorVersion-version.minorVersion;
        if(diff!=0)
            return diff;
        return patchVersion-version.patchVersion;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FwVersion && compareTo((FwVersion) obj) == 0;
    }
}
