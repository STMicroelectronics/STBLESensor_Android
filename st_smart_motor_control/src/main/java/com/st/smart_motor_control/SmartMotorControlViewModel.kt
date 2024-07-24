/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.smart_motor_control

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.board_catalog.models.DtmiContent.DtmiPropertyContent.DtmiIntegerPropertyContent
import com.st.blue_sdk.board_catalog.models.DtmiType
import com.st.blue_sdk.features.Feature
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.blue_sdk.features.extended.raw_controlled.RawControlledInfo
import com.st.blue_sdk.features.extended.raw_controlled.decodeRawData
import com.st.blue_sdk.features.extended.raw_controlled.model.RawStreamIdEntry
import com.st.blue_sdk.features.extended.raw_controlled.readRawPnPLFormat
import com.st.blue_sdk.models.NodeState
import com.st.core.GlobalConfig
import com.st.pnpl.composable.PnPLSpontaneousMessageType
import com.st.pnpl.composable.searchInfoWarningError
import com.st.pnpl.util.PnPLTypeOfCommand
import com.st.pnpl.util.SetCommandPnPLRequest
import com.st.preferences.StPreferences
import com.st.smart_motor_control.model.MotorControlFault
import com.st.smart_motor_control.model.MotorControlFault.Companion.getErrorCodeFromValue
import com.st.ui.composables.CommandRequest
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
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private typealias ComponentWithInterface = Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>

