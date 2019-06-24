/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueMS.demos.SDLog;

import android.support.annotation.NonNull;
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feature_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

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
