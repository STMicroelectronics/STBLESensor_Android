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
package com.st.plot.utils

import com.st.blue_sdk.features.acceleration.Acceleration
import com.st.blue_sdk.features.activity.Activity
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrival
import com.st.blue_sdk.features.extended.qvar.QVAR
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject
import com.st.blue_sdk.features.gyroscope.Gyroscope
import com.st.blue_sdk.features.humidity.Humidity
import com.st.blue_sdk.features.luminosity.Luminosity
import com.st.blue_sdk.features.magnetometer.Magnetometer
import com.st.blue_sdk.features.pressure.Pressure
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.temperature.Temperature

data class PlotBoundary(
    val min: Float? = null,
    val max: Float? = null,
    val nLabels: Int? = null
) {

    val enableAutoScale: Boolean
        get() = min == null && max == null

    companion object {
        private val DEFAULT_PLOT_BOUNDARIES = mapOf(
            Acceleration.NAME to PlotBoundary(nLabels = 21),
            Magnetometer.NAME to PlotBoundary(-Magnetometer.DATA_MAX, Magnetometer.DATA_MAX, 21),
            Gyroscope.NAME to PlotBoundary(-Gyroscope.DATA_MAX, Gyroscope.DATA_MAX, 11),
            Humidity.NAME to PlotBoundary(nLabels = 11),
            Luminosity.NAME to PlotBoundary(Luminosity.DATA_MIN, Luminosity.DATA_MAX, 11),
            MemsSensorFusion.NAME to PlotBoundary(
                MemsSensorFusion.DATA_MIN,
                MemsSensorFusion.DATA_MAX,
                11
            ),
            MemsSensorFusionCompat.NAME to PlotBoundary(
                MemsSensorFusionCompat.DATA_MIN,
                MemsSensorFusionCompat.DATA_MAX,
                11
            ),
            Compass.NAME to PlotBoundary(Compass.DATA_MIN, Compass.DATA_MAX, 19),
            DirectionOfArrival.NAME to PlotBoundary(
                DirectionOfArrival.DATA_MIN,
                DirectionOfArrival.DATA_MAX,
                37
            ),
            Pressure.NAME to PlotBoundary(nLabels = 11),
            Temperature.NAME to PlotBoundary(nLabels = 11),
            Activity.NAME to PlotBoundary(
                Activity.DATA_MIN,
                Activity.DATA_MAX,
                (Activity.DATA_MAX - Activity.DATA_MIN).toInt() + 1
            ),
            QVAR.NAME to PlotBoundary(nLabels = 21), //auto scale on
            ToFMultiObject.NAME to PlotBoundary(nLabels = 21), //auto scale on
        )

        fun getDefaultFor(featureName: String): PlotBoundary {
            return DEFAULT_PLOT_BOUNDARIES[featureName] ?: PlotBoundary()
        }
    }
}

// FIXME: move to sdk this missing min and max

val Luminosity.Companion.DATA_MAX
    get() = 1000f

val Luminosity.Companion.DATA_MIN
    get() = 0f

val MemsSensorFusion.Companion.DATA_MAX
    get() = 1f

val MemsSensorFusion.Companion.DATA_MIN
    get() = -1f

val MemsSensorFusionCompat.Companion.DATA_MAX
    get() = 1f

val MemsSensorFusionCompat.Companion.DATA_MIN
    get() = -1f

val Compass.Companion.DATA_MAX
    get() = 360f

val Compass.Companion.DATA_MIN
    get() = 0f

val DirectionOfArrival.Companion.DATA_MAX
    get() = 360f

val DirectionOfArrival.Companion.DATA_MIN
    get() = 0f

val Activity.Companion.DATA_MAX
    get() = 8f

val Activity.Companion.DATA_MIN
    get() = 0f

