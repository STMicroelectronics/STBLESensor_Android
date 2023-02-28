/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.BlueMS.demos.fftAmpitude.settings;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.st.BlueSTSDK.Debug;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * class with the protocol used to set the fft settings,
 * this class use the debug console to send and read the messages
 */
public class FFTSettingsConsole {

    private static final String READ_COMMAND = "getVibrParam";
    private static final String WRITE_COMMAND = "setVibrParam ";
    private static final String SET_ODR_FORMAT = WRITE_COMMAND +"-odr %d\n";
    private static final String SET_FULLSCALE_FREQ_FORMAT = WRITE_COMMAND +"-fs %d\n";
    private static final String SET_SIZE_FORMAT = WRITE_COMMAND +"-size %d\n";
    private static final String SET_WINDOW_FORMAT = WRITE_COMMAND +"-wind %d\n";
    private static final String SET_ACQUISITION_TIME_FORMAT = WRITE_COMMAND +"-tacq %d\n";

    private static final String SET_SUBRANGE_FORMAT = WRITE_COMMAND +"-subrng %d\n";
    private static final String SET_OVERLAP_FORMAT = WRITE_COMMAND +"-ovl %d\n";


    private static final String SET_ALL_FORMAT = WRITE_COMMAND+" -odr %d -fs %d -size %d -wind %d -tacq %d -subrng %d -ovl %d\r\n";

    private static final Pattern EXTRACT_ODR = Pattern.compile(".*FifoOdr\\s*=\\s*(\\d+)");
    private static final Pattern EXTRACT_FULLSCALE = Pattern.compile(".*fs\\s*=\\s*(\\d+)");
    private static final Pattern EXTRACT_WINDOWTYPE = Pattern.compile(".*wind\\s*=\\s*(\\d+)");
    private static final Pattern EXTRACT_SIZE = Pattern.compile(".*size\\s*=\\s*(\\d+)");
    private static final Pattern EXTRACT_ACQUSITION_TIME = Pattern.compile(".*tacq\\s*=\\s*(\\d+)");
    private static final Pattern EXTRACT_OVERLAP = Pattern.compile(".*ovl\\s*=\\s*(\\d+)");
    private static final Pattern EXTRACT_SUBRANGE = Pattern.compile(".*subrng\\s*=\\s*(\\d+)");
    private static final Pattern SET_DONE_RESPONSE = Pattern.compile(".*OK.*");
    private static final long COMMAND_TIMEOUT_MS = 2000;

    private @NonNull Debug mConsole;

    FFTSettingsConsole(@NonNull Debug console){
        mConsole = console;
    }

    public void setOdr(short value){
        String cmd = String.format(Locale.US,SET_ODR_FORMAT,value);
        mConsole.write(cmd);
    }

    public void setSize(short value){
        String cmd = String.format(Locale.US,SET_SIZE_FORMAT,value);
        mConsole.write(cmd);
    }

    public void setWindowType(FFTSettings.WindowType value){
        String cmd = String.format(Locale.US,SET_WINDOW_FORMAT,value.ordinal());
        mConsole.write(cmd);
    }

    public void read(FFTSettingsReadCallback callback){
        mConsole.addDebugOutputListener(new FFTSettingsReadListener(callback));
        mConsole.write(READ_COMMAND);
    }

    public void setFullScaleFrequency(short newFs) {
        String cmd = String.format(Locale.US, SET_FULLSCALE_FREQ_FORMAT,newFs);
        mConsole.write(cmd);
    }

    public void setSubRange(short newSubRange) {
        String cmd = String.format(Locale.US,SET_SUBRANGE_FORMAT,newSubRange);
        mConsole.write(cmd);
    }

    /**
     * set all the settings and call the callback when the command is applied
     * @param newSettings value to set
     * @param callback object to notify when the settings are set
     */
    public void write(@NonNull FFTSettings newSettings, @Nullable FFTSettingsWriteCallback callback) {
        String cmd = String.format(Locale.US, SET_ALL_FORMAT,newSettings.odr,newSettings.fullScale,
                newSettings.size,newSettings.winType.ordinal(),
                newSettings.acquisitionTime_s,newSettings.subRange,newSettings.overlap);
        if(callback!=null)
            mConsole.addDebugOutputListener(new FFTSettingsWriteListener(callback));
        mConsole.write(cmd);
    }

    private void setAcquisitionTime(int acquisitionTime_s) {
        String cmd = String.format(Locale.US, SET_OVERLAP_FORMAT,acquisitionTime_s);
        mConsole.write(cmd);
    }

    private void setOverlap(byte overlap) {
        String cmd = String.format(Locale.US, SET_ACQUISITION_TIME_FORMAT,overlap);
        mConsole.write(cmd);
    }

