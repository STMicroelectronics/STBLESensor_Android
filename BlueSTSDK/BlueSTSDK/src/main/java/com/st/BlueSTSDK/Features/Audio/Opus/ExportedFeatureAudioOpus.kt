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
package com.st.BlueSTSDK.Features.Audio.Opus

import com.st.BlueSTSDK.ExportedFeature
import com.st.BlueSTSDK.NodeServer

abstract class ExportedFeatureAudioOpus(parent:NodeServer) : ExportedFeature(parent) {

    /**
     * send a portion of opus encoed audio
     * the codedData will be automaticaly splitted in different package if the size is bigger than
     * the supported mtu
     */
    fun sendEncodedAudio(codedData: ByteArray) {
        val maxPayloadSize = mParent.maxPayloadSize
        val packData = BlueVoiceOpusTransportProtocol.packData(codedData, maxPayloadSize)
        packData.forEach {
            notifyData(it)
        }
    }
}

    class ExportedFeatureAudioOpusVoice(parent:NodeServer) : ExportedFeatureAudioOpus(parent)

    class ExportedFeatureAudioOpusMusic(parent:NodeServer) : ExportedFeatureAudioOpus(parent)