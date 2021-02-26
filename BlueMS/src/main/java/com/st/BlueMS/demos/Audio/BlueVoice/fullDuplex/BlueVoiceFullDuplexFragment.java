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

package com.st.BlueMS.demos.Audio.BlueVoice.fullDuplex;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.Utils.WaveformView;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.FeatureAudio;
import com.st.BlueSTSDK.Features.Audio.FeatureAudioConf;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedAudioOpusConf;
import com.st.BlueSTSDK.Features.Audio.Opus.ExportedFeatureAudioOpusVoice;
import com.st.BlueSTSDK.Features.Audio.Opus.FeatureAudioOpusConf;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.NodeServer;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Full Duplex audio streaming demo.
 */
@DemoDescriptionAnnotation(name="BlueVoice FullDuplex", iconRes= R.drawable.ic_bluetooth_audio)
public class BlueVoiceFullDuplexFragment extends BaseDemoFragment {

    private static final String BVFDCONF_PREFIX_KEY = BlueVoiceFullDuplexFragment.class.getCanonicalName();

    private static final int ENC_BITRATE = 24000;
    private static final String SENDING_STATUS_KEY = BVFDCONF_PREFIX_KEY+".SENDING_STATUS_KEY";
    private static final String RECEIVING_STATUS_KEY = BVFDCONF_PREFIX_KEY+".RECEIVING_STATUS_KEY";

    /** PCM extraction and Encode/send Threads parameters */
    private static final int THREAD_POOL_SIZE = 1;
    private static final int START_DELAY = 0;
    private final TimeUnit TIME_UNIT = TimeUnit.MICROSECONDS;

    /** Audio Encoder parameters*/
    private static final int TIME_uS = 20000;
    private static final short ENC_CHANNELS = 1;
    private static final int AUD_PKT_INPUT_SIZE = 16 * (TIME_uS/1000) * ENC_CHANNELS; //shorts
    private static final int MAX_ENC_SIZE = 300;
    private static final int ENC_SAMPLING_FREQ = 16000;
    private static final int ENC_APPLICATION_TYPE = 2048;
    private static final boolean ENC_IS_VBR = false;
    private static final int ENC_COMPLEXITY = 0;

    private static final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;

    private ExportedFeatureAudioOpusVoice mAudioTransmitter;
    private ExportedAudioOpusConf mAudioConfServer;
    private AudioCodecManager mAudioCodecManager;
    private FeatureAudio mAudio;

    private int audioSamplingFreq;
    private short audioChannels;

    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;

    private boolean isSending = false;

    private boolean flag_input1 = false;
    private short[] bufferInput1 = new short[320];
    private boolean flag_input2 = false;
    private short[] bufferInput2 = new short[320];

    private WaveformView mWaveformViewOut;
    private WaveformView mWaveformViewIn;
    private Switch startRecSwitch;

    byte[] codedBuf1 = new byte[640];
    byte[] codedBuf2 = new byte[640];

    private static final int REQUEST_MIC_REC_PERMISSION = 3;

    /**
     * listener for the audio feature, it will updates the audio values and if playback is active,
     * it write this data in the AudioTrack {@code audioTrack}.
     */
    private final Feature.FeatureListener mAudioListener = (f, sample) -> {
        short[] audioSample = ((FeatureAudio)f).getAudio(sample);
        if(audioSample != null) {
            playAudio(audioSample);
        }
    };

