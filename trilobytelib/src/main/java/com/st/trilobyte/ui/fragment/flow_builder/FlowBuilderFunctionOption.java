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
import com.st.trilobyte.models.EnumProperty;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.Function;
import com.st.trilobyte.models.Property;
import com.st.trilobyte.ui.fragment.flow_builder.functionOption.FunctionPropertyWidget;
import com.st.trilobyte.ui.fragment.flow_builder.functionOption.ThresholdPropertiesWidget;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlowBuilderFunctionOption extends BuilderFragment {

    private int mFunctionIndex;
    private Function mFunction;
    private List<Property<?>> mProperties;
    private EnumProperty mLineToCompare;

    public static FlowBuilderFunctionOption getInstance(int functionIndex) {
        FlowBuilderFunctionOption fragment = new FlowBuilderFunctionOption();
        fragment.setFunctionIndex(functionIndex);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_function_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.function_options));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                requireActivity().onBackPressed();
            }
        });

        Flow flow = getCurrentFlow();
        mFunction = flow.getFunctions().get(mFunctionIndex);

        TextView sensorName = view.findViewById(R.id.properties_description);
        sensorName.setText(mFunction.getDescription());

        view.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                saveData();
            }
        });

        mProperties = new ArrayList<>(mFunction.getProperties());

        FFTloadProperty();

        fillView((ViewGroup) view.findViewById(R.id.properties_container));
    }

    private void FFTloadProperty() {
        if (mFunction.isFFTCompare()) {
            Flow flow = getCurrentFlow();
            if (flow.getFlows().size() == 1) {
                flow = flow.getFlows().get(0);
            }
            if (flow.getFunctions().size() > 0) {
                Function lastFunction = flow.getFunctions().get(0);
                if (lastFunction.isFFT()) {
                    mLineToCompare = null;
                    for (Property property : lastFunction.getProperties()) {
                        if (property.getType().equals(Property.PropertyType.ENUM)) {
                            mLineToCompare = (EnumProperty) property;
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setFunctionIndex(final int index) {
        mFunctionIndex = index;
    }

    private void fillView(ViewGroup propertiesContainer) {
//        if(mFunction.isThresholdCompare()){
//            ThresholdPropertiesWidget widget = new ThresholdPropertiesWidget(requireContext());
//            widget.init(mProperties.get(0),getCurrentFlow().getSensors().get(0));
//            propertiesContainer.addView(widget);
//        }else {
            for (Property property : mProperties) {
                FunctionPropertyWidget widget = new FunctionPropertyWidget(getContext());
                widget.init(property);
                propertiesContainer.addView(widget);
            }
//        }
    }

    private void saveData() {

        LinearLayout propertiesContainer = getView().findViewById(R.id.properties_container);

        for (int i = 0; i < propertiesContainer.getChildCount(); i++) {
            FunctionPropertyWidget widget = (FunctionPropertyWidget) propertiesContainer.getChildAt(i);
            Property functionProperty = widget.getFunctionProperty();
            if (!widget.setValue()) {
                DialogHelper.showDialog(requireActivity(),
                        String.format(Locale.getDefault(),
                        getString(R.string.error_cannot_save_property_value_s),
                                functionProperty.getLabel()), null);
                return;
            }
            if (mFunction.isFFTCompare()) {
                if (functionProperty.getType().equals(Property.PropertyType.INT)) {
                    if (mLineToCompare != null) {
                        Double value = (Double) functionProperty.getValue();
                        Integer check = mLineToCompare.getValue()/2;
                        if (value >= check) {
                            DialogHelper.showDialog(requireActivity(),
                                    String.format(getString(R.string.error_cannot_save_property_value_d),
                                            functionProperty.getLabel(),
                                            check), null);
                            return;
                        }
                    }
                }
            }
        }


        mFunction.setProperties(mProperties);
        requireActivity().onBackPressed();
    }
}
