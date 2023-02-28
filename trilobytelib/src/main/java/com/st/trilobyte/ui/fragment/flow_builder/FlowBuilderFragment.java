package com.st.trilobyte.ui.fragment.flow_builder;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.ExtensionsKt;
import com.st.trilobyte.helper.FlowHelper;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Sensor;
import com.st.trilobyte.widget.FlowBuilderWidget;

import java.util.List;

public class FlowBuilderFragment extends BuilderFragment {
    private FlowBuilderWidget inputWidget, functionWidget, outputWidget;

    private Node.Type mBoard;

    public static FlowBuilderFragment getInstance(Node.Type board) {
        FlowBuilderFragment fragment = new FlowBuilderFragment();
        fragment.setBoardType(board);
        return fragment;
    }

    private void setBoardType(Node.Type board) {
        mBoard = board;
    }

    private boolean canAbortFlowGeneration;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flow_builder, container, false);
    }

    private FlowBuilderWidget.WidgetClickListener outputWidgetListener = new FlowBuilderWidget.WidgetClickListener() {
        @Override
        public void onWidgetSelected() {
            switchFragment(FlowBuilderSelectOutput.getInstance(mBoard));
        }

        @Override
        public void onItemSelected(int index) {
            switchFragment(FlowBuilderSelectOutput.getInstance(mBoard));
        }

        @Override
        public void onSettingSelected(int index) {
            switchFragment(FlowBuilderOutputOption.getInstance(index));
        }

        @Override
        public void onDeleteItemSelected(final int index) {
            //nothing to do
        }
    };

    private FlowBuilderWidget.WidgetClickListener functionWidgetListener = new FlowBuilderWidget.WidgetClickListener() {
        @Override
        public void onWidgetSelected() {
            setArguments(null);
            switchFragment(FlowBuilderSelectFunction.getInstance(mBoard));
        }

        @Override
        public void onItemSelected(int index) {
            //nothing to do
        }

        @Override
        public void onSettingSelected(int index) {
            switchFragment(FlowBuilderFunctionOption.getInstance(index));
        }

        @Override
        public void onDeleteItemSelected(final int index) {
            deleteFunctionAtIndex(index);
        }
    };

    @Override
    public boolean onBackPressed() {

        if (!canAbortFlowGeneration) {
            DialogHelper.showDialog(getActivity(), getString(R.string.warn_abort_flow_wizard), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        canAbortFlowGeneration = true;
                        getActivity().onBackPressed();
                    }
                }
            });
        }

        return canAbortFlowGeneration;
    }

    // listener

    private FlowBuilderWidget.WidgetClickListener inputWidgetListener = new FlowBuilderWidget.WidgetClickListener() {
        @Override
        public void onWidgetSelected() {
            switchFragment(FlowBuilderSelectInput.getInstance(mBoard));
        }

        @Override
        public void onItemSelected(int index) {
            switchFragment(FlowBuilderSelectInput.getInstance(mBoard));
        }

        @Override
        public void onSettingSelected(int index) {
            Sensor sensor = getCurrentFlow().getSensors().get(index);
            switchFragment(FlowBuilderSensorOption.getInstance(sensor.getId(),mBoard));
        }

        @Override
        public void onDeleteItemSelected(final int index) {
            //nothing to do
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Flow currentFlow = getCurrentFlow();

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        String actionbarTitle = currentFlow.getDescription() != null && !currentFlow.getDescription().isEmpty() ?
                currentFlow.getDescription() : getString(R.string.new_flow);
        actionbarText.setText(actionbarTitle);

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        inputWidget = view.findViewById(R.id.input_widget);
        functionWidget = view.findViewById(R.id.functions_widget);
        outputWidget = view.findViewById(R.id.output_widget);

        inputWidget.setWidgetListener(inputWidgetListener);
        functionWidget.setWidgetListener(functionWidgetListener);
        outputWidget.setWidgetListener(outputWidgetListener);

        view.findViewById(R.id.terminate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (currentFlow.getOutputs().size() == 0) {
                    DialogHelper.showDialog(getActivity(), getString(R.string.cannot_save_flow_not_completed), null);
                    return;
                }

                for (Output output : currentFlow.getOutputs()) {
                    if (output.getId().equals(Output.OUTPUT_AS_INPUT_ID)) {
                        if (currentFlow.getFunctions().isEmpty() && !FlowHelper.isCompositeFlow(currentFlow)) {
                            DialogHelper.showDialog(getActivity(), getString(R.string.error_cannot_save_flow), null);
                            return;
                        }
                    }
                }

                switchFragment(FlowBuilderSaveConfig.getInstance());
            }
        });

        fillView();
    }

    private void fillView() {

        inputWidget.clear();

        if (getCurrentFlow().getSensors().isEmpty() && getCurrentFlow().getFlows().isEmpty()) {
            getView().findViewById(R.id.function_layout).setVisibility(View.GONE);
            getView().findViewById(R.id.output_layout).setVisibility(View.GONE);
            return;
        }

        inputWidget.setCompleted(true);

        for (Sensor sensor : getCurrentFlow().getSensors()) {
            inputWidget.addSensor(sensor,mBoard);
        }

        for (Flow flow : getCurrentFlow().getFlows()) {
            inputWidget.addFlow(flow);
        }

        getView().findViewById(R.id.function_layout).setVisibility(View.VISIBLE);

        functionWidget.clear();
        functionWidget.setCompleted(getCurrentFlow().getFunctions().size() > 0);
        for (Function function : getCurrentFlow().getFunctions()) {
            functionWidget.addFunction(function);
        }

        getView().findViewById(R.id.add_function).setVisibility(getCurrentFlow().getFunctions().size() > 0 ? View.VISIBLE : View.GONE);
        getView().findViewById(R.id.add_function).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                switchFragment(FlowBuilderSelectFunction.getInstance(mBoard));
            }
        });

        getView().findViewById(R.id.output_layout).setVisibility(View.VISIBLE);
        outputWidget.clear();
        List<Output> outputs = getCurrentFlow().getOutputs();
        outputWidget.setCompleted(outputs != null && outputs.size() > 0);
        if (outputs != null && outputs.size() > 0) {
            for (Output output : outputs) {
                outputWidget.addOutput(output);
            }

            TextView saveTextview = getView().findViewById(R.id.save_flow_textview);
            saveTextview.setTextColor(getResources().getColor(R.color.colorAccent));
            ImageView saveImageview = getView().findViewById(R.id.save_flow_imageview);
            saveImageview.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void deleteFunctionAtIndex(final int functionIndex) {
        DialogHelper.showDialog(getActivity(), getString(R.string.ask_delete_function), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    ExtensionsKt.removeAfterIndex(getCurrentFlow().getFunctions(), functionIndex);
                    getCurrentFlow().getOutputs().clear();
                    fillView();
                }
            }
        });
    }
}
