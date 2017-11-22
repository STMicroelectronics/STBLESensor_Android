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

package com.st.BlueMS.demos.SDLog;

import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent;
import com.st.BlueSTSDK.Features.FeatureActivity;
import com.st.BlueSTSDK.Features.FeatureAudioADPCM;
import com.st.BlueSTSDK.Features.FeatureAudioADPCMSync;
import com.st.BlueSTSDK.Features.FeatureBattery;
import com.st.BlueSTSDK.Features.FeatureCarryPosition;
import com.st.BlueSTSDK.Features.FeatureCompass;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureHumidity;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;
import com.st.BlueSTSDK.Features.FeatureMemsGesture;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusion;
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact;
import com.st.BlueSTSDK.Features.FeatureMicLevel;
import com.st.BlueSTSDK.Features.FeatureMotionIntensity;
import com.st.BlueSTSDK.Features.FeaturePedometer;
import com.st.BlueSTSDK.Features.FeaturePressure;
import com.st.BlueSTSDK.Features.FeatureSDLogging;
import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Features.FeatureTemperature;
import com.st.BlueSTSDK.Node;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SDLogPresenterTest {
    private static final int LOG_INTERVAL = 110;
    private static final Feature.Sample LOGGING_SAMPLE = new Feature.Sample(
            new Number[]{FeatureSDLogging.LOGGING_STARTED}, null);
    private static final Feature.Sample LOGGING_SAMPLE_WITH_INTERVAL = new Feature.Sample(
            new Number[]{FeatureSDLogging.LOGGING_STARTED, 0xFFFFFFFF, LOG_INTERVAL}, null);
    private static final Feature.Sample NOT_LOGGING_SAMPLE =
            new Feature.Sample(new Number[]{FeatureSDLogging.LOGGING_STOPPED}, null);

    private static final Feature.Sample SD_NOT_FOUND_SAMPLE =
            new Feature.Sample(new Number[]{FeatureSDLogging.LOGGING_NO_SD}, null);

    private static final Feature.Sample IO_ERROR_SAMPLE =
            new Feature.Sample(new Number[]{FeatureSDLogging.LOGGING_IO_ERROR}, null);

    @Mock
    private SDLogContract.View mView;

    @Mock
    private FeatureSDLogging mFeature;

    @Mock
    private Node mNode;

    private SDLogPresenter mPresenter;

    @Before
    public void setUp() {
        mPresenter = new SDLogPresenter(mView, mFeature);
        when(mFeature.getParentNode()).thenReturn(mNode);
    }

    @Test
    public void ifTheFeatureIsNullTheViewIsDisabled() {
        SDLogContract.Presenter presenter = new SDLogPresenter(mView, null);
        presenter.startDemo();
        verify(mView).displayDisableLoggingView();
    }

    @Test
    public void ifFeatureIsPresentTheStatusIsRead() {
        mPresenter.startDemo();

        verify(mNode).readFeature(mFeature);
    }

    @Test
    public void whenDemosStartIntervalIsSetTo1sec() {
        mPresenter.startDemo();

        verify(mView).setLogInterval(1);
    }

    @Test
    public void ifFeatureIsNotLoggingTheViewGoesInStartLoggingMode() {
        mPresenter.onUpdate(mFeature, NOT_LOGGING_SAMPLE);

        verify(mView).displayStartLoggingView(anyListOf(Feature.class));
    }

    @Test
    public void ifFeatureIsLoggingTheViewGoesInStopLoggingMode() {
        mPresenter.onUpdate(mFeature, LOGGING_SAMPLE);
        verify(mView).displayStopLoggingView();
    }

    @Test
    public void ifLoggingPressingTheButtonStopIt() {
        mPresenter.onUpdate(mFeature, LOGGING_SAMPLE); // go to logging state
        mPresenter.onStartStopLogPressed();
        verify(mFeature).stopLogging();
    }


    @Test
    public void whenTheLogIsStoppedTheViewGoInStartState() {
        mPresenter.onUpdate(mFeature, NOT_LOGGING_SAMPLE); // go to logging state
        mPresenter.onStartStopLogPressed();
        verify(mView).displayStartLoggingView(anyListOf(Feature.class));
    }


    /**
     * crate a feature from its class instance
     *
     * @param featureClass class object that represent the feature to build
     * @param <T>          type of feature to build
     * @return the feature or null if the class doesn't has a method that request a node as a
     * parameter
     */
    protected
    @Nullable
    <T extends Feature> T buildFeatureFromClass(Class<T> featureClass) {
        try {
            Constructor<T> constructor = featureClass.getConstructor(Node.class);
            return constructor.newInstance(mNode);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }//try-catch
    }//buildFeatureFromClass

    private void theNodeHasTheFeatureOfType(Class<? extends Feature> featureType) {
        Feature fakeFeature = buildFeatureFromClass(featureType);
        assertNotNull(fakeFeature);
        when(mNode.getFeatures()).thenReturn(Collections.singletonList(fakeFeature));
    }

    public List<Feature> getDisplayFeatureWhenNodeContains(Class<? extends Feature> featureType) {
        theNodeHasTheFeatureOfType(featureType);
        mPresenter.onUpdate(mFeature, NOT_LOGGING_SAMPLE);
        ArgumentCaptor<List> arg = ArgumentCaptor.forClass(List.class);
        verify(mView).displayStartLoggingView(arg.capture());
        return arg.getValue();
    }

    public void assertNotDisplayFeatureOfType(Class<? extends Feature> featureType) {
        assertTrue(getDisplayFeatureWhenNodeContains(featureType).isEmpty());
    }

    public void assertDisplayFeatureOfType(Class<? extends Feature> featureType) {
        assertFalse(getDisplayFeatureWhenNodeContains(featureType).isEmpty());
    }

    @Test
    public void displayFeatureAreFiltered() {
        assertDisplayFeatureOfType(FeatureAcceleration.class);
    }

    @Test
    public void displayFeatureAreFiltered_2() {
        assertDisplayFeatureOfType(FeatureGyroscope.class);
    }

    @Test
    public void displayFeatureAreFiltered_3() {
        assertDisplayFeatureOfType(FeatureMagnetometer.class);
    }

    @Test
    public void displayFeatureAreFiltered_4() {
        assertDisplayFeatureOfType(FeaturePressure.class);
    }

    @Test
    public void displayFeatureAreFiltered_5() {
        assertDisplayFeatureOfType(FeatureHumidity.class);
    }

    @Test
    public void displayFeatureAreFiltered_6() {
        assertDisplayFeatureOfType(FeatureTemperature.class);
    }

    //@Test
    public void displayFeatureAreFiltered_7() {
        //needs android mock to build a memssensorFusionCompact class
        assertDisplayFeatureOfType(FeatureMemsSensorFusionCompact.class);
    }

    @Test
    public void displayFeatureAreFiltered_8() {
        assertDisplayFeatureOfType(FeatureMemsSensorFusion.class);
    }

    @Test
    public void displayFeatureAreFiltered_9() {
        assertNotDisplayFeatureOfType(FeatureAudioADPCMSync.class);
    }

    @Test
    public void displayFeatureAreFiltered_10() {
        assertNotDisplayFeatureOfType(FeatureAudioADPCMSync.class);
    }

    @Test
    public void displayFeatureAreFiltered_11() {
        assertNotDisplayFeatureOfType(FeatureSwitch.class);
    }

    @Test
    public void displayFeatureAreFiltered_12() {
        assertNotDisplayFeatureOfType(FeatureSwitch.class);
    }

    @Test
    public void displayFeatureAreFiltered_13() {
        assertNotDisplayFeatureOfType(FeatureAudioADPCM.class);
    }

    @Test
    public void displayFeatureAreFiltered_14() {
        assertNotDisplayFeatureOfType(FeatureMicLevel.class);
    }

    @Test
    public void displayFeatureAreFiltered_15() {
        assertNotDisplayFeatureOfType(FeatureBattery.class);
    }

    @Test
    public void displayFeatureAreFiltered_16() {
        assertNotDisplayFeatureOfType(FeatureSDLogging.class);
    }

    @Test
    public void displayFeatureAreFiltered_17() {
        assertNotDisplayFeatureOfType(FeatureAccelerationEvent.class);
    }

    @Test
    public void displayFeatureAreFiltered_18() {
        assertNotDisplayFeatureOfType(FeatureCompass.class);
    }

    @Test
    public void displayFeatureAreFiltered_19() {
        assertNotDisplayFeatureOfType(FeatureMotionIntensity.class);
    }

    @Test
    public void displayFeatureAreFiltered_20() {
        assertNotDisplayFeatureOfType(FeatureActivity.class);
    }


    @Test
    public void displayFeatureAreFiltered_21() {
        assertNotDisplayFeatureOfType(FeatureCarryPosition.class);
    }

    @Test
    public void displayFeatureAreFiltered_22() {
        assertNotDisplayFeatureOfType(FeatureMemsGesture.class);
    }

    @Test
    public void displayFeatureAreFiltered_23() {
        assertNotDisplayFeatureOfType(FeaturePedometer.class);
    }

    @Test
    public void ifTheSDIsNotPresentTheErrorIsShown(){
        mPresenter.onUpdate(mFeature, SD_NOT_FOUND_SAMPLE);
        verify(mView).displayNoSDCardErrorLoggingView();
    }

    @Test
    public void ifTheIOErrorTheErrorIOErrorIsShown(){
        mPresenter.onUpdate(mFeature,IO_ERROR_SAMPLE);
        verify(mView).displayIOErrorLoggingView();
    }

    @Test
    public void ifLoggingPressingTheButtonStartIt() {
        long logInterval = 19;
        Set<Feature> selectedFeature = (Set<Feature>) mock(Set.class);
        when(mFeature.getSample()).thenReturn(NOT_LOGGING_SAMPLE);
        when(mView.getLogInterval()).thenReturn(logInterval);
        when(mView.getSelectedFeature()).thenReturn(selectedFeature);
        mPresenter.onStartStopLogPressed();
        verify(mFeature).startLogging(selectedFeature, logInterval);
    }

    @Test
    public void whenTheLogIsStartedTheViewGoInStopState() {
        when(mFeature.getSample()).thenReturn(NOT_LOGGING_SAMPLE);
        mPresenter.onStartStopLogPressed();
        verify(mView).displayStopLoggingView();
    }
/*
    @Test
    public void whenLogStatusIsReadTheLogingTimeIsUpdate(){
        mPresenter.onUpdate(mFeature,LOGGING_SAMPLE_WITH_INTERVAL);
        verify(mView).setLogInterval(LOG_INTERVAL);
    }

    @Test
    public void whenALogStatusIsReadTheEnableFeatureAreUpdated(){
        Feature.Sample fakeSample = new Feature.Sample(new Number[]{1,0xFFFFFFFF},null);
        mPresenter.onUpdate(mFeature,fakeSample);

        verify(mView).setSelectedFeature(anySet());
    }*/

}