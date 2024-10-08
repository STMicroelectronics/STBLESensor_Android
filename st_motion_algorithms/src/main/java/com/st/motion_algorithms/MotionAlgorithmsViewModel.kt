/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.motion_algorithms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.motion_algorithm.AlgorithmType
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithm
import com.st.blue_sdk.features.extended.motion_algorithm.MotionAlgorithmInfo
import com.st.blue_sdk.features.extended.motion_algorithm.request.EnableMotionAlgorithm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MotionAlgorithmsViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _motAlgData =
        MutableStateFlow(
            MotionAlgorithmInfo(
                algorithmType = FeatureField(
                    value = AlgorithmType.Unknown,
                    name = "algorithm Type"
                ),
                statusType = FeatureField(
                    value = 0,
                    name = "Pose"
                )
            )
        )
    val motAlgData: StateFlow<MotionAlgorithmInfo>
        get() = _motAlgData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                MotionAlgorithm.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is MotionAlgorithmInfo) {
                        _motAlgData.emit(data)
                    }
                }
            }
        }
    }


    fun setAlgorithmTypeCommand(nodeId: String, algorithmType: AlgorithmType) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == MotionAlgorithm.NAME
        }?.let {
            val feature = it as MotionAlgorithm

            viewModelScope.launch {

                _motAlgData.emit(
                    MotionAlgorithmInfo(
                        algorithmType = FeatureField(
                            value = algorithmType,
                            name = "algorithm Type"
                        ),
                        statusType = FeatureField(
                            value = 0,
                            name = "Pose"
                        )
                    )
                )

                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = EnableMotionAlgorithm(
                        feature = feature,
                        algorithmType = algorithmType
                    )
                )
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
}
