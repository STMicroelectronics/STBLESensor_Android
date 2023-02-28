package com.st.trilobyte.ui.fragment.flow_builder;

import android.content.Context;
import androidx.fragment.app.Fragment;

import com.st.trilobyte.models.Flow;
import com.st.trilobyte.ui.NewFlowActivity;
import com.st.trilobyte.ui.fragment.StFragment;

public class BuilderFragment extends StFragment {

    private NewFlowActivity mActivity;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        mActivity = (NewFlowActivity) context;
    }

    protected Flow getCurrentFlow() {
        return mActivity.getCurrentFlow();
    }

    public void switchFragment(Fragment fragment) {
        mActivity.switchFragment(fragment);
    }
}
