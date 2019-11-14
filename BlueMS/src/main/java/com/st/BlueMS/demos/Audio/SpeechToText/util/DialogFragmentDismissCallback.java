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

package com.st.BlueMS.demos.Audio.SpeechToText.util;

import android.content.DialogInterface;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog fragment that will notify the parent when it will be dismissed or cancelled
 */
public class DialogFragmentDismissCallback extends DialogFragment {

    /**
     * interface that the parent has to implement to be notify
     */
    public interface DialogDismissCallback{
        /**
         * called when the dialog is dismissed or cancelled
         * @param dialog dialog that is dismiss or cancelled
         */
        void onDialogDismiss(DialogFragment dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        if(!notifyParentFragment(this))
            notifyParentActvity(this);
    }//onDismiss

    /**
     * if the object implement the {@link DialogDismissCallback} call the {@link DialogDismissCallback#onDialogDismiss(DialogFragment)}
     * @param target object that can implement the {@link DialogDismissCallback}
     * @param dialog parameter to pass
     * @return true if it call the method, false otherwise
     */
    private boolean invokeCallback(Object target,DialogFragment dialog){
        if (target != null) {
            if (target instanceof DialogDismissCallback) {
                ((DialogDismissCallback) target).onDialogDismiss(dialog);
                return true;
            }
        }
        return false;
    }

    /**
     * notify to the activity that the dialog is dismiss
     * @param dialog dialog that is dismiss
     */
    private boolean notifyParentActvity(DialogFragment dialog) {
        return invokeCallback(getActivity(),dialog);
    }

    /**
     * notify to the fragment that the dialog is dismiss
     * @param dialog dialog that is dismiss
     * @return true if the callback is called
     */
    private boolean notifyParentFragment(DialogFragment dialog) {
        return invokeCallback(getParentFragment(),dialog);
    }
}
