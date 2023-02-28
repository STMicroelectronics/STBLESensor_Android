package com.st.BlueMS.demos.aiDataLog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.widget.EditText;

import com.st.BlueMS.R;

/**
 * show a dialog to insert the label for a new label
 */
public class InsertLabelFragmentDialog extends DialogFragment {

    /**
     * callback called when the user press the positive button
     */
    public interface OnLabelInsertedCallback{
        void onLabelInserted(String str);
    }

    /**
     * instantiate the dialog and register the callback
     * @param callback function called when the user close the dialog
     * @return dialog that the user can use to insert a new label
     */
    public static DialogFragment instantiate(OnLabelInsertedCallback callback){
        InsertLabelFragmentDialog dialog = new InsertLabelFragmentDialog();
        dialog.setLabelInsertedListener(callback);
        return dialog;
    }

    private OnLabelInsertedCallback mCallback;

    public void setLabelInsertedListener(OnLabelInsertedCallback callback){
        mCallback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = requireContext();
        EditText labelText = new EditText(context);
        return new AlertDialog.Builder(context)
                .setTitle(R.string.iaLog_insertLabel_title)
                .setView(labelText)
                .setPositiveButton(R.string.iaLog_insertLabel_add,
                        (dialog, whichButton) ->{
                        String newLabel = labelText.getText().toString();
                        if(mCallback!=null && !newLabel.isEmpty()){
                            mCallback.onLabelInserted(newLabel);
                        }
                    }
                )
                .setNegativeButton(R.string.iaLog_insertLabel_cancel,
                        (dialog, whichButton) -> {

                        }
                )
                .create();
    }

}
