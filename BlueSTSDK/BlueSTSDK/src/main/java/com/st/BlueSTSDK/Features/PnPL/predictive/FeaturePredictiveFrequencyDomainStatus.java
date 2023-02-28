/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentatio
 *      n
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
package com.st.BlueSTSDK.Features.PnPL.predictive;

import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeaturePredictiveFrequencyDomainStatus extends FeaturePredictive {
    private static final String FEATURE_NAME = "PredictiveFrequencyDomainStatus";


    private static Field buildFreqFieldNamed(String name){
        return new Field(name,"Hz", Field.Type.Float,(1<<16)/10.0f,0);
    }

    private static Field buildValueFieldNamed(String name){
        return new Field(name,"m/s^2", Field.Type.Float,(1<<16)/100.0f,0);
    }

    public FeaturePredictiveFrequencyDomainStatus(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                buildStatusFieldNamed("StatusFreq_X"),
                buildStatusFieldNamed("StatusFreq_Y"),
                buildStatusFieldNamed("StatusFreq_Z"),
                buildFreqFieldNamed("Freq_X"),
                buildFreqFieldNamed("Freq_Y"),
                buildFreqFieldNamed("Freq_Z"),
                buildValueFieldNamed("MaxAmplitude_X"),
                buildValueFieldNamed("MaxAmplitude_Y"),
                buildValueFieldNamed("MaxAmplitude_Z"),
        });
    }

    public static Status getStatusX(Sample s){
        return getStatusFromIndex(s,0);
    }

    public static Status getStatusY(Sample s){
        return getStatusFromIndex(s,1);
    }

    public static Status getStatusZ(Sample s){
        return getStatusFromIndex(s,2);
    }

    public static float getWorstXFrequency(Sample s){
        return getFloatFromIndex(s,3);
    }

    public static float getWorstYFrequency(Sample s){
        return getFloatFromIndex(s,4);
    }

    public static float getWorstZFrequency(Sample s){
        return getFloatFromIndex(s,5);
    }

    public static float getWorstXValue(Sample s){
        return getFloatFromIndex(s,6);
    }

    public static float getWorstYValue(Sample s){
        return getFloatFromIndex(s,7);
    }

    public static float getWorstZValue(Sample s){
        return getFloatFromIndex(s,8);
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 13)
            throw new IllegalArgumentException("There are no 13 bytes available to read");

        short timeStatus = NumberConversion.byteToUInt8(data,dataOffset+0);
        float freqX = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+1)/10.0f;
        float valueX = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+3)/100.0f;
        float freqY = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+5)/10.0f;
        float valueY = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+7)/100.0f;
        float freqZ = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+9)/10.0f;
        float valueZ = NumberConversion.LittleEndian.bytesToUInt16(data,dataOffset+11)/100.0f;

        Sample s = new Sample(timestamp, new Number[]{
                extractXRawStatus(timeStatus),
                extractYRawStatus(timeStatus),
                extractZRawStatus(timeStatus),
                freqX,freqY,freqZ,
                valueX,valueY,valueZ
        },getFieldsDesc());

        return new ExtractResult(s,13);
    }
}
