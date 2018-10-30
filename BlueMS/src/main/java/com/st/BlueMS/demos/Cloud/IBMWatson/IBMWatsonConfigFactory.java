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

package com.st.BlueMS.demos.Cloud.IBMWatson;

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
import com.st.BlueSTSDK.gui.util.InputChecker.CheckRegularExpression;
import com.st.BlueMS.demos.Cloud.util.MqttClientUtil;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNotEmpty;
import com.st.BlueSTSDK.Node;

import static com.st.BlueMS.demos.Cloud.IBMWatson.IBMWatsonUtil.VALID_NAME_CHARACTER;

/**
 *  Object that help to configure the Ibm Watson Iot/BlueMX service
 */
public class IBMWatsonConfigFactory implements CloutIotClientConfigurationFactory {

    private static final String CONF_PREFERENCE = IBMWatsonConfigFactory.class.getCanonicalName();
    private static final String ORGANIZATION_KEY = CONF_PREFERENCE+".ORGANIZATION_KEY";
    private static final String AUTH_KEY = CONF_PREFERENCE+".NOTE_TYPE_KEY";
    private static final String DEVICE_ID_KEY = CONF_PREFERENCE+".DEVICE_ID_KEY";
    private static final String DEVICE_TYPE_KEY = CONF_PREFERENCE+".DEVICE_TYPE_KEY";

    private static final String FACTORY_NAME = "IBM Watson IoT";

    private EditText mDeviceIdText;
    private EditText mDeviceTypeText;
    private EditText mOrganizationText;
    private EditText mAuthTokenText;

    /**
     * if present load the organization key and the auth key,device type and device id from the app
     *  preferences
     * @param pref object where read the preference
     */
    private void loadFromPreferences(SharedPreferences pref){
        mOrganizationText.setText(pref.getString(ORGANIZATION_KEY,""));
        mAuthTokenText.setText(pref.getString(AUTH_KEY,""));
        mDeviceTypeText.setText(pref.getString(DEVICE_TYPE_KEY,""));
        mDeviceIdText.setText(pref.getString(DEVICE_ID_KEY,""));
    }

    /**
     * store the connection parameter into a preference object
     * @param pref object where store the preference
     */
    private void storeToPreference(SharedPreferences pref){
        pref.edit()
                .putString(ORGANIZATION_KEY,mOrganizationText.getText().toString())
                .putString(AUTH_KEY,mAuthTokenText.getText().toString())
                .putString(DEVICE_ID_KEY,mDeviceIdText.getText().toString())
                .putString(DEVICE_TYPE_KEY,mDeviceTypeText.getText().toString())
                .apply();
    }

    /**
     * create the gui and load the previous value
     * @param c context to use for load the view
     * @param root container where add the view
     */
    @Override
    public void attachParameterConfiguration(Context c, ViewGroup root) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.cloud_config_bluemx,root);
        TextInputLayout deviceIdLayout = v.findViewById(R.id.blueMx_deviceIdWrapper);
        mDeviceIdText = deviceIdLayout.getEditText();
        mDeviceIdText.addTextChangedListener(
                new CheckNotEmpty(deviceIdLayout,R.string.cloudLog_watson_deviceIdError));
        mDeviceIdText.addTextChangedListener(
                new CheckRegularExpression(deviceIdLayout,R.string.cloudLog_watson_invalidCharacterError,VALID_NAME_CHARACTER));

        TextInputLayout authTokenLayout = v.findViewById(R.id.blueMx_authTokenWrapper);
        mAuthTokenText = authTokenLayout.getEditText();
        mAuthTokenText.addTextChangedListener(
                new CheckNotEmpty(authTokenLayout,R.string.cloudLog_watson_authTokenError));

        TextInputLayout organizationLayout = v.findViewById(R.id.blueMx_organizationWrapper);
        mOrganizationText = organizationLayout.getEditText();
        mOrganizationText.addTextChangedListener(
                new CheckNotEmpty(organizationLayout,R.string.cloudLog_watson_organizationIDError));

        TextInputLayout deviceTypeLayout =  v.findViewById(R.id.blueMx_deviceTypeWrapper);
        mDeviceTypeText = deviceTypeLayout.getEditText();
        mDeviceTypeText.addTextChangedListener(new CheckNotEmpty(deviceTypeLayout,R.string.cloudLog_watson_deviceTypeError));
        mDeviceTypeText.addTextChangedListener(
                new CheckRegularExpression(deviceTypeLayout,R.string.cloudLog_watson_invalidCharacterError,VALID_NAME_CHARACTER));
        loadFromPreferences(c.getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE));
    }

    /**
     * if not defined by the user it loads the data using the node information
     * @param n node that will send the data to the broker
     */
    @Override
    public void loadDefaultParameters(@Nullable Node n) {
        if(n==null)
            return;
        if(mDeviceTypeText.getText().length()==0)
            mDeviceTypeText.setText(n.getType().name());
        if(mDeviceIdText.getText().length()==0)
            mDeviceIdText.setText(MqttClientUtil.getDefaultCloudDeviceName(n));
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    /**
     * create the object and save the parameters used for the connection
     * @return object for open a connection to the Ibm Watson service
     */
    @Override
    public CloutIotClientConnectionFactory getConnectionFactory() throws IllegalArgumentException {
        storeToPreference(mDeviceIdText.getContext().getSharedPreferences(CONF_PREFERENCE,Context.MODE_PRIVATE));
        return new IBMWatsonFactory(
                mOrganizationText.getText().toString(),
                mAuthTokenText.getText().toString(),
                mDeviceTypeText.getText().toString(),
                mDeviceIdText.getText().toString());
    }

}
