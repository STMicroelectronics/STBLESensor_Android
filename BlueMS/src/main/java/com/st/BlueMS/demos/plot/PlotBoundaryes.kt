package com.st.BlueMS.demos.plot

import com.st.BlueSTSDK.Feature
import com.st.BlueSTSDK.Features.*

internal data class PlotBoundary(
        val min:Float?=null,
        val max:Float?=null,
        val nLabels:Int?=null){

    val enableAutoScale:Boolean
        get() =  min==null && max == null


    companion object {
        private val DEFAULT_PLOT_BOUNDARIES = mapOf(
                FeatureAcceleration::class to PlotBoundary(FeatureAcceleration.DATA_MIN.toFloat(), FeatureAcceleration.DATA_MAX.toFloat(), 21),
                FeatureMagnetometer::class to PlotBoundary(FeatureMagnetometer.DATA_MIN.toFloat(), FeatureMagnetometer.DATA_MAX.toFloat(), 21),
                FeatureGyroscope::class to PlotBoundary(FeatureGyroscope.DATA_MIN, FeatureGyroscope.DATA_MAX, 11),
                FeatureHumidity::class to  PlotBoundary(FeatureHumidity.DATA_MIN, FeatureHumidity.DATA_MAX, 11),
                FeatureLuminosity::class to  PlotBoundary(FeatureLuminosity.DATA_MIN.toFloat(), FeatureLuminosity.DATA_MAX.toFloat(), 11),
                FeatureMemsSensorFusion::class to  PlotBoundary(FeatureMemsSensorFusion.DATA_MIN, FeatureMemsSensorFusion.DATA_MAX, 11),
                FeatureMemsSensorFusionCompact::class to  PlotBoundary(FeatureMemsSensorFusion.DATA_MIN, FeatureMemsSensorFusion.DATA_MAX, 11),
                FeatureCompass::class to  PlotBoundary(FeatureCompass.DATA_MIN, FeatureCompass.DATA_MAX, 19),
                FeatureDirectionOfArrival::class to  PlotBoundary(FeatureDirectionOfArrival.DATA_MIN.toFloat(), FeatureDirectionOfArrival.DATA_MAX.toFloat(), 37),
                FeaturePressure::class to  PlotBoundary(960.0f,1060.0f, 11),
                FeatureTemperature::class to  PlotBoundary(0.0f, 50.0f, 11),
                FeatureActivity::class to  PlotBoundary(FeatureActivity.DATA_MIN, FeatureActivity.DATA_MAX, (FeatureActivity.DATA_MAX-FeatureActivity.DATA_MIN).toInt()+1)
        )

        fun getDefaultFor(f:Feature): PlotBoundary {
            return DEFAULT_PLOT_BOUNDARIES[f::class] ?: PlotBoundary()
        }
    }
}

