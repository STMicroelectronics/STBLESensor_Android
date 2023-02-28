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

import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.min

/**
 * class that unpack the messages encapsulated into the BlueVoice transport protocol.
 * a package will be returned when frameSize bytes will be received
 */
internal class BlueVoiceOpusTransportProtocol(private val frameSize:Int) {

    private var mPartialData:ByteArray = byteArrayOf()

    /**
     * extract the data from the dataPacakge, return null if the frame is not completed or an array
     * of frameSize bytes
     */
    fun unpackData(dataPacket:ByteArray) : ByteArray?{
        when(dataPacket[0]) {
            BV_OPUS_TP_START_PACKET -> {
                resetPartialData()
                appendPackage(dataPacket)
                return null
            }
            BV_OPUS_TP_START_END_PACKET ->{
                resetPartialData()
                appendPackage(dataPacket)
                return mPartialData
            }
            BV_OPUS_TP_MIDDLE_PACKET -> {
                appendPackage(dataPacket)
                return null
            }
            BV_OPUS_TP_END_PACKET ->{
                appendPackage(dataPacket)
                return mPartialData
            }
        }
        return null
    }

    private fun resetPartialData() {
        mPartialData = byteArrayOf()
    }


    private fun appendPackage(audioSample: ByteArray) {
        mPartialData = mPartialData.plus(audioSample.copyOfRange(1, audioSample.size))
    }


    companion object{

        /**
         * Split the codedData array into a list of messages of maxLength encapsulating the
         * data into the BlueVoice transport protocol
         */
        fun packData(codedData:ByteArray, maxLength:Int) : List<ByteArray> {
            var head = BV_OPUS_TP_START_PACKET
            val baos = ByteArrayOutputStream()
            var cnt = 0
            var size: Int
            val codedDataLength = codedData.size
            val nPackage = (codedDataLength + (maxLength-1)/2)/(maxLength-1)
            val packData = ArrayList<ByteArray>(nPackage)
            while (cnt < codedDataLength) {
                size = min(maxLength - 1, codedDataLength - cnt)
                if (codedDataLength - cnt <= maxLength - 1) {
                    head = if (cnt == 0) {
                        BV_OPUS_TP_START_END_PACKET
                    } else {
                        BV_OPUS_TP_END_PACKET
                    }
                }
                when (head) {
                    BV_OPUS_TP_START_PACKET -> {
                        /*First part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, 0, maxLength - 1)
                        packData.add(baos.toByteArray())
                        head = BV_OPUS_TP_MIDDLE_PACKET
                    }
                    BV_OPUS_TP_START_END_PACKET -> {
                        /*First and last part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, 0, codedDataLength)
                        packData.add(baos.toByteArray())
                        head = BV_OPUS_TP_START_PACKET
                    }
                    BV_OPUS_TP_MIDDLE_PACKET -> {
                        /*Central part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, cnt, maxLength - 1)
                        packData.add(baos.toByteArray())
                    }
                    BV_OPUS_TP_END_PACKET -> {
                        /*Last part of an Opus packet*/
                        baos.reset()
                        baos.write(head.toInt())
                        baos.write(codedData, cnt, codedDataLength - cnt)
                        packData.add(baos.toByteArray())
                        head = BV_OPUS_TP_START_PACKET
                    }
                }
                /*length variables update*/
                cnt += size
            }
            return packData
        }

        /** Opus Transport Protocol  */
        private const val BV_OPUS_TP_START_PACKET = 0x00.toByte()
        private const val BV_OPUS_TP_START_END_PACKET = 0x20.toByte()
        private const val BV_OPUS_TP_MIDDLE_PACKET = 0x40.toByte()
        private const val BV_OPUS_TP_END_PACKET = 0x80.toByte()
    }
}