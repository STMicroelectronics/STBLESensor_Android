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

package com.st.BlueMS.demos.Cloud.AzureIot;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Cloud.AzureIot.util.ConnectionParameters;
import com.st.BlueMS.demos.Cloud.MqttClientConfigurationFactory;
import com.st.BlueMS.demos.Cloud.MqttClientConnectionFactory;
import com.st.BlueMS.demos.Cloud.util.InputChecker.InputChecker;
import com.st.BlueSTSDK.Node;

public class AzureIotConfigFactory implements MqttClientConfigurationFactory {

    private static final String CONF_PREFERENCE = AzureIotConfigFactory.class.getCanonicalName();
    private static final String CONNECTION_STRING = CONF_PREFERENCE+".CONNECTION_STRING";

    private static final String CLOUD_NAME= "Azure IoT";

    private TextView mConnectionStringText;

    @Override
    public void attachParameterConfiguration(Context c, ViewGroup root) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.cloud_config_azure,root);
        mConnectionStringText = v.findViewById(R.id.azure_connectionString);
        TextInputLayout connectionStringLayout = v.findViewById(R.id.azure_connectionStringWrapper);
        mConnectionStringText.addTextChangedListener(
                new ConnectionStringChecker(connectionStringLayout,
                        R.string.cloudLog_azure_connectionStringError));
        //load the last valid connection string
        loadFromPreferences(c.getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE));
    }

    @Override
    public void loadDefaultParameters(@Nullable Node n) { }

    @Override
    public String getName() {
        return CLOUD_NAME;
    }

    @Override
    public MqttClientConnectionFactory getConnectionFactory() throws IllegalArgumentException {
        ConnectionParameters param = ConnectionParameters.parse(mConnectionStringText.getText());
        Context c = mConnectionStringText.getContext();
        storeToPreference(c.getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE));
        return new AzureIotFactory(param);
    }

    private void loadFromPreferences(SharedPreferences pref){
        mConnectionStringText.setText(pref.getString(CONNECTION_STRING,""));
    }

    private void storeToPreference(SharedPreferences pref){
        pref.edit()
                .putString(CONNECTION_STRING,mConnectionStringText.getText().toString())
                .apply();
    }

    /**
     * class used to check that if the user input is a valid connection string or not
     */
    private static class ConnectionStringChecker extends InputChecker {

        ConnectionStringChecker(TextInputLayout textInputLayout,
                                @StringRes int errorMessageId) {
            super(textInputLayout, errorMessageId);
        }

        @Override
        protected boolean validate(String input) {
            return ConnectionParameters.hasValidFormat(input);
        }

    }//ConnectionStringChecker

}