    /**
     * request to update the plot with the new audio data
     */
    private final Feature.FeatureListener mUpdatePlot = new Feature.FeatureListener() {
        @Override
        public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            short[] audioSample = ((FeatureAudio)f).getAudio(sample);
            if(audioSample != null)
                mWaveformViewIn.updateAudioData(audioSample);
        }
    };

    /////////////////////////////////////////// AUDIO CONF /////////////////////////////////////////
    /**
     * feature where we read the audio sync values
     */
    private FeatureAudioConf mAudioConf;

    /**
     * listener for the audioSync feature, it will update the synchronism values
     */
    private final Feature.FeatureListener mAudioConfListener = (f, sample) -> {
        if(mAudioCodecManager != null){
            mAudioCodecManager.updateParams(sample);
        }

        if(audioSamplingFreq != mAudioCodecManager.getSamplingFreq() ||
                audioChannels!= mAudioCodecManager.getChannels()) {
            audioSamplingFreq = mAudioCodecManager.getSamplingFreq();
            audioChannels = mAudioCodecManager.getChannels();
        }

        if(mAudioCodecManager.isAudioEnabled() != null) {
            if (mAudioCodecManager.isAudioEnabled()) {
                if(mAudio == null) {
                    startAudioStreaming(f.getParentNode());
                    storeIsReceivingStatus(true);
                }
            }
            else {
                stopAudioStreaming();
                storeIsReceivingStatus(false);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////

    void playAudio(short sample[]){
        synchronized (this) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAudioTrack.write(sample, 0, sample.length,
                        AudioTrack.WRITE_NON_BLOCKING);
            } else {
                mAudioTrack.write(sample, 0, sample.length);
            }
        }
    }

    private void startAudioStreaming(@NonNull Node node){
        mAudio = node.getFeature(FeatureAudio.class);
        if(mAudio!=null) {
            initAudioTrack(audioSamplingFreq, audioChannels);
            mAudio.addFeatureListener(mAudioListener);
            mAudio.addFeatureListener(mUpdatePlot);

            mAudio.setAudioCodecManager(mAudioCodecManager);
            node.enableNotification(mAudio);

            mAudioCodecManager.reinit();
            mAudioTrack.play();
            mWaveformViewIn.startPlotting();
        }
    }

    private void stopAudioStreaming(){
        if(mAudio!=null && mAudioConf!=null) {
            mAudio.removeFeatureListener(mAudioListener);
            mAudio.removeFeatureListener(mUpdatePlot);
            mAudio.disableNotification();
            mAudioTrack.stop();
            mWaveformViewIn.stopPlotting();
            mAudio = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAudio != null) {
            stopAudioStreaming();
        }
        isSending = false;
        if(mAudioRecord != null) {
            mAudioRecord.stop();
        }
        mWaveformViewOut.stopPlotting();
        startRecSwitch.setText(R.string.blueVoiceFD_start);
        stopProcess();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getIsReceivingStatus()) {
            startAudioStreaming(Objects.requireNonNull(getNode()));
        }
    }

    private void initAudioTrack(int samplingFreq, short channels) {
        int ch = channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO;
        int minBufSize = AudioTrack.getMinBufferSize(samplingFreq,
                ch,
                AudioFormat.ENCODING_PCM_16BIT);

        mAudioTrack = new AudioTrack(AUDIO_STREAM,
                samplingFreq,
                ch,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize,
                AudioTrack.MODE_STREAM
                );
    }

    private void initAudioRecord(int samplingFreq, short channels){
        int ch = channels == 1 ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        int minBufSize = AudioTrack.getMinBufferSize(samplingFreq,
                ch,
                AudioFormat.ENCODING_PCM_16BIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAudioRecord = new AudioRecord.Builder()
                    .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                    .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(samplingFreq)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build())
                    .build();
        }else {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                    samplingFreq,
                    channels,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize);
        }
    }

    private boolean checkRecordAudioPermission(){
        Activity activity = requireActivity();
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRecordAudioPermission(final int requestCode){
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Activity activity = requireActivity();
        if (requestCode == REQUEST_MIC_REC_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                unlockAudioRecording();
                startRecSwitch.setEnabled(true);
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(requireContext(), R.string.blueVoiceFD_micRecPermissionDenied, Toast.LENGTH_LONG).show();
                activity.finish();
            } else {
                Toast.makeText(requireContext(), R.string.blueVoiceFD_micRecPermissionDenied, Toast.LENGTH_LONG).show();
                startRecSwitch.setEnabled(false);
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    /** Encode and Send Thread */
    private ScheduledThreadPoolExecutor pool_EncSend;
    private void startEncSendTimer(){
        /* Scheduled at fixed rate, it allows to encode and send an extracted audio packet */
        pool_EncSend.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (isSending) {
                    if (flag_input1){
                        //OPUS Encoding
                        codedBuf1 = mAudioCodecManager.encode(bufferInput1);
                        //OPUS coded packets preparing(first byte) and sending
                        mAudioTransmitter.sendEncodedAudio(codedBuf1);
                        flag_input1 = false;
                    }
                    if (flag_input2) {
                        //OPUS Encoding
                        codedBuf2 = mAudioCodecManager.encode(bufferInput2);
                        //OPUS coded packets preparing(first byte) and sending
                        mAudioTransmitter.sendEncodedAudio(codedBuf2);
                        flag_input2 = false;
                    }
                }
            }
        },START_DELAY, TIME_uS, TIME_UNIT);
    }

    private void stopEncSendTimer(){
        pool_EncSend.shutdownNow();
        pool_EncSend = null;
    }

    /**
     * Starts the PCM Extraction procedure and the Encode/Send scheduling
     */
    private void startProcess(){
        if(mAudioConfServer != null){
            mAudioConfServer.sendCommand(FeatureAudioOpusConf.getEnableNotificationCmd(true));

            Acquisition mAcquisitionThread = new Acquisition();
            mAcquisitionThread.setPriority(Thread.MAX_PRIORITY);
            mAcquisitionThread.start();

            pool_EncSend = new ScheduledThreadPoolExecutor(THREAD_POOL_SIZE);
            startEncSendTimer();
        }
    }

    /**
     * Stops (Forced) the PCM Extraction procedure and the Encode/Send scheduling
     */
    private void stopProcess(){
        if(mAudioConfServer != null) {
            mAudioConfServer.sendCommand(FeatureAudioOpusConf.getEnableNotificationCmd(false));
            if (pool_EncSend != null) {
                stopEncSendTimer();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
        {
        View mRootView = inflater.inflate(R.layout.fragment_bluevoice_fullduplex, container,
                false);

        FeatureAudioConf mAudioConf = new FeatureAudioOpusConf(getNode());
        mAudioConf.setEncParams(MAX_ENC_SIZE,
                ENC_SAMPLING_FREQ,
                ENC_CHANNELS,
                AUD_PKT_INPUT_SIZE,
                ENC_APPLICATION_TYPE,
                ENC_BITRATE,
                ENC_IS_VBR,
                ENC_COMPLEXITY);
        mAudioCodecManager = mAudioConf.instantiateManager(true, true);

        mWaveformViewOut = mRootView.findViewById(R.id.blueVoiceFD_Out_waveform_view);
        mWaveformViewIn = mRootView.findViewById(R.id.blueVoiceFD_In_waveform_view);

        startRecSwitch = mRootView.findViewById(R.id.startRecSwitch);

        if(checkRecordAudioPermission()) {
            showIntroductionMessage("To avoid Larsen effect, please keep board and" +
                    " smartphone at least at 40 cm and control audio volume. Alternatively you can" +
                    " connect an headset ", requireContext());
            unlockAudioRecording();
        } else {
            requestRecordAudioPermission(REQUEST_MIC_REC_PERMISSION);
        }

        return mRootView;
    }

    private void unlockAudioRecording(){
        initAudioRecord(ENC_SAMPLING_FREQ, ENC_CHANNELS);

        startRecSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            isSending = b;
            storeIsSendingStatus(isSending);
            if (b) {
                mAudioRecord.startRecording();
                mWaveformViewOut.startPlotting();
                startRecSwitch.setText(R.string.blueVoiceFD_stop);
                startProcess();
            } else {
                mAudioRecord.stop();
                mWaveformViewOut.stopPlotting();
                startRecSwitch.setText(R.string.blueVoiceFD_start);
                stopProcess();
            }
        });
    }

    private void restoreGuiStatus() {
        startRecSwitch.setChecked(getIsSendingStatus());
    }

    private void storeIsSendingStatus(boolean isSendingStatus){
        requireActivity().getSharedPreferences(BVFDCONF_PREFIX_KEY, Context.MODE_PRIVATE).edit()
                .putBoolean(SENDING_STATUS_KEY, isSendingStatus)
                .apply();
    }

    private void storeIsReceivingStatus(boolean isReceivingStatus){
        requireActivity().getSharedPreferences(BVFDCONF_PREFIX_KEY, Context.MODE_PRIVATE).edit()
                .putBoolean(RECEIVING_STATUS_KEY, isReceivingStatus)
                .apply();
    }

    private boolean getIsSendingStatus(){
        return requireActivity().getSharedPreferences(BVFDCONF_PREFIX_KEY,Context.MODE_PRIVATE)
                .getBoolean(SENDING_STATUS_KEY, false);
    }

    private boolean getIsReceivingStatus(){
        return requireActivity().getSharedPreferences(BVFDCONF_PREFIX_KEY,Context.MODE_PRIVATE)
                .getBoolean(RECEIVING_STATUS_KEY, false);
    }

    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        menu.findItem(R.id.startLog).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        NodeServer server = node.getNodeServer();
        if(server == null)
            return;

        mAudioTransmitter = server.getExportedFeature(ExportedFeatureAudioOpusVoice.class);

        mAudioConf = node.getFeature(FeatureAudioConf.class);
        mAudioConfServer = server.getExportedFeature(ExportedAudioOpusConf.class);

        restoreGuiStatus();

        if(mAudioConf!=null && mAudioTransmitter!=null ) {

            mAudioCodecManager = mAudioConf.instantiateManager(true,false);
            audioSamplingFreq = mAudioCodecManager.getSamplingFreq();
            audioChannels = mAudioCodecManager.getChannels();

            mAudioConf.addFeatureListener(mAudioConfListener);
            node.enableNotification(mAudioConf);

            mAudioConf.setEncParams(MAX_ENC_SIZE,
                    ENC_SAMPLING_FREQ,
                    ENC_CHANNELS,
                    AUD_PKT_INPUT_SIZE,
                    ENC_APPLICATION_TYPE,
                    ENC_BITRATE,
                    ENC_IS_VBR,
                    ENC_COMPLEXITY);
            mAudioCodecManager = mAudioConf.instantiateManager(true,true);
        }
    }//enableNeededNotification

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {  }

    private class Acquisition extends Thread{
        public void run() {
            while (isSending) {
                if(!flag_input1){
                    mAudioRecord.read(bufferInput1, 0, 320);
                    mWaveformViewOut.updateAudioData(bufferInput1);
                    flag_input1 = true;
                }
                if(!flag_input2) {
                    mAudioRecord.read(bufferInput2, 0, 320);
                    mWaveformViewOut.updateAudioData(bufferInput2);
                    flag_input2 = true;
                }
            }
        }
    }
}