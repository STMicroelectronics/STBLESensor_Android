package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.TestUtil.TestUtil;
import com.st.BlueSTSDK.Utils.NumberConversion;

import org.junit.Assert;
import org.junit.Test;


public class TestPedometerFeature {

    @Test
    public void testNullStepsSample(){
        Assert.assertTrue( FeaturePedometer.getSteps(null)<0);
    }

    @Test
    public void testInvalidSampleSteps(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertTrue(FeaturePedometer.getSteps(s)<0);
    }

    @Test
    public void testGetSampleSteps(){
        int steps = 123;
        Feature.Sample s = new Feature.Sample(100,new Number[]{steps}, new Field[]{});
        Assert.assertEquals(steps, FeaturePedometer.getSteps(s));
    }


    @Test
    public void testNullFequencySample(){
        Assert.assertTrue(FeaturePedometer.getFrequency(null) < 0);
    }

    @Test
    public void testInvalidSampleFrequency(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{2}, new Field[]{});
        Assert.assertTrue(FeaturePedometer.getFrequency(null) < 0);
    }

    @Test
    public void testGetSampleFrequency(){
        int fequency = 100;
        Feature.Sample s = new Feature.Sample(100,new Number[]{2,fequency}, new Field[]{});
        Assert.assertEquals(fequency, FeaturePedometer.getFrequency(s));
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeaturePedometer(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2,3,4,5}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeaturePedometer(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1, 2,3,4,5,6}, 1);
    }


    @Test
    public void testUpdate() throws Throwable {
        Feature f = new FeaturePedometer(null);

        long nStep = 12345;
        int freq = 678;
        byte nStepArray[] = NumberConversion.LittleEndian.uint32ToBytes(nStep);
        byte freqArray[] = NumberConversion.LittleEndian.uint16ToBytes(freq);

        UpdateFeatureUtil.callUpdate(f, 1, TestUtil.mergeArray(nStepArray, freqArray), 0);

        Assert.assertEquals(new Feature.Sample(1, new Number[]{nStep, freq}, new Field[]{}),
                f.getSample());
    }

    @Test
    public void testUpdateOffset() throws Throwable {
        Feature f = new FeaturePedometer(null);
        int offset = 4;

        long nStep = 12345;
        int freq = 678;
        byte nStepArray[] = NumberConversion.LittleEndian.uint32ToBytes(nStep);
        byte freqArray[] = NumberConversion.LittleEndian.uint16ToBytes(freq);
        byte offsetArray[] = new byte[offset];

        UpdateFeatureUtil.callUpdate(f, 1, TestUtil.mergeArray(offsetArray,
                TestUtil.mergeArray (nStepArray, freqArray)), offset);

        Assert.assertEquals(new Feature.Sample(1, new Number[]{nStep,freq}, new Field[]{}),
                f.getSample());
    }


}
