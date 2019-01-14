package com.st.BlueMS.demos.aiDataLog.viewModel;

import com.st.BlueMS.demos.aiDataLog.repository.Annotation;

public class SelectableAnnotation {

    public final Annotation annotation;

    private boolean isSelected;

    public SelectableAnnotation(Annotation annotation) {
        this.annotation = annotation;
        isSelected = false;
    }

    public boolean isSelected() {
        return isSelected;
    }

    void setSelected(boolean isSelected){
        this.isSelected=isSelected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectableAnnotation that = (SelectableAnnotation) o;

        if (isSelected != that.isSelected) return false;
        return annotation != null ? annotation.equals(that.annotation) : that.annotation == null;
    }

    @Override
    public int hashCode() {
        int result = annotation != null ? annotation.hashCode() : 0;
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }
}
