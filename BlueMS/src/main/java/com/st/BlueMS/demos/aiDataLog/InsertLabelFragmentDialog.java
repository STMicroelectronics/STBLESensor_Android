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
