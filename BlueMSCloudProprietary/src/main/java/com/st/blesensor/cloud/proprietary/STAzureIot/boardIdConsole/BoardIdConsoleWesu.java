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

package com.st.blesensor.cloud.proprietary.STAzureIot.boardIdConsole;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Debug;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoardIdConsoleWesu extends BoardIdConsole {

    /**
     * command used for receive the board id
     */
    private static final String GET_UID = "?mcuid\n";


    private static final Pattern BOARD_ID_PARSE = Pattern.compile(".*([0-9A-Fa-f]{24})(_([0-9A-Fa-f]{3,4}))?.*");

    private static final String DEFAULT_MCU_FAMILY_ID = "437";

    private static final Pattern LICENSE_TO_BYTE_CODE = Pattern.compile("([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    /**
     * object that will receive the console data
     */
    private Debug.DebugOutputListener mCurrentListener;

    /**
     * Buffer where store the command response
     */
    private StringBuilder mBuffer;

    /**
     * handler used for the command timeout
     */
    private Handler mTimeout;

    /**
     * object used for manage the get board id command
     */
    private Debug.DebugOutputListener mConsoleGetIdListener = new Debug.DebugOutputListener() {

        private static final int COMMAND_TIMEOUT_MS=1000;

        /**
         *  action to do when the timeout is fired
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0 && mReadBoardIdCallback!=null)
                    mReadBoardIdCallback.onBoardIdRead(BoardIdConsoleWesu.this,
                            extractBoardUid(mBuffer.toString()));
            }
        };

        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull String message) {

            mTimeout.removeCallbacks(onTimeout); //remove the timeout
            mBuffer.append(message);
            String uid =extractBoardUid(mBuffer.toString());
            if(uid!=null){
                onTimeout.run();
            }else {
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }
        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull String message) {
        }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {
            //when the command is send, start the timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }
    };


    /**
     *
     * @param console console where send the command
     */
    BoardIdConsoleWesu(Debug console) {
        super(console);
        mTimeout = new Handler(Looper.getMainLooper());
        mBuffer = new StringBuilder();
    }

    private static String extractBoardUid(String data){
        Matcher matcher = BOARD_ID_PARSE.matcher(data);

        if (matcher.find()) {
            String mcu_id = "";
            String mcu_id_temp = matcher.group(1);
            Matcher matcher1 = LICENSE_TO_BYTE_CODE.matcher(mcu_id_temp);
            while (matcher1.find())
                mcu_id += matcher1.group(4) + matcher1.group(3) +
                          matcher1.group(2) +  matcher1.group(1); //to invert in little endian

            String mcu_fam = matcher.group(3);
            if (mcu_fam == null) mcu_fam = DEFAULT_MCU_FAMILY_ID;
            return mcu_id + "_" + mcu_fam;
        }

        return null; //not valid board id
    }

    /**
     * change the listener to use for receive the debug console message, null will close the
     * debug console
     *
     * @param listener object to use for notify the console messages
     */
    private void setConsoleListener(Debug.DebugOutputListener listener) {
        synchronized (this) {
            if(mCurrentListener!=null)
                mConsole.removeDebugOutputListener(mCurrentListener);
            mCurrentListener = listener;
            mConsole.addDebugOutputListener(listener);
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