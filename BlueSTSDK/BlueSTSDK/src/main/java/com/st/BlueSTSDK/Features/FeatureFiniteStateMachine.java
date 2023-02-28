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
import com.st.BlueSTSDK.Utils.NumberConversion;

public class FeatureFiniteStateMachine extends Feature {

    private static final int N_MAX_OUTPUT_REGISTER = 16;
    public static final String FEATURE_NAME = "Finite State Machine";
    private static final String FEATURE_DATA_NAME = "Register_";

    private static final int FEATURE_DATA_MIN = 0;
    private static final int FEATURE_DATA_MAX = 255;

    private static int numberRegisters=16;

    private static final Field[] FEATURE_FIELDS = new Field[N_MAX_OUTPUT_REGISTER];

    static {
        for(int i=0;i<FEATURE_FIELDS.length;i++){
            FEATURE_FIELDS[i]= new Field(FEATURE_DATA_NAME+(i+1), null, Field.Type.UInt8,
                    FEATURE_DATA_MAX, FEATURE_DATA_MIN);
        }//for
    }

    /**
     * build a pressure feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureFiniteStateMachine(Node n) {
        super(FEATURE_NAME, n, FEATURE_FIELDS);
    }//FeatureMachineLearningCore

    public static int getNumberRegisters() {
        return numberRegisters;
    }

    public static short[] getRegisterStatus(Sample sample){
        Number[] data = sample.data;
        short[] outData = new short[data.length];
        for(int i = 0 ; i<data.length ; i++){
            outData[i] = data[i].shortValue();
        }
        return outData;
    }

    public static short getRegisterStatus(Sample sample, int registerIndex){
        if(hasValidIndex(sample,registerIndex)){
            return sample.data[registerIndex].shortValue();
        }
        return Short.MAX_VALUE;
    }

    @Override
    protected ExtractResult extractData(long timestamp, @NonNull byte[] data, int dataOffset) {
        if((data.length - dataOffset)<10) {
            numberRegisters = data.length - dataOffset - 1 /* one status byte */;
        } else {
            numberRegisters = data.length - dataOffset - 2 /* two status bytes */;
        }

        if (numberRegisters > N_MAX_OUTPUT_REGISTER)
            throw new IllegalArgumentException("there are "+ numberRegisters + " Bigger than Max value" + N_MAX_OUTPUT_REGISTER);

        Number[] output = new Number[numberRegisters];
        for(int i = 0; i< numberRegisters; i++){
            output[i] = NumberConversion.byteToUInt8(data,dataOffset + i);
        }

        Sample temp = new Sample(timestamp,output,getFieldsDesc());
        return new ExtractResult(temp, numberRegisters);
    }
}