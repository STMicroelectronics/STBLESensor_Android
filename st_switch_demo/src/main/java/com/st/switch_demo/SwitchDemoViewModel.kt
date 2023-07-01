/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.switch_demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.switchfeature.SwitchFeature
import com.st.blue_sdk.features.switchfeature.SwitchFeatureInfo
import com.st.blue_sdk.features.switchfeature.SwitchStatusType
import com.st.blue_sdk.features.switchfeature.request.SwitchOff
import com.st.blue_sdk.features.switchfeature.request.SwitchOn
import com.st.blue_sdk.models.Boards
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SwitchDemoViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _switchData = MutableSharedFlow<SwitchFeatureInfo>()
    val switchData: Flow<SwitchFeatureInfo>
        get() = _switchData


    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                SwitchFeature.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it), onFeaturesEnabled = {
                    readFeature(nodeId)
                }).collect {
                    val data = it.data
                    if (data is SwitchFeatureInfo) {
                        _switchData.emit(data)
                    }
                }
            }
        }
    }

    fun getNode(nodeId: String): Boards.Model {
        var boardType = Boards.Model.GENERIC
        val node = blueManager.getNode(nodeId)
        node?.let {
            boardType = node.boardType
        }

        return boardType
    }


    fun writeSwitchCommand(nodeId: String, currentValue: SwitchStatusType) {
        feature?.let {
            viewModelScope.launch {
                if (currentValue == SwitchStatusType.Off) {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = SwitchOn(
                            feature = it as SwitchFeature
                        )
                    )
                } else {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = SwitchOff(
                            feature = it as SwitchFeature
                        )
                    )
                }
            }
        }
    }

    private fun readFeature(
        nodeId: String,
        timeout: Long = 2000
    ) {
        feature?.let { feature ->
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, feature, timeout)
                data.forEach { featureUpdate ->
                    val featureData = featureUpdate.data
                    if (featureData is SwitchFeatureInfo) {
                        _switchData.emit(featureData)
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
