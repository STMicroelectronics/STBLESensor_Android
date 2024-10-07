/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.nfc_writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.extended.json_nfc.JsonNFC
import com.st.blue_sdk.features.extended.json_nfc.answer.JsonNFCResponse
import com.st.blue_sdk.features.extended.json_nfc.request.JsonCommand
import com.st.blue_sdk.features.extended.json_nfc.request.JsonNFCFeatureWriteCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NfcWritingViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val features = mutableListOf<Feature<*>>()

    private val _supportedModes =
        MutableStateFlow(
            JsonNFCResponse(
                supportedModes = FeatureField(
                    name = "SupportedModes",
                    value = null
                )
            )
        )
    val supportedModes: StateFlow<JsonNFCResponse>
        get() = _supportedModes.asStateFlow()

    fun writeJsonCommand(nodeId: String, command: JsonCommand) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == JsonNFC.NAME
        }?.let {
            val feature = it as JsonNFC

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = JsonNFCFeatureWriteCommand(
                        feature = feature,
                        nfcCommand = command
                    )
                )
            }
        }
    }

    fun startDemo(nodeId: String) {
        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).find { JsonNFC.NAME == it.name }
                ?.let { f -> features.add(f) }

            blueManager.getFeatureUpdates(nodeId = nodeId, features = features,
                onFeaturesEnabled = {
                    writeJsonCommand(nodeId, JsonCommand(Command = JsonCommand.ReadModes))
                })
                .flowOn(Dispatchers.IO)
                .onEach {
                    val data = it.data
                    if (data is JsonNFCResponse) {
                        _supportedModes.emit(data)
                    }
                }
                .launchIn(viewModelScope)
        }

//        if (features.isNotEmpty()) {
//            val feature = features.firstOrNull { it is JsonNFC } as JsonNFC?
//
//            if (feature != null) {
//                viewModelScope.launch {
//                    writeJsonCommand(nodeId, JsonCommand(Command = JsonCommand.ReadModes))
//                }
//            }
//        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, features)
        }
    }
}
