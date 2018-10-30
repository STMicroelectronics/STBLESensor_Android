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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.Utils.AudioRecorder;
import com.st.BlueMS.demos.Audio.Utils.WaveformView;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.FeatureBeamforming;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BVAudioSyncManager;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.Locale;

/**
 * Demo streaming the audio from the node.
 *
 * if a connection is available and the api key is set it can use the Google speech api to translate
 * audio to text
 */
@DemoDescriptionAnnotation(name="BlueVoice",iconRes= R.drawable.ic_bluetooth_audio,
        requareAll = {FeatureAudioADPCM.class,FeatureAudioADPCMSync.class})
public class BlueVoiceFragment extends DemoFragment {

    private static final String TAG = BlueVoiceFragment.class.getCanonicalName();

    private static final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;

    private static final String VOLUME_LEVEL = TAG+".VOLUME_LEVEL";
    private static final String IS_MUTE = TAG+".IS_MUTE";

    private static final int AUDIO_SAMPLING_FREQ = 8000;

    private static final @FeatureBeamforming.Direction int DEFAULT_BEAM_FORMING_DIRECTION = FeatureBeamforming.Direction.RIGHT;

    /////////////////////////////////////////// AUDIO //////////////////////////////////////////////
    /**
     * feature where we read  the audio values
     */
    private BVAudioSyncManager mBVAudioSyncManager = new BVAudioSyncManager();
    private FeatureAudioADPCM mAudio;

    private AudioManager mAudioManager;
    private AudioTrack mAudioTrack;

    private AudioRecorder mAudioWavDump;

    /**
     * listener for the audio feature, it will updates the audio values and if playback is active,
     * it write this data in the AudioTrack {@code audioTrack}.
     */
    private final Feature.FeatureListener mAudioListener = (f, sample) -> {
        short[] audioSample = FeatureAudioADPCM.getAudio(sample);
        playAudio(audioSample);
    };

    /**
     * if we are recording, store the audio sample into a file
     */
    private final Feature.FeatureListener mAudioListenerRec = new Feature.FeatureListener() {

        @Override
        public void onUpdate(final Feature f, final Feature.Sample sample) {
            if(mAudioWavDump != null && mAudioWavDump.isRecording()) {
                short[] audioSample = FeatureAudioADPCM.getAudio(sample);
                mAudioWavDump.writeSample(audioSample);
            }
        }

    };

