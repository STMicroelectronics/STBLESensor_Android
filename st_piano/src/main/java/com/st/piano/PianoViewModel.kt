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
import com.st.blue_sdk.features.extended.piano.request.CommandPianoSound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PianoViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) :
    ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val mKeyByte = byteArrayOf(
        1, //NOTE_C1
        2, // NOTE_CS1
        3, // NOTE_D1
        4, // NOTE_DS1
        5, // NOTE_E1
        6, // NOTE_F1
        7, // NOTE_FS1
        8, // NOTE_G1
        9, // NOTE_GS1
        10, // NOTE_A1
        11, // NOTE_AS1
        12, // NOTE_B1
        13, // NOTE_C2
        14, // NOTE_CS2
        15, // NOTE_D2
        16, // NOTE_DS2
        17, // NOTE_E2
        18, // NOTE_F2
        19, // NOTE_FS2
        20, // NOTE_G2
        21, // NOTE_GS2
        22, // NOTE_A2
        23, // NOTE_AS2
        24 // NOTE_B2
    )

    fun startDemo(nodeId: String) {

        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).firstOrNull { it.name == Piano.NAME }
                ?.also {
                    features.add(it)
                }
        }

        viewModelScope.launch {
            blueManager.getFeatureUpdates(nodeId, features)
        }
    }

    fun writePianoStartCommand(sound: Int, nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == Piano.NAME
        }?.let {
            val feature = it as Piano

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = CommandPianoSound(
                        feature = feature,
                        key = mKeyByte[sound],
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
