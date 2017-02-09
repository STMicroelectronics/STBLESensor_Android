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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;

import com.st.BlueMS.R;
import com.st.BlueSTSDK.Config.Command;
import com.st.BlueSTSDK.Config.Register;
import com.st.BlueSTSDK.Config.STWeSU.RegisterDefines;
import com.st.BlueSTSDK.Config.STWeSU.RegisterDefines.RegistersName;
import com.st.BlueSTSDK.ConfigControl;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.preferences.PreferenceFragmentWithNode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * fragment that contains the log preference -> where and how store the data
 */
public class STWeSUDevicePreferenceFragment extends PreferenceFragmentWithNode {

    private final static String TAG = STWeSUDevicePreferenceFragment.class.getCanonicalName() ;

    private SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setPreferenceSummaryTextAndValue(key, sharedPreferences.toString(), null);

        }
    };

    private ConfigControl mConfigService = null;

    private ArrayList<Register> mRegisterToReadPersistent =  new ArrayList<>();
    private ArrayList<Register> mRegisterToReadSession =  new ArrayList<>();


    private ProgressDialog mProgressRead = null;

    private void initializeConfigService(Node n){
        if(n!=null && n.isConnected()) {

            mConfigService = n.getConfigRegister();
            if (mConfigService != null) {

                boolean alreadyReading = mRegisterToReadPersistent.size() > 0 || mRegisterToReadSession.size() > 0;
                if (!alreadyReading && mProgressRead == null ) {
                    mProgressRead = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
                    mProgressRead.setTitle("Reading ");
                    mProgressRead.setMessage("Reading registers");
                    mProgressRead.show();
                }

                mConfigService.addConfigListener(configControl);
                //if


                synchronized (this) {
                    mRegisterToReadPersistent.clear();
                    mRegisterToReadPersistent.add(RegistersName.FW_VER.getRegister());
                    mRegisterToReadPersistent.add(RegistersName.BLE_LOC_NAME.getRegister());
                    mRegisterToReadPersistent.add(RegistersName.BLE_PUB_ADDR.getRegister());
                    mRegisterToReadPersistent.add(RegistersName.LED_CONFIG.getRegister());

                    mRegisterToReadPersistent.add(new Register(0x20, 3, Register.Access.RW, Register.Target.BOTH));
                    //mRegisterToReadPersistent.add(RegistersName.RADIO_TXPWR_CONFIG.getRegister());
                    //mRegisterToReadPersistent.add(RegistersName.PWR_MODE_CONFIG.getRegister());
                    //mRegisterToReadPersistent.add(RegistersName.TIMER_FREQ.getRegister());

                    mRegisterToReadPersistent.add(new Register(0x49, 2, Register.Access.RW, Register.Target.BOTH));
                    //mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURES_MAP.getRegister());
                    //mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURES_MAP.getRegister());

                    mRegisterToReadPersistent.add(new Register(0x74, 8, Register.Access.RW, Register.Target.PERSISTENT));
//                    mRegisterToReadPersistent.add(RegistersName.ACCELEROMETER_CONFIG_FS.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.ACCELEROMETER_CONFIG_ODR.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.MAGNETOMETER_CONFIG_FS.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.MAGNETOMETER_CONFIG_ODR.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GYROSCOPE_CONFIG_FS.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GYROSCOPE_CONFIG_ODR.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.PRESSURE_CONFIG_ODR.getRegister());

                    mRegisterToReadPersistent.add(new Register(0x24, 8, Register.Access.RW, Register.Target.BOTH));
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURE_CTRLS_0080.getRegister());   //aggregate with the below regs
////                    mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURE_CTRLS_0040);
////                    mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURE_CTRLS_0020);
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURE_CTRLS_0010.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURE_CTRLS_0004.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_A_FEATURE_CTRLS_0002.getRegister());

                    mRegisterToReadPersistent.add(new Register(0x35, 8, Register.Access.RW, Register.Target.BOTH));
                    mRegisterToReadPersistent.add(new Register(0x3D, 8, Register.Access.RW, Register.Target.BOTH));
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURE_CTRLS_0001.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURE_CTRLS_0080.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURE_CTRLS_0100.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURE_CTRLS_0200.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURE_CTRLS_0010.getRegister());
//                    mRegisterToReadPersistent.add(RegistersName.GROUP_B_FEATURE_CTRLS_0008.getRegister());

                    mRegisterToReadSession.clear();
                    mRegisterToReadSession.add(RegistersName.LED_CONFIG.getRegister());
                    mRegisterToReadSession.add(RegistersName.RTC_DATE_TIME.getRegister());

                    mRegisterToReadSession.add(new Register(0x21, 2, Register.Access.RW, Register.Target.BOTH));
//                    mRegisterToReadSession.add(RegistersName.TIMER_FREQ.getRegister());
//                    mRegisterToReadSession.add(RegistersName.PWR_MODE_CONFIG.getRegister());

                    mRegisterToReadSession.add(new Register(0x49, 2, Register.Access.RW, Register.Target.BOTH));
//                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURES_MAP.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURES_MAP.getRegister());

                    mRegisterToReadSession.add(new Register(0x24, 8, Register.Access.RW, Register.Target.BOTH));
//                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURE_CTRLS_0080.getRegister());  //aggregate with the below regs
////                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURE_CTRLS_0040);
////                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURE_CTRLS_0020);
//                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURE_CTRLS_0010.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURE_CTRLS_0004.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_A_FEATURE_CTRLS_0002.getRegister());
                    mRegisterToReadSession.add(new Register(0x35, 8, Register.Access.RW, Register.Target.BOTH));
                    mRegisterToReadSession.add(new Register(0x3D, 8, Register.Access.RW, Register.Target.BOTH));
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURE_CTRLS_0001.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURE_CTRLS_0080.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURE_CTRLS_0100.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURE_CTRLS_0200.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURE_CTRLS_0010.getRegister());
//                    mRegisterToReadSession.add(RegistersName.GROUP_B_FEATURE_CTRLS_0008.getRegister());
                    mRegisterToReadSession.add(RegistersName.BLUENRG_INFO.getRegister());
                }
                if (!alreadyReading)
                    ReadNextRegister(500);
            }
        }//if
    }

    private void ReadReg(ArrayList<Register> arr, Register reg){
        synchronized (this){
            arr.add(reg);
        }
    }


    private void ReadNextRegister()
    {
        ReadNextRegister(0);
    }
    private void ReadNextRegister(final int nDelay)
    {
        //Log.d(TAG, "Read Register " + nDelay);
        //start the read after 100ms
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            if (mRegisterToReadSession.isEmpty() && mRegisterToReadPersistent.isEmpty()){
                                if (mProgressRead != null && mProgressRead.isShowing())
                                    mProgressRead.dismiss();
                                mProgressRead = null;
                            }
                            if (!mRegisterToReadSession.isEmpty()) {
                                //Log.d(TAG, "---***Session " + mRegisterToReadSession.get(0).toString());
                                getRegValueFromDeviceSession(mRegisterToReadSession.get(0));
                                mRegisterToReadSession.remove(0);
                                //ReadNextRegister();
                            } else if (!mRegisterToReadPersistent.isEmpty()) {
                                //Log.d(TAG, "---***Persistent " + mRegisterToReadPersistent.get(0).toString());
                                getRegValueFromDevicePersistent(mRegisterToReadPersistent.get(0));
                                mRegisterToReadPersistent.remove(0);
                                //ReadNextRegister();
                            }
                        }
                    }
                }, nDelay);
            }
        });

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_wesu_configuration);
    }

    private void getRegValueFromDevicePersistent(Register reg){
        //Log.d(TAG, "RegisterReadResult" + String.format(" %02X", reg.getAddress()) + " target " +  Register.Target.PERSISTENT);
        Command cmdVer = new Command(reg, Register.Target.PERSISTENT);
        mConfigService.read(cmdVer);
    }
    private void getRegValueFromDeviceSession(Register reg){
        //Log.d(TAG, "RegisterReadResult" + String.format(" %02X", reg.getAddress()) + " target " + Register.Target.SESSION);
        Command cmdVer = new Command(reg, Register.Target.SESSION);
        mConfigService.read(cmdVer);
    }

    private final static int NUM_CLICKs = 5;
    private final static int TIME_OUT_NUM_CLICKs = 2000;
    private int mClicksEggs = NUM_CLICKs; // number of clicks to run the
    private Handler mResetClickEvent = null;

    private Runnable mResetClicks = new Runnable() {
        @Override
        public void run() {
            if (mResetClickEvent != null)
                mResetClickEvent.removeCallbacks(mResetClicks);
            mResetClickEvent = null;
            mClicksEggs = NUM_CLICKs;
        }
    };


    private class FeatureCtrlUpdate implements Preference.OnPreferenceClickListener {

        CheckBox mChkRAM;
        CheckBox mChkUSB;
        CheckBox mChkBLE;
        CheckBox mChkUSART;
        NumberPicker mSubSampling;
        RegistersName mRegName;

        int mValueSession;
        int mValuePersistent;

        void setSessionValue(int val){
            Log.d(TAG, "setSessionValue reg =" + mRegName + " current val =" + val );
            mValueSession = val;
        }
        void setPersistentValue(int val){
            Log.d(TAG, "setPersistentValue reg =" + mRegName + " current val =" + val );
            mValuePersistent = val;
        }

        FeatureCtrlUpdate(PreferenceManager pfm, RegistersName reg){
            mRegName=reg;
            pfm.findPreference("DEVICE_"+mRegName.toString().replace("  "," CTRLS ").replace(" ", "_")+"_S").setOnPreferenceClickListener(FeatureCtrlUpdate.this);
            pfm.findPreference("DEVICE_"+mRegName.toString().replace("  "," CTRLS ").replace(" ", "_")+"_P").setOnPreferenceClickListener(FeatureCtrlUpdate.this);
        }

        @Override
        public boolean onPreferenceClick(final Preference preference) {
            AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());

            int valueToLoad = (preference.getKey().endsWith("_S") ? mValueSession: mValuePersistent);

            Log.d(TAG, "Changing key =" + preference.getKey() + " current value " + valueToLoad );

            builder.setTitle(R.string.featureControlConfigurationTitile);

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View dlgView = inflater.inflate(R.layout.dialog_feature_control_configuration, null);
            builder.setView(dlgView);

            mChkRAM = (CheckBox)dlgView.findViewById(R.id.checkBoxRAM);
            mChkUSB = (CheckBox)dlgView.findViewById(R.id.checkBoxUSB);
            mChkBLE = (CheckBox)dlgView.findViewById(R.id.checkBoxBLE);
            mChkUSART = (CheckBox)dlgView.findViewById(R.id.checkBoxUSART);

            mSubSampling = (NumberPicker)dlgView.findViewById(R.id.numberPicker1);
            mSubSampling.setMinValue(1);
            mSubSampling.setMaxValue(255);
            mSubSampling.setWrapSelectorWheel(true);


            mChkRAM.setChecked((valueToLoad & 0x80)== 0x80);
            mChkUSB.setChecked((valueToLoad & 0x08)== 0x08);
            mChkBLE.setChecked((valueToLoad & 0x04)== 0x04);
            mChkUSART.setChecked((valueToLoad & 0x02)== 0x02);

            mSubSampling.setValue((valueToLoad>>8)&0xFF);

            builder.setPositiveButton(android.R.string.yes, (dialog, which) -> {
                int toSend = ((mSubSampling.getValue() & 0xFF) << 8) +
                        (mChkRAM.isChecked() ? 0x80:0x00) +
                        (mChkUSB.isChecked() ? 0x08:0x00) +
                        (mChkBLE.isChecked() ? 0x04:0x00) +
                        (mChkUSART.isChecked() ? 0x02:0x00);

                Command cmd = new Command(mRegName.getRegister(), (preference.getKey().endsWith("_S") ? Register.Target.SESSION : Register.Target.PERSISTENT), toSend, Field.Type.UInt16);
                mConfigService.write(cmd);
                ReadReg((preference.getKey().endsWith("_S") ? mRegisterToReadSession : mRegisterToReadPersistent),mRegName.getRegister());

            });
            builder.setNegativeButton(android.R.string.cancel, null);

            builder.create().show();

            return false;
        }
    }

    private List<FeatureCtrlUpdate> mListFeatureCtrl = new ArrayList<>();

    private void writeFeatureMapRegister(int val, Register.Target t) {
        Command cmd2 = new Command(RegistersName.GROUP_B_FEATURES_MAP.getRegister(), t, val, Field.Type.Int16);
        mConfigService.write(cmd2);
        ReadReg((t == Register.Target.SESSION )?mRegisterToReadSession :mRegisterToReadPersistent, RegistersName.GROUP_B_FEATURES_MAP.getRegister());
    }

    @Override
    protected void onNodeIsAvailable(Node node){

        Log.d(TAG, "onNodeIsAvailable " + node.getFriendlyName() + " Connection = " + node.isConnected());
        if(node.isConnected()){
            initializeConfigService(node);
        }

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(prefListener);

        getPreferenceManager().findPreference("DEVICE_FW_VERSION").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {


                if (mClicksEggs == 0) {
                    if (mResetClickEvent != null)
                        mResetClickEvent.post(mResetClicks);
                    new ReadWriteRegDialog(getActivity(), mConfigService);
                }
                else if (mResetClickEvent == null) {
                    mResetClickEvent = new Handler(getActivity().getMainLooper());
                    mResetClickEvent.postDelayed(mResetClicks,TIME_OUT_NUM_CLICKs);
                }

                mClicksEggs--;


                return false;
            }
        });

        if (mListFeatureCtrl.size() == 0) {
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_A_FEATURE_CTRLS_0080));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_A_FEATURE_CTRLS_0010));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_A_FEATURE_CTRLS_0004));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_A_FEATURE_CTRLS_0002));
            //mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0001)); //Pedometer
            //mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0080)); //Ahrs
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0100));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0200));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0400));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0010));
            mListFeatureCtrl.add(new FeatureCtrlUpdate(getPreferenceManager(), RegistersName.GROUP_B_FEATURE_CTRLS_0008));
        }
        getPreferenceManager().findPreference("DEVICE_LOCAL_NAME").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                if (newValue != null) {
                    String text = (String) newValue;
                    if (text.length() > 0) {
                        byte[] toSend = new byte[16]; //to align to two byte
                        toSend[0] = 0x09;
                        System.arraycopy(text.getBytes(), 0, toSend, 1, Math.min(text.length(), 15));
                        Command cmd = new Command(RegistersName.BLE_LOC_NAME.getRegister(), Register.Target.PERSISTENT, toSend);
                        mConfigService.write(cmd);
                        ReadReg(mRegisterToReadPersistent,RegistersName.BLE_LOC_NAME.getRegister());
                        showRestartWarning();
                    }
                }
                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_PUB_ADDR").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null) {
                    String text = ((String) newValue).toLowerCase();

                    if (text.matches("[0-9a-f][0-9a-f]:[0-9a-f][0-9a-f]:[0-9a-f][0-9a-f]:[0-9a-f][0-9a-f]:[0-9a-f][0-9a-f]:[0-9a-f][0-9a-f]")) {

                        String[] strArr = text.split(":");
                        byte[] toSend = new byte[strArr.length];

                        if (toSend.length > 0) {
                            for (int i = 0; i < toSend.length; i++)
                                toSend[toSend.length - i - 1] = (byte) (Short.parseShort(strArr[i], 16) & 0xFF);
                            Command cmd = new Command(RegistersName.BLE_PUB_ADDR.getRegister(), Register.Target.PERSISTENT, toSend);
                            mConfigService.write(cmd);
                            ReadReg(mRegisterToReadPersistent,RegistersName.BLE_PUB_ADDR.getRegister());

                            showRestartWarning();

                        }
                    }
                }
                return false;
            }
        });

        getPreferenceManager().findPreference("DEVICE_BLE_OUTPUT_POWER").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue, 16) & 0xFFFF);
                Command cmd = new Command(RegistersName.RADIO_TXPWR_CONFIG.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.RADIO_TXPWR_CONFIG.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_LED_CONFIG_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue, 16) & 0xFFFF);
                Command cmd = new Command(RegistersName.LED_CONFIG.getRegister(), Register.Target.SESSION, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadSession,RegistersName.LED_CONFIG.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_LOW_POWER_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.PWR_MODE_CONFIG.getRegister(), Register.Target.SESSION, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadSession,RegistersName.PWR_MODE_CONFIG.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_TIMER_FREQ_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.TIMER_FREQ.getRegister(), Register.Target.SESSION, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadSession,RegistersName.TIMER_FREQ.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_DATA_READ_GROUP_B_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {



                int val = 0;
                for (String strVal: (HashSet<String>)newValue)
                    val += (Integer.parseInt(strVal,16) & 0xFFFF);

//                if ((val & 0x0180) == 0x0180) {
//                    askCompactExtendedSensorFusion(val, Register.Target.SESSION);
//                }
//                else
                {
                    writeFeatureMapRegister(val, Register.Target.SESSION);
                }

                return false;
            }
        });


        synchronized (this) {
            //Show only available feature
            List<CharSequence> mKeyList = new ArrayList<>();
            List<CharSequence> mValueList = new ArrayList<>();
//        mKeyList.addAll(Arrays.asList(pS.getEntries()));
//        mValueList.addAll(Arrays.asList(pS.getEntries()));
//        List<CharSequence> mKeyListNew= new ArrayList<>();
//        List<CharSequence> mValueListNew= new ArrayList<>();
//            Log.d(TAG, "Start add feature --------------------------------------------------------");
            for (Feature f : node.getFeatures()) {
                addDataGroupBValue(f.getName(), mKeyList, mValueList);
            }

            MultiSelectListPreference pS = (MultiSelectListPreference) getPreferenceManager().findPreference("DEVICE_DATA_READ_GROUP_B_S");
            MultiSelectListPreference pP = (MultiSelectListPreference) getPreferenceManager().findPreference("DEVICE_DATA_READ_GROUP_B_P");
            pS.setEntries(Arrays.copyOf(mKeyList.toArray(), mKeyList.size(), CharSequence[].class));
            pS.setEntryValues(Arrays.copyOf(mValueList.toArray(), mValueList.size(), CharSequence[].class));
            pP.setEntries(Arrays.copyOf(mKeyList.toArray(), mKeyList.size(), CharSequence[].class));
            pP.setEntryValues(Arrays.copyOf(mValueList.toArray(), mValueList.size(), CharSequence[].class));
//            Log.d(TAG, "end add feature --------------------------------------------------------");
        }
        getPreferenceManager().findPreference("DEVICE_DATA_READ_GROUP_A_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                int val = 0;
                for (String strVal: (HashSet<String>)newValue)
                    val += (Integer.parseInt(strVal,16) & 0xFFFF);
                //Log.d(TAG, String.format("Group A Session value sent %04X", val ));
                Command cmd1 = new Command(RegistersName.GROUP_A_FEATURES_MAP.getRegister(), Register.Target.SESSION, val, Field.Type.Int16);
                mConfigService.write(cmd1);
                ReadReg(mRegisterToReadSession,RegistersName.GROUP_A_FEATURES_MAP.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_LED_CONFIG_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = Integer.parseInt((String) newValue, 16);
                Command cmd = new Command(RegistersName.LED_CONFIG.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.LED_CONFIG.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_LOW_POWER_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = Integer.parseInt((String) newValue);
                Command cmd = new Command(RegistersName.PWR_MODE_CONFIG.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.PWR_MODE_CONFIG.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_TIMER_FREQ_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = Integer.parseInt((String) newValue);
                Command cmd = new Command(RegistersName.TIMER_FREQ.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.TIMER_FREQ.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_DATA_READ_GROUP_B_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d(TAG, "Setting DEVICE_DATA_READ_GROUP_B_P To implement");
                int val = 0;
                for (String strVal : (HashSet<String>) newValue)
                    val += (Integer.parseInt(strVal, 16) & 0xFFFF);

//                if ((val & 0x0180) == 0x0180) {
//                    askCompactExtendedSensorFusion(val, Register.Target.PERSISTENT);
//                }
//                else
                {
                    writeFeatureMapRegister(val, Register.Target.PERSISTENT);
                }

                return false;
            }
        });

        getPreferenceManager().findPreference("DEVICE_DATA_READ_GROUP_A_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //Log.d(TAG, "Setting DEVICE_DATA_READ_GROUP_A_P To implement");
                int val = 0;

                for (String strVal: (HashSet<String>)newValue)
                    val += (Integer.parseInt(strVal,16) & 0xFFFF);

                //Log.d(TAG, String.format("Group A Persistent value sent %04X", val));

                Command cmd1 = new Command(RegistersName.GROUP_A_FEATURES_MAP.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd1);
                ReadReg(mRegisterToReadPersistent,RegistersName.GROUP_A_FEATURES_MAP.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_DFU_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                final Command cmd = new Command(RegistersName.DFU_REBOOT.getRegister(), Register.Target.SESSION, val, Field.Type.Int16);
//                mConfigService.write(cmd);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Warning!");
                builder.create();
                builder.setIcon(R.drawable.ic_warning_24dp);
                builder.setMessage("Node needs to be disconnected after the command is sent.\nSend the command and disconnect now?");
                builder.setNegativeButton(android.R.string.no, null);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mConfigService.write(cmd);
                        disconnectDevice();
                    }
                });
                builder.show();

                return false;
            }
        });
        //DEVICE_RTC_TIMER
        getPreferenceManager().findPreference("DEVICE_RTC_TIMER").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("RTC Timer settings");
                builder.create();
                builder.setIcon(R.drawable.ic_rtc_timer_24dp);
                builder.setMessage("Do you want to set the current date time in the node?");
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Read again
                        //ReadReg(mRegisterToReadSession,RegistersName.RTC_DATE_TIME);
                        //ReadNextRegister();
                    }
                });
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRTCTime(mConfigService);
                    }
                });
                builder.show();
                return false;
            }
        });
        getPreferenceManager().findPreference("DEVICE_POWER_OFF_S").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                // confirm before send Reboot with default and shutdown
                if (val == RegisterDefines.PowerOffModes.REBOOT_WITH_DEFAULT.getValue() ||
                        val == RegisterDefines.PowerOffModes.SHUTDOWN.getValue() ||
                        val == RegisterDefines.PowerOffModes.STAND_BY.getValue()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                    builder.setTitle("Warning!");
                    builder.create();
                    builder.setIcon(R.drawable.ic_warning_24dp);
                    int mexID =  R.string.message_shutdown;
                    if (val == RegisterDefines.PowerOffModes.STAND_BY.getValue())
                        mexID = R.string.message_standby;
                    if (val == RegisterDefines.PowerOffModes.REBOOT_WITH_DEFAULT.getValue())
                        mexID = R.string.message_reboot;
                    builder.setMessage(mexID);
                    builder.setNegativeButton(android.R.string.no, null);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendPowerOffCommandAndCloseConnection();
                        }
                    });
                    builder.show();

                } else {
                    sendPowerOffCommandAndCloseConnection();
                }


                return false;
            }
        });

        getPreferenceManager().findPreference("ACCELEROMETER_FS_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.ACCELEROMETER_CONFIG_FS.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.ACCELEROMETER_CONFIG_FS.getRegister());

                return false;
            }
        });

        getPreferenceManager().findPreference("ACCELEROMETER_ODR_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.ACCELEROMETER_CONFIG_ODR.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.ACCELEROMETER_CONFIG_ODR.getRegister());

                return false;
            }
        });

        getPreferenceManager().findPreference("MAGNETOMETER_FS_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.MAGNETOMETER_CONFIG_FS.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.MAGNETOMETER_CONFIG_FS.getRegister());

                return false;
            }
        });

        getPreferenceManager().findPreference("MAGNETOMETER_ODR_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.MAGNETOMETER_CONFIG_ODR.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.MAGNETOMETER_CONFIG_ODR.getRegister());

                return false;
            }
        });
        getPreferenceManager().findPreference("GYROSCOPE_FS_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.GYROSCOPE_CONFIG_FS.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.GYROSCOPE_CONFIG_FS.getRegister());

                return false;
            }
        });

        getPreferenceManager().findPreference("GYROSCOPE_ODR_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.GYROSCOPE_CONFIG_ODR.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.GYROSCOPE_CONFIG_ODR.getRegister());

                return false;
            }
        });

        getPreferenceManager().findPreference("PRESSURE_ODR_P").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int val = (Integer.parseInt((String) newValue) & 0xFFFF);
                Command cmd = new Command(RegistersName.PRESSURE_CONFIG_ODR.getRegister(), Register.Target.PERSISTENT, val, Field.Type.Int16);
                mConfigService.write(cmd);
                ReadReg(mRegisterToReadPersistent,RegistersName.PRESSURE_CONFIG_ODR.getRegister());

                return false;
            }
        });

        //if is null the node is not created -> the configService is initialized by the callback
        //initializeConfigService(node);

    }
    private void addDataGroupBValue(String nameToAdd, List<CharSequence> keyListNew, List<CharSequence> valueListNew) {
//        Log.d(TAG, "Adding data Group B" + nameToAdd);
        String[] arrItem = getResources().getStringArray(R.array.data_read_group_b);
        String[] arrValue = getResources().getStringArray(R.array.data_read_group_b_values);

        if (!nameToAdd.isEmpty()) {
            for (int j = 0; j < arrItem.length; j++) {
                if (arrItem[j].compareTo(nameToAdd) == 0) {
                    keyListNew.add(arrItem[j]);
                    valueListNew.add(arrValue[j]);
//                    Log.d(TAG, "Added data Group B" + arrItem[j] + "Value" + arrValue[j]);
                    break;
                }
            }
        }
    }

    private void showRestartWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Warning!");
        builder.create();
        builder.setIcon(R.drawable.ic_warning_24dp);
        builder.setMessage("Node needs to be restarted to see the changes applied.");
        builder.setPositiveButton(android.R.string.yes, null);
        builder.show();
    }
    private void setRTCTime(ConfigControl cfg){
        //Call this only for WeSUL
        if (cfg != null)
        {
            byte [] dataRtc = new byte[8];

            GregorianCalendar gregorianCalendar = new GregorianCalendar();

            dataRtc[0] = (byte) gregorianCalendar.get(Calendar.HOUR_OF_DAY); //24h hour
            dataRtc[1] = (byte) gregorianCalendar.get(Calendar.MINUTE);
            dataRtc[2] = (byte) gregorianCalendar.get(Calendar.SECOND);
            dataRtc[3] = (byte) gregorianCalendar.get(Calendar.DAY_OF_MONTH);
            dataRtc[4] = (byte) (gregorianCalendar.get(Calendar.MONTH) + 1);
            dataRtc[5] = (byte) (gregorianCalendar.get(Calendar.YEAR) % 100);
            dataRtc[6] = (byte) (((7 + gregorianCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY) % 7) + 1);
            dataRtc[7] = (byte) 0xB3;

            Log.d(TAG, "Date sent :" + SimpleDateFormat.getInstance().format(gregorianCalendar.getTime()));
            Log.d(TAG, "Buffer sent :" + String.format("[%02X, %02X, %02X, %02X, %02X, %02X, %02X, %02X]", dataRtc[0], dataRtc[1], dataRtc[2], dataRtc[3], dataRtc[4], dataRtc[5], dataRtc[6], dataRtc[7]));

            Command cmd = new Command(RegisterDefines.RegistersName.RTC_DATE_TIME.getRegister(), Register.Target.SESSION, dataRtc);
            cfg.write(cmd);
            ReadReg(mRegisterToReadSession,RegistersName.RTC_DATE_TIME.getRegister());
        }
    }

    private void disconnectDevice(){
   /*     mNodeContainer.keepConnectionOpen(false);
        if (NodeSelectedManager.getConnectedNodes().size() > 0 ) // multi connection
            NodeSelectedManager.removeNode(mNodeContainer.getNode());
        else {// single connection
            mNodeContainer.getNode().disconnect();
        }

        NodeSelectedManager.mCloseActivity = true;
        */
        getActivity().finish();
//        NodeSelectedManager.runDelay(new Runnable() {
//            @Override
//            public void run() {
//                mPreferenceActivity.closeActivity();
//            }
//        }, 100);

//        Intent i = new Intent(getActivity(), NodeListActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        getActivity().startActivity(new Intent(getActivity(), NodeListActivity.class));

    }
    private void sendPowerOffCommandAndCloseConnection(){
        disconnectDevice();
    }

    @Override
    public void onPause(){

        if(mConfigService!=null)
            mConfigService.removeConfigListener(configControl);

        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(prefListener);

        if(mListFeatureCtrl != null)
            mListFeatureCtrl.clear();

        super.onPause();
    }

    private void setPreferenceSummaryTextAndValueFeatureCTRL(final String key, int value){
        String txtListOutput = "" +
                ((value & 0x80)==0x80?"RAM ":"") +
                ((value & 0x08)==0x08?"USB ":"") +
                ((value & 0x04)==0x04?"BLE ":"") +
                ((value & 0x02)==0x02?"USART ":"") ;
        String txt ="Output: " + ((txtListOutput.isEmpty())?"None ":txtListOutput) + "- Sub sampling(" + (value>>8 & 0xFF) +")";
        setPreferenceSummaryTextAndValue(key, txt, null);
    }

    private void setPreferenceSummaryTextAndValue(final String key, final String txt,  final Set<String> valuesArr){
        setPreferenceSummaryTextAndValue(key, txt, valuesArr, null);
    }

    private void setPreferenceSummaryTextAndValue(final String key, final String txt,  final Set<String> valuesArr, final String unitStr){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Preference pref = getPreferenceManager().findPreference(key);
                if (pref != null) {
                    if ( unitStr == null)
                        pref.setSummary(txt);
                    else
                        pref.setSummary(txt + " (" + unitStr +")");

                    //pref.setDefaultValue(txt);
                    if ( pref instanceof EditTextPreference)
                        ((EditTextPreference)pref).setText(txt);
                    if (pref instanceof MultiSelectListPreference && valuesArr != null)
                        ((MultiSelectListPreference) pref).setValues(valuesArr);
                    if (pref instanceof ListPreference ) {
                        ListPreference  lp = ((ListPreference) pref);
                        for (int i = 0 ; i < lp.getEntries().length; i++ )
                            if (txt.compareToIgnoreCase((String)(lp.getEntries()[i]))==0) {
                                lp.setValueIndex(i);
                                break;
                            }
                    }
                }
            }
        });
    }

    private ConfigControl.ConfigControlListener configControl = new ConfigControl.ConfigControlListener() {
        @Override
        public void onRegisterReadResult(ConfigControl control,final Command cmd, int error) {
            //Log.d(TAG, "onRegisterReadResult" + cmd.getRegister().getAddress() + " size " + cmd.getRegister().getSize() + "(" + cmd.getData().length + ")" + " ERR= " + error);
            if (error == 0) {
                Register reg = cmd.getRegister();
                byte [] dataRead = cmd.getData();
                if (!reg.equals(RegistersName.BLUENRG_INFO.getRegister()) &&
                        !reg.equals(RegistersName.RTC_DATE_TIME.getRegister()) &&
                        reg.getAddress() >= RegistersName.RADIO_TXPWR_CONFIG.getRegister().getAddress() &&
                        reg.getSize() > 1 &&
                        dataRead.length == reg.getSize() * 2){
                    for (int i = 0; i <cmd.getRegister().getSize(); i++)
                    {
                        byte[] dataReadLocal = new byte[2];
                        dataReadLocal[0] = dataRead[i*2];
                        dataReadLocal[1] = dataRead[i*2 +1];
                        Register regLocal = new Register(reg.getAddress()+i, 1, reg.getAccess(), reg.getTarget());
                        Command cmdLocal = new Command(regLocal,cmd.getTarget(), dataReadLocal);
                        updateRegistersView(cmdLocal);
                    }

                }
                else {
                    updateRegistersView(cmd);
                }
            }
            ReadNextRegister();
        }

        private void updateRegistersView(Command cmd) {
            if (cmd.getRegister().equals(RegistersName.FW_VER.getRegister())) {
                //setUiText(mViewHolder.mTextVersion, "Ver: " + cmd.getData()[0] + "." + cmd.getData()[1]);
                String strVer = String.format("Version: %X.%X.%02X", ((cmd.getData()[1] >> 4) & 0x0F), ((cmd.getData()[1]) & 0x0F), (cmd.getData()[0] & 0xFF));
                setPreferenceSummaryTextAndValue("DEVICE_FW_VERSION", strVer, null);

                //getRegValueFromDevicePersistent(RegisterDefines.RegistersName.BLE_LOC_NAME);
            }

            if (cmd.getRegister().equals(RegistersName.BLUENRG_INFO.getRegister())) {
                //setUiText(mViewHolder.mTextVersion, "Ver: " + cmd.getData()[0] + "." + cmd.getData()[1]);
                //String strVer = String.format("HW: %X.%X.%02X", ((cmd.getData()[1] >> 4) & 0x0F), ((cmd.getData()[1]) & 0x0F), (cmd.getData()[0] & 0xFF));
                String strVer = String.format("HW: %X.%X FW:%X.%X%c", ((cmd.getData()[0] >> 4) & 0x0F), (cmd.getData()[0] & 0x0F), ((cmd.getData()[3] ) & 0xFF), (cmd.getData()[2] >> 4 & 0x0F), (cmd.getData()[2]  & 0x0F) - 1 +'a');
                setPreferenceSummaryTextAndValue("DEVICE_BLUENRG_INFO", strVer, null);

                //getRegValueFromDevicePersistent(RegisterDefines.RegistersName.BLE_LOC_NAME);
            }

            if (cmd.getRegister().equals(RegistersName.BLE_LOC_NAME.getRegister())) {

                setPreferenceSummaryTextAndValue("DEVICE_LOCAL_NAME", String.copyValueOf(cmd.getDataChar(), 1, Math.min(15, cmd.getDataChar().length)).trim(), null);

                //getRegValueFromDevicePersistent(RegisterDefines.RegistersName.BLE_PUB_ADDR);
            }
            if (cmd.getRegister().equals(RegistersName.BLE_PUB_ADDR.getRegister())) {
                String strAddr = "";
                for (int i = 0; i < cmd.getData().length; i++) {
                    strAddr += String.format(i < cmd.getData().length - 1 ? "%02X:" : "%02X", cmd.getData()[cmd.getData().length - i - 1]);
                }
                setPreferenceSummaryTextAndValue("DEVICE_PUB_ADDR", strAddr, null);

                //getRegValueFromDevicePersistent(RegisterDefines.RegistersName.FW_VER);
            }

            if (cmd.getRegister().equals(RegistersName.RADIO_TXPWR_CONFIG.getRegister())) {

                String strToSummary = getSummaryString(R.array.ble_output_power,
                        R.array.ble_output_power_values,
                        String.format("%04X", cmd.getDataShort()));
                setPreferenceSummaryTextAndValue("DEVICE_BLE_OUTPUT_POWER", strToSummary, null);

                //getRegValueFromDevicePersistent(RegisterDefines.RegistersName.BLE_PUB_ADDR);
            }
            if (cmd.getRegister().equals(RegistersName.PWR_MODE_CONFIG.getRegister())) {
                String strLowPower = getSummaryString(R.array.pwr_mode_config,
                        R.array.pwr_mode_config_values,
                        ""+cmd.getDataShort());

                if (cmd.getRegister().getTarget() == Register.Target.SESSION)
                    setPreferenceSummaryTextAndValue("DEVICE_LOW_POWER_S", strLowPower, null);
                else
                    setPreferenceSummaryTextAndValue("DEVICE_LOW_POWER_P", strLowPower, null);
            }

            if (cmd.getRegister().equals(RegistersName.LED_CONFIG.getRegister())) {
                String strLedConfig = getSummaryString(R.array.led_config,
                        R.array.led_config_values,
                        String.format("%02X", cmd.getDataShort() & 0xFF));

                if (cmd.getRegister().getTarget() == Register.Target.SESSION)
                    setPreferenceSummaryTextAndValue("DEVICE_LED_CONFIG_S", strLedConfig, null);
                else
                    setPreferenceSummaryTextAndValue("DEVICE_LED_CONFIG_P", strLedConfig, null);

            }

            if (cmd.getRegister().equals(RegistersName.GROUP_A_FEATURES_MAP.getRegister())) {
                Set<String> strValueMap = getMultiValueString(cmd.getDataShort() & 0xFFFF);
                String strMapConfig = getMultiSummaryString(R.array.data_read_group_a,
                        R.array.data_read_group_a_values, strValueMap, cmd.getDataShort() & 0xFFFF);

                if (cmd.getRegister().getTarget() == Register.Target.SESSION)
                    setPreferenceSummaryTextAndValue("DEVICE_DATA_READ_GROUP_A_S", strMapConfig, strValueMap);
                else
                    setPreferenceSummaryTextAndValue("DEVICE_DATA_READ_GROUP_A_P", strMapConfig, strValueMap);

            }
            if (cmd.getRegister().equals(RegistersName.GROUP_B_FEATURES_MAP.getRegister())) {
                Set<String> strValueMap = getMultiValueString(cmd.getDataShort() & 0xFFFF);
//                String strMapConfig = getMultiSummaryString(R.array.data_read_group_b,
//                        R.array.data_read_group_b_values, strValueMap, cmd.getDataShort() & 0xFFFF);

                String strMapConfig;
                if (cmd.getRegister().getTarget() == Register.Target.SESSION) {
                    strMapConfig = getMultiSummaryString("DEVICE_DATA_READ_GROUP_B_S", strValueMap, cmd.getDataShort() & 0xFFFF);
                    setPreferenceSummaryTextAndValue("DEVICE_DATA_READ_GROUP_B_S", strMapConfig, strValueMap);
                }
                else {
                    strMapConfig = getMultiSummaryString("DEVICE_DATA_READ_GROUP_B_P", strValueMap, cmd.getDataShort() & 0xFFFF);
                    setPreferenceSummaryTextAndValue("DEVICE_DATA_READ_GROUP_B_P", strMapConfig, strValueMap);
                }

            }

            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0001);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0002);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0004);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0008);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0010);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0020);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0040);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0080);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0100);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0200);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0400);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_0800);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_1000);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_2000);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_4000);
            updatePreference(cmd, RegistersName.GROUP_A_FEATURE_CTRLS_8000);

            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0001);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0002);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0004);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0008);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0010);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0020);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0040);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0080);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0100);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0200);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0400);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_0800);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_1000);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_2000);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_4000);
            updatePreference(cmd, RegistersName.GROUP_B_FEATURE_CTRLS_8000);


            if (cmd.getRegister().equals(RegistersName.TIMER_FREQ.getRegister())) {
                String strLowPower = ""+cmd.getDataShort();

                if (cmd.getRegister().getTarget() == Register.Target.SESSION)
                    setPreferenceSummaryTextAndValue("DEVICE_TIMER_FREQ_S", strLowPower, null);
                else
                    setPreferenceSummaryTextAndValue("DEVICE_TIMER_FREQ_P", strLowPower, null);
            }

            if (cmd.getRegister().equals(RegistersName.ACCELEROMETER_CONFIG_FS.getRegister())) {
                String strLowPower = ""+ cmd.getDataShort();
                setPreferenceSummaryTextAndValue("ACCELEROMETER_FS_P", strLowPower, null);
            }
            if (cmd.getRegister().equals(RegistersName.ACCELEROMETER_CONFIG_ODR.getRegister())) {
                String strLowPower = ""+ cmd.getDataShort();
                setPreferenceSummaryTextAndValue("ACCELEROMETER_ODR_P", strLowPower, null);
            }
            if (cmd.getRegister().equals(RegistersName.MAGNETOMETER_CONFIG_FS.getRegister())) {
                String strLowPower = ""+cmd.getDataShort();
                setPreferenceSummaryTextAndValue("MAGNETOMETER_FS_P", strLowPower, null);
            }
            if (cmd.getRegister().equals(RegistersName.MAGNETOMETER_CONFIG_ODR.getRegister())) {
                String strLowPower = ""+cmd.getDataShort();
                setPreferenceSummaryTextAndValue("MAGNETOMETER_ODR_P", strLowPower, null);
            }
            if (cmd.getRegister().equals(RegistersName.GYROSCOPE_CONFIG_FS.getRegister())) {
                String strLowPower = "" +cmd.getDataShort();
                setPreferenceSummaryTextAndValue("GYROSCOPE_FS_P", strLowPower, null);
            }
            if (cmd.getRegister().equals(RegistersName.GYROSCOPE_CONFIG_ODR.getRegister())) {
                String strLowPower = ""+ cmd.getDataShort();
                setPreferenceSummaryTextAndValue("GYROSCOPE_ODR_P", strLowPower, null);
            }
            if (cmd.getRegister().equals(RegistersName.PRESSURE_CONFIG_ODR.getRegister())) {
                String strLowPower = ""+ cmd.getDataShort();
                setPreferenceSummaryTextAndValue("PRESSURE_ODR_P", strLowPower, null);
            }

            if (cmd.getRegister().equals(RegistersName.RTC_DATE_TIME.getRegister())) {
                String strDevTimer = "Not Valid";
                String strRtcConfig = "-";
                byte [] dataArr = cmd.getData();
                if (dataArr != null && dataArr.length >= 8) {
                    if ((dataArr[0] >= 0 && dataArr[0] < 24) && (dataArr[1] >= 0 && dataArr[1] < 60) &&
                            (dataArr[2] >= 0 && dataArr[2] < 60) && (dataArr[3] > 0 && dataArr[3] <= 31) &&
                            (dataArr[4] > 0 && dataArr[4] <= 12) && (dataArr[5] >= 0 && dataArr[5] < 100)) {
                        strDevTimer = String.format("%02d/%02d/%02d - %02d:%02d", dataArr[3], dataArr[4], dataArr[5], dataArr[0], dataArr[1]);
                    }

                    strRtcConfig = getSummaryString(R.array.rtc_timer,
                            R.array.rtc_timer_values,
                            String.format("%02X", dataArr[7] & 0xFF));
                }
                setPreferenceSummaryTextAndValue("DEVICE_RTC_TIMER", strDevTimer + " (" + strRtcConfig + ")", null);

            }
        }

        private void updatePreference(Command cmd, RegistersName regName ){
            if (cmd.getRegister().equals(regName.getRegister())) {
                int val = cmd.getDataShort() & 0xFFFF;

                String prefKey = "DEVICE_"+regName.toString().replace("  "," CTRLS ").replace(" ", "_")+ ((cmd.getRegister().getTarget() == Register.Target.SESSION)?"_S":"_P");
                for (FeatureCtrlUpdate f :mListFeatureCtrl) {
                    if (f.mRegName.getRegister().getAddress() == cmd.getRegister().getAddress()) {
                        if (cmd.getRegister().getTarget() == Register.Target.SESSION)
                            f.setSessionValue(val);
                        else
                            f.setPersistentValue(val);
                        break;
                    }
                }
                //Log.d(TAG, "update register " + prefKey + " Value to changed " + val );
                setPreferenceSummaryTextAndValueFeatureCTRL(prefKey, val);
            }
        }

        private String getSummaryString(int arrayItemID, int valueItemID, String value) {
            String strToSummary = "Not Available";
            String[] arrItem = getResources().getStringArray(arrayItemID);
            String[] arrValue = getResources().getStringArray(valueItemID);
            if (value != null) {
                for (int i = 0; i < arrValue.length; i++) {
                    if (value.compareToIgnoreCase(arrValue[i]) == 0) {
                        if (i < arrItem.length)
                            strToSummary = arrItem[i];
                        break;
                    }
                }
            }
            //only for debug
            if (strToSummary.startsWith("Not"))
                strToSummary += " (" + value +")";
            return strToSummary;
        }

        private String getMultiSummaryString(int arrayItemID, int valueItemID, Set<String> values, int value) {
            String strToSummary = "None";
            String[] arrItem = getResources().getStringArray(arrayItemID);
            String[] arrValue = getResources().getStringArray(valueItemID);

            if (values.size() > 0) {
                strToSummary = "";
                for (String str : values) {
                    for (int j = 0; j < arrValue.length; j++) {
                        if (str.compareToIgnoreCase(arrValue[j]) == 0) {
                            if (j < arrItem.length)
                                strToSummary += (strToSummary.length() > 0 ? ", " : "") + arrItem[j];
                            break;
                        }
                    }
                }
            }
            return strToSummary + " (" + String.format("%04X", value) +")";
        }

        private String getMultiSummaryString(String multiSelectorItemKey, Set<String> values, int value) {
            String strToSummary = "None";
            MultiSelectListPreference pS = (MultiSelectListPreference) getPreferenceManager().findPreference(multiSelectorItemKey);

            CharSequence[] arrItem = pS.getEntries();
            CharSequence[] arrValue = pS.getEntryValues();

            if (values.size() > 0) {
                strToSummary = "";
                for (String str : values) {
                    for (int j = 0; j < arrValue.length; j++) {
                        if (str.compareToIgnoreCase(arrValue[j].toString()) == 0) {
                            if (j < arrItem.length)
                                strToSummary += (strToSummary.length() > 0 ? ", " : "") + arrItem[j];
                            break;
                        }
                    }
                }
            }
            return strToSummary + " (" + String.format("%04X", value) +")";
        }

        private Set<String> getMultiValueString( int value) {

            Set<String> strItemSelected = new HashSet<>();

            if (value != 0) {
                for (int i = 0; i < 16; i++) {
                    if ((value & (1 << i)) != 0) {
                        String valueStr = String.format("%04X", (1 << i));
                        strItemSelected.add(valueStr);
                    }
                }
            }

            return strItemSelected;
        }
        @Override
        public void onRegisterWriteResult(ConfigControl control,Command cmd, int error) {
            //Log.d(TAG, "onRegisterWriteResult" + cmd.getRegister().getAddress() + " size " + cmd.getRegister().getSize() + "("+ cmd.getData().length +")" + " ERR= " + error);
            ReadNextRegister(100);
        }

        @Override
        public void onRequestResult(ConfigControl control,Command cmd, boolean success) {
            //Log.d(TAG, "onRequestResult" + cmd.getRegister().getAddress() + " size " + cmd.getRegister().getSize() + " Success= " + success);
        }
    };
}//LogPreferenceFragment