/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.piano

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.piano.Piano
import com.st.blue_sdk.features.extended.piano.PianoCommand
import com.st.blue_sdk.features.extended.piano.PianoInfo
import com.st.blue_sdk.features.extended.piano.request.CommandPianoSound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PianoViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) :
    ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _pianoData = MutableSharedFlow<PianoInfo>()
    val pianoData: Flow<PianoInfo>
        get() = _pianoData


    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == Piano.NAME }
                ?.also {
                    features.add(it)
                }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features).collect {
                val data = it.data
                if (data is PianoInfo) {
                    _pianoData.emit(data)
                }
            }
        }
    }

    fun writePianoStartCommand(key: Byte, nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == Piano.NAME
        }?.let {
            val feature = it as Piano

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = CommandPianoSound(
                        feature = feature,
                        key = key,
                        command = PianoCommand.Start
                    )
                )
            }
        }
    }

    fun writePianoStopCommand(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == Piano.NAME
        }?.let {
            val feature = it as Piano

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = CommandPianoSound(
                        feature = feature,
                        command = PianoCommand.Stop
                    )
                )
            }
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, features)
        }
    }
}
