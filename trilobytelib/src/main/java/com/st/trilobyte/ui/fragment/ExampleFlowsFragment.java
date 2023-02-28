package com.st.trilobyte.ui.fragment;

import android.content.Intent;
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
import com.st.trilobyte.adapter.FlowsAdapter;
import com.st.trilobyte.helper.FileHelperKt;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.models.FlowCategory;
import com.st.trilobyte.ui.NewFlowActivity;
import com.st.trilobyte.ui.SensorDetailActivity;
import com.st.trilobyte.ui.ViewFlowDetailActivity;

import java.util.Collections;
import java.util.List;

public class ExampleFlowsFragment extends Fragment {

    private final static String EXTRA_CATEGORY = "extra-category";
    private final static String EXTRA_BOARD_TYPE = "extra-board_type";

    private final static int SHOW_FLOW_DETAIL_REQ_CODE = 2000;

    private NavigationDelegate mFlowListener;
    private FlowsAdapter mAdapter;

    private String mCategory;

    private Node.Type mBoard;

    public static ExampleFlowsFragment getInstance(FlowCategory category, NavigationDelegate listener, Node.Type board) {
        ExampleFlowsFragment fragment = new ExampleFlowsFragment();
        if (category != null) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_CATEGORY, category.categoryName);
            bundle.putSerializable(EXTRA_BOARD_TYPE, board);
            fragment.setArguments(bundle);
        }
        fragment.setExpertModeListener(listener);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFlowListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_example_flows, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(EXTRA_CATEGORY)) {
            mCategory = getArguments().getString(EXTRA_CATEGORY);
        }

        if (getArguments() != null && getArguments().containsKey(EXTRA_BOARD_TYPE)) {
            mBoard = (Node.Type) getArguments().getSerializable(EXTRA_BOARD_TYPE);
        }

        TextView actionbarTitle = view.findViewById(R.id.actionbar_text);
        actionbarTitle.setText(getString(R.string.example_flow));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });

        final ListView flowsList = view.findViewById(R.id.flow_list);
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

                showFlowDetail((Flow) mAdapter.getItem(index));
            }
        });

        mAdapter = new FlowsAdapter(new FlowsAdapter.FlowActionListener() {
            @Override
            public void upload(final Flow flow) {
                if (mFlowListener != null) {
                    mFlowListener.uploadFlows(Collections.singletonList(flow));
                }
            }

            @Override
            public void delete(final Flow flow) {
                // nothing to do
            }
        });

        flowsList.setAdapter(mAdapter);
        fillAdapter();
    }

    private void fillAdapter() {
        mAdapter.clear();
        List<Flow> flows = FileHelperKt.loadExampleFlows(requireContext(),mBoard);
        mAdapter.addFlows(FileHelperKt.filterFlowsByCategory(flows, mCategory));
        mAdapter.notifyDataSetChanged();
    }

    private void showFlowDetail(Flow flow) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ViewFlowDetailActivity.EXTRA_FLOW, flow);
        bundle.putSerializable(ViewFlowDetailActivity.EXTRA_BOARD_TYPE, mBoard);
        bundle.putBoolean(ViewFlowDetailActivity.CAN_BE_EDITABLE, false);
        Intent intent = new Intent(getContext(), ViewFlowDetailActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, SHOW_FLOW_DETAIL_REQ_CODE);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_FLOW_DETAIL_REQ_CODE) {
            if (resultCode == ViewFlowDetailActivity.EDIT_FLOW_RESULT_CODE) {
                Intent intent = new Intent(getActivity(), NewFlowActivity.class);
                intent.putExtra(NewFlowActivity.EXTRA_BOARD_TYPE,mBoard);
                intent.putExtras(data);
                startActivity(intent);
            }
        }
    }

    public void setExpertModeListener(final NavigationDelegate expertModeListener) {
        mFlowListener = expertModeListener;
    }
}
