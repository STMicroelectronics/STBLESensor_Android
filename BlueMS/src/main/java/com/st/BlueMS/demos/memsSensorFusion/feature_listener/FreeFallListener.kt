package com.st.BlueMS.demos.memsSensorFusion.feature_listener

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureAccelerationEvent

class FreeFallListener(val notificationDelay: Int, val onFeatureUpdate: () -> Unit) :
    Feature.FeatureListener {

    private var mLastNotificationTime = 0

    override fun onUpdate(f: Feature, sample: Feature.Sample) {

        if (FeatureAccelerationEvent.hasAccelerationEvent(
                sample,
                FeatureAccelerationEvent.FREE_FALL
            ).not()
        ) {
            return
        }

        if (sample.notificationTime - mLastNotificationTime > notificationDelay) {
            mLastNotificationTime = sample.notificationTime.toInt()
            onFeatureUpdate()
        }
    }
}