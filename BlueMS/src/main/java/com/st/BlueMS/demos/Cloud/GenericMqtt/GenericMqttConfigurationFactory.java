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

package com.st.BlueMS.demos.Cloud.GenericMqtt;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.st.BlueMS.R;
import com.st.BlueMS.demos.Cloud.CloutIotClientConfigurationFactory;
import com.st.BlueMS.demos.Cloud.CloutIotClientConnectionFactory;
import com.st.BlueMS.demos.Cloud.util.MqttClientUtil;
import com.st.BlueMS.demos.Cloud.util.InputChecker.CheckNumberRange;
import com.st.BlueMS.demos.Cloud.util.InputChecker.CheckRegularExpression;
import com.st.BlueSTSDK.Node;

import java.util.regex.Pattern;

/**
 * Ask the parameters for a generic mqtt broker
 */
public class GenericMqttConfigurationFactory implements CloutIotClientConfigurationFactory {
    private static final String FACTORY_NAME = "Generic MQTT";

    /*
     * pattern to match a an broker url
     */
    private static final Pattern MQTT_URL = Pattern.compile("(ssl|tcp)://[^\\s]+");

    private static final String CONF_PREFERENCE = GenericMqttConfigurationFactory.class.getCanonicalName();
    private static final String BROKER_URL_KEY = CONF_PREFERENCE+".BROKER_URL_KEY";
    private static final String USER_KEY = CONF_PREFERENCE+".USER_KEY";
    private static final String PORT_KEY = CONF_PREFERENCE+".PORT_KEY";
    private static final String CLIENT_ID_KEY = CONF_PREFERENCE+".CLIENT_ID_KEY";;

    private EditText mBrokerUrlText;
    private EditText mUserNameText;
    private EditText mPasswordText;
    private EditText mPortText;
    private EditText mClientIdText;

    /**
     * if present load the previous connection data from the app preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mBrokerUrlText.setText(pref.getString(BROKER_URL_KEY,""));
        mUserNameText.setText(pref.getString(USER_KEY,""));
        mPortText.setText(pref.getString(PORT_KEY,""));
        mClientIdText.setText(pref.getString(CLIENT_ID_KEY,""));
    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        pref.edit()
                .putString(BROKER_URL_KEY, mBrokerUrlText.getText().toString())
                .putString(USER_KEY,mUserNameText.getText().toString())
                .putString(PORT_KEY,mPortText.getText().toString())
                .putString(CLIENT_ID_KEY,mClientIdText.getText().toString())
                .apply();
    }

    @Override
    public void attachParameterConfiguration(Context c, ViewGroup root) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.cloud_config_generic_mqtt,root);

        TextInputLayout brokerUrlLayout = v.findViewById(R.id.genericMqtt_brokerUrlWrapper);
        mBrokerUrlText = brokerUrlLayout.getEditText();
        mBrokerUrlText.addTextChangedListener(
                new CheckRegularExpression(brokerUrlLayout,R.string.cloudLog_genericMqtt_brokerUrlError,MQTT_URL));

        TextInputLayout userLayout = v.findViewById(R.id.genericMqtt_UserWrapper);
        mUserNameText = userLayout.getEditText();

        TextInputLayout passwordLayout = v.findViewById(R.id.genericMqtt_passwordWrapper);
        mPasswordText = passwordLayout.getEditText();


        TextInputLayout clientIdLayout = v.findViewById(R.id.genericMqtt_clientIdWrapper);
        mClientIdText = clientIdLayout.getEditText();

        TextInputLayout portLayout = v.findViewById(R.id.genericMqtt_portWrapper);
        mPortText = portLayout.getEditText();
        mPortText.addTextChangedListener(new CheckNumberRange(portLayout,R.string.cloudLog_genericMqtt_portError,
                0,1<<16));

        loadFromPreferences(c.getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE));

    }

    @Override
    public void loadDefaultParameters(@Nullable Node n) {
        if(n==null)
            return;
        if(mClientIdText.getText().length()==0)
            mClientIdText.setText(MqttClientUtil.getDefaultCloudDeviceName(n));
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @Override
    public CloutIotClientConnectionFactory getConnectionFactory() throws IllegalArgumentException {

        storeToPreference(mBrokerUrlText.getContext().getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE));
        return new GenericMqttFactory(mBrokerUrlText.getText().toString(),
                mPortText.getText().toString(),
                mClientIdText.getText().toString(),
                mUserNameText.getText().toString(),
                mPasswordText.getText().toString());
    }
}
