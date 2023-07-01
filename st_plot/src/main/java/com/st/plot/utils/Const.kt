package com.st.plot.utils

import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.features.acceleration.Acceleration
import com.st.blue_sdk.features.acceleration.AccelerationInfo
import com.st.blue_sdk.features.co_sensor.COSensor
import com.st.blue_sdk.features.co_sensor.COSensorInfo
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.compass.CompassInfo
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrival
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrivalInfo
import com.st.blue_sdk.features.event_counter.EventCounter
import com.st.blue_sdk.features.event_counter.EventCounterInfo
import com.st.blue_sdk.features.extended.euler_angle.EulerAngle
import com.st.blue_sdk.features.extended.euler_angle.EulerAngleInfo
import com.st.blue_sdk.features.extended.qvar.QVAR
import com.st.blue_sdk.features.extended.qvar.QVARInfo
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObjectInfo
import com.st.blue_sdk.features.gyroscope.Gyroscope
import com.st.blue_sdk.features.gyroscope.GyroscopeInfo
import com.st.blue_sdk.features.humidity.Humidity
import com.st.blue_sdk.features.humidity.HumidityInfo
import com.st.blue_sdk.features.luminosity.Luminosity
import com.st.blue_sdk.features.luminosity.LuminosityInfo
import com.st.blue_sdk.features.magnetometer.Magnetometer
import com.st.blue_sdk.features.magnetometer.MagnetometerInfo
import com.st.blue_sdk.features.mems_norm.MemsNorm
import com.st.blue_sdk.features.mems_norm.MemsNormInfo
import com.st.blue_sdk.features.mic_level.MicLevel
import com.st.blue_sdk.features.mic_level.MicLevelInfo
import com.st.blue_sdk.features.motion_intensity.MotionIntensity
import com.st.blue_sdk.features.motion_intensity.MotionIntensityInfo
import com.st.blue_sdk.features.pressure.Pressure
import com.st.blue_sdk.features.pressure.PressureInfo
import com.st.blue_sdk.features.proximity.Proximity
import com.st.blue_sdk.features.proximity.ProximityInfo
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionInfo
import com.st.blue_sdk.features.temperature.Temperature
import com.st.blue_sdk.features.temperature.TemperatureInfo
import com.st.plot.PlotEntry
import java.lang.StringBuilder

const val PLOT_SETTINGS = "Plot Settings"

val PLOTTABLE_FEATURE = listOf(
    Acceleration.NAME,
    Compass.NAME,
    DirectionOfArrival.NAME,
    Gyroscope.NAME,
    Humidity.NAME,
    Luminosity.NAME,
    Magnetometer.NAME,
    MemsSensorFusion.NAME,
    MemsSensorFusionCompat.NAME,
    MicLevel.NAME,
    MotionIntensity.NAME,
    Proximity.NAME,
    Pressure.NAME,
    Temperature.NAME,
    COSensor.NAME,
    EulerAngle.NAME,
    MemsNorm.NAME,
    QVAR.NAME,
    ToFMultiObject.NAME,
    EventCounter.NAME
)

internal fun Feature<*>.fieldsDesc(): Map<String, String> =
    when (this) {
        is Acceleration -> mapOf("X" to "mg", "Y" to "mg", "Z" to "mg")
        is Compass -> mapOf("Angle" to "°")
        is DirectionOfArrival -> mapOf("Angle" to "°")
        is Gyroscope -> mapOf("X" to "dps", "Y" to "dps", "Z" to "dps")
        is Humidity -> mapOf("Humidity" to "%")
        is Luminosity -> mapOf("Luminosity" to "Lux")
        is Magnetometer -> mapOf("X" to "mGa", "Y" to "mGa", "Z" to "mGa")
        is MemsSensorFusion -> mapOf("qi" to "qi", "qj" to "qj", "qk" to "qk", "qs" to "qs")
        is MemsSensorFusionCompat -> mapOf("qi" to "qi", "qj" to "qj", "qk" to "qk", "qs" to "qs")
        is MicLevel -> {
            val varMap = mutableMapOf<String, String>()
            for (micNum in 0 until this.numMic) {
                varMap["Mic_$micNum"] = "dB"
            }
            varMap.toMap()
        }

        is MotionIntensity -> mapOf("Intensity" to "")
        is Proximity -> mapOf("Proximity" to "mm")
        is Pressure -> mapOf("Pressure" to "mBar")
        is Temperature -> mapOf("Temperature" to "℃")
        is COSensor -> mapOf("CO Concentration" to "ppm")
        is EulerAngle -> mapOf("Yaw" to "°", "Pitch" to "°", "Roll" to "°")
        is MemsNorm -> mapOf("Norm" to "")
        is QVAR -> mapOf("QVAR" to "LSB", "DQVAR" to "LSB")
        is ToFMultiObject -> mapOf(
            "Obj_0" to "mm",
            "Obj_1" to "mm",
            "Obj_2" to "mm",
            "Obj_3" to "mm"
        )

        is EventCounter -> mapOf("Event" to "#Event")
        else -> mapOf("Events" to "")
    }

