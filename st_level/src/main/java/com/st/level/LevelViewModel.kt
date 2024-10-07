/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.level

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.euler_angle.EulerAngle
import com.st.blue_sdk.features.extended.euler_angle.EulerAngleInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LevelViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _levelData =
        MutableStateFlow(EulerAngleInfo(
            yaw = FeatureField(
                value = 0f,
                name = "Yaw"
            ),
            pitch = FeatureField(
                value = 0f,
                name = "Pitch"
            ),
            roll = FeatureField(
                value = 0f,
                name = "Roll"
            )
        ))
    val levelData: StateFlow<EulerAngleInfo>
        get() = _levelData.asStateFlow()

    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == EulerAngle.NAME }?.also {
                features.add(it)
            }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is EulerAngleInfo) {
                    _levelData.emit(data)
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, features)
        }
    }
}
