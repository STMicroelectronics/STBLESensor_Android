/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.source_localization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrival
import com.st.blue_sdk.features.direction_of_arrival.DirectionOfArrivalInfo
import com.st.blue_sdk.features.direction_of_arrival.request.SetSensitivityHigh
import com.st.blue_sdk.features.direction_of_arrival.request.SetSensitivityLow
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObjectInfo
import com.st.blue_sdk.models.Boards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SourceLocalizationViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: DirectionOfArrival? = null

    var lowSensitivity: Boolean = false

    private val _directionData =
        MutableStateFlow<DirectionOfArrivalInfo?>(
            null
        )
    val directionData: StateFlow<DirectionOfArrivalInfo?>
        get() = _directionData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                DirectionOfArrival.NAME == it.name
            }?.let { f ->
                feature = f as DirectionOfArrival
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId,
                    listOf(it),
                    onFeaturesEnabled = {
                        enableLowSensitivity(nodeId, lowSensitivity)
                    }
                ).collect {
                    val data = it.data
                    if (data is DirectionOfArrivalInfo) {
                        _directionData.emit(data)
                    }
                }
            }
        }
    }

    fun getNode(nodeId: String): Boards.Model {
        var boardType = Boards.Model.GENERIC
        val node = blueManager.getNode(nodeId)
        node?.let {
            boardType = node.boardType
        }
        return boardType
    }

    fun enableLowSensitivity(nodeId: String, sensitivityLow: Boolean) {
        feature?.let {
            lowSensitivity = sensitivityLow
            viewModelScope.launch {
                if (sensitivityLow) {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = SetSensitivityLow(feature = it)
                    )
                } else {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = SetSensitivityHigh(feature = it)
                    )
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
}
