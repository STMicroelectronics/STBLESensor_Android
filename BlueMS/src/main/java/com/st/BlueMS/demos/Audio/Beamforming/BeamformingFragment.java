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

package com.st.BlueMS.demos.Audio.Beamforming;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.Utils.AudioRecorder;
import com.st.BlueMS.demos.Audio.Utils.SquareImageView;
import com.st.BlueMS.demos.Audio.Utils.WaveformView;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Audio.FeatureAudio;
import com.st.BlueSTSDK.Features.Audio.FeatureAudioConf;
import com.st.BlueSTSDK.Features.FeatureBeamforming;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Features.Audio.AudioCodecManager;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

import java.util.HashMap;
import java.util.Map;

import static com.st.BlueSTSDK.Features.FeatureBeamforming.Direction;

/**
 * Demo streaming the audio from the node.
 *
 * if a connection is available and the api key is set it can use the Google speech api to translate
 * audio to text
 */
@DemoDescriptionAnnotation(name="Beamforming",iconRes= R.drawable.beamforming_icon,
        requareAll = {FeatureAudio.class,FeatureAudioConf.class, FeatureBeamforming.class})
public class BeamformingFragment extends BaseDemoFragment {

    private static final String TAG = BeamformingFragment.class.getCanonicalName();
    private static final String CURRENT_DIR = TAG+".CURRENT_BF_DIRECTION";
    private static final int AUDIO_SAMPLING_FREQ = 8000;
    private static final short AUDIO_CHANNELS = 1;

    private static final @FeatureBeamforming.Direction int DEFAULT_DIRECTION = Direction.RIGHT;

    private WaveformView mWaveformView;

    //NOTE beamforming /////////////////////////////////////////////////////////////////////////////

    private @Direction int mCurrentDirId=DEFAULT_DIRECTION;
    private SquareImageView mBoard;
    private CompoundButton mTopButton;
    private CompoundButton mTopRightButton;
    private CompoundButton mRightButton;
    private CompoundButton mBottomRightButton;
    private CompoundButton mBottomButton;
    private CompoundButton mBottomLeftButton;
    private CompoundButton mLeftButton;
    private CompoundButton mTopLeftButton;

    private Map<CompoundButton,Integer> mButtonToDirection = new HashMap<>(8);


    private CompoundButton.OnCheckedChangeListener mOnDirectionSelected = (compoundButton, isSelected) -> {
        if(!isSelected)
            return;

        @Direction int buttonDir = mButtonToDirection.get(compoundButton);
        setBeamFormingButton(buttonDir);
    };
    //NOTE /////////////////////////////////////////////////////////////////////////////////////////

    private AudioRecorder mAudioRecorder;

    /////////////////////////////////////////// AUDIO //////////////////////////////////////////////
    /**
     * feature where we read  the audio values
     */
    private FeatureAudio mAudio;
    private AudioTrack mAudioTrack;
    private AudioCodecManager mBVAudioSyncManager;

    /**
     * listener for the audio feature, it will updates the audio values and if playback is active,
     * it write this data in the AudioTrack {@code audioTrack}.
     */
    private final Feature.FeatureListener mAudioListener = (f, sample) -> {
        short[] audioSample = ((FeatureAudio)f).getAudio(sample);
        playAudio(audioSample);
    };

    private final Feature.FeatureListener mAudioListenerRec = new Feature.FeatureListener() {

        @Override
        public void onUpdate(@NonNull final Feature f, @NonNull final Feature.Sample sample) {
            if(mAudioRecorder != null && mAudioRecorder.isRecording()) {
                short[] audioSample = ((FeatureAudio)f).getAudio(sample);
                mAudioRecorder.writeSample(audioSample);
            }
        }

    };

