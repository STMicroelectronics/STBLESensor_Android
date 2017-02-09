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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import junit.framework.Assert;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GoogleAsrKeyTest {

    private static final String FAKE_KEY ="012345678901234567890123456789012345678";
    private static final String INVALID_SMALL_KEY ="SMALL KEY";

    @Test
    public void keyMustHaveLength39(){
        try {
            new GoogleAsrKey(FAKE_KEY);
        }catch (Exception e) {
            Assert.fail("Exception Throw: " + e);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ExceptionIsThrowIfLengthIsSmaller(){
        new GoogleAsrKey(INVALID_SMALL_KEY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ExceptionIsThrowIfLengthIsBigger(){
        new GoogleAsrKey(FAKE_KEY+"X");
    }

    @Test
    public void keyIsStoredInTheClass(){
        GoogleAsrKey key = new GoogleAsrKey(FAKE_KEY);
        Assert.assertEquals(FAKE_KEY,key.getKey());
    }


    @Test
    @SuppressLint("CommitPrefEdits")
    public void keyIsStoredInPreference(){
        SharedPreferences pref = mock(SharedPreferences.class);
        SharedPreferences.Editor editor = mock(SharedPreferences.Editor.class);

        when(pref.edit()).thenReturn(editor);
        when(editor.putString(anyString(),anyString())).thenReturn(editor);

        GoogleAsrKey key = new GoogleAsrKey(FAKE_KEY);
        key.store(pref);

        verify(pref).edit();
        verify(editor).putString(anyString(),eq(FAKE_KEY));

    }

    @Test
    public void loadKeyStoredInPreference(){
        SharedPreferences pref = mock(SharedPreferences.class);
        when(pref.getString(anyString(),anyString())).thenReturn(FAKE_KEY);
        GoogleAsrKey key = GoogleAsrKey.loadKey(pref);

        assertNotNull(key);
        assertEquals(FAKE_KEY,key.getKey());
    }

    @Test
    public void returnNullIfKeyIsNotStoredInPreference(){
        SharedPreferences pref = mock(SharedPreferences.class);
        when(pref.getString(anyString(),anyString())).thenReturn(null);
        GoogleAsrKey key = GoogleAsrKey.loadKey(pref);

        assertNull(key);
    }

    @Test
    public void returnNullIfInvalidKeyIsStoredInPreference(){
        SharedPreferences pref = mock(SharedPreferences.class);
        when(pref.getString(anyString(),anyString())).thenReturn(INVALID_SMALL_KEY);
        GoogleAsrKey key = GoogleAsrKey.loadKey(pref);

        assertNull(key);
    }

}