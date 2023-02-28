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

package com.st.BlueSTSDK.Utils;

import android.os.Handler;
import android.os.Looper;

import com.st.BlueSTSDK.Debug;

/**
 * Class that send a command to the debug console and wait for the answer
 */
//TODO check if  this class can be used inside the FwUpgrade/LicenseManager
public class ConsoleCommand {

    private Debug mConsole;
    private Callback mCallback;
    private long mCommandTimeout;

    /**
     * listener that wait the timeout before send the result to the callback
     */
    private Debug.DebugOutputListener mConsoleListener = new Debug.DebugOutputListener() {

        private Handler mTimeout;
        private boolean mFirstSent =true;
        private StringBuilder mBuffer = new StringBuilder();
        private Runnable mOnTimeout = new Runnable() {
            @Override
            public void run() {
                mCallback.onCommandResponds(mBuffer.toString());
                mConsole.removeDebugOutputListener(mConsoleListener);
            }
        };

        @Override
        public void onStdOutReceived(Debug debug, String message) {
            mBuffer.append(message);
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        /*
        * when the command is sent start the timeout to wait the response
        */
        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {

            if(!writeResult){
                mCallback.onCommandError();
                mConsole.removeDebugOutputListener(mConsoleListener);
                if(mTimeout!=null){
                    mTimeout.removeCallbacks(mOnTimeout);
                }
                return;
            }
            //else
            if(mFirstSent){
                mTimeout = new Handler(Looper.myLooper());
                mTimeout.postDelayed(mOnTimeout,mCommandTimeout);
                mFirstSent=false;
            }
        }
    };


    /**
     *
     * @param debug debug console where send the command
     * @param timeout time to wait to have the complete response
     */
    public ConsoleCommand(Debug debug, long timeout) {
        mConsole=debug;
        mCommandTimeout=timeout;
    }

    /**
     * send a command to the node
     * @param command command to send
     * @param callback object where send the command results
     */
    public void exec(String command, Callback callback) {
        mConsole.addDebugOutputListener(mConsoleListener);
        mCallback=callback;
        mConsole.write(command);
    }

    /**
     * interface where notify the command results
     */
    public interface Callback {
        /**
         * call when the command complete
         * @param response command response
         */
        void onCommandResponds(String response);

        /**
         * call when was not possible to send the command
         */
        void onCommandError();
    }
}