    /**
     * interface to implement to receive the current settings
     */
    public interface FFTSettingsReadCallback{
        /**
         * function cole when the settings are read
         * @param values current settings or null if the read fails
         */
        void onRead(@Nullable FFTSettings values);
    }

    /**
     * interface to implement to receive the write status
     */
    public interface FFTSettingsWriteCallback{
        /**
         * call when the settings write complete
         * @param success true if the settings are set, false if there was an transmission error or
         *                an error applying the settings
         */
        void onWrite(boolean success);
    }

    /**
     * class used to wait the set settings response
     */
    private final class FFTSettingsWriteListener implements Debug.DebugOutputListener{
        private StringBuffer mBuffer;

        private FFTSettingsWriteCallback mCallback;
        /**
         * handler used for the command timeout
         */
        private Handler mTimeout;
        private Runnable onTimeout = () -> notifySettingsWrite(false);

        private void notifySettingsWrite(boolean success){
            mConsole.removeDebugOutputListener(this);
            mCallback.onWrite(success);
        }

        FFTSettingsWriteListener(FFTSettingsWriteCallback callback){
            mCallback = callback;
            mTimeout = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onStdOutReceived(@NotNull Debug debug, @NotNull String message) {
            mTimeout.removeCallbacks(onTimeout);
            mBuffer.append(message);
            Log.d("Settings","response: "+mBuffer);
            if(SET_DONE_RESPONSE.matcher(mBuffer).find()){
                notifySettingsWrite(true);
            }else{
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }

        }

        @Override
        public void onStdErrReceived(@NotNull Debug debug, @NotNull String message) {

        }

        @Override
        public void onStdInSent(@NotNull Debug debug, @NotNull String message, boolean writeResult) {
            if(mBuffer==null) {
                mBuffer = new StringBuffer();
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }
        }
    }

    /**
     * class used to receive the current settings from the board
     */
    private final class FFTSettingsReadListener implements Debug.DebugOutputListener{

        private StringBuffer mBuffer;

        private FFTSettingsReadCallback mCallback;
        /**
         * handler used for the command timeout
         */
        private Handler mTimeout;
        private Runnable onTimeout = () -> notifySettingsRead(null);

        private @Nullable Integer extractInt(CharSequence str, Pattern regExp){
            Matcher match = regExp.matcher(str);
            if(match.find()) {
                try {
                    return Integer.decode(match.group(1));
                } catch (NumberFormatException | IllegalStateException | IndexOutOfBoundsException e) {
                    return null;
                }
            }
            return null;
        }

        private void notifySettingsRead(@Nullable FFTSettings settings){
            mConsole.removeDebugOutputListener(this);
            mCallback.onRead(settings);
        }

        private @Nullable FFTSettings buildSettings(CharSequence str){
            Integer odr = extractInt(str,EXTRACT_ODR);
            Integer fullScale = extractInt(str,EXTRACT_FULLSCALE);
            Integer winType = extractInt(str,EXTRACT_WINDOWTYPE);
            Integer size = extractInt(str,EXTRACT_SIZE);
            Integer acquisitionTime = extractInt(str,EXTRACT_ACQUSITION_TIME);
            Integer overlap = extractInt(str, EXTRACT_OVERLAP);
            Integer subrange = extractInt(str, EXTRACT_SUBRANGE);

            if(odr!=null && fullScale!=null && winType!=null && size!=null && acquisitionTime!=null
                && overlap!=null){
                return new FFTSettings(odr.shortValue(),
                        fullScale.byteValue(),
                        size.shortValue(),
                        FFTSettings.WindowType.fromByte(winType.byteValue()),
                        acquisitionTime.shortValue(),
                        overlap.byteValue(),
                        subrange!=null ? subrange.byteValue() : 1
                );
            } else {
                return null;
            }
        }

        FFTSettingsReadListener(FFTSettingsReadCallback callback){
            mCallback = callback;
            mTimeout = new Handler(Looper.getMainLooper());
        }

        @Override
        public void onStdOutReceived(@NonNull Debug debug, @NonNull String message) {
            if(mBuffer == null) //a message from the console arrive, but wasn't the answer to our command
                return;
            mTimeout.removeCallbacks(onTimeout);
            mBuffer.append(message);
            FFTSettings readSettings = buildSettings(mBuffer);
            if(readSettings!=null){
                notifySettingsRead(readSettings);
            }else{
                Log.d("FFTSettings","buffer: "+mBuffer.toString());
                mTimeout.postDelayed(onTimeout,COMMAND_TIMEOUT_MS);
            }

        }

        @Override
        public void onStdErrReceived(@NonNull Debug debug, @NonNull String message) {

        }

        @Override
        public void onStdInSent(@NonNull Debug debug, @NonNull String message, boolean writeResult) {
            mBuffer = new StringBuffer();
            mTimeout.postDelayed(onTimeout,COMMAND_TIMEOUT_MS);
        }
    }


}
