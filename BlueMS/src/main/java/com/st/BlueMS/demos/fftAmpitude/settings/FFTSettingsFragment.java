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
package com.st.BlueMS.demos.fftAmpitude.settings;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.fftAmpitude.FFTAmplitudePlotFragment;
import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;

/**
 * fragment used to set the fft settings
 */
public class FFTSettingsFragment extends Fragment {

    private static final String ARG_NODE_TAG = FFTSettingsFragment.class.getName()+".ARG_NODE_TAG";

    /**
     * build an instance of this fragment
     * @param node node where the settings will be sent, note: the node must be already connected
     *             and must export the Debug service
     * @return framgent to change the fft settings
     */
    public static Fragment newInstance(@NonNull Node node) {
        FFTSettingsFragment fragment = new FFTSettingsFragment();
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

    private FFTSettingsViewModel mSettingsViewModel;

    private Spinner mWindowSelector;
    private Spinner mOdrSelector;
    private Spinner mSizeSelector;
    private Spinner mFullScaleSelector;
    private Spinner mSubRangesSelector;
    private EditText mOverlapValue;
    private EditText mTimeAcquisitionValue;

    private @Nullable FFTSettings.WindowType windowTypeFromSpinnerIndex(int index){
        for(FFTSettings.WindowType winType :
             FFTSettings.WindowType.values()){
            if(winType.ordinal() == index){
                return winType;
            }
        }
        return null;
    }

    private void setupWindowSelector(Spinner selector,FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.fttAmpl_settings_winValues);
        selector.setAdapter(adapter);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                FFTSettings.WindowType newType = windowTypeFromSpinnerIndex(i);
                if(newType!=null) {
                    viewModel.setNewWindow(newType);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getWindowType().observe(getViewLifecycleOwner(), windowType -> {
            if(windowType==null)
                return;
            int index = windowType.ordinal();
            selector.setSelection(index);
        });
    }

    private void setupOdrSelector(Spinner selector,FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.fttAmpl_settings_odrValues);
        selector.setAdapter(adapter);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String textOdr = (String) adapterView.getItemAtPosition(i);
                try {
                    short newValue = Short.parseShort(textOdr);
                    viewModel.setOdr(newValue);
                }catch (NumberFormatException e){ }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getOdr().observe(getViewLifecycleOwner(), newOdr -> {
            if(newOdr==null)
                return;
            selector.setSelection(adapter.getPosition(newOdr.toString()));
        });
    }

    private void setupSizeSelector(Spinner selector,FFTSettingsViewModel viewModel){
        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.fttAmpl_settings_sizeValues);
        selector.setAdapter(adapter);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String textSize = (String) adapterView.getItemAtPosition(i);
                try {
                    short newValue = Short.parseShort(textSize);
                    viewModel.setSize(newValue);
                }catch (NumberFormatException e){ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getSize().observe(getViewLifecycleOwner(), newSize -> {
            if(newSize==null)
                return;
            selector.setSelection(adapter.getPosition(newSize.toString()));
        });
    }

    private void setupFullScaleFrequencySelector(Spinner selector, FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.fttAmpl_settings_fullScaleValues);
        selector.setAdapter(adapter);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String textSize = (String) adapterView.getItemAtPosition(i);
                try {
                    byte newValue = Byte.parseByte(textSize);
                    viewModel.setSensorFullScale(newValue);
                }catch (NumberFormatException e){ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getSensorFullScale().observe(getViewLifecycleOwner(), newFs -> {
            if(newFs==null)
                return;
            selector.setSelection(adapter.getPosition(newFs.toString()));
        });
    }

    private void setupSubRangeSelector(Spinner selector,FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter = com.st.BlueMS.demos.util
                .ArrayAdapter.createAdapterFromArray(requireContext(),R.array.fttAmpl_settings_subRangesValues);
        selector.setAdapter(adapter);

        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String textSize = (String) adapterView.getItemAtPosition(i);
                try {
                    byte newValue = Byte.parseByte(textSize);
                    viewModel.setSubRange(newValue);
                }catch (NumberFormatException e){ }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        viewModel.getSubRange().observe(getViewLifecycleOwner(), newSubRange -> {
            if(newSubRange==null)
                return;
            selector.setSelection(adapter.getPosition(newSubRange.toString()));
        });
    }

