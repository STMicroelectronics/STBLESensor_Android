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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog Fragment that show a message and an ok button
 */
public class SimpleFragmentDialog extends DialogFragment {
    private static final String MESSAGE_ID = "Message_id";
    private static final String TITLE_ID = "title_id";
    private static final String MESSAGE_STR = "Message_str";
    private static final String TITLE_STR = "Message_str";

    public static SimpleFragmentDialog newInstance(@StringRes int message) {
        SimpleFragmentDialog frag = new SimpleFragmentDialog();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_ID, message);
        frag.setArguments(args);
        return frag;
    }


    public static SimpleFragmentDialog newInstance(@StringRes int title,@StringRes int message) {
        SimpleFragmentDialog frag = new SimpleFragmentDialog();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_ID, message);
        args.putInt(TITLE_ID, title);
        frag.setArguments(args);
        return frag;
    }

    public static SimpleFragmentDialog newInstance(@StringRes int title,@NonNull String message) {
        SimpleFragmentDialog frag = new SimpleFragmentDialog();
        Bundle args = new Bundle();
        args.putString(MESSAGE_STR, message);
        args.putInt(TITLE_ID, title);
        frag.setArguments(args);
        return frag;
    }

    public static SimpleFragmentDialog newInstance(@NonNull String message) {
        SimpleFragmentDialog frag = new SimpleFragmentDialog();
        Bundle args = new Bundle();
        args.putString(MESSAGE_STR, message);
        frag.setArguments(args);
        return frag;
    }

    private DialogInterface.OnClickListener mClickListener = null;

    private @Nullable String getMessage(){
        final Bundle args = getArguments();
        if(args!=null && args.containsKey(MESSAGE_ID))
            return getString(args.getInt(MESSAGE_ID));
        if(args!=null && args.containsKey(MESSAGE_STR))
            return args.getString(MESSAGE_STR);
        return null;
    }

    private @Nullable String getTitle(){
        final Bundle args = getArguments();
        if(args!=null && args.containsKey(TITLE_ID))
            return getString(args.getInt(TITLE_ID));
        if(args!=null && args.containsKey(TITLE_STR))
            return args.getString(TITLE_STR);
        return null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getMessage();
        String title = getTitle();
        AlertDialog.Builder dialogBuilder =  new AlertDialog.Builder(requireContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        (dialog, whichButton) ->{
                            if(mClickListener!=null)
                                mClickListener.onClick(dialog,whichButton);
                        }
                )
                .setCancelable(false);
        if(title!=null)
            dialogBuilder.setTitle(title);
        return dialogBuilder.create();
    }


    public void setOnclickListener(DialogInterface.OnClickListener clickListener){
        mClickListener = clickListener;
    }

}
