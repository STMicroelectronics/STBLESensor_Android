package com.st.BlueMS.demos.memsSensorFusion.feature_listener

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureProximity

data class ProximityData(val proximity: Int)

class FeatureListenerImpl(val onFeatureUpdate: (ProximityData) -> Unit) : Feature.FeatureListener {

    override fun onUpdate(f: Feature, sample: Feature.Sample) {

        val proximity = FeatureProximity.getProximityDistance(sample)
        onFeatureUpdate(ProximityData(proximity))
    }
}