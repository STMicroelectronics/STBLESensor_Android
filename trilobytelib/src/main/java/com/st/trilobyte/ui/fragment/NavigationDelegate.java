package com.st.trilobyte.ui.fragment;

import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.FlowCategory;

import java.util.List;

public interface NavigationDelegate {

    void showExpertMode();

    void showFlowCategory(FlowCategory category);

    void uploadFlows(List<Flow> flows);
}
