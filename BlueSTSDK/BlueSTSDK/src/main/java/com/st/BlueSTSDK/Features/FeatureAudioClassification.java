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
package com.st.BlueSTSDK.Features;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeatureAudioClassification extends Feature {

    public static final String FEATURE_NAME = "Audio Classification";
    public static final String FEATURE_UNIT = "#";
    public static final String[] FEATURE_DATA_NAME = {"Audio Class", "Algorithm"};

    /**
     * Enum containing the possible result of the audio classification algorithm
     */
    public enum AudioClass {
        /**
         * unknown state
         */
        UNKNOWN,
        /**
         * audio classification is "indoor"
         */
        INDOOR,
        /**
         * audio classification is "outdoor"
         */
        OUTDOOR,
        /**
         * audio classification is "in-vehicle"
         */
        IN_VEHICLE,
        /**
         * audio classification is "baby is crying"
         */
        BABY_IS_CRYING,
        /**
         * For understanding when ASC is not running
         */
        ASC_OFF,
        /**
         * For understanding when ASC is running
         */
        ASC_ON,
        /**
         * invalid state
         */
        ERROR
    }//Position

    /**
     * extract the classification from a sensor sample
     * @param sample data read from the node
     * @return classification detected by the node
     */
    public static AudioClass getAudioClass(Sample sample){
        if(hasValidIndex(sample,0)){
            int activityId = sample.data[0].byteValue();
            switch (activityId){
                case -1:
                    return AudioClass.UNKNOWN;
                case 0x00:
                    return AudioClass.INDOOR;
                case 0x01:
                    return AudioClass.OUTDOOR;
                case 0x02:
                    return AudioClass.IN_VEHICLE;
                case 0x03:
                    return AudioClass.BABY_IS_CRYING;
                case -16:
                    return AudioClass.ASC_OFF;
                case -15:
                    return AudioClass.ASC_ON;
                default:
                    return AudioClass.ERROR;
            }//switch
        }//if
        //else
        return AudioClass.ERROR;
    }//getGesture

    public static short getAlgorithmType(Sample sample){
        if(hasValidIndex(sample,1)){
            return sample.data[1].shortValue();
        }
        return 0;
    }


    /**
     * build a scene feature
     * @param n node that will send data to this feature
     */
    public FeatureAudioClassification(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[0], FEATURE_UNIT, Field.Type.UInt8,
                        0xFF,0),
                new Field(FEATURE_DATA_NAME[1], FEATURE_UNIT, Field.Type.UInt8,
                        0xFF,0),
        });
    }//FeatureAudioClassification


    private ExtractResult extractAudioClass(long timestamp, byte[] data, int dataOffset){
        Sample temp = new Sample(timestamp, new Number[]{
                data[dataOffset]
        }, getFieldsDesc());
        return new ExtractResult(temp, 1);
    }

    private ExtractResult extractAudioClassAndAlgorithm(long timestamp, byte[] data, int dataOffset){
        Sample temp = new Sample(timestamp, new Number[]{
                data[dataOffset],
                NumberConversion.byteToUInt8(data,dataOffset+1)
        }, getFieldsDesc());
        return new ExtractResult(temp, 2);
    }

    /**
     * read a byte with the Audio Classification data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the Audio Classification id)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        int byteAvailable = data.length - dataOffset;
        if (byteAvailable < 1)
            throw new IllegalArgumentException("There are no 1 byte available to read");
        if(byteAvailable == 1 ) {
            return extractAudioClass(timestamp,data,dataOffset);
        }else{
            return extractAudioClassAndAlgorithm(timestamp,data,dataOffset);
        }
    }//extractData

    @NonNull
    @Override
    public String toString(){
        Sample sample = mLastSample;
        if(sample != null) {
            return FEATURE_NAME+":\n"+
                    "\tTimestamp: "+ sample.timestamp+"\n" +
                    "\tAudio Class: "+ getAudioClass(sample)+"\n" +
                    "\tAlgorithm: "+ getAlgorithmType(sample);
        }else{
            return super.toString();
        }
    }//toString
}
