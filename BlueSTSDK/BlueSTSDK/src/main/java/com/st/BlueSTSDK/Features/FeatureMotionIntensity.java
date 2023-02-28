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

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

/**
 * Feature that use the osxMotionID library for detect the use motion intensity.
 * The intensity is a number between 0 and 10
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureMotionIntensity extends Feature {
    public static final String FEATURE_NAME = "VerticalContext Intensity";
    public static final String FEATURE_UNIT = null;
    public static final String FEATURE_DATA_NAME = "Intensity";
    public static final float DATA_MAX = 10;
    public static final float DATA_MIN = 0;

    /**
     * extract the motion intensity from the sample
     * @param sample data read from the node
     * @return motion intensity or a negative number
     */
    public static byte getMotionIntensity(Sample sample){
        if(hasValidIndex(sample,0))
            return sample.data[0].byteValue();
        return -1;
    }//getMotionIntensity

    /**
     * build a activity feature
     * @param n node that will send data to this feature
     */
    public FeatureMotionIntensity(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX,DATA_MIN),
        });
    }//FeatureMotionIntensity


    /**
     * read a byte with the activity data send from the node
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (1) and data extracted (the motion intensity)
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
