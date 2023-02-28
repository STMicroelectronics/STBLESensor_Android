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

public class TestTemperatureFunction {

    @Test
    public void testNullSampleTemperature(){
        Assert.assertEquals(Float.NaN, FeatureTemperature.getTemperature(null), 0.0f);
    }

    @Test
    public void testInvalidSampleTemperature(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureTemperature.getTemperature(s), 0.0f);
    }

    @Test
    public void testGetSampleTemperature(){
        float x = 50.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeatureTemperature.getTemperature(s), 0.0f);
    }


    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureTemperature(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{4}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureTemperature(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2}, 1);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureTemperature(null);

        float temperature = 12.3f;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(temperature*10));

        UpdateFeatureUtil.callUpdate(f, 1, temp, 0);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{temperature}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureTemperature(null);
        int offset =4;
        byte data[] = new byte[offset+2];

        float temperature = 12.3f;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(temperature*10));
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];

        UpdateFeatureUtil.callUpdate(f, 1, data, offset);

        Assert.assertEquals(new Feature.Sample(1,new Float[]{temperature}, new Field[]{}),
                f.getSample());
    }

}
