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

package com.st.BlueMS.demos.memsSensorFusion.calibration;


import android.os.Handler;
import androidx.annotation.DrawableRes;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.widget.ImageButton;

import com.st.BlueMS.R;

/**
 * calibration view implementation
 */
public class CalibrationView implements CalibrationContract.View {
    private static final String CALIBRATION_DIALOG_TAG = CalibrationView.class.getCanonicalName()+"mCalibDialog";

    /**
     * fragment manager used to display the dialog
     */
    private FragmentManager mFragmentManager;
    /**
     * button to change when the calibration is done
     */
    private ImageButton mCalibButton;
    /**
     * main thread used to change the gui state
     */
    private Handler mainThread;


    /**
     * @param manager fragment manager to use to add the calibration dialog
     * @param calibButton button that will trigger the calibration
     */
    public  CalibrationView(FragmentManager manager,ImageButton calibButton){
        mFragmentManager = manager;
        mCalibButton = calibButton;
        mainThread =new Handler(mCalibButton.getContext().getMainLooper());
    }

    @Override
    public void showCalibrationDialog() {
        mainThread.post(() -> {
            //check if a dialog is already displayed
            DialogFragment dialog = (DialogFragment)
                    mFragmentManager.findFragmentByTag(CALIBRATION_DIALOG_TAG);
            if(dialog==null) {
                dialog = new CalibrationDialogFragment();
                if(!mFragmentManager.isStateSaved())
                    dialog.show(mFragmentManager, CALIBRATION_DIALOG_TAG);
            }
        });
    }

    @Override
    public void hideCalibrationDialog() {
        mainThread.post(() -> {
            DialogFragment dialog = (DialogFragment)
                    mFragmentManager.findFragmentByTag(CALIBRATION_DIALOG_TAG);
            if(dialog!=null) {
                dialog.dismiss();
            }
        });

    }

    @Override
    public void setCalibrationButtonState(final boolean isCalibrated) {
        mainThread.post(() -> {
            @DrawableRes int imgId = isCalibrated ? R.drawable.calibrated : R.drawable.uncalibrated;
            mCalibButton.setImageResource(imgId);
        });
    }
}
