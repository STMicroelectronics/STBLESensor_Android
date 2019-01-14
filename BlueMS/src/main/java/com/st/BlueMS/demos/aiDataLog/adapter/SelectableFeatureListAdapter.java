package com.st.BlueMS.demos.aiDataLog.adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.aiDataLog.viewModel.SelectableFeature;

import java.util.List;

public class SelectableFeatureListAdapter extends RecyclerView.Adapter<SelectableFeatureListAdapter.ViewHolder> {

    public interface SelectableFeatureListCallback{
        void onSelectItem(CharSequence name);
        void onDeselectItem(CharSequence name);
    }

    private SelectableFeatureListCallback mCallback;
    private List<SelectableFeature> mData;

    public SelectableFeatureListAdapter(@NonNull List<SelectableFeature> data,@NonNull SelectableFeatureListCallback callback){
        mCallback = callback;
        mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ai_log_feature_item,parent,false);
        return new SelectableFeatureListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setValue(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        final CompoundButton item;

        public ViewHolder(View itemView) {
            super(itemView);
            item = itemView.findViewById(R.id.iaLog_selectData_item);
            item.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if(isChecked){
                    mCallback.onSelectItem(item.getText());
                }else{
                    mCallback.onDeselectItem(item.getText());
                }//if-else
            });
        }

        public void setValue(SelectableFeature feature){
            item.setChecked(feature.isSelected);
            item.setText(feature.name);
        }
    }

}
