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
package com.st.BlueSTSDK.Features.Audio.Opus;

import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
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
public class FeatureAudioOpus extends FeatureAudio {

    public static final String FEATURE_NAME = "AudioOpus";
    public static final String FEATURE_DATA_NAME = "Opus";

    private OpusManager mOpusManager = null;
    private BlueVoiceOpusTransportProtocol mOpusTransportDecoder;

    protected static final Field AUDIO_FIELD =
            new Field(FEATURE_DATA_NAME,null, Field.Type.ByteArray,Byte.MAX_VALUE, Byte.MIN_VALUE);

    /**
     * build an Audio Opus Feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureAudioOpus(Node n) {
        super(FEATURE_NAME, n, new Field[]{ AUDIO_FIELD });
    }

    protected FeatureAudioOpus(String name, Node n, Field data[]) {
        super(name,n,data);
        if(data[0]!=AUDIO_FIELD){
            throw new IllegalArgumentException("First data[0] must be FeatureAudioOpus" +
                    ".AUDIO_FIELD");
        }//if
    }

    /**
     * needed for opus packet reconstruction
     * @param manager
     */
    public void setAudioCodecManager(AudioCodecManager manager){
        mOpusManager = (OpusManager) manager;
        mOpusTransportDecoder = new BlueVoiceOpusTransportProtocol(mOpusManager.getTransportFrameByteSize());
    }

    @Nullable
    public short[] getAudio(Feature.Sample sample) {

        if (sample != null && sample.data[0] != null){
            int length = sample.data.length;
            short[] audioPckt = new short[length];
            getAudio(sample,audioPckt);
            return audioPckt;
        }
        //else
        return null;
    }

    /**
     * extract the audio sample from a feature sample, this function is useful for avoid to allocate
     * a new array at each sample
     * @param sample ble sample
     * @param outData array where store the audio sample
     * @return true if the sample is a valid sample
     */
    public static boolean getAudio(Feature.Sample sample, short outData[]){
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
     * extract the audio data from the node raw data
     *
     * @param data       array where read the Field data (a 20 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes (20) and data extracted (the audio information)
     * @throws IllegalArgumentException if the data array has not the correct number of elements
     */
    @Override
    protected Feature.ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {

        byte[] opusFrame = mOpusTransportDecoder.unpackData(data);
        if (opusFrame != null) {
            short[] decodedData = mOpusManager.decode(opusFrame);
            Number[] dataPkt = new Number[decodedData.length];

            for (int i = 0; i < decodedData.length; i++) {
                dataPkt[i] = decodedData[i];
            }
            Feature.Sample audioData = new Feature.Sample(dataPkt,getFieldsDesc());
            return new Feature.ExtractResult(audioData,dataPkt.length);
        }
        return new Feature.ExtractResult(null,0);

    }//update
}
