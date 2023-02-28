package com.st.BlueMS.demos.memsSensorFusion.feature_listener

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.FeatureMemsSensorFusionCompact
import java.util.concurrent.atomic.AtomicLong

data class SensorFusionData(
    val qi: Float,
    val qj: Float,
    val qk: Float,
    val qs: Float,
    val avgQuaternionRate: Long
)

class SensorFusionListener(val onFeatureUpdate: (SensorFusionData) -> Unit) :
    Feature.FeatureListener {

    /**
     * time of when we receive the first sample
     */
    private var mFistQuaternionTime: Long = -1

    /**
     * number of sample that we received
     */
    private val mNQuaternion = AtomicLong(0)

    fun resetQuaternionRate() {
        mFistQuaternionTime = -1
        mNQuaternion.set(0)
    }

    override fun onUpdate(f: Feature, data: Feature.Sample) {

        if (mFistQuaternionTime < 0) mFistQuaternionTime = System.currentTimeMillis()

        //+1 for avoid division by 0 the first time that we initialize mFistQuaternionTime
        val averageQuaternionRate = mNQuaternion.incrementAndGet() * 1000 /
                (System.currentTimeMillis() - mFistQuaternionTime + 1)

        val qi = FeatureMemsSensorFusionCompact.getQi(data)
        val qj = FeatureMemsSensorFusionCompact.getQj(data)
        val qk = FeatureMemsSensorFusionCompact.getQk(data)
        val qs = FeatureMemsSensorFusionCompact.getQs(data)

        onFeatureUpdate(SensorFusionData(qi, qj, qk, qs, averageQuaternionRate))
    }
}