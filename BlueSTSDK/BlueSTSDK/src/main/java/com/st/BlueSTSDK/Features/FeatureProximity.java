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

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * Feature that contains the data comes form a proximity sensor.
 * <p>
 * Node: since the sensor is the same,it is possible that you can not have data from a proximity
 * sensor and from a luminosity sensor at the same time
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureProximity extends Feature {

    public static final String FEATURE_NAME = "Proximity";
    public static final String FEATURE_UNIT = "mm";
    public static final String FEATURE_DATA_NAME = "Proximity";
    /**
     * the sensor return this value when the object is out of range
     */
    public static final int OUT_OF_RANGE_VALUE = 0xFFFF;
    /**
     * maximum object distance manage by the sensor
     */
    protected static final float LOW_RANGE_DATA_MAX = 0x00FE;
    protected static final float HIGH_RANGE_DATA_MAX = 0X7FFE;
    /**
     * minimal object distance manage by the sensor
     */
    protected static final float DATA_MIN = 0;

    private static final Field[] LOW_RANGE_FIELDS = new Field[] {
            new Field(FEATURE_DATA_NAME, FEATURE_UNIT,
                    Field.Type.UInt16, LOW_RANGE_DATA_MAX,DATA_MIN)};

    private static final Field[] HIGH_RANGE_FIELDS = new Field[] {
            new Field(FEATURE_DATA_NAME, FEATURE_UNIT,
                    Field.Type.UInt16, HIGH_RANGE_DATA_MAX,DATA_MIN)};

    /**
     * build a proximity feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureProximity(Node n) {
        super(FEATURE_NAME, n, HIGH_RANGE_FIELDS);
    }//FeatureProximity

    /**
     * extract the proximity value from the feature raw data
     *
     * @param sample feature raw data
     * @return proximity value or -1 if the data array is not valid
     */
    public static int getProximityDistance(Sample sample) {
        if (hasValidIndex(sample,0))
            return sample.data[0].intValue();
        //else
        return -1; // the luminosity is always positive
    }//getProximityDistance

    public static boolean isOutOfRangeDistance(Sample s){
        return getProximityDistance(s)==OUT_OF_RANGE_VALUE;
    }


    private static boolean isLowRangeSensor(int value){
        return (value & 0x8000)==0;
    }

    private static int getRangeValue(int value){
        return (value & ~0x8000);
    }

    private Sample getLowRangeSample(long timestamp, int value){
        int rangeValue = getRangeValue(value);
        if(rangeValue > LOW_RANGE_DATA_MAX){
            rangeValue = OUT_OF_RANGE_VALUE;
        }

        return new Sample(timestamp, new Number[]{rangeValue},LOW_RANGE_FIELDS);
    }

    private Sample getHighRangeSample(long timestamp,int value){
        int rangeValue = getRangeValue(value);
        if(rangeValue > HIGH_RANGE_DATA_MAX){
            rangeValue = OUT_OF_RANGE_VALUE;
        }
        return new Sample(timestamp, new Number[]{rangeValue} , HIGH_RANGE_FIELDS);
    }

    /**
     * extract the proximity value from the node raw data, it reads an uint16
     *
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (2) and data extracted (the proximity information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp,byte[] data, int dataOffset) {
        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 bytes available to read");

        Sample temp;
        int value = NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset);
        if(isLowRangeSensor(value)){
            temp = getLowRangeSample(timestamp,value);
        }else
            temp = getHighRangeSample(timestamp,value);

        return new ExtractResult(temp,2);
    }//update

    @Override
    public String toString(){
        //create the string with the feature data
        Sample sample = mLastSample; //keep a reference for be secure to be thread safe
        if(sample==null)
            return FEATURE_NAME+":\n\tNo Data";
        //else
        if(sample.data.length==0){
            return FEATURE_NAME+":\n\tNo Data";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(FEATURE_NAME).append(":\n\tTimestamp: ").append(sample.timestamp).append('\n');

        int distance = getProximityDistance(sample);
        sb.append(FEATURE_DATA_NAME).append(": ");
        if(distance != OUT_OF_RANGE_VALUE)
            sb.append(distance);
        else
            sb.append("Out Of Range");

        return sb.toString();
    }

}