    /**
     * request to update the plot with the new audio data
     */
    private final Feature.FeatureListener mUpdatePlot = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            short[] audioSample = FeatureAudioADPCM.getAudio(sample);
            mWaveformView.updateAudioData(audioSample);
        }
    };

    /////////////////////////////////////////// AUDIO SYNC /////////////////////////////////////////
    /**
     * feature where we read the audio sync values
     */
    private FeatureAudioADPCMSync mAudioSync;

    /**
     * listener for the audioSync feature, it will update the synchronism values
     */
    private final Feature.FeatureListener mAudioSyncListener = (f, sample) -> {
        if(mBVAudioSyncManager!=null){
            mBVAudioSyncManager.setSyncParams(sample);
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private FeatureBeamforming mAudioBeamforming;

    private SeekBar mVolumeBar;
    private ManageMuteButton mMuteButton;

    private WaveformView mWaveformView;

    private Switch mBeamformingSwitch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAudioTrack= new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLING_FREQ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                FeatureAudioADPCM.AUDIO_PACKAGE_SIZE,
                AudioTrack.MODE_STREAM);

        mAudioWavDump = new AudioRecorder((LogFeatureActivity) getActivity(),FeatureAudioADPCM.FEATURE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View mRootView = inflater.inflate(R.layout.fragment_bluevoice, container, false);

        //NOTE audio plot //////////////////////////////////////////////////////////////////////////
        mWaveformView = mRootView.findViewById(R.id.blueVoice_waveform_view);
        //NOTE /////////////////////////////////////////////////////////////////////////////////////

        //NOTE beamforming /////////////////////////////////////////////////////////////////////////
        mBeamformingSwitch = mRootView.findViewById(R.id.blueVoice_beamformingValue);
        setupBeamformingSwitch();
        //NOTE /////////////////////////////////////////////////////////////////////////////////////

        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);

        mVolumeBar = mRootView.findViewById(R.id.blueVoice_volumeValue);
        setUpVolumeBar(mAudioManager.getStreamMaxVolume(AUDIO_STREAM));

        mMuteButton = new ManageMuteButton(mRootView.findViewById(R.id.blueVoice_muteButton));

        TextView mSamplingRateValue = mRootView.findViewById(R.id.blueVoice_samplingRateValue);
        mSamplingRateValue.setText(String.format(Locale.getDefault(),"%d kHz", AUDIO_SAMPLING_FREQ/1000));


        if(savedInstanceState!=null){
            restoreGuiStatus(savedInstanceState);
        }

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
    public void onStart() {
        super.onStart();
        mAudioTrack.play();

    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAudioWavDump.isRecording())
            mAudioWavDump.stopRec();
    }

    @Override
    public void onStop(){
        super.onStop();
        stopAudioTrack();
    }

    private void restoreGuiStatus(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey(VOLUME_LEVEL)){
            mVolumeBar.setProgress(savedInstanceState.getInt(VOLUME_LEVEL));
        }

        if(savedInstanceState.containsKey(IS_MUTE)){
            if(savedInstanceState.getBoolean(IS_MUTE)!=mMuteButton.isMute())
                mMuteButton.changeState();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mVolumeBar!=null)
            outState.putInt(VOLUME_LEVEL,mVolumeBar.getProgress());

        if(mMuteButton!=null)
            outState.putBoolean(IS_MUTE,mMuteButton.isMute());

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

        mAudioWavDump.registerRecordMenu(menu,inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void startAudioStreaming(@NonNull Node node){
        mAudio = node.getFeature(FeatureAudioADPCM.class);
        mAudioSync = node.getFeature(FeatureAudioADPCMSync.class);
        mAudioBeamforming = node.getFeature(FeatureBeamforming.class);
        if(mAudio!=null && mAudioSync!=null) {
            mAudio.addFeatureListener(mAudioListener);
            mAudio.addFeatureListener(mUpdatePlot);
            mAudio.addFeatureListener(mAudioListenerRec);

            mBVAudioSyncManager.reinitResetFlag();
            mAudio.setAudioSyncManager(mBVAudioSyncManager);

            node.enableNotification(mAudio);

            mAudioSync.addFeatureListener(mAudioSyncListener);
            node.enableNotification(mAudioSync);
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

    private void stopAudioStreaming(@NonNull Node node){
        if(mAudio!=null && mAudioSync!=null) {
            mAudio.removeFeatureListener(mAudioListener);
            mAudio.removeFeatureListener(mUpdatePlot);
            mAudio.removeFeatureListener(mAudioListenerRec);
            node.disableNotification(mAudio);

            mAudioSync.removeFeatureListener(mAudioSyncListener);
            node.disableNotification(mAudioSync);
        }

        if(mAudioBeamforming!=null){
            if(mBeamformingSwitch!=null && mBeamformingSwitch.isChecked())
                mAudioBeamforming.enableBeamForming(false);
            node.disableNotification(mAudioBeamforming);
        }
        updateGui(this::displayNoStreamingAudioStatus);
    }

    private void displayNoStreamingAudioStatus(){
        mWaveformView.stopPlotting();
        mBeamformingSwitch.setEnabled(false);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
          startAudioStreaming(node);
    }//enableNeededNotification

    /**
     * remove the listener and disable the notification
     *
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        stopAudioStreaming(node);
    }//disableNeedNotification

    private void stopAudioTrack(){
        synchronized(this) {
            mAudioTrack.pause();
            mAudioTrack.flush();
        }
    }

    void playAudio(short sample[]){
        synchronized (this) {
            mAudioTrack.write(sample, 0, sample.length);
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