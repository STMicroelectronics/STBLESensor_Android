/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_extrapolation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.neai_extrapolation.NeaiExtrapolation
import com.st.blue_sdk.features.extended.neai_extrapolation.NeaiExtrapolationInfo
import com.st.blue_sdk.features.extended.neai_extrapolation.request.WriteStartExtrapolationCommand
import com.st.blue_sdk.features.extended.neai_extrapolation.request.WriteStopExtrapolationCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeaiExtrapolationViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _extrapolationData = MutableSharedFlow<NeaiExtrapolationInfo>()
    val extrapolationData: Flow<NeaiExtrapolationInfo>
        get() = _extrapolationData


    fun writeStartCommand(nodeId: String) {
        feature?.let {
            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStartExtrapolationCommand(feature = it as NeaiExtrapolation)
                )
            }
        }
    }

    fun writeStopCommand(nodeId: String) {
        feature?.let {
            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStopExtrapolationCommand(feature = it as NeaiExtrapolation)
                )
            }
        }
    }

    fun startDemo(nodeId: String) {

        if (feature == null) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == NeaiExtrapolation.NAME }
                ?.let { feature = it }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(feature!!)).collect {
                    val data = it.data
                    if (data is NeaiExtrapolationInfo) {
                        _extrapolationData.emit(data)
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        feature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(feature!!))
            }
        }
    }
}
