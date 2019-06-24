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
package com.st.BlueMS.demos.motionAlgorithm;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.util.BaseDemoFragment;
import com.st.BlueSTSDK.Features.FeatureMotionAlgorithm;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

@DemoDescriptionAnnotation(
        iconRes = R.drawable.activity_demo_icon,
        name = "Motion Algorithms",
        requareAll = {FeatureMotionAlgorithm.class})
public class MotionAlgorithmFragment extends BaseDemoFragment {


    public MotionAlgorithmFragment() {
        // Required empty public constructor
    }

    private AbsSpinner mAlgoSelector;
    private ImageView mResultIcon;
    private TextView mResultLabel;

    private MotionAlgorithmViewModel mViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_motion_algorithms, container, false);

        mAlgoSelector = root.findViewById(R.id.motionAlgo_selector);
        mResultIcon = root.findViewById(R.id.motionAlgo_resultIcon);
        mResultLabel = root.findViewById(R.id.motionAlgo_resultLabel);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mViewModel = ViewModelProviders
                .of(this)
                .get(MotionAlgorithmViewModel.class);

        setUpAlgoSelector(mAlgoSelector,mViewModel);
        setupDesktopTypeListener(mViewModel,mResultLabel,mResultIcon);
        setupVerticalContextListener(mViewModel,mResultLabel,mResultIcon);
        setupPoseEstimationListener(mViewModel,mResultLabel,mResultIcon);
    }

    private void setupDesktopTypeListener(MotionAlgorithmViewModel viewModel, TextView label, ImageView icon) {
        viewModel.getDesktopType().observe(this, desktopType -> {
            if(desktopType == null){
                label.setText(desktopString(FeatureMotionAlgorithm.DesktopType.UNKNOWN));
                icon.setImageResource(desktopIcon(FeatureMotionAlgorithm.DesktopType.UNKNOWN));
            }else{
                label.setText(desktopString(desktopType));
                icon.setImageResource(desktopIcon(desktopType));
            }
        });
    }

    private static @DrawableRes int
    desktopIcon(FeatureMotionAlgorithm.DesktopType type){

        switch (type){
            case UNKNOWN:
                return R.drawable.motion_algo_unknown;
            case SITTING:
                return R.drawable.desktop_type_sitting;
            case STANDING:
                return R.drawable.desktop_type_standing;
        }
        return R.drawable.motion_algo_unknown;
    }

    private static @StringRes int desktopString(FeatureMotionAlgorithm.DesktopType type){

        switch (type){
            case UNKNOWN:
                return R.string.motionAlgo_unknown;
            case SITTING:
                return R.string.motionAlgo_desktop_sitting;
            case STANDING:
                return R.string.motionAlgo_desktop_standing;
        }
        return R.string.motionAlgo_unknown;
    }


    private void setupVerticalContextListener(MotionAlgorithmViewModel viewModel, TextView label, ImageView icon) {
        viewModel.getVerticalContext().observe(this, verticalContext -> {
            if(verticalContext == null){
                label.setText(verticalContextString(FeatureMotionAlgorithm.VerticalContext.UNKNOWN));
                icon.setImageResource(verticalContextIcon(FeatureMotionAlgorithm.VerticalContext.UNKNOWN));
            }else{
                label.setText(verticalContextString(verticalContext));
                icon.setImageResource(verticalContextIcon(verticalContext));
            }
        });
    }

    private static @DrawableRes int
    verticalContextIcon(FeatureMotionAlgorithm.VerticalContext type){

        switch (type){
            case UNKNOWN:
                return R.drawable.motion_algo_unknown;
            case FLOOR:
                return R.drawable.motion_algo_vertical_floor;
            case UP_DOWN:
                return R.drawable.motion_algo_vertical_updown;
            case STAIRS:
                return R.drawable.motion_algo_vertical_stairs;
            case ELEVATOR:
                return R.drawable.motion_algo_vertical_elevator;
            case ESCALATOR:
                return R.drawable.motion_algo_vertical_escalator;
        }
        return R.drawable.motion_algo_unknown;
    }

    private static @StringRes int
    verticalContextString(FeatureMotionAlgorithm.VerticalContext type){

        switch (type){
            case UNKNOWN:
                return R.string.motionAlgo_unknown;
            case FLOOR:
                return R.string.motionAlgo_vertical_floor;
            case UP_DOWN:
                return R.string.motionAlgo_vertical_upDown;
            case STAIRS:
                return R.string.motionAlgo_vertical_stairs;
            case ELEVATOR:
                return R.string.motionAlgo_vertical_elevator;
            case ESCALATOR:
                return R.string.motionAlgo_vertical_escalator;
        }
        return R.string.motionAlgo_unknown;
    }


    private void setupPoseEstimationListener(MotionAlgorithmViewModel viewModel, TextView label, ImageView icon) {
        viewModel.getPoseEstimation().observe(this, pose -> {
            if(pose == null){
                label.setText(poseContextString(FeatureMotionAlgorithm.Pose.UNKNOWN));
                icon.setImageResource(poseContextIcon(FeatureMotionAlgorithm.Pose.UNKNOWN));
            }else{
                label.setText(poseContextString(pose));
                icon.setImageResource(poseContextIcon(pose));
            }
        });
    }

    private static @DrawableRes int
    poseContextIcon(FeatureMotionAlgorithm.Pose type){
        switch (type){
            case UNKNOWN:
                return R.drawable.motion_algo_unknown;
            case SITTING:
                return R.drawable.motion_algo_pose_sitting;
            case STANDING:
                return R.drawable.motion_algo_pose_standing;
            case LYING_DOWN:
                return R.drawable.motion_algo_pose_lying_down;
        }
        return R.drawable.motion_algo_unknown;
    }

    private static @StringRes int
    poseContextString(FeatureMotionAlgorithm.Pose type){

        switch (type){
            case UNKNOWN:
                return R.string.motionAlgo_unknown;
            case SITTING:
                return R.string.motionAlgo_pose_sitting;
            case STANDING:
                return R.string.motionAlgo_pose_standing;
            case LYING_DOWN:
                return R.string.motionAlgo_pose_layingDown;
        }
        return R.string.motionAlgo_unknown;
    }

    private @Nullable
    FeatureMotionAlgorithm.AlgorithmType algorithmTypeFromSpinnerIndex(int index){
        for(FeatureMotionAlgorithm.AlgorithmType algoType :
                FeatureMotionAlgorithm.AlgorithmType.values()){
            if(algoType.id == index){
                return algoType;
            }
        }
        return null;
    }


    private void setUpAlgoSelector(AbsSpinner selector,MotionAlgorithmViewModel viewModel){

        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.motionAlgo_algoValues);
        selector.setAdapter(adapter);
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FeatureMotionAlgorithm.AlgorithmType algoType = algorithmTypeFromSpinnerIndex(i);
                if(algoType!=null) {
                    viewModel.setAlgorithm(algoType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getCurrentAlgorithm().observe(this, algorithmType -> {
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
}