internal fun FeatureUpdate<*>.toPlotEntry(feature: Feature<*>, xOffset: Long): PlotEntry? =
    when (val data = this.data) {
        is AccelerationInfo ->
            if (feature !is Acceleration) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.x.value, data.y.value, data.z.value).toFloatArray()
                )

        is CompassInfo ->
            if (feature !is Compass) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.angle.value).toFloatArray()
                )

        is DirectionOfArrivalInfo ->
            if (feature !is DirectionOfArrival) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.angle.value.toFloat()).toFloatArray()
                )

        is GyroscopeInfo ->
            if (feature !is Gyroscope) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.x.value, data.y.value, data.z.value).toFloatArray()
                )

        is HumidityInfo ->
            if (feature !is Humidity) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.humidity.value).toFloatArray()
                )

        is LuminosityInfo ->
            if (feature !is Luminosity) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.luminosity.value.toFloat()).toFloatArray()
                )

        is MagnetometerInfo ->
            if (feature !is Magnetometer) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.x.value, data.y.value, data.z.value).toFloatArray()
                )

        is MemsSensorFusionInfo ->
            if (feature !is MemsSensorFusion && feature !is MemsSensorFusionCompat) null
            else {
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(
                        data.quaternions[0].value.qi,
                        data.quaternions[0].value.qj,
                        data.quaternions[0].value.qs,
                        data.quaternions[0].value.qk
                    ).toFloatArray()
                )
            }

        is MicLevelInfo ->
            if (feature !is MicLevel) null
            else {
                val sampleList = mutableListOf<Float>()
                for (element in data.micsLevel) {
                    sampleList.add(element.value.toFloat())
                }

                PlotEntry(
                    notificationTime.time - xOffset,
                    sampleList.toFloatArray()
                )
            }

        is MotionIntensityInfo ->
            if (feature !is MotionIntensity) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.intensity.value.toFloat()).toFloatArray()
                )

        is ProximityInfo ->
            if (feature !is Proximity || data.proximity.value == ProximityInfo.OUT_OF_RANGE_VALUE) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.proximity.value.toFloat()).toFloatArray()
                )

        is PressureInfo ->
            if (feature !is Pressure) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.pressure.value).toFloatArray()
                )

        is TemperatureInfo ->
            if (feature !is Temperature) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.temperature.value).toFloatArray()
                )

        is COSensorInfo ->
            if (feature !is COSensor) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.concentration.value).toFloatArray()
                )

        is EulerAngleInfo ->
            if (feature !is EulerAngle) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.yaw.value, data.pitch.value, data.roll.value).toFloatArray()
                )

        is MemsNormInfo ->
            if (feature !is MemsNorm) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.norm.value).toFloatArray()
                )

        is QVARInfo ->
            if (feature !is QVAR) null
            else {
                val yData = mutableListOf<Float>()
                yData.add((data.qvar.value ?: 0).toFloat())
                yData.add((data.dqvar.value ?: 0).toFloat())

                PlotEntry(
                    notificationTime.time - xOffset,
                    yData.toFloatArray()
                )
            }

        is ToFMultiObjectInfo ->
            if (feature !is ToFMultiObject) null
            else {
                val yData = mutableListOf<Float>()
                for (element in data.distanceObjs) {
                    yData.add(element.value.toFloat())
                }
                //Filling remaining Distances ==0
                for (element in data.nObjsFound.value until 4) {
                    yData.add(0.0f)
                }
                PlotEntry(
                    notificationTime.time - xOffset,
                    yData.toFloatArray()
                )
            }

        is EventCounterInfo ->
            if (feature !is EventCounter) null
            else
                PlotEntry(
                    notificationTime.time - xOffset,
                    listOf(data.count.value.toFloat()).toFloatArray()
                )

        else -> null
    }

