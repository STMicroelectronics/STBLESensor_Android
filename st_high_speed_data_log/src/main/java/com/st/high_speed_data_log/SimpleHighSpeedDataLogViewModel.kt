package com.st.high_speed_data_log

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.models.NodeState
import com.st.core.GlobalConfig
import com.st.pnpl.composable.PnPLSpontaneousMessageType
import com.st.preferences.StPreferences
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.ENABLE_PROPERTY_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

@HiltViewModel
class SimpleHighSpeedDataLogViewModel @Inject constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences
) : ViewModel() {

    private var observeFeatureJob: Job? = null
    private var observeNodeStatusJob: Job? = null
    private var pnplFeature: PnPL? = null
    private var features: List<Feature<*>> = emptyList()
    private var shouldInitDemo: Boolean = true
    private var sensorsActive = listOf<String>()
    private var componentWithInterface: List<ComponentWithInterface> = listOf()

    private val _acquisitionName = MutableStateFlow(value = "")
    private val _modelUpdates =
        MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _sensors = MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _componentStatusUpdates = MutableStateFlow<List<JsonObject>>(value = emptyList())
    private val _isLogging = MutableStateFlow(value = false)
    private val _isSDCardInserted = MutableStateFlow(value = false)
    private val _isLoading = MutableStateFlow(value = false)
    private val _isConnectionLost = MutableStateFlow(value = false)
    private val _statusMessage: MutableStateFlow<PnPLSpontaneousMessageType?> =
        MutableStateFlow(value = null)

    val sensors: StateFlow<List<ComponentWithInterface>> = _sensors.asStateFlow()
    val componentStatusUpdates: StateFlow<List<JsonObject>> = _componentStatusUpdates.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val isLogging = _isLogging.asStateFlow()
    val isSDCardInserted = _isSDCardInserted.asStateFlow()
    val statusMessage: StateFlow<PnPLSpontaneousMessageType?> = _statusMessage.asStateFlow()
    val isConnectionLost: StateFlow<Boolean> = _isConnectionLost.asStateFlow()

    var isBeta = false

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

                        val isMounted = interfaceModel.contents
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


    private fun List<DtmiContent>.hsdl2SensorPropertyFilter() =
        filter { content -> sensorPropNamePredicate(content.name) }

    private fun List<ComponentWithInterface>.hsdl2SensorsFilter() = filter {
        it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR
    }.map {
        Pair(
            it.first, it.second.copy(contents = it.second.contents.hsdl2SensorPropertyFilter())
        )
    }

    private suspend fun getModel(nodeId: String) {
        _isLoading.update { true }

        componentWithInterface =
            blueManager.getDtmiModel(nodeId = nodeId, isBeta = stPreferences.isBetaApplication())
                ?.extractComponents(demoName = null) ?: emptyList()

        _sensors.update { componentWithInterface.hsdl2SensorsFilter() }

        _isLoading.update { false }
    }

    private suspend fun sendCommand(
        nodeId: String,
        cmd: PnPLCmd,
        askTheStatus: Boolean
    ) {
        _isLoading.update { true }

        pnplFeature?.let {
            blueManager.writeFeatureCommand(
                responseTimeout = 0, nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = it, cmd = cmd
                )
            )
        }

        if (askTheStatus) {
            sendGetStatusComponentInfoCommand(name = cmd.command, nodeId = nodeId)
        }
    }

    private suspend fun sendGetAllCommand(nodeId: String) {
        sendCommand(
            nodeId = nodeId,
            cmd = PnPLCmd.ALL,
            askTheStatus = false
        )
    }

    private suspend fun sendGetLogControllerCommand(nodeId: String) {
        sendCommand(
            nodeId = nodeId,
            cmd = PnPLCmd.LOG_CONTROLLER,
            askTheStatus = false
        )
    }

    private suspend fun sendGetStatusComponentInfoCommand(name: String, nodeId: String) {
        sendCommand(
            nodeId = nodeId,
            cmd = PnPLCmd(command = "get_status", request = name),
            askTheStatus = false
        )
    }

    private suspend fun sendSetTimeCommand(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val sdf = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.ROOT)
        val datetime = sdf.format(Date(timeInMillis))

        sendCommand(
            nodeId = nodeId,
            cmd = PnPLCmd(
                component = LOG_CONTROLLER_JSON_KEY,
                command = "set_time",
                fields = mapOf("datetime" to datetime)
            ),
            askTheStatus = false
        )
    }

    private suspend fun sendSetNameCommand(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val nameFormatter = HsdlConfig.datalogNameFormat ?: "EEE MMM d yyyy HH:mm:ss"
        val sdf = SimpleDateFormat(nameFormatter, Locale.UK)
        val datetime = sdf.format(Date(timeInMillis))

        _acquisitionName.update { datetime }

        sendCommand(
            nodeId = nodeId,
            cmd = PnPLCmd(
                component = "",
                command = ACQUISITION_INFO_JSON_KEY,
                fields = mapOf(NAME_JSON_KEY to datetime, DESC_JSON_KEY to "Empty")
            ),
            askTheStatus = false
        )
    }

    private fun onFeaturesEnabled(nodeId: String) {
        viewModelScope.launch {
            val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)

            var maxWriteLength =
                node?.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength ?: 20
            node?.let {
                if (maxWriteLength > (node.maxPayloadSize)) {
                    maxWriteLength = (node.maxPayloadSize)
                }
            }
            pnplFeature?.setMaxPayLoadSize(maxWriteLength)

            if (_sensors.value.isEmpty()) {
                getModel(nodeId = nodeId)

                sendGetLogControllerCommand(nodeId = nodeId)
            }

            _modelUpdates.update {
                blueManager.getDtmiModel(
                    nodeId = nodeId,
                    isBeta = stPreferences.isBetaApplication()
                )?.filterComponentsByProperty(propName = STREAM_JSON_KEY) ?: emptyList()
            }
        }
    }


    private suspend fun initDemo(nodeId: String) {

        if (shouldInitDemo) {
            shouldInitDemo = false
            if (!_isLogging.value) {
                sendSetNameCommand(nodeId = nodeId)

                delay(500)

                sendGetAllCommand(nodeId)
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

                    Log.d(TAG, "isLoading ${_isLoading.value}")
                    Log.d(TAG, "isLogging ${_isLogging.value}")
                    Log.d(TAG, "isSDCardInserted ${_isSDCardInserted.value}")
                }
        }
    }

    private suspend fun _sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        pnplFeature?.let { pnplFeature ->
            value?.let {
                _isLoading.value = true
                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(
                        feature = pnplFeature,
                        cmd = PnPLCmd(
                            component = name,
                            command = it.commandName,
                            fields = it.request
                        )
                    )
                )
            }
        }
    }

    fun sendCommand(nodeId: String, name: String, commandRequest: CommandRequest?) {
        commandRequest?.let {
            if (it.commandName == "load_file") {
                runBlocking {
                    //RunBlocking for avoiding that the HSDataLogFragment starts
                    // before to finish the load process
                    //Without the sendGetAllCommand, because when the HSDataLogFragment will start...
                    // it triggers a get status command
                    _sendCommand(nodeId = nodeId, name = name, value = commandRequest)
                }
            } else {
                viewModelScope.launch {
                    _sendCommand(nodeId = nodeId, name = name, value = commandRequest)
                    sendGetStatusComponentInfoCommand(name = name, nodeId = nodeId)
                }
            }
        }
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        viewModelScope.launch {
            sendCommand(
                nodeId = nodeId,
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

            delay(500)

            //For Avoiding to change again the Acquisition name
            //setName(nodeId)

            sendCommand(
                nodeId = nodeId,
                cmd = PnPLCmd.START_LOG,
                askTheStatus = false
            )

            delay(500)

            sendGetLogControllerCommand(nodeId = nodeId)
            //sendGetAllCommand(nodeId)
        }
    }

    fun stopLog(nodeId: String) {
        viewModelScope.launch {
            sendCommand(
                nodeId = nodeId,
                cmd = PnPLCmd.STOP_LOG,
                askTheStatus = false
            )

            shouldInitDemo = true

            delay(500)

            sendGetLogControllerCommand(nodeId = nodeId)
        }

    }

    fun startDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        observeNodeStatusJob?.cancel()

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

            features = if (pnplFeature == null) emptyList() else listOf(pnplFeature!!)

            observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
                features = features,
                onFeaturesEnabled = { onFeaturesEnabled(nodeId = nodeId) })
                .flowOn(context = Dispatchers.IO).map { it.data }.onEach { data ->
                    if (data is PnPLConfig) {
                        handleStatusUpdate(data = data)

                        initDemo(nodeId = nodeId)

                        updateUIStatus(data = data)

                        getModel(nodeId = nodeId)
                    }

                }.launchIn(scope = viewModelScope)
        }
    }

    fun stopDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        observeNodeStatusJob?.cancel()

        _componentStatusUpdates.update { emptyList() }

        shouldInitDemo = true

        //Not optimal... but in this way... I am able to see the get status if demo is customized
        runBlocking {
            blueManager.disableFeatures(nodeId = nodeId, features = features)

            _sensors.update { emptyList() }

            pnplFeature = null
            features = emptyList()
        }
    }

    fun refresh(nodeId: String) {
        viewModelScope.launch {
            sendGetAllCommand(nodeId = nodeId)
        }
    }

    companion object {
        const val STREAM_JSON_KEY = "st_ble_stream"
        const val LOG_STATUS_JSON_KEY = "log_status"
        const val SD_JSON_KEY = "sd_mounted"
        const val LOG_CONTROLLER_JSON_KEY = "log_controller"
        const val PNPL_RESPONSE_JSON_KEY = "PnPL_Response"

        private const val ACQUISITION_INFO_JSON_KEY = "acquisition_info"
        private const val DESC_JSON_KEY = "description"
        private const val NAME_JSON_KEY = "name"

        private const val TAG = "SimpleHighSpeedDataLogViewModel"
    }
}