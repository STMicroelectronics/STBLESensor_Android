/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
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
package com.st.BlueSTSDK.Features;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureFitnessActivity extends Feature {

    public static final String FEATURE_NAME = "Fitness Activity";
    private static final Field[] FEATURE_FIELDS = new Field[]{
            new Field("Activity",null, Field.Type.UInt8,0,0xFF),
            new Field("ActivityCounter",null, Field.Type.UInt16,0,0xFFFF)
    };

    public enum ActivityType{
        NO_ACTIVITY((byte)0x00),
        BICEP_CURL((byte)0x01),
        SQUAT((byte)0x02),
        PUSH_UP((byte)0x03);

        public final byte id;

        ActivityType(byte id){
            this.id = id;
        }

        static ActivityType fromRawId(byte id){
            for (ActivityType type: ActivityType.values()) {
                if(type.id == id){
                    return type;
                }
            }
            return ActivityType.NO_ACTIVITY;
        }

    }


    public static ActivityType getActivity(Sample sample){
        if(hasValidIndex(sample,0)){
            byte rawValue = sample.data[0].byteValue();
            return  ActivityType.fromRawId(rawValue);
        }
        return ActivityType.NO_ACTIVITY;
    }

    public static int getActivityCount(Sample sample){
        if(hasValidIndex(sample,1)){
            return  sample.data[1].intValue();
        }//if
        return -1;
    }//getPosition

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureFitnessActivity(Node n) {
        super(FEATURE_NAME, n, FEATURE_FIELDS);
    }//FeatureMotionAlgorithm

    public void enableActivity(ActivityType activity){
        writeData(new byte[]{activity.id});
    }

    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if (data.length - dataOffset < 3)
            throw new IllegalArgumentException("There are no 3 byte available to read");

        final  byte activityId = data[dataOffset];
        final int count = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+1);

        Sample temp = new Sample(timestamp,new Number[]{
                activityId,
                count
        },getFieldsDesc());
        return new ExtractResult(temp,3);
    }//extractData

}
