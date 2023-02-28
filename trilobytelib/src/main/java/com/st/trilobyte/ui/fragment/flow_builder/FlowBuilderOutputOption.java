package com.st.trilobyte.ui.fragment.flow_builder;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.st.trilobyte.R;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.models.Output;
import com.st.trilobyte.models.Property;
import com.st.trilobyte.ui.fragment.flow_builder.functionOption.FunctionPropertyWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlowBuilderOutputOption extends BuilderFragment {

    private Output mOutput;
    private int mOutputIndex;
    private List<Property<?>> mProperties;

    public static FlowBuilderOutputOption getInstance(int optionIndex) {
        FlowBuilderOutputOption fragment = new FlowBuilderOutputOption();
        fragment.setOutputIndex(optionIndex);
        return fragment;
    }

    private void setOutputIndex(int optionIndex) {
        mOutputIndex = optionIndex;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_output_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.output_options));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        mOutput = getCurrentFlow().getOutputs().get(mOutputIndex);
        mProperties = new ArrayList<>(mOutput.getProperties());

        TextView outputName = view.findViewById(R.id.output_description);
        outputName.setText(mOutput.getDescription());

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                saveData();
            }
        });

        fillView();
    }

    private void fillView() {

        LinearLayout propertiesContainer = getView().findViewById(R.id.properties_container);

        for (Property property : mProperties) {
            FunctionPropertyWidget widget = new FunctionPropertyWidget(getContext());
            widget.init(property);
            propertiesContainer.addView(widget);
        }
    }

    private void saveData() {

        LinearLayout propertiesContainer = getView().findViewById(R.id.properties_container);

        for (int i = 0; i < propertiesContainer.getChildCount(); i++) {
            FunctionPropertyWidget widget = (FunctionPropertyWidget) propertiesContainer.getChildAt(i);
            if (!widget.setValue()) {
                DialogHelper.showDialog(getActivity(), String.format(Locale.getDefault(),
                        getString(R.string.error_cannot_save_property_value_s), widget.getFunctionProperty().getLabel()), null);
                return;
            }
        }

        mOutput.setProperties(mProperties);
        getActivity().onBackPressed();
    }
}
