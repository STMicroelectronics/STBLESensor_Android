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
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

public class FeatureMotionAlgorithm extends Feature {
    public static final String FEATURE_NAME = "Motion Algorithm";
    private static final Field[] FEATURE_FIELDS = new Field[]{
            new Field("AlgorithmId",null, Field.Type.UInt8,0,0xFF),
            new Field("StatusId",null, Field.Type.UInt8,0,0xFF)
    };


    public enum AlgorithmType{
        UNKNOWN((byte)0x00),
        POSE_ESTIMATION((byte)0x01),
        DESKTOP_TYPE_DETECTION((byte)0x02),
        VERTICAL_CONTEXT((byte)0x03);

        public final byte id;

        AlgorithmType(byte id){
            this.id = id;
        }

        static AlgorithmType fromRawId(byte id){
            for (AlgorithmType type: AlgorithmType.values()) {
                if(type.id == id){
                    return type;
                }
            }
            return AlgorithmType.UNKNOWN;
        }

    }


    public static AlgorithmType getAlgorithm(Sample sample){
        if(hasValidIndex(sample,0)){
            byte rawValue = sample.data[0].byteValue();
            return AlgorithmType.fromRawId(rawValue);
        }
        return AlgorithmType.UNKNOWN;
    }

    /**
     * Enum containing the possible result of the carry position detection
     */
    public enum Pose {
        UNKNOWN,
        SITTING,
        STANDING,
        LYING_DOWN
    }//Pose

    /**
     * extract the position from a sensor sample
     * @param sample data read from the node
     * @return position detected by the node
     */
    public static Pose getPose(Sample sample){
        if(hasValidIndex(sample,1) &&
            getAlgorithm(sample) == AlgorithmType.POSE_ESTIMATION){
            int poseId = sample.data[1].byteValue();
            switch (poseId){
                case 0x00:
                    return Pose.UNKNOWN;
                case 0x01:
                    return Pose.SITTING;
                case 0x02:
                    return Pose.STANDING;
                case 0x03:
                    return Pose.LYING_DOWN;
                default:
                    return Pose.UNKNOWN;

            }//switch
        }//if
        return Pose.UNKNOWN;
    }//getPosition


    /**
     * Enum containing the possible result of the carry position detection
     */
    public enum VerticalContext {
        /** In case of no pressure sensor data or reliable data */
        UNKNOWN,
        /** When walking on flat surface */
        FLOOR,
        /** If significant change observed in height but not sufficient to declare as stairs or elevator */
        UP_DOWN,
        /** Detection of stairs */
        STAIRS,
        /** Elevator */
        ELEVATOR,
        /** Detect Escalator provided no walking on escalator */
        ESCALATOR
    }//Position

    /**
     * extract the position from a sensor sample
     * @param sample data read from the node
     * @return position detected by the node
     */
    public static VerticalContext getVerticalContext(Sample sample){
        if(hasValidIndex(sample,1) &&
                getAlgorithm(sample) == AlgorithmType.VERTICAL_CONTEXT){
            int activityId = sample.data[1].byteValue();
            switch (activityId){
                case 0x00:
                    return VerticalContext.UNKNOWN;
                case 0x01:
                    return VerticalContext.FLOOR;
                case 0x02:
                    return VerticalContext.UP_DOWN;
                case 0x03:
                    return VerticalContext.STAIRS;
                case 0x04:
                    return VerticalContext.ELEVATOR;
                case 0x05:
                    return VerticalContext.ESCALATOR;
                default:
                    return VerticalContext.UNKNOWN;

            }//switch
        }//if
        return VerticalContext.UNKNOWN;
    }//getPosition

    /**
     * Enum containing the possible result of the carry position detection
     */
    public enum DesktopType {
        UNKNOWN,
        SITTING,
        STANDING,
    }//Pose

    /**
     * extract the position from a sensor sample
     * @param sample data read from the node
     * @return position detected by the node
     */
    public static DesktopType getDesktopType(Sample sample){
        if(hasValidIndex(sample,1) &&
                getAlgorithm(sample) == AlgorithmType.DESKTOP_TYPE_DETECTION){
            int poseId = sample.data[1].byteValue();
            switch (poseId){
                case 0x00:
                    return DesktopType.UNKNOWN;
                case 0x01:
                    return DesktopType.SITTING;
                case 0x02:
                    return DesktopType.STANDING;
                default:
                    return DesktopType.UNKNOWN;

            }//switch
        }//if
        return DesktopType.UNKNOWN;
    }//getPosition

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureMotionAlgorithm(Node n) {
        super(FEATURE_NAME, n, FEATURE_FIELDS);
    }//FeatureMotionAlgorithm

    public void enableAlgorithm(AlgorithmType algo){
        Log.d("Motion","set algo: "+algo.id);
        writeData(new byte[]{algo.id});
    }

    /**
     * read a byte with the carry position data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the motion information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 byte available to read");

        final  byte algoId = data[dataOffset];
        final  byte statusId = data[dataOffset+1];

        Sample temp = new Sample(timestamp,new Number[]{
                algoId,
                statusId
        },getFieldsDesc());
        return new ExtractResult(temp,2);
    }//extractData

}
