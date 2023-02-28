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

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

/**
 * This feature will contains the quaternion compute in the device using the sensor fusion
 * algorithm (gyroscope + accelerometer + magnetometer).
 * <p>
 * The data will be transfer using float data for each component.
 * </p>
 * <p>
 * the quaternion components are normalized to 1
 * </p>
 * <p>
 * Qi = x component, Qj = y component Qk = z component Qs = scalar component
 * </p>
 * <p>The AutoConfigure process will acquire the magnetometer data for calibrate the magnetometer
 * sensors </p>
 *
 *
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureMemsSensorFusion extends FeatureAutoConfigurable {

    public static final String FEATURE_NAME = "MEMS Sensor Fusion";
    public static final String FEATURE_UNIT = null;
    public static final String[] FEATURE_DATA_NAME = new String[]{"qi", "qj", "qk", "qs"};
    public static final float DATA_MAX = 1.0f;
    public static final float DATA_MIN = -1.0f;

    /**
     * index where you can find Qi quaternion value/description
     */
    public static final int QI_INDEX = 0;
    /**
     * index where you can find Qj quaternion value/description
     */
    public static final int QJ_INDEX = 1;
    /**
     * index where you can find Qk quaternion value/description
     */
    public static final int QK_INDEX = 2;
    /**
     * index where you can find Qs quaternion value/description
     */
    public static final int QS_INDEX = 3;

    /**
     * build a Mems sensor fusion feature
     *
     * @param node note that will send data to this feature
     */
    public FeatureMemsSensorFusion(Node node) {
        this(FEATURE_NAME, node);
    }//FeatureMemsSensorFusion

    /**
     * create a feature that export the quaternion data with a different name
     * @param name feature name
     * @param node node that will export the data
     */
    protected FeatureMemsSensorFusion(String name, Node node) {
        super(name, node, new Field[]{
                new Field(FEATURE_DATA_NAME[QI_INDEX], FEATURE_UNIT, Field.Type.Float,
                        DATA_MAX, DATA_MIN),
                new Field(FEATURE_DATA_NAME[QJ_INDEX], FEATURE_UNIT, Field.Type.Float,
                        DATA_MAX, DATA_MIN),
                new Field(FEATURE_DATA_NAME[QK_INDEX], FEATURE_UNIT, Field.Type.Float,
                        DATA_MAX, DATA_MIN),
                new Field(FEATURE_DATA_NAME[QS_INDEX], FEATURE_UNIT, Field.Type.Float,
                        DATA_MAX, DATA_MIN)
        });
    }//FeatureMemsSensorFusion

    /**
     * Get the quaternion qi component
     *
     * @param sample sample data from the sensor
     * @return quaternion qs component, or Nan if the array doesn't contain data
     */
     public static float getQi(Sample sample) {
         if(sample!=null)
            if (sample.data.length > QI_INDEX)
                if (sample.data[QI_INDEX] != null)
                    return sample.data[QI_INDEX].floatValue();
         //else
        return Float.NaN;
    }

    /**
     * Get the quaternion qj component
     *
     * @param sample sample data from the sensor
     * @return quaternion qi component, or Nan if the array doesn't contain data
     */
    public static float getQj(Sample sample) {
        if(sample!=null)
            if (sample.data.length > QJ_INDEX)
                if (sample.data[QJ_INDEX] != null)
                    return sample.data[QJ_INDEX].floatValue();
        //else
        return Float.NaN;
    }

    /**
     * Get the quaternion qk component
     *
     * @param sample sample data from the sensor
     * @return quaternion qj component, or Nan if the array doesn't contain data
     */
    public static float getQk(Sample sample) {
        if(sample!=null)
            if (sample.data.length > QK_INDEX)
                if (sample.data[QK_INDEX] != null)
                    return sample.data[QK_INDEX].floatValue();
        //else
        return Float.NaN;
    }

    /**
     * Get the quaternion qk component
     *
     * @param sample sample data from the sensor
     * @return quaternion qk component, or Nan if the array doesn't contain data
     */
    public static float getQs(Sample sample) {
        if(sample!=null)
            if (sample.data.length > QS_INDEX)
                if (sample.data[QS_INDEX] != null)
                    return sample.data[QS_INDEX].floatValue();
        //else
        return Float.NaN;
    }



    /**
     * compute the q3 component knowing that quaternion is normalized, the sum of square is 1
     *
     * @param qi quaternion x component
     * @param qj quaternion y component
     * @param qk quaternion z component
     * @return qs = sqrt(1- (qi^2+qj^2+qk^2))
     */
    protected static float getQs(float qi, float qj, float qk) {
        float t=1 - (qi * qi + qj * qj + qk * qk);
        return  t>0 ? (float) (Math.sqrt(t)) : 0;
    }//getQs

    /**
     * extract the quaternion data from the raw node data, the q3 component is optional if the
     * quaternion is normalized.
     *<p>
     *     this feature will read 16 if the quaternion is not normalized or it the w component is
     *     transmitted, 12 if the quaternion is normalized and the w component is not transmitted
     *     (will be computed by the feature)
     *</p>
     *
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (16 or 12) and data extracted (the quaternion information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 12)
            throw new IllegalArgumentException("There are no 12 bytes available to read");

        float qi = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset);
        float qj = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 4);
        float qk = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 8);

        int nReadByte;
        float qs;
        if ((data.length - dataOffset) > 12) {
            qs = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset + 12);
            //normalize the quaternion
            final double norm = Math.sqrt(qi*qi+qj*qj+qk*qk+qs*qs);
            qi /= norm;
            qj /= norm;
            qk /= norm;
            qs /= norm;

            nReadByte = 16;
        } else {
            qs = getQs(qi, qj, qk);
            nReadByte = 12;
        }//if-else

        return new ExtractResult(new Sample(timestamp, new Number[]{ qi,qj,qk,qs },getFieldsDesc()),nReadByte);
    }

    @Override
    public String toString(){
        //keep a reference for avoid partial update
        Sample sample = mLastSample;
        if(sample==null)
            return super.toString();
        Number data[] = sample.data;
        Field dataDesc[] = getFieldsDesc();
        return String.format(FEATURE_NAME+":\n\tTimestamp: %d\n\tQuat:(%s: %.3f, %s: %.3f, %s: %" +
                        ".3f, %s: %.3f)",sample.timestamp,
                dataDesc[0].getName(),data[0].floatValue(),
                dataDesc[1].getName(),data[1].floatValue(),
                dataDesc[2].getName(),data[2].floatValue(),
                dataDesc[3].getName(),data[3].floatValue());
    }
}
