/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.sensor_fusion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent
import com.st.blue_sdk.features.acceleration_event.AccelerationEventInfo
import com.st.blue_sdk.features.acceleration_event.AccelerationType
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.blue_sdk.features.acceleration_event.request.EnableDetectionAccelerationEvent
import com.st.blue_sdk.features.proximity.Proximity
import com.st.blue_sdk.features.proximity.ProximityInfo
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionInfo
import com.st.blue_sdk.features.sensor_fusion.Quaternion
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.services.calibration.CalibrationService
import com.st.blue_sdk.services.calibration.CalibrationServiceImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorFusionViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var featureSensorFusion: Feature<*>? = null

    private var featureProximity: Feature<*>? = null
    private var featureFreeFall: Feature<*>? = null

    private val _fusionData =
        MutableStateFlow<Quaternion?>(null)
    val fusionData: StateFlow<Quaternion?>
        get() = _fusionData.asStateFlow()

    private val _proximityData =
        MutableStateFlow<Int?>(null)
    val proximity: StateFlow<Int?>
        get() = _proximityData.asStateFlow()

    private val _freeFallData =
        MutableStateFlow<Pair<Boolean,Long?>>(Pair(false,null))
    val freeFall: StateFlow<Pair<Boolean,Long?>>
        get() = _freeFallData.asStateFlow()

    private val _calibrationStatus =
        MutableStateFlow(false)
    val calibrationStatus: StateFlow<Boolean>
        get() = _calibrationStatus.asStateFlow()

    private suspend fun getCalibration(feature: Feature<*>, nodeId: String) {
        calibrationService.getCalibration(
            feature = feature,
            nodeId = nodeId
        ).also {
            _calibrationStatus.emit(it.status)
        }
    }

    private suspend fun startCalibration(feature: Feature<*>, nodeId: String) {
        calibrationService.startCalibration(
            feature = feature,
            nodeId = nodeId
        ).also {
            _calibrationStatus.emit(it.status)
        }
    }

    private fun setFreeFallDetectableEventCommand(nodeId: String, feature: AccelerationEvent) {
        viewModelScope.launch {
            //Disable the default event type
            blueManager.writeFeatureCommand(
                nodeId = nodeId,
                featureCommand = EnableDetectionAccelerationEvent(
                    feature = feature,
                    event = DetectableEventType.Multiple,
                    enable = false
                )
            )
            //Enable the free fall event type
            blueManager.writeFeatureCommand(
                nodeId = nodeId,
                featureCommand = EnableDetectionAccelerationEvent(
                    feature = feature,
                    event = DetectableEventType.FreeFall,
                    enable = true
                )
            )
        }
    }

    fun startDemo(nodeId: String) {
        //_resetCube.emit(false)

        //Sensor Fusion Feature
        if (featureSensorFusion == null) {
            blueManager.nodeFeatures(nodeId).find {
                MemsSensorFusionCompat.NAME == it.name || MemsSensorFusion.NAME == it.name
            }?.let { f ->
                featureSensorFusion = f
            }
        }
        featureSensorFusion?.let {

            viewModelScope.launch {
                blueManager.getConfigControlUpdates(nodeId = nodeId).collect {
                    if (it is CalibrationStatus) {
                        _calibrationStatus.emit(it.status)
                    }
                }
            }

            viewModelScope.launch {
                blueManager.getFeatureUpdates(
                    nodeId,
                    listOf(it)
                ).collect {
                    val data = it.data
                    if (data is MemsSensorFusionInfo) {

                        if (data.quaternions.size == 1) {
                            _fusionData.emit(data.quaternions[0].value)
                        } else {
                            var prevTimeStamp: Long = -1
                            for (current in data.quaternions) {
                                val currentTimeStamp = current.value.timeStamp
                                if (prevTimeStamp != -1L) {
                                    delay(currentTimeStamp - prevTimeStamp)
                                }
                                _fusionData.emit(current.value)
                                prevTimeStamp = currentTimeStamp
                            }
                           // _fusionData.emit(data.quaternions[0].value)
                        }
                    }
                }
            }
        }

        //take a look on debug Console for SensorTile.box and SensorTile.box-Pro
        viewModelScope.launch {
            blueManager.getDebugMessages(nodeId = nodeId)?.collect {
                val message = it.payload
                if (message.isNotEmpty()) {
                    val matcher = CalibrationServiceImpl.STATUS_PARSER.matcher(message)
                    if (matcher.matches()) {
                        _calibrationStatus.emit(true)
                    }
                }
            }
        }

        featureSensorFusion?.let {
            viewModelScope.launch {
                featureSensorFusion?.let { feature ->
                    getCalibration(feature, nodeId)
                }
            }
        }

        //Proximity Feature
        if (featureProximity == null) {
            blueManager.nodeFeatures(nodeId).find {
                Proximity.NAME == it.name
            }?.let { f ->
                featureProximity = f
            }
        }

        //Free Fall Feature
        if (featureFreeFall == null) {
            blueManager.nodeFeatures(nodeId).find {
                AccelerationEvent.NAME == it.name
            }?.let { f ->
                featureFreeFall = f
            }
        }

        featureFreeFall?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(
                    nodeId,
                    listOf(it),
                    onFeaturesEnabled = {
                        setFreeFallDetectableEventCommand(
                            nodeId = nodeId,
                            feature = it as AccelerationEvent
                        )
                    }
                ).collect { it ->
                    val data = it.data
                    if (data is AccelerationEventInfo) {
                        if (data.accEvent.map { event -> event.value }
                                .firstOrNull { event -> event == AccelerationType.FreeFall } != null) {
                            _freeFallData.emit(Pair(true,it.timeStamp))
                        }
                    }
                }
            }
        }
    }

    fun enableProximityNotification(nodeId: String) {
        featureProximity?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(
                    nodeId,
                    listOf(it)
                ).collect {
                    val data = it.data
                    if (data is ProximityInfo) {
                        _proximityData.emit(data.proximity.value)
                    }
                }
            }
        }
    }

    fun disableProximityNotification(nodeId: String) {
        featureProximity?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }

    fun nodeHaveProximityFeature():Boolean { return featureProximity!=null}

    fun stopDemo(nodeId: String) {
        featureSensorFusion?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }

        disableProximityNotification(nodeId)

        featureFreeFall?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }

    fun resetCubeCalibration(nodeId: String) {
        coroutineScope.launch {
            featureSensorFusion?.let { feature ->
                startCalibration(feature,nodeId)
            }
        }
    }

    fun getNode(nodeId: String): Boards.Model {
        var boardType = Boards.Model.GENERIC
        val node = blueManager.getNode(nodeId)
        node?.let {
            boardType = node.boardType
        }
        return boardType
    }
}
