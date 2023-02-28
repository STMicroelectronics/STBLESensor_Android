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

import java.util.Date;

public class TestCarryPositionFeature {

    @Test
    public void testNullSamplePositionId(){
        Assert.assertEquals(FeatureCarryPosition.Position.ERROR,
                FeatureCarryPosition.getPosition(null));
    }

    @Test
    public void testInvalidSamplePositionId(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.ERROR,
                FeatureCarryPosition.getPosition(s));
    }

    @Test
    public void testSampleActivityId(){
        byte activityId = 0x02;
        Feature.Sample s = new Feature.Sample(100,new Number[]{activityId}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.IN_HAND,
                FeatureCarryPosition.getPosition(s));
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureCarryPosition(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureCarryPosition(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3, 4, 5, 6,7}, 7);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureCarryPosition(null);
        int offset = 0;
        byte data[] = new byte[offset+7];

        data[offset]=0x00;

        int readByte = UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(1, readByte);
        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertEquals(FeatureCarryPosition.Position.UNKNOWN,
                FeatureCarryPosition.getPosition(f.getSample()));

    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureActivity(null);
        int offset = 0;
        byte data[] = new byte[offset+7];

        data[offset]=0x01;

        int readByte = UpdateFeatureUtil.callUpdate(f, 2, data, offset);

        Assert.assertEquals(1, readByte);
        Assert.assertEquals(2, f.getSample().timestamp);
        Assert.assertEquals(FeatureCarryPosition.Position.ON_DESK,
                FeatureCarryPosition.getPosition(f.getSample()));

    }

    @Test
    public void testStatusValue(){

        Feature.Sample s = new Feature.Sample(100,new Number[]{0,0,0,0x00}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.UNKNOWN,
                FeatureCarryPosition.getPosition(s));

        s = new Feature.Sample(100,new Number[]{0x01}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.ON_DESK,
                FeatureCarryPosition.getPosition(s));

        s = new Feature.Sample(100,new Number[]{0x02}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.IN_HAND,
                FeatureCarryPosition.getPosition(s));

        s = new Feature.Sample(100,new Number[]{0x03}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.NEAR_HEAD,
                FeatureCarryPosition.getPosition(s));

        s = new Feature.Sample(100,new Number[]{0x04}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.SHIRT_POCKET,
                FeatureCarryPosition.getPosition(s));

        s = new Feature.Sample(100,new Number[]{0x05}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.TROUSERS_POCKET,
                FeatureCarryPosition.getPosition(s));

        s = new Feature.Sample(100,new Number[]{0x06}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.ARM_SWING,
                FeatureCarryPosition.getPosition(s));


        //other value are errors
        s = new Feature.Sample(100,new Number[]{0x12}, new Field[]{});
        Assert.assertEquals(FeatureCarryPosition.Position.ERROR,
                FeatureCarryPosition.getPosition(s));

    }

}
