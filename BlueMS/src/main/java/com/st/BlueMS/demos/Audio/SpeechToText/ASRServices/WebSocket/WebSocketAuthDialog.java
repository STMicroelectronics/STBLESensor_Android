package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.WebSocket;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.SpeechToText.util.DialogFragmentDismissCallback;


public class WebSocketAuthDialog extends DialogFragmentDismissCallback {

    private static View loadView(Context c, @LayoutRes int layout){
        return LayoutInflater.from(c).inflate(layout,null);
    }

    private EditText mEndpointField;
    private EditText mUserField;
    private EditText mPassword;

    private View buildView(Context c,@Nullable WebSocketParam key){
        View root = loadView(c, R.layout.dialog_websocket_auth);
        mUserField = root.findViewById(R.id.dialog_websocket_userValue);
        mPassword = root.findViewById(R.id.dialog_websocket_passwordValue);
        mEndpointField = root.findViewById(R.id.dialog_websocket_endpointValue);
        if(key!=null) {
            mEndpointField.setText(key.endpoint);
            mUserField.setText(key.user);
            mPassword.setText(key.password);
        }
        return root;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();
        final View rootView = getActivity().findViewById(android.R.id.content);
        WebSocketParam currentKey = WebSocketParam.loadParam(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_websocket_title);

        builder.setView(buildView(context,currentKey));

        builder.setCancelable(false);

        builder.setPositiveButton(R.string.blueVoice_dialogConfirm, (dialog, which) -> {
            try {
                WebSocketParam asrKey = new WebSocketParam(mEndpointField.getText().toString(),
                        mUserField.getText().toString(),
                        mPassword.getText().toString());
                asrKey.store(context);
            }catch (IllegalArgumentException e){
                Snackbar mInvalidKeySnackbar = Snackbar.make(rootView,
                        context.getString(R.string.blueVoice_asrInvalidKey,e.getMessage()), Snackbar.LENGTH_LONG);
                mInvalidKeySnackbar.show();
                dialog.dismiss();
                return;
            }
            Snackbar.make(rootView, R.string.blueVoice_asrKeyInserted, Snackbar.LENGTH_LONG).show();
        });

        builder.setNegativeButton(R.string.blueVoice_dialogBack, null);
        return builder.create();
    }

}
