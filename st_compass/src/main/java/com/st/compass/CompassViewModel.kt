/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.compass

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.compass.CompassInfo
import com.st.blue_sdk.services.calibration.CalibrationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompassViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val _compassInfo =
        MutableStateFlow(
            CompassInfo(angle = FeatureField(value = 0f, name = "Angle"))
        )
    val compassInfo: StateFlow<CompassInfo>
        get() = _compassInfo.asStateFlow()

    private val _calibrationStatus =
        MutableStateFlow(
            false
        )
    val calibrationStatus: StateFlow<Boolean>
        get() = _calibrationStatus.asStateFlow()


    private var compassFeature: Compass? = null

    private var localNodeId: String? = null

    private suspend fun getCalibration(feature: Feature<*>, nodeId: String) {
        calibrationService.getCalibration(
            feature = feature,
            nodeId = nodeId
        ).also {
            _calibrationStatus.emit(it.status)
        }
    }

    fun startCalibration() {
        localNodeId?.let { nodeId ->
            compassFeature?.let { feature ->
                viewModelScope.launch {
                    _calibrationStatus.emit(false)
                    calibrationService.startCalibration(
                        feature = feature,
                        nodeId = nodeId
                    )
                }
            }
        }
    }

    fun startDemo(nodeId: String) {
        localNodeId = nodeId
        if (compassFeature == null) {
            blueManager.nodeFeatures(nodeId).find {
                Compass.NAME == it.name
            }?.let { feature ->
                compassFeature = feature as Compass
            }
        }

        viewModelScope.launch {
            blueManager.getConfigControlUpdates(nodeId = nodeId).collect {
                if (it is CalibrationStatus) {
                    _calibrationStatus.emit(it.status)
                }
            }
        }

        viewModelScope.launch {
            compassFeature?.let { feature ->
                blueManager.getFeatureUpdates(nodeId, listOf(feature)).collect {
                    val data = it.data
                    if (data is CompassInfo) {
                        _compassInfo.emit(data)
                    }
                }
            }
        }

        viewModelScope.launch {
            compassFeature?.let { feature ->
                getCalibration(feature, nodeId)
            }
        }
    }

    fun stopDemo(nodeId: String) {
        compassFeature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }
}
