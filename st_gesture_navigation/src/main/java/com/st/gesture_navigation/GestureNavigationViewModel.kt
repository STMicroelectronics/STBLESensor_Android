/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.gesture_navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigation
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationButton
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationGestureType
import com.st.blue_sdk.features.extended.gesture_navigation.GestureNavigationInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestureNavigationViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _gestureData =
        MutableStateFlow<Pair<GestureNavigationInfo, Long?>>(
            Pair(
                GestureNavigationInfo(
                    gesture = FeatureField(
                        name = "Gesture",
                        value = GestureNavigationGestureType.Undefined
                    ),
                    button = FeatureField(
                        name = "Button",
                        value = GestureNavigationButton.Undefined
                    )
                ), null
            )
        )
    val gestureData: StateFlow<Pair<GestureNavigationInfo, Long?>>
        get() = _gestureData.asStateFlow()

    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == GestureNavigation.NAME }
                ?.also {
                    features.add(it)
                }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is GestureNavigationInfo) {
                    if ((data.button.value != GestureNavigationButton.Error) && (data.gesture.value != GestureNavigationGestureType.Error)) {
                        _gestureData.emit(Pair(data, it.timeStamp))
                    }
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
