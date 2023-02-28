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

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.AccEvent.AccEventFragment;
import com.st.BlueMS.demos.memsSensorFusion.calibration.CalibrationContract;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter view for a list of Feature, each feature has a switch that can be selected or not
 */
public class FeatureListViewAdapter extends
        RecyclerView.Adapter<FeatureListViewAdapter.ViewHolder> {

    /**
     * Interface used to notify when the feature state changed
     */
    public interface OnFeatureSelectChange {
        /**
         * method called when a feature is selected
         * @param f feature selected
         */
        void onFeatureSelect(Feature f);

        /**
         * metho called when a feature is deselected
         * @param f feature deselected
         */
        void onFeatureDeSelect(Feature f);
    }

    private List<Feature> mAvailableFeature;
    private OnFeatureSelectChange mListener;
    private Context mcontex;

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
        mcontex = parent.getContext();
        View view = LayoutInflater.from(mcontex)
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
        if(f instanceof FeatureAccelerationEvent) {
            holder.select_acc_event.setVisibility(View.VISIBLE);
            //create the adapter and attach it to the spinner
            {
                holder.mDetectableEventArrayAdapter = new ArrayAdapter<>(mcontex,
                        android.R.layout.simple_list_item_1,
                        AccEventFragment.getDetectableEvent(node.getType()));
                holder.mEventSelector.setAdapter(holder.mDetectableEventArrayAdapter);

                holder.mEventSelector.setSelection(holder.mDetectableEventArrayAdapter.getPosition(holder.mCurrentEvent));

                //holder.mEventSelector.setOnItemSelectedListener(this);
                holder.mEventSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        holder.mCurrentEvent = (FeatureAccelerationEvent.DetectableEvent) adapterView.getSelectedItem();
                        ((FeatureAccelerationEvent) holder.feature).detectEvent(holder.mCurrentEvent,true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        holder.mCurrentEvent=FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT;

                    }
                });
                holder.mEventSelector.setEnabled(true);
            }
        } else {
            holder.select_acc_event.setVisibility(View.GONE);
        }
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
        public View select_acc_event;
        public Spinner mEventSelector;
        /**
         * current selected event
         */
        public FeatureAccelerationEvent.DetectableEvent mCurrentEvent=FeatureAccelerationEvent.DEFAULT_ENABLED_EVENT;
        /**
         * adapter with the list of supported event
         */
        private ArrayAdapter<FeatureAccelerationEvent.DetectableEvent> mDetectableEventArrayAdapter;

        public ViewHolder(View view) {
            super(view);

            featureNameLabel = (TextView) view.findViewById(R.id.log_featureNameLabel);
            enableLogButton = (CompoundButton) view.findViewById(R.id.log_enableButton);
            /*
            enableLogButton.setOnCheckedChangeListener((compoundButton, status) -> {
                if (status)
                    mListener.onFeatureSelect(feature);
                else
                    mListener.onFeatureDeSelect(feature);
            });
            */
            enableLogButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean status) {
                    if (status)
                        mListener.onFeatureSelect(feature);
                    else
                        mListener.onFeatureDeSelect(feature);
                }
            });
            feature = null;
            select_acc_event = (View) view.findViewById(R.id.select_acc_event_type);
            mEventSelector = (Spinner) view.findViewById(R.id.selected_acc_event_type);
        }//ViewHolder

    }//ViewHolder

}