internal fun FeatureUpdate<*>.toPlotDesc(feature: Feature<*>): String? =
    when (val data = this.data) {
        is AccelerationInfo ->
            if (feature !is Acceleration) null
            else
                "TS:$timeStamp X:${data.x.value} Y:${data.y.value} Z:${data.z.value}"

        is CompassInfo ->
            if (feature !is Compass) null
            else
                "TS:$timeStamp Angle:${data.angle.value}"

        is DirectionOfArrivalInfo ->
            if (feature !is DirectionOfArrival) null
            else
                "TS:$timeStamp Angle:${data.angle.value}"

        is GyroscopeInfo ->
            if (feature !is Gyroscope) null
            else
                "TS:$timeStamp X:${data.x.value} Y:${data.y.value} Z:${data.z.value}"

        is HumidityInfo ->
            if (feature !is Humidity) null
            else
                "TS:$timeStamp Hum:${data.humidity.value}"

        is LuminosityInfo ->
            if (feature !is Luminosity) null
            else
                "TS:$timeStamp Lux:${data.luminosity.value}"

        is MagnetometerInfo ->
            if (feature !is Magnetometer) null
            else
                "TS:$timeStamp X:${data.x.value} Y:${data.y.value} Z:${data.z.value}"

        is MemsSensorFusionInfo ->
            if (feature !is MemsSensorFusion && feature !is MemsSensorFusionCompat) null
            else
                "TS:$timeStamp qi:${data.quaternions[0].value.qi} qj:${data.quaternions[0].value.qj} qs:${data.quaternions[0].value.qs} qk: ${data.quaternions[0].value.qk}"

        is MicLevelInfo ->
            if (feature !is MicLevel) null
            else {
                val out = StringBuilder()
                out.append("TS:$timeStamp")

                for (element in data.micsLevel) {
                    out.append(" ${element.name}:${element.value}")
                }
                out.toString()
            }

        is MotionIntensityInfo ->
            if (feature !is MotionIntensity) null
            else
                "TS:$timeStamp Int:${data.intensity.value}"

        is ProximityInfo ->
            if (feature !is Proximity) null
            else
                "TS:$timeStamp mm:${data.proximity.value}"

        is PressureInfo ->
            if (feature !is Pressure) null
            else
                "TS:$timeStamp Press:${data.pressure.value}"

        is TemperatureInfo ->
            if (feature !is Temperature) null
            else
                "TS:$timeStamp Temp:${data.temperature.value}"

        is COSensorInfo ->
            if (feature !is COSensor) null
            else
                "TS:$timeStamp ppm:${data.concentration.value}"

        is EulerAngleInfo ->
            if (feature !is EulerAngle) null
            else
                "TS:$timeStamp Yaw:${data.yaw.value} Pitch:${data.pitch.value} Roll:${data.roll.value}"

        is MemsNormInfo ->
            if (feature !is MemsNorm) null
            else
                "TS:$timeStamp Norm:${data.norm.value}"

        is QVARInfo ->
            if (feature !is QVAR) null
            else {
                val out = StringBuilder()
                out.append("TS:$timeStamp")
                data.qvar.value?.let { out.append(" QVAR:${it.toFloat()}") }
                data.dqvar.value?.let { out.append(" DQVAR:${it.toFloat()}") }
                out.toString()
            }

        is ToFMultiObjectInfo ->
            if (feature !is ToFMultiObject) null
            else {
                val out = StringBuilder()
                out.append("TS:$timeStamp")

                for (element in data.distanceObjs) {
                    out.append(" ${element.name}:${element.value} ")
                }
                out.toString()
            }

        is EventCounterInfo ->
            if (feature !is EventCounter) null
            else
                "TS: $timeStamp #:${data.count.value}"

        else -> null
    }