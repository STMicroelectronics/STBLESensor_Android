/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.asset_tracking_event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.asset_tracking_event.AssetTrackingEvent
import com.st.blue_sdk.features.extended.asset_tracking_event.AssetTrackingEventInfo
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventData
import com.st.blue_sdk.features.extended.asset_tracking_event.model.AssetTrackingEventType
import com.st.blue_sdk.features.extended.asset_tracking_event.model.StatusAssetTrackingEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.plus

@HiltViewModel
class AssetTrackingEventFViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) :
    ViewModel() {

    var job: Job? = null

    private val features = mutableListOf<Feature<*>>()

    private val _trackingEventsData: MutableStateFlow<List<Pair<Long, AssetTrackingEventData>>> =
        MutableStateFlow(
            emptyList()
        )
    val trackingEventsData: StateFlow<List<Pair<Long, AssetTrackingEventData>>>
        get() = _trackingEventsData.asStateFlow()

    private val _assetTrackingStatus: MutableStateFlow<Pair<AssetTrackingEventType, StatusAssetTrackingEvent>?> =
        MutableStateFlow(null)
    val assetTrackingStatus: StateFlow<Pair<AssetTrackingEventType, StatusAssetTrackingEvent>?>
        get() = _assetTrackingStatus.asStateFlow()


    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == AssetTrackingEvent.NAME }
                ?.also {
                    features.add(it)
                }
        }

        job?.cancel()

        job = viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is AssetTrackingEventInfo) {

                    when (data.event.value.type) {
                        AssetTrackingEventType.Reset -> {
                            _trackingEventsData.value = emptyList()
                        }

                        AssetTrackingEventType.Fall,
                        AssetTrackingEventType.Shock -> {
                            _trackingEventsData.value += Pair(
                                it.notificationTime.time,
                                data.event.value
                            )
                        }

                        AssetTrackingEventType.Stationary,
                        AssetTrackingEventType.Motion -> {
                            data.event.value.status?.let { status ->
                                _assetTrackingStatus.value =
                                    Pair(data.event.value.type, status)
                            }
                        }

                        AssetTrackingEventType.Null -> {}
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, features)
        }
        job?.cancel()
    }


    fun clearEventList() {
        _trackingEventsData.value = emptyList()
    }
}