    private final Feature.FeatureListener mUpdatePlot = new Feature.FeatureListener() {
        @Override
        public void onUpdate(@NonNull Feature f, @NonNull Feature.Sample sample) {
            short[] audioSample = ((FeatureAudio)f).getAudio(sample);
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
        if(mBVAudioSyncManager!=null){
            mBVAudioSyncManager.updateParams(sample);
        }
    };
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////// AUDIO SYNC /////////////////////////////////////////
    private FeatureBeamforming mAudioBeamforming;
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initAudioTrack(AUDIO_SAMPLING_FREQ,AUDIO_CHANNELS);
    }

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
    }

    private void initializeAudioDump(FeatureAudio fa) {
        LogFeatureActivity activity = (LogFeatureActivity) requireActivity();
        mAudioRecorder = new AudioRecorder(activity, fa != null ? fa.getName() : "Audio");
        activity.invalidateOptionsMenu();
    }

    private void loadDirectionButton(View root){
        mTopButton = root.findViewById(R.id.radioBFdirTop);
        mTopRightButton = root.findViewById(R.id.radioBFdirTopRight);
        mRightButton = root.findViewById(R.id.radioBFdirRight);
        mBottomRightButton = root.findViewById(R.id.radioBFdirBottomRight);
        mBottomButton = root.findViewById(R.id.radioBFdirBottom);
        mBottomLeftButton = root.findViewById(R.id.radioBFdirBottomLeft);
        mLeftButton = root.findViewById(R.id.radioBFdirLeft);
        mTopLeftButton = root.findViewById(R.id.radioBFdirTopLeft);
    }

    private void setupOnCheckedDirListener(){
        mTopButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mTopRightButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mRightButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mBottomRightButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mBottomButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mBottomLeftButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mLeftButton.setOnCheckedChangeListener(mOnDirectionSelected);
        mTopLeftButton.setOnCheckedChangeListener(mOnDirectionSelected);
    }

    private void buildButtonToDirectionMap(){
        mButtonToDirection.clear();
        mButtonToDirection.put(mTopButton, Direction.TOP);
        mButtonToDirection.put(mTopRightButton,Direction.TOP_RIGHT);
        mButtonToDirection.put(mRightButton,Direction.RIGHT);
        mButtonToDirection.put(mBottomRightButton,Direction.BOTTOM_RIGHT);
        mButtonToDirection.put(mBottomButton,Direction.BOTTOM);
        mButtonToDirection.put(mBottomLeftButton,Direction.BOTTOM_LEFT);
        mButtonToDirection.put(mLeftButton,Direction.LEFT);
        mButtonToDirection.put(mTopLeftButton,Direction.TOP_LEFT);
    }

    private void show2MicConfiguration(){
        mTopButton.setVisibility(View.GONE);
        mTopRightButton.setVisibility(View.GONE);
        mRightButton.setVisibility(View.VISIBLE);
        mBottomRightButton.setVisibility(View.GONE);

        mBottomButton.setVisibility(View.GONE);
        mBottomLeftButton.setVisibility(View.GONE);
        mLeftButton.setVisibility(View.VISIBLE);
        mTopLeftButton.setVisibility(View.GONE);
    }

    private void show4MicConfiguration(){
        mTopButton.setVisibility(View.VISIBLE);
        mTopRightButton.setVisibility(View.VISIBLE);
        mRightButton.setVisibility(View.VISIBLE);
        mBottomRightButton.setVisibility(View.VISIBLE);
        mBottomButton.setVisibility(View.VISIBLE);
        mBottomLeftButton.setVisibility(View.VISIBLE);
        mLeftButton.setVisibility(View.VISIBLE);
        mTopLeftButton.setVisibility(View.VISIBLE);
    }

    private void setBoardType(Node.Type type){
        if(mBoard.getDrawable()!=null)
            return;
        switch (type){
            case BLUE_COIN:
                mBoard.setImageResource(R.drawable.ic_board_bluecoin_bg);
                show4MicConfiguration();
                break;
            case NUCLEO:
                mBoard.setImageResource(R.drawable.ic_board_nucleo_bg);
                show2MicConfiguration();
                break;
            default:
                mBoard.setImageResource(R.drawable.mic_on);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_beamforming, container, false);

        mWaveformView = view.findViewById(R.id.blueVoice_waveform_view);

        loadDirectionButton(view);
        buildButtonToDirectionMap();

        mBoard = view.findViewById(R.id.beamforming_board_image);

        Node n = getNode();
        if(n!=null)
            setBoardType(n.getType());

        //when the view is displayed
        view.post(this::alignDirectionButtonOnCircle);

        if(savedInstanceState!=null){
            @Direction int temp = savedInstanceState.getInt(CURRENT_DIR,DEFAULT_DIRECTION);
            mCurrentDirId = temp;
        }

        return view;
    }

    private CompoundButton getButtonForDirection(int direction){
        for( Map.Entry<CompoundButton,Integer> pair : mButtonToDirection.entrySet()){
            if(pair.getValue()==direction)
                return pair.getKey();
        }
        return null;
    }

    private void alignDirectionButtonOnCircle(){
        //the image is squared
        float imageSize = mBoard.getHeight();
        //the radio button is square
        float buttonHalfSize =  mRightButton.getWidth()/2;
        float r = (mRightButton.getX()-mLeftButton.getX())/2;

        int margin2 = (int)((imageSize / 2) - (r / Math.sqrt(2)) - buttonHalfSize);
        RelativeLayout.LayoutParams rel_btn;

        rel_btn = (RelativeLayout.LayoutParams) mTopRightButton.getLayoutParams();
        rel_btn.topMargin = margin2;
        rel_btn.setMarginEnd(margin2);
        //update the parameters
        mTopRightButton.setLayoutParams(rel_btn);

        rel_btn = (RelativeLayout.LayoutParams) mBottomRightButton.getLayoutParams();
        rel_btn.bottomMargin = margin2;
        rel_btn.setMarginEnd( margin2);
        //update the parameters
        mBottomRightButton.setLayoutParams(rel_btn);

        rel_btn = (RelativeLayout.LayoutParams) mBottomLeftButton.getLayoutParams();
        rel_btn.bottomMargin = margin2;
        rel_btn.setMarginStart( margin2);
        //update the parameters
        mBottomLeftButton.setLayoutParams(rel_btn);

        rel_btn = (RelativeLayout.LayoutParams) mTopLeftButton.getLayoutParams();
        rel_btn.topMargin = margin2;
        rel_btn.setMarginStart( margin2);
        //update the parameters
        mTopLeftButton.setLayoutParams(rel_btn);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAudioTrack.play();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAudioRecorder!=null && mAudioRecorder.isRecording())
            mAudioRecorder.stopRec();
    }

    @Override
    public void onStop(){
        super.onStop();
        stopAudioTrack();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CURRENT_DIR,mCurrentDirId);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.findItem(R.id.startLog).setVisible(false);
        if(mAudioRecorder!=null)
            mAudioRecorder.registerRecordMenu(menu,inflater);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        setBoardType(node.getType());
        mAudio = node.getFeature(FeatureAudio.class);
        mAudioSync = node.getFeature(FeatureAudioConf.class);
        mAudioBeamforming = node.getFeature(FeatureBeamforming.class);
        if(mAudio!=null && mAudioSync!=null && mAudioBeamforming!=null) {
            mBVAudioSyncManager = mAudioSync.instantiateManager();
            initializeAudioDump(mAudio);
            mAudio.addFeatureListener(mAudioListener);
            mAudio.addFeatureListener(mUpdatePlot);
            mWaveformView.startPlotting();
            mBVAudioSyncManager.reinit();
            mAudio.setAudioCodecManager(mBVAudioSyncManager);
            node.enableNotification(mAudio);

            mAudioSync.addFeatureListener(mAudioSyncListener);
            node.enableNotification(mAudioSync);

            node.enableNotification(mAudioBeamforming);
            mAudioBeamforming.enableBeamForming(true);
            mAudioBeamforming.useStrongBeamformingAlgorithm(true);

            setBeamFormingButton(mCurrentDirId);

            mAudio.addFeatureListener(mAudioListenerRec);
        }//if

        setupOnCheckedDirListener();

    }//enableNeededNotification

    private void setBeamFormingButton(@Direction int newDirection) {
        CompoundButton selectedButton = getButtonForDirection(newDirection);

        if(selectedButton == null)
            return;
        if(mAudioBeamforming!=null) {
            mCurrentDirId=newDirection;
            mAudioBeamforming.setBeamFormingDirection(mCurrentDirId);
            selectedButton.setChecked(true);
            deselectAllButtonDifferentFrom(selectedButton);
        }

    }

    private void deselectAllButtonDifferentFrom(CompoundButton selected) {
        for(CompoundButton button : mButtonToDirection.keySet()){
            if(button != selected) {
                button.setChecked(false);
            }
        }
    }

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

            mWaveformView.stopPlotting();
            node.disableNotification(mAudio);
        }
        if(mAudioSync!=null) {
            mAudioSync.removeFeatureListener(mAudioSyncListener);
            node.disableNotification(mAudioSync);
        }

        if(mAudioBeamforming!=null){
            node.disableNotification(mAudioBeamforming);
            mAudioBeamforming.enableBeamForming(false);
        }
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
            mAudioTrack.write(sample, 0, sample.length);
        }
    }

}