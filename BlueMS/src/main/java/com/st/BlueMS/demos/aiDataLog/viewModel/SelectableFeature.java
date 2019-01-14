package com.st.BlueMS.demos.aiDataLog.viewModel;

public class SelectableFeature {

    public final CharSequence name;
    public final long mask;
    public boolean isSelected;


    public SelectableFeature(String name, long mask) {
        this.name = name;
        this.mask = mask;
        isSelected=false;
    }
}
