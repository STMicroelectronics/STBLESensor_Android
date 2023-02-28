package com.st.BlueSTSDK.Features;

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

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

/**
 * The feature will contain the a detected gesture using data from the mems sensor
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureMemsGesture extends Feature {

    public static final String FEATURE_NAME = "MEMS Gesture";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = "Gesture";
    public static final short DATA_MAX = 3;
    public static final short DATA_MIN = 0;

    /**
     * Enum containing the possible result of the gesture detection
     */
    public enum Gesture {
        /** unknown gesture */
        UNKNOWN,
        /**  pick up gesture*/
        PICK_UP,
        /**  glance gesture*/
        GLANCE,
        /**  wake up gesture*/
        WAKE_UP,
        /** invalid state*/
        ERROR

    }//Position

    /**
     * extract the gesture from a sensor sample
     * @param sample data read from the node
     * @return gesture detected by the node
     */
    public static Gesture getGesture(Sample sample){
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[0] != null){
                    int activityId = sample.data[0].byteValue();
                    switch (activityId){
                        case 0x00:
                            return Gesture.UNKNOWN;
                        case 0x01:
                            return Gesture.PICK_UP;
                        case 0x02:
                            return Gesture.GLANCE;
                        case 0x03:
                            return Gesture.WAKE_UP;
                        default:
                            return Gesture.ERROR;
                    }//switch
                }//if
            //if
        //if sample!=null
        return Gesture.ERROR;
    }//getGesture

    /**
     * build a carry gesture feature
     * @param n node that will send data to this feature
     */
    public FeatureMemsGesture(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX,DATA_MIN)
        });
    }//FeatureGesture

    /**
     * read a byte with the gesture data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the gesture information)
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

    @Override
    public String toString(){
        Sample sample = mLastSample;
        if(sample!=null){
            return FEATURE_NAME+":\n"+
                    "\tTimestamp: "+ sample.timestamp+"\n" +
                    "\tGesture: "+ getGesture(sample);
        }else{
            return super.toString();
        }

    }//toString

}

