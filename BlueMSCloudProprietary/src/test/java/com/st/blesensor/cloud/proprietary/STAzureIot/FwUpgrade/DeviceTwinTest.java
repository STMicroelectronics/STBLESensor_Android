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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceTwinTest {

    private static final String DEVICE_ID = "DeviceId";
    private static final String RP_NAME = "name";
    private static final String RP_VALUE = "value";

    private DeviceTwin mDevice;
    private Gson mSerializer;

    @Before
    public void init(){
        mDevice = new DeviceTwin(DEVICE_ID);
        mSerializer = new GsonBuilder()
            //.registerTypeAdapter(DeviceTwin.class,new DeviceTwin.JsonSerializer())
            .create();
    }

    @Test(expected = IllegalArgumentException.class)
    public void deviceIdMustBeNotNull(){
        new DeviceTwin(null);
    }

    @Test
    public void deviceIdIsReturnedByTheGetter(){
        assertEquals(DEVICE_ID,mDevice.getDeviceId());
    }

    @Test
    public void aReportedPropertyCanBeAdded(){
        mDevice.addReportedProperty(RP_NAME,RP_VALUE);
    }

    @Test
    public void anExistingRPCanBeRetreiveByName(){
        mDevice.addReportedProperty(RP_NAME,RP_VALUE);
        assertEquals(RP_VALUE,mDevice.getReportedProperty(RP_NAME));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void returnedPropertyMapIsReadOnly(){
        mDevice.addReportedProperty(RP_NAME,RP_VALUE);

        mDevice.getAllReportedProperty().put("newProp","newValue");
    }


    @Test
    public void returnedPropetyMapContainsAddedProp(){
        mDevice.addReportedProperty(RP_NAME,RP_VALUE);

        assertTrue(mDevice.getAllReportedProperty().containsKey(RP_NAME));
    }

    @Test
    public void aNonExistingPRReturnNull(){
        assertNull(mDevice.getReportedProperty(RP_NAME));
    }


    private static final String METHOD_NAME = "solveAllProblems";
    private static final String METHOD_DESCRIPTION = "this method will solve all your problems";
    private static final RemoteMethod.Param[] METHOD_PARAM = {
            new RemoteMethod.Param(RemoteMethod.Type.String,"firstProblem"),
            new RemoteMethod.Param(RemoteMethod.Type.String,"secondProblem")
    };
    private static final RemoteMethod REMOTE_METHOD = new RemoteMethod(METHOD_DESCRIPTION, METHOD_NAME, METHOD_PARAM);

    @Test(expected = UnsupportedOperationException.class)
    public void returnedRemoteMethodsReadOnly(){
        mDevice.addRemoteMethod(REMOTE_METHOD);

        mDevice.getRemoteMethods().add(new RemoteMethod("desc","name"));
    }

    @Test
    public void returnedRemoteMethodsContainsAddedMethods(){
        mDevice.addRemoteMethod(REMOTE_METHOD);
        assertTrue(mDevice.getRemoteMethods().contains(REMOTE_METHOD));
    }

}