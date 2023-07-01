/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.neai_classification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassification
import com.st.blue_sdk.features.extended.neai_class_classification.NeaiClassClassificationInfo
import com.st.blue_sdk.features.extended.neai_class_classification.request.WriteStarClassificationCommand
import com.st.blue_sdk.features.extended.neai_class_classification.request.WriteStopClassificationCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NeaiClassificationViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) :
    ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _classificationData = MutableSharedFlow<NeaiClassClassificationInfo>()
    val classificationData: Flow<NeaiClassClassificationInfo>
        get() = _classificationData


    fun writeStopClassificationCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiClassClassification.NAME
        }?.let {
            val feature = it as NeaiClassClassification

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStopClassificationCommand(feature = feature)
                )
            }
        }
    }

    fun writeStartClassificationCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == NeaiClassClassification.NAME
        }?.let {
            val feature = it as NeaiClassClassification

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = WriteStarClassificationCommand(feature = feature)
                )
            }
        }
    }


    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == NeaiClassClassification.NAME }
                ?.also {
                    features.add(it)
                }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is NeaiClassClassificationInfo) {
                    _classificationData.emit(data)
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
