package com.st.trilobyte.ui.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.st.trilobyte.R;
import com.st.trilobyte.models.Flow;

import java.util.ArrayList;
import java.util.List;

public class FlowAdapter extends RecyclerView.Adapter<FlowAdapter.ViewHolder> {

    private List<Flow> mItems;

    private List<Flow> mSelectedItems;

    private OnSelectionChangeListener mListener;

    public FlowAdapter(@NonNull List<Flow> items,
                       @Nullable OnSelectionChangeListener listener) {
        mItems = items;
        mSelectedItems = new ArrayList<>();
        mListener = listener;
    }

    public void setItems(@NonNull List<Flow> items) {
        mItems = items;
        mSelectedItems = new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    public List<Flow> getSelectedItems() {
        return mSelectedItems;
    }

    public void setSelectedItems(final List<Flow> selectedItems) {
        mSelectedItems = selectedItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(
                LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.flow_item, viewGroup, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final Flow item = mItems.get(i);

        viewHolder.mCheckBox.setText(item.getDescription());

        viewHolder.mCheckBox.setOnCheckedChangeListener(null);
        viewHolder.mCheckBox.setChecked(mSelectedItems.contains(item));
        viewHolder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSelectedItems.add(item);
                } else {
                    mSelectedItems.remove(item);
                }
                if (mListener != null) {
                    mListener.onSelectionChange(mSelectedItems);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox mCheckBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCheckBox = itemView.findViewById(R.id.list_item_checkbox);
        }
    }

    public void setListener(@NonNull OnSelectionChangeListener listener) {
        mListener = listener;
    }

    public interface OnSelectionChangeListener {
        void onSelectionChange(@NonNull List<Flow> selection);
    }
}
