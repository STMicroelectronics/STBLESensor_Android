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
 * Feature used for notify the free fall event from the accelerometer
 * @deprecated use {@link FeatureAccelerationEvent} enabling {@link
 * com.st.BlueSTSDK.Features.FeatureAccelerationEvent.AccelerationEvent#FREE_FALL}
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
@Deprecated
public class FeatureFreeFall extends Feature {

    public static final String FEATURE_NAME="FreeFall";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = FEATURE_NAME;
    public static final float DATA_MAX = 1;
    public static final float DATA_MIN = 0;

    /**
     * build an free fall feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureFreeFall(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX, DATA_MIN)
        });
    }//FeatureFreeFall

    /**
     * return true if there was a free fall event
     * @param sample sample containing the data from the sensor
     * @return true if there was a free fall event
     */
    public static boolean getFreeFallStatus(Sample sample) {
        if(sample!=null)
            if(sample.data.length>0)
                if (sample.data[0] != null)
                    return sample.data[0].byteValue()!=0;
        //else
        return false;
    }//getActivity

    /**
     * it will read a byte from the data send from the node, it the value is !=0 it will be a
     * positive free fall event
     *
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the free fall information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if (data.length - dataOffset <1 )
            throw new IllegalArgumentException("There are no 1 bytes available to read");
        Sample temp = new Feature.Sample(timestamp,new Number[]{
                data[dataOffset],
        },getFieldsDesc());
        return new ExtractResult(temp,1);
    }
}
