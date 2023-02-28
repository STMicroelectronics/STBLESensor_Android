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
 * Feature that will contain the data from an accelerometer sensor.
 * <p>The data are in mg</p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureAcceleration extends Feature {

    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "Accelerometer";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "mg";
    /**
     * name of the data
     */
    public static final String[] FEATURE_DATA_NAME = {"X", "Y", "Z"};
    /**
     * max acceleration handle by the sensor
     */
    public static final short DATA_MAX = 16000;
    /**
     * min acceleration handle by the sensor
     */
    public static final short DATA_MIN = -16000;

    /** index where you can find acceleration value/description in the x direction */
    private static final int ACC_X_INDEX = 0;
    /** index where you can find acceleration value/description in the y direction */
    private static final int ACC_Y_INDEX = 1;
    /** index where you can find acceleration value/description in the z direction */
    private static final int ACC_Z_INDEX = 2;

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeatureAcceleration(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME[ACC_X_INDEX], FEATURE_UNIT, Field.Type.Int16,
                                DATA_MAX, DATA_MIN),
                        new Field(FEATURE_DATA_NAME[ACC_Y_INDEX], FEATURE_UNIT, Field.Type.Int16,
                                DATA_MAX, DATA_MIN),
                        new Field(FEATURE_DATA_NAME[ACC_Z_INDEX], FEATURE_UNIT, Field.Type.Int16,
                                DATA_MAX, DATA_MIN)
                });
    }//FeatureAcceleration

    /**
     * Get the acceleration on the X axis
     *
     * @param s sample data from the sensor
     * @return acceleration in the X axis, or Nan if the array doesn't contain data
     */
    public static float getAccX(Sample s) {
        if(s!=null)
            if (s.data.length > ACC_X_INDEX)
                if (s.data[ACC_X_INDEX] != null)
                    return s.data[ACC_X_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getAccX

    /**
     * Get the acceleration on the Y axis
     *
     * @param s sample from the sensor
     * @return acceleration in the Y axis, or Nan if the array doesn't contain data
     */
    public static float getAccY(Sample s) {
        if(s!=null)
            if (s.data.length > ACC_Y_INDEX)
                if (s.data[ACC_Y_INDEX] != null)
                    return s.data[ACC_Y_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getAccY

    /**
     * Get the acceleration on the Z axis
     *
     * @param s sample data from the sensor
     * @return acceleration in the Z axis, or Nan if the array doesn't contain data
     */
    public static float getAccZ(Sample s) {
        if(s!=null)
            if (s.data.length > ACC_Z_INDEX)
                if (s.data[ACC_Z_INDEX] != null)
                    return s.data[ACC_Z_INDEX].floatValue();
        //else
        return Float.NaN;
    }//getAccZ

    /**
     * extract the acceleration data, it will read 3 int16
     *
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read bytes (6) and data extracted (the acceleration)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 6)
            throw new IllegalArgumentException("There are no 6 bytes available to read");

        Sample temp = new Sample(timestamp, new Number[]{
                //x
                (NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 0)),
                //y
                (NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 2)),
                //z
                (NumberConversion.LittleEndian.bytesToInt16(data, dataOffset + 4)),
        },getFieldsDesc());

        return new ExtractResult(temp,6);
    }//update


    @Override
    public String toString(){
        //create the string with the feature data
        StringBuilder sb = new StringBuilder();
        Sample sample = mLastSample; //keep a reference for be thread safe
        if(sample==null)
            return super.toString();
        sb.append(FEATURE_NAME).append(":\n\tTimestamp: ").append(sample.timestamp).append('\n');
        Number data[] = sample.data;
        Field dataDesc[] = getFieldsDesc();
        sb.append("\tData: ( ");
        for (int i = 0; i < data.length; i++) {
            sb.append(String.format("%s: %4d ", dataDesc[i].getName(), data[i].intValue()));
        }//for
        sb.append(')');
        return sb.toString();
    }
}
