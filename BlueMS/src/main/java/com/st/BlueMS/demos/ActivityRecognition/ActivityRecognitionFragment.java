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

package com.st.BlueMS.demos.ActivityRecognition;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Features.FeatureActivity.ActivityType;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

/**
 * Fragment that show the 6 possible activity and change the alpha of the selected one
 */
@DemoDescriptionAnnotation(name="Activity Recognition", requareAll = {FeatureActivity.class},
    iconRes = R.drawable.activity_demo_icon)
public class ActivityRecognitionFragment extends BaseDemoFragment {

    private static final String CURRENT_ALGORITHM = ActivityRecognitionFragment.class.getName()+".CURRENT_ALGORITHM";
    private static final String CURRENT_ACTION = ActivityRecognitionFragment.class.getName()+".CURRENT_ACTION";

    /**
     * feature where we will read the activity type
     */
    private Feature mActivityRecognition;

    private View mWaitView; // dummy view to display until the first data isn't arrived

    private ActivityView mMotionARView; // default view
    private ActivityView mGMPView; // GMP activity recognition
    private ActivityView mIGNView; // IGN activity recognition
    private ActivityView mHAR_MLCView; // activity recognition from mlc
    private ActivityView mAPD_MLCView; // adult presence recognition from mlc

    private ActivityView mAllView[];

    private int mCurrentAlgorithm=0;
    private ActivityType mCurrentActivity=ActivityType.NO_ACTIVITY;

    /**
     * return the view where show the result
     * @param algorithmId id of the algorithm
     * @return view where display the activity
     */
    private ActivityView getViewFromAlgoId(int algorithmId){
        switch (algorithmId){
            case 0:
                return  mMotionARView;
            case 1:
                return  mGMPView;
            case 2:
                return mIGNView;
            case 3:
                return mHAR_MLCView;
            case 4:
                return mAPD_MLCView;
        }
        return null;
    }


    /**
     * hide all the view except the view associated with the algorithmId and display the activity in
     * that view
     * @param algorithmId algorithm that generates the activity
     * @param type activity to display
     */
    private void showActivity(int algorithmId, ActivityType type){
        ActivityView activeView = getViewFromAlgoId(algorithmId);
        mWaitView.setVisibility(View.GONE);
        for (ActivityView view : mAllView){
            if(activeView == view){
                view.setVisibility(View.VISIBLE);
                view.setActivity(type);
            }else{
                view.setVisibility(View.GONE);
            }
        }
        mCurrentActivity = type;
        mCurrentAlgorithm = algorithmId;
    }

    /**
     * listener that will change the alpha to the selected image
     */
    private Feature.FeatureListener mActivityListener = (f, sample) -> {
        final ActivityType status = FeatureActivity.getActivityStatus(sample);
        final int algoId = FeatureActivity.getAlgorithmType(sample);

        updateGui(() -> {
            showActivity(algoId, status);
        });
    };

    public ActivityRecognitionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_activity_recognition, container, false);
        mWaitView = root.findViewById(R.id.activity_waitingData);
        mMotionARView = root.findViewById(R.id.activity_view_motionAR);
        mGMPView = root.findViewById(R.id.activity_view_GMP);
        mIGNView = root.findViewById(R.id.activity_view_IGN);
        mHAR_MLCView = root.findViewById(R.id.activity_view_HAR_MLC);
        mAPD_MLCView = root.findViewById(R.id.activity_view_APD_MLC);

        mAllView = new ActivityView[]{mMotionARView,mGMPView,mIGNView,mHAR_MLCView,mAPD_MLCView};

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(CURRENT_ALGORITHM)&&
                savedInstanceState.containsKey(CURRENT_ACTION)) {
            mCurrentAlgorithm = savedInstanceState.getInt(CURRENT_ALGORITHM);
            mCurrentActivity = (ActivityType) savedInstanceState.getSerializable(CURRENT_ACTION);
            showActivity(mCurrentAlgorithm,mCurrentActivity);
        }
        return root;
    }


    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(CURRENT_ALGORITHM,mCurrentAlgorithm);
        savedInstanceState.putSerializable(CURRENT_ACTION,mCurrentActivity);
    }//onSaveInstanceState

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mActivityRecognition = node.getFeature(FeatureActivity.class);
        if (mActivityRecognition != null) {
            mActivityRecognition.addFeatureListener(mActivityListener);
            node.enableNotification(mActivityRecognition);
            //we have a notification only if the state change -> we force a read for have the
            //initial state
            node.readFeature(mActivityRecognition);
            showActivityToast(R.string.activityRecognition_started);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        if (mActivityRecognition != null) {
            mActivityRecognition.removeFeatureListener(mActivityListener);
            node.disableNotification(mActivityRecognition);
        }
    }

}
