package com.st.BlueMS.demos.aiDataLog;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.aiDataLog.viewModel.AnnotationLogViewModel;
import com.st.BlueSTSDK.Features.FeatureAILogging;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoFragment;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;

@DemoDescriptionAnnotation(name="AI Data Log",
        iconRes=R.drawable.multiple_log_icon,
        requareAll = {FeatureAILogging.class}
)
public class AIDataLogDemoFragment extends DemoFragment implements AILogSetParametersDemoFragment.OnDataSelectedListener {

    private static String SELECT_DATA_FRAGMENT_TAG = AIDataLogDemoFragment.class.getName()+".SELECT_DATA_FRAGMENT_TAG";

    public AIDataLogDemoFragment(){
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_data_log_demo,container,false);
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {

        AnnotationLogViewModel viewModel = ViewModelProviders.of(requireActivity()).get(AnnotationLogViewModel.class);
        viewModel.start(node.getFeature(FeatureAILogging.class));

        FragmentManager fm = getChildFragmentManager();
        if(fm.findFragmentByTag(SELECT_DATA_FRAGMENT_TAG)!=null)
            return;
        fm.beginTransaction()
                .add(R.id.aiLog_fragment, AILogSetParametersDemoFragment.newInstance(node),SELECT_DATA_FRAGMENT_TAG)
                .commit();
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        AnnotationLogViewModel viewModel = ViewModelProviders.of(requireActivity()).get(AnnotationLogViewModel.class);
        viewModel.stop();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.findItem(R.id.startLog).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDataSelectedEnded() {
        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction()
                .replace(R.id.aiLog_fragment,AnnotationListFragment.newInstance(),SELECT_DATA_FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }
}
