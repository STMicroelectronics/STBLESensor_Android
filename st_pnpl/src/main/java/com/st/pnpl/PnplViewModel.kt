/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.pnpl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.preferences.StPreferences
import com.st.ui.composables.CommandRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@HiltViewModel
class PnplViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var observeFeatureJob: Job? = null

    private val _modelUpdates =
        MutableStateFlow<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>(
            emptyList()
        )
    val modelUpdates: StateFlow<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>
        get() = _modelUpdates.asStateFlow()

    private val _componentStatusUpdates = MutableStateFlow<List<JsonObject>>(emptyList())
    val componentStatusUpdates: StateFlow<List<JsonObject>>
        get() = _componentStatusUpdates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private val _enableCollapse = MutableStateFlow(false)
    val enableCollapse: StateFlow<Boolean>
        get() = _enableCollapse.asStateFlow()

    private val _lastStatusUpdatedAt = MutableStateFlow(0L)
    val lastStatusUpdatedAt: StateFlow<Long>
        get() = _lastStatusUpdatedAt.asStateFlow()

    private fun sendGetAllCommand(nodeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val feature =
                blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }
                    ?: return@launch

            if (feature is PnPL) {
                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(feature = feature, cmd = PnPLCmd.ALL)
                )
            }
        }
    }

    fun getModel(nodeId: String, demoName: String?) {
        viewModelScope.launch {
            _isLoading.value = true

            _modelUpdates.value =
                blueManager.getDtmiModel(nodeId = nodeId, isBeta = stPreferences.isBetaApplication())?.extractComponents(demoName = demoName)
                    ?: emptyList()

            //If we want that Demo Settings start with the components Expanded..
            //_enableCollapse.value = demoName.isNullOrEmpty()

            _enableCollapse.value =true

            _isLoading.value = false

            sendGetAllCommand(nodeId = nodeId)
        }
    }

    fun sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        viewModelScope.launch {
            _isLoading.value = true

            val feature =
                blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }
                    ?: return@launch

            if (feature is PnPL) {
                value?.let {
                    blueManager.writeFeatureCommand(
                        responseTimeout = 0,
                        nodeId = nodeId, featureCommand = PnPLCommand(
                            feature = feature,
                            cmd = PnPLCmd(
                                component = name,
                                command = it.commandName,
                                fields = it.request
                            )
                        )
                    )

                    sendGetAllCommand(nodeId = nodeId)
                }
            }
        }
    }

    private suspend fun sendGetStatusComponentInfoCommand(
        name: String,
        nodeId: String,
        feature: PnPL
    ) {
        _isLoading.value = true

        blueManager.writeFeatureCommand(
            responseTimeout = 0,
            nodeId = nodeId, featureCommand =
            PnPLCommand(
                feature = feature,
                cmd = PnPLCmd(command = "get_status", request = name)
            )
        )
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true

            val feature =
                blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }
                    ?: return@launch

            if (feature is PnPL) {
                value.let {
                    val featureCommand = PnPLCommand(
                        feature = feature,
                        cmd = PnPLCmd(
                            command = name,
                            fields = mapOf(it)
                        )
                    )

                    blueManager.writeFeatureCommand(
                        responseTimeout = 0,
                        nodeId = nodeId,
                        featureCommand = featureCommand
                    )


                    //sendGetAllCommand(nodeId = nodeId)
                    sendGetStatusComponentInfoCommand(feature=feature, name =name,nodeId = nodeId)
                }
            }
        }
    }

    fun startDemo(nodeId: String, demoName: String?) {
        observeFeatureJob?.cancel()

        blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }?.let { feature ->
            observeFeatureJob = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = listOf(feature),
                onFeaturesEnabled = {
                    if (_modelUpdates.value.isEmpty()) {
                        getModel(nodeId = nodeId, demoName = demoName)
                    }
                }).flowOn(Dispatchers.IO).onEach { featureUpdate ->
                val data = featureUpdate.data
                if (data is PnPLConfig) {
                    data.deviceStatus.value?.components?.let { json ->
                        _lastStatusUpdatedAt.value = System.currentTimeMillis()
                        _componentStatusUpdates.value = json
                        _isLoading.value = false
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun stopDemo(nodeId: String) {
        observeFeatureJob?.cancel()

        _componentStatusUpdates.value = emptyList()

        //coroutineScope.launch {
        runBlocking {
            val features = blueManager.nodeFeatures(nodeId = nodeId).filter { it.name == PnPL.NAME }

            blueManager.disableFeatures(
                nodeId = nodeId, features = features
            )
        }
    }
}
