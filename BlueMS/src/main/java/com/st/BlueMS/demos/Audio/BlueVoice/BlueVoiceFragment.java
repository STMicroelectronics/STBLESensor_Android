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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASREngineFactory;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRLanguage;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRMessage;
import com.st.BlueMS.demos.Audio.BlueVoice.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.BlueVoice.util.AudioBuffer;
import com.st.BlueMS.demos.Audio.BlueVoice.util.DialogFragmentDismissCallback;
import com.st.BlueMS.demos.Audio.BlueVoice.util.DialogFragmentDismissCallback.DialogDismissCallback;
import com.st.BlueMS.demos.Audio.Utils.AudioRecorder;
import com.st.BlueMS.demos.Audio.Utils.WaveformView;
import com.st.BlueMS.demos.util.DemoWithNetFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.FeatureBeamforming;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BVAudioSyncManager;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Demo streaming the audio from the node.
 *
 * if a connection is available and the api key is set it can use the Google speech api to translate
 * audio to text
 */
@DemoDescriptionAnnotation(name="BlueVoice",iconRes= R.drawable.ic_bluetooth_audio,
        requareAll = {FeatureAudioADPCM.class,FeatureAudioADPCMSync.class})
public class BlueVoiceFragment extends DemoWithNetFragment implements ASRRequestCallback,DialogDismissCallback {

    private static final String TAG = BlueVoiceFragment.class.getCanonicalName();

    private static final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;

    private static final String VOLUME_LEVEL = TAG+".VOLUME_LEVEL";
    private static final String IS_MUTE = TAG+".IS_MUTE";
    private static final String ASR_RESULTS = TAG +".ASR_RESULTS";
    private static final String SELECTED_ASR_LANGUAGE = TAG+".SELECTED_ASR_LANGUAGE";

    private static final int AUDIO_SAMPLING_FREQ = 8000;
    private static final int MAX_RECORDING_TIME_S = 5;
    private static final String ASR_DIALOG_TAG = TAG+".ASR_AUTH_KEY_DIALOG";

    private static final @FeatureBeamforming.Direction int DEFAULT_BEAM_FORMING_DIRECTION = FeatureBeamforming.Direction.RIGHT;

    /////////////////////////////////////////// AUDIO //////////////////////////////////////////////
    /**
     * feature where we read  the audio values
     */
    private BVAudioSyncManager mBVAudioSyncManager = new BVAudioSyncManager();
    private FeatureAudioADPCM mAudio;

    private AudioManager mAudioManager;
    private AudioTrack mAudioTrack;

    private boolean mIsRecording;
    private AudioBuffer mRecordedAudio;

    private ASREngine mAsrEngine;

    private AudioRecorder mAudioWavDump;

    /**
     * listener for the audio feature, it will updates the audio values and if playback is active,
     * it write this data in the AudioTrack {@code audioTrack}.
     */
    private final Feature.FeatureListener mAudioListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(final Feature f, final Feature.Sample sample) {
            short[] audioSample = FeatureAudioADPCM.getAudio(sample);

            if(mIsRecording){
                if(mAsrEngine.hasContinuousRecognizer()) {
                    mAsrEngine.sendASRRequest(new AudioBuffer(audioSample),BlueVoiceFragment.this);
                } else {
                    final int nRecordedSample = mRecordedAudio.append(audioSample);
                    if(mRecordedAudio.isFull()){
                        sendAsrRequest();
                    }
                    updateGui(() -> mRecordBar.setProgress(nRecordedSample));
                }
            }
            else{
                playAudio(audioSample);
            }
        }

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

    private View mRootView;

    private View mAsrView;
    private Snackbar mAsrSnackbar;
    private TextView mAsrStatus;
    private AbsListView mAsrResultListView;
    private ArrayList<String> mAsrResults = new ArrayList<>();
    private ArrayAdapter<String> mAsrResultsAdapter;

    private TextView mRecordBarText;
    private ProgressBar mRecordBar;
    private TextView mRequestStatus;
    private ImageButton mRecButton;
    private WaveformView mWaveformView;

    private Switch mBeamformingSwitch;

    /**
     * load the language to use for the asr from a preference file, if not selected english is the
     * default one
     * @param c context to use to load the language
     * @return language to use for the asr
     */
    private static @ASRLanguage.Language int loadSelectedLanguage(Context c){
        SharedPreferences pref = c.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        //noinspection ResourceType
        return pref.getInt(SELECTED_ASR_LANGUAGE, ASRLanguage.Language.ENGLISH);
    }

