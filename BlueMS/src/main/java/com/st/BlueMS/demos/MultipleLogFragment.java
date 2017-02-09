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

package com.st.BlueMS.demos;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Feature.FeatureListener;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.DemosActivity;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.ArrayList;
import java.util.List;


@DemoDescriptionAnnotation(name="Logging",iconRes=R.drawable.multiple_log_icon)
public class MultipleLogFragment extends DemoFragment {

    private RecyclerView mFeatureListView;
    private FeatureListViewAdapter mFeatureListAdapter;
    public MultipleLogFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        mFeatureListAdapter =  new FeatureListViewAdapter(node.getFeatures());
        mFeatureListView.setAdapter(mFeatureListAdapter);
        DemosActivity activity = (DemosActivity) getActivity();
        activity.startLogging();
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        DemosActivity activity = (DemosActivity) getActivity();
        activity.stopLogging();
        disableAllNotification();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_multiple_log, container, false);
        mFeatureListView =(RecyclerView) root.findViewById(R.id.featureList);
        return root;
    }

    public void disableAllNotification(){
        for(int i =0; i < mFeatureListAdapter.getItemCount();i++){
            final Feature f = mFeatureListAdapter.getItem(i);
            final Node  n= f.getParentNode();
            if(n.isEnableNotification(f)){
                n.disableNotification(f);
                FeatureListViewAdapter.ViewHolder vh = (FeatureListViewAdapter.ViewHolder)
                        mFeatureListView.findViewHolderForAdapterPosition(i);
                if(vh!=null){
                    f.removeFeatureListener(vh.mUpdateFeatureValue);
                }//vh
            }//if
        }//for
    }

    private class FeatureListViewAdapter extends
            RecyclerView.Adapter<FeatureListViewAdapter.ViewHolder>{

        private List<Feature> mAvailableFeature;

        public FeatureListViewAdapter(List<Feature> items) {
            mAvailableFeature = new ArrayList<>(items.size());
            for (Feature f: items)
                if(f.isEnabled())
                    mAvailableFeature.add(f);

        }//NodeRecyclerViewAdapter

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.feature_log_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            if(holder.mFeature!=null)//disable the update for the old feature
                holder.mFeature.removeFeatureListener(holder.mUpdateFeatureValue);
            //enable for the new ones
            final Feature f = mAvailableFeature.get(position);
            final Node node = f.getParentNode();
            holder.mFeature = f;
            holder.mFeatureNameLabel.setText(f.getName());
            if(node.isEnableNotification(f)) {
                holder.mEnableLogButton.setEnabled(node.isEnableNotification(f));
                f.addFeatureListener(holder.mUpdateFeatureValue);
            }
        }//onBindViewHolder

        @Override
        public int getItemCount() {
            return mAvailableFeature.size();
        }


        public Feature getItem(int i){
            return mAvailableFeature.get(i);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public final TextView mFeatureNameLabel;
            public final TextView mValueLabel;
            public final Button mEnableLogButton;
            public final FeatureListener mUpdateFeatureValue= new FeatureListener() {
                @Override
                public void onUpdate(Feature f, Feature.Sample sample) {
                    final String value = f.toString();
                    MultipleLogFragment.this.updateGui(new Runnable() {
                        @Override
                        public void run() {
                            mValueLabel.setText(value);
                        }
                    });
                }
            };
            public Feature mFeature;

            public ViewHolder(View view) {
                super(view);

                mFeatureNameLabel = (TextView) view.findViewById(R.id.featureNameLabel);
                mValueLabel = (TextView) view.findViewById(R.id.featureValueLabel);
                mEnableLogButton = (Button) view.findViewById(R.id.enableLogButton);
                mEnableLogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Node node = mFeature.getParentNode();
                        if(node.isEnableNotification(mFeature)){
                            mFeature.removeFeatureListener(mUpdateFeatureValue);
                            node.disableNotification(mFeature);
                            mValueLabel.setText("");
                        }else{
                            mFeature.addFeatureListener(mUpdateFeatureValue);
                            node.enableNotification(mFeature);
                        }//if-else
                    }//onClick
                });
                mFeature=null;
            }
        }

    }
}
