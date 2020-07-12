package com.st.BlueMS.demos.fftAmpitude.settings;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.st.BlueSTSDK.Debug;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public interface FFTSettingsReadCallback{
        void onRead(@Nullable FFTSettings values);
    }

    public interface FFTSettingsWriteCallback{
        void onWrite(boolean success);
    }

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
        public void onStdOutReceived(Debug debug, String message) {
            mTimeout.removeCallbacks(onTimeout);
            mBuffer.append(message);
            //Log.d("Settings","response: "+mBuffer);
            if(SET_DONE_RESPONSE.matcher(mBuffer).find()){
                notifySettingsWrite(true);
            }else{
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }

        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            if(mBuffer==null) {
                mBuffer = new StringBuffer();
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }
        }
    }

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
        public void onStdOutReceived(Debug debug, String message) {
            mTimeout.removeCallbacks(onTimeout);
            mBuffer.append(message);
            FFTSettings readSettings = buildSettings(mBuffer);
            if(readSettings!=null){
                notifySettingsRead(readSettings);
            }else{
                //Log.d("FFTSettings","buffer: "+mBuffer.toString());
                mTimeout.postDelayed(onTimeout,COMMAND_TIMEOUT_MS);
            }

        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            mBuffer = new StringBuffer();
            mTimeout.postDelayed(onTimeout,COMMAND_TIMEOUT_MS);
        }
    }


}
