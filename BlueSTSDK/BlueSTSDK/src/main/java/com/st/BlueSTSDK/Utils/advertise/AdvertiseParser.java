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

import androidx.annotation.NonNull;
import android.util.SparseArray;

public class AdvertiseParser {

    public final static byte FLAG_DATA_TYPE = 0x01;
    public final static byte INCOMPLETE_LIST_OF_128_UUID = 0x06;
    public final static byte DEVICE_NAME_TYPE = 0x09;
    public final static byte TX_POWER_TYPE = 0x0A;
    public final static byte VENDOR_DATA_TYPE = (byte) 0xff;

    public static SparseArray<byte[]> split(@NonNull byte[] advertise){
        SparseArray<byte[]> splitAdvertise = new SparseArray<>();
        int ptr = 0;
        while (ptr < advertise.length - 2) {
            int length = advertise[ptr++] & 0xff;
            if (length == 0)
                break;

            final byte type = (advertise[ptr++]);
            //min between the length field and the remaining array length
            final int fieldLength = Math.min(length-1,advertise.length-ptr);

            byte[] data = new byte[fieldLength];
            System.arraycopy(advertise,ptr,data,0,fieldLength);
            splitAdvertise.put(type,data);

            ptr += fieldLength;
        }
        return splitAdvertise;
    }

}