@HiltViewModel
class SmartMotorControlViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var observeFeatureJob: Job? = null
    private var observeNodeStatusJob: Job? = null
    private val pnplFeatures: MutableList<Feature<*>> = mutableListOf()
    private var shouldInitDemo: Boolean = true
    private var setShouldInitDemoAtResponse: Boolean = false
    private var shouldRenameTags: Boolean = true

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

    private val _sensorsActuators =
        MutableStateFlow<List<ComponentWithInterface>>(
            emptyList()
        )
    val sensorsActuators: StateFlow<List<ComponentWithInterface>>
        get() = _sensorsActuators.asStateFlow()

    private val _motorSpeedControl = MutableStateFlow<DtmiIntegerPropertyContent?>(null)
    val motorSpeedControl: StateFlow<DtmiIntegerPropertyContent?>
        get() = _motorSpeedControl

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

    private val rawPnPLFormat: MutableList<RawStreamIdEntry> = mutableListOf()

    private val _faultStatus = MutableStateFlow<MotorControlFault>(MotorControlFault.None)
    val faultStatus: StateFlow<MotorControlFault>
        get() = _faultStatus

    private val _temperature = MutableStateFlow<Int?>(null)
    val temperature: StateFlow<Int?>
        get() = _temperature

    private val _speedRef = MutableStateFlow<Int?>(null)
    val speedRef: StateFlow<Int?>
        get() = _speedRef

    private val _speedMeas = MutableStateFlow<Int?>(null)
    val speedMeas: StateFlow<Int?>
        get() = _speedMeas

    private val _busVoltage = MutableStateFlow<Int?>(null)
    val busVoltage: StateFlow<Int?>
        get() = _busVoltage

    private val _neaiClassName = MutableStateFlow<String?>(null)
    val neaiClassName: StateFlow<String?>
        get() = _neaiClassName

    private val _neaiClassProb = MutableStateFlow<Float?>(null)
    val neaiClassProb: StateFlow<Float?>
        get() = _neaiClassProb

    private val _isMotorRunning = MutableStateFlow(false)
    val isMotorRunning = _isMotorRunning.asStateFlow()

    private val _motorSpeed = MutableStateFlow(1024)
    val motorSpeed = _motorSpeed.asStateFlow()

    private val _statusMessage: MutableStateFlow<PnPLSpontaneousMessageType?> = MutableStateFlow(null)
    val statusMessage: StateFlow<PnPLSpontaneousMessageType?>
        get() = _statusMessage.asStateFlow()

    private val _isConnectionLost = MutableStateFlow(false)
    val isConnectionLost: StateFlow<Boolean>
        get() = _isConnectionLost.asStateFlow()

    private var nodeIdLocal: String?=null

    var temperatureUnit: String = "°C"
    var speedRefUnit: String = "rpm"
    var speedMeasUnit: String = "rpm"
    var busVoltageUnit: String = "Volt"

    private fun sensorPropNamePredicate(name: String): Boolean =
        name == "odr" || name == "fs" ||
                name == "enable" || name == "aop" ||
                name == "load_file" || name == "ucf_status" || name == "mounted"

    private fun actuatorsPropNamePredicate(name: String): Boolean =
        name == "enable" || name == "st_ble_stream"

    private fun motorControlPropNamePredicate(name: String): Boolean =
        name == "motor_speed"

    private fun tagsNamePredicate(name: String): Boolean =
        name == TAGS_INFO_JSON_KEY || name == ACQUISITION_INFO_JSON_KEY

    private fun acquisitionPropNamePredicate(name: String): Boolean =
        name == "name" || name == "description"

    private fun tagsPropNamePredicate(name: String): Boolean =
        name != "max_tags_num"

    private fun List<DtmiContent>.motorControlTagPropertyFilter() =
        filter { content -> acquisitionPropNamePredicate(content.name) }

    private fun List<DtmiContent>.motorControlSensorPropertyFilter() =
        filter { content -> sensorPropNamePredicate(content.name) }


    private fun List<DtmiContent>.motorControlActuatorPropertyFilter() =
        filter { content -> actuatorsPropNamePredicate(content.name) }

    private fun List<DtmiContent>.motorControlPropertyFilter() =
        filter { content -> motorControlPropNamePredicate(content.name) }


    private fun List<ComponentWithInterface>.motorControlSensorsFilter() =
        filter {
            it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR
        }.map {
            Pair(
                it.first,
                it.second.copy(contents = it.second.contents.motorControlSensorPropertyFilter())
            )
        }

    private fun List<ComponentWithInterface>.motorControlActuatorsFilter() =
        filter {
            it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.ACTUATORS
        }.map {
            Pair(
                it.first,
                it.second.copy(contents = it.second.contents.motorControlActuatorPropertyFilter())
            )
        }

    private fun List<ComponentWithInterface>.motorControlFilter() =
        filter {
            it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.OTHER &&
                    it.first.name == MOTOR_CONTROLLER_JSON_KEY
        }.map {
            Pair(
                it.first,
                it.second.copy(contents = it.second.contents.motorControlPropertyFilter())
            )
        }

    init {
        viewModelScope.launch {
            isLogging.collect { value ->
                MotorControlConfig.isLogging = value
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
        filter { content -> //  tag not enabled when MotorControlConfig has tag label override is hide
            if (MotorControlConfig.tags.isNotEmpty()) {
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
        map { content -> //  tag component when MotorControlConfig has tag label override hide enabled toggle
            if (content.type == DtmiType.PROPERTY && content is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent) {
                if (content.schema is DtmiContent.DtmiObjectContent) {
                    val schema = content.schema as DtmiContent.DtmiObjectContent
                    return@map content.copy(schema = schema.copy(fields = schema.fields.filter {
                        if (MotorControlConfig.tags.isNotEmpty()) {
                            it.name != ENABLED_JSON_KEY
                        } else {
                            true
                        }
                    }))
                }
            }

            content
        }

    private fun List<ComponentWithInterface>.motorControlTagsFilter() =
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
                                contents = contentWithInterface.second.contents.motorControlTagPropertyFilter()
                            )
                        )
                    }

                    else -> contentWithInterface
                }
            }
            .sortedBy { it.first.name }


    fun cleanStatusMessage() {
        viewModelScope.launch {
            _statusMessage.emit(null)
        }
    }


    private suspend fun getModel(nodeId: String) {
        _isLoading.value = true

        val componentWithInterface =
            blueManager.getDtmiModel(nodeId = nodeId, isBeta = stPreferences.isBetaApplication())
                ?.extractComponents(demoName = null)
                ?: emptyList()

        val tmpList: MutableList<ComponentWithInterface> = mutableListOf()

        //Add Actuators
        tmpList.addAll(componentWithInterface.motorControlActuatorsFilter())
        //Add Sensors
        tmpList.addAll(componentWithInterface.motorControlSensorsFilter())

        tmpList.addAll(componentWithInterface.motorControlTagsFilter())

        _sensorsActuators.value = tmpList.toList()


        val value =
            componentWithInterface.motorControlFilter().firstOrNull()?.second?.contents?.first()

        if (value is DtmiIntegerPropertyContent) {
            _motorSpeedControl.value = value
        }

        _tags.value = componentWithInterface.motorControlTagsFilter()

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
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(),
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
                featureCommand = PnPLCommand(
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                    cmd = PnPLCmd.ALL
                )
            )
        }
    }

    private suspend fun sendGetComponentStatus(nodeId: String, compName: String) {
        if (pnplBleResponses) {
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = PnPLTypeOfCommand.Status, pnpLCommand = PnPLCmd(command = "get_status", request = compName)
                )
            )
        } else {
            _isLoading.value = true

            blueManager.writeFeatureCommand(
                responseTimeout = 0,
                nodeId = nodeId,
                featureCommand = PnPLCommand(
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                    cmd = PnPLCmd(command = "get_status", request = compName)
                )
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
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                    cmd = PnPLCmd.LOG_CONTROLLER
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
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                    cmd = PnPLCmd.TAGS_INFO
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
        val nameFormatter = MotorControlConfig.datalogNameFormat ?: "EEE MMM d yyyy HH:mm:ss"
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
                    request = mapOf(DESC_JSON_KEY to "")
                )
            )
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
                        feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                        cmd = PnPLCmd.START_LOG
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
                        feature = pnplFeatures.filterIsInstance<PnPL>().first(),
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
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(), cmd = PnPLCmd(
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

                    value.let {
                        val featureCommand = PnPLCommand(
                            feature = pnplFeatures.filterIsInstance<PnPL>().first(), cmd = PnPLCmd(
                                command = name, fields = mapOf(it)
                            )
                        )

                        blueManager.writeFeatureCommand(
                            responseTimeout = 0,

                            nodeId = nodeId, featureCommand = featureCommand
                        )

                        sendGetComponentStatus(nodeId = nodeId, compName = name)
                    }
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
                if( it.connectionStatus.prev == NodeState.Ready && it.connectionStatus.current == NodeState.Disconnected) {
                    //GlobalConfig.navigateBack?.let { it1 -> it1(nodeId) }
                    _isConnectionLost.emit(true)
                }
            }
        }

        pnplFeatures.addAll(
            blueManager.nodeFeatures(nodeId = nodeId)
                .filter { it.name == PnPL.NAME || it.name == RawControlled.NAME }
        )

        observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
            features = pnplFeatures,
            onFeaturesEnabled = {
                viewModelScope.launch {

                    val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)
                    var maxWriteLength = node.catalogInfo?.characteristics?.firstOrNull { it.name ==  PnPL.NAME }?.maxWriteLength
                    maxWriteLength?.let {
                        if (maxWriteLength!! > (node.maxPayloadSize)) {
                            maxWriteLength = (node.maxPayloadSize)
                        }
                        (pnplFeatures.firstOrNull { it.name == PnPL.NAME } as PnPL?)?.setMaxPayLoadSize(maxWriteLength!!)
                    }

                    if (_sensorsActuators.value.isEmpty() || _tags.value.isEmpty()) {
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

                        //Search the RawPnPL Format
                        val rawPnPLFormatTmp: MutableList<RawStreamIdEntry> = mutableListOf()
                        readRawPnPLFormat(
                            rawPnPLFormat = rawPnPLFormatTmp,
                            json = json,
                            modelUpdates = _sensorsActuators.value
                        )
                        //Update the Format if it's necessary
                        if (rawPnPLFormatTmp.isNotEmpty()) {
                            rawPnPLFormat.clear()
                            rawPnPLFormat.addAll(rawPnPLFormatTmp)

                            rawPnPLFormat.forEach { stream ->
                                stream.formats.forEach { format ->
                                    if (format.name == "temperature") {
                                        format.format.unit?.let { unit ->
                                            temperatureUnit = unit
                                        }
                                    }

                                    if (format.name == "ref_speed") {
                                        format.format.unit?.let { unit ->
                                            speedRefUnit = unit
                                        }
                                    }

                                    if (format.name == "speed") {
                                        format.format.unit?.let { unit ->
                                            speedMeasUnit = unit
                                        }
                                    }

                                    if (format.name == "bus_voltage") {
                                        format.format.unit?.let { unit ->
                                            busVoltageUnit = unit
                                        }
                                    }
                                }
                            }
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
                                                feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                                                cmd = commandQueue[0].pnpLCommand
                                            )
                                        )
                                    }

                                    if (firstOne.askTheStatus) {
                                        when (firstOne.typeOfCommand) {
                                            PnPLTypeOfCommand.Command -> sendGetComponentStatus(
                                                compName = firstOne.pnpLCommand.command,
                                                nodeId = nodeId
                                            )

                                            PnPLTypeOfCommand.Set -> sendGetComponentStatus(
                                                compName = firstOne.pnpLCommand.command,
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
                                                        feature = pnplFeatures.filterIsInstance<PnPL>().first(),
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


                        json.find { it.containsKey(MOTOR_CONTROLLER_JSON_KEY) }
                            ?.get(MOTOR_CONTROLLER_JSON_KEY)?.jsonObject?.let { motorControllerJson ->

                                _isMotorRunning.value =
                                    motorControllerJson[MOTOR_STATUS_JSON_KEY]?.jsonPrimitive?.booleanOrNull
                                        ?: false
                                Log.d(TAG, "isMotorRunning ${_isMotorRunning.value}")

                                _motorSpeed.value =
                                    motorControllerJson[MOTOR_SPEED_JSON_KEY]?.jsonPrimitive?.intOrNull
                                        ?: 1024

                                Log.d(TAG, "_motorSpeed ${_motorSpeed.value}")

                            }

                        json.find { it.containsKey(TAGS_INFO_JSON_KEY) }
                            ?.get(TAGS_INFO_JSON_KEY)?.jsonObject?.let { tags ->
                                //_componentStatusUpdates.value = json

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
                                //Ask Tags info
                                sendGetTagsInfoCommand(nodeId)

                                //Ask motor controller status
                                sendGetComponentStatus(
                                    nodeId = nodeId,
                                    compName = MOTOR_CONTROLLER_JSON_KEY
                                )

                                sendGetAllCommand(nodeId = nodeId)

                            } else {
                                setName(nodeId = nodeId)
                                if (shouldRenameTags) {
                                    shouldRenameTags = false
                                    //for (index in 0..4) {
                                    for (index in 0..<numberOfTags) {
                                        if (index in 0..MotorControlConfig.tags.lastIndex) {
                                            val tagName = MotorControlConfig.tags[index]

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
                                                        askTheStatus = index == MotorControlConfig.tags.lastIndex
                                                    )
                                                )

                                            } else {
                                                _isLoading.value = true
                                                val renameCommand = PnPLCommand(
                                                    feature = pnplFeatures.filterIsInstance<PnPL>()
                                                        .first(),
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
                                                    feature = pnplFeatures.filterIsInstance<PnPL>()
                                                        .first(),
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
                                                    feature = pnplFeatures.filterIsInstance<PnPL>()
                                                        .first(),
                                                    cmd = PnPLCmd(
                                                        command = TAGS_INFO_JSON_KEY,
                                                        request = "$TAG_JSON_KEY$index",
                                                        fields = mapOf(ENABLED_JSON_KEY to false)
                                                    )
                                                )

                                                // viewModelScope.launch {
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
                } else if (data is RawControlledInfo) {

                    //Search the StreamID and decode the data
                    val streamId =
                        decodeRawData(data = data.data, rawFormat = rawPnPLFormat)

                    //Print out the data decoded
                    if (streamId != RawControlled.STREAM_ID_NOT_FOUND) {
                        val foundStream = rawPnPLFormat.firstOrNull { it.streamId == streamId }
                        foundStream?.let {

                            //Search Fault Status
                            var value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "fault" }?.format?.values?.firstOrNull()
                            if (value is Int) {
                                _faultStatus.emit(getErrorCodeFromValue(value))
                            }

                            //Search Temperature
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "temperature" }?.format?.values?.firstOrNull()

                            if (value is Int) {
                                _temperature.emit(value)
                            }

                            //Search Speed Reference
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "ref_speed" }?.format?.values?.firstOrNull()
                            if (value is Int) {
                                _speedRef.emit(value)
                            }

                            //Search Speed Measurement
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "speed" }?.format?.values?.firstOrNull()
                            if (value is Int) {
                                _speedMeas.emit(value)
                            }

                            //Search Bus voltage
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "bus_voltage" }?.format?.values?.firstOrNull()
                            if (value is Int) {
                                _busVoltage.emit(value)
                            }

                            //Search NeaiClassName
                            value = foundStream.formats.firstOrNull { entry -> entry.name == "class_neai"}?.format?.values?.firstOrNull()
                            if (value is String) {
                                _neaiClassName.emit(value)
                            }

                            //Search NeaiClassProbability
                            value = foundStream.formats.firstOrNull { entry -> entry.name == "probability_neai"}?.format?.values?.firstOrNull()
                            if (value is Float) {
                                _neaiClassProb.emit(value)
                            }

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

//        coroutineScope.launch {
//            blueManager.disableFeatures(
//                nodeId = nodeId, features = pnplFeatures
//            )
//        }
        //Not optimal... but in this way... I am able to see the get status if demo is customized
        runBlocking {
            blueManager.disableFeatures(
                nodeId = nodeId, features = pnplFeatures
            )
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
                            request = "$TAG_JSON_KEY$tag",
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
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                    cmd = PnPLCmd(
                        command = TAGS_INFO_JSON_KEY,
                        request = "$TAG_JSON_KEY$tag",
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
        const val MOTOR_CONTROLLER_JSON_KEY = "motor_controller"
        private const val SLOW_TELEMETRY_JSON_KEY = "slow_mc_telemetries"
        private const val MOTOR_STATUS_JSON_KEY = "motor_status"
        private const val MOTOR_SPEED_JSON_KEY = "motor_speed"
        private const val TAGS_INFO_JSON_KEY = "tags_info"
        private const val ACQUISITION_INFO_JSON_KEY = "acquisition_info"
        private const val LABEL_JSON_KEY = "label"
        private const val TAG_JSON_KEY = "sw_tag"
        private const val ENABLED_JSON_KEY = "enabled"
        private const val DESC_JSON_KEY = "description"
        private const val NAME_JSON_KEY = "name"
        private const val STATUS_JSON_KEY = "status"
        private const val TAG = "SmartMotorControlViewModel"
    }
}
