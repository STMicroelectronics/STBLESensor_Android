package com.st.trilobyte.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.adapter.FlowCategoryAdapter;
import com.st.trilobyte.helper.FileHelperKt;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.FlowCategory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExampleCategoriesFlowsFragment extends Fragment {

    private FlowCategoryAdapter mAdapter;
    private NavigationDelegate mFlowListener;

    Node.Type mBoard;

    public static ExampleCategoriesFlowsFragment getInstance(NavigationDelegate listener, Node.Type board) {
        ExampleCategoriesFlowsFragment fragment = new ExampleCategoriesFlowsFragment();
        fragment.setExpertModeListener(listener);
        fragment.setBoardType(board);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_example_flows, container,
                false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarTitle = view.findViewById(R.id.actionbar_text);
        actionbarTitle.setText(getString(R.string.example_flow_categories));

        mAdapter = new FlowCategoryAdapter();
        ListView flowsList = view.findViewById(R.id.flow_list);
        flowsList.setAdapter(mAdapter);

        View footerView = getActivity().getLayoutInflater().inflate(R.layout.flows_footer_view, flowsList, false);
        footerView.findViewById(R.id.footer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mFlowListener != null) {
                    mFlowListener.showExpertMode();
                }
            }
        });

        flowsList.addFooterView(footerView);
        flowsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int index, final long l) {

                if (index >= mAdapter.getCount()) {
                    return;
                }

                mFlowListener.showFlowCategory((FlowCategory) mAdapter.getItem(index));
            }
        });

        fillAdapter();
    }

    private void fillAdapter() {
        mAdapter.clear();

        Set<FlowCategory> categories = new HashSet<>();
        List<Flow> flows = FileHelperKt.loadExampleFlows(requireContext(),mBoard);
        for (Flow flow : flows) {
            categories.add(new FlowCategory(flow.getCategory()));
        }

        mAdapter.addFlows(new ArrayList<>(categories));
        mAdapter.notifyDataSetChanged();
    }

    public void setExpertModeListener(final NavigationDelegate expertModeListener) {
        mFlowListener = expertModeListener;
    }

    public void setBoardType(final Node.Type board) {
        mBoard = board;
    }

    @Override
    public void onDestroy() {
        mFlowListener = null;
        super.onDestroy();
    }
}
