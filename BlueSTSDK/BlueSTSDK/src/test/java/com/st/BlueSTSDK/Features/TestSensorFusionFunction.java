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

public class TestSensorFusionFunction {

    @Test
    public void testNullSampleQi(){
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQi(null), 0.0f);
    }

    @Test
    public void testInvalidSampleQi(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQi(s), 0.0f);
    }

    @Test
    public void testGetSampleQi(){
        float x = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeatureMemsSensorFusion.getQi(s), 0.0f);
    }

    @Test
    public void testNullSampleQj(){
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQj(null), 0.0f);
    }

    @Test
    public void testInvalidSampleQj(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQj(s), 0.0f);
    }

    @Test
    public void testGetSampleQj(){
        float y = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,y}, new Field[]{});
        Assert.assertEquals(y, FeatureMemsSensorFusion.getQj(s), 0.0f);
    }

    @Test
    public void testNullSampleQk(){
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQk(null), 0.0f);
    }


    @Test
    public void testInvalidSampleQk(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,0.0f}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQk(s), 0.0f);
    }

    @Test
    public void testGetSampleQk(){
        float z = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,0.0f,z}, new Field[]{});
        Assert.assertEquals(z, FeatureMemsSensorFusion.getQk(s), 0.0f);
    }

    @Test
    public void testNullSampleQs(){
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQs(null), 0.0f);
    }

    @Test
    public void testInvalidSampleQs(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,0.0f,0.0f}, new Field[]{});
        Assert.assertEquals(Float.NaN, FeatureMemsSensorFusion.getQs(s), 0.0f);
    }

    @Test
    public void testGetSampleQs(){
        float w = 1.0f;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0.0f,0.0f,0.0f,w}, new Field[]{});
        Assert.assertEquals(w, FeatureMemsSensorFusion.getQs(s), 0.0f);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        FeatureMemsSensorFusion f = new FeatureMemsSensorFusion(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        FeatureMemsSensorFusion f = new FeatureMemsSensorFusion(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, 1);
    }


    @Test
    public void testUpdateFull() throws Throwable {
        Feature f = new FeatureMemsSensorFusion(null);
        byte data[] = new byte[4*4];

        float x=1.0f,y=2.0f,z=3.0f,w=4.0f;
        final double norm = Math.sqrt(x*x+y*y+z*z+w*w);
        x /= norm;
        y /= norm;
        z /= norm;
        w /= norm;

        byte temp[] = NumberConversion.LittleEndian.floatToBytes(x);
        data[0]=temp[0];
        data[1]=temp[1];
        data[2]=temp[2];
        data[3]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(y);
        data[4]=temp[0];
        data[5]=temp[1];
        data[6]=temp[2];
        data[7]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(z);
        data[8]=temp[0];
        data[9]=temp[1];
        data[10]=temp[2];
        data[11]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(w);
        data[12]=temp[0];
        data[13]=temp[1];
        data[14]=temp[2];
        data[15]=temp[3];

        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, 0);

        Assert.assertEquals(readBytes,16);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{x,y,z,w}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateFullWithOffset() throws Throwable {
        Feature f = new FeatureMemsSensorFusion(null);
        int offset = 5;
        byte data[] = new byte[offset+4*4];

        float x=1.0f,y=2.0f,z=3.0f,w=4.0f;
        final double norm = Math.sqrt(x*x+y*y+z*z+w*w);
        x /= norm;
        y /= norm;
        z /= norm;
        w /= norm;

        byte temp[] = NumberConversion.LittleEndian.floatToBytes(x);
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];
        data[offset+2]=temp[2];
        data[offset+3]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(y);
        data[offset+4]=temp[0];
        data[offset+5]=temp[1];
        data[offset+6]=temp[2];
        data[offset+7]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(z);
        data[offset+8]=temp[0];
        data[offset+9]=temp[1];
        data[offset+10]=temp[2];
        data[offset+11]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(w);
        data[offset+12]=temp[0];
        data[offset+13]=temp[1];
        data[offset+14]=temp[2];
        data[offset+15]=temp[3];

        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, offset);

        Assert.assertEquals(readBytes,16);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{x,y,z,w}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateMissingScalar() throws Throwable {
        Feature f = new FeatureMemsSensorFusion(null);
        byte data[] = new byte[3*4];

        float x=1.0f,y=2.0f,z=3.0f,w=4.0f;
        final double norm = Math.sqrt(x * x + y * y + z * z + w * w);
        x /= norm;
        y /= norm;
        z /= norm;
        w /= norm;

        byte temp[] = NumberConversion.LittleEndian.floatToBytes(x);
        data[0]=temp[0];
        data[1]=temp[1];
        data[2]=temp[2];
        data[3]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(y);
        data[4]=temp[0];
        data[5]=temp[1];
        data[6]=temp[2];
        data[7]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(z);
        data[8]=temp[0];
        data[9]=temp[1];
        data[10]=temp[2];
        data[11]=temp[3];


        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, 0);

        Assert.assertEquals(readBytes, 12);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{x,y,z,w}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateMissingScalarWithOffset() throws Throwable {
        Feature f = new FeatureMemsSensorFusion(null);
        int offset = 4;
        byte data[] = new byte[offset+3*4];

        float x=1.0f,y=2.0f,z=3.0f,w=4.0f;
        final double norm = Math.sqrt(x * x + y * y + z * z + w * w);
        x /= norm;
        y /= norm;
        z /= norm;
        w /= norm;

        byte temp[] = NumberConversion.LittleEndian.floatToBytes(x);
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];
        data[offset+2]=temp[2];
        data[offset+3]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(y);
        data[offset+4]=temp[0];
        data[offset+5]=temp[1];
        data[offset+6]=temp[2];
        data[offset+7]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(z);
        data[offset+8]=temp[0];
        data[offset+9]=temp[1];
        data[offset+10]=temp[2];
        data[offset+11]=temp[3];


        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, offset);

        Assert.assertEquals(readBytes, 12);

        Assert.assertEquals(new Feature.Sample(1, new Float[]{x,y,z,w}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateNoNormalized() throws Throwable {
        Feature f = new FeatureMemsSensorFusion(null);
        byte data[] = new byte[4*4];

        float x=1.0f,y=2.0f,z=3.0f,w=4.0f;
        final double norm = Math.sqrt(x*x+y*y+z*z+w*w);

        byte temp[] = NumberConversion.LittleEndian.floatToBytes(x);
        data[0]=temp[0];
        data[1]=temp[1];
        data[2]=temp[2];
        data[3]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(y);
        data[4]=temp[0];
        data[5]=temp[1];
        data[6]=temp[2];
        data[7]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(z);
        data[8]=temp[0];
        data[9]=temp[1];
        data[10]=temp[2];
        data[11]=temp[3];

        temp = NumberConversion.LittleEndian.floatToBytes(w);
        data[12]=temp[0];
        data[13]=temp[1];
        data[14]=temp[2];
        data[15]=temp[3];

        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, 0);

        Assert.assertEquals(readBytes, 16);

        x /= norm;
        y /= norm;
        z /= norm;
        w /= norm;

        Assert.assertEquals(new Feature.Sample(1, new Float[]{x,y,z,w}, new Field[]{}),
                f.getSample());
    }

/*
    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureMemsSensorFusion(null);
        int offset =4;
        byte data[] = new byte[offset+2];

        float temperature = 12.3f;

        byte temp[] = NumberConversion.LittleEndian.int16ToBytes((short)(temperature*10));
        data[offset+0]=temp[0];
        data[offset+1]=temp[1];

        UpdateFeatureUtil.callUpdate(f, 1, data, offset);

        Assert.assertEquals(new Feature.Sample(1,new Float[]{temperature}),
                f.getSample());
    }
*/
}
