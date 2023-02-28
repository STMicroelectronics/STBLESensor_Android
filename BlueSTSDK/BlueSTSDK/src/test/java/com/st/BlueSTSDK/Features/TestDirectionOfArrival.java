package com.st.BlueSTSDK.Features;


import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.TestUtil.TestUtil;
import com.st.BlueSTSDK.Utils.NumberConversion;

import org.junit.Assert;
import org.junit.Test;

public class TestDirectionOfArrival {

    @Test
    public void testNullSampleDirectionOfArrival(){
        Assert.assertEquals(Short.MIN_VALUE, FeatureDirectionOfArrival.getSoundAngle(null));
    }

    @Test
    public void testInvalidSampleDirectionOfArrival(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(Short.MIN_VALUE, FeatureDirectionOfArrival.getSoundAngle(s));
    }

    @Test
    public void testGetSampleDirectionOfArrival(){
        short x = 50;
        Feature.Sample s = new Feature.Sample(100,new Number[]{x}, new Field[]{});
        Assert.assertEquals(x, FeatureDirectionOfArrival.getSoundAngle(s));
    }


    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureDirectionOfArrival(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureDirectionOfArrival(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2}, 1);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureDirectionOfArrival(null);

        short angle = 123;
        byte temp[] = NumberConversion.LittleEndian.int16ToBytes(angle);

        UpdateFeatureUtil.callUpdate(f, 1, temp, 0);

        Assert.assertEquals(new Feature.Sample(1, new Number[]{angle}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureDirectionOfArrival(null);
        int offset =4;

        short angle = 123;
        byte temp[] = NumberConversion.LittleEndian.int16ToBytes(angle);

        UpdateFeatureUtil.callUpdate(f, 1, TestUtil.mergeArray(new byte[offset],temp), offset);

        Assert.assertEquals(new Feature.Sample(1, new Number[]{angle}, new Field[]{}),
                f.getSample());
    }

}
