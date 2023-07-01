/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.environmental

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.humidity.Humidity
import com.st.blue_sdk.features.humidity.HumidityInfo
import com.st.blue_sdk.features.pressure.Pressure
import com.st.blue_sdk.features.pressure.PressureInfo
import com.st.blue_sdk.features.temperature.Temperature
import com.st.blue_sdk.features.temperature.TemperatureInfo
import com.st.environmental.model.EnvironmentalValues
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnvironmentalViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var environmentalFeatures: MutableList<Feature<*>> = mutableListOf()

    var environmentalValues by mutableStateOf(
        value =
        EnvironmentalValues(
            temperatures = emptyMap(),
            hasTemperatures = false,
            humidity = 0f,
            hasHumidity = false,
            pressure = 0f,
            hasPressure = false
        )
    )
        private set

    fun startDemo(nodeId: String) {

        if (environmentalFeatures.isEmpty()) {
            val filteredFeatures = blueManager.nodeFeatures(nodeId = nodeId).asSequence().filter {
                it.name == Temperature.NAME || it.name == Pressure.NAME || it.name == Humidity.NAME
            }.toList()
            environmentalFeatures.addAll(filteredFeatures)
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId = nodeId, features = environmentalFeatures)
                .collect {
                    when (val data = it.data) {
                        is TemperatureInfo -> {
                            val newTemperatures = environmentalValues.temperatures.toMutableMap()
                            newTemperatures[data.featureId.value] = data.temperature.value
                            environmentalValues =
                                environmentalValues.copy(temperatures = newTemperatures)

                            environmentalValues =
                            environmentalValues.copy(hasTemperatures = true)
                        }

                        is HumidityInfo -> {
                            environmentalValues =
                                environmentalValues.copy(humidity = data.humidity.value)

                            environmentalValues =
                                environmentalValues.copy(hasHumidity = true)
                        }

                        is PressureInfo -> {
                            environmentalValues =
                                environmentalValues.copy(pressure = data.pressure.value)

                            environmentalValues =
                                environmentalValues.copy(hasPressure = true)
                        }
                    }
                }
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId = nodeId, features = environmentalFeatures)
        }
    }

    fun readFeatures(
        nodeId: String,
        timeout: Long = 2000
    ) {

        environmentalFeatures.forEach { feature ->
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, feature, timeout)
                data.forEach { featureUpdate ->
                    val featureData = featureUpdate.data

                    when (val dataInfo = featureData) {
                        is TemperatureInfo -> {
                            val newTemperatures = environmentalValues.temperatures.toMutableMap()
                            newTemperatures[dataInfo.featureId.value] = dataInfo.temperature.value
                            environmentalValues =
                                environmentalValues.copy(temperatures = newTemperatures)

                            environmentalValues =
                                environmentalValues.copy(hasTemperatures = true)
                        }

                        is HumidityInfo -> {
                            environmentalValues =
                                environmentalValues.copy(humidity = dataInfo.humidity.value)

                            environmentalValues =
                                environmentalValues.copy(hasHumidity = true)
                        }

                        is PressureInfo -> {
                            environmentalValues =
                                environmentalValues.copy(pressure = dataInfo.pressure.value)

                            environmentalValues =
                                environmentalValues.copy(hasPressure = true)
                        }
                    }
                }
            }
        }
    }
}
