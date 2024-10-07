/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.proximity_gesture_recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.proximity_gesture.ProximityGesture
import com.st.blue_sdk.features.proximity_gesture.ProximityGestureInfo
import com.st.blue_sdk.features.proximity_gesture.ProximityGestureType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProximityGestureRecognitionViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _gestureData =
        MutableStateFlow<Pair<ProximityGestureInfo, Long?>>(
            Pair(
                ProximityGestureInfo(
                    gesture = FeatureField(
                        name = "Gesture",
                        value = ProximityGestureType.Unknown
                    )
                ), null
            )
        )
    val gestureData: StateFlow<Pair<ProximityGestureInfo, Long?>>
        get() = _gestureData.asStateFlow()


    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                ProximityGesture.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is ProximityGestureInfo) {
                        _gestureData.emit(Pair(data,it.timeStamp))
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
}
