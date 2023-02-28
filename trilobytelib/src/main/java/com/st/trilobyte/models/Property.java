package com.st.trilobyte.models;

import java.io.Serializable;

public class Property<T> implements Serializable {

    public enum PropertyType {
        FLOAT, INT, ENUM, STRING, BOOL
    }

    private String label;

    private PropertyType type;

    private T value;

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public PropertyType getType() {
        return type;
    }

    public void setType(final PropertyType type) {
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Property{" +
                "label='" + label + '\'' +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}
