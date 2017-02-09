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

package com.st.BlueMS.demos.cloud;

import com.st.BlueMS.demos.cloud.IBMWatsonQuickStartFactory.MqttFeatureListenerQuickStart;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;



@RunWith(MockitoJUnitRunner.class)
public class MqttFeatureListenerQuickStartTest {

    private static final String FEATURE_NAME="FeatureName";

    @Mock
    private Feature mFakeFeature;

    @Mock
    private MqttAndroidClient mClient;

    private Feature.Sample mFeatureSample = new Feature.Sample(0,
            new Number[]{0},
            new Field[]{new Field("Field0",null, Field.Type.Int8,0,0)});



    @Before
    public void setUpMockObject(){

        Mockito.when(mFakeFeature.getName()).thenReturn(FEATURE_NAME);

    }


    @Test
    public void publishTopicContainsFeatureName() throws MqttException {
        MqttFeatureListenerQuickStart mListener = new MqttFeatureListenerQuickStart(mClient);
        mListener.onUpdate(mFakeFeature,mFeatureSample);

        Mockito.verify(mClient).publish(Mockito.matches(".*"+FEATURE_NAME+".*"),any(MqttMessage.class));
    }

    @Test
    public void publishTopicContainsFeatureNameWithoutSpace() throws MqttException {
        MqttFeatureListenerQuickStart mListener = new MqttFeatureListenerQuickStart(mClient);
        Mockito.when(mFakeFeature.getName()).thenReturn("Name With Space");
        mListener.onUpdate(mFakeFeature,mFeatureSample);

        Mockito.verify(mClient).publish(Mockito.matches("\\S+"),any(MqttMessage.class));
    }

    private static class MqttPlayloadMather extends ArgumentMatcher<MqttMessage>{

        private String data;
        MqttPlayloadMather(String data){
            this.data=data;
        }

        @Override
        public boolean matches(Object argument) {
            MqttMessage msg = (MqttMessage) argument;
            String playload = new String(msg.getPayload());
            return playload.contains(data);
        }
    }



    @Test
    public void publishMessageWithSampleData() throws MqttException, JSONException {
        MqttFeatureListenerQuickStart mListener = new MqttFeatureListenerQuickStart(mClient);
        mListener.onUpdate(mFakeFeature,mFeatureSample);

        String sampleJson = JSONSampleSerializer.serialize(mFeatureSample).toString();

        Mockito.verify(mClient).publish(anyString(),
                argThat(new MqttPlayloadMather(sampleJson)));
    }

    private static class MqttMessageWithQos0 extends ArgumentMatcher<MqttMessage>{

        @Override
        public boolean matches(Object argument) {
            MqttMessage msg = (MqttMessage) argument;
            return msg.getQos()==0;
        }
    }

    @Test
    public void publishMessageWithQos0() throws MqttException, JSONException {
        MqttFeatureListenerQuickStart mListener = new MqttFeatureListenerQuickStart(mClient);
        mListener.onUpdate(mFakeFeature,mFeatureSample);

        Mockito.verify(mClient).publish(anyString(), argThat(new MqttMessageWithQos0()));
    }

}
