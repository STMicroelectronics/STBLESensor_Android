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
package com.st.BlueMS.demos.aiDataLog;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.aiDataLog.adapter.RangeSpinnerAdapter;
import com.st.BlueMS.demos.aiDataLog.adapter.SelectableFeatureListAdapter;
import com.st.BlueMS.demos.aiDataLog.viewModel.AnnotationLogViewModel;
import com.st.BlueMS.demos.aiDataLog.viewModel.LogParametersViewModel;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;


public class AILogSetParametersDemoFragment extends Fragment {

    private static final String ARG_NODE_TAG = AIDataLogDemoFragment.class.getName()+".ARG_NODE_TAG";

    interface OnDataSelectedListener {
        void onDataSelectedEnded();
    }

    public AILogSetParametersDemoFragment() {
        // Required empty public constructor
    }
    public static AILogSetParametersDemoFragment newInstance(Node node) {
        AILogSetParametersDemoFragment fragment = new AILogSetParametersDemoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NODE_TAG, node.getTag());

        fragment.setArguments(args);
        return fragment;
    }

    private @Nullable Node extractNode(){
        Bundle args = getArguments();
        if(args == null)
            return null;
        String nodeTag = args.getString(ARG_NODE_TAG);
        if(nodeTag==null)
            return null;
        return Manager.getSharedInstance().getNodeWithTag(nodeTag);
    }

    private SelectableFeatureListAdapter mFeatureListAdapter;
    private LogParametersViewModel mParametersViewModel;
    private OnDataSelectedListener mListener;


    private void extractListener(Context context) {
        if (context instanceof OnDataSelectedListener) {
            mListener = (OnDataSelectedListener) context;
        } else {
            Fragment parent = getParentFragment();
            if (parent instanceof OnDataSelectedListener) {
                mListener = (OnDataSelectedListener) parent;
            }
        }
        if (mListener == null) {
            throw new IllegalArgumentException("The Activity or the parent fragment must extend OnDataSelectedListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        extractListener(context);

        Node node = extractNode();
        if(node==null)
            return;

        mParametersViewModel = ViewModelProviders.of(requireActivity())
                .get(LogParametersViewModel.class);
        mFeatureListAdapter = new SelectableFeatureListAdapter(mParametersViewModel.getSupportedFeature(),
                new SelectableFeatureListAdapter.SelectableFeatureListCallback() {
            @Override
            public void onSelectItem(CharSequence name) {
                mParametersViewModel.selectFeatureWithName(name);
            }

            @Override
            public void onDeselectItem(CharSequence name) {
                mParametersViewModel.deselectFeatureWithName(name);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setUpEnvironmentalFequencySelector(View rootView){
        Spinner environmentalFrequencySelector = rootView.findViewById(R.id.iaLog_selectData_environmentalSelector);

        RangeSpinnerAdapter adapter = new RangeSpinnerAdapter(requireContext(),
                R.string.iaLog_selectData_environmentalSamplingFreqValueFormat,
                LogParametersViewModel.MIN_ENVIRONMENTAL_SAMPLING_HZ,
                LogParametersViewModel.MAX_ENVIRONMENTAL_SAMPLING_HZ,
                LogParametersViewModel.ENVIRONMENTAL_SAMPLING_STEP_HZ);
        environmentalFrequencySelector.setAdapter(adapter);

        environmentalFrequencySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mParametersViewModel.setEnvironmentalSamplingFrequency(adapter.getValue(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                float startValue = mParametersViewModel.getEnvironmentalSamplingFrequencyOrDefault();
                environmentalFrequencySelector.setSelection(adapter.getPosition(startValue));
            }
        });

        float startValue = mParametersViewModel.getEnvironmentalSamplingFrequencyOrDefault();
        environmentalFrequencySelector.setSelection(adapter.getPosition(startValue));
    }

    private View mIsLoggingView;
    private View mSetParametersView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ailog_set_parameters_demo, container, false);

        view.<RecyclerView>findViewById(R.id.aiLog_featureData_list).setAdapter(mFeatureListAdapter);

        view.findViewById(R.id.iaLog_selectData_nextButton).setOnClickListener(v -> showAnnotationListFragment());

        mIsLoggingView = view.findViewById(R.id.aiLog_selectData_isLogging);
        mSetParametersView = view.findViewById(R.id.iaLog_selectData_SetParameters);

        setUpInertialFrequencySelector(view);

        setUpEnvironmentalFequencySelector(view);
        setUpLogView(view);
        setUpAudioVolumeSelector(view);

        return view;
    }

    private void setUpAudioVolumeSelector(View rootView) {
        Spinner audioVolumeSelector = rootView.findViewById(R.id.iaLog_selectData_audioVolumeSelector);

        SpinnerAdapter adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                convertToObjArray(LogParametersViewModel.AUDIO_VOLUME_VALUES,
                        R.string.iaLog_selectData_audioVolumeValueFormat));

        audioVolumeSelector.setAdapter(adapter);

        audioVolumeSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mParametersViewModel.setAudioVolume(LogParametersViewModel.AUDIO_VOLUME_VALUES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                float startValue = mParametersViewModel.getAudioSamplingFrequencyOrDefault();
                mParametersViewModel.setAudioVolume(startValue);
            }
        });

        audioVolumeSelector.setSelection(LogParametersViewModel.DEFAULT_AUDIO_INDEX);
    }

    private void setUpLogView(View root) {
        AnnotationLogViewModel loggingViewModel = ViewModelProviders.of(requireActivity()).get(AnnotationLogViewModel.class);
        loggingViewModel.getIsLogging().observe(getViewLifecycleOwner(), this::showLoggingView);
        showLoggingView(loggingViewModel.getIsLogging().getValue());
        root.findViewById(R.id.aiLog_selectData_isLogging_stopLog).setOnClickListener(v ->
                loggingViewModel.startStopLogging(mParametersViewModel.getSelectedFeatureMask(),
                mParametersViewModel.getEnvironmentalSamplingFrequencyOrDefault(),
                mParametersViewModel.getInertialSamplingFrequencyOrDefault(),
                        mParametersViewModel.getAudioSamplingFrequencyOrDefault())
        );
    }


    private void showLoggingView(@Nullable Boolean isLogging){
        if( isLogging == null || !isLogging){ // unknow or not logging
            mIsLoggingView.setVisibility(View.GONE);
            mSetParametersView.setVisibility(View.VISIBLE);
        }else{ // isLogging = true
            mIsLoggingView.setVisibility(View.VISIBLE);
            mSetParametersView.setVisibility(View.GONE);
        }
    }


    private String[] convertToObjArray(float[] array, @StringRes int dataFormat){
        String[] objArray = new String[array.length];
        Resources res = getResources();
        for (int i = 0; i < array.length; i++) {
            objArray[i]=res.getString(dataFormat,array[i]);
        }
        return objArray;
    }



    private void setUpInertialFrequencySelector(View view) {
        Spinner inertialFrequencySelector = view.findViewById(R.id.iaLog_selectData_inertialFrequencySelector);
        SpinnerAdapter adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                convertToObjArray(LogParametersViewModel.INERTIAL_SAMPLING_VALUES,
                        R.string.iaLog_selectData_inertialSamplingFreqValueFormat));
        inertialFrequencySelector.setAdapter(adapter);
        inertialFrequencySelector.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mParametersViewModel.setInertialSamplingFrequency(LogParametersViewModel.INERTIAL_SAMPLING_VALUES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                int position = LogParametersViewModel.DEFAULT_INERTIAL_INDEX;
                mParametersViewModel.setInertialSamplingFrequency(LogParametersViewModel.INERTIAL_SAMPLING_VALUES[position]);
            }
        });

        inertialFrequencySelector.setSelection(LogParametersViewModel.DEFAULT_INERTIAL_INDEX);
    }

    private void showAnnotationListFragment(){
        mListener.onDataSelectedEnded();
    }



}
