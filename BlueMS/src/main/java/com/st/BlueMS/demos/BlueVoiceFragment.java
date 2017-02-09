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

package com.st.BlueMS.demos;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.DemoWithNetFragment;
import com.st.BlueMS.demos.util.bluevoice.AsrAsyncRequest;
import com.st.BlueMS.demos.util.bluevoice.AudioBuffer;
import com.st.BlueMS.demos.util.bluevoice.GoogleAsrKey;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.BVAudioSyncManager;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.util.ArrayList;
import java.util.Locale;

@DemoDescriptionAnnotation(name="BlueVoice",iconRes= R.drawable.ic_bluetooth_audio,
        requareAll = {FeatureAudioADPCM.class,FeatureAudioADPCMSync.class})
public class BlueVoiceFragment extends DemoWithNetFragment implements AsrAsyncRequest.AsrAsyncRequestCallback {

    private static final String TAG = BlueVoiceFragment.class.getCanonicalName();

    private static final int AUDIO_STREAM = AudioManager.STREAM_MUSIC;

    private static final String VOLUME_LEVEL = BlueVoiceFragment.class.getCanonicalName()+"" +
            ".VOLUME_LEVEL";
    private static final String IS_MUTE = BlueVoiceFragment.class.getCanonicalName()+"" +
            ".IS_MUTE";
    private static final String ASR_RESULTS = BlueVoiceFragment.class.getCanonicalName()+"" +
            ".ASR_RESULTS";

    private static final int AUDIO_SAMPLING_FREQ = 8000;
    private static final int MAX_RECORDING_TIME_S = 5;


    /////////////////////////////////////////// AUDIO //////////////////////////////////////////////
    /**
     * feature where we read  the audio values
     */
    private FeatureAudioADPCM  mAudio;
    private AudioManager mAudioManager;
    private AudioTrack mAudioTrack;
    private GoogleAsrKey mAsrKey;
    private boolean mIsRecording;
    private AudioBuffer mRecordedAudio;
    private AsrAsyncRequest mAsrService;
    private BVAudioSyncManager mBVAudioSyncManager = new BVAudioSyncManager();

    /**
     * listener for the audio feature, it will updates the audio values and if playback is active,
     * it write this data in the AudioTrack {@code audioTrack}.
     */
    private final Feature.FeatureListener mAudioListener = new Feature.FeatureListener() {

        @Override
        public void onUpdate(final Feature f, final Feature.Sample sample) {
            short audioSample[] = FeatureAudioADPCM.getAudio(sample);

            if(mIsRecording){
                final int nRecordedSample = mRecordedAudio.append(audioSample);
                if(mRecordedAudio.isFull()){
                    sendAsrRequest();
                }
                updateGui(new Runnable() {
                    @Override
                    public void run() {
                        mRecordBar.setProgress(nRecordedSample);
                    }
                });
            }else{
                playAudio(audioSample);
            }
        }

    };

    /////////////////////////////////////////// AUDIO SYNC /////////////////////////////////////////
    /**
     * feature where we read the audio sync values
     */
    private FeatureAudioADPCMSync  mAudioSync;

