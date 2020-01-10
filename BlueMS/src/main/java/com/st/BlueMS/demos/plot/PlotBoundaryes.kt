/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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

