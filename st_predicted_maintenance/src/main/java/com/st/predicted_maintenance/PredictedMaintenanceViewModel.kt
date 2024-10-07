/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.predicted_maintenance

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.extended.predictive.PredictiveAccelerationStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveAccelerationStatusInfo
import com.st.blue_sdk.features.extended.predictive.PredictiveFrequencyStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveFrequencyStatusInfo
import com.st.blue_sdk.features.extended.predictive.PredictiveSpeedStatus
import com.st.blue_sdk.features.extended.predictive.PredictiveSpeedStatusInfo
import com.st.blue_sdk.features.extended.predictive.Status
import com.st.blue_sdk.models.RssiData
import com.st.predicted_maintenance.utilities.Point
import com.st.predicted_maintenance.utilities.ViewStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PredictedMaintenanceViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var featureSpeedStatus: Feature<*>? = null
    private var featureAccelerationStatus: Feature<*>? = null
    private var featureFrequencyStatus: Feature<*>? = null

    private val _speedData =
        MutableStateFlow<ViewStatus?>(null)
    val speedData: StateFlow<ViewStatus?>
        get() = _speedData.asStateFlow()

    private val _accData =
        MutableStateFlow<ViewStatus?>(null)
    val accData: StateFlow<ViewStatus?>
        get() = _accData.asStateFlow()

    private val _freqData =
        MutableStateFlow<ViewStatus?>(null)
    val freqData: StateFlow<ViewStatus?>
        get() = _freqData.asStateFlow()

    val mSpeedStatusVisibility = MutableLiveData(false)
    val mAccStatusVisibility = MutableLiveData(false)
    val mFrequencyStatusVisibility = MutableLiveData(false)

    fun startDemo(nodeId: String) {
        //Initial Unknown Status
        val unknown =
            ViewStatus(xStatus = Status.UNKNOWN, yStatus = Status.UNKNOWN, zStatus = Status.UNKNOWN)

        if (featureSpeedStatus == null) {
            blueManager.nodeFeatures(nodeId).find {
                PredictiveSpeedStatus.NAME == it.name
            }?.let { f ->
                featureSpeedStatus = f
                mSpeedStatusVisibility.postValue(true)
            }
        }

        featureSpeedStatus?.let {
            viewModelScope.launch {
                //Initial value
                _speedData.emit(unknown)
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is PredictiveSpeedStatusInfo) {
                        _speedData.emit(
                            ViewStatus(
                                xStatus = data.statusX.value,
                                yStatus = data.statusY.value,
                                zStatus = data.statusZ.value,
                                x = Point(value = data.speedX.value),
                                y = Point(value = data.speedY.value),
                                z = Point(value = data.speedZ.value)
                            )
                        )
                    }
                }
            }
        }

        if (featureAccelerationStatus == null) {
            blueManager.nodeFeatures(nodeId).find {
                PredictiveAccelerationStatus.NAME == it.name
            }?.let { f ->
                featureAccelerationStatus = f
                mAccStatusVisibility.postValue(true)
            }
        }

        featureAccelerationStatus?.let {
            viewModelScope.launch {
                //Initial value
                _accData.emit(unknown)
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is PredictiveAccelerationStatusInfo) {
                        _accData.emit(
                            ViewStatus(
                                xStatus = data.statusX.value,
                                yStatus = data.statusY.value,
                                zStatus = data.statusZ.value,
                                x = Point(value = data.accX.value),
                                y = Point(value = data.accY.value),
                                z = Point(value = data.accZ.value)
                            )
                        )
                    }
                }
            }
        }

        if (featureFrequencyStatus == null) {
            blueManager.nodeFeatures(nodeId).find {
                PredictiveFrequencyStatus.NAME == it.name
            }?.let { f ->
                featureFrequencyStatus = f
                mFrequencyStatusVisibility.postValue(true)
            }
        }

        featureFrequencyStatus?.let {
            viewModelScope.launch {
                //Initial value
                _freqData.emit(unknown)
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is PredictiveFrequencyStatusInfo) {
                        _freqData.emit(
                            ViewStatus(
                                xStatus = data.statusX.value,
                                yStatus = data.statusY.value,
                                zStatus = data.statusZ.value,
                                x = Point(
                                    freq = data.worstXFreq.value,
                                    value = data.worstXValue.value
                                ),
                                y = Point(
                                    freq = data.worstYFreq.value,
                                    value = data.worstYValue.value
                                ),
                                z = Point(
                                    freq = data.worstZFreq.value,
                                    value = data.worstZValue.value
                                )
                            )
                        )
                    }
                }
            }
        }

    }

    fun stopDemo(nodeId: String) {
        featureSpeedStatus?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }

        featureAccelerationStatus?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }

        featureFrequencyStatus?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }
}
