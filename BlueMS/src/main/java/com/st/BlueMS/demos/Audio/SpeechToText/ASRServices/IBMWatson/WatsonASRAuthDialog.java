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
 * - Neither the apiKey nor trademarks of STMicroelectronics International N.V. nor any other
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

package com.st.BlueMS.demos.Audio.SpeechToText.ASRServices.IBMWatson;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Audio.SpeechToText.util.DialogFragmentDismissCallback;

/**
 * Class which defines a Dialog used for the ASR activation key loading
 */
public class WatsonASRAuthDialog extends DialogFragmentDismissCallback {

    private static View loadView(Context c, @LayoutRes int layout){
        return LayoutInflater.from(c).inflate(layout,null);
    }

    private EditText mApiKeyField;
    private EditText mEndpointField;

    private View buildView(Context c, @Nullable WatsonARSKey key){
        View root = loadView(c,R.layout.dialog_watson_auth);
        EditText apiKeyField = root.findViewById(R.id.dialog_watson_apiKeyValue);
        if(mApiKeyField!=null){
            apiKeyField.setText(mApiKeyField.getText().toString());
        }
        mApiKeyField = apiKeyField;

        EditText endpointField = root.findViewById(R.id.dialog_watson_endpointValue);
        if(mEndpointField !=null){
            endpointField.setText(mEndpointField.getText().toString());
        }
        mEndpointField = endpointField;
        if(key!=null) {
            mApiKeyField.setText(key.apiKey);
            mEndpointField.setText(key.endpoint);
        }
        return root;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = requireContext();
        final View rootView = getActivity().findViewById(android.R.id.content);
        WatsonARSKey currentKey = WatsonARSKey.loadKey(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Watson - Speech To Text");

        builder.setView(buildView(context,currentKey));

        builder.setCancelable(false);

        builder.setPositiveButton(R.string.blueVoice_dialogConfirm, (dialog, which) -> {
            try {
                WatsonARSKey asrKey = new WatsonARSKey(
                        mEndpointField.getText().toString(),
                        mApiKeyField.getText().toString());
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
