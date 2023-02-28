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

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;


public class FeatureEventCounter extends Feature {
    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "EventCounter";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Events";
    /**
     * max angle handle by the sensor
     */
    public static final float DATA_MAX = (1L<<32L)-1;
    /**
     * min angle handle by the sensor
     */
    public static final float DATA_MIN = 0;

    /**
     * extract the compass value from the sensor raw data
     *
     * @param sample sensor raw data
     * @return compass value or nan if the data array is not valid
     */
    public static float getEventCounter(Sample sample) {
        if(hasValidIndex(sample,0))
            return sample.data[0].longValue();
        //else
        return Float.NaN;
    }

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n node that will update this feature
     */
    public FeatureEventCounter(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME,null, Field.Type.Float,DATA_MAX,DATA_MIN)
        });
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 4)
            throw new IllegalArgumentException("There are no 4 bytes available to read");
        Sample temp = new Sample(timestamp,new Number[]{
                NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset)
        },getFieldsDesc());
        return new ExtractResult(temp,4);
    }
}
