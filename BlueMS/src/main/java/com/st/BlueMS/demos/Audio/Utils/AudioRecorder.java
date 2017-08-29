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
package com.st.BlueMS.demos.Audio.Utils;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.gui.preferences.LogPreferenceFragment;

import java.io.DataOutput;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * class that permit to record an audio [.wav] file and send as an e-mail attachment
 */
public class AudioRecorder {

    private static final String TAG = AudioRecorder.class.getCanonicalName();
    //asus zen phone 2 request to be lower than 16
    private static final int PERMISSION_REQUEST_ID = TAG.hashCode()%16;

    //we use a single sheared thread to manage the write request
    private static Handler sWriteThread=null;

    /**
     * initialize an singleton thread where queue the write request
     * @return handler where queue the write request
     */
    private static Handler createWriteQueue(){
        synchronized (AudioRecorder.class) {
            if(sWriteThread==null) {
                HandlerThread temp = new HandlerThread(AudioRecorder.class.getName());
                temp.start();
                sWriteThread = new Handler(temp.getLooper());
            }
            return sWriteThread;
        }
    }

    private final String mFileSuffix;
    private final LogFeatureActivity mActivity;
    private boolean mIsRecStarted = false;

    private RandomAccessFile mOut;
    private int mDataCursor;
    private String mDirectoryPath;
    private String mFileName;

    private final Runnable mWriteWavHeader = new Runnable() {
        @Override
        public void run() {
            try {
                mOut.writeBytes("RIFF"); // chunk id
                writeLittleEndianEInt(mOut, 0); // chunk size
                mOut.writeBytes("WAVE"); // format
                mOut.writeBytes("fmt "); // subchunk 1 id
                writeLittleEndianEInt(mOut, 16); // subchunk 1 size
                writeLittleEndianEShort(mOut, (short) 1); // audio format (1 = PCM)
                writeLittleEndianEShort(mOut, (short) 1); // number of channels
                writeLittleEndianEInt(mOut, 8000); // sample rate
                writeLittleEndianEInt(mOut, 8000 * 2); // byte rate
                writeLittleEndianEShort(mOut, (short) 4); // block align
                writeLittleEndianEShort(mOut, (short) 16); // bits per sample
                mOut.writeBytes("data"); // subchunk 2 id
                writeLittleEndianEInt(mOut, 0); // subchunk 2 size
                mIsRecStarted = true;
            } catch (IOException ioe){
                ioe.printStackTrace();
                mIsRecStarted=false;
            }
        }
    };

    private final Runnable mCloseWavFile = new Runnable() {
        @Override
        public void run() {
            try {
                mOut.seek(4);
                writeLittleEndianEInt(mOut, 36 + (mDataCursor *2)); // chunk size
                mOut.seek(40);
                writeLittleEndianEInt(mOut, (mDataCursor *2));
                mOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mIsRecStarted = false;
            File file = new File(mFileName);
            LogFeatureActivity.exportDataByMail(mActivity,mDirectoryPath,new File[]{file},false);
        }
    };

    //thread where all the file operation are executed
    private Handler mWriteThread;

    public AudioRecorder(LogFeatureActivity activity, String fileSufix) {
        mActivity = activity;
        mFileSuffix = fileSufix;
        mWriteThread = createWriteQueue();
    }

    /**
     * add a menu items start and stop the file recording
     * @param menu menu where add the items
     * @param inflater object used to load the menu item
     */
    public void registerRecordMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_record_audio, menu);
        final MenuItem stopMenuItem = menu.findItem(R.id.menu_stopAudioREC);
        final MenuItem startMenuItem = menu.findItem(R.id.menu_startAudioREC);
        startMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startRec(mFileSuffix)) {
                    startMenuItem.setVisible(false);
                    stopMenuItem.setVisible(true);
                }
            }
        });

        stopMenuItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMenuItem.setVisible(true);
                stopMenuItem.setVisible(false);
                stopRec();
            }
        });

        stopMenuItem.setVisible(false);
    }

    /**
     * return true if the recording is started
     * @return true if the recording is started.
     */
    public boolean isRecording() {
        return mIsRecStarted;
    }

    /**
     * Open a File for audio recording.
     */
    private boolean openRecFile(String fileSuffix){
        try {
            if(!mActivity.checkWriteSDPermission(PERMISSION_REQUEST_ID))
                return false;

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
            mDirectoryPath = sharedPrefs.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
            SimpleDateFormat DATE_FORMAT_PREFIX = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            Date mStartLog = new Date();
            String logPrefixName = DATE_FORMAT_PREFIX.format(mStartLog);
            mFileName = String.format("%s/%s_%s.wav", mDirectoryPath, logPrefixName, fileSuffix);

            File file = new File(mFileName);
            if(!file.exists())
                file.getParentFile().mkdirs();
            mOut = new RandomAccessFile(file,"rw");
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }//try-catch
    }

    /**
     * Start the audio record
     * @return true if the recording is started
     */
    public boolean startRec(String fileSufix){

        if(!openRecFile(fileSufix))
            return false;

        if(!mIsRecStarted){
            mWriteThread.post(mWriteWavHeader);
        }

        return true;
    }

    /**
     * Write an audio sample passed as parameters to the previously opened file
     * @param sample the audio sample you want to write to file
     */
    public synchronized void writeSample(final short[] sample){
        if(mIsRecStarted && sample!=null){
            mWriteThread.post(new Runnable() {
                @Override
                public void run() {
                try {
                    writeSample(mOut, sample);
                    mDataCursor += sample.length;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                }
            });//post
        }//if
    }

    /**
     * Close the audio .wav file and open the dialog to send it via mail
     * @return true if the recording is stopped.
     */
    public boolean stopRec(){
        if (mIsRecStarted) {
            mWriteThread.post(mCloseWavFile);
            return true;
        }
        return false;
    }

    private void writeLittleEndianEInt(DataOutput output, int value) throws IOException {
        output.write(NumberConversion.LittleEndian.int32ToBytes(value));
    }

    private void writeLittleEndianEShort(DataOutput output, short value) throws IOException {
        output.write(NumberConversion.LittleEndian.int16ToBytes(value));
    }

    private void writeSample(DataOutput output, short[] value) throws IOException {
        for(short s : value){
            writeLittleEndianEShort(output,s);
        }
    }

}
