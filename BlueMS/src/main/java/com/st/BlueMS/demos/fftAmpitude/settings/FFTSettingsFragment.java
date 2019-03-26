package com.st.BlueMS.demos.fftAmpitude.settings;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
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

public class FFTSettingsFragment extends Fragment {

    private static final String ARG_NODE_TAG = FFTAmplitudePlotFragment.class.getName()+".ARG_NODE_TAG";

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

    private ArrayAdapter<CharSequence> createAdapterFromArray(@ArrayRes int res){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                res, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void setupWindowSelector(Spinner selector,FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter =
                createAdapterFromArray(R.array.fttAmpl_settings_winValues);
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

        viewModel.getWindowType().observe(this, windowType -> {
            if(windowType==null)
                return;
            int index = windowType.ordinal();
            selector.setSelection(index);
        });
    }

    private void setupOdrSelector(Spinner selector,FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter =
                createAdapterFromArray(R.array.fttAmpl_settings_odrValues);
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

        viewModel.getOdr().observe(this, newOdr -> {
            if(newOdr==null)
                return;
            selector.setSelection(adapter.getPosition(newOdr.toString()));
        });
    }

    private void setupSizeSelector(Spinner selector,FFTSettingsViewModel viewModel){
        ArrayAdapter<CharSequence> adapter =
                createAdapterFromArray(R.array.fttAmpl_settings_sizeValues);
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

        viewModel.getSize().observe(this, newSize -> {
            if(newSize==null)
                return;
            selector.setSelection(adapter.getPosition(newSize.toString()));
        });
    }

    private void setupFullScaleFrequencySelector(Spinner selector, FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter =
                createAdapterFromArray(R.array.fttAmpl_settings_fullScaleValues);
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

        viewModel.getSensorFullScale().observe(this, newFs -> {
            if(newFs==null)
                return;
            selector.setSelection(adapter.getPosition(newFs.toString()));
        });
    }

    private void setupSubRangeSelector(Spinner selector,FFTSettingsViewModel viewModel){

        ArrayAdapter<CharSequence> adapter =
                createAdapterFromArray(R.array.fttAmpl_settings_subRangesValues);
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

        viewModel.getSubRange().observe(this, newSubRange -> {
            if(newSubRange==null)
                return;
            selector.setSelection(adapter.getPosition(newSubRange.toString()));
        });
    }

    private void setUpOverlapInput(TextInputLayout layout, EditText textEdit, FFTSettingsViewModel viewModel){
        viewModel.getOverlap().observe(this, newOvl -> {
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
        viewModel.getTimeAcquisition().observe(this, newTime -> {
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

        mSettingsViewModel.getUpdateParamCorrectly().observe(this, successUpdate -> {
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
