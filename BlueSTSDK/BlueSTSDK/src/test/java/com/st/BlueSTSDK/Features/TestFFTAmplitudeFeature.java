package com.st.BlueSTSDK.Features;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestFFTAmplitudeFeature {

    private class TestableFFTAmplitude extends FeatureFFTAmplitude {



        TestableFFTAmplitude(Node n) {
            super(n);
        }

        Sample getFFTSample(){
            return mLastSample;
        }

    }

    private float[] getRandomArray(int length){
        Random r = new Random();
        float[] data = new float[length];
        for (int i = 0 ; i<length ; i++){
            data[i] = r.nextFloat();
        }
        return data;
    }

    private byte[] encode(float freqSample, float[] x, float[] y, float[] z){
        int nSample = x.length;
        byte data[] = new byte[7+nSample*4*3];

        int offset = 0 ;
        byte temp[] = NumberConversion.LittleEndian.uint16ToBytes(nSample);
        System.arraycopy(temp,0,data,offset,temp.length);
        offset += temp.length;

        temp = new byte[]{3};
        System.arraycopy(temp,0,data,offset,temp.length);
        offset += temp.length;

        temp = NumberConversion.LittleEndian.floatToBytes(freqSample);
        System.arraycopy(temp,0,data,offset,temp.length);
        offset += temp.length;

        for( float val : x){
            temp = NumberConversion.LittleEndian.floatToBytes(val);
            System.arraycopy(temp,0,data,offset,temp.length);
            offset += temp.length;
        }

        for( float val : y){
            temp = NumberConversion.LittleEndian.floatToBytes(val);
            System.arraycopy(temp,0,data,offset,temp.length);
            offset += temp.length;
        }

        for( float val : z){
            temp = NumberConversion.LittleEndian.floatToBytes(val);
            System.arraycopy(temp,0,data,offset,temp.length);
            offset += temp.length;
        }

        return data;
    }

    private List<byte[]> splitArray(byte data[], int length){

        int nSplit = (int)Math.ceil(((double)data.length)/length);

        List<byte[]> out = new ArrayList<>(nSplit);

        for (int i = 0 ; i<nSplit ; i++ ){
            int remainingData = Math.min(length,data.length-i*length);
            byte[] splitData = new byte[remainingData];

            System.arraycopy(data,i*length,splitData,0,remainingData);
            out.add(splitData);
        }

        return  out;
    }


    @Test
    public void testUpdate0Samples() throws Throwable {
        TestableFFTAmplitude f = new TestableFFTAmplitude(null);

        float freqUpdate = 12.34f;

        byte data[] = encode(freqUpdate,new float[0],new float[0],new float[0]);

        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, 2);

        Assert.assertEquals(readBytes,7);

        Feature.Sample s = f.getFFTSample();

        Assert.assertEquals(0,FeatureFFTAmplitude.getNSample(s));
        Assert.assertEquals(3,FeatureFFTAmplitude.getNComponents(s));
        Assert.assertEquals(freqUpdate,FeatureFFTAmplitude.getFreqStep(s),0.001);

        Assert.assertEquals(0,FeatureFFTAmplitude.getDataLoadPercentage(s));

    }

    @Test
    public void testUpdate1Samples() throws Throwable {
        TestableFFTAmplitude f = new TestableFFTAmplitude(null);

        float freqUpdate = 45.67f;

        float x[] = getRandomArray(1);
        float y[] = getRandomArray(1);
        float z[] = getRandomArray(1);

        byte data[] = encode(freqUpdate,x,y,z);

        int readBytes = UpdateFeatureUtil.callUpdate(f, 1, data, 2);

        Assert.assertEquals(readBytes,19);

        Feature.Sample s = f.getFFTSample();

        Assert.assertEquals(1,FeatureFFTAmplitude.getNSample(s));
        Assert.assertEquals(freqUpdate,FeatureFFTAmplitude.getFreqStep(s),0.001);

        Assert.assertEquals(100,FeatureFFTAmplitude.getDataLoadPercentage(s));
        Assert.assertTrue(FeatureFFTAmplitude.isComplete(s));

        Assert.assertArrayEquals(x,FeatureFFTAmplitude.getXComponent(s),0.0001f);
        Assert.assertArrayEquals(x,FeatureFFTAmplitude.getComponent(s,0),0.0001f);
        Assert.assertArrayEquals(y,FeatureFFTAmplitude.getYComponent(s),0.0001f);
        Assert.assertArrayEquals(y,FeatureFFTAmplitude.getComponent(s,1),0.0001f);
        Assert.assertArrayEquals(z,FeatureFFTAmplitude.getZComponent(s),0.0001f);
        Assert.assertArrayEquals(z,FeatureFFTAmplitude.getComponent(s,2),0.0001f);
    }


    @Test
    public void testUpdateWithSplitData() throws Throwable {
        TestableFFTAmplitude f = new TestableFFTAmplitude(null);

        float freqUpdate = 45.67f;
        int sampleSize = 100;
        float x[] = getRandomArray(sampleSize);
        float y[] = getRandomArray(sampleSize);
        float z[] = getRandomArray(sampleSize);

        byte data[] = encode(freqUpdate,x,y,z);
        List<byte[]> splitData = splitArray(data,20);

        for ( byte[] split : splitData){
            int readBytes = UpdateFeatureUtil.callUpdate(f, 1, split, 2);
            Assert.assertEquals(split.length,readBytes);
        }

        Feature.Sample s = f.getFFTSample();

        Assert.assertEquals(sampleSize,FeatureFFTAmplitude.getNSample(s));
        Assert.assertEquals(freqUpdate,FeatureFFTAmplitude.getFreqStep(s),0.001);

        Assert.assertEquals(100,FeatureFFTAmplitude.getDataLoadPercentage(s));
        Assert.assertTrue(FeatureFFTAmplitude.isComplete(s));

        Assert.assertArrayEquals(x,FeatureFFTAmplitude.getXComponent(s),0.0001f);
        Assert.assertArrayEquals(y,FeatureFFTAmplitude.getYComponent(s),0.0001f);
        Assert.assertArrayEquals(z,FeatureFFTAmplitude.getZComponent(s),0.0001f);
    }

}
