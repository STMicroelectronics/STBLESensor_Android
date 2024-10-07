/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.motion_intensity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.motion_intensity.MotionIntensity
import com.st.blue_sdk.features.motion_intensity.MotionIntensityInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotionIntensityViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val angleOffset =
        listOf(-135.0f, -108.0f, -81.0f, -54.0f, -27.0f, 00.0f, 27.0f, 54.0f, 81.0f, 108.0f, 135.0f)

    private var feature: Feature<*>? = null

    private val _motIntData =
        MutableStateFlow(
            MotionIntensityInfo(intensity = FeatureField(name = "Intensity", value = 0))
        )
    val motIntData: StateFlow<MotionIntensityInfo>
        get() = _motIntData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                MotionIntensity.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is MotionIntensityInfo) {
                        _motIntData.emit(data)
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        feature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }

    fun getAngle(intensity: Short): Float {
        return if (intensity.toInt() in angleOffset.indices) {
            angleOffset[intensity.toInt()]
        } else {
            angleOffset[0]
        }
    }
}
