/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.acceleration_event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.acceleration_event.AccelerationEvent
import com.st.blue_sdk.features.acceleration_event.AccelerationEventInfo
import com.st.blue_sdk.features.acceleration_event.DetectableEventType
import com.st.blue_sdk.features.acceleration_event.request.EnableDetectionAccelerationEvent
import com.st.blue_sdk.models.Boards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccelerationEventViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _accEventData =
        MutableStateFlow<Pair<AccelerationEventInfo, Long?>>(
            Pair(AccelerationEventInfo.emptyAccelerationEventInfo(), null)
        )
    val accEventData: StateFlow<Pair<AccelerationEventInfo, Long?>>
        get() = _accEventData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                AccelerationEvent.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is AccelerationEventInfo) {
//                        Log.i(
//                            "AccelerationEventViewModel",
//                            "Ts=[${it.timeStamp}] AC=[${data.accEvent}] S=[${data.numSteps}]"
//                        )
                        _accEventData.emit(Pair(data, it.timeStamp))
                    }
                }
            }
        }
    }

    fun getBoardType(nodeId: String): Boards.Model {
        var boardType = Boards.Model.GENERIC
        val node = blueManager.getNode(nodeId)
        node?.let {
            boardType = node.boardType
        }

        return boardType
    }


    fun setDetectableEventCommand(nodeId: String, event: DetectableEventType, enable: Boolean) {
        feature?.let {
            val feature = it as AccelerationEvent

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = EnableDetectionAccelerationEvent(
                        feature = feature,
                        event = event,
                        enable = enable
                    )
                )
                _accEventData.emit(Pair(AccelerationEventInfo.emptyAccelerationEventInfo(), null))
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
