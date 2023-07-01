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
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.compass.Compass
import com.st.blue_sdk.features.compass.CompassInfo
import com.st.blue_sdk.services.calibration.CalibrationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompassViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val calibrationService: CalibrationService,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    val compassInfo = MutableLiveData<CompassInfo>()
    val calibrationStatus = MutableLiveData<Boolean>()

    private var compassFeature: Compass? = null

    private suspend fun getCalibration(feature: Feature<*>, nodeId: String) {
        calibrationService.getCalibration(
            feature = feature,
            nodeId = nodeId
        ).also {
            calibrationStatus.postValue(it.status)
        }
    }

    fun startDemo(nodeId: String) {
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
                    calibrationStatus.postValue(it.status)
                }
            }
        }

        viewModelScope.launch {
            compassFeature?.let { feature ->
                blueManager.getFeatureUpdates(nodeId, listOf(feature)).collect {
                    val data = it.data
                    if (data is CompassInfo) {
                        compassInfo.postValue(data)
                    }
                }
            }
        }

        viewModelScope.launch {
            compassFeature?.let { feature ->
                getCalibration(feature,nodeId)
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
