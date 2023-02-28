package com.st.trilobyte.models;

import java.util.List;

public class EnumProperty extends Property<Integer> {

    private List<PropertyEnumValue> enumValues;

    public List<PropertyEnumValue> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(final List<PropertyEnumValue> enumValues) {
        this.enumValues = enumValues;
    }
}
