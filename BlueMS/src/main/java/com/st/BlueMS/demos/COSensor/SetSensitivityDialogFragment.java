package com.st.BlueMS.demos.COSensor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
