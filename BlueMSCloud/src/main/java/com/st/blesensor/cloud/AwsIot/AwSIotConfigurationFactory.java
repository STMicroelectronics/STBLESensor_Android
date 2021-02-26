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

package com.st.blesensor.cloud.AwsIot;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.view.ViewGroup;

import com.st.blesensor.cloud.CloudIotClientConfigurationFactory;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.blesensor.cloud.util.MqttClientUtil;
import com.st.BlueSTSDK.Node;


public class AwSIotConfigurationFactory implements CloudIotClientConfigurationFactory {

    private static final String NAME = "AWS IoT";
    private static final String CONFIG_FRAGMENT_TAG = AwsConfigFragment.class.getCanonicalName();

    @Override
    public void attachParameterConfiguration(@NonNull FragmentManager fm, @NonNull ViewGroup root, @Nullable String id_mcu) {

        //check if a fragment is already attach, and remove it to attach the new one
        AwsConfigFragment configFragment = (AwsConfigFragment)fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);


        if(configFragment==null) {
            AwsConfigFragment newFragment = new AwsConfigFragment();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(root.getId(), newFragment, CONFIG_FRAGMENT_TAG);
            transaction.commitNow();
        }
    }

    @Override
    public void detachParameterConfiguration(@NonNull FragmentManager fm, @NonNull ViewGroup root) {
        AwsConfigFragment configFragment = (AwsConfigFragment)fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);
        if(configFragment!=null)
            fm.beginTransaction().remove(configFragment).commit();
    }

    @Override
    public void loadDefaultParameters(@NonNull FragmentManager fm,@Nullable Node n) {
        AwsConfigFragment mConfigFragment = (AwsConfigFragment)fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);
        if(mConfigFragment!=null && n!=null)
            mConfigFragment.setClientId(MqttClientUtil.getDefaultCloudDeviceName(n));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CloudIotClientConnectionFactory getConnectionFactory(@NonNull FragmentManager fm) throws IllegalArgumentException {
        AwsConfigFragment mConfigFragment = (AwsConfigFragment)fm.findFragmentByTag(CONFIG_FRAGMENT_TAG);
        return new AwsIotMqttFactory(mConfigFragment.getClientId(),mConfigFragment.getEndpoint(),
               mConfigFragment.getCertificateFile(),mConfigFragment.getPrivateKeyFile());
    }

}
