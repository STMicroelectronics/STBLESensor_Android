/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.heart_rate_demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.external.std.BodySensorLocation
import com.st.blue_sdk.features.external.std.BodySensorLocationInfo
import com.st.blue_sdk.features.external.std.BodySensorLocationType
import com.st.blue_sdk.features.external.std.HeartRate
import com.st.blue_sdk.features.external.std.HeartRateInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeartRateDemoViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var featureHeartRate: Feature<*>? = null
    private var featureBodyLocation: Feature<*>? = null

    private var features: MutableList<Feature<*>> = mutableListOf()

    private val _heartData =
        MutableStateFlow<Pair<HeartRateInfo?, Long>>(
            Pair(null,0)
        )
    val heartData: StateFlow<Pair<HeartRateInfo?, Long>>
        get() = _heartData.asStateFlow()

    private val _locationData =
        MutableStateFlow(
            BodySensorLocationInfo(bodySensorLocation = FeatureField(value= BodySensorLocationType.NotKnown, name = "Body Sensor Location"))
        )
    val locationData: StateFlow<BodySensorLocationInfo>
        get() = _locationData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).find {
                HeartRate.NAME == it.name
            }?.let { f ->
                featureHeartRate = f
                features.add(f)
            }

            blueManager.nodeFeatures(nodeId).find {
                BodySensorLocation.NAME == it.name
            }?.let { f ->
                featureBodyLocation = f
                features.add(f)
            }
        }

        if (features.isNotEmpty()) {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(
                    nodeId,
                    features
                ).collect {
                    val data = it.data
                    if (data is HeartRateInfo) {
                        _heartData.emit(Pair(data,it.timeStamp))
                    } else if (data is BodySensorLocationInfo) {
                        _locationData.emit(data)
                    }
                }
            }
        }

        //Read one time the Feature Location
        readFeatureLocation(nodeId)
    }

    private fun readFeatureLocation(
        nodeId: String,
        timeout: Long = 2000
    ) {
        featureBodyLocation?.let {
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, it, timeout)
                data.forEach { featureUpdate ->
                    val dataFeature = featureUpdate.data
                    if (dataFeature is BodySensorLocationInfo) {
                        _locationData.emit(dataFeature)
                    }
                }
            }
        }
    }

    fun readFeatureHeartRate(
        nodeId: String,
        timeout: Long = 2000
    ) {
        featureHeartRate?.let {
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, it, timeout)
                data.forEach { featureUpdate ->
                    val dataFeature = featureUpdate.data
                    if (dataFeature is HeartRateInfo) {
                        _heartData.emit(Pair(dataFeature,featureUpdate.timeStamp))
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        if(features.isNotEmpty()) {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId,features)
            }
        }
    }
}
