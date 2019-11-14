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
package com.st.BlueMS.demos.COSensor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.st.BlueMS.R;


public class SetSensitivityDialogFragment extends DialogFragment {

    public interface SetSensitivityDialogFragmentCallback{
        void onNewSensitivity(float sensitivity);
    }

    private static final String DEFAULT_SENSITIVITY_KEY = SetSensitivityDialogFragment.class
            .getCanonicalName()+".DEFAULT_SENSITIVITY_KEY";

    public static DialogFragment newInstance(float defaultSensitivity){

        DialogFragment dialog = new SetSensitivityDialogFragment();

        Bundle data = new Bundle();

        data.putFloat(DEFAULT_SENSITIVITY_KEY,defaultSensitivity);

        dialog.setArguments(data);

        return dialog;
    }


    // Use this instance of the interface to deliver action events
    private SetSensitivityDialogFragmentCallback mListener;
    private EditText mSensitivityInput;


    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        if(ctx instanceof SetSensitivityDialogFragmentCallback){
            mListener = (SetSensitivityDialogFragmentCallback) ctx;
        }else if( getParentFragment() instanceof SetSensitivityDialogFragmentCallback){
            mListener = (SetSensitivityDialogFragmentCallback) getParentFragment();
        }else if( getTargetFragment() instanceof SetSensitivityDialogFragmentCallback){
            mListener = (SetSensitivityDialogFragmentCallback) getTargetFragment();
        }else{
            throw new ClassCastException("One of Activity,Parent Fragment or Target fragment must" +
                    " implement SetSensitivityDialogFragmentCallback");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_set_co_sensitivity, null);
        mSensitivityInput = dialogView.findViewById(R.id.coSensor_sensitivytValue);
        mSensitivityInput.setText(getString(R.string.coSensor_numberFormat,getDefaultSensitivity()));
        builder.setView(dialogView)
                .setTitle(R.string.coSensor_setSensitivityDialogTitle)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> {
                    mListener.onNewSensitivity(getSensitivity());
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

    private float getSensitivity(){
        try{
            return Float.parseFloat(mSensitivityInput.getText().toString());
        }catch (NumberFormatException e){
            return getDefaultSensitivity();
        }
    }

    private float getDefaultSensitivity(){
        return getArguments().getFloat(DEFAULT_SENSITIVITY_KEY);
    }

}
