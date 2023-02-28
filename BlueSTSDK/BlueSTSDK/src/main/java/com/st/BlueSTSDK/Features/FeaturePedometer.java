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
 * Feature that contains the pedometer information, the number of steps and the frequency
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeaturePedometer extends Feature{

    public static final String FEATURE_NAME = "Pedometer";
    public static final String FEATURE_UNIT[] = {null,"step/min"};
    public static final String FEATURE_DATA_NAME[] ={"Steps","Frequency"} ;
    public static final Number DATA_MAX[] = {Integer.MAX_VALUE,Short.MAX_VALUE};
    public static final Number DATA_MIN[] = {0,0};

    /**
     * Index where find the number of steps
     */
    public static final int NUMBER_STEPS_INDEX = 0;

    /**
     * Index where find the steps frequency
     */
    public static final int FREQUENCY_STEPS_INDEX = 1;

    /**
     * return the number of steps detected by the node
     * @param s sample
     * @return number of steps or a negative number if it not a valid sample
     */
    public static long getSteps(Sample s) {
        if(s!=null)
            if (s.data.length > NUMBER_STEPS_INDEX)
                if (s.data[NUMBER_STEPS_INDEX] != null)
                    return s.data[NUMBER_STEPS_INDEX].longValue();
        //else
        return -1;
    }//getSteps

    /**
     * return the steps frequency
     * @param s sample
     * @return steps frequency or or a negative number if is not a valid sample
     */
    public static int getFrequency(Sample s) {
        if(s!=null)
            if (s.data.length > FREQUENCY_STEPS_INDEX)
                if (s.data[FREQUENCY_STEPS_INDEX] != null)
                    return s.data[FREQUENCY_STEPS_INDEX].intValue();
        //else
        return -1;
    }//getFrequency

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeaturePedometer(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME[NUMBER_STEPS_INDEX],
                                FEATURE_UNIT[NUMBER_STEPS_INDEX], Field.Type.UInt32,
                                DATA_MAX[NUMBER_STEPS_INDEX],
                                DATA_MIN[NUMBER_STEPS_INDEX]),
                        new Field(FEATURE_DATA_NAME[FREQUENCY_STEPS_INDEX],
                                FEATURE_UNIT[FREQUENCY_STEPS_INDEX], Field.Type.UInt16,
                                DATA_MAX[FREQUENCY_STEPS_INDEX],
                                DATA_MIN[FREQUENCY_STEPS_INDEX]),
                });
    }//FeatureAcceleration

    /**
     * reat an uint32 (#steps ) and a uint16 (frequency)
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return the sample and the number of read byte (6)
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 6)
            throw new IllegalArgumentException("There are no 6 bytes available to read");
        Sample temp = new Sample(timestamp, new Number[]{
                (NumberConversion.LittleEndian.bytesToUInt32(data, dataOffset + 0)),
                (NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset + 4))
        },getFieldsDesc());

        return new ExtractResult(temp,6);
    }
}
