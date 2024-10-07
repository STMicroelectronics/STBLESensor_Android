/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.activity_recognition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.activity.Activity
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.activity.ActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ActivityRecognitionViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _activityData =
        MutableStateFlow<Pair<ActivityInfo, Long?>>(
            Pair(
                ActivityInfo(
                    activity = FeatureField(
                        value = ActivityType.NoActivity,
                        name = "Activity"
                    ),
                    algorithm = FeatureField(
                        value = ActivityInfo.ALGORITHM_NOT_DEFINED,
                        name = "Algorithm"
                    ),
                    date = FeatureField(
                        value = Date(),
                        name = "Date"
                    )
                ), null
            )
        )
    val activityData: StateFlow<Pair<ActivityInfo, Long?>>
        get() = _activityData.asStateFlow()


    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                Activity.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it),
                    onFeaturesEnabled = {
                        readFeature(nodeId)
                    }).collect {
                    val data = it.data
                    if (data is ActivityInfo) {
                        _activityData.emit(Pair(data, it.timeStamp))
                    }
                }
            }
        }
    }

    private fun readFeature(
        nodeId: String,
        timeout: Long = 2000
    ) {
        feature?.let {
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, it, timeout)
                data.forEach { featureUpdate ->
                    val dataFeature = featureUpdate.data
                    if (dataFeature is ActivityInfo) {
                        _activityData.emit(Pair(dataFeature,featureUpdate.timeStamp))
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
