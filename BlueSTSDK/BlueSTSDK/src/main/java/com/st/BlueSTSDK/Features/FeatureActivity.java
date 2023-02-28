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
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Feature that will contain the activity detect by the activity recognition code that run on
 * the node
 *<p>
 * the sample data will contain the activity detected by the sensor and the time of when we
 * receive the message.
 *</p>
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureActivity extends Feature {

    public static final String FEATURE_NAME = "Activity Recognition";
    public static final String[] FEATURE_UNIT = {null, "ms", null};
    public static final String[] FEATURE_DATA_NAME = {"Activity", "Date", "Algorithm"};
    public static final float DATA_MAX = 8;
    public static final float DATA_MIN = 0;

    /**
     * object used for print the data in the toString code, we print the day and the hour
     */
    private static final DateFormat DATA_FORMAT = new SimpleDateFormat("dd-MMM HH:mm:ss",
            Locale.getDefault());

    /**
     * Enum containing the possible result of the activity recognition
     */
    public enum ActivityType {
        /**
         * initial state, no enough data for take a decision
         */
        NO_ACTIVITY,
        /**
         * the person is stationary
         */
        STATIONARY,
        /**
         * the person is walking
         */
        WALKING,
        /**
         * the person is walking fast
         */
        FASTWALKING,
        /**
         * the person is running
         */
        JOGGING,
        /**
         * the person is using a bike
         */
        BIKING,
        /**
         * the person is driving
         */
        DRIVING,
        /**
         * the person is doing the stairs
         */
        STAIRS,
        /**
         * the adult is in the car
         */
        ADULT_IN_CAR,
        /**
         * invalid state
         */
        ERROR
    }//ActivityType

    /**
     * extract the activity from a sensor sample
     * @param sample data read from the node
     * @return type of activity detected by the node
     */
    public static ActivityType getActivityStatus(Sample sample){
        if(hasValidIndex(sample,0)){
            int activityId = sample.data[0].byteValue();
            switch (activityId){
                case 0x00:
                    return ActivityType.NO_ACTIVITY;
                case 0x01:
                    return ActivityType.STATIONARY;
                case 0x02:
                    return ActivityType.WALKING;
                case 0x03:
                    return ActivityType.FASTWALKING;
                case 0x04:
                    return ActivityType.JOGGING;
                case 0x05:
                    return ActivityType.BIKING;
                case 0x06:
                    return ActivityType.DRIVING;
                case 0x07:
                    return ActivityType.STAIRS;
                case 0x08:
                    return ActivityType.ADULT_IN_CAR;
                default:
                    return ActivityType.ERROR;
            }//switch
        }//if
        return ActivityType.ERROR;
    }//getActivityStatus

    /**
     * extract the data when we receive the notification from the node
     * @param sample sample read from the node
     * @return local time when we receive the data, or null if it is an invalid sample
     */
    public static @Nullable Date getActivityDate(Sample sample){
        if(hasValidIndex(sample,1))
            return new Date(sample.data[1].longValue());
        //else
        return null;
    }//getActivityDate

    public static short getAlgorithmType(Sample sample){
        if(hasValidIndex(sample,2)){
            return sample.data[2].shortValue();
        }
        return 0;
    }


    /**
     * build a activity feature
     * @param n node that will send data to this feature
     */
    public FeatureActivity(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[0], FEATURE_UNIT[0], Field.Type.UInt8,
                        DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[1], FEATURE_UNIT[1], Field.Type.Int64,
                        Long.MAX_VALUE,0),
                new Field(FEATURE_DATA_NAME[2], FEATURE_UNIT[2], Field.Type.UInt8,
                        0xFF,0),
        });
    }//FeatureActivity


    private ExtractResult extractActivity(long timestamp, byte[] data, int dataOffset){
        Sample temp = new Sample(timestamp, new Number[]{
                data[dataOffset],
                System.currentTimeMillis()
        }, getFieldsDesc());
        return new ExtractResult(temp, 1);
    }

    private ExtractResult extractActivityAndAlgorithm(long timestamp, byte[] data, int dataOffset){
        Sample temp = new Sample(timestamp, new Number[]{
                data[dataOffset],
                System.currentTimeMillis(),
                NumberConversion.byteToUInt8(data,dataOffset+1)
        }, getFieldsDesc());
        return new ExtractResult(temp, 2);
    }

    /**
     * read a byte with the activity data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the Activity id)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        int byteAvailable = data.length - dataOffset;
        if ( byteAvailable< 1)
            throw new IllegalArgumentException("There are no 1 byte available to read");
        if(byteAvailable == 1 ) {
            return extractActivity(timestamp,data,dataOffset);
        }else{
            return extractActivityAndAlgorithm(timestamp,data,dataOffset);
        }
    }//extractData

    @Override
    public String toString(){
        Sample sample = mLastSample;
        if(sample!=null){
            return FEATURE_NAME+":\n"+
                    "\tTimestamp: "+ sample.timestamp+"\n" +
                    "\tActivity: "+ getActivityStatus(sample)+"\n" +
                    "\tDate: "+ DATA_FORMAT.format(getActivityDate(sample))+
                    "\tAlgorithm: "+ getAlgorithmType(sample);
        }else{
            return super.toString();
        }

    }//toString

}//FeatureActivity
