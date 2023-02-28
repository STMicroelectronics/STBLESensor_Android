package com.st.BlueMS.demos.aiDataLog;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
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

/**
 * fragment that show the list of annotation and start/stop the logging
 */
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

                public boolean onMove(@NonNull RecyclerView recyclerView,
                                      @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return true;// true if moved, false otherwise
                }

                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
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
    public void onAttach(@NonNull Context context) {
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

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity fragmentActivity = requireActivity();
        mAnnotationViewModel = ViewModelProviders.of(fragmentActivity).get(AnnotationLogViewModel.class);
        mLogParametersViewModel = ViewModelProviders.of(fragmentActivity).get(LogParametersViewModel.class);
        mAnnotationViewModel.getAllAnnotation().observe(getViewLifecycleOwner(), annotations -> mAdapter.setAnnotation(annotations));

        mAnnotationViewModel.getIsLogging().observe(getViewLifecycleOwner(), isLogging -> {
            if(isLogging==null)
                return;
            @DrawableRes int icon = isLogging ? R.drawable.ai_log_stop : R.drawable.ai_log_start;
            @StringRes int text = isLogging ? R.string.aiLog_annotation_button_stopLog : R.string.aiLog_annotation_button_startLog;
            mStartStopLogButton.setCompoundDrawablesRelativeWithIntrinsicBounds(getResources().getDrawable(icon),null,null,null);
            mStartStopLogButton.setText(text);
        });

        mAnnotationViewModel.getMissingSDError().observe(getViewLifecycleOwner(), isMissingSd -> {
            if( isMissingSd == null || !isMissingSd)
                return;
            showError(R.string.aiLog_annotation_errorSDMissing);
            mAnnotationViewModel.missSDErrorShown();
        });

        mAnnotationViewModel.getIOError().observe(getViewLifecycleOwner(), hasIOError -> {
            if( hasIOError == null || !hasIOError)
                return;
            showError(R.string.aiLog_annotation_errorIOError);
            mAnnotationViewModel.ioErrorShown();
        });
    }

    private void showError(@StringRes int message) {
        //TODO: use get getChildFragmentManager()
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
        //TODO: use get getChildFragmentManager()
        DialogFragment dialog = InsertLabelFragmentDialog.instantiate(
                str -> mAnnotationViewModel.insert(str)
        );
        FragmentManager manager = getFragmentManager();
        if(manager!=null) {
            dialog.show(manager, INSERT_DIALOG_TAG);
        }
    }

}
