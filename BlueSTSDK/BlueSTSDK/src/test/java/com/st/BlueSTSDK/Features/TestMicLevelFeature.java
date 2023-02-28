package com.st.BlueSTSDK.Features;


import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Utils.NumberConversion;

import org.junit.Assert;
import org.junit.Test;

public class TestMicLevelFeature {

    @Test
    public void testNullInvalidSample(){
        Assert.assertTrue(FeatureMicLevel.getMicLevel(null,0)<0);
        Assert.assertTrue(FeatureMicLevel.getMicLevel(null,10)<0);
    }


    @Test
    public void testInvalidSample(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertTrue(FeatureMicLevel.getMicLevel(s,0)<0);
    }

    @Test
    public void testGetSample(){
        byte level = 123;
        Feature.Sample s = new Feature.Sample(100,new Number[]{level}, new Field[]{});
        Assert.assertEquals(level, FeatureMicLevel.getMicLevel(s, 0));
    }

    @Test
    public void testInvalidSample2(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{0}, new Field[]{});
        Assert.assertTrue(FeatureMicLevel.getMicLevel(s, 1) < 0);
    }

    @Test
    public void testGetSample2(){
        byte level = 123;
        Feature.Sample s = new Feature.Sample(100,new Number[]{0,level}, new Field[]{});
        Assert.assertEquals(level, FeatureMicLevel.getMicLevel(s, 1));
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureMicLevel(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureMicLevel(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2, 3}, 3);
    }

    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeatureMicLevel(null);
        byte data[] = new byte[]{1,2,3};

        UpdateFeatureUtil.callUpdate(f, 1, data, 0);

        Assert.assertEquals(new Feature.Sample(1, new Byte[]{data[0], data[1], data[2]},
                new Field[]{}), f.getSample());
        Assert.assertEquals(data.length,f.getFieldsDesc().length);
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeatureMicLevel(null);
        byte data[] = new byte[]{0,0,0,1,2,3};

        UpdateFeatureUtil.callUpdate(f, 2, data, 3);

        Assert.assertEquals(new Feature.Sample(2, new Byte[]{data[3], data[4], data[5]}, new Field[]{}),
                f.getSample());
        Assert.assertEquals(data.length-3, f.getFieldsDesc().length);

    }

}
