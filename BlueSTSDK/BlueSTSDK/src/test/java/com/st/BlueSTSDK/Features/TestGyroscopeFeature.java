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

public class TestGyroscopeFeature {

    @Test
    public void testNullSampleX(){
        Assert.assertEquals(Float.NaN, FeatureGyroscope.getGyroX(null), 0.0f);
    }

    @Test
    public void testInvalidSampleX(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureGyroscope.getGyroX(s), 0.0f);
    }

    @Test
    public void testGetSampleX(){
        float x = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeatureGyroscope.getGyroX(s), 0.0f);
    }

    @Test
    public void testNullSampleY(){
        Assert.assertEquals(Float.NaN, FeatureGyroscope.getGyroY(null), 0.0f);
    }

    @Test
    public void testInvalidSampleY(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0}, new Field[]{});
        Assert.assertEquals(Float.NaN,FeatureGyroscope.getGyroY(s),0.0f);
    }

    @Test
    public void testGetSampleY(){
        float y = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,y}, new Field[]{});
        Assert.assertEquals(y, FeatureGyroscope.getGyroY(s), 0.0f);
    }

    @Test
    public void testNullSampleZ(){
        Assert.assertEquals(Float.NaN, FeatureGyroscope.getGyroZ(null), 0.0f);
    }

    @Test
    public void testInvalidSampleZ(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0}, new Field[]{});
        Assert.assertEquals(Float.NaN,FeatureGyroscope.getGyroZ(s),0.0f);
    }

    @Test
    public void testGetSampleZ(){
        float z = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,z}, new Field[]{});
        Assert.assertEquals(z, FeatureGyroscope.getGyroZ(s), 0.0f);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureGyroscope(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureGyroscope(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6}, 1);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureGyroscope(null);
        byte data[] = new byte[6];

        short x=10,y=-100,z=300;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(x*10));
        data[0]=temp[0];
        data[1]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(y*10));
        data[2]=temp[0];
        data[3]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(z*10));
        data[4]=temp[0];
        data[5]=temp[1];

        UpdateFeatureUtil.callUpdate(f, 1, data, 0);

        Assert.assertEquals(new Feature.Sample(1,new Float[]{(float)x,(float)y,(float)z}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureGyroscope(null);
        int offset = 4;
        byte data[] = new byte[offset+6];

        short x=10,y=-100,z=300;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(x*10));
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(y*10));
        data[offset+2]=temp[0];
        data[offset+3]=temp[1];

        temp = NumberConversion.LittleEndian.int16ToBytes((short)(z*10));
        data[offset+4]=temp[0];
        data[offset+5]=temp[1];

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(new Feature.Sample(2,new Float[]{(float)x,(float)y,(float)z}, new Field[]{}),
                f.getSample());
    }


}
