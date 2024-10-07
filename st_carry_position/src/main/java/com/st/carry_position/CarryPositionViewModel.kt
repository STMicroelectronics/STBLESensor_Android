/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.carry_position

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.activity.ActivityType
import com.st.blue_sdk.features.carry_position.CarryPosition
import com.st.blue_sdk.features.carry_position.CarryPositionInfo
import com.st.blue_sdk.features.carry_position.CarryPositionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CarryPositionViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _positionData =
        MutableStateFlow<Pair<CarryPositionInfo, Long?>>(
            Pair(
                CarryPositionInfo(
                    position = FeatureField(
                        name = "Carry Position",
                        value = CarryPositionType.Unknown
                    )
                ), null
            )
        )
    val positionData: StateFlow<Pair<CarryPositionInfo, Long?>>
        get() = _positionData.asStateFlow()


    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                CarryPosition.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is CarryPositionInfo) {
                        _positionData.emit(Pair(data, it.timeStamp))
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
