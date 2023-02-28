package com.st.BlueSTSDK.Features;


import androidx.annotation.NonNull;

import com.st.BlueSTSDK.Feature;

import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class TestAccEventFeature {

    public class FeatureAccelerationEventTestable extends FeatureAccelerationEvent{
        int timestamp=0;
        public FeatureAccelerationEventTestable(){
            super(null);
        }

        public boolean sendCommand(byte commandType,@NonNull byte[] data){
            parseCommandResponse(timestamp++,commandType,data);
            return true;
        }
    }

    @Test
    public void testNullSampleAccNone(){
        Assert.assertEquals(FeatureAccelerationEvent.NO_EVENT, FeatureAccelerationEvent
                .getAccelerationEvent(null));
    }

    @Test
    public void testInvalidSampleAccEvent(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{}, new Field[]{});
        Assert.assertEquals(FeatureAccelerationEvent.NO_EVENT, FeatureAccelerationEvent
                .getAccelerationEvent(s));
    }

    @Test
    public void testGetSampleAccEvent(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{FeatureAccelerationEvent.FREE_FALL}, new Field[]{});
        Assert.assertEquals(FeatureAccelerationEvent.FREE_FALL,
                FeatureAccelerationEvent.getAccelerationEvent(s));
    }

    @Test
    public void testNullSamplePedometerSteps(){
        Assert.assertTrue(FeatureAccelerationEvent.getPedometerSteps(null) < 0);
    }

    @Test
    public void testInvalidSamplePedometerSteps(){
        Feature.Sample s = new Feature.Sample(100,new Number[]{1}, new Field[]{});
        Assert.assertTrue(FeatureAccelerationEvent.getPedometerSteps(null) < 0);
    }

    @Test
    public void testGetSamplePedometerSteps(){
        int nStep = 1234;
        Feature.Sample s = new Feature.Sample(100,new Number[]{FeatureAccelerationEvent.PEDOMETER,nStep}, new Field[]{});
        Assert.assertEquals(FeatureAccelerationEvent.PEDOMETER,
                FeatureAccelerationEvent.getAccelerationEvent(s));
        Assert.assertEquals(nStep,
                FeatureAccelerationEvent.getPedometerSteps(s));
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSize() throws Throwable {
        Feature f = new FeatureAccelerationEvent(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithInvalidSizeWithOffset() throws Throwable {
        Feature f = new FeatureAccelerationEvent(null);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1}, 1);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithPedometerInvalidSize() throws Throwable {
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER,true);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1}, 0);
    }

    @Test(expected= IllegalArgumentException.class)
    public void updateWithPedometerInvalidSizeWithOffset() throws Throwable {
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER,true);
        UpdateFeatureUtil.callUpdate(f, 100, new byte[]{1,2}, 1);
    }


    @Test
    public void enableEventHasCallback(){
        FeatureAccelerationEvent.FeatureAccelerationEventListener listener =
                mock(FeatureAccelerationEvent.FeatureAccelerationEventListener.class);
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.addFeatureListener(listener);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);
        //TestUtil.execAllAsyncTask();

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, false);

        verify(listener,timeout(200)).onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent
                .PEDOMETER, false);
    }

    @Test
    public void enableANewEventDisableThePreviousOne(){
        FeatureAccelerationEvent.FeatureAccelerationEventListener listener =
                mock(FeatureAccelerationEvent.FeatureAccelerationEventListener.class);
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.addFeatureListener(listener);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER,
                        false);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.DOUBLE_TAP,
                        true);

    }

    @Test
    public void enableNoneDisableThePreviousOne(){
        FeatureAccelerationEvent.FeatureAccelerationEventListener listener =
                mock(FeatureAccelerationEvent.FeatureAccelerationEventListener.class);
        FeatureAccelerationEvent f = new FeatureAccelerationEventTestable();
        f.addFeatureListener(listener);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER, true);

        f.detectEvent(FeatureAccelerationEvent.DetectableEvent.NONE, true);

        verify(listener,timeout(200)).
                onDetectableEventChange(f, FeatureAccelerationEvent.DetectableEvent.PEDOMETER,
                        false);

    }
}
