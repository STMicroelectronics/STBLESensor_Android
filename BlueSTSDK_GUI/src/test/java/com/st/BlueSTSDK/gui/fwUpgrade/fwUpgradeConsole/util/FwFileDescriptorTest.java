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

package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FwFileDescriptorTest {

    private static Uri createFileWithExtension(String extension){
        return new Uri.Builder()
                .appendPath("file."+extension)
                .build();
    };

    @Test
    public void binExtensionIsBinFileType(){
        Uri file = createFileWithExtension("bin");
        assertEquals(FwFileDescriptor.BIN, FwFileDescriptor.getFileType(file));
        file = createFileWithExtension("BIN");
        assertEquals(FwFileDescriptor.BIN, FwFileDescriptor.getFileType(file));
    }

    @Test
    public void imgExtensionIsImgFileType(){
        Uri file = createFileWithExtension("img");
        assertEquals(FwFileDescriptor.IMG, FwFileDescriptor.getFileType(file));
        file = createFileWithExtension("IMG");
        assertEquals(FwFileDescriptor.IMG, FwFileDescriptor.getFileType(file));
    }

    @Test
    public void otherExtensionIsUnknownFileType(){
        Uri file = createFileWithExtension("txt");
        assertEquals(FwFileDescriptor.UNKNOWN, FwFileDescriptor.getFileType(file));
        file = createFileWithExtension("TXT");
        assertEquals(FwFileDescriptor.UNKNOWN, FwFileDescriptor.getFileType(file));
    }


    @Test
    @Ignore
    public void readFileLengthOnSetup() throws FileNotFoundException {
        Uri file = createFileWithExtension("bin");
        ContentResolver cr = mock(ContentResolver.class);
        final int FILE_LENGTH=10;
        InputStream simpleStream = new ByteArrayInputStream(new byte[FILE_LENGTH]);
        when(cr.openInputStream(eq(file))).thenReturn(simpleStream);

        //doReturn(simpleStream).when(cr).openInputStream(file);

        FwFileDescriptor mDescriptor = new FwFileDescriptor(cr,file);
        assertEquals(FILE_LENGTH,mDescriptor.getLength());

    }


}