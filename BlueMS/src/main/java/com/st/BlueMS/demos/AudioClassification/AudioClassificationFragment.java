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

package com.st.BlueMS.demos.AudioClassification;

import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAudioClassification;
import com.st.BlueSTSDK.Features.FeatureAudioClassification.AudioClass;
import com.st.BlueSTSDK.Node;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Demo showing the output of the Audio scene classification NN library.
 */
@DemoDescriptionAnnotation(name="Audio Classification",
        requareAll = {FeatureAudioClassification.class},
        iconRes = R.drawable.ic_bluetooth_audio)
public class AudioClassificationFragment extends BaseDemoFragment {

    private static final String CURRENT_ALGORITHM = AudioClassificationFragment.class.getName()+".CURRENT_ALGORITHM";
    private static final String CURRENT_ACTION = AudioClassificationFragment.class.getName()+".CURRENT_ACTION";

    /**
     * feature where we will read the position
     */
    private Feature mAudioRecognition;

    private View mWaitView;
    private AudioView mSceneClassificationView;    // Scene Classification view
    private AudioView mBabyCryingView;             // Baby Crying view

    private AudioView mAllView[];

    private int mCurrentAlgorithm = 0;
    private AudioClass mCurrentAudioClass = AudioClass.UNKNOWN;

    private AudioView getViewFromAlgoId(int algorithmId){
        switch (algorithmId){
            case 0:
                return  mSceneClassificationView;
            case 1:
                return  mBabyCryingView;
        }
        return null;
    }


    private void showActivity(int algorithmId, AudioClass type){
        AudioView activeView = getViewFromAlgoId(algorithmId);
        mWaitView.setVisibility(View.GONE);
        for (AudioView view : mAllView){
            if(activeView == view){
                view.setVisibility(View.VISIBLE);
                view.setActivity(type);
            }else{
                view.setVisibility(View.GONE);
            }
        }
        mCurrentAudioClass = type;
        mCurrentAlgorithm = algorithmId;
    }


    /**
     * listener that will change the alpha to the selected image
     */
    private Feature.FeatureListener mAudioRecognitionListener = (f, sample) -> {
        final AudioClass status = FeatureAudioClassification.getAudioClass(sample);
        final int algoId = FeatureAudioClassification.getAlgorithmType(sample);

        updateGui(() -> {
            showActivity(algoId, status);
        });
    };

    public AudioClassificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_audio_classification, container, false);
        mWaitView = root.findViewById(R.id.audio_waitingData);
        mSceneClassificationView = root.findViewById(R.id.audio_view_scene_classification);
        mBabyCryingView = root.findViewById(R.id.audio_view_baby_crying);

        mAllView = new AudioView[] { mSceneClassificationView, mBabyCryingView };

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(CURRENT_ALGORITHM)&&
                savedInstanceState.containsKey(CURRENT_ACTION)) {
            mCurrentAlgorithm = savedInstanceState.getInt(CURRENT_ALGORITHM);
            mCurrentAudioClass = (AudioClass) savedInstanceState.getSerializable(CURRENT_ACTION);
            showActivity(mCurrentAlgorithm, mCurrentAudioClass);
        }
        return root;
    }

    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(CURRENT_ALGORITHM, mCurrentAlgorithm);
        savedInstanceState.putSerializable(CURRENT_ACTION, mCurrentAudioClass);
    }//onSaveInstanceState

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mAudioRecognition = node.getFeature(FeatureAudioClassification.class);
        if (mAudioRecognition != null) {
            mAudioRecognition.addFeatureListener(mAudioRecognitionListener);
            node.enableNotification(mAudioRecognition);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mAudioRecognition != null) {
            mAudioRecognition.removeFeatureListener(mAudioRecognitionListener);
            node.disableNotification(mAudioRecognition);
        }
    }
}
