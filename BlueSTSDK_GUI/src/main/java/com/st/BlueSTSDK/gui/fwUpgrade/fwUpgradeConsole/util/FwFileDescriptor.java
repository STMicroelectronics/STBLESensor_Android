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

import androidx.annotation.IntDef;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FwFileDescriptor {

    @IntDef({BIN, IMG,UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FirmwareFileType {}

    public static final int UNKNOWN = 0;
    public static final int BIN = 1;
    public static final int IMG = 2;


    public static @FirmwareFileType int getFileType(Uri file){
        String fileName = file.getLastPathSegment().toLowerCase();

        if(fileName.endsWith("bin"))
            return BIN;
        if(fileName.endsWith("img"))
            return IMG;

        return UNKNOWN;

    }

    private @FirmwareFileType int mType;
    private ContentResolver mContentResolver;
    private Uri mFile;
    private long mFileLength;

    private void setFileLength(){
        try {
            InputStream in = mContentResolver.openInputStream(mFile);
            mFileLength=getFileLength(in);
        } catch (FileNotFoundException e) {
            mFileLength=0;
        }
    }

    public FwFileDescriptor(ContentResolver resolver, Uri file) {
        mType = getFileType(file);
        mContentResolver=resolver;
        mFile=file;
        setFileLength();
    }

    public @FirmwareFileType int getType() {
        return mType;
    }

    public long getLength(){
        return mFileLength;
    }

    public InputStream openFile() throws FileNotFoundException {
        InputStream in = mContentResolver.openInputStream(mFile);
        if(mType==IMG)
            return  new ImgFileInputStream(in,mFileLength);
        return in;
    }

    private static long getFileLength(InputStream input) {
        long nBytes = 0;
        try {
            while(input.read()>=0){
                nBytes++;
            }//while
        } catch (IOException e) {
            nBytes=0;
        }//try-catch

        return nBytes;
    }//getFileLength

}
