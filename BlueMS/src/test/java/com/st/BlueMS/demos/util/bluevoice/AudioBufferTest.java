/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.BlueMS.demos.util.bluevoice;

import android.media.AudioTrack;

import com.st.BlueMS.matcher.MatchShortArray;

import junit.framework.Assert;

import static org.junit.Assert.*;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.io.OutputStream;

public class AudioBufferTest {

    @Test
    public void testGetSamplingRate(){
        final int samplingRate = 10;
        AudioBuffer buf = new AudioBuffer(samplingRate,1);
        assertEquals(samplingRate,buf.getSamplingRate());
    }

    @Test
    public void bufferContainsEnoughtSpace(){
        final int samplingRate = 10;
        final int maxSecond = 4;

        AudioBuffer buf = new AudioBuffer(samplingRate,maxSecond);

        assertTrue(samplingRate*maxSecond<=buf.getBufferLength());
    }

    @Test
    public void newBufferIsNotFull(){
        AudioBuffer buf = new AudioBuffer(1,1);
        Assert.assertFalse(buf.isFull());
    }

    @Test
    public void appendReturnCurrentBufferSize(){
        final int samplingRate =10;
        final int dataSize=7;
        AudioBuffer buf = new AudioBuffer(samplingRate,1);

        Assert.assertEquals(dataSize,buf.append(new short[dataSize]));
        Assert.assertEquals(samplingRate,buf.append(new short[dataSize]));
    }

    @Test
    public void appendEmptyDataDoesntDoAnything(){
        final int samplingRate =10;
        final int dataSize=7;
        AudioBuffer buf = new AudioBuffer(samplingRate,1);

        Assert.assertEquals(dataSize,buf.append(new short[dataSize]));
        Assert.assertEquals(dataSize,buf.append(new short[0]));
    }

    @Test
    public void writeToExtractTheInsertedData() throws IOException {
        AudioBuffer buf = new AudioBuffer(10,1);
        short data[] = new short[]{0x0102,0x0304};
        buf.append(data);
        OutputStream os = mock(OutputStream.class);

        buf.writeLittleEndianTo(os);

        verify(os).write(0x02);
        verify(os).write(0x01);

        verify(os).write(0x04);
        verify(os).write(0x03);
    }


    @Test
    public void writeToAudioTrackWriteCorrectData(){
        AudioBuffer buf = new AudioBuffer(10,1);
        final short data[] = new short[]{0x0102,0x0304};
        buf.append(data);

        AudioTrack audio = mock(AudioTrack.class);
        buf.writeTo(audio);

        ArgumentMatcher<short[]> arrayStartWith = MatchShortArray.startingWith(data);

        verify(audio).write(argThat(arrayStartWith),eq(0), eq(data.length));
    }

}