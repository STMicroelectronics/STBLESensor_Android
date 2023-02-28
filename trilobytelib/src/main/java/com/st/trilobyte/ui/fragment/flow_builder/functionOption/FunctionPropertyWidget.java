package com.st.trilobyte.ui.fragment.flow_builder.functionOption;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.st.trilobyte.R;
import com.st.trilobyte.helper.DeviceHelperKt;
import com.st.trilobyte.models.BoolProperty;
import com.st.trilobyte.models.EnumProperty;
import com.st.trilobyte.models.NumberProperty;
import com.st.trilobyte.models.Property;
import com.st.trilobyte.models.PropertyEnumValue;
import com.st.trilobyte.models.StringProperty;

import java.util.Locale;

public class FunctionPropertyWidget extends LinearLayout {

    protected Property mFunctionProperty;

    protected View mView;

    public FunctionPropertyWidget(final Context context) {
        super(context);
    }

    public FunctionPropertyWidget(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FunctionPropertyWidget(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(Property functionProperty) {
        mFunctionProperty = functionProperty;
        setLayout();
        renderView();
    }

    protected void setLayout() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.function_property_widget, this, false);
        addView(mView);
    }

    protected void renderView() {

        TextView label = mView.findViewById(R.id.property_label);
        label.setText(mFunctionProperty.getLabel());
        RadioGroup radioGroup = mView.findViewById(R.id.property_radiogroup);
        TextView valueTextview = mView.findViewById(R.id.property_value);
        Switch valueSwitch = mView.findViewById(R.id.property_switch);
        if (mFunctionProperty.getType() == Property.PropertyType.ENUM) {
            radioGroup.setVisibility(VISIBLE);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int spacing = (int) DeviceHelperKt.convertDpToPixel(8, getContext());
            params.setMargins(spacing, spacing, spacing, spacing);

            final EnumProperty property = (EnumProperty) mFunctionProperty;
            for (PropertyEnumValue enumValue : property.getEnumValues()) {

                RadioButton radioButton = new RadioButton(getContext());
                radioButton.setLayoutParams(params);
                radioButton.setText(enumValue.getLabel());
                radioButton.setId(enumValue.getValue());
                radioButton.setChecked(enumValue.getValue() == property.getValue());
                radioGroup.addView(radioButton);

                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {
                        if (isChecked) {
                            property.setValue(compoundButton.getId());
                        }
                    }
                });
            }

        } else if (mFunctionProperty.getType() == Property.PropertyType.STRING) {
            valueTextview.setVisibility(VISIBLE);

            StringProperty stringProperty = (StringProperty) mFunctionProperty;
            valueTextview.setText(stringProperty.getValue());
            valueTextview.setInputType(InputType.TYPE_CLASS_TEXT);

        } else if (mFunctionProperty.getType() == Property.PropertyType.BOOL) {
            valueSwitch.setVisibility(VISIBLE);

            BoolProperty booleanProperty = (BoolProperty) mFunctionProperty;
            valueSwitch.setChecked(booleanProperty.getValue());
        } else {
            valueTextview.setVisibility(VISIBLE);

            final NumberProperty property = (NumberProperty) mFunctionProperty;

            if (mFunctionProperty.getType() == Property.PropertyType.FLOAT) {
                valueTextview.setText(String.format(Locale.UK, "%.10f", property.getValue()));
                valueTextview.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            } else if (mFunctionProperty.getType() == Property.PropertyType.INT) {
                valueTextview.setText(String.format(Locale.UK, "%d", property.getAsInteger()));
                valueTextview.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        }
    }

    public Property getFunctionProperty() {
        return mFunctionProperty;
    }

    public boolean setValue() {

        if (mFunctionProperty.getType() == Property.PropertyType.ENUM) {
            return true;
        }

        Switch switchView = mView.findViewById(R.id.property_switch);
        if (mFunctionProperty.getType() == Property.PropertyType.BOOL) {
            BoolProperty property = (BoolProperty) mFunctionProperty;
            property.setValue(switchView.isChecked());

            return true;
        }

        TextView valueTextview = mView.findViewById(R.id.property_value);

        if (mFunctionProperty.getType() == Property.PropertyType.STRING) {
            StringProperty property = (StringProperty) mFunctionProperty;
            property.setValue(valueTextview.getText().toString());
            return true;
        }

        try {
            NumberProperty property = (NumberProperty) mFunctionProperty;
            double value = Double.parseDouble(valueTextview.getText().toString());

            if (property.getMinValue() != null) {
                if (value < property.getMinValue()) {
                    return false;
                }
            }

            if (property.getMaxValue() != null) {
                if (value > property.getMaxValue()) {
                    return false;
                }
            }

            property.setValue(value);
            return true;

        } catch (Exception e) {
            return false;
        }
    }
}
