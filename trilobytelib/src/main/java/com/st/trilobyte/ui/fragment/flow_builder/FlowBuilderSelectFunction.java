package com.st.trilobyte.ui.fragment.flow_builder;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.DeviceHelperKt;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.FlowHelper;
import com.st.trilobyte.helper.FunctionHelper;
import com.st.trilobyte.helper.RawResHelperKt;
import com.st.trilobyte.helper.SensorHelper;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class FlowBuilderSelectFunction extends BuilderFragment {

    private List<Sensor> sensorList;

    private List<Function> functionList;

    private List<Function> filteredFunctionList;

    private RadioGroup mRadioGroup;

    private List<String> mFlowInputs = new ArrayList<>();

    private Node.Type mBoard;

    public static FlowBuilderSelectFunction getInstance(Node.Type board) {
        FlowBuilderSelectFunction fragment = new FlowBuilderSelectFunction();
        fragment.setBoardType(board);
        return fragment;
    }

    private void setBoardType(Node.Type board) {
        mBoard = board;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_function, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sensorList = RawResHelperKt.getSensorList(getContext(),mBoard);

        functionList = RawResHelperKt.getFunctionList(getContext(),mBoard);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.functions));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                final Function selectedFunction = getSelectedFunction(mRadioGroup.getCheckedRadioButtonId());

                if (selectedFunction == null) {
                    DialogHelper.showDialog(getActivity(), getString(R.string.error_select_function_to_continue), null);
                    return;
                }

                if (hasFunctionAmbiguousInputs(selectedFunction)) {
                    return;
                }

                if (!validateFunctionParameterCount(selectedFunction)) {
                    DialogHelper.showDialog(getActivity(),
                            getString(R.string.error_not_enough_parameter_count, selectedFunction.getParametersCount()), null);
                    return;
                }

                addFunctionToFlow(selectedFunction);
            }
        });

        fillView();
    }

    private void fillView() {

        Flow currentFlow = getCurrentFlow();

        filteredFunctionList = new ArrayList<>(functionList);

        if (currentFlow.getFunctions().isEmpty()) {
            FlowHelper.getFlowSensorInputs(currentFlow, mFlowInputs);
            FlowHelper.getFlowFunctionInputs(currentFlow, mFlowInputs);
        } else {
            Function lastFunction = currentFlow.getFunctions().get(currentFlow.getFunctions().size() - 1);
            mFlowInputs.add(lastFunction.getId());
        }

        FunctionHelper.filterFunctionsByMandatoryInputs(filteredFunctionList, mFlowInputs);
        FunctionHelper.filterFunctionsByInputs(filteredFunctionList, mFlowInputs);
        FunctionHelper.filterFunctionByRepeatCount(currentFlow, filteredFunctionList);

        mRadioGroup = getView().findViewById(R.id.function_radiogroup);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int spacing = (int) DeviceHelperKt.convertDpToPixel(8, getContext());
        params.setMargins(spacing, spacing, spacing, spacing);

        for (int i = 0; i < filteredFunctionList.size(); i++) {
            Function function = filteredFunctionList.get(i);
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setLayoutParams(params);
            radioButton.setText(function.getDescription());
            radioButton.setId(i);
            mRadioGroup.addView(radioButton);
        }
    }

    private void addFunctionToFlow(Function selectedFunction) {
        getCurrentFlow().getFunctions().add(selectedFunction);
        getCurrentFlow().setOutputs(new ArrayList<Output>());
        getActivity().onBackPressed();
    }

    /**
     * Segnala all'utente che la funzione selezionata è compatibile con più ingressi selezionati e
     * di conseguenza deve correggere la sezione di input.
     *
     * @param selectedFunction
     * @return true if user must choose which input to use for the selected function
     */
    private boolean hasFunctionAmbiguousInputs(Function selectedFunction) {

        if (!getCurrentFlow().getFunctions().isEmpty()) {
            return false;
        }

        StringBuilder builder = new StringBuilder();

        List<String> matchingIds = new ArrayList<>();
        for (String inputId : mFlowInputs) {
            if (selectedFunction.getInputs().contains(inputId)) {
                matchingIds.add(inputId);
                String description = getInputDescriptionById(inputId);
                builder.append("- " + description + "\n");
            }
        }

        boolean hasConflictingInputs = matchingIds.size() > selectedFunction.getParametersCount();

        if (hasConflictingInputs) {
            DialogHelper.showDialog(getActivity(), String.format(getString(R.string.warn_recheck_input_section),
                    builder.toString()), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    getActivity().onBackPressed();
                }
            });
        }

        return hasConflictingInputs;
    }

    private String getInputDescriptionById(String id) {

        Sensor sensor = SensorHelper.findSensorById(sensorList, id);
        if (sensor != null) {
            return sensor.getDescription();
        }

        Function function = FunctionHelper.findFunctionById(functionList, id);
        if (function != null) {
            return function.getDescription();
        }

        return "";
    }

    private boolean validateFunctionParameterCount(Function selectedFunction) {

        List<String> matchingIds = new ArrayList<>();
        for (String inputId : mFlowInputs) {
            if (selectedFunction.getInputs().contains(inputId)) {
                matchingIds.add(inputId);
            }
        }

        return matchingIds.size() == selectedFunction.getParametersCount();
    }

    private Function getSelectedFunction(int position) {
        if (position < 0 || position >= filteredFunctionList.size()) {
            return null;
        }
        return filteredFunctionList.get(position);
    }
}
