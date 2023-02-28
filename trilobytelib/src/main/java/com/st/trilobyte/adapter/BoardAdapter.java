package com.st.trilobyte.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.trilobyte.R;

import java.util.ArrayList;
import java.util.List;

public class BoardAdapter extends BaseAdapter {

    private List<Node> nodes = new ArrayList<>();

    private BoardListener mBoardListener;

    public BoardAdapter(BoardListener listener) {
        mBoardListener = listener;
    }

    @Override
    public int getCount() {
        return nodes.size();
    }

    @Override
    public Object getItem(final int i) {
        return nodes.get(i);
    }

    @Override
    public long getItemId(final int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {

        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.board_adapter_row, viewGroup, false);
        }

        final Node node = (Node) getItem(i);

        TextView boardName = view.findViewById(R.id.board_name);
        boardName.setText(node.getFriendlyName());

        view.findViewById(R.id.upload_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (mBoardListener != null) {
                    mBoardListener.onBoardSelected(node);
                }
            }
        });

        return view;
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void clear() {
        nodes.clear();
    }

    // interfaces

    public interface BoardListener {
        void onBoardSelected(Node node);
    }
}
