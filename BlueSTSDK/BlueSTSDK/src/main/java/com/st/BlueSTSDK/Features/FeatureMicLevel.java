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

/**
 * This feature contains the audio level from an array of microphones
 * <p>
 * Since the number of microphones is not fixed it will consume all the available bytes from the
 * Bluetooth characteristics
 * </p>
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureMicLevel extends Feature {

    /**
     * Name of the feature
     */
    public static final String FEATURE_NAME = "Mic Level";
    /**
     * data units
     */
    public static final String FEATURE_UNIT = "dB";
    /**
     * name of the data
     */
    public static final String FEATURE_DATA_NAME = "Mic";
    /**
     * max audio level handle by the sensor
     */
    public static final short DATA_MAX = 128;
    /**
     * min audio level handle by the sensor
     */
    public static final short DATA_MIN = 0;

    /**
     * build the feature
     *
     * @param n node that will provide the data
     */
    public FeatureMicLevel(Node n) {
        super(FEATURE_NAME, n,
                new Field[]{
                        new Field(FEATURE_DATA_NAME, FEATURE_UNIT, Field.Type.UInt8,
                                DATA_MAX, DATA_MIN),
                });
    }//FeatureMicLevel

    /**
     * Get the auto level for the microphone {@code micLevel}
     *
     * @param s sample from the sensor
     * @param micLevel microphone to extract
     * @return microphone level or a negative number if the microphone doesn't exist
     */
    public static byte getMicLevel(Sample s,int micLevel) {
        if(s!=null)
            if (micLevel < s.data.length)
                    return s.data[micLevel].byteValue();
        //else
        return Byte.MIN_VALUE;
    }//getMicLevel

    /**
     * the number of microphone is not fixed so this function will read all the available data
     * @param timestamp data timestamp
     * @param data       array where read the data
     * @param dataOffset offset where start to read the data
     * @return data sample and the number of read bytes
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        int nMic = data.length-dataOffset;
        if (nMic <= 0)
            throw new IllegalArgumentException("There are no more than 1 byte available to read");

        //update the feature desc if needed
        if(mDataDesc.length!=nMic){
            Field temp[] = new Field[nMic];
            for(int i=0;i<nMic;i++){
                temp[i]= new Field(FEATURE_DATA_NAME+(i+1), FEATURE_UNIT, Field.Type.UInt8,
                        DATA_MAX, DATA_MIN);
            }//for
            mDataDesc=temp;
        }//if mDataDesc

        Number levels[] = new Number[nMic];

        for(int i=0;i<nMic;i++){
            levels[i]=data[dataOffset+i];
        }//for

        return new ExtractResult(new Sample(timestamp,levels,getFieldsDesc()),nMic);
    }//extractData
}
