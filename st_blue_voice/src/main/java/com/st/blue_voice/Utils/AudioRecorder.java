/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.blue_voice.Utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import com.st.blue_sdk.utils.NumberConversion;

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
    private static final int PERMISSION_REQUEST_ID = TAG.hashCode() % 16;

    //we use a single sheared thread to manage the write request
    private static Handler sWriteThread = null;
    private final Context mContext;
    private int mSamplingFreq = 8000;
    private short mChannels = 1;
    private final String mFileSuffix;
    private boolean mIsRecStarted = false;
    private RandomAccessFile mOut;
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
                writeLittleEndianEShort(mOut, mChannels); // number of channels
                writeLittleEndianEInt(mOut, mSamplingFreq); // sample rate
                writeLittleEndianEInt(mOut, (mSamplingFreq * 16 * mChannels) / 8); // byte rate //NOTE (Sample Rate * BitsPerSample * Channels) / 8
                writeLittleEndianEShort(mOut, (short) ((16 * mChannels) / 8)); // block align //NOTE (BitsPerSample * Channels) / 8
                writeLittleEndianEShort(mOut, (short) 16); // bits per sample //NOTE BitsPerSample
                mOut.writeBytes("data"); // subchunk 2 id
                writeLittleEndianEInt(mOut, 0); // subchunk 2 size
                mIsRecStarted = true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                mIsRecStarted = false;
            }
        }
    };
    private int mDataCursor;
    private String mDirectoryPath;
    private String mFileName;
    private final Runnable mCloseWavFile = new Runnable() {
        @Override
        public void run() {
            try {
                mOut.seek(4);
                writeLittleEndianEInt(mOut, 36 + (mDataCursor * 2)); // chunk size
                mOut.seek(22);
                writeLittleEndianEShort(mOut, mChannels); // number of channels
                writeLittleEndianEInt(mOut, mSamplingFreq); // sample rate
                writeLittleEndianEInt(mOut, (mSamplingFreq * 16 * mChannels) / 8); // byte rate //NOTE (Sample Rate * BitsPerSample * Channels) / 8
                writeLittleEndianEShort(mOut, (short) ((16 * mChannels) / 8)); // block align //NOTE (BitsPerSample * Channels) / 8
                mOut.seek(40);
                writeLittleEndianEInt(mOut, (mDataCursor * 2));
                mOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mIsRecStarted = false;
            File file = new File(mFileName);
//            if(!mActivity.isFinishing())
//                LogFeatureActivity.exportDataByMail(mActivity,mDirectoryPath,new File[]{file},false);
        }
    };
    //thread where all the file operation are executed
    private final Handler mWriteThread;

    public AudioRecorder(Context context, String fileSufix) {
        this.mContext = context;
        mFileSuffix = fileSufix;
        mWriteThread = createWriteQueue();
    }

    /**
     * initialize an singleton thread where queue the write request
     *
     * @return handler where queue the write request
     */
    private static Handler createWriteQueue() {
        synchronized (AudioRecorder.class) {
            if (sWriteThread == null) {
                HandlerThread temp = new HandlerThread(AudioRecorder.class.getName());
                temp.start();
                sWriteThread = new Handler(temp.getLooper());
            }
            return sWriteThread;
        }
    }

    /**
     * Open a File for audio recording.
     */
    private boolean openRecFile(String fileSuffix) {
        try {
            mDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/STMicroelectronics/logs";
            SimpleDateFormat DATE_FORMAT_PREFIX = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            Date mStartLog = new Date();
            String logPrefixName = DATE_FORMAT_PREFIX.format(mStartLog);
            mFileName = String.format("%s/%s_%s.wav", mDirectoryPath, logPrefixName, fileSuffix);

            File file = new File(mFileName);
            if (!file.exists())
                file.getParentFile().mkdirs();
            mOut = new RandomAccessFile(file, "rw");
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }//try-catch
    }

    /**
     * Start the audio record
     *
     * @return true if the recording is started
     */
    public boolean startRec() {

        if (!openRecFile(mFileSuffix))
            return false;

        if (!mIsRecStarted) {
            mWriteThread.post(mWriteWavHeader);
        }

        return true;
    }

    /**
     * Write an audio sample passed as parameters to the previously opened file
     *
     * @param sample the audio sample you want to write to file
     */
    public synchronized void writeSample(final short[] sample) {
        if (mIsRecStarted && sample != null) {
            mWriteThread.post(() -> {
                try {
                    writeSample(mOut, sample);
                    mDataCursor += sample.length;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });//post
        }//if
    }

    /**
     * Close the audio .wav file and open the dialog to send it via mail
     *
     * @return true if the recording is stopped.
     */
    public boolean stopRec() {
        if (mIsRecStarted) {
            mWriteThread.post(mCloseWavFile);
            return true;
        }
        return false;
    }

    /**
     * Update Audio Recorder parameters
     */
    public void updateParams(int samplingFreq, short channels) {
        this.mSamplingFreq = samplingFreq;
        this.mChannels = channels;
    }

    private void writeLittleEndianEInt(DataOutput output, int value) throws IOException {
        output.write(NumberConversion.LittleEndian.int32ToBytes(value));
    }

    private void writeLittleEndianEShort(DataOutput output, short value) throws IOException {
        output.write(NumberConversion.LittleEndian.int16ToBytes(value));
    }

    private void writeSample(DataOutput output, short[] value) throws IOException {
        for (short s : value) {
            writeLittleEndianEShort(output, s);
        }
    }

}
