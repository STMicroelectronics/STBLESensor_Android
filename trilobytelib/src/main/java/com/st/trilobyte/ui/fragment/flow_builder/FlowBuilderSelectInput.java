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
import com.st.trilobyte.helper.FileHelperKt;
import com.st.trilobyte.helper.RawResHelperKt;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;

import java.util.ArrayList;
import java.util.List;

public class FlowBuilderSelectInput extends BuilderFragment {

    private List<Sensor> selectedSensors;

    private List<Flow> selectedFlows;

    private Node.Type mBoard;

    public static FlowBuilderSelectInput getInstance(Node.Type board) {
        FlowBuilderSelectInput fragment = new FlowBuilderSelectInput();
        fragment.setBoardType(board);
        return fragment;
    }

    private void setBoardType(Node.Type board) {
        mBoard = board;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_select_input_sources, container, false);
    }

    private boolean bothMLCAndFSMArePresent(){
        // Sensors that cannot be enabled together
        boolean mlcSelected = false;
        boolean fsmSelected = false;
        for (int i = 0; i < selectedSensors.size(); i++) {
            if (selectedSensors.get(i).getId().contains("S12")) {
                mlcSelected = true;
            }
            if (selectedSensors.get(i).getId().contains("S13")) {
                fsmSelected = true;
            }
        }

        return mlcSelected && fsmSelected;
    }

    private boolean multipleAccelerometerAreSelected(){
        // Sensors that cannot be enabled together
        boolean mlcSelected = false;
        boolean fsmSelected = false;
        String accelerometerId = "S5";
        int nSelected = 0;
        for (int i = 0; i < selectedSensors.size(); i++) {
            if (selectedSensors.get(i).getId().contains(accelerometerId)) {
                nSelected ++;
            }
        }

        return nSelected>1;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.input_sources));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (selectedSensors.isEmpty() && selectedFlows.isEmpty()) {
                    DialogHelper.showDialog(getActivity(), getString(R.string.error_select_input_before_save), null);
                    return;
                }


                if (bothMLCAndFSMArePresent()) {
                    DialogHelper.showDialog(getActivity(), getString(R.string.error_select_mlc_fsm_before_save), null);
                    return;
                }

                if (multipleAccelerometerAreSelected()) {
                    DialogHelper.showDialog(getActivity(), getString(R.string.error_select_same_sensor_before_save), null);
                    return;
                }


                getCurrentFlow().getFunctions().clear();
                getCurrentFlow().setOutputs(new ArrayList<Output>());
                getCurrentFlow().setSensors(selectedSensors);
                getCurrentFlow().setFlows(selectedFlows);
                getActivity().onBackPressed();
            }
        });

        selectedSensors = getCurrentFlow().getSensors();
        selectedFlows = getCurrentFlow().getFlows();

        fillSensorContainer();
        fillAvailableInputs(FileHelperKt.loadSavedFlows(mBoard), R.id.flows_container, R.id.flows_as_input_label);
        fillAvailableInputs(RawResHelperKt.getExpFlowList(getContext(),mBoard), R.id.exp_flows_container, R.id.exp_flows_label);
    }

    private void fillSensorContainer() {

        final LinearLayout sensorContainer = getView().findViewById(R.id.sensors_container);
        sensorContainer.removeAllViews();

        List<Sensor> sensors = RawResHelperKt.getSensorList(getContext(),mBoard);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        final ArrayList<String> sensorsName = new ArrayList<>();
        final ArrayList<Integer> checkBoxId = new ArrayList<>();

        for (int i = 0; i < sensors.size(); i++) {
            final Sensor sensor = sensors.get(i);
            View view = inflater.inflate(R.layout.select_input_row, sensorContainer, false);

            TextView sensorNameTextview = view.findViewById(R.id.input_name);
            sensorNameTextview.setText(sensor.getDescription());
            sensorsName.add(sensor.getDescription());

            final CheckBox checkBox = view.findViewById(R.id.checkbox);
            checkBox.setId(i);
            checkBoxId.add(checkBox.getId());
            checkBox.setChecked(selectedSensors.contains(sensor));

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                    if (checked) {
                        selectedSensors.add(sensor);
                        return;
                    }
                    selectedSensors.remove(sensor);
                }
            });

            sensorContainer.addView(view);
        }
    }

    private void fillAvailableInputs(List<Flow> flows, int containerId, int labelId) {

        LinearLayout flowsContainer = getView().findViewById(containerId);
        flowsContainer.removeAllViews();

        if (flows == null) {
            getView().findViewById(labelId).setVisibility(View.GONE);
            return;
        }

        for (final Flow flow : flows) {
            if (flow.canBeUsedAsInput()) {
                View view = getLayoutInflater().inflate(R.layout.select_input_row, flowsContainer, false);

                TextView nameTextview = view.findViewById(R.id.input_name);
                nameTextview.setText(flow.getDescription());

                CheckBox checkBox = view.findViewById(R.id.checkbox);
                checkBox.setChecked(selectedFlows.contains(flow));

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton compoundButton, final boolean checked) {
                        if (checked) {
                            selectedFlows.add(flow);
                            return;
                        }

                        selectedFlows.remove(flow);
                    }
                });

                flowsContainer.addView(view);
            }
        }
        getView().findViewById(labelId).setVisibility(flowsContainer.getChildCount() == 0 ? View.GONE : View.VISIBLE);
    }
}
