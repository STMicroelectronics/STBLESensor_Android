/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
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

/**
 * The feature will contain the device position
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureCarryPosition extends Feature {
    public static final String FEATURE_NAME = "Carry Position";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = "Position";
    public static final float DATA_MAX = 6;
    public static final float DATA_MIN = 0;

    /**
     * Enum containing the possible result of the carry position detection
     */
    public enum Position {
        /** we don't have enough data for decide */
        UNKNOWN,
        /** the device is on the table */
        ON_DESK,
        /** the device is on the user hand */
        IN_HAND,
        /** the device is near the user head */
        NEAR_HEAD,
        /** the device is on the user shirt pocket */
        SHIRT_POCKET,
        /** the device is on the trousers pocket */
        TROUSERS_POCKET,
        /** the device is on the user hand/arm an the user is swinging its arm*/
        ARM_SWING,
        /** unknown status */
        ERROR
    }//Position

    /**
     * extract the position from a sensor sample
     * @param sample data read from the node
     * @return position detected by the node
     */
    public static Position getPosition(Sample sample){
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[0] != null){
                    int activityId = sample.data[0].byteValue();
                    switch (activityId){
                        case 0x00:
                            return Position.UNKNOWN;
                        case 0x01:
                            return Position.ON_DESK;
                        case 0x02:
                            return Position.IN_HAND;
                        case 0x03:
                            return Position.NEAR_HEAD;
                        case 0x04:
                            return Position.SHIRT_POCKET;
                        case 0x05:
                            return Position.TROUSERS_POCKET;
                        case 0x06:
                            return Position.ARM_SWING;
                        default:
                            return Position.ERROR;
                    }//switch
                }//if
            //if
        //if sample!=null
        return Position.ERROR;
    }//getPosition

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureCarryPosition(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX,DATA_MIN)
        });
    }//FeatureCarryPosition

    /**
     * read a byte with the carry position data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the carry information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if (data.length - dataOffset < 1)
            throw new IllegalArgumentException("There are no 1 byte available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                data[dataOffset]
        },getFieldsDesc());
        return new ExtractResult(temp,1);
    }//extractData

}
