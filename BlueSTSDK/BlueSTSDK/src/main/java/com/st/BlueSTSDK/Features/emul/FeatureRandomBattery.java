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
package com.st.BlueSTSDK.Features.emul;

import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeEmulator;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.Random;

/**
 * generate random data for emulate the class {@link FeatureBattery}
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureRandomBattery extends FeatureBattery implements NodeEmulator.EmulableFeature {

    Random mRandom = new Random();
    int mCharge =5;
    public FeatureRandomBattery(Node parent) {
        super(parent);
    }


    @Override
    public byte[] generateFakeData() {
        byte data[] = new byte[7];

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(mCharge*10));
        mCharge = (mCharge+10)%100;
        System.arraycopy(temp, 0, data, 0, 2);

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(mRandom.nextFloat()*1000));
        System.arraycopy(temp, 0, data, 2, 2);

        temp = NumberConversion.LittleEndian.int16ToBytes((short)((mRandom.nextFloat()*10)-5));
        System.arraycopy(temp, 0, data, 4, 2);

        temp = new byte[] {(byte) (mRandom.nextFloat()*4)};
        System.arraycopy(temp, 0, data, 5, 1);

        return data;
    }
}
