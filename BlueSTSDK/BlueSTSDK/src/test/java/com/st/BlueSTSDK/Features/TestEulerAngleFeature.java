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

public class TestEulerAngleFeature {

    @Test
    public void testNullSampleYaw(){
        Assert.assertEquals(Float.NaN, FeatureEulerAngle.getYaw(null), 0.0f);
    }

    @Test
    public void testInvalidSampleYaw(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureEulerAngle.getYaw(s), 0.0f);
    }

    @Test
    public void testGetSampleYaw(){
        float x = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeatureEulerAngle.getYaw(s), 0.0f);
    }

    @Test
    public void testNullSamplePitch(){
        Assert.assertEquals(Float.NaN, FeatureEulerAngle.getPitch(null), 0.0f);
    }

    @Test
    public void testInvalidSamplePitch(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureEulerAngle.getPitch(s), 0.0f);
    }

    @Test
    public void testGetSamplePitch(){
        float y = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,y}, new Field[]{});
        Assert.assertEquals(y, FeatureEulerAngle.getPitch(s), 0.0f);
    }

    @Test
    public void testNullSampleRoll(){
        Assert.assertEquals(Float.NaN, FeatureEulerAngle.getRoll(null), 0.0f);
    }


    @Test
    public void testInvalidSampleRoll(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,0.0f}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureEulerAngle.getRoll(s), 0.0f);
    }

    @Test
    public void testGetSampleRoll(){
        float z = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,0.0f,z}, new Field[]{});
        Assert.assertEquals(z, FeatureEulerAngle.getRoll(s), 0.0f);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        FeatureEulerAngle f = new FeatureEulerAngle(null);
        UpdateFeatureUtil.callUpdate(f, 100, UpdateFeatureUtil.gerRandomArray(11), 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        FeatureEulerAngle f = new FeatureEulerAngle(null);
        UpdateFeatureUtil.callUpdate(f, 100, UpdateFeatureUtil.gerRandomArray(12), 1);
    }

    private byte[] encode(float yaw,float pitch,float roll){
        byte data[] = new byte[3*4];
        byte temp[] = NumberConversion.LittleEndian.floatToBytes(yaw);
        System.arraycopy(temp,0,data,0,4);
        temp = NumberConversion.LittleEndian.floatToBytes(pitch);
        System.arraycopy(temp,0,data,4,4);
        temp = NumberConversion.LittleEndian.floatToBytes(roll);
        System.arraycopy(temp,0,data,8,4);
        return data;
    }


    @Test
    public void testUpdateFull() throws Throwable {
        Feature f = new FeatureEulerAngle(null);
        float yaw = 10.0f;
        float pitch = 15.0f;
        float roll = -89.0f;
        byte data[] = encode(yaw,pitch,roll);

        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, 0);

        Assert.assertEquals(readBytes,12);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{yaw,pitch,roll}, f.getFieldsDesc()),
                f.getSample());
    }

}
