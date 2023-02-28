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
package com.st.BlueSTSDK.Features.Audio.ADPCM;


import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.EncParams;
import com.st.BlueSTSDK.Node;

/**
 * Class containing the sync data needed in a ADPCM stream decoding
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class ADPCMManager implements AudioCodecManager {

    private static final String CODEC_NAME = "ADPCM";
    private static final int SAMPLING_FREQ = 8000;
    private static final short CHANNELS = 1;

    private boolean intra_flag=false;
    private short adpcm_index_in=0;
    private int adpcm_predsample_in=0;


    @Override
    public void reinit(){
        intra_flag = false;
    }

    @Override
    public String getCodecName() {
        return CODEC_NAME;
    }

    @Override
    public int getSamplingFreq() {
        return SAMPLING_FREQ;
    }

    @Override
    public short getChannels() {
        return CHANNELS;
    }

    @Override
    public Boolean isAudioEnabled() {
        return null;
    }

    @Override
    public void updateParams(Feature.Sample sample){
        synchronized (this) {
            adpcm_index_in = FeatureAudioADPCMSync.getIndex(sample);
            adpcm_predsample_in = FeatureAudioADPCMSync.getPredictedSample(sample);
            intra_flag = true;
        }
    }

    @Override
    public byte[] encode(short[] input) {
        //Empty for now
        return null;
    }

    @Override
    public short[] decode(byte[] encodeData) {
        short[] decodedData = new short[encodeData.length * 2];
        for (int i=0; i<encodeData.length; i++) {
            decodedData[2*i] = decode((byte)(encodeData[i] & 0x0F));
            decodedData[(2*i)+1] = decode((byte)((encodeData[i] >> 4) & 0x0F));
        }
        return decodedData;
    }


    /** Quantizer step size lookup table */
    private static final short[] StepSizeTable={7,8,9,10,11,12,13,14,16,17,
            19,21,23,25,28,31,34,37,41,45,
            50,55,60,66,73,80,88,97,107,118,
            130,143,157,173,190,209,230,253,279,307,
            337,371,408,449,494,544,598,658,724,796,
            876,963,1060,1166,1282,1411,1552,1707,1878,2066,
            2272,2499,2749,3024,3327,3660,4026,4428,4871,5358,
            5894,6484,7132,7845,8630,9493,10442,11487,12635,13899,
            15289,16818,18500,20350,22385,24623,27086,29794,32767};

    /** Table of index changes */
    private static final byte[] IndexTable = {-1,-1,-1,-1,2,4,6,8,-1,-1,-1,-1,2,4,6,8};

    private short index=0;
    private int predsample=0;

    /**
     * ADPCM_Decode.
     * @param code: a byte containing a 4-bit ADPCM sample.
     * @return : a struct which contains a 16-bit ADPCM sample
     */
    private short decode(byte code) {
        short step;
        int diffq;

        if(intra_flag) {
            predsample = adpcm_predsample_in;
            index = adpcm_index_in;
            reinit();
        }
        step = StepSizeTable[index];

        /* 2. inverse code into diff */
        diffq = step>> 3;
        if ((code&4)!=0)
        {
            diffq += step;
        }

        if ((code&2)!=0)
        {
            diffq += step>>1;
        }

        if ((code&1)!=0)
        {
            diffq += step>>2;
        }

        /* 3. add diff to predicted sample*/
        if ((code&8)!=0)
        {
            predsample -= diffq;
        }
        else
        {
            predsample += diffq;
        }

        /* check for overflow*/
        if (predsample > 32767)
        {
            predsample = 32767;
        }
        else if (predsample < -32768)
        {
            predsample = -32768;
        }

        /* 4. find new quantizer step size */
        index += IndexTable [code];
        /* check for overflow*/
        if (index < 0)
        {
            index = 0;
        }
        if (index > 88)
        {
            index = 88;
        }

        /* 5. save predict sample and index for next iteration */
        /* done! static variables */

        /* 6. return new speech sample*/
        return (short)predsample;
    }
}

