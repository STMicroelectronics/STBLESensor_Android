package com.st.trilobyte.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;
import com.st.trilobyte.adapter.FlowsAdapter;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.FileHelperKt;
import com.st.trilobyte.models.Flow;
import com.st.trilobyte.ui.ComposeIfActivity;
import com.st.trilobyte.ui.NewFlowActivity;
import com.st.trilobyte.ui.SelectFlowsActivity;
import com.st.trilobyte.ui.ViewFlowDetailActivity;

import java.util.Collections;

public class CustomFlowsFragment extends StFragment {

    private final static int SHOW_FLOW_DETAIL_REQ_CODE = 2000;

    private FlowsAdapter mAdapter;

    private NavigationDelegate mFlowListener;

    Node.Type mBoard;

    public static CustomFlowsFragment getInstance(NavigationDelegate listener, Node.Type board) {
        CustomFlowsFragment fragment = new CustomFlowsFragment();
        fragment.setFlowListener(listener);
        fragment.setBoardType(board);
        return fragment;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFlowListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_flows, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView actionbarText = view.findViewById(R.id.actionbar_text);
        actionbarText.setText(getString(R.string.custom_flows));

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
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
                deleteSelectedFlow(flow);
            }
        });

        ListView listView = view.findViewById(R.id.flow_list);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, final View view, final int i, final long l) {
                showFlowDetail((Flow) mAdapter.getItem(i));
            }
        });

        view.findViewById(R.id.start_new_flow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getActivity(), NewFlowActivity.class);
                intent.putExtra(NewFlowActivity.EXTRA_BOARD_TYPE,mBoard);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.build_if_statement).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = ComposeIfActivity.Companion.provideIntent(getContext(),mBoard);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.play_flows).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = SelectFlowsActivity.Companion.provideIntent(getContext(),mBoard);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapter();
    }

    private void fillAdapter() {
        mAdapter.clear();
        mAdapter.addFlows(FileHelperKt.loadSavedFlows(mBoard));
        mAdapter.notifyDataSetChanged();
    }

    private void showFlowDetail(Flow flow) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ViewFlowDetailActivity.EXTRA_FLOW, flow);
        bundle.putSerializable(ViewFlowDetailActivity.EXTRA_BOARD_TYPE,mBoard);
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

    private void deleteSelectedFlow(final Flow flow) {
        DialogHelper.showDialog(getActivity(), getString(R.string.request_delete_selected_flow), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, final int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    flow.getFile().delete();
                    fillAdapter();
                }
            }
        });
    }

    public void setFlowListener(final NavigationDelegate flowListener) {
        mFlowListener = flowListener;
    }

    public void setBoardType(final Node.Type board) {
        mBoard = board;
    }
}
