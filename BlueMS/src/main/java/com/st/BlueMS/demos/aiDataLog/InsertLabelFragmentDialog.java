package com.st.BlueMS.demos.aiDataLog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.st.BlueMS.R;

public class InsertLabelFragmentDialog extends DialogFragment {

    public interface OnLabelInsertedCallback{
        void onLabelInserted(String str);
    }

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
