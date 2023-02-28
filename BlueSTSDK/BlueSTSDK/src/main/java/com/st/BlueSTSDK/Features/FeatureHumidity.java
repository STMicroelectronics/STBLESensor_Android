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
 * Feature that contains the humidity sensor values, this sensor export the percentage of water
 * in the air. The data will have 1 decimal value
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureHumidity extends Feature {

    public static final String FEATURE_NAME = "Humidity";

    public static final String FEATURE_UNIT = "%";
    public static final String FEATURE_DATA_NAME = "Humidity";
    public static final float DATA_MAX = 100;
    public static final float DATA_MIN = 0;

    protected static final Field HUMIDITY_FILED =
            new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.Float, DATA_MAX, DATA_MIN);

    /**
     * build a Feature of type FeatureHumidity
     *
     * @param n node that will send data to this feature
     */
    public FeatureHumidity(Node n) {
        super(FEATURE_NAME, n, new Field[]{ HUMIDITY_FILED });
    }//FeatureHumidity

    protected FeatureHumidity(String name, Node n,Field data[]) {
        super(name,n,data);
        if(data[0]!=HUMIDITY_FILED){
            throw new IllegalArgumentException("First data[0] must be FeatureHumidity" +
                    ".HUMIDITY_FILED");
        }//if
    }

    /**
     * extract the humidity value from the sensor raw data
     *
     * @param s sensor raw data
     * @return humidity value or nan if the data array is not valid
     */
    public static float getHumidity(Sample s) {
        if(s!=null)
            if(s.data.length>0)
                if (s.data[0] != null)
                    return s.data[0].floatValue();
        //else
        return Float.NaN;
    }//getHumidity

    /**
     * extract the humidity from the node raw data, it will read a uint16 and scale if by a factor
     * 10 for retrieve the decimal value.
     *
     * @param data       array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (2) and data extracted (the humidity information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 2)
            throw new IllegalArgumentException("There are no 2 bytes available to read");

        //it has 1 decimal value -> divide by 10
        Sample temp = new Sample(timestamp,new Number[]{
                NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset)/10.0f
        },getFieldsDesc());

        return new ExtractResult(temp,2);
    }//update

}
