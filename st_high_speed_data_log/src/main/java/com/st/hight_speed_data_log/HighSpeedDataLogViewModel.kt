/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.hight_speed_data_log

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.board_catalog.models.DtmiType
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.models.NodeState
import com.st.core.GlobalConfig
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private typealias ComponentWithInterface = Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>

@HiltViewModel
class HighSpeedDataLogViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var observeFeatureJob: Job? = null
    private var observeNodeStatusJob: Job? = null
    private val pnplFeatures: MutableList<PnPL> = mutableListOf()
    private var shouldInitDemo: Boolean = true
    private var setShouldInitDemoAtResponse: Boolean = false
    private var shouldRenameTags: Boolean = true

    private var nodeIdLocal: String? = null

    private var pnplBleResponses: Boolean = false

    private var numberOfTags = 4

    private var commandQueue: MutableList<SetCommandPnPLRequest> = mutableListOf()

    private val _tags =
        MutableStateFlow<List<ComponentWithInterface>>(
            emptyList()
        )
    val tags: StateFlow<List<ComponentWithInterface>>
        get() = _tags.asStateFlow()

    private val _vespucciTags =
        MutableStateFlow<Map<String, Boolean>>(
            mutableMapOf()
        )
    val vespucciTags: StateFlow<Map<String, Boolean>>
        get() = _vespucciTags.asStateFlow()

    private val _acquisitionName =
        MutableStateFlow("")
    val acquisitionName: StateFlow<String>
        get() = _acquisitionName.asStateFlow()

    private val _sensors =
        MutableStateFlow<List<ComponentWithInterface>>(
            emptyList()
        )
    val sensors: StateFlow<List<ComponentWithInterface>>
        get() = _sensors.asStateFlow()

    private val _componentStatusUpdates = MutableStateFlow<List<JsonObject>>(emptyList())
    val componentStatusUpdates: StateFlow<List<JsonObject>>
        get() = _componentStatusUpdates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean>
        get() = _isLoading.asStateFlow()

    private val _isLogging = MutableStateFlow(false)
    val isLogging = _isLogging.asStateFlow()

    private val _isSDCardInserted = MutableStateFlow(false)
    val isSDCardInserted = _isSDCardInserted.asStateFlow()

    private val _statusMessage: MutableStateFlow<PnPLSpontaneousMessageType?> =
        MutableStateFlow(null)
    val statusMessage: StateFlow<PnPLSpontaneousMessageType?>
        get() = _statusMessage.asStateFlow()

    private val _isConnectionLost = MutableStateFlow(false)
    val isConnectionLost: StateFlow<Boolean>
        get() = _isConnectionLost.asStateFlow()

    private fun sensorPropNamePredicate(name: String): Boolean =
        name == "odr" || name == "fs" ||
                name == "enable" || name == "aop" ||
                name == "load_file" || name == "ucf_status" || name == "mounted"

    private fun tagsNamePredicate(name: String): Boolean =
        name == TAGS_INFO_JSON_KEY || name == ACQUISITION_INFO_JSON_KEY

    private fun acquisitionPropNamePredicate(name: String): Boolean =
        name == "name" || name == "description"

    private fun tagsPropNamePredicate(name: String): Boolean =
        name != "max_tags_num"

    private fun List<DtmiContent>.hsdl2TagPropertyFilter() =
        filter { content -> acquisitionPropNamePredicate(content.name) }

    private fun List<DtmiContent>.hsdl2SensorPropertyFilter() =
        filter { content -> sensorPropNamePredicate(content.name) }

    private fun List<ComponentWithInterface>.hsdl2SensorsFilter() =
        filter {
            it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR
        }.map {
            Pair(
                it.first,
                it.second.copy(contents = it.second.contents.hsdl2SensorPropertyFilter())
            )
        }

    init {
        viewModelScope.launch {
            isLogging.collect { value ->
                HsdlConfig.isLogging = value
            }
        }
    }

    private fun List<DtmiContent>.hideDisabledTagWhenLoggingFilter() =
        filter { content -> //  tag not enabled when logging is hide
            if (_isLogging.value) {
                val tagsInfoJson = _componentStatusUpdates.value.find {
                    it.containsKey(TAGS_INFO_JSON_KEY)
                }?.get(TAGS_INFO_JSON_KEY)?.jsonObject
                if (tagsInfoJson?.containsKey(content.name) == true) {
                    val propJson = tagsInfoJson[content.name]
                    if (propJson is JsonObject) {
                        if (propJson.containsKey(ENABLED_JSON_KEY)) {
                            return@filter propJson[ENABLED_JSON_KEY]?.jsonPrimitive?.booleanOrNull
                                ?: true
                        }
                    }
                }
            }

            true
        }

    private fun List<DtmiContent>.hideDisabledTagWhenConfigHasTag() =
        filter { content -> //  tag not enabled when HsdlConfig has tag label override is hide
            if (HsdlConfig.tags.isNotEmpty()) {
                val tagsInfoJson = _componentStatusUpdates.value.find {
                    it.containsKey(TAGS_INFO_JSON_KEY)
                }?.get(TAGS_INFO_JSON_KEY)?.jsonObject
                if (tagsInfoJson?.containsKey(content.name) == true) {
                    val propJson = tagsInfoJson[content.name]
                    if (propJson is JsonObject) {
                        if (propJson.containsKey(ENABLED_JSON_KEY)) {
                            return@filter propJson[ENABLED_JSON_KEY]?.jsonPrimitive?.booleanOrNull
                                ?: true
                        }
                    }
                }
            }

            true
        }

    private fun List<DtmiContent>.enableOrStatusPropertyMap() =
        map { content -> //  tag component when not logging hide enabled toggle/show status toggle and vice versa
            if (content.type == DtmiType.PROPERTY && content is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent) {
                if (content.schema is DtmiContent.DtmiObjectContent) {
                    val schema = content.schema as DtmiContent.DtmiObjectContent
                    return@map content.copy(schema = schema.copy(fields = schema.fields.filter {
                        if (_isLogging.value) {
                            it.name != ENABLED_JSON_KEY
                        } else {
                            it.name != STATUS_JSON_KEY
                        }
                    }))
                }
            }

            content
        }

    private fun List<DtmiContent>.enablePropertyMap() =
        map { content -> //  tag component when HsdlConfig has tag label override hide enabled toggle
            if (content.type == DtmiType.PROPERTY && content is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent) {
                if (content.schema is DtmiContent.DtmiObjectContent) {
                    val schema = content.schema as DtmiContent.DtmiObjectContent
                    return@map content.copy(schema = schema.copy(fields = schema.fields.filter {
                        if (HsdlConfig.tags.isNotEmpty()) {
                            it.name != ENABLED_JSON_KEY
                        } else {
                            true
                        }
                    }))
                }
            }

            content
        }

    private fun List<ComponentWithInterface>.hsdl2TagsFilter() =
        filter { tagsNamePredicate(it.first.name) }
            .map { contentWithInterface ->
                when (contentWithInterface.first.name) {
                    TAGS_INFO_JSON_KEY -> {
                        Pair(
                            contentWithInterface.first,
                            contentWithInterface.second.copy(
                                contents = contentWithInterface.second.contents
                                    .filter { content -> tagsPropNamePredicate(content.name) }
                                    .enableOrStatusPropertyMap()
                                    .enablePropertyMap()
                                    .hideDisabledTagWhenLoggingFilter()
                                    .hideDisabledTagWhenConfigHasTag()
                            )
                        )
                    }

                    ACQUISITION_INFO_JSON_KEY -> {
                        Pair(
                            contentWithInterface.first,
                            contentWithInterface.second.copy(
                                contents = contentWithInterface.second.contents.hsdl2TagPropertyFilter()
                            )
                        )
                    }

                    else -> contentWithInterface
                }
            }
            .sortedBy { it.first.name }


    private suspend fun getModel(nodeId: String) {
        _isLoading.value = true

        val componentWithInterface =
            blueManager.getDtmiModel(nodeId = nodeId, isBeta = stPreferences.isBetaApplication())
                ?.extractComponents(demoName = null)
                ?: emptyList()

        _sensors.value = componentWithInterface.hsdl2SensorsFilter()

        _tags.value = componentWithInterface.hsdl2TagsFilter()

        _isLoading.value = false
    }

    private suspend fun addCommandToQueueAndCheckSend(
        nodeId: String,
        newCommand: SetCommandPnPLRequest
    ) {
        commandQueue.add(newCommand)
        //if it's the only one command in the list... send it
        if (commandQueue.size == 1) {
            _isLoading.value = true
            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(),
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
            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId,
                featureCommand = PnPLCommand(feature = pnplFeatures.first(), cmd = PnPLCmd.ALL)
            )
        }
    }

    private suspend fun sendGetLogControllerCommand(nodeId: String) {
        if (pnplBleResponses) {
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Status, pnpLCommand = PnPLCmd.LOG_CONTROLLER
                )
            )
        } else {
            _isLoading.value = true
            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(), cmd = PnPLCmd.LOG_CONTROLLER
                )
            )
        }
    }

    private suspend fun sendGetTagsInfoCommand(nodeId: String) {
        if (pnplBleResponses) {
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Status, pnpLCommand = PnPLCmd.TAGS_INFO
                )
            )
        } else {
            _isLoading.value = true
            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(),
                    cmd = PnPLCmd.TAGS_INFO
                )
            )
        }
    }


    private suspend fun sendGetStatusComponentInfoCommand(name: String, nodeId: String) {
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
                    feature = pnplFeatures.first(),
                    cmd = PnPLCmd(command = "get_status", request = name)
                )
            )
        }
    }

    private suspend fun setTime(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val sdf = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.ROOT)
        val datetime = sdf.format(Date(timeInMillis))

        if (pnplBleResponses) {
            //add the command to the list
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Set, pnpLCommand = PnPLCmd(
                        component = LOG_CONTROLLER_JSON_KEY,
                        command = "set_time",
                        fields = mapOf("datetime" to datetime)
                    ),
                    askTheStatus = false
                )
            )
        } else {
            _sendCommand(
                nodeId = nodeId, name = LOG_CONTROLLER_JSON_KEY,
                value = CommandRequest(
                    commandName = "set_time",
                    commandType = "",
                    request = mapOf("datetime" to datetime)
                )
            )
        }
    }

    private suspend fun setName(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val nameFormatter = HsdlConfig.datalogNameFormat ?: "EEE MMM d yyyy HH:mm:ss"
        val sdf = SimpleDateFormat(nameFormatter, Locale.UK)
        val datetime = sdf.format(Date(timeInMillis))

        _acquisitionName.emit(datetime)

        if (pnplBleResponses) {
            //add the command to the list
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Set, pnpLCommand = PnPLCmd(
                        component = "",
                        command = ACQUISITION_INFO_JSON_KEY,
                        fields = mapOf(NAME_JSON_KEY to datetime)
                    ),
                    askTheStatus = false
                )
            )

            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Set, pnpLCommand = PnPLCmd(
                        component = "",
                        command = ACQUISITION_INFO_JSON_KEY,
                        fields = mapOf(DESC_JSON_KEY to "Empty")
                    ),
                    askTheStatus = false
                )
            )
        } else {
            _sendCommand(
                nodeId = nodeId, name = "", value = CommandRequest(
                    commandName = ACQUISITION_INFO_JSON_KEY,
                    commandType = "",
                    request = mapOf(NAME_JSON_KEY to datetime)
                )
            )

            _sendCommand(
                nodeId = nodeId, name = "", value = CommandRequest(
                    commandName = ACQUISITION_INFO_JSON_KEY,
                    commandType = "",
                    request = mapOf(DESC_JSON_KEY to "Empty")
                )
            )
        }
    }

    fun cleanStatusMessage() {
        viewModelScope.launch {
            _statusMessage.emit(null)
        }
    }

    fun startLog(nodeId: String) {

        viewModelScope.launch {
            setTime(nodeId)

            //For Avoiding to change again the Acquisition name
            //setName(nodeId)

            if (pnplBleResponses) {
                //add the command to the list
                addCommandToQueueAndCheckSend(
                    nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                        typeOfCommand = PnPLTypeOfCommand.Log, pnpLCommand = PnPLCmd.START_LOG,
                        askTheStatus = true
                    )
                )
            } else {
                _isLoading.value = true

                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId, featureCommand = PnPLCommand(
                        feature = pnplFeatures.first(), cmd = PnPLCmd.START_LOG
                    )
                )

                sendGetLogControllerCommand(nodeId = nodeId)
            }
        }
    }

    fun stopLog(nodeId: String) {
        viewModelScope.launch {
            if (pnplBleResponses) {
                //add the command to the list
                addCommandToQueueAndCheckSend(
                    nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                        typeOfCommand = PnPLTypeOfCommand.Log, pnpLCommand = PnPLCmd.STOP_LOG,
                        askTheStatus = true
                    )
                )
                setShouldInitDemoAtResponse = true
            } else {
                _isLoading.value = true
                shouldInitDemo = true
                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(
                        feature = pnplFeatures.first(),
                        cmd = PnPLCmd.STOP_LOG
                    )
                )
                sendGetLogControllerCommand(nodeId = nodeId)
            }
        }
    }

    fun sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        value?.let {
            if (pnplBleResponses) {
                //add the command to the list
                viewModelScope.launch {
                    addCommandToQueueAndCheckSend(
                        nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                            typeOfCommand = PnPLTypeOfCommand.Command, pnpLCommand = PnPLCmd(
                                component = name,
                                command = value.commandName,
                                fields = value.request
                            ),
                            askTheStatus = it.commandName != "load_file"
                        )
                    )
                }
            } else {
                if (it.commandName == "load_file") {
                    runBlocking {
                        //RunBlocking for avoiding that the HSDataLogFragment starts
                        // before to finish the load process
                        //Without the sendGetAllCommand, because when the HSDataLogFragment will start...
                        // it triggers a get status command
                        _sendCommand(nodeId, name, value)
                    }
                } else {
                    viewModelScope.launch {
                        _sendCommand(nodeId, name, value)
                        sendGetAllCommand(nodeId)
                    }
                }
            }
        }
    }

    private suspend fun _sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        value?.let {
            _isLoading.value = true
            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(), cmd = PnPLCmd(
                        component = name, command = it.commandName, fields = it.request
                    )
                )
            )
        }
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        value.let {
            viewModelScope.launch {
                if (pnplBleResponses) {
                    //add the command to the list
                    addCommandToQueueAndCheckSend(
                        nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                            typeOfCommand = PnPLTypeOfCommand.Set, pnpLCommand = PnPLCmd(
                                command = name,
                                fields = mapOf(it)
                            )
                        )
                    )
                } else {
                    _isLoading.value = true
                    //Write the Command ans ask immediately after for the status
                    val featureCommand = PnPLCommand(
                        feature = pnplFeatures.first(), cmd = PnPLCmd(
                            command = name, fields = mapOf(it)
                        )
                    )

                    blueManager.writeFeatureCommand(
                        responseTimeout = 0,
                        nodeId = nodeId, featureCommand = featureCommand
                    )

                    sendGetStatusComponentInfoCommand(name = name, nodeId = nodeId)
                }
            }
        }
    }

    fun disconnect() {
        nodeIdLocal?.let { nodeId ->
            GlobalConfig.navigateBack?.let { it1 -> it1(nodeId) }
        }
    }

    fun resetConnectionLost() {
        viewModelScope.launch {
            _isConnectionLost.emit(false)
        }
    }

    fun startDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        observeNodeStatusJob?.cancel()

        nodeIdLocal = nodeId

        //We need to know the number of Sw Tags before to start
        runBlocking {
            val tags = blueManager.getDtmiModel(
                nodeId = nodeId,
                isBeta = stPreferences.isBetaApplication()
            )?.extractComponent(compName = TAGS_INFO_JSON_KEY)?.firstOrNull()

            tags?.let {
                numberOfTags = tags.second.contents.filter{tagsPropNamePredicate(it.name)}.size
            }
        }

        //Make the disconnection if the we lost the node
        observeNodeStatusJob = viewModelScope.launch {
            blueManager.getNodeStatus(nodeId = nodeId).collect {
                if (it.connectionStatus.prev == NodeState.Ready && it.connectionStatus.current == NodeState.Disconnected) {
                    //GlobalConfig.navigateBack?.let { it1 -> it1(nodeId) }
                    _isConnectionLost.emit(true)
                }
            }
        }

        if (pnplFeatures.isEmpty()) {
            pnplFeatures.addAll(
                blueManager.nodeFeatures(nodeId = nodeId)
                    .filter { it.name == PnPL.NAME }.filterIsInstance<PnPL>()
            )
        }

        observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
            features = pnplFeatures,
            onFeaturesEnabled = {
                viewModelScope.launch {
                    val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)
                    var maxWriteLength =
                        node.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength
                    maxWriteLength?.let {
                        if (maxWriteLength!! > (node.maxPayloadSize)) {
                            maxWriteLength = (node.maxPayloadSize)
                        }
                        pnplFeatures.firstOrNull { it.name == PnPL.NAME }
                            ?.setMaxPayLoadSize(maxWriteLength!!)
                    }

                    if (_sensors.value.isEmpty() || _tags.value.isEmpty()) {
                        getModel(nodeId = nodeId)

                        sendGetLogControllerCommand(nodeId)
                    }
                }
            }).flowOn(Dispatchers.IO).onEach { featureUpdate ->
            featureUpdate.data.let { data ->
                if (data is PnPLConfig) {
                    data.deviceStatus.value?.components?.let { json ->

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
                            //data.setCommandResponse.value?.let { setCommandResponse ->
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

                                    //Ask the status
                                    val firstOne = commandQueue.first()

                                    //Remove the command from the list and send Next One
                                    commandQueue.removeFirst()
                                    if (commandQueue.isNotEmpty()) {
                                        _isLoading.value = true
                                        blueManager.writeFeatureCommand(
                                            responseTimeout = 0,
                                            nodeId = nodeId, featureCommand = PnPLCommand(
                                                feature = pnplFeatures.first(),
                                                cmd = commandQueue[0].pnpLCommand
                                            )
                                        )
                                    }

                                    if (firstOne.askTheStatus) {
                                        when (firstOne.typeOfCommand) {
                                            PnPLTypeOfCommand.Command -> sendGetStatusComponentInfoCommand(
                                                name = firstOne.pnpLCommand.command,
                                                nodeId = nodeId
                                            )

                                            PnPLTypeOfCommand.Set -> sendGetStatusComponentInfoCommand(
                                                name = firstOne.pnpLCommand.command,
                                                nodeId = nodeId
                                            )

                                            PnPLTypeOfCommand.Log -> sendGetLogControllerCommand(
                                                nodeId = nodeId
                                            )

                                            else -> {}
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
                                                _isLoading.value = true
                                                blueManager.writeFeatureCommand(
                                                    responseTimeout = 0,
                                                    nodeId = nodeId, featureCommand = PnPLCommand(
                                                        feature = pnplFeatures.first(),
                                                        cmd = commandQueue[0].pnpLCommand
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        json.find { it.containsKey(LOG_CONTROLLER_JSON_KEY) }
                            ?.get(LOG_CONTROLLER_JSON_KEY)?.jsonObject?.let { logControllerJson ->
                                _isSDCardInserted.value =
                                    logControllerJson[SD_JSON_KEY]?.jsonPrimitive?.booleanOrNull
                                        ?: false
                                _isLogging.value =
                                    logControllerJson[LOG_STATUS_JSON_KEY]?.jsonPrimitive?.booleanOrNull
                                        ?: false

                                if (!_isLogging.value && setShouldInitDemoAtResponse) {
                                    setShouldInitDemoAtResponse = false
                                    shouldInitDemo = true
                                }

                                Log.d(TAG, "isLoading ${_isLoading.value}")
                                Log.d(TAG, "isLogging ${_isLogging.value}")
                                Log.d(TAG, "isSDCardInserted ${_isSDCardInserted.value}")
                            }

                        json.find { it.containsKey(TAGS_INFO_JSON_KEY) }
                            ?.get(TAGS_INFO_JSON_KEY)?.jsonObject?.let { tags ->

                                val vespucciTagsMap = mutableMapOf<String, Boolean>()
                                tags.forEach {
                                    val key = it.key
                                    val value = it.value
                                    if (key.startsWith(TAG_JSON_KEY)) {
                                        if (value is JsonObject) {
                                            val enabledJsonElement =
                                                value.getOrDefault(ENABLED_JSON_KEY, null)
                                            enabledJsonElement?.let { enabled ->
                                                val labelJsonElement =
                                                    value.getOrDefault(LABEL_JSON_KEY, null)
                                                labelJsonElement?.let { label ->
                                                    val statusJsonElement =
                                                        value.getOrDefault(STATUS_JSON_KEY, null)
                                                    statusJsonElement?.let { status ->
                                                        if (enabled.jsonPrimitive.booleanOrNull == true) {
                                                            vespucciTagsMap[label.jsonPrimitive.content] =
                                                                status.jsonPrimitive.boolean
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                _vespucciTags.emit(vespucciTagsMap)
                            }

                        if (shouldInitDemo) {
                            shouldInitDemo = false
                            if (_isLogging.value) {
                                sendGetTagsInfoCommand(nodeId)
                            } else {
                                setName(nodeId = nodeId)
                                if (shouldRenameTags) {
                                    shouldRenameTags = false
                                    //for (index in 0..4) {
                                    for (index in 0..<numberOfTags) {
                                        if (index in 0..HsdlConfig.tags.lastIndex) {
                                            val tagName = HsdlConfig.tags[index]

                                            if (pnplBleResponses) {
                                                //add the command to the list
                                                addCommandToQueueAndCheckSend(
                                                    nodeId = nodeId,
                                                    newCommand = SetCommandPnPLRequest(
                                                        typeOfCommand = PnPLTypeOfCommand.Set,
                                                        pnpLCommand = PnPLCmd(
                                                            command = TAGS_INFO_JSON_KEY,
                                                            request = "$TAG_JSON_KEY$index",
                                                            fields = mapOf(LABEL_JSON_KEY to tagName)
                                                        ),
                                                        askTheStatus = false
                                                    )
                                                )

                                                //add the command to the list
                                                addCommandToQueueAndCheckSend(
                                                    nodeId = nodeId,
                                                    newCommand = SetCommandPnPLRequest(
                                                        typeOfCommand = PnPLTypeOfCommand.Set,
                                                        pnpLCommand = PnPLCmd(
                                                            command = TAGS_INFO_JSON_KEY,
                                                            request = "$TAG_JSON_KEY$index",
                                                            fields = mapOf(ENABLED_JSON_KEY to true)
                                                        ),
                                                        askTheStatus = index == HsdlConfig.tags.lastIndex
                                                    )
                                                )
                                            } else {
                                                _isLoading.value = true
                                                val renameCommand = PnPLCommand(
                                                    feature = pnplFeatures.first(),
                                                    cmd = PnPLCmd(
                                                        command = TAGS_INFO_JSON_KEY,
                                                        request = "$TAG_JSON_KEY$index",
                                                        fields = mapOf(LABEL_JSON_KEY to tagName)
                                                    )
                                                )

                                                blueManager.writeFeatureCommand(
                                                    responseTimeout = 0,
                                                    nodeId = nodeId, featureCommand = renameCommand
                                                )


                                                val enableCommand = PnPLCommand(
                                                    feature = pnplFeatures.first(),
                                                    cmd = PnPLCmd(
                                                        command = TAGS_INFO_JSON_KEY,
                                                        request = "$TAG_JSON_KEY$index",
                                                        fields = mapOf(ENABLED_JSON_KEY to true)
                                                    )
                                                )

                                                //delay(50L)

                                                blueManager.writeFeatureCommand(
                                                    responseTimeout = 0,
                                                    nodeId = nodeId, featureCommand = enableCommand
                                                )
                                            }
                                        } else {
                                            if (pnplBleResponses) {
                                                //add the command to the list
                                                addCommandToQueueAndCheckSend(
                                                    nodeId = nodeId,
                                                    newCommand = SetCommandPnPLRequest(
                                                        typeOfCommand = PnPLTypeOfCommand.Set,
                                                        pnpLCommand = PnPLCmd(
                                                            command = TAGS_INFO_JSON_KEY,
                                                            request = "$TAG_JSON_KEY$index",
                                                            fields = mapOf(ENABLED_JSON_KEY to false)
                                                        ),
                                                        askTheStatus = false
                                                    )
                                                )
                                            } else {
                                                _isLoading.value = true
                                                val disableCommand = PnPLCommand(
                                                    feature = pnplFeatures.first(),
                                                    cmd = PnPLCmd(
                                                        command = TAGS_INFO_JSON_KEY,
                                                        request = "$TAG_JSON_KEY$index",
                                                        fields = mapOf(ENABLED_JSON_KEY to false)
                                                    )
                                                )
                                                blueManager.writeFeatureCommand(
                                                    responseTimeout = 0,
                                                    nodeId = nodeId, featureCommand = disableCommand
                                                )
                                            }
                                        }

                                        //delay(50L)
                                    }
                                    //delay(100L)
                                }
                                sendGetAllCommand(nodeId)
                            }
                        }

                        getModel(nodeId = nodeId)

                        if (json.size == 1) {
                            _componentStatusUpdates.update {
                                it.filter { jo -> jo.keys != json.first().keys } + json
                            }
                        } else {
                            _componentStatusUpdates.value = json
                        }
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun stopDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        observeNodeStatusJob?.cancel()

        _componentStatusUpdates.value = emptyList()
        shouldInitDemo = true
        shouldRenameTags = true

//        val job = coroutineScope.launch {
//            blueManager.disableFeatures(
//                nodeId = nodeId, features = pnplFeatures
//            )
//        }
        //Not optimal... but in this way... I am able to see the get status if demo is customized
        runBlocking {
            blueManager.disableFeatures(
                nodeId = nodeId, features = pnplFeatures
            )

            //pnplFeatures.clear()
            _sensors.value = emptyList()
            _tags.value = emptyList()
        }
    }

    fun refresh(nodeId: String) {
        viewModelScope.launch {
            sendGetAllCommand(nodeId)
        }
    }

    fun onTagChangeState(nodeId: String, tag: String, newState: Boolean) {
        viewModelScope.launch {
            if (pnplBleResponses) {
                //add the command to the list
                addCommandToQueueAndCheckSend(
                    nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                        typeOfCommand = PnPLTypeOfCommand.Set, pnpLCommand = PnPLCmd(
                            command = TAGS_INFO_JSON_KEY,
                            request = "$TAG_JSON_KEY${HsdlConfig.tags.indexOf(tag)}",
                            fields = mapOf(STATUS_JSON_KEY to newState)
                        ),
                        askTheStatus = false
                    )
                )

                val oldTagsStatus = _vespucciTags.value.toMutableMap()
                oldTagsStatus[tag] = newState

                _vespucciTags.emit(oldTagsStatus)

            } else {
                val enableCommand = PnPLCommand(
                    feature = pnplFeatures.first(),
                    cmd = PnPLCmd(
                        command = TAGS_INFO_JSON_KEY,
                        request = "$TAG_JSON_KEY${HsdlConfig.tags.indexOf(tag)}",
                        fields = mapOf(STATUS_JSON_KEY to newState)
                    )
                )

                val oldTagsStatus = _vespucciTags.value.toMutableMap()
                oldTagsStatus[tag] = newState

                _vespucciTags.emit(oldTagsStatus)

                //delay(50L)

                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId,
                    featureCommand = enableCommand
                )
            }
        }
    }

    companion object {
        private const val LOG_STATUS_JSON_KEY = "log_status"
        private const val SD_JSON_KEY = "sd_mounted"
        private const val LOG_CONTROLLER_JSON_KEY = "log_controller"
        private const val TAGS_INFO_JSON_KEY = "tags_info"
        private const val ACQUISITION_INFO_JSON_KEY = "acquisition_info"
        private const val LABEL_JSON_KEY = "label"
        private const val TAG_JSON_KEY = "sw_tag"
        private const val ENABLED_JSON_KEY = "enabled"
        private const val DESC_JSON_KEY = "description"
        private const val NAME_JSON_KEY = "name"
        private const val STATUS_JSON_KEY = "status"
        private const val TAG = "HsdlViewModel"
    }
}
