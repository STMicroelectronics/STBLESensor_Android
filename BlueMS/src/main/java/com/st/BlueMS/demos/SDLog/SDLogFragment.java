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

package com.st.BlueMS.demos.SDLog;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureSDLogging;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fragment showing all the feature available and permit to log multiple feature at the same time
 */
@DemoDescriptionAnnotation(name="SD Logging",iconRes=R.drawable.multiple_log_icon,
        requareAll = FeatureSDLogging.class)
public class SDLogFragment extends DemoFragment implements SDLogContract.View{

    private TextView mHoursValue;
    private TextView mMinuteValue;
    private TextView mSecondsValue;
    private TextView mErrorMessage;
    private RecyclerView mFeatureListView;
    private FeatureListViewAdapter mFeatureListAdapter;
    private ImageButton mStartLogButton;

    private SDLogContract.Presenter mPresenter;

    public SDLogFragment() {
        // Required empty public constructor
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        FeatureSDLogging logFeature = node.getFeature(FeatureSDLogging.class);
        mPresenter = new SDLogPresenter(this, logFeature);
        mPresenter.startDemo();
    }

    public static void hideSoftKeyboard(Context c, View focusView) {
        if(focusView==null)
            return;
        InputMethodManager inputMethodManager =
                (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager!=null)
            inputMethodManager.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        mPresenter.stopDemo();
        Activity currentActivity = getActivity();
        hideSoftKeyboard(currentActivity,currentActivity.getCurrentFocus());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_sd_log, container, false);
        mFeatureListView = root.findViewById(R.id.sdLog_featureList);
        mSecondsValue = root.findViewById(R.id.sdLog_secondsValue);
        mMinuteValue = root.findViewById(R.id.sdLog_minutesValues);
        mHoursValue = root.findViewById(R.id.sdLog_hoursValues);
        mErrorMessage = root.findViewById(R.id.sdLog_errorLabel);
        mStartLogButton = root.findViewById(R.id.sdLog_startButton);
        mStartLogButton.setOnClickListener(view -> mPresenter.onStartStopLogPressed());
        return root;
    }

    private static int parseInt(String str,int defaultValue){
        try{
            return Integer.parseInt(str);
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }

    @Override
    public long getLogInterval(){
        int hours = parseInt(mHoursValue.getText().toString(),0);
        int seconds = parseInt(mSecondsValue.getText().toString(),0);
        int minutes = parseInt(mMinuteValue.getText().toString(),0);
        return (hours*60 + minutes)*60 + seconds;
    }

    @Override
    public void setLogInterval(final long seconds) {
        final int sec = (int) (seconds % 60);
        final int hours = (int) (seconds / (60*60));
        final int minute = (int) (seconds/60 - hours * 60);
        updateGui(() -> {
            mSecondsValue.setText(String.format(Locale.getDefault(),"%d",sec));
            mMinuteValue.setText(String.format(Locale.getDefault(),"%d", minute));
            mHoursValue.setText(String.format(Locale.getDefault(),"%d",hours));
        });
    }

    private void displayErrorView(@StringRes int message){
        updateGui(()->{
            showStartLoggingView();
            mErrorMessage.setText(message);
            mErrorMessage.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void displayNoSDCardErrorLoggingView() {
        displayErrorView(R.string.sdLog_no_sd_error);
    }

    @Override
    public void displayIOErrorLoggingView() {
        displayErrorView(R.string.sdLog_no_sd_error);
    }

    @Override
    public void setSelectedFeature(final Set<Feature> features) {
        if(mFeatureListAdapter!=null)
            updateGui(() -> mFeatureListAdapter.setSelectedFeature(features));

    }

    @Override
    public Set<Feature> getSelectedFeature() {
        if(mFeatureListAdapter==null)
            return  Collections.emptySet();
        return mFeatureListAdapter.getSelectedFeature();
    }

    private void setSelectableFeature(List<Feature> features) {
        mFeatureListAdapter = new FeatureListViewAdapter(features);
        mFeatureListView.setAdapter(mFeatureListAdapter);
    }

    private void setInputEnabled(final boolean isEnabled){
        mHoursValue.setEnabled(isEnabled);
        mSecondsValue.setEnabled(isEnabled);
        mMinuteValue.setEnabled(isEnabled);
        mStartLogButton.setEnabled(isEnabled);
    }

    @Override
    public void displayStopLoggingView() {
        updateGui(() -> {
            setInputEnabled(false);
            mErrorMessage.setVisibility(View.GONE);
            mFeatureListView.setVisibility(View.INVISIBLE);
            mStartLogButton.setEnabled(true);
            mStartLogButton.setImageResource(R.drawable.sd_log_stop);
        });
    }

    @Override
    public void displayStartLoggingView(final List<Feature> availableFeature) {
        updateGui(() -> {
            showStartLoggingView();
            setSelectableFeature(availableFeature);
        });
    }

    private void showStartLoggingView() {
        setInputEnabled(true);
        mFeatureListView.setVisibility(View.VISIBLE);
        mStartLogButton.setEnabled(true);
        mStartLogButton.setImageResource(R.drawable.sd_log_start);
    }

    @Override
    public void displayDisableLoggingView() {
        setInputEnabled(false);
    }


    private static class FeatureListViewAdapter extends
            RecyclerView.Adapter<FeatureListViewAdapter.ViewHolder>{

        private List<Feature> mAvailableFeature;
        private Set<Feature> mSelectedFeature;

        FeatureListViewAdapter(List<Feature> items) {
            mAvailableFeature = new ArrayList<>(items.size());
            for (Feature f: items)
                if(f.isEnabled())
                    mAvailableFeature.add(f);

            mSelectedFeature = new HashSet<>(mAvailableFeature.size());
        }//NodeRecyclerViewAdapter

        Set<Feature> getSelectedFeature(){
            return mSelectedFeature;
        }

        void setSelectedFeature(Set<Feature> features){
            mSelectedFeature = features;
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
                    } else
                        mSelectedFeature.remove(mFeature);
                });
                mFeature=null;
            }
        }

    }
}
