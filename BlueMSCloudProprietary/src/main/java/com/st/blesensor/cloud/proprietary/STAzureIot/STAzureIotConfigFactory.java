package com.st.blesensor.cloud.proprietary.STAzureIot;
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

import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.st.blesensor.cloud.CloudIotClientConfigurationFactory;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.BlueSTSDK.Node;
import com.st.blesensor.cloud.proprietary.R;
import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.AuthToken;
import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.DeviceData;
import com.st.blesensor.cloud.proprietary.STAzureIot.AuthKey.KeyManager;
import com.st.blesensor.cloud.proprietary.STAzureIot.boardIdConsole.BoardIdConsole;

public class STAzureIotConfigFactory implements CloudIotClientConfigurationFactory {

    private static final String FACTORY_NAME = "Azure IoT - ST Web Dashboard";

    private Handler mMainThread;
    private Node mNode=null;
    private AuthToken mToken;

    private Button mRegisterButton;
    private TextView mRegistrationStatus;
    private ProgressBar mRegistrationProgress;
    private KeyManager mKeyManager;

    @Override
    public void attachParameterConfiguration(@NonNull FragmentManager fm, ViewGroup root, @Nullable String id_mcu,@Nullable String fw_version) {
        Context c = root.getContext();
        mMainThread = new Handler(c.getMainLooper());
        LayoutInflater inflater = LayoutInflater.from(c);
        View v = inflater.inflate(R.layout.cloud_config_st_azure,root);
        mRegisterButton =  v.findViewById(R.id.StAzure_registerButton);
        mRegisterButton.setOnClickListener(view -> loadDeviceData(mNode));
        mRegistrationProgress =  v.findViewById(R.id.StAzure_progress);
        mRegistrationStatus =  v.findViewById(R.id.StAzure_progressText);
        mKeyManager = new KeyManager(c);
    }

    @Override
    public void loadDefaultParameters(@NonNull FragmentManager fm,@Nullable Node n) {
       if(n==null)
           return;

        mNode=n;
        mKeyManager.checkLocalAuthKeyAvailability(new DeviceData(n.getTag()), new KeyManager.KeyManagerCallback() {
            @Override
            public void onAuthKeyGenerated(DeviceData deviceData, AuthToken token) {
                setRegistrationComplete(token);
            }

            @Override
            public void onAuthKeyGeneratedFail(DeviceData deviceData, Throwable error) { }
        });
    }

    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @Override
    public CloudIotClientConnectionFactory getConnectionFactory(@NonNull FragmentManager fm) throws IllegalArgumentException {
        if(mToken==null)
            throw new IllegalArgumentException("Invalid Registration data");
        return new STAzureIotFactory(mToken);
    }

    private void setProgressText(@StringRes final int text){
        mMainThread.post(() -> {
            mRegistrationStatus.setVisibility(View.VISIBLE);
            mRegistrationStatus.setText(text);
        });
    }

    private void showProgressBar(final boolean show){
        mMainThread.post(() -> {
            mRegistrationProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterButton.setVisibility(!show ? View.VISIBLE : View.GONE);
        });
    }

    private void changeVisibility(final View v, final int visibility){
        mMainThread.post(() -> v.setVisibility(visibility));
    }

    private void setRegistrationComplete(AuthToken token){
        setProgressText(R.string.cloudLog_StAzure_registrationComplete);
        mToken=token;
        showProgressBar(false);
        changeVisibility(mRegisterButton, View.GONE);
    }

    private void loadDeviceData(@Nullable final Node node){
        if(node==null)
            return;

        BoardIdConsole console = BoardIdConsole.getBoardIdConsole(node);
        if(console==null) {
            setProgressText(R.string.cloudLog_StAzure_licConsoleMissing);
            return;
        }
        setProgressText(R.string.cloudLog_StAzure_licConsoleFound);
        showProgressBar(true);
        console.readBoardId((console1, uid) -> {

            DeviceData deviceData;
            try {
                deviceData = new DeviceData(uid, node.getTag());
            }catch (IllegalArgumentException e){
                Log.e(FACTORY_NAME,e.getMessage());
                setProgressText(R.string.cloudLog_StAzure_invalidDeviceData);
                showProgressBar(false);
                return;
            }

            setProgressText(R.string.cloudLog_StAzure_nodeRegistration);

            mKeyManager.requestAuthKey(deviceData, new KeyManager.KeyManagerCallback() {
                @Override
                public void onAuthKeyGenerated(DeviceData deviceData, AuthToken token) {
                    setRegistrationComplete(token);
                }

                @Override
                public void onAuthKeyGeneratedFail(DeviceData deviceData, Throwable t) {
                    Log.e(FACTORY_NAME,t.getMessage());
                    setProgressText(R.string.cloudLog_StAzure_registrationFail);
                    showProgressBar(false);
                }
            });
        });

    }

    @Override
    public void detachParameterConfiguration(@NonNull FragmentManager fm, @NonNull ViewGroup root) {
        root.removeAllViews();
        mMainThread=null;
    }
}
