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
package com.st.BlueMS.demos.aiDataLog;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.aiDataLog.adapter.AnnotationListAdapter;
import com.st.BlueMS.demos.aiDataLog.viewModel.AnnotationLogViewModel;
import com.st.BlueMS.demos.aiDataLog.viewModel.LogParametersViewModel;
import com.st.BlueMS.demos.aiDataLog.viewModel.SelectableAnnotation;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;

import java.util.List;

public class AnnotationListFragment extends Fragment {

    private static final String INSERT_DIALOG_TAG = AnnotationListFragment.class.getName()+".InsertDialog";
    private static final String ERROR_DIALOG_TAG = AnnotationListFragment.class.getName()+".ErrorDialog";

    public static Fragment newInstance(){
        return new AnnotationListFragment();
    }

    private AnnotationListAdapter mAdapter;
    private AnnotationLogViewModel mAnnotationViewModel;
    private LogParametersViewModel mLogParametersViewModel;
    private Button mStartStopLogButton;


    private ItemTouchHelper mSwapToDelete = new ItemTouchHelper(
            new ItemTouchHelper.SimpleCallback(0,
                    ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

                public boolean onMove(RecyclerView recyclerView,
                                      RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return true;// true if moved, false otherwise
                }

                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    int position = viewHolder.getAdapterPosition();
                    List<SelectableAnnotation> allAnnotation =  mAnnotationViewModel.getAllAnnotation().getValue();
                    if(allAnnotation!=null)
                        mAnnotationViewModel.remove(allAnnotation.get(position));
                }

            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_ai_log_list_annotation,container,false);
        RecyclerView annotationList = view.findViewById(R.id.aiLog_annotation_list);
        annotationList.setAdapter(mAdapter);
        mSwapToDelete.attachToRecyclerView(annotationList);
        mStartStopLogButton = view.findViewById(R.id.aiLog_annotation_startButton);
        mStartStopLogButton.setOnClickListener(v ->
                mAnnotationViewModel.startStopLogging(mLogParametersViewModel.getSelectedFeatureMask(),
                mLogParametersViewModel.getEnvironmentalSamplingFrequencyOrDefault(),
                mLogParametersViewModel.getInertialSamplingFrequencyOrDefault(),
                mLogParametersViewModel.getAudioSamplingFrequencyOrDefault())
        );

        Button addLabel = view.findViewById(R.id.aiLog_annotation_addLabelButton);
        addLabel.setOnClickListener(v -> showAddAnnotationDialog());
        addLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_add_accent_24dp,0,0,0);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAdapter = new AnnotationListAdapter(context);
        mAdapter.setOnAnnotationInteractionCallback(new AnnotationListAdapter.AnnotationInteractionCallback() {
            @Override
            public void onAnnotationSelected(SelectableAnnotation selected) {
                mAnnotationViewModel.select(selected);
            }

            @Override
            public void onAnnotationDeselected(SelectableAnnotation deselected) {
                mAnnotationViewModel.deselect(deselected);
            }

            @Override
            public void onRemoved(SelectableAnnotation annotation) {
                mAnnotationViewModel.remove(annotation);
            }
        });

        FragmentActivity fragmentActivity = requireActivity();
        mAnnotationViewModel = ViewModelProviders.of(fragmentActivity).get(AnnotationLogViewModel.class);
        mLogParametersViewModel = ViewModelProviders.of(fragmentActivity).get(LogParametersViewModel.class);
        mAnnotationViewModel.getAllAnnotation().observe(this, annotations -> mAdapter.setAnnotation(annotations));

        mAnnotationViewModel.getIsLogging().observe(this, isLogging -> {
            if(isLogging==null)
                return;
            @DrawableRes int icon = isLogging ? R.drawable.ai_log_stop : R.drawable.ai_log_start;
            @StringRes int text = isLogging ? R.string.aiLog_annotation_button_stopLog : R.string.aiLog_annotation_button_startLog;
            mStartStopLogButton.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(icon),null,null,null);
            mStartStopLogButton.setText(text);
        });

        mAnnotationViewModel.getMissingSDError().observe(this, isMissingSd -> {
            if( isMissingSd == null || !isMissingSd)
                return;
            showError(R.string.aiLog_annotation_errorSDMissing);
            mAnnotationViewModel.missSDErrorShown();
        });

        mAnnotationViewModel.getIOError().observe(this, hasIOError -> {
            if( hasIOError == null || !hasIOError)
                return;
            showError(R.string.aiLog_annotation_errorIOError);
            mAnnotationViewModel.ioErrorShown();
        });

    }

    private void showError(@StringRes int message) {
        FragmentManager manager = getFragmentManager();
        if(manager==null)
            return;
        //another error is displayed
        if (manager.findFragmentByTag(ERROR_DIALOG_TAG)!=null)
            return;

        DialogFragment errorDialog = SimpleFragmentDialog.newInstance(R.string.aiLog_annotation_errorTitle,message);
        errorDialog.show(manager, ERROR_DIALOG_TAG);
    }

    public void showAddAnnotationDialog(){
        DialogFragment dialog = InsertLabelFragmentDialog.instantiate(
                str -> mAnnotationViewModel.insert(str)
        );
        FragmentManager manager = getFragmentManager();
        if(manager!=null) {
            dialog.show(manager, INSERT_DIALOG_TAG);
        }
    }

}
