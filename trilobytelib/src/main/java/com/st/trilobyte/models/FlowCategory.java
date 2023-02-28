package com.st.trilobyte.models;

public class FlowCategory {

    public String categoryName;

    public FlowCategory(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlowCategory that = (FlowCategory) o;

        return categoryName.equals(that.categoryName);
    }

    @Override
    public int hashCode() {
        return categoryName.hashCode();
    }
}
