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
import com.st.pnpl.composable.PnPLSpontaneousMessageType
import com.st.pnpl.composable.searchInfoWarningError
import com.st.pnpl.util.PnPLTypeOfCommand
import com.st.pnpl.util.SetCommandPnPLRequest
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

    private var pnplBleResponses: Boolean = false

    private var featurePnPL: PnPL? = null

    private var commandQueue: MutableList<SetCommandPnPLRequest> = mutableListOf()

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

    private val _statusMessage: MutableStateFlow<PnPLSpontaneousMessageType?> =
        MutableStateFlow(null)
    val statusMessage: StateFlow<PnPLSpontaneousMessageType?>
        get() = _statusMessage.asStateFlow()

    private val _lastStatusUpdatedAt = MutableStateFlow(0L)
    val lastStatusUpdatedAt: StateFlow<Long>
        get() = _lastStatusUpdatedAt.asStateFlow()


    private suspend fun addCommandToQueueAndCheckSend(
        nodeId: String,
        newCommand: SetCommandPnPLRequest
    ) {
        commandQueue.add(newCommand)
        //if it's the only one command in the list... send it
        if (commandQueue.size == 1) {
            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = featurePnPL!!,
                    cmd = commandQueue[0].pnpLCommand
                )
            )
        }
    }

    private suspend fun sendGetAllCommand(nodeId: String) {
        if (pnplBleResponses) {
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Status, pnpLCommand = PnPLCmd.ALL
                )
            )
        } else {
            _isLoading.value = true
            featurePnPL?.let {
                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(feature = featurePnPL!!, cmd = PnPLCmd.ALL)
                )
            }
        }
    }

    fun cleanStatusMessage() {
        viewModelScope.launch {
            _statusMessage.emit(null)
        }
    }

    fun getModel(nodeId: String, demoName: String?) {
        viewModelScope.launch {
            _isLoading.value = true

            _modelUpdates.value =
                blueManager.getDtmiModel(
                    nodeId = nodeId,
                    isBeta = stPreferences.isBetaApplication()
                )?.extractComponents(demoName = demoName)
                    ?: emptyList()

            //If we want that Demo Settings start with the components Expanded..
            //_enableCollapse.value = demoName.isNullOrEmpty()

            _enableCollapse.value = true

            _isLoading.value = false

            sendGetAllCommand(nodeId = nodeId)
        }
    }

    fun sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        _isLoading.value = true
        featurePnPL?.let {
            value?.let {

                if (pnplBleResponses) {
                    viewModelScope.launch {
                        addCommandToQueueAndCheckSend(
                            nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                                typeOfCommand = PnPLTypeOfCommand.Command, pnpLCommand = PnPLCmd(
                                    component = name,
                                    command = it.commandName,
                                    fields = it.request
                                )
                            )
                        )
                    }
                } else {
                    //Write the Command ans ask immediately after for the status
                    viewModelScope.launch {
                        blueManager.writeFeatureCommand(
                            responseTimeout = 0,
                            nodeId = nodeId, featureCommand = PnPLCommand(
                                feature = featurePnPL!!,
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
    }

    private suspend fun sendGetStatusComponentInfoCommand(
        name: String,
        nodeId: String,
        feature: PnPL
    ) {
        if (pnplBleResponses) {
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Status,
                    pnpLCommand = PnPLCmd(command = "get_status", request = name)
                )
            )
        } else {
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
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {

        _isLoading.value = true
        featurePnPL?.let {
            value.let {

                if (pnplBleResponses) {
                    viewModelScope.launch {
                        addCommandToQueueAndCheckSend(
                            nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                                typeOfCommand = PnPLTypeOfCommand.Set, pnpLCommand = PnPLCmd(
                                    command = name,
                                    fields = mapOf(it)
                                )
                            )
                        )
                    }
                } else {
                    //Write the Command ans ask immediately after for the status
                    viewModelScope.launch {
                        val featureCommand = PnPLCommand(
                            feature = featurePnPL!!,
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

                        sendGetStatusComponentInfoCommand(
                            feature = featurePnPL!!,
                            name = name,
                            nodeId = nodeId
                        )
                    }
                }
            }
        }
    }

    fun startDemo(nodeId: String, demoName: String?) {
        observeFeatureJob?.cancel()

        commandQueue.clear()

        blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }?.let { feature ->

            featurePnPL = feature as PnPL
            observeFeatureJob = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = listOf(feature),
                onFeaturesEnabled = {

                    viewModelScope.launch {
                        val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)
                        var maxWriteLength =
                            node.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength
                        maxWriteLength?.let {
                            if (maxWriteLength!! > (node.maxPayloadSize)) {
                                maxWriteLength = (node.maxPayloadSize)
                            }
                            (feature as PnPL).setMaxPayLoadSize(maxWriteLength!!)
                        }
                    }

                    if (_modelUpdates.value.isEmpty()) {
                        getModel(nodeId = nodeId, demoName = demoName)
                    }
                }).flowOn(Dispatchers.IO).onEach { featureUpdate ->
                val data = featureUpdate.data
                if (data is PnPLConfig) {

                    data.deviceStatus.value?.components?.let { json ->
                        _lastStatusUpdatedAt.value = System.currentTimeMillis()
                        _componentStatusUpdates.value = json

                        //Check if the BLE Responses are == true
                        data.deviceStatus.value?.pnplBleResponses?.let { value ->
                            pnplBleResponses = value
                        }

                        //Search the Spontaneous messages
                        val message = searchInfoWarningError(json)
                        message?.let {
                            _statusMessage.emit(message)
                        }

                        if (pnplBleResponses) {
                            //Search the Set/Command Response if they are allowed for the fw
                            if (data.setCommandResponse.value != null) {
                                if (data.setCommandResponse.value!!.response != null) {
                                    if (!data.setCommandResponse.value!!.response!!.status) {
                                        //Report one Message...
                                        val messageStatus = PnPLSpontaneousMessageType.ERROR
                                        messageStatus.message =
                                            data.setCommandResponse.value!!.response!!.message
                                                ?: "Generic Error"
                                        _statusMessage.emit(messageStatus)
                                    }

                                    val firstOne = commandQueue.first()

                                    //Remove the command from the list and send Next One
                                    commandQueue.removeFirst()
                                    if (commandQueue.isNotEmpty()) {
                                        blueManager.writeFeatureCommand(
                                            responseTimeout = 0,
                                            nodeId = nodeId, featureCommand = PnPLCommand(
                                                feature = featurePnPL!!,
                                                cmd = commandQueue[0].pnpLCommand
                                            )
                                        )
                                    }

                                    if (firstOne.askTheStatus) {
                                        //Ask the status
                                        if (firstOne.typeOfCommand == PnPLTypeOfCommand.Command) {
                                            sendGetAllCommand(nodeId = nodeId)
                                        } else {
//                                            sendGetStatusComponentInfoCommand(
//                                                feature = featurePnPL!!,
//                                                name = firstOne.pnpLCommand.command,
//                                                nodeId = nodeId
//                                            )
                                            sendGetAllCommand(nodeId = nodeId)
                                        }
                                    }

                                } else {
                                    if (commandQueue.isNotEmpty()) {
                                        val firstOne = commandQueue.first()
                                        if (firstOne.typeOfCommand != PnPLTypeOfCommand.Status) {
                                            val messageStatus = PnPLSpontaneousMessageType.ERROR
                                            messageStatus.message =
                                                "Status Message from a not Get Status Command[${firstOne.typeOfCommand}]"
                                            _statusMessage.emit(messageStatus)
                                        } else {

                                            commandQueue.removeFirst()
                                            if (commandQueue.isNotEmpty()) {
                                                blueManager.writeFeatureCommand(
                                                    responseTimeout = 0,
                                                    nodeId = nodeId, featureCommand = PnPLCommand(
                                                        feature = featurePnPL!!,
                                                        cmd = commandQueue[0].pnpLCommand
                                                    )
                                                )
                                            }
                                        }
                                    }

                                }
                            }
                        }

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
