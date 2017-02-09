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

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.Config.STWeSU.RegisterDefines;
import com.st.BlueSTSDK.ConfigControl;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

/**
 * Created by claudio iozzia on 05/07/2016.
 */
public class CalibrationManagerWesu {
    private static final String TAG = CalibrationManagerWesu.class.getCanonicalName();

    private static final int READ_CALIBRATION_STATUS_TIMEOUT = 5000; //5Seconds

    private boolean mWaitUntilCalibrationComplete =false;
    private boolean mCalibrationStatus = false;
    private Debug mDebug;
    private ConfigControl mConfig;
    private CalibrationEventCallback mCalibEventNotify;
    private Handler mReadConfigRegister;
    private Runnable mReadReg = new Runnable() {
        @Override
        public void run() {
            if (mConfig != null) {
                Command cmd = new Command(RegisterDefines.RegistersName.MOTION_FX_CALIBRATION_LIC_STATUS.getRegister(), Register.Target.SESSION);
                mConfig.read(cmd);
            }
            mReadConfigRegister.postDelayed(mReadReg,READ_CALIBRATION_STATUS_TIMEOUT);
        }
    };

    private ConfigControl.ConfigControlListener mConfigControl = new ConfigControl.ConfigControlListener() {
        @Override
        public void onRegisterReadResult(ConfigControl config, Command cmd, int error) {
            if (cmd.getRegister().getAddress() == RegisterDefines.RegistersName.MOTION_FX_CALIBRATION_LIC_STATUS.getRegister().getAddress()) {
                Log.d(TAG, "Calibration WeSU read calibration status regs");
                if ((cmd.getData()[0]) == 1) { //0x8C session - calibration state (0 mag not calibrated- 1 mag calibrated ) only LSB (FX Calibration Status)
                    mCalibrationStatus = true;
                    if (mCalibEventNotify != null)
                        mCalibEventNotify.onCalibrationStatusChange(true);
                    stopCalibration();
                }
                else if (!mWaitUntilCalibrationComplete){
                    if (mConfig != null)
                        mConfig.removeConfigListener(mConfigControl);

                    if (mReadConfigRegister != null)
                        mReadConfigRegister.removeCallbacks(mReadReg);

                }
            }
        }

        @Override
        public void onRegisterWriteResult(ConfigControl config,Command cmd, int error) {  }

        @Override
        public void onRequestResult(ConfigControl config,Command cmd, boolean success) {  }
    };

    private Debug.DebugOutputListener mDebugListener = new Debug.DebugOutputListener() {
        @Override
        public void onStdOutReceived(Debug debug, String message) {
            Log.d(TAG, message);
            if (message.contains("#MAG_CAL_DONE")) {
                mCalibrationStatus = true;
                if (mCalibEventNotify != null)
                    mCalibEventNotify.onCalibrationStatusChange(true);
                stopCalibration();
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {

        }
    };

    public CalibrationManagerWesu(@NonNull Node n, CalibrationEventCallback event) {
        Log.d(TAG, "Calibration WeSU create and check status");
        mCalibrationStatus = false;
        mCalibEventNotify = event;
        mReadConfigRegister = new Handler(Looper.getMainLooper());
        mDebug = n.getDebug();
        mConfig = n.getConfigRegister();
        mWaitUntilCalibrationComplete = false;

        if (mDebug != null)
            mDebug.addDebugOutputListener(mDebugListener); //compatibility with old wesu

        if (mConfig != null)
            mConfig.addConfigListener(mConfigControl);

        //read the current status
        mReadConfigRegister.post(mReadReg);
    }

    public void stopCalibration(){
        Log.d(TAG, "Calibration WeSU STOP");
        mWaitUntilCalibrationComplete = false;
        if (mDebug != null)
            mDebug.removeDebugOutputListener(mDebugListener);

        if (mConfig != null)
            mConfig.removeConfigListener(mConfigControl);

        if (mReadConfigRegister != null)
            mReadConfigRegister.removeCallbacks(mReadReg);

    }

    public void startCalibration(){
        Log.d(TAG, "Calibration WeSU START");
        mCalibrationStatus = false;
        if (mCalibEventNotify != null)
            mCalibEventNotify.onCalibrationStatusChange(false);

        mWaitUntilCalibrationComplete = true;
        if (mConfig != null) {
            Command cmd = new Command(RegisterDefines.RegistersName.MAGNETOMETER_CALIBRATION_START.getRegister(), Register.Target.PERSISTENT, 3, Field.Type.Int16);
            mConfig.write(cmd);
        }
        mReadConfigRegister.post(mReadReg);


    }

    public boolean isCalibrated(){return mCalibrationStatus;}


    /**
     * callback interface
     */
    public interface CalibrationEventCallback {

        /**
         * call when the calibration state change
         * @param success true if the device is calibrated
         */
        void onCalibrationStatusChange(boolean success);
    }

}
