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

package com.st.blesensor.cloud.IBMWatson;

import android.content.Context;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.st.blesensor.cloud.CloudIotClientConfigurationFactory;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.blesensor.cloud.R;
import com.st.blesensor.cloud.util.MqttClientUtil;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNotEmpty;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckRegularExpression;

import com.st.BlueSTSDK.Node;

import static com.st.blesensor.cloud.IBMWatson.IBMWatsonUtil.VALID_NAME_CHARACTER;

/**
 *  Object that help to configure the Ibm Watson Iot/BlueMX service, using the quickstart configuration
 */
public class IBMWatsonQuickStartConfigFactory implements CloudIotClientConfigurationFactory {

    private static final String FACTORY_NAME="IBM Watson IoT - Quickstart";
    private EditText mDeviceIdText;
    private Node.Type mNodeType;


    @Override
    public void attachParameterConfiguration(Context c, ViewGroup root) {
        LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.cloud_config_bluemx_quickstart,root);
        mDeviceIdText = v.findViewById(R.id.blueMXQuick_deviceId);
        TextInputLayout deviceIdLayout = v.findViewById(R.id.blueMXQuick_deviceIdWrapper);
        mDeviceIdText.addTextChangedListener(
                new CheckNotEmpty(deviceIdLayout,R.string.cloudLog_watson_deviceIdError));
        mDeviceIdText.addTextChangedListener(
                new CheckRegularExpression(deviceIdLayout,R.string.cloudLog_watson_invalidCharacterError,VALID_NAME_CHARACTER));
    }

    @Override
    public void loadDefaultParameters(@Nullable Node n) {
        if(n==null){
            mNodeType = Node.Type.GENERIC;
            return;
        }//else
        if(mDeviceIdText.getText().length()==0)
            mDeviceIdText.setText(MqttClientUtil.getDefaultCloudDeviceName(n));
        mNodeType=n.getType();
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @Override
    public CloudIotClientConnectionFactory getConnectionFactory() throws IllegalArgumentException {
        return new IBMWatsonQuickStartFactory(mNodeType.name(),mDeviceIdText.getText().toString());
    }
}
