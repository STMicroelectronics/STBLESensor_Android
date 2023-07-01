/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_anomaly_detection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetection
import com.st.blue_sdk.features.extended.neai_anomaly_detection.NeaiAnomalyDetectionInfo
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteDetectionCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteLearningCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteResetKnowledgeCommand
import com.st.blue_sdk.features.extended.neai_anomaly_detection.request.WriteStopCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeaiAnomalyDetectionViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) :
    ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _anomalyDetectionData = MutableSharedFlow<NeaiAnomalyDetectionInfo>()
    val anomalyDetectionData: Flow<NeaiAnomalyDetectionInfo>
        get() = _anomalyDetectionData

    fun writeStopCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiAnomalyDetection.NAME
        }?.let {
            val feature = it as NeaiAnomalyDetection

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStopCommand(feature = feature)
                )
            }
        }
    }

    fun writeDetectionCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiAnomalyDetection.NAME
        }?.let {
            val feature = it as NeaiAnomalyDetection

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteDetectionCommand(feature = feature)
                )
            }
        }
    }

    fun writeLearningCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiAnomalyDetection.NAME
        }?.let {
            val feature = it as NeaiAnomalyDetection

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteLearningCommand(feature = feature)
                )
            }
        }
    }

    fun writeResetLearningCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiAnomalyDetection.NAME
        }?.let {
            val feature = it as NeaiAnomalyDetection

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteResetKnowledgeCommand(feature = feature)
                )
            }
        }
    }

    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == NeaiAnomalyDetection.NAME }
                ?.also {
                    features.add(it)
                }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is NeaiAnomalyDetectionInfo) {
                    _anomalyDetectionData.emit(data)
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
