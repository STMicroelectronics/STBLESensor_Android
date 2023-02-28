package com.st.trilobyte.models;

import java.io.Serializable;
import java.util.Objects;

public class FilterConfiguration implements Serializable {

    public CutOff highPass;

    public CutOff lowPass;

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FilterConfiguration that = (FilterConfiguration) o;
        return Objects.equals(highPass, that.highPass) &&
                Objects.equals(lowPass, that.lowPass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(highPass, lowPass);
    }
}
