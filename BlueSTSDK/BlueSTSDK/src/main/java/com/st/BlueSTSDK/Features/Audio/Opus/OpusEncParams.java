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
package com.st.BlueSTSDK.Features.Audio.Opus;

import com.st.BlueSTSDK.Features.Audio.EncParams;

public class OpusEncParams extends EncParams {

    /** Opus encoder params */
    private int opusEncFrameSize;// = 10;
    private int opusEncSamplingFreq;// = 48000;
    private short opusEncChannels;// = 2;
    private int opusEncFrameSizePcm;// = (int)((OpusEncSamplingFreq /1000)* opusEncFrameSize);
    private int opusEncApplication;// = 2049;
    private int opusEncBitrate;// = 48000;
    private boolean opusEncVbr;// = false;
    private int opusEncComplexity;// = 0;

    public OpusEncParams(int opusEncFrameSize,
                         int opusEncSamplingFreq,
                         short opusEncChannels,
                         int opusEncFrameSizePcm,
                         int opusEncApplication,
                         int opusEncBitrate,
                         boolean opusEncVbr,
                         int opusEncComplexity) {
        this.opusEncFrameSize = opusEncFrameSize;
        this.opusEncSamplingFreq = opusEncSamplingFreq;
        this.opusEncChannels = opusEncChannels;
        this.opusEncFrameSizePcm = opusEncFrameSizePcm;
        this.opusEncApplication = opusEncApplication;
        this.opusEncBitrate = opusEncBitrate;
        this.opusEncVbr = opusEncVbr;
        this.opusEncComplexity = opusEncComplexity;
    }

    public int getEncFrameSize() {
        return opusEncFrameSize;
    }

    public int getEncSamplingFreq() {
        return opusEncSamplingFreq;
    }

    public short getEncChannels() {
        return opusEncChannels;
    }

    public int getEncFrameSizePcm() {
        return opusEncFrameSizePcm;
    }

    public int getEncApplication() {
        return opusEncApplication;
    }

    public int getEncBitrate() {
        return opusEncBitrate;
    }

    public boolean isEncVbr() {
        return opusEncVbr;
    }

    public int getEncComplexity() {
        return opusEncComplexity;
    }
}
