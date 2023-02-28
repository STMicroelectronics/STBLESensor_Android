package com.st.trilobyte.models;

import java.io.Serializable;
import java.util.Objects;

public class CutOff implements Serializable {

    private String label;

    private int value;

    public String getLabel() {
        return label;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CutOff cutOff = (CutOff) o;
        return value == cutOff.value &&
                Objects.equals(label, cutOff.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, value);
    }

    @Override
    public String toString() {
        return "Cutoff{" +
                "label='" + label + '\'' +
                ", value=" + value +
                '}';
    }
}
