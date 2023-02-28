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
package com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole;

import android.os.Parcel;

import androidx.annotation.IntDef;

import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that store a version for the ble firmware. The version is of type:
 * Ble_x.y.z or BleMS_x.y.z where z is a char where 0=0, a = 1
 * the Ble or BleMS prefix tell which type of ble chip the board is using
 */
public class FwVersionBle extends FwVersion {

    private static final String BLE_TYPE="Ble";
    private static final String BLEMS_TYPE="BleMS";
    private static final Pattern PARSE_FW_VERSION=Pattern.compile("((?:"+BLE_TYPE+")|(?:"+
            BLEMS_TYPE+"))_(\\d+)\\.(\\d+)\\.(\\w)");

    /**
     * enum with the ble chip
     */
    @IntDef({BLE,BLE_MS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BleType {}

    /**
     * constant used when the node is using the BlueNRG chip (as the X-NUCLEO-IDB04A1 board)
     */
    public static final int BLE = 0;

    /**
     *constant used when the node is using the  BlueNRG-MS chip (as the X-NUCLEO-IDB05A1)
     */
    public static final int BLE_MS = 1;

    private @BleType int mType;

    /**
     * convert a char into a number, the '0' is mapped as 0, a as 1 ecc
     * @param c char to convert to integer
     * @return integer version corresponding to the char
     */
    private static int charToPatch(char c){
        if(c=='0')
            return 0;
        else
            return (c-'a')+1;
    }

    /**
     * convert a number into a char version. the 0 is mapped as '0' 1 as 'a' ecc
     * @param v version to convert
     * @return char corresponding the number
     */
    private static char patchToChar(int v){
        if(v==0){
            return '0';
        }else
            return (char) ('a'+(v-1));
    }

    public FwVersionBle(CharSequence version) throws IllegalVersionFormatException {
        Matcher matcher = PARSE_FW_VERSION.matcher(version);
        if (!matcher.matches())
            throw new IllegalVersionFormatException();
        switch (matcher.group(1)){
            case BLE_TYPE:
                mType=BLE;
                break;
            case BLEMS_TYPE:
                mType=BLE_MS;
                break;
        }
        majorVersion = Integer.parseInt(matcher.group(2));
        minorVersion = Integer.parseInt(matcher.group(3));
        patchVersion = charToPatch(matcher.group(4).charAt(0));
    }

    @Override
    public String toString() {
        return ""+majorVersion+"."+minorVersion+"."+patchToChar(patchVersion)+"( "+
                (mType==BLE ? BLE_TYPE : BLEMS_TYPE)+" )";
    }

    public static final Creator<FwVersionBle> CREATOR = new Creator<FwVersionBle>() {
        @Override
        public FwVersionBle createFromParcel(Parcel in) {
            return new FwVersionBle(in);
        }

        @Override
        public FwVersionBle[] newArray(int size) {
            return new FwVersionBle[size];
        }
    };

    protected FwVersionBle(Parcel in) {
        super(in);
        //noinspection ResourceType
        mType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel,i);
        parcel.writeInt(mType);
    }

}
