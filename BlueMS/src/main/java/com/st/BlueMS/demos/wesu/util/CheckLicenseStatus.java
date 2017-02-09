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

package com.st.BlueMS.demos.wesu.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.Config.STWeSU.RegisterDefines;
import com.st.BlueSTSDK.Config.STWeSU.RegisterDefines.RegistersName;
import com.st.BlueSTSDK.ConfigControl;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.DemosActivity;

/**
 * Class that read the license register and notify the read result thought a callback
 */
public class CheckLicenseStatus {

    /**
     * Build a CheckLicenseStatus that read the license status and show a Snake bar if the license
     * is not present.
     * the snack bar will have a button for open the license manager
     * @param activity acivity used to open the license manager
     * @param rootView demo root view, used for the snackbar
     * @param node node where read the register
     * @param register register to read
     * @param noLicMessage message to show if the license is not present
     */
    public static void checkLicenseRegister(DemosActivity activity, View rootView,@NonNull Node node,
                                            RegisterDefines.RegistersName register,@StringRes int noLicMessage){
        new CheckLicenseStatus(node.getConfigRegister(), register, (regAddress, licIsPresent) -> {
            if(!licIsPresent){
                Snackbar.make(rootView,noLicMessage,Snackbar.LENGTH_INDEFINITE)
                        .setAction("License", view -> activity.startLicenseManagerActivity(node))
                        .show();
            }
        });
    }

    /**
     * Callback interface
     */
    public interface LicenseStatusReadEvent{

        /**
         * function called when have the read results
         * @param regAddress read register
         * @param licStatus true if the license is present and correct, false otherwise
         */
        void onLicenseStatusRead(RegistersName regAddress,boolean licStatus );
    }

    /**
     * callback object
     */
    private @Nullable LicenseStatusReadEvent mLicenseStatusEventNotify;

    /**
     * register read
     */
    private RegistersName mReadReg;

    /**
     * Config register callback, check if the register is the one that we request and parse the
     * result data
     */
    private ConfigControl.ConfigControlListener mConfigControl = new ConfigControl.ConfigControlListener() {
        @Override
        public void onRegisterReadResult(ConfigControl control, Command cmd, int error) {
            if (cmd.getRegister().getAddress() == mReadReg.getRegister().getAddress()) {
                boolean licIsPresent = (cmd.getData()[1])==1;

                control.removeConfigListener(mConfigControl);

                if (mLicenseStatusEventNotify != null){
                    mLicenseStatusEventNotify.onLicenseStatusRead( mReadReg, licIsPresent);
                }//if not null
            }//if the register
        }

        @Override
        public void onRegisterWriteResult(ConfigControl control, Command cmd, int error) {   }

        @Override
        public void onRequestResult(ConfigControl control, Command cmd, boolean success) {   }
    };


    /**
     *
     * @param config
     * @param reg
     * @param callback
     */
    public CheckLicenseStatus(@Nullable ConfigControl config, RegistersName reg,
                              @Nullable LicenseStatusReadEvent callback) {
        mLicenseStatusEventNotify = callback;
        mReadReg=reg;

        if (config != null) {
            config.addConfigListener(mConfigControl);
            Command cmd = new Command(reg.getRegister(), Register.Target.SESSION);
            config.read(cmd);
        }
    }

}
