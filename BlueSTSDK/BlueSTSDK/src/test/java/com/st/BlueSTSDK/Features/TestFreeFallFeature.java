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

import org.junit.Assert;
import org.junit.Test;

public class TestFreeFallFeature {

    @Test
    public void testNullFreeFallStatus(){
        Assert.assertFalse(FeatureFreeFall.getFreeFallStatus(null));
    }

    @Test
    public void testInvalidFreeFallStatus(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertFalse(FeatureFreeFall.getFreeFallStatus(s));
    }

    @Test
    public void testSampleActivity(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{1}, new Field[]{});
        Assert.assertTrue(FeatureFreeFall.getFreeFallStatus(s));
    }


    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureFreeFall(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureFreeFall(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6,7}, 7);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureFreeFall(null);
        int offset = 0;
        byte data[] = new byte[offset+1];
        data[offset]=1;

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertTrue(FeatureFreeFall.getFreeFallStatus(f.getSample()));

        data[offset]=0;

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);
        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertFalse(FeatureFreeFall.getFreeFallStatus(f.getSample()));
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureFreeFall(null);
        int offset = 8;
        byte data[] = new byte[offset+1];
        data[offset]=1;

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertTrue(FeatureFreeFall.getFreeFallStatus(f.getSample()));

        data[offset]=0;

        UpdateFeatureUtil.callUpdate(f, 2, data, offset);
        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertFalse(FeatureFreeFall.getFreeFallStatus(f.getSample()));

    }

}