    private void setUpOverlapInput(TextInputLayout layout, EditText textEdit, FFTSettingsViewModel viewModel){
        viewModel.getOverlap().observe(getViewLifecycleOwner(), newOvl -> {
            if(newOvl == null){
                return;
            }
            String newText = newOvl.toString();
            textEdit.setText(newText);
            textEdit.setSelection(newText.length());
        });

        textEdit.addTextChangedListener(new CheckNumberRange(layout,R.string.fttAmpl_settings_ovlOutOfRange,
                FFTSettingsViewModel.MIN_OVERLAP,FFTSettingsViewModel.MAX_OVERLAP));

        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    byte newOverlap = Byte.parseByte(charSequence.toString());
                    viewModel.setOverlap(newOverlap);
                }catch (NumberFormatException e){ }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void setUpTimeAcquisitionInput(TextInputLayout layout, EditText textEdit, FFTSettingsViewModel viewModel){
        viewModel.getTimeAcquisition().observe(getViewLifecycleOwner(), newTime -> {
            if(newTime == null){
                return;
            }
            String newText = newTime.toString();
            textEdit.setText(newText);
            textEdit.setSelection(newText.length());
        });

        textEdit.addTextChangedListener(new CheckNumberRange(layout,R.string.fttAmpl_settings_tacqOutOfRange,
                FFTSettingsViewModel.MIN_TIME_ACQUISITION_MS,FFTSettingsViewModel.MAX_TIME_ACQUISITION_MS));

        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    int newTime = Integer.parseInt(charSequence.toString());
                    viewModel.setAcquisitionTime(newTime);
                }catch (NumberFormatException e){ }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void setupSaveButton(View saveButton, FFTSettingsViewModel viewModel){
        saveButton.setOnClickListener(view -> {
            Node node = extractNode();
            if(node!=null)
                viewModel.writeSettingsTo(node);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fft_ampl_settings,container,false);

        mWindowSelector = root.findViewById(R.id.fftAmpl_settings_window);
        mOdrSelector = root.findViewById(R.id.fftAmpl_settings_odr);
        mSizeSelector = root.findViewById(R.id.fftAmpl_settings_size);
        mFullScaleSelector = root.findViewById(R.id.fftAmpl_settings_fullScale);
        mSubRangesSelector = root.findViewById(R.id.fftAmpl_settings_subRange);
        mOverlapValue = root.findViewById(R.id.fftAmpl_settings_overlapValue);
        mTimeAcquisitionValue = root.findViewById(R.id.fftAmpl_settings_timeAcquisitionValue);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSettingsViewModel = ViewModelProviders
                .of(requireActivity())
                .get(FFTSettingsViewModel.class);

        setupWindowSelector(mWindowSelector,mSettingsViewModel);
        setupOdrSelector(mOdrSelector,mSettingsViewModel);
        setupSizeSelector(mSizeSelector,mSettingsViewModel);
        setupFullScaleFrequencySelector(mFullScaleSelector,mSettingsViewModel);
        setupSubRangeSelector(mSubRangesSelector,mSettingsViewModel);
        setUpOverlapInput(view.findViewById(R.id.fftAmpl_settings_overlapLayout),
                mOverlapValue,mSettingsViewModel);
        setUpTimeAcquisitionInput(view.findViewById(R.id.fftAmpl_settings_timeAcquisitionLayout),
                mTimeAcquisitionValue,mSettingsViewModel);
        setupSaveButton(view.findViewById(R.id.fftAmpl_settings_saveButton),mSettingsViewModel);

        mSettingsViewModel.getUpdateParamCorrectly().observe(getViewLifecycleOwner(), successUpdate -> {
            if(successUpdate==null)
                return;
             @StringRes int message = successUpdate ?R.string.fttAmpl_settings_updateCorrect :
                    R.string.fttAmpl_settings_updateFail;
            Toast.makeText(requireContext(),message,Toast.LENGTH_SHORT).show();
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        Node node = extractNode();
        if(node!=null){
            mSettingsViewModel.readSettingsFrom(node);
        }
    }

}
