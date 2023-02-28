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

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.st.blesensor.cloud.CloudIotClientConnectionFactory;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;

/**
 * Device twin with a possibility to trigger a firmware upgrade form the cloud
 */
public class UpgradableDevice extends DeviceTwin{

    @VisibleForTesting
    public static final String FW_NAME_KEY="FwName";

    @VisibleForTesting
    public static final String FW_VERSION_KEY="FwVersion";

    private static final RemoteMethod.Param FW_LOCATION_PARAM =new RemoteMethod.Param(RemoteMethod.Type.String,"FwPackageUri");

    @VisibleForTesting
    public static final RemoteMethod FW_UPGRADE_METHOD = new RemoteMethod(
            "Updates device Firmware. Use parameter FwPackageUri to specify the URI of the firmware file",
            "FirmwareUpdate",
            FW_LOCATION_PARAM
    );

    public UpgradableDevice(@NonNull String deviceId) {
        super(deviceId);
        addRemoteMethod(FW_UPGRADE_METHOD);
    }

    /**
     * Set the current firmware version
     * @param version current version
     */
    public void setCurrentFwVersion(@NonNull FwVersionBoard version){
        if(version==null)
            throw new IllegalArgumentException("version must not be null");

        addReportedProperty(FW_NAME_KEY,version.getName());
        String fwVersion = version.getMajorVersion()+"."+version.getMinorVersion()+"."+version.getPatchVersion();
        addReportedProperty(FW_VERSION_KEY,fwVersion);
    }

    /**
     * register a callback when a remote firmware upgrade is called
     * @param connection connection used to invoke the remote update
     * @param callback object where notify the firmware update
     */
    public void addFwUpgradeAvailableCallback(DeviceTwinConnection connection, final CloudIotClientConnectionFactory.FwUpgradeAvailableCallback callback){
        connection.onRemoteMethodCall(this, (method, parm) -> {
            if(method.equals(FW_UPGRADE_METHOD))
                callback.onFwUpgradeAvailable(parm.get(FW_LOCATION_PARAM.name).getAsString());
        });
    }




}
