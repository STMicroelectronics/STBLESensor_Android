/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.sensor_fusion

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusion
import com.st.blue_sdk.features.sensor_fusion.MemsSensorFusionCompat
import com.st.blue_sdk.services.calibration.CalibrationService
import com.st.blue_sdk.services.calibration.CalibrationServiceImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SensorFusionCalibrationViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService
) : ViewModel() {

    val calibrationStatus = MutableLiveData<Boolean>()

    fun startCalibration(nodeId: String) {
        viewModelScope.launch {
            blueManager.nodeFeatures(nodeId)
                .find { MemsSensorFusionCompat.NAME == it.name || MemsSensorFusion.NAME == it.name }
                ?.let { feature ->

                    calibrationService.startCalibration(
                        feature = feature,
                        nodeId = nodeId
                    ).also {
                        calibrationStatus.postValue(it.status)
                    }

                    blueManager.getConfigControlUpdates(nodeId).collect {
                        if (it is CalibrationStatus) {
                            calibrationStatus.postValue(it.status)
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
                        calibrationStatus.postValue(true)
                    }
                }
            }
        }
    }
}
