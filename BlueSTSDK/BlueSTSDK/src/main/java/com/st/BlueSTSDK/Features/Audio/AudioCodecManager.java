/*******************************************************************************
 * COPYRIGHT(c) 2015 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentatio
 *      n
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
package com.st.BlueSTSDK.Features.Audio;

import com.st.BlueSTSDK.Feature.Sample;
import com.st.BlueSTSDK.Node;

/**
 * Class containing the abstraction of an Audio Codec Manager
 *
 * @author STMicroelectronics - Central Labs.
 * @version 1.0
 */
public interface AudioCodecManager {

    /**
     * method used to reinitialize the Codec
     */
    void reinit();

    /**
     * returns Audio Codec Name
     */
    String getCodecName();

    /**
     * returns Audio Sampling Frequency parameter
     */
    int getSamplingFreq();

    /**
     * returns Audio Channels parameter
     */
    short getChannels();

    /**
     * returns true if the audio stream is enabled, false elsewhere
     */
    Boolean isAudioEnabled();

    /**
     * update Audio Codec parameters extracted from a Sample passed as parameter
     * @param sample the Sample which contains the sync params
     */
    void updateParams(Sample sample);

    /**
     * Encode function
     * @param pcmData input data to be encoded, pcm ad signed 16bit
     * @return encoded data
     */
    byte[] encode(short[] pcmData);


    /**
     * Encode function
     * @param encodeData input data to be encoded
     * @return decoded data into pcm signed 16bit
     */
    short[] decode(byte[] encodeData);

}
