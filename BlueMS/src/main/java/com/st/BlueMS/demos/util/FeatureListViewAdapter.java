/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
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

package com.st.BlueMS.demos.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.List;

public class FeatureListViewAdapter extends
        RecyclerView.Adapter<FeatureListViewAdapter.ViewHolder> {

    public interface OnFeatureSelectChange {
        void onFeatureSelect(Feature f);
        void onFeatureDeSelect(Feature f);
    }

    private List<Feature> mAvailableFeature;
    private OnFeatureSelectChange mListener;

    public FeatureListViewAdapter(List<Feature> items, OnFeatureSelectChange listener) {
        mListener = listener;
        mAvailableFeature = filterEnabledFeatures(items);
    }//NodeRecyclerViewAdapter


    private static List<Feature> filterEnabledFeatures(List<Feature> features) {
        List<Feature> temp = new ArrayList<>(features.size());
        for (Feature f : features)
            if (f.isEnabled())
                temp.add(f);
        return temp;
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
        final Node node = f.getParentNode();
        holder.feature = f;
        holder.featureNameLabel.setText(f.getName());
        holder.enableLogButton.setChecked(node.isEnableNotification(f));
        holder.enableLogButton.setEnabled(holder.enableButton);
    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return mAvailableFeature.size();
    }

    public Feature getItem(int i) {
        return mAvailableFeature.get(i);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public Feature feature;
        public final TextView featureNameLabel;
        public final CompoundButton enableLogButton;
        public boolean enableButton=true;

        public ViewHolder(View view) {
            super(view);

            featureNameLabel = (TextView) view.findViewById(R.id.featureNameLabel);
            enableLogButton = (CompoundButton) view.findViewById(R.id.enableLogButton);
            enableLogButton.setOnCheckedChangeListener((compoundButton, status) -> {
                if (status)
                    mListener.onFeatureSelect(feature);
                else
                    mListener.onFeatureDeSelect(feature);
            });
            feature = null;
        }//ViewHolder

    }//ViewHolder

}