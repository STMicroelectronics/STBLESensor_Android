package com.st.trilobyte.ui.fragment.flow_builder;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.st.trilobyte.R;
import com.st.trilobyte.helper.DialogHelper;
import com.st.trilobyte.helper.FileHelperKt;
import com.st.trilobyte.helper.SaveListener;
import com.st.trilobyte.models.Flow;

public class FlowBuilderSaveConfig extends BuilderFragment {

    public static FlowBuilderSaveConfig getInstance() {
        return new FlowBuilderSaveConfig();
    }

    private String mFlowDescription = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_save_flow, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Flow currentFlow = getCurrentFlow();

        mFlowDescription = currentFlow.getDescription();

        if (currentFlow.getDescription() != null && !currentFlow.getDescription().isEmpty()) {
            EditText name = getView().findViewById(R.id.name_edittext);
            name.setText(currentFlow.getDescription());
        }

        if (currentFlow.getNotes() != null) {
            EditText notes = getView().findViewById(R.id.notes_edittext);
            notes.setText(currentFlow.getNotes());
        }

        view.findViewById(R.id.left_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                getActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.finish_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                saveFlow();
            }
        });
    }

    private void saveFlow() {

        Flow currentFlow = getCurrentFlow();

        EditText name = getView().findViewById(R.id.name_edittext);
        if (name.getText().toString().trim().isEmpty()) {
            DialogHelper.showDialog(getActivity(), getString(R.string.error_name_required), null);
            return;
        }

        final String description = name.getText().toString().trim();
        currentFlow.setDescription(description);

        EditText notes = getView().findViewById(R.id.notes_edittext);
        currentFlow.setNotes(notes.getText().toString().trim());

        if (FileHelperKt.hasFlowConflictingName(currentFlow.getDescription())) {
            DialogHelper.showDialog(getActivity(), getString(R.string.error_conflicting_flow_names), getString(R.string.yes), getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        writeFlowOnDisk();
                    }
                }
            });

            return;
        }

        if (mFlowDescription != null && !description.equals(mFlowDescription)) {
            currentFlow.generateId();
        }

        writeFlowOnDisk();
    }

    private void writeFlowOnDisk() {
        FileHelperKt.saveFlow(getCurrentFlow(), new SaveListener() {
            @Override
            public void onSuccess() {
                getActivity().finish();
            }

            @Override
            public void onError() {
                DialogHelper.showDialog(getActivity(), getString(R.string.error_saving_flow_check_external_memory), null);
            }
        });
    }
}

