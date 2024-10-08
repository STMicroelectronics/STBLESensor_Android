/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.fitness

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivity
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivityInfo
import com.st.blue_sdk.features.extended.fitness_activity.FitnessActivityType
import com.st.blue_sdk.features.extended.fitness_activity.request.EnableActivityDetection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FitnessActivityViewModel
@Inject internal constructor(
    // Inject BlueManager
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val _currentActivity =
        MutableStateFlow(
            FitnessActivityType.NoActivity
        )
    val currentActivity: StateFlow<FitnessActivityType>
        get() = _currentActivity.asStateFlow()

    private val _currentCounter =
        MutableStateFlow(0)
    val currentCounter: StateFlow<Int>
        get() = _currentCounter.asStateFlow()
    
    private var fitnessActivityFeature: FitnessActivity? = null


    fun setActivity(nodeId: String, type: FitnessActivityType) {
        viewModelScope.launch {
            fitnessActivityFeature?.let {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = EnableActivityDetection(feature = it, activityType = type)
                )
            }
        }
    }

    fun startDemo(nodeId: String) {
        // Find the feature
        if (fitnessActivityFeature == null) {
            blueManager.nodeFeatures(nodeId).find {
                FitnessActivity.NAME == it.name
            }?.let { feature ->
                fitnessActivityFeature = feature as FitnessActivity
            }
        }

        // Listen the feature updates
        viewModelScope.launch {
            fitnessActivityFeature?.let { feature ->
                blueManager.getFeatureUpdates(nodeId, listOf(feature)).collect {
                    val data = it.data
                    if(data is FitnessActivityInfo) {
                        // Update UI livedata
                        data.count.value.let { count ->
                            _currentCounter.emit(count)
                        }
                        data.activity.value.let { type ->
                            _currentActivity.emit(type)
                        }
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        // Stop listen feature updates
        fitnessActivityFeature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }
}
