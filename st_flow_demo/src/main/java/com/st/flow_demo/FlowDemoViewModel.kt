/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.flow_demo

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.board_catalog.models.OptionByte
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.flow_demo.helpers.FlowSaveDeleteState
import com.st.flow_demo.helpers.getCounterFlowList
import com.st.flow_demo.helpers.getExpFlowList
import com.st.flow_demo.helpers.getFunctionList
import com.st.flow_demo.helpers.getOutputList
import com.st.flow_demo.helpers.getSensorList
import com.st.flow_demo.helpers.loadExampleFlows
import com.st.flow_demo.helpers.saveFlow
import com.st.flow_demo.models.Flow
import com.st.flow_demo.models.Function
import com.st.flow_demo.models.Output
import com.st.blue_sdk.board_catalog.models.Sensor
import com.st.blue_sdk.bt.advertise.getOptBytes
import com.st.blue_sdk.bt.advertise.getOptBytesOffset
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtConfigCommands
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.core.api.ApplicationAnalyticsService
import com.st.ext_config.util.dateToString
import com.st.ext_config.util.timeToString
import com.st.flow_demo.helpers.parseFlowFile
import com.st.flow_demo.uploader.CommunicationError
import com.st.flow_demo.uploader.FlowUploaderHelper
import com.st.flow_demo.uploader.startFlowMessage
import com.st.preferences.StPreferences
import com.st.ui.composables.CommandRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.io.FileNotFoundException
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class FlowDemoViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope,
    private val stPreferences: StPreferences,
    private val appAnalyticsService: Set<@JvmSuppressWildcards ApplicationAnalyticsService>
) : ViewModel() {

    var node: Node? = null

    var flowSelected: Flow? = null
    var expressionSelected: Flow? = null

    var flowOnCreation: Flow? = null

    var sensorOnConfig: Sensor? = null
    var functionOnConfig: Function? = null
    var outputOnConfig: Output? = null

    private val _sensorsList =
        MutableStateFlow<List<Sensor>>(
            emptyList()
        )
    val sensorsList: StateFlow<List<Sensor>>
        get() = _sensorsList.asStateFlow()

    private val _expansionSensorsList =
        MutableStateFlow<List<Sensor>>(
            emptyList()
        )
    val expansionSensorsList: StateFlow<List<Sensor>>
        get() = _expansionSensorsList.asStateFlow()

    private val _flowsExampleList =
        MutableStateFlow<List<Flow>>(
            emptyList()
        )
    val flowsExampleList: StateFlow<List<Flow>>
        get() = _flowsExampleList.asStateFlow()


    private val _flowsCustomList =
        MutableStateFlow<List<Flow>>(
            emptyList()
        )
    val flowsCustomList: StateFlow<List<Flow>>
        get() = _flowsCustomList.asStateFlow()


    private val _flowSaveDeleteState = MutableStateFlow(FlowSaveDeleteState.DEAD_BEEF)
    val flowSaveDeleteState: StateFlow<FlowSaveDeleteState>
        get() = _flowSaveDeleteState.asStateFlow()

    private val _availableFunctions = MutableStateFlow<List<Function>>(emptyList())
    val availableFunctions: StateFlow<List<Function>>
        get() = _availableFunctions


    private val _availableOutputs = MutableStateFlow<List<Output>>(emptyList())
    val availableOutputs: StateFlow<List<Output>>
        get() = _availableOutputs

    private val _availableExpFlow = MutableStateFlow<List<Flow>>(emptyList())
    val availableExpFlow: StateFlow<List<Flow>>
        get() = _availableExpFlow

    private val _availableCounterFlow = MutableStateFlow<List<Flow>>(emptyList())
    val availableCounterFlow: StateFlow<List<Flow>>
        get() = _availableCounterFlow

    private val _flowBytesSent = MutableStateFlow<Int>(0)
    val flowByteSent: StateFlow<Int>
        get() = _flowBytesSent

    private val _flowMessageReceived = MutableStateFlow<Pair<CommunicationError, String?>>(
        Pair(
            CommunicationError.FLOW_NO_ERROR,
            null
        )
    )
    val flowMessageReceived: StateFlow<Pair<CommunicationError, String?>>
        get() = _flowMessageReceived

    private var connectionJob: Job? = null

    private val _modelUpdates =
        mutableStateOf<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>(
            emptyList()
        )
    val modelUpdates: State<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>
        get() = _modelUpdates

    private val _componentStatusUpdates = mutableStateOf<List<JsonObject>>(emptyList())
    val componentStatusUpdates: State<List<JsonObject>>
        get() = _componentStatusUpdates

    private val _isLoading = mutableStateOf(value = false)
    val isLoading: State<Boolean>
        get() = _isLoading

    private val _enableCollapse = mutableStateOf(value = false)
    val enableCollapse: State<Boolean>
        get() = _enableCollapse

    private val _lastStatusUpdatedAt = mutableStateOf(value = 0L)
    val lastStatusUpdatedAt: State<Long>
        get() = _lastStatusUpdatedAt

    private var featurePnPL: Feature<*>? = null
    private var observeFeaturePnPLJob: Job? = null

    fun startDemo(nodeId: String, context: Context) {
        //node = blueManager.getNode(nodeId)

        blueManager.nodeFeatures(nodeId).firstOrNull { it.name == PnPL.NAME }?.let { feature ->
            featurePnPL = feature
        }

        viewModelScope.launch(Dispatchers.IO) {
            node = blueManager.getNodeWithFirmwareInfo(nodeId)

            node?.let {
                //Read the Sensors List
                _sensorsList.value = getSensorList(context, node!!.boardType)

                //Read the Example Flows
                _flowsExampleList.value = loadExampleFlows(context, node!!.boardType)

                //Read all the Resources needed for Expert View
                readAvailableResourcesForExpertView(context)

                setTimeDate(nodeId = nodeId)
            }
        }

        featurePnPL?.let {
            observeFeaturePnPLJob?.cancel()
            observeFeaturePnPLJob = viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(featurePnPL!!))
                    .collect { featureUpdate ->
                        val data = featureUpdate.data

                        if (data is PnPLConfig) {
                            data.deviceStatus.value?.components?.let { json ->
                                _lastStatusUpdatedAt.value = System.currentTimeMillis()
                                _componentStatusUpdates.value = json
                                _isLoading.value = false
                            }
                        }
                    }
            }
        }
    }

    fun isPnPLExported(): Boolean {
        return featurePnPL != null
    }


    fun stopDemo(nodeId: String) {

        observeFeaturePnPLJob?.cancel()

        _componentStatusUpdates.value = emptyList()

        node = null
    }

    private fun sendGetAllCommand(nodeId: String) {
        featurePnPL?.let {
            viewModelScope.launch {
                _isLoading.value = true
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(feature = featurePnPL as PnPL, cmd = PnPLCmd.ALL)
                )
            }
        }
    }

    fun sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        featurePnPL?.let {
            viewModelScope.launch {
                _isLoading.value = true
                value?.let {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = PnPLCommand(
                            feature = featurePnPL as PnPL,
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

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        featurePnPL?.let {
            viewModelScope.launch {
                _isLoading.value = true
                value.let {
                    val featureCommand = PnPLCommand(
                        feature = featurePnPL as PnPL,
                        cmd = PnPLCmd(
                            command = name,
                            fields = mapOf(it)
                        )
                    )

                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = featureCommand
                    )

                    sendGetAllCommand(nodeId = nodeId)
                }
            }
        }
    }

    fun getModel(nodeId: String, compName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            _modelUpdates.value =
                blueManager.getDtmiModel(
                    nodeId = nodeId,
                    isBeta = stPreferences.isBetaApplication()
                )?.extractComponent(compName = compName)
                    ?: emptyList()

            _enableCollapse.value = false

            _isLoading.value = false

            sendGetAllCommand(nodeId = nodeId)
        }
    }

    fun getBoardType(): Boards.Model {
        var retValue = Boards.Model.GENERIC
        node?.let {
            retValue = node!!.boardType
        }
        return retValue
    }

    fun getFlowByID(flowId: String): Flow? {
        return _flowsExampleList.value.firstOrNull { it.id == flowId }
    }

    fun saveFlowOnPhone(context: Context, file: Uri?) {
        file?.let {
            val result = saveFlow(context, file, flowSelected!!)
            _flowSaveDeleteState.value = result
            if (result == FlowSaveDeleteState.SAVED) {
                val tmpList = _flowsCustomList.value.toMutableList()
                flowSelected!!.file = DocumentFile.fromSingleUri(context, file)
                tmpList.add(flowSelected!!)
                _flowsCustomList.value = tmpList.toList()
            }
        }
    }

    fun retrieveSensorsAdapter() {
        node?.let {
            val uniqueIds = node!!.catalogInfo?.let { it.compatibleSensorAdapters }
            uniqueIds?.let { Ids ->
                val tmpList = mutableListOf<Sensor>()
                viewModelScope.launch(Dispatchers.IO) {
                    Ids.forEach {
                        val sensor = blueManager.getSensorAdapter(uniqueId = it)
                        sensor?.let {
                            tmpList.add(sensor)
                            //Log.i("CompatibleSensorAdapter", "$sensor")
                        }
                    }
                    _expansionSensorsList.value = tmpList.toList()
                }
            }
        }
    }

    fun resetSavedFlowState() {
        _flowSaveDeleteState.value = FlowSaveDeleteState.DEAD_BEEF
    }

    fun deleteFlow() {
        //Delete the selected flow
        flowSelected?.let {
            val result = it.file?.delete()
            if (result == true) {
                val tmpList = _flowsCustomList.value.toMutableList()
                tmpList.remove(it)
                _flowsCustomList.value = tmpList.toList()
                _flowSaveDeleteState.value = FlowSaveDeleteState.DELETED
            } else {
                _flowSaveDeleteState.value = FlowSaveDeleteState.ERROR_DELETING
            }
        }
    }

    private fun readAvailableResourcesForExpertView(context: Context) {
        readFunctions(context)
        readOutputs(context)
        readExpFlows(context)
        readCounterFlow(context)
    }

    private fun readFunctions(context: Context) {
        node?.let {
            //Read the Functions List
            _availableFunctions.value = getFunctionList(context, node!!.boardType)
        }
    }

    private fun readOutputs(context: Context) {
        node?.let {
            //Read the Outputs List
            _availableOutputs.value = getOutputList(context, node!!.boardType)
        }
    }

    private fun readExpFlows(context: Context) {
        node?.let {
            //Read the Exp Flows
            _availableExpFlow.value = getExpFlowList(context, node!!.boardType)
        }
    }

    private fun readCounterFlow(context: Context) {
        node?.let {
            //Read the Exp Flows
            _availableCounterFlow.value = getCounterFlowList(context, node!!.boardType)
        }
    }

    fun sendFlowToBoard(flowCompressed: ByteArray) {
        node?.let {
            _flowBytesSent.value = 0
            _flowMessageReceived.value = Pair(CommunicationError.FLOW_NO_ERROR, null)

            //Start listening Messages on DebugConsole
            //Each message from .box/.box-Pro are 20 bytes
            connectionJob?.cancel()
            connectionJob = coroutineScope.launch {
                //val buffer = StringBuffer()
                blueManager.getDebugMessages(nodeId = it.device.address)?.collect {
                    //buffer.append(it.payload)
                    //buffer.delete(0, buffer.length)

                    Log.i("FlowTmp", " [${it.payload.length}] <${it.payload}>")
                    val escapedMessage = it.payload.apply {
                        replace("\n", "")
                        replace("\r", "")
                    }

                    when {
                        escapedMessage.startsWith(FlowUploaderHelper.SEND_FLOW_RESPONSE) -> {
                            //Send First Real package
                            val nextMessage = prepareNextMessage(flowCompressed, 0)
                            nextMessage?.let {
                                sendMessage(nextMessage)
                            }
                            _flowBytesSent.value = 0
                            _flowMessageReceived.value =
                                Pair(CommunicationError.FLOW_NO_ERROR, "Start First Message")
                        }

                        escapedMessage.startsWith(FlowUploaderHelper.FLOW_PARSED_MESSAGE_OK) -> {
                            //Flow Fully Received and parsed
                            _flowMessageReceived.value =
                                Pair(
                                    CommunicationError.FLOW_RECEIVED_AND_PARSED,
                                    "Flow Received and Parsed"
                                )
                        }

                        escapedMessage.startsWith(FlowUploaderHelper.SEND_PARSING_FLOW_RESPONSE) -> {
                            //Flow Fully Received, parsing on going
                            _flowMessageReceived.value =
                                Pair(
                                    CommunicationError.FLOW_RECEIVED,
                                    "Flow Received Parsing on going"
                                )
                        }

                        escapedMessage.startsWith(FlowUploaderHelper.FLOW_ERROR_MESSAGE) -> {
                            //Error....
                            var errorCode = CommunicationError.GENERIC_ERROR.code
                            try {
                                val parsedError = it.payload.removeTerminatorCharacters()
                                    .substring(it.payload.indexOf(":") + 1)
                                errorCode = Integer.parseInt(parsedError)
//                                if ((errorCode > CommunicationError.FLOW_COMPATIBILITY_ERROR.code) || (errorCode < CommunicationError.FW_VERSION_ERROR.code)) {
//                                    errorCode = CommunicationError.GENERIC_ERROR.code
//                                }
                            } catch (ignored: Exception) {
                            } finally {
                                val code = CommunicationError.getCommunicationError(errorCode)
                                _flowMessageReceived.value = Pair(code, code.name)
                            }
                            return@collect
                        }

                        else -> {
                            //Send Next Package
                            val nextMessage = prepareNextMessage(flowCompressed, it.payload.length)
                            nextMessage?.let {
                                sendMessage(nextMessage)
                            }
                            //_flowBytesSent.value += it.payload.length
                            _flowMessageReceived.value =
                                Pair(CommunicationError.FLOW_NO_ERROR, "Sending...")
                        }
                    }
                }
            }
            //First Message with the Dimension of the Compressed Flow
            Log.d("FlowTmp", "startFlowMessage")
            val message = startFlowMessage(flowCompressed.size)
            sendMessage(message)
        }
    }

    private fun prepareNextMessage(flowCompressed: ByteArray, length: Int): ByteArray? {
        _flowBytesSent.value += length
        if (_flowBytesSent.value > flowCompressed.size) {
            return null
        }
        val lastChar = minOf(
            _flowBytesSent.value + FlowUploaderHelper.CHARACTERISTIC_SIZE,
            flowCompressed.size
        )
        val lenDataToSend = lastChar - _flowBytesSent.value
        val dataToSend = ByteArray(lenDataToSend)
        flowCompressed.copyInto(dataToSend, 0, _flowBytesSent.value, lastChar)
        return dataToSend
    }

    private fun sendMessage(message: ByteArray) {
        node?.let {
            viewModelScope.launch {
                blueManager.writeDebugMessage(
                    //nodeId = it.device.address, msg = message.toString()
                    nodeId = it.device.address, msg = String(message, StandardCharsets.ISO_8859_1)
                )
            }
        }
    }

    private fun String.removeTerminatorCharacters(): String {
        return this.replace("\n", "").replace("\r", "")
    }

    fun getNodeId(): String? {
        return node?.device?.address
    }


    fun reportExampleAppAnalytics(flow: Flow) {
        appAnalyticsService.forEach {
            it.flowExampleAppAnalytics(flowName = flow.description)
        }
    }

    fun reportExpertAppAnalytics(flow: Flow) {
        appAnalyticsService.forEach {
            it.flowExpertAppAnalytics(flowName = flow.description)
        }
    }

    fun reportExpertAppInputAnalytics(flow: Flow) {
        appAnalyticsService.forEach { service ->
            flow.sensors.forEach { sensor ->
                sensor.configuration?.let { config ->
                    service.flowExpertAppInputSensorAnalytics(
                        id = sensor.id,
                        model = sensor.model,
                        odr = config.odr
                    )
                }
            }
        }
    }

    fun reportExpertAppFunctionAnalytics(flow: Flow) {
        appAnalyticsService.forEach { service ->
            flow.functions.forEach { function ->
                service.flowExpertAppFunctionAnalytics(
                    id = function.id,
                    desc = function.description
                )
            }
        }
    }

    fun reportExpertAppOutputAnalytics(flow: Flow) {
        appAnalyticsService.forEach { service ->
            flow.outputs.forEach { output ->
                service.flowExpertAppOutputAnalytics(id = output.id, desc = output.description)
            }
        }
    }

    fun getMountedDil24FromOptionBytes(): String? {
        if (node != null) {
            if ((node!!.advertiseInfo != null) && (node!!.catalogInfo != null)) {
                val optionByte = node!!.catalogInfo!!.optionBytes[0]
                if (OptionByte.OptionByteValueType.fromFormat(optionByte.format) == OptionByte.OptionByteValueType.ENUM_STRING) {
                    val optionByteValue =
                        node!!.advertiseInfo!!.getOptBytes()[0 + 1 + node!!.advertiseInfo!!.getOptBytesOffset()]

                    if (optionByteValue != optionByte.escapeValue) {
                        return optionByte.stringValues!!.find { it.value == optionByteValue }?.displayName
                    }
                }
            }
        }
        return null
    }

    fun getRunningFlowFromOptionBytes(): String? {
        if (node != null) {
            if ((node!!.advertiseInfo != null) && (node!!.catalogInfo != null)) {
                val optionByte = node!!.catalogInfo!!.optionBytes[1]
                if (OptionByte.OptionByteValueType.fromFormat(optionByte.format) == OptionByte.OptionByteValueType.ENUM_STRING) {
                    val optionByteValue =
                        node!!.advertiseInfo!!.getOptBytes()[0 + 2 + node!!.advertiseInfo!!.getOptBytesOffset()]

                    if (optionByteValue != optionByte.escapeValue) {
                        return optionByte.stringValues!!.find { it.value == optionByteValue }?.displayName
                    }
                }
            }
        }
        return null
    }


    fun parseSavedFlow(context: Context, dir: DocumentFile?) {
        dir?.let {

            val flows = mutableListOf<Flow>()

            dir.listFiles().forEach { file ->
                if (file.isFile) {
                    try {
                        val stream = context.contentResolver.openInputStream(file.uri)

                        stream?.let {
                            Log.i("parseFlowFile", "${file.name}")
                            val flow = parseFlowFile(stream)
                            if (flow != null && flow.version == Flow.FLOW_VERSION && flow.board_compatibility.contains(
                                    node!!.boardType.name
                                )
                            ) {
                                flow.file = file
                                flows.add(flow)
                            }
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }

            }

            _flowsCustomList.value = flows
        }
    }

    private fun setTimeDate(nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find { feature ->
            ExtConfiguration.NAME == feature.name
        }?.let {
            //Set Time Command
            viewModelScope.launch {
                var featureCommand = ExtendedFeatureCommand(
                    feature = it as ExtConfiguration,
                    extendedCommand = ExtConfigCommands.buildConfigCommand(
                        command = ExtConfigCommands.SET_TIME,
                        argString = Date().timeToString()
                    ),
                    hasResponse = false
                )

                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = featureCommand
                )

                //Set Date Command
                featureCommand = ExtendedFeatureCommand(
                    feature = it as ExtConfiguration,
                    extendedCommand = ExtConfigCommands.buildConfigCommand(
                        command = ExtConfigCommands.SET_DATE,
                        argString = Date().dateToString()
                    ),
                    hasResponse = false
                )

                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = featureCommand
                )
            }
        }
    }
}