package com.st.BlueMS.demos.SDLog;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class FeatureListViewAdapter extends
        RecyclerView.Adapter<FeatureListViewAdapter.ViewHolder> {

    public interface FeatureListCallback {
        void onSelect(Feature f);

        void onDeSelect(Feature f);
    }

    private List<Feature> mAvailableFeature;
    private Set<Feature> mSelectedFeature;
    private @Nullable
    FeatureListCallback mCallback;

    FeatureListViewAdapter(List<Feature> items, @Nullable FeatureListCallback callback) {
        mAvailableFeature = new ArrayList<>(items.size());
        mCallback = callback;
        for (Feature f : items)
            if (f.isEnabled())
                mAvailableFeature.add(f);

        mSelectedFeature = new HashSet<>(mAvailableFeature.size());
    }//NodeRecyclerViewAdapter

    void setSelectedFeature(Set<Feature> features) {
        mSelectedFeature.addAll(features);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feature_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        //enable for the new ones
        final Feature f = mAvailableFeature.get(position);
        holder.mFeature = f;
        holder.mFeatureNameLabel.setText(f.getName());
        holder.mEnableLogButton.setChecked(
                mSelectedFeature.contains(f));
    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return mAvailableFeature.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView mFeatureNameLabel;
        final CompoundButton mEnableLogButton;

        Feature mFeature;

        ViewHolder(View view) {
            super(view);

            mFeatureNameLabel = view.findViewById(R.id.log_featureNameLabel);
            mEnableLogButton = view.findViewById(R.id.log_enableButton);
            mEnableLogButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mSelectedFeature.add(mFeature);
                    if (mCallback != null)
                        mCallback.onSelect(mFeature);
                } else {
                    mSelectedFeature.remove(mFeature);
                    if (mCallback != null)
                        mCallback.onDeSelect(mFeature);
                }
            });
            mFeature = null;
        }
    }

}
