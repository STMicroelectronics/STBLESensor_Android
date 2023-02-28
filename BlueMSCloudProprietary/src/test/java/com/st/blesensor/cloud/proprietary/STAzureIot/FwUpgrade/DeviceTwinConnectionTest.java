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

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeviceTwinConnectionTest {

    private static final String DEVICE_ID = "deviceID";

    @Mock
    private IMqttAsyncClient mConnection;

    @Mock
    private DeviceTwinConnection.RemoteMethodInvocation mRemoteMethodCallback;

    private DeviceTwin mDevice;


    private DeviceTwinConnection mDeviceConnection;

    @Before
    public void init(){
        mDevice = new DeviceTwin(DEVICE_ID);
        mDeviceConnection = new DeviceTwinConnection(mConnection);
    }


    @Test
    public void syncIsSendingTheReportedProprieties()throws MqttException{
        String topic = "$iothub/twin/PATCH/properties/reported/?$rid=";
        mDevice.addReportedProperty("prop1","value1");

        mDeviceConnection.updateRemoteTwin(mDevice);
        verify(mConnection).publish(startsWith(topic),argThat(payloadContains("prop1","value1")));
    }

    public void syncIsEncodingMethodSignCorrectly() throws MqttException{
        String name = "solveAllProblems";
        String desc = "this method will solve all your problems";
        RemoteMethod.Param[] params = {
                new RemoteMethod.Param(RemoteMethod.Type.String,"firstProblem"),
                new RemoteMethod.Param(RemoteMethod.Type.String,"secondProblem")
        };
        mDevice.addRemoteMethod(new RemoteMethod(name, desc, params));
        String singEncode="\""+name+"--firstProblem-string-secondProblem-string\"";

        verify(mConnection).publish(anyString(),argThat(payloadContains(singEncode,desc)));
    }

    public void syncIsEncodingMethodWithoutParamSignCorrectly() throws MqttException{
        String name = "solveAllProblems";
        String desc = "this method will solve all your problems";
        mDevice.addRemoteMethod(new RemoteMethod(desc, name));
        String singEncode="\""+name+"\"";

        verify(mConnection).publish(anyString(),argThat(payloadContains(singEncode,desc)));
    }


    private static final RemoteMethod SIMPLE_REMOTE_METHOD = new RemoteMethod("methodDesc","methodName");

    private static String getMethodTopic(String methodName){
        return "$iothub/methods/POST/"+methodName;
    }

    @Test
    public void syncSendTheRemoteMethod() throws MqttException{
        mDevice.addRemoteMethod(SIMPLE_REMOTE_METHOD);
        mDeviceConnection.updateRemoteTwin(mDevice);
        verify(mConnection).publish(anyString(),argThat(payloadContains(SIMPLE_REMOTE_METHOD.getName(),
                SIMPLE_REMOTE_METHOD.getDescription())));
    }

    @Test
    public void addMethodCallbackSubscribeToRemoteMethodTopic() throws MqttException {
        mDevice.addRemoteMethod(SIMPLE_REMOTE_METHOD);
        mDeviceConnection.onRemoteMethodCall(mDevice,null);

        verify(mConnection).subscribe(startsWith(getMethodTopic(SIMPLE_REMOTE_METHOD.getName())),eq(0),
                Matchers.any(),any(IMqttActionListener.class),any(IMqttMessageListener.class));
    }

    private ArgumentCaptor<IMqttMessageListener> getMethodCallback(RemoteMethod method) throws MqttException{

        mDevice.addRemoteMethod(method);
        mDeviceConnection.onRemoteMethodCall(mDevice, mRemoteMethodCallback);

        ArgumentCaptor<IMqttMessageListener> methodCallback = ArgumentCaptor.forClass(IMqttMessageListener.class);

        verify(mConnection).subscribe(startsWith(getMethodTopic(SIMPLE_REMOTE_METHOD.getName())),eq(0),
                Matchers.any(),any(IMqttActionListener.class),methodCallback.capture());
        return methodCallback;
    }

    @Test
    public void invokeAMethodWillReturnAn200StatusWithTheSameRequestID() throws Exception {
        ArgumentCaptor<IMqttMessageListener> methodCallback = getMethodCallback(SIMPLE_REMOTE_METHOD);
        String requestID="12abc"; // hex string
        String topic ="$iothub/methods/POST/"+SIMPLE_REMOTE_METHOD.getName()+"/?$rid="+requestID;

        methodCallback.getValue().messageArrived(topic,null);

        verify(mConnection).publish(eq("$iothub/methods/res/200/?$rid="+requestID),any(MqttMessage.class));

    }

    @Test
    public void aRequestWithoutTheRidIsNotManage() throws Exception {
        ArgumentCaptor<IMqttMessageListener> methodCallback = getMethodCallback(SIMPLE_REMOTE_METHOD);

        String topic ="$iothub/methods/POST/"+SIMPLE_REMOTE_METHOD.getName();

        MqttMessage msg = mock(MqttMessage.class);

        methodCallback.getValue().messageArrived(topic,msg);
        verify(mConnection,never()).publish(anyString(),any(MqttMessage.class));

    }



    @Test
    public void aRemoteInvocationTriggerTheLocalCallback() throws Exception {
        ArgumentCaptor<IMqttMessageListener> methodCallback = getMethodCallback(SIMPLE_REMOTE_METHOD);

        String requestID="12abc"; // hex string
        String topic ="$iothub/methods/POST/"+SIMPLE_REMOTE_METHOD.getName()+"/?$rid="+requestID;

        JsonObject methodParam = new JsonObject();
        methodParam.addProperty("param1","value1");
        methodParam.addProperty("param2",10);

        MqttMessage msg = new MqttMessage(methodParam.toString().getBytes());

        methodCallback.getValue().messageArrived(topic,msg);

        verify(mRemoteMethodCallback).invoke(eq(SIMPLE_REMOTE_METHOD),eq(methodParam));
    }


    private static ArgumentMatcher<MqttMessage> payloadContains(final CharSequence... strToMatch){
        return new ArgumentMatcher<MqttMessage>() {
            @Override
            public boolean matches(Object argument) {
                if (!(argument instanceof MqttMessage))
                    return false;

                MqttMessage msg = (MqttMessage)argument;
                String body = new String(msg.getPayload());

                for( CharSequence findMe : strToMatch){
                    if(!body.contains(findMe))
                        return false;
                }
                //all string found
                return true;
            }
        };
    }

    @Test
    public void MqttMessageMatcherTest(){

        MqttMessage msg = new MqttMessage("mqtt message body".getBytes());
        assertTrue(payloadContains("body","message").matches(msg));
        assertFalse(payloadContains("mqtt","Hello").matches(msg));

    }

}