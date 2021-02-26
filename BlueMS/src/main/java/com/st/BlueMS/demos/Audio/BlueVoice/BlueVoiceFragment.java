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

package com.st.BlueMS.demos.Audio.BlueVoice;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.Utils.AudioRecorder;
import com.st.BlueMS.demos.Audio.Utils.WaveformView;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Features.Audio.FeatureAudio;
import com.st.BlueSTSDK.Features.Audio.FeatureAudioConf;
import com.st.BlueSTSDK.Features.FeatureBeamforming;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.util.Locale;

/**
 * Demo streaming the audio from the node.
 *
 * if a connection is available and the api key is set it can use the Google speech api to translate
 * audio to text
 */
@DemoDescriptionAnnotation(name="BlueVoice",iconRes= R.drawable.ic_bluetooth_audio,
        requareAll = {FeatureAudio.class,FeatureAudioConf.class})
public class BlueVoiceFragment extends BaseDemoFragment {

    private static final String BVCONF_PREFIX_KEY = BlueVoiceFragment.class.getCanonicalName();

    private static final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;

    private static final String VOLUME_LEVEL_KEY = BVCONF_PREFIX_KEY+".VOLUME_LEVEL_KEY";
    private static final String IS_MUTE_KEY = BVCONF_PREFIX_KEY+".IS_MUTE";

    private int audioSamplingFreq;
    private short audioChannels;

    private static final @FeatureBeamforming.Direction int DEFAULT_BEAM_FORMING_DIRECTION = FeatureBeamforming.Direction.RIGHT;

    /////////////////////////////////////////// AUDIO //////////////////////////////////////////////
    /**
     * feature where we read  the audio values
     */
    private AudioCodecManager mAudioCodecManager;
    private FeatureAudio mAudio;

    private AudioManager mAudioManager;
    private AudioTrack mAudioTrack;
    private AudioRecorder mAudioWavDump;

    private Switch mBeamformingSwitch;

    /**
     * listener for the audio feature, it will updates the audio values and if playback is active,
     * it write this data in the AudioTrack {@code audioTrack}.
     */
    private final Feature.FeatureListener mAudioListener = (f, sample) -> {
        short[] audioSample = ((FeatureAudio)f).getAudio(sample);
        if(audioSample != null)
            playAudio(audioSample);
    };

    /**
     * if we are recording, store the audio sample into a file
     */
    private final Feature.FeatureListener mAudioListenerRec = new Feature.FeatureListener() {

        @Override
        public void onUpdate(@NonNull final Feature f, @NonNull final Feature.Sample sample) {
            if(mAudioWavDump != null && mAudioWavDump.isRecording()) {
                short[] audioSample = ((FeatureAudio)f).getAudio(sample);
                if(audioSample != null)
                    mAudioWavDump.writeSample(audioSample);
            }
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
                mWaveformView.updateAudioData(audioSample);
        }
    };

    /////////////////////////////////////////// AUDIO SYNC /////////////////////////////////////////
    /**
     * feature where we read the audio sync values
     */
    private FeatureAudioConf mAudioSync;

