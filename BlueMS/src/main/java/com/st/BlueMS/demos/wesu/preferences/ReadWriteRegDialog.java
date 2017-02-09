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

package com.st.BlueMS.demos.wesu.preferences;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.ConfigControl;
import com.st.BlueSTSDK.Features.Field;

/**
 * Created by claudio iozzia on 30/06/2016.
 */
class ReadWriteRegDialog  {
    private final static String TAG = ReadWriteRegDialog.class.getCanonicalName() ;

    private ConfigControl mConfig;
    private AlertDialog mDlg;
    private Activity mActivity;
    private Switch mSwitch;
    private NumberPicker mAddress;
    private NumberPicker mValueHigh;
    private NumberPicker mValueLow;

    private static String [] getHexValuesItems(){
        String [] valuesArr = new String[256];
        for (int i = 0; i < 256; i++)
            valuesArr[i]= String.format("%02X", i);

        return valuesArr;
    }

    private void configureNumberPicker(NumberPicker np){
        String [] valArr = getHexValuesItems();
        np.setMinValue(0);
        np.setMaxValue(valArr.length -1);
        np.setWrapSelectorWheel(true);
        np.setDisplayedValues(valArr);
    }

     ReadWriteRegDialog(Activity a, ConfigControl config){
         mActivity=a;
         mConfig = config;
         mConfig.addConfigListener(configControl);
         AlertDialog.Builder builder =  new AlertDialog.Builder(a);
         builder.setView(R.layout.low_register_read_write);

         builder.setTitle("Read Write register");

         builder.setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mConfig.removeConfigListener(configControl);
                mDlg.dismiss();
            }
         });
         mDlg = builder.create();
         mDlg.show();

         mSwitch = (Switch)mDlg.findViewById(R.id.switchTargetReg);
         mAddress = (NumberPicker)mDlg.findViewById(R.id.numberPicker1);
         mValueHigh = (NumberPicker)mDlg.findViewById(R.id.numberPicker2);
         mValueLow = (NumberPicker)mDlg.findViewById(R.id.numberPicker3);

         configureNumberPicker(mAddress);
         configureNumberPicker(mValueHigh);
         configureNumberPicker(mValueLow);

         View temp =mDlg.findViewById(R.id.btnRead);
         if(temp!=null)
             temp.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     Register reg = new Register(mAddress.getValue(), 1, mSwitch.isChecked() ? Register.Target.PERSISTENT : Register.Target.SESSION);
                     Command cmdVer = new Command(reg, mSwitch.isChecked() ? Register.Target.PERSISTENT : Register.Target.SESSION);
                     mConfig.read(cmdVer);
                 }
             });

         temp = mDlg.findViewById(R.id.btnWrite);
         if(temp!=null)
         temp.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 long value = ((mValueHigh.getValue() & 0xFF) << 8) + (mValueLow.getValue() & 0xFF);
                 Log.d(TAG, "Register address Write =" + mAddress.getValue() + " value =" + String.format("%04X", value) );
                 Register reg = new Register(mAddress.getValue(), 1, mSwitch.isChecked() ? Register.Target.PERSISTENT : Register.Target.SESSION);
                 Command cmdVer = new Command(reg, mSwitch.isChecked() ? Register.Target.PERSISTENT : Register.Target.SESSION, value, Field.Type.UInt16);
                 mConfig.write(cmdVer);
             }
         });

         mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
             @Override
             public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                 mSwitch.setText(b?R.string.register_sw_target_persistent:R.string.register_sw_target_session);
             }
         });

    }

    private ConfigControl.ConfigControlListener configControl = new ConfigControl.ConfigControlListener() {
        @Override
        public void onRegisterReadResult(ConfigControl config,Command cmd, int error) {
            if (error == 0) {
                if (cmd.getRegister().getAddress() == mAddress.getValue()) {


                    final int val = cmd.getDataShort();
                    Log.d(TAG, "Register address Read=" + mAddress.getValue() + " value =" + String.format("%04X", val) );

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mValueHigh.setValue( (val >> 8) & 0xFF);
                            mValueLow.setValue( (val ) & 0xFF);
                        }
                    });

                }
            }
        }

        @Override
        public void onRegisterWriteResult(ConfigControl config,Command cmd, int error) {
            Log.d(TAG, "Register address Write result=" + cmd.getRegister().getAddress() + " value =" + String.format("%04X", cmd.getDataShort()) + " error =" + error );
        }

        @Override
        public void onRequestResult(ConfigControl config,Command cmd, boolean success) {
            Log.d(TAG, "Register address Request result=" + cmd.getRegister().getAddress() + " success =" + success );
        }
    };
}
