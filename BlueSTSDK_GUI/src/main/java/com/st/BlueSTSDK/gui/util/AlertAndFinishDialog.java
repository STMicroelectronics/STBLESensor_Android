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
package com.st.BlueSTSDK.gui.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;

/**
 * Display and close close the actvity when the use visualize the message
 */
public class AlertAndFinishDialog extends DialogFragment {

    private static final String TITLE = AlertAndFinishDialog.class.getCanonicalName()+".TITLE";
    private static final String MESSAGE = AlertAndFinishDialog.class.getCanonicalName()+".MESSAGE";
    private static final String KEEP_CONNECTION_OPEN = AlertAndFinishDialog.class.getCanonicalName()
            +".KEEP_CONNECTION_OPEN";


    /**
     * create the dialog
     * @param title dialog title
     * @param msg dialog message
     * @param keepConnectionOpen keep the node connection open also if the activity is stopped
     * @return dialog that display the message and close the activity after the user press the ok button
     */
    public static AlertAndFinishDialog newInstance(String title,String msg,boolean
            keepConnectionOpen) {
        AlertAndFinishDialog frag = new AlertAndFinishDialog();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, msg);
        args.putBoolean(KEEP_CONNECTION_OPEN,keepConnectionOpen);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(TITLE);
        String msg = args.getString(MESSAGE);
        final boolean keepConnectionOpen = args.getBoolean(KEEP_CONNECTION_OPEN,false);

        final ActivityWithNode actvity = (ActivityWithNode)getActivity();

        return new AlertDialog.Builder(actvity)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(R.drawable.ic_warning_24dp)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok,
                        (dialog, id) -> {
                            dialog.dismiss();
                            actvity.keepConnectionOpen(keepConnectionOpen,false);
                            actvity.finish();
                        }).create();
    }


    @TargetApi(23)
    @Override public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        //if (Build.VERSION.SDK_INT >= 23) {
        super.onAttach(context);
        onAttachToContext(context);
        //}
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    private void onAttachToContext(Context context) {
        try {
            ActivityWithNode temp = (ActivityWithNode) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName() + " must extend ActivityWithNode");
        }//try
    }

}