    /**
     * listener for the audioSync feature, it will update the synchronism values
     */
    private final Feature.FeatureListener mAudioSyncListener = (f, sample) -> {
        if(mAudioCodecManager != null){
            mAudioCodecManager.updateParams(sample);
        }

        if(audioSamplingFreq != mAudioCodecManager.getSamplingFreq() ||
                audioChannels!= mAudioCodecManager.getChannels()) {
            audioSamplingFreq = mAudioCodecManager.getSamplingFreq();
            audioChannels = mAudioCodecManager.getChannels();
        }

        if(mAudioCodecManager.isAudioEnabled() != null) {
            if (mAudioCodecManager.isAudioEnabled())
                startAudioStreaming(f.getParentNode());
            else
                stopAudioStreaming();
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void initAudioTrack(int samplingFreq, short channels) {
        int minBufSize = AudioTrack.getMinBufferSize(samplingFreq,
                channels == 1 ? AudioFormat.CHANNEL_OUT_MONO : AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplingFreq,
                channels,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufSize,
                AudioTrack.MODE_STREAM);

        mAudioWavDump.updateParams(samplingFreq,channels);
    }

    private void updateCodecUI(String codecName, int samplingFreq) {
        updateGui(() -> {
            mSamplingRateValue.setText(String.format(Locale.getDefault(), "%d kHz", samplingFreq / 1000));
            mCodecValue.setText(codecName);
        });
    }

    private FeatureBeamforming mAudioBeamforming;

    private SeekBar mVolumeBar;
    private ManageMuteButton mMuteButton;

    private WaveformView mWaveformView;

    private TextView mSamplingRateValue;
    private TextView mCodecValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void initializeAudioDump(FeatureAudio fa) {
        LogFeatureActivity activity = (LogFeatureActivity) requireActivity();
        mAudioWavDump = new AudioRecorder(activity, fa != null ? fa.getName() : "Audio");
        activity.invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mRootView = inflater.inflate(R.layout.fragment_bluevoice, container, false);

        //NOTE audio plot //////////////////////////////////////////////////////////////////////////
        mWaveformView = mRootView.findViewById(R.id.blueVoice_waveform_view);
        //NOTE /////////////////////////////////////////////////////////////////////////////////////

        //NOTE beamforming /////////////////////////////////////////////////////////////////////////
        mBeamformingSwitch = mRootView.findViewById(R.id.blueVoice_beamformingValue);
        setupBeamformingSwitch();
        //NOTE /////////////////////////////////////////////////////////////////////////////////////
        mAudioManager = (AudioManager)requireContext().getSystemService(Context.AUDIO_SERVICE);

        mVolumeBar = mRootView.findViewById(R.id.blueVoice_volumeValue);
        setUpVolumeBar(mAudioManager.getStreamMaxVolume(AUDIO_STREAM));

        mMuteButton = new ManageMuteButton(mRootView.findViewById(R.id.blueVoice_muteButton));

        mSamplingRateValue = mRootView.findViewById(R.id.blueVoice_samplingRateValue);

        mCodecValue = mRootView.findViewById(R.id.blueVoice_codecValue);

        restoreGuiStatus();

        return mRootView;
    }

    //NOTE beamforming /////////////////////////////////////////////////////////////////////////////
    private void setupBeamformingSwitch() {
        mBeamformingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(mAudioBeamforming!=null) {
                mAudioBeamforming.enableBeamForming(isChecked);
                mAudioBeamforming.setBeamFormingDirection(DEFAULT_BEAM_FORMING_DIRECTION);
            }
        });
    }
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();
        if(mAudioWavDump!=null && mAudioWavDump.isRecording())
            mAudioWavDump.stopRec();
        if(mVolumeBar != null)
            storeVolumeLevel(mVolumeBar.getProgress());
        if(mMuteButton != null)
            storeMuteStatus(mMuteButton.isMute());
    }

    @Override
    public void onStop(){
        super.onStop();
        stopAudioTrack();
    }

    private void restoreGuiStatus(){
        mVolumeBar.setProgress(getVolumeLevel());
        if(getMuteStatus() != mMuteButton.isMute())
            mMuteButton.changeState();
    }

    private void storeVolumeLevel(int volumeLevel){
        requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY,Context.MODE_PRIVATE).edit()
                .putInt(VOLUME_LEVEL_KEY, volumeLevel)
                .apply();
    }

    private void storeMuteStatus(boolean isMute){
        requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY,Context.MODE_PRIVATE).edit()
                .putBoolean(IS_MUTE_KEY, isMute)
                .apply();
    }

    private int getVolumeLevel(){
        return requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY,Context.MODE_PRIVATE)
                .getInt(VOLUME_LEVEL_KEY, mAudioManager.getStreamVolume(AUDIO_STREAM));
    }

    private boolean getMuteStatus(){
        return requireActivity().getSharedPreferences(BVCONF_PREFIX_KEY,Context.MODE_PRIVATE)
                .getBoolean(IS_MUTE_KEY,false);
    }

    private void setUpVolumeBar(int maxVolume){
        mVolumeBar.setMax(maxVolume);
        mVolumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newVolumeLevel, boolean b) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolumeLevel, 0);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        mVolumeBar.setProgress(maxVolume / 2);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.startLog).setVisible(false);
        if(mAudioWavDump!=null)
            mAudioWavDump.registerRecordMenu(menu,inflater);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void startConfStreaming(@NonNull Node node){

        mAudioSync = node.getFeature(FeatureAudioConf.class);

        if(mAudioSync!=null) {

            mAudioCodecManager = mAudioSync.instantiateManager(true,false);
            audioSamplingFreq = mAudioCodecManager.getSamplingFreq();
            audioChannels = mAudioCodecManager.getChannels();

            mAudioSync.addFeatureListener(mAudioSyncListener);
            node.enableNotification(mAudioSync);
        }
    }

    private void startAudioStreaming(@NonNull Node node){
        mAudio = node.getFeature(FeatureAudio.class);

        mBeamformingSwitch.setEnabled(false);

        if(node.getFeature(FeatureBeamforming.class) != null){
            mAudioBeamforming = node.getFeature(FeatureBeamforming.class);
        }

        if(mAudio!=null) {

            initializeAudioDump(mAudio);
            initAudioTrack(audioSamplingFreq, audioChannels);
            updateCodecUI(mAudioCodecManager.getCodecName(), audioSamplingFreq);

            mAudio.addFeatureListener(mAudioListener);
            mAudio.addFeatureListener(mUpdatePlot);
            mAudio.addFeatureListener(mAudioListenerRec);

            mAudio.setAudioCodecManager(mAudioCodecManager);
            node.enableNotification(mAudio);

            mAudioCodecManager.reinit();
            mAudioTrack.play();

        }//if
        if(mAudioBeamforming!=null){
            node.enableNotification(mAudioBeamforming);
            //NOTE beamforming /////////////////////////////////////////////////////////////////////
            if(mBeamformingSwitch!=null) {
                if(mBeamformingSwitch.isChecked()){
                    mAudioBeamforming.enableBeamForming(true);
                    mAudioBeamforming.setBeamFormingDirection(DEFAULT_BEAM_FORMING_DIRECTION);
                }
                mAudioBeamforming.useStrongBeamformingAlgorithm(false);
            }
            //NOTE /////////////////////////////////////////////////////////////////////////////////
        }
        updateGui(this::displayStreamingAudioStatus);
    }

    private void displayStreamingAudioStatus(){
        mWaveformView.startPlotting();
        mBeamformingSwitch.setEnabled(mAudioBeamforming!=null);
    }

    private void stopConfStreaming(){
        if(mAudioSync!=null) {
            mAudioSync.removeFeatureListener(mAudioSyncListener);
            mAudioSync.disableNotification();
        }
    }

    private void stopAudioStreaming(){
        if(mAudio!=null && mAudioSync!=null) {
            mAudio.removeFeatureListener(mAudioListener);
            mAudio.removeFeatureListener(mUpdatePlot);
            mAudio.removeFeatureListener(mAudioListenerRec);
            mAudio.disableNotification();
        }
        if(mAudioBeamforming!=null){
            if(mBeamformingSwitch!=null && mBeamformingSwitch.isChecked())
                mAudioBeamforming.enableBeamForming(false);
            mAudioBeamforming.disableNotification();
        }
        updateGui(this::displayNoStreamingAudioStatus);
    }

    private void displayNoStreamingAudioStatus(){
        mWaveformView.stopPlotting();
        mBeamformingSwitch.setEnabled(false);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        startConfStreaming(node);
        startAudioStreaming(node);
    }//enableNeededNotification

    /**
     * remove the listener and disable the notification
     *
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        stopAudioStreaming();
        stopConfStreaming();
    }//disableNeedNotification

    private void stopAudioTrack(){
        synchronized(this) {
            if(mAudioTrack!=null) {
                mAudioTrack.pause();
                mAudioTrack.flush();
            }
        }
    }

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

    private class ManageMuteButton implements View.OnClickListener{

        private ImageButton mMuteButton;
        private boolean mIsMute;

        ManageMuteButton(ImageButton button){
            mIsMute=false;
            mMuteButton = button;
            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            changeState();
        }

        boolean isMute(){return mIsMute;}

        void changeState(){
            mIsMute=!mIsMute;
            if(mIsMute)
                muteAudio();
            else
                unMuteAudio();
        }

        private void muteAudio(){
            mMuteButton.setImageResource(R.drawable.ic_volume_off_black_32dp);
            mAudioManager.setStreamVolume(AUDIO_STREAM,0,0);
            mVolumeBar.setEnabled(false);
        }

        private void unMuteAudio(){
            mMuteButton.setImageResource(R.drawable.ic_volume_up_black_32dp);
            mAudioManager.setStreamVolume(AUDIO_STREAM,mVolumeBar.getProgress(),0);
            mVolumeBar.setEnabled(true);
        }
    }
}