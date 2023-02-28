package com.st.BlueSTSDK;

import android.os.Build;

import com.st.BlueSTSDK.Features.FeatureSwitch;
import com.st.BlueSTSDK.Features.FeatureTemperature;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,manifest = Config.NONE, sdk = 23)
public class NodeStaticTest {


    private Node createNodeWithFeatureMask(int featueMask){
        Node node = mock(Node.class);
        when(node.getTypeId()).thenReturn((byte)0);
        when(node.getAdvertiseBitMask()).thenReturn((long) featueMask);
        return  node;
    }

    @Test
    public void nodeExportFeatureUseTheDefaultMapToWork(){
        Node n = createNodeWithFeatureMask(0x20000000);
        Assert.assertTrue(n.isExportingFeature(FeatureSwitch.class));
        Assert.assertFalse(n.isExportingFeature(FeatureTemperature.class));
    }

    @Test
    public void nodeExportFeatureworksWithMultipleFeature(){
        Node n = createNodeWithFeatureMask(0x20000000);
        Assert.assertTrue(n.isExportingFeature(FeatureSwitch.class));
        Assert.assertFalse(n.isExportingFeature(FeatureTemperature.class));
    }

}
