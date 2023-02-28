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

import com.st.BlueSTSDK.Feature.Sample;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Node;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * Class containing data needed in a Opus stream decoding
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class OpusManager implements AudioCodecManager {

    private static final String CODEC_NAME = "Opus";

    /* Opus NDK library loading */
    static {
        System.loadLibrary("opusUser");
    }

    private boolean isPlaying = true;

    /* Opus NDK functions */
    /** Opus Decoder Initialization function declaration*/
    private native int OpusDecInit(int sampFreq, int channels);
    /** Opus Decoder Decoding function declaration*/
    private native byte[] OpusDecode(byte[] input, int in_length, int frameSizePcm);
    /** Opus Encoder Initialization function declaration*/
    private native int OpusEncInit(int sampFreq, int channels, int app, int bitrate, boolean cvbr, int complexity);
    /** Opus Encoder Encode function declaration*/
    private native byte[] OpusEncode(short[] input, int encodedFrameSize, int frameSizePcm, int channels);

    /** Opus Decoder parameters */
    private float opusDecFrameSize;
    private int opusDecSamplingFreq;
    private short opusDecChannels;
    private int opusDecFrameSizePCM;
    private short[] opusOutputPCM;

    /** Constructor. It loads default value statically from the Opus Configuration Feature */
    public OpusManager(boolean hasDecoder, boolean hasEncoder) {
        if(hasDecoder){
            this.opusDecFrameSize = FeatureAudioOpusConf.getDecDefaultFrameSize();
            this.opusDecSamplingFreq = FeatureAudioOpusConf.getDecDefaultSamplingFreq();
            this.opusDecChannels = FeatureAudioOpusConf.getDecDefaultChannels();
            this.opusDecFrameSizePCM = FeatureAudioOpusConf.getDecDefaultFrameSizePCM();
            this.opusOutputPCM = new short[opusDecFrameSizePCM];

            OpusDecInit(opusDecSamplingFreq, opusDecChannels);
        }
        if(hasEncoder){
            OpusEncInit(FeatureAudioOpusConf.getEncParams().getEncSamplingFreq(),
                    FeatureAudioOpusConf.getEncParams().getEncChannels(),
                    FeatureAudioOpusConf.getEncParams().getEncApplication(),
                    FeatureAudioOpusConf.getEncParams().getEncBitrate(),
                    FeatureAudioOpusConf.getEncParams().isEncVbr(),
                    FeatureAudioOpusConf.getEncParams().getEncComplexity());
        }
    }

    @Override
    public void reinit() {
    }

    @Override
    public String getCodecName() {
        return CODEC_NAME;
    }

    @Override
    public int getSamplingFreq() {
        return opusDecSamplingFreq;
    }

    @Override
    public short getChannels() {
        return opusDecChannels;
    }

    @Override
    public Boolean isAudioEnabled() {
        return isPlaying;
    }

    int getTransportFrameByteSize(){
        return opusDecFrameSizePCM*2;
    }

    @Override
    public void updateParams(Sample sample){
        if (sample.data[0].byteValue()==FeatureAudioOpusConf.BV_OPUS_CONF_CMD) {
            this.opusDecFrameSize = FeatureAudioOpusConf.getFrameSize(sample);
            this.opusDecSamplingFreq = FeatureAudioOpusConf.getSamplingFreq(sample);
            this.opusDecChannels = FeatureAudioOpusConf.getChannels(sample);
            this.opusDecFrameSizePCM = (int)(((opusDecSamplingFreq /1000)* opusDecFrameSize));
            this.opusOutputPCM = new short[opusDecFrameSizePCM];

            OpusDecInit(opusDecSamplingFreq, opusDecChannels);
        }
        if (sample.data[0].byteValue()==FeatureAudioOpusConf.BV_OPUS_CONTROL) {
            if (sample.data[1].byteValue() == FeatureAudioOpusConf.BV_OPUS_ENABLE_NOTIF_REQ) {
                isPlaying=true;
            } else if (sample.data[1].byteValue() == FeatureAudioOpusConf.BV_OPUS_DISABLE_NOTIF_REQ) {
                isPlaying=false;
            }
        }
    }

    @Override
    public short[] decode(byte[] encodedData){
        byte[] OPUSdecoded = OpusDecode(encodedData, encodedData.length, opusDecFrameSizePCM);
        for (int i = 0; i < (OPUSdecoded.length / 2); i++){
            opusOutputPCM[i] = (short)(((OPUSdecoded[2 * i]&0xFF)) | (OPUSdecoded[2 * i + 1]&0xFF)<<8 );
        }
        return opusOutputPCM;
    }

    @Override
    public byte[] encode(short[] input){
        return OpusEncode(input,
                FeatureAudioOpusConf.getEncParams().getEncFrameSize(),
                FeatureAudioOpusConf.getEncParams().getEncFrameSizePcm(),
                FeatureAudioOpusConf.getEncParams().getEncChannels());
    }

}
