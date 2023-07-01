/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.fitness;

import static com.st.core.ConstKt.ARG_NODE_ID;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivityType;
import com.st.ui.legacy.ArrayAdapterUtil;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FitnessActivityFragment extends Fragment {

    private AbsSpinner mActivitySelector;
    private ImageView mResultIcon;
    private TextView mResultLabel;

    private AnimatorSet mPulseAnim;

    private FitnessActivityViewModel mViewModel;
    private String nodeId;

    private static @StringRes
    int activityTypeString(FitnessActivityType type) {

        switch (type) {
            case NoActivity:
                return R.string.st_fitnessActivity_activity_none;
            case BicepCurl:
                return R.string.st_fitnessActivity_activity_bicepsCurl;
            case Squat:
                return R.string.st_fitnessActivity_activity_squat;
            case PushUp:
                return R.string.st_fitnessActivity_activity_pushUp;
        }
        return R.string.st_fitnessActivity_activity_none;
    }

    private static @DrawableRes
    int activityTypeImage(FitnessActivityType type) {

        switch (type) {
            case NoActivity:
                return R.drawable.fitness_unknown_activity_icon;
            case BicepCurl:
                return R.drawable.fitness_bicipet_curl_icon;
            case Squat:
                return R.drawable.fitness_squat_icon;
            case PushUp:
                return R.drawable.fitness_push_up_icon;
        }
        return R.drawable.fitness_unknown_activity_icon;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fitness_activity_fragment, container, false);

        // Read the nodeId from navigation arguments
        if (getArguments() != null) {
            nodeId = getArguments().getString(ARG_NODE_ID);
        }

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
        mViewModel = new ViewModelProvider(this).get(FitnessActivityViewModel.class);

        setUpActivitySelector(mActivitySelector, mViewModel);
        setUpActivityCounter(mViewModel, mResultLabel);
        setUpActivityIcon(mViewModel, mResultIcon);
    }

    private void setUpActivityIcon(FitnessActivityViewModel viewModel, ImageView icon) {
        viewModel.getCurrentActivity().observe(getViewLifecycleOwner(), activityType -> {
            if (activityType == null)
                return;
            icon.setImageResource(activityTypeImage(activityType));
        });

        viewModel.getCurrentCounter().observe(getViewLifecycleOwner(), integer -> {
            if (integer == null)
                return;
            if (!mPulseAnim.isRunning()) {
                mPulseAnim.start();
            }
        });

    }

    private void setUpActivityCounter(FitnessActivityViewModel viewModel, TextView label) {
        viewModel.getCurrentCounter().observe(getViewLifecycleOwner(), counter -> {
            if (counter == null || counter < 0) {
                return;
            }
            FitnessActivityType type = mViewModel.getCurrentActivity().getValue();
            if (type == null)
                return;
            String activityType = getString(activityTypeString(type));
            String counterStr = getString(R.string.st_fitnessActivity_common_counterLabelFormatter, counter, activityType);
            label.setText(counterStr);
        });
    }

    private @Nullable
    FitnessActivityType activityTypeFromSpinnerIndex(int index) {
        for (FitnessActivityType activityType : FitnessActivityType.values()) {
            if (activityType.ordinal() == index) {
                return activityType;
            }
        }
        return null;
    }

    private void setUpActivitySelector(AbsSpinner selector, FitnessActivityViewModel viewModel) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapterUtil.createAdapterFromArray(requireContext(), R.array.st_fitnessActivity_activities);
        selector.setAdapter(adapter);
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FitnessActivityType activityType = activityTypeFromSpinnerIndex(i);
                if (activityType != null) {
                    viewModel.setActivity(nodeId, activityType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        viewModel.getCurrentActivity().observe(getViewLifecycleOwner(), algorithmType -> {
            if (algorithmType == null)
                return;
            int index = algorithmType.ordinal();
            selector.setSelection(index);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start demo
        mViewModel.startDemo(nodeId = nodeId);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop demo
        mViewModel.stopDemo(nodeId = nodeId);
    }
}