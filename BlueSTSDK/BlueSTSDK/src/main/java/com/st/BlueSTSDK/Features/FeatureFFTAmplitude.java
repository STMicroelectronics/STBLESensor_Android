/*******************************************************************************
 * COPYRIGHT(c) 2019 STMicroelectronics
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

import androidx.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeatureFFTAmplitude extends DeviceTimestampFeature {
    public static final String FEATURE_NAME = "FFT Amplitude";
    /**
     * build a proximity feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureFFTAmplitude(Node n) {
        super(FEATURE_NAME, n , new Field[]{
                new Field("ReceiveStatus","%",Field.Type.UInt8,0,100),
                new Field("N Sample",null,Field.Type.UInt16,0,(1<<16)-1),
                new Field("N Components",null,Field.Type.UInt8,0,(1<<8)-1),
                new Field("Frequency Step","Hz",Field.Type.Float,0,Float.MAX_VALUE)
        });
    }//FeatureProximity

    public static boolean isComplete(Sample s){
        if (s instanceof  FFTSample){
            return ((FFTSample) s).isComplete();
        }
        return false;
    }

    public static byte getDataLoadPercentage(Sample s){
        if (s instanceof  FFTSample){
            return ((FFTSample) s).getDataLoadPercentage();
        }
        return -1;
    }

    public static int getNSample(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).nSample;
        return 0;
    }

    public static int getNComponents(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).nComponents;
        return 0;
    }

    public static float getFreqStep(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).freqStep;
        return 0;
    }

    public static float[] getXComponent(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(0);
        return new float[0];
    }

    public static float[] getYComponent(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(1);
        return new float[0];
    }

    public static float[] getZComponent(Sample s){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(2);
        return new float[0];
    }

    public static float[] getComponent(Sample s, int index){
        if(s instanceof FFTSample)
            return ((FFTSample) s).getComponent(index);
        return new float[0];
    }

    public static List<float[]> getComponents(Sample s){
        if (!(s instanceof  FFTSample)){
            return Collections.emptyList();
        }
        FFTSample sample = (FFTSample)s;
        int nComponents = sample.nComponents;
        List<float[]>components = new ArrayList<>(nComponents);
        for (int i = 0; i < nComponents; i++) {
            components.add(sample.getComponent(i));
        }
        return Collections.unmodifiableList(components);
    }


    private static class FFTSample extends Sample{

        final int nSample;
        final short nComponents;
        final float freqStep;

        private byte[] rawData;
        private int nLastData;

        FFTSample(long timestamp, @NonNull Field[] dataDesc,int nSample, short nComponents, float freqStep ) {
            super(timestamp, new Number[]{0,nSample,nComponents,freqStep}, dataDesc);
            this.nSample = nSample;
            this.nComponents = nComponents;
            this.freqStep = freqStep;
            rawData = new byte[nSample*nComponents*4]; // components * 4 byte each float
            nLastData =0;
        }

        void appendData(byte[] data , int offset){
            int spaceAvailable = rawData.length-nLastData;
            int dataAvailable = data.length - offset;
            int dataToCopy = Math.min(spaceAvailable,dataAvailable);
            System.arraycopy(data,offset,rawData,nLastData,dataToCopy);
            nLastData += dataToCopy;
            super.data[0] = getDataLoadPercentage();
        }

        byte getDataLoadPercentage(){
            if(rawData.length == 0)
                return 0;
            return (byte) ((nLastData*100)/rawData.length);
        }

        boolean isComplete(){
            return nLastData == rawData.length;
        }

        private static float[] extractFloat(byte rawData[],int startOffset, int nFloat){
            float[] out = new float[nFloat];
            for (int i = 0 ; i<nFloat ; i++){
                out[i] = NumberConversion.LittleEndian.bytesToFloat(rawData,startOffset+4*i);
            }
            return out;
        }

        float[] getComponent(int index){
            if(index>=nComponents)
                throw new IllegalArgumentException("Max component is "+nComponents);
            int startOffset = index * nSample *4;
            return extractFloat(rawData,startOffset,nSample);
        }


    }

    private FFTSample mPartialSample;

    private FFTSample readHeaderData(long timestamp, byte[] data, int dataOffset){
        if(data.length-dataOffset < 7){
            throw new IllegalArgumentException("There are no 7 bytes available to read");
        }
        //Log.d("FFT",Arrays.toString(data));

        int nSample =  NumberConversion.LittleEndian.bytesToUInt16(data, dataOffset);
        short nComponents =  NumberConversion.byteToUInt8(data, dataOffset+2);
        float freqStep = NumberConversion.LittleEndian.bytesToFloat(data, dataOffset+3);

        FFTSample sample = new FFTSample(timestamp,getFieldsDesc(),nSample,nComponents,freqStep);

        sample.appendData(data,dataOffset+7);

        return sample;
    }

    @Override
    public void enableNotification() {
        mPartialSample=null;
        super.enableNotification();
    }

    @Override
    public void disableNotification(){
        mPartialSample=null;
        super.disableNotification();
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        Sample returnSample;
        if(mPartialSample == null){
            mPartialSample = readHeaderData(timestamp,data,dataOffset);
            returnSample = mPartialSample;
        }else{
            mPartialSample.appendData(data,dataOffset);
            returnSample = mPartialSample;
            if(mPartialSample.isComplete()){
                mPartialSample = null;
            }
        }
        return new ExtractResult(returnSample,data.length);
    }
}
