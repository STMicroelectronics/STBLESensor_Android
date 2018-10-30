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

package com.st.BlueMS.demos.Audio.SpeechToText;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASREngineFactory;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRLanguage;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRMessage;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRRequestCallback;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.ASRSelector.AsrSelectorDialogFragment;
import com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.GoogleASR.GoogleASREngine;
import com.st.BlueMS.demos.Audio.SpeechToText.util.DialogFragmentDismissCallback;
import com.st.BlueMS.demos.Audio.Utils.AudioBuffer;
import com.st.BlueMS.demos.Audio.Utils.AudioRecorder;
import com.st.BlueMS.demos.util.DemoWithNetFragment;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.FeatureBeamforming;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BVAudioSyncManager;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;


import java.util.ArrayList;


/**
 * Demo streaming the audio to an online service for speech to text
 *
 * if a connection is available and the api key is set it can use different speech to text api to
 * translate  audio to text
 */
@DemoDescriptionAnnotation(name="SpeechToText",iconRes= R.drawable.ic_bluetooth_audio,
        requareAll = {FeatureAudioADPCM.class,FeatureAudioADPCMSync.class})
public class SpeechToTextFragment extends DemoWithNetFragment implements ASRRequestCallback,
        AsrSelectorDialogFragment.AsrSelectorCallback,
        DialogFragmentDismissCallback.DialogDismissCallback{

    private static final String TAG = SpeechToTextFragment.class.getCanonicalName();

    private static final String ASR_RESULTS = TAG +".ASR_RESULTS";
    private static final String SELECTED_ASR_LANGUAGE = TAG+".SELECTED_ASR_LANGUAGE";
    private static final String SELECTED_ASR_ENGINE = TAG+".SELECTED_ASR_ENGINE";

    private static final String DEFAULT_ASR_ENGINE = GoogleASREngine.DESCRIPTION.getName();
    private static final @ASRLanguage.Language int DEFAULT_ASR_LANGUAGE = ASRLanguage.Language.ENGLISH_UK;

    private static final int AUDIO_SAMPLING_FREQ = 8000;
    private static final int MAX_RECORDING_TIME_S = 5;
    private static final String ASR_KEY_DIALOG_TAG = TAG+".ASR_AUTH_KEY_DIALOG";
    private static final String ASR_ENGINE_DIALOG_TAG = TAG+".ASR_ENGINE_DIALOG";

    private static final @FeatureBeamforming.Direction int DEFAULT_BEAM_FORMING_DIRECTION = FeatureBeamforming.Direction.RIGHT;

    /////////////////////////////////////////// AUDIO //////////////////////////////////////////////
    /**
     * feature where we read  the audio values
     */
    private BVAudioSyncManager mBVAudioSyncManager = new BVAudioSyncManager();
    private FeatureAudioADPCM mAudio;

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
                    mAsrEngine.sendASRRequest(new AudioBuffer(audioSample), SpeechToTextFragment.this);
                } else {
                    final int nRecordedSample = mRecordedAudio.append(audioSample);
                    if(mRecordedAudio.isFull()){
                        sendAsrRequest();
                    }
                    updateGui(() -> mRecordBar.setProgress(nRecordedSample));
                }
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


    private FeatureAccelerationEvent mAccEvent;

    private Feature.FeatureListener mTapListener = new Feature.FeatureListener() {
        private static final long MIN_EVENT_TIME_DIFFERENCE_MS = 1000;
        private long mLastEvent=0;

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if(FeatureAccelerationEvent.hasAccelerationEvent(sample,FeatureAccelerationEvent.DOUBLE_TAP)){
                long now = System.currentTimeMillis();
                if(now-mLastEvent > MIN_EVENT_TIME_DIFFERENCE_MS) {
                    mLastEvent = now;
                    changeAudioStreamStatus();
                }
            }


        }
    };

    private void changeAudioStreamStatus() {
        if(!mIsRecording){
            updateGui(()-> mEngineStatus.setText(R.string.stt_engineStatus_connecting));
            mAsrEngine.startListener(mEngineConnectionCallback);
        }else{
            mAsrEngine.stopListener(mEngineConnectionCallback);
        }
    }


    private View mRootView;

    private View mAsrView;
    private Snackbar mAsrSnackbar;
    private AbsListView mAsrResultListView;
    private ArrayList<String> mAsrResults = new ArrayList<>();
    private ArrayAdapter<String> mAsrResultsAdapter;

    private ProgressBar mRecordBar;
    private TextView mEngineStatus;
    private ImageButton mRecButton;

    private Switch mBeamformingSwitch;

    /**
     * load the language to use for the asr from a preference file, if not selected english is the
     * default one
     * @param c context to use to load the language
     * @return language to use for the asr
     */
    private static @ASRLanguage.Language int loadSelectedLanguage(Context c){
        SharedPreferences pref = c.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        return pref.getInt(SELECTED_ASR_LANGUAGE, DEFAULT_ASR_LANGUAGE);
    }

    private static String loadSelectedEngine(Context c){
        SharedPreferences pref = c.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        return pref.getString(SELECTED_ASR_ENGINE,DEFAULT_ASR_ENGINE);
    }

    /**
     * function that store the user selected language in a shared preference file
     * @param c context to use to store the value
     * @param language value to store
     */
    private static void storeSelectedLanguage(Context c,String engineName,@ASRLanguage.Language int language) {
        SharedPreferences pref = c.getSharedPreferences(TAG,Context.MODE_PRIVATE);
        pref.edit()
                .putInt(SELECTED_ASR_LANGUAGE, language)
                .putString(SELECTED_ASR_ENGINE,engineName)
                .apply();
    }

    /**
     * Create an engine that works with the language selected by the user
     * @param context context to use to create the engine
     * @return engine to use to translate audio to text
     */
    private static @Nullable
    ASREngine loadAsrEngine(Context context){
        @ASRLanguage.Language int language = loadSelectedLanguage(context);
        String name = loadSelectedEngine(context);
        return ASREngineFactory.getASREngine(context,name,language);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAudioWavDump = new AudioRecorder((LogFeatureActivity) getActivity(),FeatureAudioADPCM.FEATURE_NAME);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAsrEngine!=null)
            mAsrEngine.destroyListener();
    }

    private View mSetEngineKeyButton;
    private TextView mEngineName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_speech_to_text, container, false);

        View selectEngineButton = mRootView.findViewById(R.id.stt_selectEngineButton);
        selectEngineButton.setOnClickListener(v -> showSelectEngineDialog());

        mSetEngineKeyButton = mRootView.findViewById(R.id.stt_setEngineKeyButton);
        mSetEngineKeyButton.setOnClickListener(v -> showSetEngineKeyDialog());


        mEngineName = mRootView.findViewById(R.id.stt_engineNameValue);

        mBeamformingSwitch = mRootView.findViewById(R.id.blueVoice_beamformingValue);
        setupBeamformingSwitch();

        mAsrView = mRootView.findViewById(R.id.card_view_asrResults);
        mAsrResultListView = mRootView.findViewById(R.id.asrResults);
        setupResultListView();

        mRecButton = mRootView.findViewById(R.id.stt_recordButton);
        setupRecordButton();

        mRecordBar = mRootView.findViewById(R.id.stt_recordProgress);
        mRecordBar.setIndeterminate(false);
        mEngineStatus = mRootView.findViewById(R.id.sst_engineStatus);

        mAsrSnackbar = Snackbar.make(mRootView, R.string.blueVoice_loadAsrKey,Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.blueVoice_addKeyButton, view -> showSetEngineKeyDialog());

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
            rootView.findViewById(R.id.blueVoice_beamformingLabel).setVisibility(View.VISIBLE);
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
            startAudioStreaming();
            updateGui(() ->{
                if(isAdded()) {
                    mEngineStatus.setText(R.string.stt_engineStatus_connected);
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
                    mEngineStatus.setText(e.getLocalizedMessage());
            });
        }

        @Override
        public void onEngineStop() {
            mIsRecording=false;
            stopAudioStreaming();
            updateGui(()->{
                if(isAdded()) {
                    mEngineStatus.setText(R.string.stt_engineStatus_disconnected);
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
                        mEngineStatus.setText(R.string.stt_engineStatus_connecting);
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
                    mAsrEngine.startListener(mEngineConnectionCallback);
                    mEngineStatus.setText(R.string.stt_engineStatus_recording);
                    mRecordBar.setProgress(0);
                    mRecordBar.setVisibility(View.VISIBLE);
                    returnValue = true;
                }
                if ((action == MotionEvent.ACTION_UP ||
                        action == MotionEvent.ACTION_CANCEL ) && mIsRecording) {
                    mAsrEngine.stopListener(mEngineConnectionCallback);
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
            mEngineStatus.setText(R.string.blueVoice_sendRequest);
            mRecordBar.setProgress(0);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mAsrEngine = loadAsrEngine(getActivity());
        displayEngineInfo(mAsrEngine);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAudioWavDump.isRecording())
            mAudioWavDump.stopRec();
    }

    private void restoreGuiStatus(Bundle savedInstanceState) {

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

        if(!mAsrResults.isEmpty())
            outState.putStringArrayList(ASR_RESULTS,mAsrResults);

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.startLog).setVisible(false);

        mAudioWavDump.registerRecordMenu(menu,inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * if the engine need it this function show the dialog to request the key to the user
     */
    private void showSetEngineKeyDialog(){
        if(mAsrEngine!=null && mAsrEngine.needAuthKey()){
            DialogFragment dialog = mAsrEngine.getAuthKeyDialog();
            if(dialog!=null)
                dialog.show(getChildFragmentManager(),ASR_KEY_DIALOG_TAG);
        }
    }

    private void showSelectEngineDialog(){
        DialogFragment dialog = new AsrSelectorDialogFragment();
        dialog.show(getChildFragmentManager(),ASR_ENGINE_DIALOG_TAG);
    }


    private void startAudioStreaming(){
        Node node = getNode();
        if(node==null)
            return;

        mAudio = node.getFeature(FeatureAudioADPCM.class);
        mAudioSync = node.getFeature(FeatureAudioADPCMSync.class);
        mAudioBeamforming = node.getFeature(FeatureBeamforming.class);
        if(mAudio!=null && mAudioSync!=null) {
            mAudio.addFeatureListener(mAudioListener);

            mAudio.addFeatureListener(mAudioListenerRec);

            mBVAudioSyncManager.reinitResetFlag();
            mAudio.setAudioSyncManager(mBVAudioSyncManager);

            node.enableNotification(mAudio);

            mAudioSync.addFeatureListener(mAudioSyncListener);
            node.enableNotification(mAudioSync);

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
    }

    private void stopAudioStreaming(){
        Node node = getNode();
        if(node==null)
            return;

        if(mAudio!=null && mAudioSync!=null &&
                node.isEnableNotification(mAudio) &&
                node.isEnableNotification(mAudioSync)) {
            mAudio.removeFeatureListener(mAudioListener);
            mAudio.removeFeatureListener(mAudioListenerRec);

            node.disableNotification(mAudio);
            if(mAsrEngine.hasContinuousRecognizer())
                mAsrEngine.stopListener(mEngineConnectionCallback);

            mAudioSync.removeFeatureListener(mAudioSyncListener);
            node.disableNotification(mAudioSync);
        }

        if(mAudioBeamforming!=null &&
                node.isEnableNotification(mAudioBeamforming)){
            if(mBeamformingSwitch!=null && mBeamformingSwitch.isChecked())
                mAudioBeamforming.enableBeamForming(false);
            node.disableNotification(mAudioBeamforming);
        }
        if(mAsrSnackbar!=null)
            mAsrSnackbar.dismiss();
    }


    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mAccEvent = node.getFeature(FeatureAccelerationEvent.class);
        if(mAccEvent!=null) {
            mAccEvent.detectEvent(FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT,false);
            mAccEvent.detectEvent(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP,true);
            mAccEvent.addFeatureListener(mTapListener);

            node.enableNotification(mAccEvent);
        }

        mBeamformingSwitch.setEnabled(node.getFeature(FeatureBeamforming.class)!=null);

    }//enableNeededNotification

    /**
     * remove the listener and disable the notification
     *
     * @param node node where disable the notification
     */
    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if(mAccEvent!=null){
            mAccEvent.removeFeatureListener(mTapListener);
            node.disableNotification(mAccEvent);
        }
        stopAudioStreaming();
        if(mAsrEngine!=null && mIsRecording){
            mAsrEngine.stopListener(mEngineConnectionCallback);
            mAsrEngine.destroyListener();
        }
    }//disableNeedNotification



    private void enableASRView(){
        if(!isOnline()){
            Snackbar.make(mRootView, R.string.blueVoice_enableConnection,
                    Snackbar.LENGTH_LONG).show();
            return;
        }
        if(mAsrEngine!=null && mAsrEngine.hasLoadedAuthKey()) {
            mAsrSnackbar.dismiss();
            updateGui(() -> {
                //we are online and we have the asr key
                mAsrView.setVisibility(View.VISIBLE);
                mRecButton.setVisibility(View.VISIBLE);
            });
        } else {
            mAsrSnackbar.show();
            disableASRView();
        }
    }

    @Override
    protected void onSystemHasConnectivity() {
        super.onSystemHasConnectivity();
        if (!asrIsEnabled()){
            enableASRView();
        }//if
    }

    @Override
    protected void onSystemLostConnectivity() {
        super.onSystemLostConnectivity();
        disableASRView();
    }

    private void disableASRView() {
        mEngineName.setText(R.string.stt_engine_disabled);
    }

    private boolean asrIsEnabled(){
        return mRecButton.getVisibility()==View.VISIBLE;
    }

    @Override
    public void onAsrRequestSend() {
        mEngineStatus.setText(R.string.blueVoice_waitForAsr);
    }

    @Override
    public void onAsrResponse(String text) {
        updateGui(() -> {
            //Log.d(TAG, "onAsrResponse: Resp:" + text);
            if(text!=null) {
                mAsrResultsAdapter.insert(text, 0);
            }
        });

    }

    @Override
    public void onAsrResponseError(@ASRMessage.Status int errorType){
        updateGui(() -> {
            if(getActivity()!=null)
                mEngineStatus.setText(ASRMessage.getMessage(getActivity(),errorType));
            if(mAsrEngine.hasContinuousRecognizer()){
                mIsRecording = false;
                mRecButton.getBackground().clearColorFilter();
            }
        });

    }

    @Override
    public void onAsrEngineSelected(String name, int language) {
        storeSelectedLanguage(getActivity(),name,language);
        mAsrEngine = loadAsrEngine(getActivity());
        displayEngineInfo(mAsrEngine);
        enableASRView();
    }

    private void displayEngineInfo(ASREngine engine){
        if(engine==null)
            return;
        mEngineName.setText(getString(R.string.stt_engine_name_format,
                engine.getDescription().getName(),
                ASRLanguage.getLanguage(getActivity(),loadSelectedLanguage(getActivity()))));
        if( engine.needAuthKey()){
            mSetEngineKeyButton.setVisibility(View.VISIBLE);
        }else{
            mSetEngineKeyButton.setVisibility(View.GONE);
        }
    }

    /**
     * called when the user dismiss the asr key dialog -> we recreate the engine and
     * check if we can enable the asr.
     * @param dialog dialog that was dismissed
     */
    @Override
    public void onDialogDismiss(DialogFragment dialog) {
        enableASRView();
    }

}