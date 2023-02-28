/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 * <p/>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of STMicroelectronics nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p/>
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
 ******************************************************************************/
package com.st.BlueSTSDK.Features.Audio.ADPCM;

import android.util.Log;

import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.FeatureAudioConf;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

/**
 * Feature that contains the audio synchronization parameters necessary to ADPCM audio decompression
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public class FeatureAudioADPCMSync extends FeatureAudioConf {

    public static final String FEATURE_NAME = "Audio Sync";

    /** name of the exported data */
    public static final String[] FEATURE_DATA_NAME = {"ADPCM_index", "ADPCM_predSample"};

    /** index where you can find adpcm index value/description */
    public static final int ADPCM_INDEX_INDEX = 0;
    /** index where you can find adpcm predsample value/description*/
    public static final int ADPCM_PREDSAMPLE_INDEX = 1;
    
    /**
     * build an AudioADPCMSync Feature
     *
     * @param node node that will send data to this feature
     */
    public FeatureAudioADPCMSync(Node node) {
        super(FEATURE_NAME, node,
                new Field[]{
                        new Field(FEATURE_DATA_NAME[ADPCM_INDEX_INDEX], null, Field.Type.Int16,
                                Short.MAX_VALUE, Short.MIN_VALUE),
                        new Field(FEATURE_DATA_NAME[ADPCM_PREDSAMPLE_INDEX], null, Field.Type.Int32,
                                Integer.MAX_VALUE, Integer.MIN_VALUE)
                });
    }//FeatureAudioADPCMSync

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
        if (data.length == 6) {
            //syncIn(data);
            Number[] syncParams = new Number[2];
            syncParams[0] = getADPCMIndex(data);
            syncParams[1] = getADPCMPredsample(data);
            Sample audioSyncParams = new Sample(syncParams,getFieldsDesc());
            return new ExtractResult(audioSyncParams,6);
        } else {
            Log.e("FeatureAudioADPCM", "data length: " + data.length);
            throw new IllegalArgumentException("There are no 6 bytes available to read");
        }
    }//update

    public static int getPredictedSample(Sample s){
        if(hasValidIndex(s,ADPCM_PREDSAMPLE_INDEX))
            return s.data[ADPCM_PREDSAMPLE_INDEX].intValue();
        //else
        return 0;
    }

    public static short getIndex(Sample s){
        if(hasValidIndex(s,ADPCM_INDEX_INDEX))
            return s.data[ADPCM_INDEX_INDEX].shortValue();
        //else
        return -1;
    }

    /**
     * method which extract the two synchronization parameters from a buffer passed as parameter
     * @param buffer_in the buffer (6 bytes array) which contains the sync params
     */
    private short getADPCMIndex(byte[] buffer_in) {
        short adpcm_index_in;
        adpcm_index_in = (short) (((short) buffer_in[0]) & 0x00FF);
        adpcm_index_in |= (short) (((short) buffer_in[1] << 8) & 0xFF00);
        return adpcm_index_in;
    }

    /**
     * method which extract the two synchronization parameters from a buffer passed as parameter
     * @param buffer_in the buffer (6 bytes array) which contains the sync params
     */
    private int getADPCMPredsample(byte[] buffer_in) {
        int adpcm_predsample_in;
        adpcm_predsample_in = ((int) buffer_in[2]) & 0x000000FF;
        adpcm_predsample_in |= ((int) buffer_in[3] << 8) & 0x0000FF00;
        adpcm_predsample_in |= ((int) buffer_in[4] << 16) & 0x00FF0000;
        adpcm_predsample_in |= ((int) buffer_in[5] << 24) & 0xFF000000;
        return adpcm_predsample_in;
    }

    @Override
    public AudioCodecManager instantiateManager(boolean hasDecoder, boolean hasEncoder) {
        return new ADPCMManager();
    }

    @Override
    public void setEncParams(int encFrameSize, int encSamplingFreq, short encChannels, int encFrameSizePcm, int encApplication, int encBitrate, boolean encVbr, int encComplexity) {
        //Empty at the moment!!!
    }

}
