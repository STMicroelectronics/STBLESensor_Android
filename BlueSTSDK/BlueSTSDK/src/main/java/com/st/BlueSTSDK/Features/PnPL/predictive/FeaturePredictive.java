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
package com.st.BlueSTSDK.Features.PnPL.predictive;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

public abstract class FeaturePredictive extends Feature {
    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param name     name of the feature
     * @param n        node that will update this feature
     * @param dataDesc description of the data that belong to this feature
     */
    public FeaturePredictive(String name, Node n, @NonNull Field[] dataDesc) {
        super(name, n, dataDesc);
    }


    public enum Status {
        GOOD,
        WARNING,
        BAD,
        UNKNOWN;

        static Status fromByte(byte value){
            switch (value){
                case 0x00:
                    return GOOD;
                case 0x01:
                    return WARNING;
                case 0x02:
                    return BAD;
                default:
                    return UNKNOWN;
            }
        }
    }

    protected static Status getStatusFromIndex(Sample s, int i){
        if(hasValidIndex(s,i)){
            return Status.fromByte(s.data[i].byteValue());
        }
        return Status.UNKNOWN;
    }

    protected static byte extractXRawStatus(short value){
        return (byte)((value >> 4) & 0x03);
    }

    protected static byte extractYRawStatus(short value){
        return (byte)((value >> 2) & 0x03);
    }

    protected static byte extractZRawStatus(short value){
        return (byte)((value ) & 0x03);
    }

    protected static Field buildStatusFieldNamed(String name){
        return new Field(name,null, Field.Type.UInt8,4,0);
    }
}
