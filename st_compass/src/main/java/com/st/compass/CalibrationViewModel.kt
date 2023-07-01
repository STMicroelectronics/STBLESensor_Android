/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.compass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.CalibrationStatus
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.services.calibration.CalibrationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalibrationViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService
) : ViewModel() {

    val calibrationStatus = MutableLiveData<Boolean>()

    fun startCalibration(nodeId: String) {
        viewModelScope.launch {
            blueManager.nodeFeatures(nodeId).find { Compass.NAME == it.name }?.let { feature ->
                val compassFeature = feature as Compass

                calibrationService.startCalibration(
                    feature = compassFeature,
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
    }
}
