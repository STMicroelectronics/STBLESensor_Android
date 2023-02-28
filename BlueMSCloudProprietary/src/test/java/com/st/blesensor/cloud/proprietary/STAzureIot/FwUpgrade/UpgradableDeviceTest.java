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

package com.st.blesensor.cloud.proprietary.STAzureIot.FwUpgrade;

import com.google.gson.JsonObject;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.blesensor.cloud.CloudIotClientConnectionFactory.FwUpgradeAvailableCallback;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class UpgradableDeviceTest {

    private static final String DEVICE_ID = "deviceId";
    private static final FwVersionBoard VERSION = new FwVersionBoard("fwName","cpuName",1,2,3);

    private UpgradableDevice mDevice;

    @Mock
    private DeviceTwinConnection mConnection;

    @Before
    public void init(){
        mDevice = new UpgradableDevice(DEVICE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void theVersionMustNotBeNull(){
        mDevice.setCurrentFwVersion(null);
    }

    @Test
    public void theDeviceRegisterTheFwNameAsReportedProperty(){
        mDevice.setCurrentFwVersion(VERSION);
        Assert.assertEquals(mDevice.getReportedProperty(UpgradableDevice.FW_NAME_KEY),VERSION.getName());
    }

    @Test
    public void theDeviceRegisterTheFwVersionAsReportedProperty(){
        mDevice.setCurrentFwVersion(VERSION);

        String fwVersion = VERSION.getMajorVersion()+"."+VERSION.getMinorVersion()+"."+VERSION.getPatchVersion();
        Assert.assertEquals(mDevice.getReportedProperty(UpgradableDevice.FW_VERSION_KEY),fwVersion);
    }

    @Test
    public void theDeviceRegisterARemoteMethod(){
        Assert.assertFalse(mDevice.getRemoteMethods().isEmpty());
    }

    @Test
    public void addACallbackRegisterTheCallbackOnTheConnectionObject(){

        mDevice.addFwUpgradeAvailableCallback(mConnection,
                mock(FwUpgradeAvailableCallback.class));

        verify(mConnection).onRemoteMethodCall(eq(mDevice),
                any(DeviceTwinConnection.RemoteMethodInvocation.class));
    }

    private JsonObject packFwLocation(String location){
        JsonObject remoteCallParam = new JsonObject();
        remoteCallParam.addProperty(UpgradableDevice.FW_UPGRADE_METHOD.getParams().get(0).name,location);
        return remoteCallParam;
    }

    private DeviceTwinConnection.RemoteMethodInvocation registerCallback(FwUpgradeAvailableCallback callback){

        ArgumentCaptor<DeviceTwinConnection.RemoteMethodInvocation> remoteCallback =
                ArgumentCaptor.forClass(DeviceTwinConnection.RemoteMethodInvocation.class);

        mDevice.addFwUpgradeAvailableCallback(mConnection,callback);

        verify(mConnection).onRemoteMethodCall(eq(mDevice),remoteCallback.capture());
        return remoteCallback.getValue();
    }

    @Test
    public void whenTheRemoteMethodIsCalledTheCallbackIsInvokated(){

        FwUpgradeAvailableCallback callback =
                mock(FwUpgradeAvailableCallback.class);

        DeviceTwinConnection.RemoteMethodInvocation remoteMethodInvocation = registerCallback(callback);
        String url ="fwPathLocation";

        remoteMethodInvocation.invoke(UpgradableDevice.FW_UPGRADE_METHOD,packFwLocation(url));

        verify(callback).onFwUpgradeAvailable(eq(url));
    }

    @Test
    public void theCallbakIsInvokedOnlyIfTheUpgradeRemoteMethodIsCalled(){
        FwUpgradeAvailableCallback callback =
                mock(FwUpgradeAvailableCallback.class);

        DeviceTwinConnection.RemoteMethodInvocation remoteMethodInvocation = registerCallback(callback);

        RemoteMethod method = new RemoteMethod("desc","name");

        remoteMethodInvocation.invoke(method,packFwLocation("unused"));

        verify(callback,never()).onFwUpgradeAvailable(anyString());

    }

}