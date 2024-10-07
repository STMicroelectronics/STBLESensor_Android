/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.audio_classification_demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.audio_classification.AudioClassType
import com.st.blue_sdk.features.extended.audio_classification.AudioClassification
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo.Companion.ALGORITHM_NOT_DEFINED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioClassificationDemoViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null


    private val _audioClassificationData =
        MutableStateFlow<Pair<AudioClassificationInfo, Long?>>(
            Pair(AudioClassificationInfo(
                classification = FeatureField(
                    value = AudioClassType.Unknown,
                    name = "AudioClassification"
                ),
                algorithm =  FeatureField(
                    value = ALGORITHM_NOT_DEFINED,
                    name = "Algorithm"
                )
            ),null)
        )
    val audioClassificationData: StateFlow<Pair<AudioClassificationInfo, Long?>>
        get() = _audioClassificationData.asStateFlow()


    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                AudioClassification.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(
                    nodeId,
                    listOf(it),
                    onFeaturesEnabled = {
                        readFeature(nodeId)
                    }
                ).collect {
                    val data = it.data
                    if (data is AudioClassificationInfo) {
                        _audioClassificationData.emit(Pair(data,it.timeStamp))
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
                    if (dataFeature is AudioClassificationInfo) {
                        _audioClassificationData.emit(Pair(dataFeature,featureUpdate.timeStamp))
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
