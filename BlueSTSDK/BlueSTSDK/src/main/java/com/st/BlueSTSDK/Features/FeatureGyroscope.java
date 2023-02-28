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
 * Feature that contains the data from a gyroscope sensor.
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureGyroscope extends Feature {

    /** feature name */
    public static final String FEATURE_NAME = "Gyroscope";
    /** unit of the exported data */
    public static final String FEATURE_UNIT = "dps";
    /** name of the exported data */
    public static final String[] FEATURE_DATA_NAME = {"X", "Y", "Z"};

    /** max value of one component*/
    public static final float DATA_MAX = ((float)(1 << 15))/10.0f;
    /** min value of one component*/
    public static final float DATA_MIN = -DATA_MAX;

    /** index where you can find gyroscope value/description in the x direction */
    public static final int GYRO_X_INDEX = 0;
    /** index where you can find gyroscope value/description in the y direction*/
    public static final int GYRO_Y_INDEX = 1;
    /** index where you can find gyroscope value/description in the z direction*/
    public static final int GYRO_Z_INDEX = 2;

    /**
     * build a gyroscope feature
     * @param node node where the feature will read the data
     */
    public FeatureGyroscope(Node node) {
        super(FEATURE_NAME, node,
                new Field[]{
                        new Field(FEATURE_DATA_NAME[GYRO_X_INDEX], FEATURE_UNIT, Field.Type.Float,
                                DATA_MAX, DATA_MIN),
                        new Field(FEATURE_DATA_NAME[GYRO_Y_INDEX], FEATURE_UNIT, Field.Type.Float,
                                DATA_MAX, DATA_MIN),
                        new Field(FEATURE_DATA_NAME[GYRO_Z_INDEX], FEATURE_UNIT, Field.Type.Float,
                                DATA_MAX, DATA_MIN)
                });
    }//FeatureGyroscope

    /**
     * Get the gyroscope on the X axis
     *
     * @param s sample data from the sensor
     * @return gyroscope in the X axis, or Nan if the array doesn't contain data
     */
    public static float getGyroX(Sample s) {
        if(s!=null)
            if (s.data.length > GYRO_X_INDEX)
                if (s.data[GYRO_X_INDEX] != null)
                    return s.data[GYRO_X_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getGyroX

    /**
     * Get the gyroscope on the Y axis
     *
     * @param s sample data from the sensor
     * @return gyroscope in the Y axis, or Nan if the array doesn't contain data
     */
    public static float getGyroY(Sample s) {
        if(s!=null)
            if (s.data.length > GYRO_Y_INDEX)
                if (s.data[GYRO_Y_INDEX] != null)
                    return s.data[GYRO_Y_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getGyroY

    /**
     * Get the gyroscope on the Z axis
     *
     * @param s sample data from the sensor
     * @return gyroscope in the Z axis, or Nan if the array doesn't contain data
     */
    public static float getGyroZ(Sample s) {
        if(s!=null)
            if (s.data.length > GYRO_Z_INDEX)
                if (s.data[GYRO_Z_INDEX] != null)
                    return s.data[GYRO_Z_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getGyroZ

    /**
     * Extract the gyroscope data, it will read 3 int16
     *
     * @param data array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (6) and data extracted (the gyroscope information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 6)
            throw new IllegalArgumentException("There are no 6 bytes available to read");

        Sample temp = new Sample(timestamp, new Number[]{
                //x
                (NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 0))/10.0f,
                //y
                (NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2))/10.0f,
                //z
                (NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4))/10.0f
        },getFieldsDesc());

        return new ExtractResult(temp,6);
    }//update

    @Override
    public String toString(){
        //create the string with the feature data
        StringBuilder sb = new StringBuilder();
        Sample sample = mLastSample;
        if(sample==null)
            return super.toString();
        sb.append(FEATURE_NAME).append(":\n\tTimestamp: ").append(sample.timestamp).append('\n');
        Number data[] = sample.data;
        Field dataDesc[] = getFieldsDesc();
        sb.append("\tData: ( ");
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%s: %.1f ",dataDesc[i].getName(),data[i].floatValue()));
        }//for
        sb.append(')');
        return sb.toString();
    }

}