    /**
     * listener for the audioSync feature, it will update the synchronism values
     */
    private final Feature.FeatureListener mAudioSyncListener = new Feature.FeatureListener() {
        @Override
        public void onUpdate(Feature f, final Feature.Sample sample) {
            if(mBVAudioSyncManager!=null){
                mBVAudioSyncManager.setSyncParams(sample);
            }
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////


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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAudioTrack= new AudioTrack(AudioManager.STREAM_MUSIC, AUDIO_SAMPLING_FREQ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                FeatureAudioADPCM.AUDIO_PACKAGE_SIZE,
                AudioTrack.MODE_STREAM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_bluevoice2, container, false);

        mAudioManager = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);

        mVolumeBar =(SeekBar) mRootView.findViewById(R.id.volumeValue);
        setUpVolumeBar(mAudioManager.getStreamMaxVolume(AUDIO_STREAM));

        mMuteButton = new ManageMuteButton((ImageButton) mRootView.findViewById(R.id.muteButton));

        //mCodecNameValue = (TextView) mRootView.findViewById(R.id.codecValue);

        TextView mSamplingRateValue = (TextView) mRootView.findViewById(R.id.samplingRateValue);
        mSamplingRateValue.setText(String.format(Locale.getDefault(),"%d kHz", AUDIO_SAMPLING_FREQ/1000));

        mAsrStatus = (TextView) mRootView.findViewById(R.id.asrStatusValue);

        mAsrView = mRootView.findViewById(R.id.card_view_asrResults);
        mAsrResultListView = (AbsListView) mRootView.findViewById(R.id.asrResults);
        setupResultListView();

        mRecButton = (ImageButton) mRootView.findViewById(R.id.recordButton);
        setupRecordButton();

        mRecordBar = (ProgressBar) mRootView.findViewById(R.id.recordTimeValue);
        mRecordBar.setIndeterminate(false);
        mRecordBarText = (TextView) mRootView.findViewById(R.id.recordedTime);
        mRequestStatus = (TextView) mRootView.findViewById(R.id.requestStatus);

        mAsrSnackbar = Snackbar.make(mRootView, R.string.loadAsrKey,Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.BlueVoice_addKeyButton, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildAskAsrKeyDialog().show();
            }
        });

        if(savedInstanceState!=null){
            restoreGuiStatus(savedInstanceState);
        }

        return mRootView;
    }

    private void setupResultListView() {
        mAsrResultsAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,mAsrResults);
        mAsrResultListView.setAdapter(mAsrResultsAdapter);
        mAsrResultListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                String res = mAsrResults.remove(i);
                mAsrResultsAdapter.notifyDataSetChanged();
                return !res.isEmpty();
            }
        });
    }

    private void setupRecordButton() {
        mRecButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN && !mIsRecording){
                    mRecordedAudio = new AudioBuffer(AUDIO_SAMPLING_FREQ,MAX_RECORDING_TIME_S);
                    mRecordBar.setMax(mRecordedAudio.getBufferLength());
                    mRequestStatus.setText(R.string.blueVoice_recording);
                    mRecordBar.setVisibility(View.VISIBLE);
                    mRecordBarText.setVisibility(View.VISIBLE);
                    mIsRecording=true;

                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP && mIsRecording){
                    sendAsrRequest();
                    return true;
                }
                return false;
            }
        });
    }

    void sendAsrRequest(){
        mIsRecording=false;
        mAsrService.sendRequest(mRecordedAudio);
        mRecordedAudio =null;
        updateGui(new Runnable() {
            @Override
            public void run() {
                mRecordBar.setVisibility(View.GONE);
                mRecordBarText.setVisibility(View.GONE);
                mRequestStatus.setText(R.string.blueVoice_sendRequest);
                mRecordBar.setProgress(0);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        mAudioTrack.play();
    }

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

        if(savedInstanceState.containsKey(ASR_RESULTS)){
            ArrayList<String> oldResult=savedInstanceState.getStringArrayList(ASR_RESULTS);
            mAsrResultsAdapter.addAll(oldResult);
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
        inflater.inflate(R.menu.menu_bluevoice_feature_demo, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.showASRDialog){
            buildAskAsrKeyDialog().show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mAudio = node.getFeature(FeatureAudioADPCM.class);
        mAudioSync = node.getFeature(FeatureAudioADPCMSync.class);
        if(mAudio!=null && mAudioSync!=null) {
            mAudio.addFeatureListener(mAudioListener);
            mBVAudioSyncManager.reinitResetFlag();
            mAudio.setAudioSyncManager(mBVAudioSyncManager);
            node.enableNotification(mAudio);

            mAudioSync.addFeatureListener(mAudioSyncListener);
            node.enableNotification(mAudioSync);
            enableASR();
        }//if

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
            node.disableNotification(mAudio);
        }
        if(mAudioSync!=null) {
            mAudioSync.removeFeatureListener(mAudioSyncListener);
            node.disableNotification(mAudioSync);
        }

        if(mAsrSnackbar!=null)
            mAsrSnackbar.dismiss();

        updateGui(new Runnable() {
            @Override
            public void run() {
                disableASR();
            }
        });

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
        SharedPreferences pref = getActivity().getSharedPreferences(TAG,Context.MODE_PRIVATE);
        mAsrKey = GoogleAsrKey.loadKey(pref);
        if(mAsrKey==null){
            mAsrSnackbar.show();
            return;
        }
        mAsrService = new AsrAsyncRequest(mAsrKey,this);
        updateGui(new Runnable() {
            @Override
            public void run() {
                //we are online and we have the asr key
                mAsrView.setVisibility(View.VISIBLE);
                mRequestStatus.setVisibility(View.VISIBLE);
                mRecButton.setVisibility(View.VISIBLE);
                mAsrStatus.setText(R.string.asr_enabled);
            }
        });

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

    private Dialog buildAskAsrKeyDialog(){
        final Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.blueVoice_asr_key);

        final EditText input = new EditText(context);
        if(mAsrKey!=null)
            input.setText(mAsrKey.getKey());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setCancelable(false);

        builder.setPositiveButton(R.string.dialogConfirm, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    mAsrKey = new GoogleAsrKey(input.getText());
                }catch (IllegalArgumentException e){
                    Snackbar mInvalidKeySnackbar = Snackbar.make(mRootView, context.getString(R.string.asrInvalidKey)+e.getMessage(), Snackbar.LENGTH_LONG);
                    mInvalidKeySnackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            if(!asrIsEnabled()) {
                                enableASR();
                            }
                        }
                    });
                    mInvalidKeySnackbar.show();
                    return;
                }
                mAsrKey.store(context.getSharedPreferences(TAG,Context.MODE_PRIVATE));
                Snackbar.make(mRootView, R.string.asrKeyInserted, Snackbar.LENGTH_LONG).show();
                if(!asrIsEnabled()) {
                    enableASR();
                }
            }
        });
        builder.setNegativeButton(R.string.dialogBack, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!asrIsEnabled()) {
                    enableASR();
                }
                dialog.cancel();
            }
        });

        return builder.create();
    }

    private void disableASR() {
        mAsrView.setVisibility(View.GONE);
        mRequestStatus.setVisibility(View.GONE);
        mRecButton.setVisibility(View.GONE);
        mAsrStatus.setText(R.string.asr_disabled);
    }

    private boolean asrIsEnabled(){
        return mRecButton.getVisibility()==View.VISIBLE;
    }

    @Override
    public void onRequestSend() {
        mRequestStatus.setText(R.string.blueVoice_waitForAsr);
    }

    @Override
    public void onRequestRespond(@AsrAsyncRequest.Status int status, String response) {
        Log.d(TAG, "onRequestRespond: Status:"+status+" Resp:"+response);
        switch (status) {
            case AsrAsyncRequest.IO_CONNECTION_ERROR:
                mRequestStatus.setText(R.string.blueVoice_ioError);
                break;
            case AsrAsyncRequest.NOT_RECOGNIZED:
                mRequestStatus.setText(R.string.blueVoice_notRecognized);
                break;
            case AsrAsyncRequest.NO_ERROR:
                //mAsrResultsAdapter.add(response);
                mAsrResultsAdapter.insert(response,0);
                mRequestStatus.setText("");
                break;
            case AsrAsyncRequest.REQUEST_FAILED:
                mRequestStatus.setText(R.string.blueVoice_requestFailed);
                break;
            case AsrAsyncRequest.RESPONSE_ERROR:
                mRequestStatus.setText(R.string.blueVoice_responseError);
                break;
        }
    }

    private class ManageMuteButton implements View.OnClickListener{

        private ImageButton mMuteButton;
        private boolean mIsMute;

        public ManageMuteButton(ImageButton button){
            mIsMute=false;
            mMuteButton = button;
            button.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            changeState();
        }

        public boolean isMute(){return mIsMute;}

        public boolean changeState(){
            mIsMute=!mIsMute;
            if(mIsMute)
                muteAudio();
            else
                unMuteAudio();
            return mIsMute;
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