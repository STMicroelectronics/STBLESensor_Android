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

import androidx.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.EncParams;
import com.st.BlueSTSDK.Features.Audio.FeatureAudioConf;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

/**
 * Feature that contains commands related to audio transmission (using FeatureAudioOpus).
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureAudioOpusConf extends FeatureAudioConf {

    public static final String FEATURE_NAME = "Audio Opus Conf";

    /** name of the exported data */
    public static final String[] FEATURE_DATA_NAME = {"Opus_Cmd_Id","Opus_Cmd_Payload"};

    /** name of the exported data Conf Command*/
    public static final String[] FEATURE_DATA_NAME_CONF_CMD = {"Opus_Cmd_Id","Opus_FrameSize","Opus_SamplingFreq","Opus_Channels"};

    /** name of the exported data Control Command*/
    public static final String[] FEATURE_DATA_NAME_CONTROL = {"Opus_Cmd_Id","Opus_Enable_Disable"};

    /** index where you can find the opus config command id */
    private static final int OPUS_CONF_CMD_ID_INDEX = 0;
    /** index where you can find the opus config command payload */
    private static final int OPUS_CONF_CMD_PAYLOAD_INDEX = 1;

    /** index where you can find opus frame size value/description */
    private static final int OPUS_FRAME_SIZE_SUBINDEX = 1;
    /** index where you can find opus sampling frequency value/description*/
    private static final int OPUS_SAMPLING_FREQ_SUBINDEX = 2;
    /** index where you can find opus channels value/description*/
    private static final int OPUS_CHANNELS_SUBINDEX = 3;

    /** index where you can find opus enable/disable value/description*/
    private static final int OPUS_ONOFF_SUBINDEX = 1;

    public static final byte BV_OPUS_CONTROL =                  0x0A;

    public static final byte BV_OPUS_ENABLE_NOTIF_REQ =               0x10;
    public static final byte BV_OPUS_DISABLE_NOTIF_REQ =              0x11;

    public static final byte BV_OPUS_CONF_CMD =                       0x0B;

    private static final byte BV_OPUS_FRAME_SIZE_2_5 =                 0x20;
    private static final byte BV_OPUS_FRAME_SIZE_5 =                   0x21;
    private static final byte BV_OPUS_FRAME_SIZE_10 =                  0x22;
    private static final byte BV_OPUS_FRAME_SIZE_20 =                  0x23;
    private static final byte BV_OPUS_FRAME_SIZE_40 =                  0x24;
    private static final byte BV_OPUS_FRAME_SIZE_60 =                  0x25;

    private static final byte BV_OPUS_SAMPLING_FREQ_8 =                0x30;
    private static final byte BV_OPUS_SAMPLING_FREQ_16 =               0x31;
    private static final byte BV_OPUS_SAMPLING_FREQ_24 =               0x32;
    private static final byte BV_OPUS_SAMPLING_FREQ_48 =               0x33;

    private static final byte BV_OPUS_CHANNELS_1 =                     0x40;
    private static final byte BV_OPUS_CHANNELS_2 =                     0x41;

    /** Opus decoder params [default values] */
    private static final float OPUS_DEC_MS = 20;
    private static final int OPUS_DEC_SAMPLING_FREQ = 16000;
    private static final short OPUS_DEC_CHANNELS = 1;
    private static final int OPUS_DEC_FRAME_SIZE_PCM = (int)((OPUS_DEC_SAMPLING_FREQ/1000)* OPUS_DEC_MS);

    /** Opus encoder params */
    private static OpusEncParams opusEncParams;

    /**
     * build an Audio Opus Configuration Feature
     *
     * @param n node that will send data to this feature
     */
    public FeatureAudioOpusConf(Node n) {
        super(FEATURE_NAME, n, new Field[]{
                new Field(FEATURE_DATA_NAME[OPUS_CONF_CMD_ID_INDEX], null, Field.Type.Int8,
                        Byte.MAX_VALUE, Byte.MIN_VALUE),
                new Field(FEATURE_DATA_NAME[OPUS_CONF_CMD_PAYLOAD_INDEX], null, Field.Type.ByteArray,
                        Byte.MAX_VALUE, Byte.MIN_VALUE)
        });
    }

    public static float getDecDefaultFrameSize() {
        return OPUS_DEC_MS;
    }

    public static int getDecDefaultSamplingFreq() {
        return OPUS_DEC_SAMPLING_FREQ;
    }

    public static short getDecDefaultChannels() {
        return OPUS_DEC_CHANNELS;
    }

    public static int getDecDefaultFrameSizePCM() {
        return OPUS_DEC_FRAME_SIZE_PCM;
    }

    public static OpusEncParams getEncParams(){
        return opusEncParams;
    }

    /**
     * extract the audio sync data from the node raw data, in this case it read
     * a short {@code adpcm_index_in} and an int {@code adpcm_predsample_in} values which represent
     * the synchronization parameters.
     *
     * @param data       array where read the Field data (a 6 bytes array)
     * @param dataOffset offset where start to read the data (0 by default)
     * @return number of read bytes (6) and data extracted (the audio sync params, a short and an int)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length == 4 || data.length == 2) {
            if (data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONF_CMD){
                return extractConfigurationData(data);
            } else if (data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONTROL){
                return getExtractControlData(data);
            } else {
                return new ExtractResult(null,0);
            }
        } else {
            Log.e("FeatureAudioOpusConf", "data length: " + data.length);
            throw new IllegalArgumentException("Received command not recognized (nor 2 or 4 bytes length)");
        }
    }//update

    private final static Field[] CONTROL_FIELDS = new Field[]{
            new Field(FEATURE_DATA_NAME_CONTROL[OPUS_CONF_CMD_ID_INDEX], null, Field.Type.Int8,
                    Byte.MAX_VALUE, Byte.MIN_VALUE),
            new Field(FEATURE_DATA_NAME_CONTROL[OPUS_ONOFF_SUBINDEX], null, Field.Type.Int8,
                    Byte.MAX_VALUE, Byte.MIN_VALUE)
    };

    @NonNull
    private ExtractResult getExtractControlData(byte[] data) {
        Number[] onOffParams = new Number[2];
        onOffParams[OPUS_CONF_CMD_ID_INDEX] = BV_OPUS_CONTROL;
        onOffParams[OPUS_ONOFF_SUBINDEX] = data[OPUS_ONOFF_SUBINDEX];
        Sample opusOnOffParams = new Sample(onOffParams, CONTROL_FIELDS);
        return new ExtractResult(opusOnOffParams,2);
    }

    private final static Field[] CONFIGURATION_FIELD = new Field[]{
            new Field(FEATURE_DATA_NAME_CONF_CMD[OPUS_CONF_CMD_ID_INDEX], null, Field.Type.Int8,
                    Byte.MAX_VALUE, Byte.MIN_VALUE),
            new Field(FEATURE_DATA_NAME_CONF_CMD[OPUS_FRAME_SIZE_SUBINDEX], null, Field.Type.Int8,
                    Byte.MAX_VALUE, Byte.MIN_VALUE),
            new Field(FEATURE_DATA_NAME_CONF_CMD[OPUS_SAMPLING_FREQ_SUBINDEX], null, Field.Type.Int8,
                    Byte.MAX_VALUE, Byte.MIN_VALUE),
            new Field(FEATURE_DATA_NAME_CONF_CMD[OPUS_CHANNELS_SUBINDEX], null, Field.Type.Int8,
                    Byte.MAX_VALUE, Byte.MIN_VALUE)
    };

    @NonNull
    private ExtractResult extractConfigurationData(byte[] data) {
        Number[] confParams = new Number[4];
        confParams[OPUS_CONF_CMD_ID_INDEX] = BV_OPUS_CONF_CMD;
        confParams[OPUS_FRAME_SIZE_SUBINDEX] = getOpusFrameSize(data);
        confParams[OPUS_SAMPLING_FREQ_SUBINDEX] = getOpusSamplingFreq(data);
        confParams[OPUS_CHANNELS_SUBINDEX] = getOpusChannels(data);
        Sample opusConfParams = new Sample(confParams,CONFIGURATION_FIELD);
        return new ExtractResult(opusConfParams,4);
    }

    private Number getOpusFrameSize(byte[] data) {
        if (data[OPUS_CONF_CMD_ID_INDEX] == BV_OPUS_CONF_CMD) {
            switch (data[OPUS_FRAME_SIZE_SUBINDEX]) {
                case BV_OPUS_FRAME_SIZE_2_5:
                    return 2.5f;
                case BV_OPUS_FRAME_SIZE_5:
                    return 5;
                case BV_OPUS_FRAME_SIZE_10:
                    return 10;
                case BV_OPUS_FRAME_SIZE_20:
                    return 20;
                case BV_OPUS_FRAME_SIZE_40:
                    return 40;
                case BV_OPUS_FRAME_SIZE_60:
                    return 60;
            }
        }
        return OPUS_DEC_MS;
    }

    private Number getOpusSamplingFreq(byte[] data) {
        if (data[0] == BV_OPUS_CONF_CMD) {
            switch (data[OPUS_SAMPLING_FREQ_SUBINDEX]) {
                case BV_OPUS_SAMPLING_FREQ_8:
                    return 8000;
                case BV_OPUS_SAMPLING_FREQ_16:
                    return 16000;
                case BV_OPUS_SAMPLING_FREQ_24:
                    return 24000;
                case BV_OPUS_SAMPLING_FREQ_48:
                    return 48000;
            }
        }
        return OPUS_DEC_SAMPLING_FREQ;
    }

    private Number getOpusChannels(byte[] data) {
        if (data[0] == BV_OPUS_CONF_CMD) {
            switch (data[OPUS_CHANNELS_SUBINDEX]) {
                case BV_OPUS_CHANNELS_1:
                    return 1;
                case BV_OPUS_CHANNELS_2:
                    return 2;
            }
        }
        //else
        return OPUS_DEC_CHANNELS;
    }

    public static float getFrameSize(Sample sample) {
        if (hasValidIndex(sample,OPUS_FRAME_SIZE_SUBINDEX)){
            return sample.data[OPUS_FRAME_SIZE_SUBINDEX].floatValue();
        }
        //else
        return OPUS_DEC_MS;
    }

    public static int getSamplingFreq(Sample sample) {
        if(hasValidIndex(sample,OPUS_SAMPLING_FREQ_SUBINDEX))
            return sample.data[OPUS_SAMPLING_FREQ_SUBINDEX].intValue();
        //else
        return OPUS_DEC_SAMPLING_FREQ;
    }


    public static short getChannels(Sample sample) {
        if(hasValidIndex(sample,OPUS_CHANNELS_SUBINDEX))
            return sample.data[OPUS_CHANNELS_SUBINDEX].shortValue();
        //else
        return OPUS_DEC_CHANNELS;
    }

    public static byte[] getEnableNotificationCmd(boolean enable){
        byte[] command = {BV_OPUS_CONTROL, enable ? BV_OPUS_ENABLE_NOTIF_REQ : BV_OPUS_DISABLE_NOTIF_REQ};
        return command;
    }

    @Override
    public AudioCodecManager instantiateManager(boolean hasDecoder, boolean hasEncoder) {
        return new OpusManager(hasDecoder,hasEncoder);
    }

    public void setEncParams(int encFrameSize,
                             int encSamplingFreq,
                             short encChannels,
                             int encFrameSizePcm,
                             int encApplication,
                             int encBitrate,
                             boolean encVbr,
                             int encComplexity) {

        opusEncParams = new OpusEncParams(encFrameSize,
                encSamplingFreq,
                encChannels,
                encFrameSizePcm,
                encApplication,
                encBitrate,
                encVbr,
                encComplexity);
    }
}
