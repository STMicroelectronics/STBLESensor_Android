package com.st.BlueSTSDK.Utils;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.FeatureAcceleration;
import com.st.BlueSTSDK.Features.FeatureGyroscope;
import com.st.BlueSTSDK.Features.FeatureMagnetometer;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConnectionOptionTest {

    @Test
    public void defaultBuilderDontResetCacheAndDisableAutoConnect(){
        ConnectionOption opt = ConnectionOption.builder().build();
        Assert.assertFalse(opt.resetCache());
        Assert.assertFalse(opt.enableAutoConnect());
    }

    @Test
    public void defaultBuilderHasNotUserDefinedFeature(){
        ConnectionOption opt = ConnectionOption.builder().build();
        Assert.assertNull(opt.getUserDefineFeature());
    }

    @Test
    public void resetCacheChangeCacheFlag(){
        ConnectionOption opt = ConnectionOption.builder()
                .resetCache(true)
                .build();
        Assert.assertTrue(opt.resetCache());
    }

    @Test
    public void enableAutoConnectChangeTheFlag(){
        ConnectionOption opt = ConnectionOption.builder()
                .enableAutoConnect(true)
                .build();
        Assert.assertTrue(opt.enableAutoConnect());
    }


    @Test
    public void addFeatureStoreTheUserDefinedFeature(){
        UUID uuidA = UUID.randomUUID();
        List<Class<? extends  Feature>> featuresClassA =
                Arrays.asList(FeatureAcceleration.class,FeatureGyroscope.class);

        UUID uuidB = UUID.randomUUID();
        Class<? extends  Feature> featureClassB = FeatureMagnetometer.class;

        ConnectionOption opt = ConnectionOption.builder()
                .addFeature(uuidA, featuresClassA)
                .addFeature(uuidB, featureClassB)
                .build();

        Map<UUID,List<Class< ? extends Feature>>> userDefineFeature = opt.getUserDefineFeature();

        Assert.assertNotNull(userDefineFeature);
        Assert.assertTrue(userDefineFeature.containsKey(uuidA));
        Assert.assertTrue(userDefineFeature.get(uuidA).contains(featuresClassA.get(0)));
        Assert.assertTrue(userDefineFeature.get(uuidA).contains(featuresClassA.get(1)));

        Assert.assertTrue(userDefineFeature.containsKey(uuidB));
        Assert.assertTrue(userDefineFeature.get(uuidB).contains(featureClassB));

    }

    @Test
    public void addFeatureAcceptASingleClass(){
        UUID random = UUID.randomUUID();
        Class<? extends  Feature> featureClass = FeatureAcceleration.class;
        ConnectionOption opt = ConnectionOption.builder()
                .addFeature(random, featureClass)
                .build();

        Map<UUID,List<Class< ? extends Feature>>> userDefineFeature = opt.getUserDefineFeature();

        Assert.assertNotNull(userDefineFeature);
        Assert.assertTrue(userDefineFeature.containsKey(random));
        Assert.assertTrue(userDefineFeature.get(random).contains(featureClass));

    }

    @Test
    public void aFeatureMapCanBeused(){
        UUID random = UUID.randomUUID();
        Class<? extends  Feature> featureClass = FeatureAcceleration.class;
        UUIDToFeatureMap map = new UUIDToFeatureMap();
        map.put(random,featureClass);
        ConnectionOption opt = ConnectionOption.builder()
                .setFeatureMap(map)
                .build();

        Map<UUID,List<Class< ? extends Feature>>> userDefineFeature = opt.getUserDefineFeature();

        Assert.assertNotNull(userDefineFeature);
        Assert.assertTrue(userDefineFeature.containsKey(random));
        Assert.assertTrue(userDefineFeature.get(random).contains(featureClass));

    }

    @Test(expected = IllegalArgumentException.class)
    public void multipleAddFeatureOnTheSameUUIDThrowAnException(){
        UUID uuidA = UUID.randomUUID();
        Class<? extends  Feature> featureClassA = FeatureMagnetometer.class;

        Class<? extends  Feature> featureClassB = FeatureMagnetometer.class;

        ConnectionOption opt = ConnectionOption.builder()
                .addFeature(uuidA, featureClassA)
                .addFeature(uuidA, featureClassB)
                .build();
    }

}