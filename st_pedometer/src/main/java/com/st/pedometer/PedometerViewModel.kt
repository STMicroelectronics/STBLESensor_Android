/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pedometer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.pedometer.Pedometer
import com.st.blue_sdk.features.pedometer.PedometerInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PedometerViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _stepData =
        MutableStateFlow<Pair<PedometerInfo, Long?>>(
            Pair(
                PedometerInfo(
                    steps = FeatureField(
                        name = "Steps",
                        value = 0
                    ),
                    frequency = FeatureField(
                        name = "Frequency",
                        value = 0
                    )
                ), null
            )
        )
    val stepData: StateFlow<Pair<PedometerInfo, Long?>>
        get() = _stepData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                Pedometer.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(
                    nodeId,
                    listOf(it), onFeaturesEnabled = {
                        readFeature(nodeId)
                    }
                ).collect {
                    val data = it.data
                    if (data is PedometerInfo) {
                        _stepData.emit(Pair(data, it.timeStamp))
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

    fun readFeature(
        nodeId: String,
        timeout: Long = 2000
    ) {
        feature?.let {
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, it, timeout)
                data.forEach { featureUpdate ->
                    val dataFeature = featureUpdate.data
                    if (dataFeature is PedometerInfo) {
                        _stepData.emit(Pair(dataFeature, featureUpdate.timeStamp))
                    }
                }
            }
        }
    }
}
