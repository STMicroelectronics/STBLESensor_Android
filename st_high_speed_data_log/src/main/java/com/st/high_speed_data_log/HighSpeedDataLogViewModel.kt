/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.high_speed_data_log

import android.os.CountDownTimer
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
import com.st.ui.composables.ENABLE_PROPERTY_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

typealias ComponentWithInterface = Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>

@HiltViewModel
class HighSpeedDataLogViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences
) : ViewModel() {

    private var observeFeatureJob: Job? = null
    private var observeNodeStatusJob: Job? = null
    private var pnplFeature: PnPL? = null
    private var shouldInitDemo: Boolean = true
    private var setShouldInitDemoAtResponse: Boolean = false
    private var shouldDisableTags: Boolean = true
    private var commandQueue: MutableList<SetCommandPnPLRequest> = mutableListOf()
    private val tagNames: MutableList<String> = mutableListOf()
    private var sensorsActive = listOf<String>()
    private var componentWithInterface: List<ComponentWithInterface> = listOf()

    private val _tags = MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _numActiveTags = MutableStateFlow(value = 0)
    private val _sensors = MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _componentStatusUpdates = MutableStateFlow<List<JsonObject>>(value = emptyList())
    private val _isLogging = MutableStateFlow(value = false)
    private val _isSDCardInserted = MutableStateFlow(value = false)
    private val _isLoading = MutableStateFlow(value = false)
    private val _isConnectionLost = MutableStateFlow(value = false)
    private val _statusMessage: MutableStateFlow<PnPLSpontaneousMessageType?> =
        MutableStateFlow(value = null)

    val tags: StateFlow<List<ComponentWithInterface>> = _tags.asStateFlow()
    val numActiveTags = _numActiveTags.asStateFlow()
    val sensors: StateFlow<List<ComponentWithInterface>> = _sensors.asStateFlow()
    val componentStatusUpdates: StateFlow<List<JsonObject>> = _componentStatusUpdates.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val isLogging = _isLogging.asStateFlow()
    val isSDCardInserted = _isSDCardInserted.asStateFlow()
    val statusMessage: StateFlow<PnPLSpontaneousMessageType?> = _statusMessage.asStateFlow()
    val isConnectionLost: StateFlow<Boolean> = _isConnectionLost.asStateFlow()


    private val _numActiveSensors = MutableStateFlow(value = 0)
    val numActiveSensors = _numActiveSensors.asStateFlow()

    var isBeta = false

    private var enableStartStopDemo = true

    private var timer: CountDownTimer? = null

    init {
        isBeta = stPreferences.isBetaApplication()
    }

    init {
        viewModelScope.launch {
            _sensors
                .combine(_componentStatusUpdates) { sensors, status ->
                    val sensorsEnabled = mutableListOf<String>()
                    val sensorsActiveLocal = mutableListOf<String>()

                    sensors.forEach { sensor ->
                        val name = sensor.first.name
                        val interfaceModel = sensor.second
                        val data = (status.find { it.containsKey(name) })?.get(name)

                        var isMounted = interfaceModel.contents
                            .filterIsInstance<DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent>()
                            .find { it.name == "mounted" }
                            ?.let { enableProperty ->
                                val defaultData = true
                                var booleanData = true
                                if (data is JsonObject && data[enableProperty.name] is JsonPrimitive) {
                                    booleanData =
                                        (data[enableProperty.name] as JsonPrimitive).booleanOrNull
                                            ?: defaultData
                                }
                                if (data == null) {
                                    booleanData = false
                                }
                                booleanData
                            } ?: true

                        if(data==null) {
                            isMounted = false
                        }

                        if (isMounted) {
                            sensorsActiveLocal.add(name)

                            val isEnabled = interfaceModel.contents
                                .filterIsInstance<DtmiContent.DtmiPropertyContent.DtmiBooleanPropertyContent>()
                                .find { it.name == ENABLE_PROPERTY_NAME }
                                ?.let { enableProperty ->
                                    val defaultData = enableProperty.initValue
                                    var booleanData = false
                                    if (data is JsonObject && data[enableProperty.name] is JsonPrimitive) {
                                        booleanData =
                                            (data[enableProperty.name] as JsonPrimitive).booleanOrNull
                                                ?: defaultData
                                    }

                                    booleanData
                                } ?: false

                            if (isEnabled) {
                                sensorsEnabled.add(name)
                            }
                        }
                    }
                    //Update List of active sensors
                    sensorsActive = sensorsActiveLocal.toList()

                    sensorsEnabled
                }
                .collect { sensorsEnabled ->
                    Log.d(TAG, "sensorsEnabled = ${sensorsEnabled.joinToString(", ")}")
                    _numActiveSensors.emit(sensorsEnabled.size)
                }
        }
    }

    private fun sensorPropNamePredicate(name: String): Boolean =
        name == "odr" ||
                name == "fs" ||
                name == "enable" ||
                name == "aop" ||
                name == "load_file" ||
                name == "ucf_status" ||
                name == "mounted" ||
                name == "resolution" ||
                name == "ranging_mode" ||
                name == "integration_time" ||
                name == "exposure_time" ||
                name == "intermeasurement_time" ||
                name == "transmittance" ||
                name == "embedded_compensation" ||
                name == "software_compensation" ||
                name == "compensation_type" ||
                name == "sw_presence_threshold" ||
                name == "sw_motion_threshold" ||
                name == "adc_conversion_time"

    private fun tagsNamePredicate(name: String): Boolean =
        name == TAGS_INFO_JSON_KEY || name == ACQUISITION_INFO_JSON_KEY

    private fun acquisitionPropNamePredicate(name: String): Boolean =
        name == "name" || name == "description"

    private fun tagsPropNamePredicate(name: String): Boolean =
        name != "max_tags_num" && name.startsWith("hw_tag").not()

    private fun List<DtmiContent>.hsdl2TagPropertyFilter() =
        filter { content -> acquisitionPropNamePredicate(content.name) }

    private fun List<DtmiContent>.hsdl2SensorPropertyFilter() =
        filter { content -> sensorPropNamePredicate(content.name) }

    private fun List<ComponentWithInterface>.hsdl2SensorsFilter() = filter {
        it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR
    }.map {
        Pair(
            it.first, it.second.copy(contents = it.second.contents.hsdl2SensorPropertyFilter())
        )
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

    private fun List<ComponentWithInterface>.hsdl2TagsFilter() =
        filter { tagsNamePredicate(it.first.name) }.map { contentWithInterface ->
            when (contentWithInterface.first.name) {
                TAGS_INFO_JSON_KEY -> {
                    Pair(
                        contentWithInterface.first,
                        contentWithInterface.second.copy(
                            contents = contentWithInterface.second.contents.filter { content ->
                                tagsPropNamePredicate(
                                    content.name
                                )
                            }.enableOrStatusPropertyMap()
                                .hideDisabledTagWhenLoggingFilter()
                        )
                    )
                }

                ACQUISITION_INFO_JSON_KEY -> {
                    Pair(
                        contentWithInterface.first, contentWithInterface.second.copy(
                            contents = contentWithInterface.second.contents.hsdl2TagPropertyFilter()
                        )
                    )
                }

                else -> contentWithInterface
            }
        }.sortedBy { it.first.name }

    private suspend fun getModel(nodeId: String) {
        if (componentWithInterface.isEmpty()) {
           // _isLoading.update { true }
            componentWithInterface =
                blueManager.getDtmiModel(
                    nodeId = nodeId,
                    isBeta = stPreferences.isBetaApplication()
                )?.extractComponents(demoName = null) ?: emptyList()
        }

        _sensors.update { componentWithInterface.hsdl2SensorsFilter() }

        _tags.update {componentWithInterface.hsdl2TagsFilter() }

        _isLoading.update { false }
    }

    private suspend fun addCommandToQueueAndCheckSend(
        nodeId: String,
        newCommand: SetCommandPnPLRequest
    ) {
        commandQueue.add(newCommand)
        //if it's the only one command in the list... send it

        pnplFeature?.let {
            if (commandQueue.size == 1) {
                _isLoading.update { true }

                blueManager.writeFeatureCommand(
                    responseTimeout = 0, nodeId = nodeId, featureCommand = PnPLCommand(
                        feature = it, cmd = commandQueue[0].pnpLCommand
                    )
                )

                createAndStartTimer()
            }
        }
    }


    private fun createAndStartTimer() {
        timer = object: CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                _isLoading.update { false }
                val messageStatus = PnPLSpontaneousMessageType.WARNING
                messageStatus.message =
                    "Communication error\nTry to exit and enter again in the demo or restart application and, if it's necessary, restart the board"
                _statusMessage.update { messageStatus }
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun stopTimer() {
        timer?.let {
            (timer as CountDownTimer).cancel()
            timer = null
        }
    }

    private suspend fun sendCommand(
        nodeId: String,
        typeOfCmd: PnPLTypeOfCommand,
        cmd: PnPLCmd,
        askTheStatus: Boolean
    ) {
        addCommandToQueueAndCheckSend(
            nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                typeOfCommand = typeOfCmd,
                pnpLCommand = cmd,
                askTheStatus = askTheStatus
            )
        )
    }

    private suspend fun sendGetAllCommand(nodeId: String) {
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Status,
            cmd = PnPLCmd.ALL,
            askTheStatus = false
        )
    }

    private suspend fun sendGetLogControllerCommand(nodeId: String) {
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Status,
            cmd = PnPLCmd.LOG_CONTROLLER,
            askTheStatus = false
        )
    }

    private suspend fun sendGetTagsInfoCommand(nodeId: String) {
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Status,
            cmd = PnPLCmd.TAGS_INFO,
            askTheStatus = false
        )
    }

    private suspend fun sendGetStatusComponentInfoCommand(name: String, nodeId: String) {
        val sensorName = name.substringBefore("_")

        if (sensorsActive.none { it.startsWith(sensorName) }) {
            //In this way... we are here for any component that it's not a sensor
            sendCommand(
                nodeId = nodeId,
                typeOfCmd = PnPLTypeOfCommand.Status,
                cmd = PnPLCmd(command = "get_status", request = name),
                askTheStatus = false
            )
        } else {
            sensorsActive.filter {
                it.startsWith(sensorName)
            }.forEach {
                sendCommand(
                    nodeId = nodeId,
                    typeOfCmd = PnPLTypeOfCommand.Status,
                    cmd = PnPLCmd(command = "get_status", request = it),
                    askTheStatus = false
                )
            }
        }
    }

    private suspend fun sendSetTimeCommand(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val sdf = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.ROOT)
        val datetime = sdf.format(Date(timeInMillis))

        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Set,
            cmd = PnPLCmd(
                component = LOG_CONTROLLER_JSON_KEY,
                command = "set_time",
                fields = mapOf("datetime" to datetime)
            ),
            askTheStatus = false
        )
    }

    private fun onFeaturesEnabled(nodeId: String) {
        viewModelScope.launch {
            val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)

            var maxWriteLength =
                node?.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength ?:20
            node?.let {
                if (maxWriteLength > (node.maxPayloadSize)) {
                    maxWriteLength = (node.maxPayloadSize)
                }
            }
            pnplFeature?.setMaxPayLoadSize(maxWriteLength)

            if (_sensors.value.isEmpty() || _tags.value.isEmpty()) {
                getModel(nodeId = nodeId)

                sendGetLogControllerCommand(nodeId = nodeId)
            }
        }
    }

    private suspend fun sendDisableTagCommand(
        nodeId: String,
        index: Int,
        askTheStatus: Boolean = false
    ) {
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Set,
            PnPLCmd(
                command = TAGS_INFO_JSON_KEY,
                request = tagNames[index],
                fields = mapOf(ENABLED_JSON_KEY to false)
            ),
            askTheStatus = askTheStatus
        )
    }

    private suspend fun handlePnplResponses(nodeId: String, data: PnPLConfig) {
        data.deviceStatus.value?.components?.let { json ->
            //Search the Spontaneous messages
            val message = searchInfoWarningError(json)

            message?.let {
                _statusMessage.update { message }

                if (message == PnPLSpontaneousMessageType.ERROR) {
                    //Remove the command from the list and send Next One
                    if (commandQueue.isNotEmpty()) {
                        commandQueue.removeAt(0)
                        if (commandQueue.isNotEmpty()) {
                            pnplFeature?.let {
                                blueManager.writeFeatureCommand(
                                    responseTimeout = 0,
                                    nodeId = nodeId, featureCommand = PnPLCommand(
                                        feature = it,
                                        cmd = commandQueue[0].pnpLCommand
                                    )
                                )
                                createAndStartTimer()
                            }
                        }
                    }
                }
            }

            //Search the Set/Command Response if they are allowed for the fw
            //data.setCommandResponse.value?.let { setCommandResponse ->
            if (data.setCommandResponse.value != null) {
                if (data.setCommandResponse.value?.response != null) {
                    if (data.setCommandResponse.value?.response?.status == false) {
                        //Report one Message...
                        val messageStatus = PnPLSpontaneousMessageType.ERROR
                        messageStatus.message =
                            data.setCommandResponse.value?.response?.message ?: "Generic Error"
                        _statusMessage.update { messageStatus }
                    }

                    //Ask the status
                    commandQueue.firstOrNull()?.let { firstOneCmd ->

                        //Remove the command from the list and send Next One
                        Log.w(TAG, "handlePnplResponses")
                        if (commandQueue.isNotEmpty()) {
                            commandQueue.removeAt(index = 0)
                        }

                        if (commandQueue.isNotEmpty()) {
                            _isLoading.update { true }

                            pnplFeature?.let {
                                blueManager.writeFeatureCommand(
                                    responseTimeout = 0,
                                    nodeId = nodeId,
                                    featureCommand = PnPLCommand(
                                        feature = it,
                                        cmd = commandQueue[0].pnpLCommand
                                    )
                                )

                                createAndStartTimer()
                            }
                        }

                        if (firstOneCmd.askTheStatus) {
                            when (firstOneCmd.typeOfCommand) {
                                PnPLTypeOfCommand.Command -> {
                                    firstOneCmd.pnpLCommand.component?.let { compName ->
                                        sendGetStatusComponentInfoCommand(
                                            name = compName,
                                            nodeId = nodeId
                                        )
                                    }
                                }

                                PnPLTypeOfCommand.Set -> sendGetStatusComponentInfoCommand(
                                    name = firstOneCmd.pnpLCommand.command,
                                    nodeId = nodeId
                                )

                                PnPLTypeOfCommand.Log -> sendGetLogControllerCommand(
                                    nodeId = nodeId
                                )

                                else -> Unit
                            }
                        }
                    }
                } else {
                    if (commandQueue.isNotEmpty()) {
                        commandQueue.firstOrNull()?.let { firstOneCmd ->
                            if (firstOneCmd.typeOfCommand != PnPLTypeOfCommand.Status) {
                                val messageStatus = PnPLSpontaneousMessageType.ERROR
                                messageStatus.message =
                                    "Status Message from a not Get Status Command[${firstOneCmd.typeOfCommand}]"
                                _statusMessage.update { messageStatus }
                            } else {

                                if (commandQueue.isNotEmpty()) {
                                    commandQueue.removeAt(index = 0)
                                }

                                if (commandQueue.isNotEmpty()) {
                                    pnplFeature?.let {
                                        _isLoading.update { true }
                                        blueManager.writeFeatureCommand(
                                            responseTimeout = 0,
                                            nodeId = nodeId,
                                            featureCommand = PnPLCommand(
                                                feature = it, cmd = commandQueue[0].pnpLCommand
                                            )
                                        )

                                        createAndStartTimer()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun initDemo(nodeId: String) {
        if (shouldInitDemo) {
            shouldInitDemo = false
            if (_isLogging.value) {
                sendGetTagsInfoCommand(nodeId)
            } else {
                if (shouldDisableTags) {
                    shouldDisableTags = false
                    for (index in 0..<tagNames.size) {
                        sendDisableTagCommand(nodeId = nodeId, index = index)
                    }
                }

                sendGetAllCommand(nodeId = nodeId)
            }
        }
    }

    private fun updateUIStatus(data: PnPLConfig) {
        data.deviceStatus.value?.components?.let { json ->
            if (json.size == 1) {
                json.find { it.containsKey(PNPL_RESPONSE_JSON_KEY) } ?: run {
                    _componentStatusUpdates.update {
                        it.filter { jo -> jo.keys != json.first().keys } + json
                    }
                }
            } else {
                _componentStatusUpdates.update { json }
            }
        }
    }

    private fun handleStatusUpdate(data: PnPLConfig) {
        data.deviceStatus.value?.components?.let { json ->
            json.find { it.containsKey(LOG_CONTROLLER_JSON_KEY) }
                ?.get(LOG_CONTROLLER_JSON_KEY)?.jsonObject?.let { logControllerJson ->
                    _isSDCardInserted.update {
                        logControllerJson[SD_JSON_KEY]?.jsonPrimitive?.booleanOrNull ?: false
                    }
                    _isLogging.update {
                        logControllerJson[LOG_STATUS_JSON_KEY]?.jsonPrimitive?.booleanOrNull
                            ?: false
                    }

                    if (!_isLogging.value && setShouldInitDemoAtResponse) {
                        setShouldInitDemoAtResponse = false
                        shouldInitDemo = true
                    }

                    Log.d(TAG, "isLoading ${_isLoading.value}")
                    Log.d(TAG, "isLogging ${_isLogging.value}")
                    Log.d(TAG, "isSDCardInserted ${_isSDCardInserted.value}")
                }
        }
    }

        private fun handleNumActiveTagsUpdate(data: PnPLConfig) {
        data.deviceStatus.value?.components?.let { json ->
            json.find { it.containsKey(TAGS_INFO_JSON_KEY) }
                ?.get(TAGS_INFO_JSON_KEY)?.jsonObject?.let { tags ->

                    var locNumActiveTags = 0
                    tags.forEach {
                        val key = it.key
                        val value = it.value
                        if (tagNames.contains(key)) {
                            if (value is JsonObject) {
                                val enabledJsonElement = value.getOrDefault(ENABLED_JSON_KEY, null)
                                enabledJsonElement?.let { enabled ->
                                    val labelJsonElement = value.getOrDefault(LABEL_JSON_KEY, null)
                                    labelJsonElement?.let { label ->
                                        val statusJsonElement =
                                            value.getOrDefault(STATUS_JSON_KEY, null)
                                        statusJsonElement?.let { _ ->
                                            if (enabled.jsonPrimitive.booleanOrNull == true) {
                                                locNumActiveTags++
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    _numActiveTags.update {locNumActiveTags}
                }
        }
    }

    fun sendCommand(nodeId: String, name: String, commandRequest: CommandRequest?) {
        commandRequest?.let {
            //add the command to the list
            viewModelScope.launch {
                addCommandToQueueAndCheckSend(
                    nodeId = nodeId,
                    newCommand = SetCommandPnPLRequest(
                        typeOfCommand = PnPLTypeOfCommand.Command,
                        pnpLCommand = PnPLCmd(
                            component = name,
                            command = commandRequest.commandName,
                            fields = commandRequest.request
                        ),
                        //askTheStatus = it.commandName != "load_file"
                        askTheStatus = true
                    )
                )
            }
        }
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        viewModelScope.launch {
            sendCommand(
                nodeId = nodeId,
                typeOfCmd = PnPLTypeOfCommand.Set,
                PnPLCmd(
                    command = name,
                    fields = mapOf(value)
                ),
                askTheStatus = true
            )
        }
    }

    fun resetConnectionLost() {
        viewModelScope.launch {
            _isConnectionLost.update { false }
        }
    }

    fun disconnect(nodeId: String) {
        GlobalConfig.navigateBack?.invoke(nodeId)
    }

    fun cleanStatusMessage() {
        viewModelScope.launch {
            _statusMessage.update { null }
        }
    }

    fun startLog(nodeId: String) {
        viewModelScope.launch {
            sendSetTimeCommand(nodeId = nodeId)
            sendCommand(
                nodeId = nodeId,
                typeOfCmd = PnPLTypeOfCommand.Log,
                cmd = PnPLCmd.START_LOG,
                askTheStatus = true
            )
        }
    }

    fun stopLog(nodeId: String) {
        runBlocking {
            sendCommand(
                nodeId = nodeId,
                typeOfCmd = PnPLTypeOfCommand.Log,
                cmd = PnPLCmd.STOP_LOG,
                askTheStatus = true
            )

            setShouldInitDemoAtResponse = true
            shouldDisableTags = false
        }

    }

    fun initDemo() {
        //Should be already done... but.. just in order to be sure...
        enableStartStopDemo = true
    }

    fun startDemo(nodeId: String) {
        if(enableStartStopDemo) {
            observeFeatureJob?.cancel()
            observeNodeStatusJob?.cancel()
            tagNames.clear()
            _numActiveTags.update { 0 }

            runBlocking {
                val tags = blueManager.getDtmiModel(
                    nodeId = nodeId, isBeta = stPreferences.isBetaApplication()
                )?.extractComponent(compName = TAGS_INFO_JSON_KEY)?.firstOrNull()

                tags?.let {
                    tagNames.addAll(tags.second.contents
                        .filter { tagsPropNamePredicate(it.name) }
                        .map { it.name })
                }
            }

            observeNodeStatusJob = viewModelScope.launch {
                blueManager.getNodeStatus(nodeId = nodeId)
                    .collect {
                        if (it.connectionStatus.prev == NodeState.Ready &&
                            it.connectionStatus.current == NodeState.Disconnected
                        ) {
                            _isConnectionLost.update { true }
                        }
                    }
            }

            if (pnplFeature == null) {
                pnplFeature =
                    blueManager.nodeFeatures(nodeId = nodeId).filter { it.name == PnPL.NAME }
                        .filterIsInstance<PnPL>().firstOrNull()

                pnplFeature?.let { feature ->
                    observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
                        features = listOf(feature),
                        onFeaturesEnabled = { onFeaturesEnabled(nodeId = nodeId) })
                        .flowOn(context = Dispatchers.IO).map { it.data }.onEach { data ->
                            if (data is PnPLConfig) {

                                stopTimer()

                                handleStatusUpdate(data = data)

                                updateUIStatus(data = data)

                                handleNumActiveTagsUpdate(data = data)

                                initDemo(nodeId = nodeId)

                                getModel(nodeId = nodeId)

                                handlePnplResponses(nodeId = nodeId, data = data)
                            }
                        }.launchIn(scope = viewModelScope)
                }
            }
        } else {
            enableStartStopDemo = true
        }
    }

    fun stopDemo(nodeId: String) {
        if(enableStartStopDemo) {
            observeFeatureJob?.cancel()
            observeNodeStatusJob?.cancel()

            _componentStatusUpdates.update { emptyList() }

            shouldInitDemo = true
            tagNames.clear()

            //Not optimal... but in this way... I am able to see the get status if demo is customized
            runBlocking {
                pnplFeature?.let { feature ->
                    blueManager.disableFeatures(nodeId = nodeId, features = listOf(feature))

                    _sensors.update { emptyList() }
                    _tags.update { emptyList() }

                    pnplFeature = null
                }
            }
        }
    }

    fun refresh(nodeId: String) {
        viewModelScope.launch {
            sendGetAllCommand(nodeId = nodeId)
        }
    }

    fun setEnableStartStopDemo(value: Boolean) {
        enableStartStopDemo = value
    }

    companion object {
        const val LOG_STATUS_JSON_KEY = "log_status"
        const val SD_JSON_KEY = "sd_mounted"
        const val LOG_CONTROLLER_JSON_KEY = "log_controller"
        const val PNPL_RESPONSE_JSON_KEY = "PnPL_Response"
        const val STATUS_JSON_KEY = "status"

        private const val TAGS_INFO_JSON_KEY = "tags_info"
        private const val ACQUISITION_INFO_JSON_KEY = "acquisition_info"
        private const val LABEL_JSON_KEY = "label"
        private const val ENABLED_JSON_KEY = "enabled"

        private const val TAG = "HsdlViewModel"
    }
}
