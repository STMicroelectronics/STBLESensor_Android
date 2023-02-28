package com.st.trilobyte.models;

public class NumberProperty extends Property<Double> {

    private Double minValue;

    private Double maxValue;

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(final Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(final Double maxValue) {
        this.maxValue = maxValue;
    }

    public int getAsInteger() {
        return (int) getValue().doubleValue();
    }

    @Override
    public String toString() {
        return "NumberProperty{" +
                "minValue=" + minValue +
                ", maxValue=" + maxValue +
                '}';
    }
}
