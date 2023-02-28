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
package com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;

/**
 * Implement the FwUpgradeConsole for a board running the BlueMs firmware.
 * In this case the protocol is:
 * mobile:upgrade[Ble|Fw]+length+fileCrc
 * node:fileCrc
 * mobile: file data, the file is spited in message of 16bytes
 * node: when all the byte are write return 1 if the crc is ok, -1 otherwise
 */
public class FwVersionConsoleNucleo extends FwVersionConsole {


    static private final String GET_VERSION_BOARD_FW="versionFw\n";
    static private final String GET_VERSION_BLE_FW="versionBle\n";

    /**
     * if all the messages are not send in 1s an error is fired
     */
    static private final int LOST_MSG_TIMEOUT_MS=1000;

    private Debug mConsole;

    /**
     * object that will receive the console data
     */
    private Debug.DebugOutputListener mCurrentListener;

    /**
     * Buffer where store the command response
     */
    private StringBuilder mBuffer;

    /**
     * object used for manage the get board id command
     */
    private GetVersionProtocol mConsoleGetFwVersion= new GetVersionProtocol();

    private static boolean isCompleteLine(StringBuilder buffer) {
        if(buffer.length()>2){
            String endLine = buffer.substring(buffer.length()-2);
            return endLine.equals("\r\n") || endLine.equals("\n\r");
        }

        return false;
    }

    /**
     * class used for wait/parse the fw version response
     */
    private class GetVersionProtocol implements Debug.DebugOutputListener {

        private @FirmwareType int mRequestFwType;
        private  int mNInvalidLine=0;

        /**
         * if the timeout is rise, fire an error of type
         * */
        private Runnable onTimeout = () -> notifyVersionRead(null);

        private void notifyVersionRead(FwVersion version){
            setConsoleListener(null);
            if (mCallback != null)
                mCallback.onVersionRead(FwVersionConsoleNucleo.this,mRequestFwType,version);
        }

        private void requestVersion(@FirmwareType int fwType){
            mRequestFwType=fwType;
            switch (fwType) {
                case FirmwareType.BLE_FW:
                    mConsole.write(GET_VERSION_BLE_FW);
                    break;
                case FirmwareType.BOARD_FW:
                    mConsole.write(GET_VERSION_BOARD_FW);
                    break;
                default:
                    notifyVersionRead(null);
                    break;
            }
        }

        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull String message) {
            mBuffer.append(message);
            //Log.i("FW upgrade","message="+message);
            //Log.i("FW upgrade","mBuffer="+mBuffer.toString());
            if (isCompleteLine(mBuffer)) {
                //remove time out
                mTimeout.removeCallbacks(onTimeout);
                //delete the termination "\r\n" or "\n\r"
                mBuffer.delete(mBuffer.length()-2,mBuffer.length());
                //Search if there are inside some messages that contains "\n" and delete them
                int subStringPos = 0;
                while(subStringPos!=-1) {
                    subStringPos = mBuffer.indexOf("\n");
                    if (subStringPos != -1) {
                        mBuffer.delete(subStringPos, subStringPos + 1);
                    }
                }
                //here we should have a clean string useful for searching the FW Version

                //check if it a valid fwVersion
                FwVersion version=null;
                try {
                    switch (mRequestFwType) {
                        case FirmwareType.BLE_FW:
                            version = new FwVersionBle(mBuffer.toString());
                            break;
                        case FirmwareType.BOARD_FW:
                            version = new FwVersionBoard(mBuffer.toString());
                            break;
                    }
                }catch (IllegalVersionFormatException e){
                    //remove invalid data and wait another timeout
                    mBuffer.delete(0,mBuffer.length());
                    if(++mNInvalidLine % 10 ==0) {
                        //send again the request message, the message get lost
                        requestVersion(mRequestFwType);
                    }
                    mTimeout.postDelayed(onTimeout,LOST_MSG_TIMEOUT_MS);
                    return;
                }//try-catch
                notifyVersionRead(version);
            }//else wait another package
        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull String message) { }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {
            mTimeout.postDelayed(onTimeout,LOST_MSG_TIMEOUT_MS);
        }
    }


    /**
     * handler used for the command timeout
     */
    private Handler mTimeout;

    /**
     * build a debug console without a callback
     * @param console console to use for send the command
     */
    FwVersionConsoleNucleo(Debug console){
        this(console,null);
    }

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    private FwVersionConsoleNucleo(Debug console, FwVersionConsole.FwVersionCallback callback) {
        super(callback);
        mConsole = console;
        mTimeout = new Handler(Looper.getMainLooper());
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


    private boolean isWaitingAnswer() {
        return mCurrentListener != null;
    }

    @Override
    public boolean readVersion(@FirmwareType int fwType) {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetFwVersion);
        mConsoleGetFwVersion.requestVersion(fwType);
        return true;
    }
}
