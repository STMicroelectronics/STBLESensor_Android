package com.st.trilobyte.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.st.trilobyte.R;
import com.st.trilobyte.models.FlowCategory;

import java.util.ArrayList;
import java.util.List;

public class FlowCategoryAdapter extends BaseAdapter {

    private final List<FlowCategory> mFlowCategories = new ArrayList<>();

    @Override
    public int getCount() {
        return mFlowCategories.size();
    }

    @Override
    public Object getItem(int position) {
        return mFlowCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Context context = parent.getContext();

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.flow_category_adapter_cell,
                    parent, false);
        }

        FlowCategory item = (FlowCategory) getItem(position);

        TextView categoryName = view.findViewById(R.id.flow_category_textview);
        categoryName.setText(item.categoryName);

        return view;
    }

    public void addFlows(List<FlowCategory> categories) {
        mFlowCategories.addAll(categories);
        notifyDataSetChanged();
    }

    public void clear() {
        mFlowCategories.clear();
    }
}
