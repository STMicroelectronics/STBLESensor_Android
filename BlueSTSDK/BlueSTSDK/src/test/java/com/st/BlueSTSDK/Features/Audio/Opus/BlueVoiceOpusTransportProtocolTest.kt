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

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class BlueVoiceOpusTransportProtocolEncodeTest{

    private val data = byteArrayOf(0x01,0x02,0x03)
    @Test
    fun smallPackageStartWith0x20(){
        val encodedData = BlueVoiceOpusTransportProtocol.packData(data,10)
        assertEquals(1,encodedData.size)
        val packageData = encodedData.first()
        assertEquals(data.size+1,packageData.size)
        assertEquals(0x20.toByte(),packageData[0])
        assertArrayEquals(packageData.drop(1).toByteArray(),data)

    }

    @Test
    fun splitDataStartWith0x00EndsWith0x80(){
        val encodedData = BlueVoiceOpusTransportProtocol.packData(data,3)

        assertEquals(2,encodedData.size)
        val firstPackageData = encodedData[0]
        assertEquals(3,firstPackageData.size)
        assertEquals(0x00.toByte(),firstPackageData[0])
        assertEquals(data.dropLast(1),firstPackageData.drop(1))

        val lastPackageData = encodedData[1]
        assertEquals(lastPackageData.size,2)
        assertEquals(lastPackageData[0],0x80.toByte())
        assertEquals(data[2],lastPackageData[1])

    }

    @Test
    fun middlePackageStartWith0x40(){
        val encodedData = BlueVoiceOpusTransportProtocol.packData(data,2)

        assertEquals(3,encodedData.size)
        val middlePackageData = encodedData[1]
        assertEquals(2,middlePackageData.size)
        assertEquals(0x40.toByte(),middlePackageData[0])
        assertEquals(data[1],middlePackageData[1])

    }
}


@RunWith(JUnit4::class)
class BlueVoiceOpusTransportProtocolDecodeTest{

    @Test
    fun singlePackageStartWith0x20(){
        val decoder = BlueVoiceOpusTransportProtocol(2)
        val encodedData = byteArrayOf(0x20.toByte(),0x01,0x02)
        val decodedData = decoder.unpackData(encodedData)

        assertNotNull(decodedData)
        assertEquals(2,decodedData?.size)
        assertArrayEquals(encodedData.drop(1).toByteArray(),decodedData)
    }


    @Test
    fun multiplePacakgeAreMerged(){
        val decoder = BlueVoiceOpusTransportProtocol(3)
        val encodedData = listOf(
                byteArrayOf(0x00.toByte(),0x01,0x02),
                byteArrayOf(0x80.toByte(),0x03)
        )
        var decodedData = decoder.unpackData(encodedData[0])
        assertNull(decodedData)
        decodedData = decoder.unpackData(encodedData[1])
        assertNotNull(decodedData)
        assertEquals(3,decodedData?.size)
        assertArrayEquals(byteArrayOf(0x1,0x2,0x3),decodedData)
    }

    @Test
    fun multiplePacakgeAreMerged_2(){
        val decoder = BlueVoiceOpusTransportProtocol(3)
        val encodedData = listOf(
                byteArrayOf(0x00.toByte(),0x01),
                byteArrayOf(0x40.toByte(),0x02),
                byteArrayOf(0x80.toByte(),0x03)
        )
        var decodedData = decoder.unpackData(encodedData[0])
        assertNull(decodedData)
        decodedData = decoder.unpackData(encodedData[1])
        assertNull(decodedData)
        decodedData = decoder.unpackData(encodedData[2])
        assertNotNull(decodedData)
        assertEquals(3,decodedData?.size)
        assertArrayEquals(byteArrayOf(0x1,0x2,0x3),decodedData)
    }

}