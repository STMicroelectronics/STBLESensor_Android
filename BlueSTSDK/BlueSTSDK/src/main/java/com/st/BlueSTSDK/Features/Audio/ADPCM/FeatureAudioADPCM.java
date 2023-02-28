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

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.FeatureAudio;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

/**
 * Feature that contains the compressed audio data acquired form a microphone.
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureAudioADPCM extends FeatureAudio {

    public static final String FEATURE_NAME = "Audio";
    public static final String FEATURE_DATA_NAME = "ADPCM";

    /**
     * number of sample received for each feature notification
     */
    public static final int AUDIO_PACKAGE_SIZE=40;

    private ADPCMManager mBVBvAudioSyncManager =null;

    protected static final Field AUDIO_FIELD = new Field(FEATURE_DATA_NAME,null, Field.Type.ByteArray,-128,127);

    /**
     * build an Audio ADPCM Feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureAudioADPCM(Node n) {
        super(FEATURE_NAME, n, new Field[]{ AUDIO_FIELD });
    }

    protected FeatureAudioADPCM(String name, Node n, Field data[]) {
        super(name,n,data);
        if(data[0]!=AUDIO_FIELD){
            throw new IllegalArgumentException("First data[0] must be FeatureAudioADPCM" +
                    ".AUDIO_FIELD");
        }//if
    }

    public short[] getAudio(Sample sample) {

        if (sample != null && sample.data!=null){
            int length = sample.data.length;
            short[] audioPckt = new short[length];
            getAudio(sample,audioPckt);
            return audioPckt;
        }
        //else
        return new short[]{};
    }

    /**
     * extract the audio sample from a feature sample, this function is useful for avoid to allocate
     * a new array at each sample
     * @param sample ble sample
     * @param outData array where store the audio sample
     * @return true if the sample is a valid sample
     */
    public static boolean getAudio(Sample sample, short outData[]){
        if (sample != null && sample.data!=null){
            int length = Math.min(sample.data.length,outData.length);
            for(int i = 0 ; i < length ; i++){
                if (sample.data[i] != null)
                    outData[i] = sample.data[i].shortValue();
            }
            return true;
        }
        return false;
    }


    /**
     * set the object synchronization parameters necessary to the decompression process
     * @param manager struct which contains the synchronization parameters
     */
    public void setAudioCodecManager(AudioCodecManager manager){
        mBVBvAudioSyncManager = ((ADPCMManager)manager);
    }

    /**
     * extract the audio data from the node raw data, in this case it read an array of 40 shorts.
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes (20) and data extracted (the audio information, the 40 shorts array)
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if(data.length == 20){
            short[] decodedData = mBVBvAudioSyncManager.decode(data);
            Number[] dataPkt = new Number[decodedData.length];
            for (int i=0; i<decodedData.length; i++) {
               dataPkt[i] = decodedData[i];
            }
            Sample audioData = new Sample(dataPkt,getFieldsDesc());
            return new ExtractResult(audioData,20);
        }
        else{
            throw new IllegalArgumentException("There are no 20 bytes available to read");
        }
    }//update

}
