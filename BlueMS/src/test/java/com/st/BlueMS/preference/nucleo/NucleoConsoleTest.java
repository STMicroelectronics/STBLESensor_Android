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

package com.st.BlueMS.preference.nucleo;
import com.st.BlueSTSDK.Debug;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.GregorianCalendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NucleoConsoleTest {

    @Mock
    private Debug mConsole;

    private NucleoConsole mNucleo;

    @Before
    public void setUp(){
        mNucleo = new NucleoConsole(mConsole);
    }

    @Test
    public void hasCommandSetName(){

        final String newName = "NEWNAME";

        mNucleo.setName(newName);

        verify(mConsole).write("setName "+newName+"\n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameIsSmallerThan7Char(){
        final String longName = "LooooooooooongName";
        mNucleo.setName(longName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameMustNotBeEmpty(){
        mNucleo.setName("");
    }

    @Test
    public void canSetTheTime(){
        // 03 feb 2001 02:03:04
        Date date = new GregorianCalendar(2001,01,03,04,05,06).getTime();
        mNucleo.setTime(date);
        verify(mConsole).write("setTime 04:05:06\n");
    }

    @Test
    public void canSetTheDate(){
        // Sat 03 feb 2001 02:03:04
        Date date = new GregorianCalendar(2001,01,03,04,05,06).getTime();
        mNucleo.setDate(date);
        verify(mConsole).write("setDate 06/03/02/01\n");
    }

    @Test
    public void setTimeCommandIsSmallerThan20byte(){
        // 03 feb 2001 02:03:04
        Date date = new GregorianCalendar(2001,01,03,04,05,06).getTime();
        mNucleo.setTime(date);
        ArgumentCaptor<String> commandCapture = ArgumentCaptor.forClass(String.class);
        verify(mConsole).write(commandCapture.capture());
        Assert.assertTrue(commandCapture.getValue().length()<=20);
    }

    @Test
    public void setDateCommandIsSmallerThan20byte(){
        // Sat 03 feb 2001 02:03:04
        Date date = new GregorianCalendar(2001,01,03,04,05,06).getTime();
        mNucleo.setDate(date);
        ArgumentCaptor<String> commandCapture = ArgumentCaptor.forClass(String.class);
        verify(mConsole).write(commandCapture.capture());
        Assert.assertTrue(commandCapture.getValue().length()<=20);
    }

    @Test
    public void setDateAndTimeSend2Commands(){
        Date date = new GregorianCalendar(2001,01,03,04,05,06).getTime();
        mNucleo.setDateAndTime(date);
        verify(mConsole).write("setDate 06/03/02/01\n");
        verify(mConsole).write("setTime 04:05:06\n");
    }
}