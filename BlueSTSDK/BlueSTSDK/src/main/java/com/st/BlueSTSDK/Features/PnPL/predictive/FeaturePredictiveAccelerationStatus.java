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

public class FeaturePredictiveAccelerationStatus extends FeaturePredictive {
    private static final String FEATURE_NAME = "PredictiveAccelerationStatus";

    private static Field buildAccFieldNamed(String name){
        return new Field(name,"m/s^2",Field.Type.Float,Float.MAX_VALUE,0);
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

    public static float getAccX(Sample s){
        return getFloatFromIndex(s,3);
    }

    public static float getAccY(Sample s){
        return getFloatFromIndex(s,4);
    }

    public static float getAccZ(Sample s){
        return getFloatFromIndex(s,5);
    }

    public FeaturePredictiveAccelerationStatus(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                buildStatusFieldNamed("StatusAcc_X"),
                buildStatusFieldNamed("StatusAcc_Y"),
                buildStatusFieldNamed("StatusAcc_Z"),
                buildAccFieldNamed("AccPeak_X"),
                buildAccFieldNamed("AccPeak_Y"),
                buildAccFieldNamed("AccPeak_Z"),

        });
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 12)
            throw new IllegalArgumentException("There are no 12 bytes available to read");

        short timeStatus = NumberConversion.byteToUInt8(data,dataOffset+0);
        float speedX = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+1);
        float speedY = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+5);
        float speedZ = NumberConversion.LittleEndian.bytesToFloat(data,dataOffset+9);

        Sample s = new Sample(timestamp, new Number[]{
                extractXRawStatus(timeStatus),
                extractYRawStatus(timeStatus),
                extractZRawStatus(timeStatus),
                speedX,
                speedY,
                speedZ
        },getFieldsDesc());

        return new ExtractResult(s,12);
    }
}
