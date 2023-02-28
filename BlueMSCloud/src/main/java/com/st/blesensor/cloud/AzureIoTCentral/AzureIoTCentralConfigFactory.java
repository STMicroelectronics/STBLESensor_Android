/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.blesensor.cloud.AzureIoTCentral;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.st.blesensor.cloud.CloudIotClientConfigurationFactory;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNotEmpty;
import com.st.blesensor.cloud.R;

public class AzureIoTCentralConfigFactory implements CloudIotClientConfigurationFactory {
    private static final String CLOUD_NAME= "Azure IoTCentralClient";

    private static final String CONF_PREFERENCE = AzureIoTCentralConfigFactory.class.getCanonicalName();
    private static final String SCOPE_ID_KEY = CONF_PREFERENCE+".SCOPE_ID_KEY";
    private static final String MASTER_KEY = CONF_PREFERENCE+".NOTE_TYPE_KEY";
    private static final String DEVICE_ID_KEY = CONF_PREFERENCE+".DEVICE_ID_KEY";

    private static final Uri CREATE_APP_PAGE = Uri.parse("https://azure.microsoft.com/en-us/services/iot-central/");

    private EditText mScopeIdText;
    private EditText mMasterKeyText;
    private EditText mDeviceIdText;

    /**
     * if present load the organization key and the auth key,device type and device id from the app
     *  preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mScopeIdText.setText(pref.getString(SCOPE_ID_KEY,null));
        mMasterKeyText.setText(pref.getString(MASTER_KEY,null));
        mDeviceIdText.setText(pref.getString(DEVICE_ID_KEY,null));
    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        pref.edit()
                .putString(SCOPE_ID_KEY,mScopeIdText.getText().toString())
                .putString(MASTER_KEY,mMasterKeyText.getText().toString())
                .putString(DEVICE_ID_KEY,mDeviceIdText.getText().toString())
                .apply();
    }

    private static SharedPreferences getShearedPreference(Context c){
        return c.getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE);
    }

    private void openIoTCentralSite(Context c){
            try {
                c.startActivity(new Intent(Intent.ACTION_VIEW, CREATE_APP_PAGE));
            }catch (ActivityNotFoundException e) {
                Log.d("IoTCentral",e.getMessage());
            }
    }

    @Override
    public void attachParameterConfiguration(@NonNull FragmentManager fm, ViewGroup root, @Nullable String id_mcu,@Nullable String fw_version) {
        LayoutInflater inflater = LayoutInflater.from(root.getContext());
        View v = inflater.inflate(R.layout.cloud_config_azure_iotcentral,root);

        TextInputLayout scopeIdLayout = v.findViewById(R.id.azure_iotCentral_scopeIdWrapper);
        mScopeIdText = scopeIdLayout.getEditText();
        mScopeIdText.addTextChangedListener(
                new CheckNotEmpty(scopeIdLayout,R.string.azure_iotCentral_scopeIdEmptyError));

        TextInputLayout masterKeyTextLayout = v.findViewById(R.id.azure_iotCentral_masterKeyWrapper);
        mMasterKeyText = masterKeyTextLayout.getEditText();
        mMasterKeyText.addTextChangedListener(
                new CheckNotEmpty(masterKeyTextLayout,R.string.azure_iotCentral_masterKeyEmptyError));

        TextInputLayout deviceIdTextLayout = v.findViewById(R.id.azure_iotCentral_deviceIdWrapper);
        mDeviceIdText = deviceIdTextLayout.getEditText();
        mDeviceIdText.addTextChangedListener(
                new CheckNotEmpty(deviceIdTextLayout,R.string.azure_iotCentral_deviceIdEmptyError));

        v.findViewById(R.id.azure_iotCentral_createAppButton).setOnClickListener(
                view -> openIoTCentralSite(root.getContext())
        );

        loadFromPreferences(getShearedPreference(root.getContext()));
    }

    @Override
    public void detachParameterConfiguration(@NonNull FragmentManager fm, @NonNull ViewGroup root) {
        root.removeAllViews();
    }

    @Override
    public void loadDefaultParameters(@NonNull FragmentManager fm,@Nullable Node n) {

    }

    @Override
    public String getName() {
        return CLOUD_NAME;
    }

    @Override
    public CloudIotClientConnectionFactory getConnectionFactory(@NonNull FragmentManager fm) throws IllegalArgumentException {
        storeToPreference(getShearedPreference(mDeviceIdText.getContext()));
        return new AzureIotCentralFactory(mScopeIdText.getText().toString(),
                mDeviceIdText.getText().toString(),
                mMasterKeyText.getText().toString());
    }
}
