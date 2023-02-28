package com.st.trilobyte.models;

import java.io.Serializable;

public class PropertyEnumValue implements Serializable {

    private String label;

    private int value;

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PropertyEnumValues{" +
                "label='" + label + '\'' +
                ", value=" + value +
                '}';
    }
}
