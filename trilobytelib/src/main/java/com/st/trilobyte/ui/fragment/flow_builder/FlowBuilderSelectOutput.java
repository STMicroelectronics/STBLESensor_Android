package com.st.trilobyte.ui.fragment.flow_builder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.RawResHelperKt;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlowBuilderSelectOutput extends BuilderFragment {

    private List<Output> outputs;
    private List<Output> selectedOutputs;

    private Node.Type mBoard;

    public static FlowBuilderSelectOutput getInstance(Node.Type board) {
        FlowBuilderSelectOutput fragment = new FlowBuilderSelectOutput();
        fragment.setBoardType(board);
        return fragment;
    }

    private void setBoardType(Node.Type board) {
        mBoard = board;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_output, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        outputs = RawResHelperKt.getOutputList(getContext(),mBoard);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.output));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.continue_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (selectedOutputs.isEmpty()) {
                    DialogHelper.showDialog(getActivity(), getString(R.string.error_select_output_to_save), null);
                    return;
                }

                if (hasOutputAmbiguousInputs()) {
                    return;
                }

                getCurrentFlow().setOutputs(selectedOutputs);
                getActivity().onBackPressed();
            }
        });

        selectedOutputs = getCurrentFlow().getOutputs();
        filterOutputs();
    }

    private void filterOutputs() {

        Flow flow = getCurrentFlow();

        List<Set<String>> setList = new ArrayList<>();
        getAvailableOutputs(flow, setList);

        List<String> outputIds;

        if (setList.isEmpty()) {
            outputIds = new ArrayList<>();
        } else {
            Set<String> out = setList.get(0);
            for (final Set<String> set : setList) {
                out.retainAll(set);
            }

            outputIds = new ArrayList<>(out);
        }

        List<Output> availableOutputs = filterAvailableOutputs(new ArrayList<>(outputIds));

        for (Output selectedOutput : selectedOutputs) {
            if (!availableOutputs.contains(selectedOutput)) {
                selectedOutputs.remove(selectedOutput);
            }
        }

        fillView(availableOutputs);
    }

    private void getAvailableOutputs(Flow flow, List<Set<String>> outputs) {

        if (!flow.getFunctions().isEmpty()) {
            Function lastFunction = flow.getFunctions().get(flow.getFunctions().size() - 1);
            Set<String> out = new HashSet<>(lastFunction.getOutputs());
            outputs.add(out);
            return;
        }

        for (Sensor sensor : flow.getSensors()) {
            outputs.add(new HashSet<>(sensor.getOutputs()));
        }

        for (Flow parent : flow.getFlows()) {
            getAvailableOutputs(parent, outputs);
        }
    }

    private List<Output> filterAvailableOutputs(List<String> allowedOutputs) {

        List<Output> availableOutputs = new ArrayList<>(outputs);
        for (int i = availableOutputs.size() - 1; i >= 0; i--) {
            Output output = availableOutputs.get(i);
            if (!allowedOutputs.contains(output.getId())) {
                availableOutputs.remove(output);
            }
        }

        return availableOutputs;
    }

    private void fillView(List<Output> outputs) {
        LinearLayout outputContainer = getView().findViewById(R.id.outputs_container);
        outputContainer.removeAllViews();

        for (final Output output : outputs) {
            View view = getLayoutInflater().inflate(R.layout.select_output_row, outputContainer, false);

            TextView outputNameTextview = view.findViewById(R.id.output_name);
            outputNameTextview.setText(output.getDescription());

            CheckBox checkBox = view.findViewById(R.id.checkbox);
            checkBox.setChecked(selectedOutputs.contains(output));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                    if (checked) {
                        selectedOutputs.add(output);
                        return;
                    }

                    selectedOutputs.remove(output);
                }
            });

            outputContainer.addView(view);
        }
    }

    public boolean hasOutputAmbiguousInputs() {
        if (selectedOutputs.size() <= 1) {
            return false;
        }

        boolean onceLogic = false;
        boolean onceHw = false;
        boolean asInput = false;
        boolean asExp = false;
        for (Output output : selectedOutputs) {
            onceHw = onceHw || !output.isLogic();
            onceLogic = onceLogic || output.isLogic();
            asInput = asInput || output.getId().equals(Output.OUTPUT_AS_INPUT_ID);
            asExp = asExp || output.getId().equals(Output.OUTPUT_EXP_ID);

            if (asInput && asExp) {
                DialogHelper.showDialog(getActivity(), getString(R.string.error_cannot_set_two_logical_output), null);
                return true;
            }

            if (onceHw && onceLogic) {
                DialogHelper.showDialog(getActivity(), getString(R.string.error_select_outputs_to_save), null);
                return true;
            }
        }

        return false;
    }
}
