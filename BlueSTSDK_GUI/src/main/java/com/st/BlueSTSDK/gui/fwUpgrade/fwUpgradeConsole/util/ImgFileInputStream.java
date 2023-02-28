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

import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Scanner;

/**
 * Utility class that read a img file converting the char data into byte.
 * the file is read as block of uint32(8char) and it will invert the byte order
 * it the file as the string "01020304" a sequence of read will return the bytes: 0x04,0x03,0x02,
 * 0x01
 */
public class ImgFileInputStream extends InputStream {

    /**
     * utility class used for read a line from the file
     */
    private Scanner mScanner;

    /**
     * stack used for keep the line content
     */
    private ArrayDeque<Integer> mBuffer= new ArrayDeque<>(4);

    private long mFileSize;

    ImgFileInputStream(InputStream input, long streamByteLength){
        long nLine = streamByteLength/10;
        mFileSize=(streamByteLength-2*nLine)/2;
        mScanner=new Scanner(input);
    }

    /**
     * fill the buffer stack with the line
     * @return true if the read is ok, false if it reach the EOF
     */
    private boolean readLine(){
        if(!mScanner.hasNextLine())
            return false;
        //else
        String line = mScanner.nextLine();
        for(int i=0;i<4;i++){
            String value = line.substring(2*i,2*i+2);
            mBuffer.add(Integer.parseInt(value,16));
        }
        return true;
    }

    @Override
    public int read() {
        if(mBuffer.isEmpty())
            if(!readLine()) // if the read fail, the file ended
                return -1;
        return mBuffer.removeLast();
    }

    @Override
    public boolean markSupported(){
        return false;
    }

    public long length(){
       return mFileSize;
    }
}
