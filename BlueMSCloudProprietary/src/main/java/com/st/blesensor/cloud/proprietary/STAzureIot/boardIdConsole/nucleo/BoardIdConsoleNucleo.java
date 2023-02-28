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

package com.st.blesensor.cloud.proprietary.STAzureIot.boardIdConsole.nucleo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.blesensor.cloud.proprietary.STAzureIot.boardIdConsole.BoardIdConsole;

public class BoardIdConsoleNucleo extends BoardIdConsole {

    /**
     * command used for receive the board id
     */
    private static final String GET_UID = "uid\n";

    /**
     * object that will receive the console data
     */
    private Debug.DebugOutputListener mCurrentListener;

    /**
     * Buffer where store the command response
     */
    private StringBuilder mBuffer;

    private static @Nullable String getFinisedCommand(StringBuilder answer){
        int length = answer.length();
        if(length>2 && answer.substring(length-2).equals("\r\n"))
            return answer.substring(0,length-2);
        if(length>1 && answer.substring(length-1).equals("\n"))
            return answer.substring(0,length-1);
        return null;
    }

    /**
     * object used for manage the get board id command
     */
    private Debug.DebugOutputListener mConsoleGetIdListener = new Debug.DebugOutputListener() {

        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull String message) {
            mBuffer.append(message);
            String answer = getFinisedCommand(mBuffer);
            if (answer!=null) {
                setConsoleListener(null);
                if (mReadBoardIdCallback != null)
                    mReadBoardIdCallback.onBoardIdRead(BoardIdConsoleNucleo.this, answer);
            }
        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull String message) {

        }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {

        }
    };


    /**
     *
     * @param console console where send the command
     */
    public BoardIdConsoleNucleo(Debug console) {
        super(console);
        mBuffer = new StringBuilder();
    }

    /**
     * change the listener to use for receive the debug console message, null will close the
     * debug console
     *
     * @param listener object to use for notify the console messages
     */
    private void setConsoleListener(Debug.DebugOutputListener listener) {
        synchronized (this) {
            mConsole.removeDebugOutputListener(mCurrentListener);
            mConsole.addDebugOutputListener(listener);
            mCurrentListener = listener;
        }//synchronized
    }

    @Override
    public boolean isWaitingAnswer() {
        return mCurrentListener != null;
    }

    private ReadBoardIdCallback mReadBoardIdCallback=null;

    @Override
    public boolean readBoardId(ReadBoardIdCallback callback) {
        if (isWaitingAnswer())
            return false;

        mReadBoardIdCallback=callback;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetIdListener);
        mConsole.write(GET_UID);
        return true;
    }

}