    /**
     * function that store the user selected language in a shared preference file
     * @param c context to use to store the value
     * @param language value to store
     */
    private static void storeSelectedLanguage(Context c,@ASRLanguage.Language int language) {
        SharedPreferences pref = c.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        pref.edit()
                .putInt(SELECTED_ASR_LANGUAGE, language)
                .apply();
    }

    /**
     * Create an engine that works with the language selected by the user
     * @param context context to use to create the engine
     * @return engine to use to translate audio to text
     */
    private static ASREngine loadAsrEngine(Context context){
        @ASRLanguage.Language int language = loadSelectedLanguage(context);
        return ASREngineFactory.getASREngine(context, ASRLanguage.getLocale(language));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAudioTrack= new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLING_FREQ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                FeatureAudioADPCM.AUDIO_PACKAGE_SIZE,
                AudioTrack.MODE_STREAM);

        mAsrEngine = loadAsrEngine(getActivity());
        mAudioWavDump = new AudioRecorder((LogFeatureActivity) getActivity(),FeatureAudioADPCM.FEATURE_NAME);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAsrEngine.destroyListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_bluevoice2, container, false);

        //NOTE audio plot //////////////////////////////////////////////////////////////////////////
        mWaveformView = mRootView.findViewById(R.id.waveform_view);
        //NOTE /////////////////////////////////////////////////////////////////////////////////////

        //NOTE beamforming /////////////////////////////////////////////////////////////////////////
        mBeamformingSwitch = mRootView.findViewById(R.id.beamformingValue);
        setupBeamformingSwitch();
        //NOTE /////////////////////////////////////////////////////////////////////////////////////

        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);

        mVolumeBar = mRootView.findViewById(R.id.volumeValue);
        setUpVolumeBar(mAudioManager.getStreamMaxVolume(AUDIO_STREAM));

        mMuteButton = new ManageMuteButton(mRootView.findViewById(R.id.muteButton));

        TextView mSamplingRateValue = mRootView.findViewById(R.id.samplingRateValue);
        mSamplingRateValue.setText(String.format(Locale.getDefault(),"%d kHz", AUDIO_SAMPLING_FREQ/1000));

        mAsrStatus = mRootView.findViewById(R.id.asrStatusValue);

        mAsrView = mRootView.findViewById(R.id.card_view_asrResults);
        mAsrResultListView = mRootView.findViewById(R.id.asrResults);
        setupResultListView();

        mRecButton = mRootView.findViewById(R.id.recordButton);
        setupRecordButton();

        mRecordBar = mRootView.findViewById(R.id.recordTimeValue);
        mRecordBar.setIndeterminate(false);
        mRecordBarText = mRootView.findViewById(R.id.recordedTime);
        mRequestStatus = mRootView.findViewById(R.id.requestStatus);

        mAsrSnackbar = Snackbar.make(mRootView, R.string.blueVoice_loadAsrKey,Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.blueVoice_addKeyButton, view -> displayAuthKeyDialog());

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

    private void enableBeamFormingView(){
        mBeamformingSwitch.setVisibility(View.VISIBLE);
        View rootView = getView();
        if(rootView!=null)
            rootView.findViewById(R.id.beamformingLabel).setVisibility(View.VISIBLE);
    }
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    private void setupResultListView() {
        mAsrResultsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,mAsrResults);
        mAsrResultListView.setAdapter(mAsrResultsAdapter);
        mAsrResultListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            String res = mAsrResults.remove(i);
            mAsrResultsAdapter.notifyDataSetChanged();
            return !res.isEmpty();
        });
    }

    private ASREngine.ASRConnectionCallback mEngineConnectionCallback = new ASREngine.ASRConnectionCallback() {
        @Override
        public void onEngineStart() {
            mIsRecording=true;
            updateGui(() ->{
                if(isAdded()) {
                    mRequestStatus.setText(R.string.blueVoice_connected);
                    int color = getResources().getColor(R.color.blueVoice_continusStreamingButtonColor);
                    mRecButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            });
        }

        @Override
        public void onEngineFail(Throwable e) {
            onEngineStop();
            updateGui(()->{
                if(isAdded())
                    mRequestStatus.setText(e.getLocalizedMessage());
            });
        }

        @Override
        public void onEngineStop() {
            mIsRecording=false;
            updateGui(()->{
                if(isAdded()) {
                    mRequestStatus.setText("");
                    int color = getResources().getColor(R.color.colorAccent);
                    mRecButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            });
        }
    };

    private void setupRecordButton() {
        mRecButton.setOnTouchListener((view, motionEvent) -> {
            int action = motionEvent.getActionMasked();
            boolean returnValue = false;
            if(mAsrEngine.hasContinuousRecognizer()) {
                if (action == MotionEvent.ACTION_DOWN) {
                    if(!mIsRecording){
                        mRequestStatus.setText(R.string.blueVoice_connecting);
                        mAsrEngine.startListener(mEngineConnectionCallback);
                    }else{
                        mAsrEngine.stopListener(mEngineConnectionCallback);
                    }
                    returnValue=true;
                }
            } else {
                if (action == MotionEvent.ACTION_DOWN && !mIsRecording) {
                    mRecordedAudio = new AudioBuffer(AUDIO_SAMPLING_FREQ, MAX_RECORDING_TIME_S);
                    mRecordBar.setMax(mRecordedAudio.getBufferLength());
                    mRequestStatus.setText(R.string.blueVoice_recording);
                    mRecordBar.setVisibility(View.VISIBLE);
                    mRecordBarText.setVisibility(View.VISIBLE);
                    mIsRecording = true;
                    returnValue = true;
                }
                if ((action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_CANCEL ) && mIsRecording) {
                    mIsRecording = false;
                    sendAsrRequest();
                    returnValue = true;
                }
            }
            view.performClick();
            return returnValue;
        });
    }

    void sendAsrRequest(){
        mAsrEngine.sendASRRequest(mRecordedAudio,this);
        mRecordedAudio = mIsRecording ? new AudioBuffer(AUDIO_SAMPLING_FREQ, MAX_RECORDING_TIME_S) : null;
        updateGui(() -> {
            mRecordBar.setVisibility(View.GONE);
            mRecordBarText.setVisibility(View.GONE);
            mRequestStatus.setText(R.string.blueVoice_sendRequest);
            mRecordBar.setProgress(0);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAudioTrack.play();

    }

    @Override
    public void onStop(){
        super.onStop();
        stopAudioTrack();
        if(mAudioWavDump.isRecording())
            mAudioWavDump.stopRec();
    }

    private void restoreGuiStatus(Bundle savedInstanceState) {
        if(savedInstanceState.containsKey(VOLUME_LEVEL)){
            mVolumeBar.setProgress(savedInstanceState.getInt(VOLUME_LEVEL));
        }

        if(savedInstanceState.containsKey(IS_MUTE)){
            if(savedInstanceState.getBoolean(IS_MUTE)!=mMuteButton.isMute())
                mMuteButton.changeState();
        }

        if(savedInstanceState.containsKey(ASR_RESULTS)){
            ArrayList<String> oldResult=savedInstanceState.getStringArrayList(ASR_RESULTS);
            if (oldResult != null) {
                mAsrResultsAdapter.addAll(oldResult);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mVolumeBar!=null)
            outState.putInt(VOLUME_LEVEL,mVolumeBar.getProgress());

        if(mMuteButton!=null)
            outState.putBoolean(IS_MUTE,mMuteButton.isMute());

        if(!mAsrResults.isEmpty())
            outState.putStringArrayList(ASR_RESULTS,mAsrResults);

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

        inflater.inflate(R.menu.menu_bluevoice_feature_demo, menu);

        mAudioWavDump.registerRecordMenu(menu,inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * if the engine need it this function show the dialog to request the key to the user
     */
    private void displayAuthKeyDialog(){
        if(mAsrEngine.needAuthKey()){
            DialogFragment dialog = mAsrEngine.getAuthKeyDialog();
            if(dialog!=null)
                dialog.show(getChildFragmentManager(),ASR_DIALOG_TAG);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.showASRDialog){
            if(mAsrEngine.needAuthKey())
                displayAuthKeyDialog();
            else
                Snackbar.make(mRootView, R.string.blueVoice_noAsrKeeyNeeded,Snackbar.LENGTH_LONG).show();
            return true;
        }
        if(id == R.id.showLanguageDialog){
            DialogFragment dialog = new SelectLanguageDialog();
            dialog.show(getChildFragmentManager(),"selectLang");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mAudio = node.getFeature(FeatureAudioADPCM.class);
        mAudioSync = node.getFeature(FeatureAudioADPCMSync.class);
        mAudioBeamforming = node.getFeature(FeatureBeamforming.class);
        if(mAudio!=null && mAudioSync!=null) {
            mAudio.addFeatureListener(mAudioListener);

            mAudio.addFeatureListener(mUpdatePlot);
            mWaveformView.startPlotting();

            mAudio.addFeatureListener(mAudioListenerRec);

            mBVAudioSyncManager.reinitResetFlag();
            mAudio.setAudioSyncManager(mBVAudioSyncManager);

            node.enableNotification(mAudio);

            mAudioSync.addFeatureListener(mAudioSyncListener);
            node.enableNotification(mAudioSync);

            enableASR();
        }//if
        if(mAudioBeamforming!=null){
            enableBeamFormingView();
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
    }//enableNeededNotification

    /**
     * remove the listener and disable the notification
     *
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mAudio!=null) {
            mAudio.removeFeatureListener(mAudioListener);
           mAudio.removeFeatureListener(mUpdatePlot);
            mAudio.removeFeatureListener(mAudioListenerRec);

            node.disableNotification(mAudio);
            mWaveformView.stopPlotting();
            if(mAsrEngine.hasContinuousRecognizer())
                mAsrEngine.stopListener(mEngineConnectionCallback);

        }
        if(mAudioSync!=null) {
            mAudioSync.removeFeatureListener(mAudioSyncListener);
            node.disableNotification(mAudioSync);
        }

        if(mAudioBeamforming!=null){
            if(mBeamformingSwitch!=null && mBeamformingSwitch.isChecked())
                mAudioBeamforming.enableBeamForming(false);
            node.disableNotification(mAudioBeamforming);
        }
        if(mAsrSnackbar!=null)
            mAsrSnackbar.dismiss();


        updateGui(this::disableASR);
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

    private void enableASR(){
        if(!isOnline()){
            Snackbar.make(mRootView, R.string.blueVoice_enableConnection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        if(mAsrEngine.hasLoadedAuthKey()) {
            mAsrSnackbar.dismiss();
            final String enableValue = getResources().getString(R.string.blueVoice_asrEnabled,mAsrEngine.getName());
            updateGui(() -> {
                //we are online and we have the asr key
                mAsrView.setVisibility(View.VISIBLE);
                mRequestStatus.setVisibility(View.VISIBLE);
                mRecButton.setVisibility(View.VISIBLE);
                mAsrStatus.setText(enableValue);
            });
        } else {
            mAsrSnackbar.show();
            disableASR();
        }
    }

    @Override
    protected void onSystemHasConnectivity() {
        super.onSystemHasConnectivity();
        if (!asrIsEnabled()){
            enableASR();
        }//if
    }

    @Override
    protected void onSystemLostConnectivity() {
        super.onSystemLostConnectivity();
        disableASR();
    }

    private void disableASR() {
        mAsrView.setVisibility(View.GONE);
        mRequestStatus.setVisibility(View.GONE);
        mRecButton.setVisibility(View.GONE);
        mAsrStatus.setText(R.string.blueVoice_asrDisabled);
    }

    private boolean asrIsEnabled(){
        return mRecButton.getVisibility()==View.VISIBLE;
    }

    @Override
    public void onAsrRequestSend() {
        mRequestStatus.setText(R.string.blueVoice_waitForAsr);
    }

    @Override
    public void onAsrResponse(String text) {
        updateGui(() -> {
            //Log.d(TAG, "onAsrResponse: Resp:" + text);
            if(text!=null) {
                mAsrResultsAdapter.insert(text, 0);
                mRequestStatus.setText("");
            }
        });

    }

    @Override
    public void onAsrResponseError(@ASRMessage.Status int errorType){
        updateGui(() -> {
            if(getActivity()!=null)
                mRequestStatus.setText(ASRMessage.getMessage(getActivity(),errorType));
            if(mAsrEngine.hasContinuousRecognizer()){
                mIsRecording = false;
                mRecButton.getBackground().clearColorFilter();
            }
        });

    }

    /**
     * called when the user dismiss the asr key or asr language dialog -> we recreate the engine and
     * check if we can enable the asr.
     * @param dialog dialog that was dismissed
     */
    @Override
    public void onDialogDismiss(DialogFragment dialog) {
        mAsrEngine = loadAsrEngine(getActivity());
        enableASR();
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


    /**
     * dialog that display the available languages and permit to the user to select what is the
     * language to use
     */
    public static class SelectLanguageDialog extends DialogFragmentDismissCallback {

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Activity activity = getActivity();
            AlertDialog.Builder  builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.blueVoice_chooseAsrLanguage);

            String[] supportedLangs = ASRLanguage.getSupportedLanguages(activity);

            int selectedLang = loadSelectedLanguage(activity);

            builder.setSingleChoiceItems(supportedLangs, selectedLang, (dialogInterface, i) -> {
                storeSelectedLanguage(getActivity(),i);
                dialogInterface.dismiss();
            });

            builder.setNegativeButton(android.R.string.cancel,null);

            return builder.create();
        }//onCreateDialog

    }//SelectLanguageDialog

}