/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.BlueMS.demos.fitnessActivity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Features.FeatureFitnessActivity;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(
        iconRes = R.drawable.fitness_demo_icon,
        name = "Fitness Activity",
        requareAll = {FeatureFitnessActivity.class})
public class FitnessActivityFragment extends DemoFragment {

    public FitnessActivityFragment() {
        // Required empty public constructor
    }

    private AbsSpinner mActivitySelector;
    private ImageView mResultIcon;
    private TextView mResultLabel;

    private AnimatorSet mPulseAnim;

    private FitnessActivityViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_fitness_activity, container, false);

        mActivitySelector = root.findViewById(R.id.fitnessActivity_selector);
        mResultIcon = root.findViewById(R.id.fitnessActivity_resultIcon);
        mResultLabel = root.findViewById(R.id.fitnessActivity_resultLabel);

        mPulseAnim = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(),
                R.animator.pulse);
        mPulseAnim.setTarget(mResultIcon);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders
                .of(this)
                .get(FitnessActivityViewModel.class);

        setUpActivitySelector(mActivitySelector,mViewModel);
        setUpActivityCounter(mViewModel,mResultLabel);
        setUpActivityIcon(mViewModel,mResultIcon);
    }

    private void setUpActivityIcon(FitnessActivityViewModel viewModel, ImageView icon) {
        viewModel.getCurrentActivity().observe(this, activityType -> {
            if(activityType == null)
                return;
            icon.setImageResource(activityTypeImage(activityType));
        });

        viewModel.getCurrentCounter().observe(this, integer -> {
            if(integer == null)
                return;
            if(!mPulseAnim.isRunning()){
                mPulseAnim.start();
            }
        });

    }

    private void setUpActivityCounter(FitnessActivityViewModel viewModel, TextView label) {
        viewModel.getCurrentCounter().observe(this, counter -> {
            if(counter == null || counter<0){
                return;
            }
            FeatureFitnessActivity.ActivityType type = mViewModel.getCurrentActivity().getValue();
            if(type == null)
                return;
            String acitvity = getString(activityTypeString(type));
            String counterStr = getString(R.string.fitnessAct_counterLabel_format,counter,acitvity);
            label.setText(counterStr);
        });
    }

    private @Nullable
    FeatureFitnessActivity.ActivityType activityTypeFromSpinnerIndex(int index){
        for(FeatureFitnessActivity.ActivityType activityType :
                FeatureFitnessActivity.ActivityType.values()){
            if(activityType.id == index){
                return activityType;
            }
        }
        return null;
    }

    private void setUpActivitySelector(AbsSpinner selector, FitnessActivityViewModel viewModel){

        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.fitnessAct_activityValues);
        selector.setAdapter(adapter);
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FeatureFitnessActivity.ActivityType activityType = activityTypeFromSpinnerIndex(i);
                if(activityType!=null) {
                    viewModel.setActivity(activityType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getCurrentActivity().observe(this, algorithmType -> {
            if(algorithmType==null)
                return;
            int index = algorithmType.id;
            selector.setSelection(index);
        });
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mViewModel.startListenDataFrom(node);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        mViewModel.stopListenDataFrom(node);
    }


    private static @StringRes
    int activityTypeString(FeatureFitnessActivity.ActivityType type){

        switch (type){
            case NO_ACTIVITY:
                return R.string.fitnessAct_activity_none;
            case BICEP_CURL:
                return R.string.fitnessAct_activity_bicepsCurl;
            case SQUAT:
                return R.string.fitnessAct_activity_squat;
            case PUSH_UP:
                return R.string.fitnessAct_activity_pushUp;
        }
        return R.string.fitnessAct_activity_none;
    }

    private static @DrawableRes
    int activityTypeImage(FeatureFitnessActivity.ActivityType type){

        switch (type){
            case NO_ACTIVITY:
                return R.drawable.motion_algo_unknown;
            case BICEP_CURL:
                return R.drawable.fitness_bicipet_curl;
            case SQUAT:
                return R.drawable.fitness_squat;
            case PUSH_UP:
                return R.drawable.fitness_push_up;
        }
        return R.drawable.motion_algo_unknown;
    }
}
