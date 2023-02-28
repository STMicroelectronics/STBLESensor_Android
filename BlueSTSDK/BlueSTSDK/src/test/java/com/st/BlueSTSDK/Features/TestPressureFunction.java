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
import com.st.BlueSTSDK.Utils.NumberConversion;

import org.junit.Assert;
import org.junit.Test;

public class TestPressureFunction {

    @Test
    public void testNullSamplePressure(){
        Assert.assertEquals(Float.NaN, FeaturePressure.getPressure(null), 0.0f);
    }

    @Test
    public void testInvalidSamplePressure(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeaturePressure.getPressure(s), 0.0f);
    }

    @Test
    public void testGetSamplePressure(){
        float x = 50.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeaturePressure.getPressure(s), 0.0f);
    }


    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeaturePressure(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2,3}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeaturePressure(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2,3,4}, 1);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeaturePressure(null);

        float pressure = 12.34f;

        byte temp[] = NumberConversion.LittleEndian.int32ToBytes((short) (pressure * 100));


        UpdateFeatureUtil.callUpdate(f, 1, temp, 0);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{pressure}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeaturePressure(null);
        int offset =4;
        byte data[] = new byte[offset+4];

        float pressure = 12.34f;

        byte temp[] = NumberConversion.LittleEndian.int32ToBytes((short)(pressure*100));
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];
        data[offset+2]=temp[2];
        data[offset+3]=temp[3];


        UpdateFeatureUtil.callUpdate(f, 1, data, offset);

        Assert.assertEquals(new Feature.Sample(1,new Float[]{pressure}, new Field[]{}),
                f.getSample());
    }

}
