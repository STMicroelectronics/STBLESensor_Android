/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.high_speed_data_log

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.board_catalog.models.DtmiType
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled.Companion.STREAM_ID_NOT_FOUND
import com.st.blue_sdk.features.extended.raw_controlled.RawControlledInfo
import com.st.blue_sdk.features.extended.raw_controlled.decodeRawData
import com.st.blue_sdk.features.extended.raw_controlled.model.RawStreamIdEntry
import com.st.blue_sdk.features.extended.raw_controlled.readRawPnPLFormat
import com.st.blue_sdk.models.NodeState
import com.st.core.GlobalConfig
import com.st.high_speed_data_log.model.StreamData
import com.st.high_speed_data_log.model.StreamDataChannel
import com.st.pnpl.composable.PnPLSpontaneousMessageType
import com.st.pnpl.composable.searchInfoWarningError
import com.st.pnpl.util.PnPLTypeOfCommand
import com.st.pnpl.util.SetCommandPnPLRequest
import com.st.preferences.StPreferences
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.ENABLE_PROPERTY_NAME
import com.st.ui.utils.localizedDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class AIoTCraftHighSpeedDataLogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences
) : ViewModel() {

    private var firstStopLogDone = HsdlConfig.isVespucci.not()
    private var firstDisableAllDone = false
    private var firstGetStatusAllDone = false
    private var enableStopDemo = true
    private var observeFeatureJob: Job? = null
    private var observeNodeStatusJob: Job? = null
    private var pnplFeature: PnPL? = null
    private var rawFeature: RawControlled? = null
    private var features: List<Feature<*>> = emptyList()
    private var shouldInitDemo: Boolean = true
    private var setShouldInitDemoAtResponse: Boolean = false
    private var shouldRenameTags: Boolean = true
    private var pnplBleResponses: Boolean = HsdlConfig.isVespucci
    private var streamDataBuffer = mutableListOf<StreamDataChannel>()
    private var commandQueue: MutableList<SetCommandPnPLRequest> = mutableListOf()
    private val rawPnPLFormat: MutableList<RawStreamIdEntry> = mutableListOf()
    private val tagNames: MutableList<String> = mutableListOf()
    private var sensorsActive = listOf<String>()
    private var componentWithInterface: List<ComponentWithInterface> = listOf()

    private val _uomKeys = MutableStateFlow<Map<String, String>>(value = emptyMap())
    private val _tags = MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _currentSensorEnabled: MutableStateFlow<String> = MutableStateFlow(value = "")
    private val _vespucciTagsActivation = MutableStateFlow<List<String>>(value = emptyList())
    private val _vespucciTags = MutableStateFlow<Map<String, Boolean>>(value = mutableMapOf())
    private val _acquisitionName = MutableStateFlow(value = "")
    private val _modelUpdates =
        MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _sensors = MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _streamSensors = MutableStateFlow<List<ComponentWithInterface>>(value = emptyList())
    private val _componentStatusUpdates = MutableStateFlow<List<JsonObject>>(value = emptyList())
    private val _streamDataBuffered = MutableStateFlow<StreamData?>(value = null)
    private val _streamData = MutableStateFlow<StreamData?>(value = null)
    private val _isLogging = MutableStateFlow(value = false)
    private val _isSDCardInserted = MutableStateFlow(value = false)
    private val _isLoading = MutableStateFlow(value = false)
    private val _isConnectionLost = MutableStateFlow(value = false)
    private val _enableLog = MutableStateFlow(value = false)
    private val _statusMessage: MutableStateFlow<PnPLSpontaneousMessageType?> =
        MutableStateFlow(value = null)

    val tags: StateFlow<List<ComponentWithInterface>> = _tags.asStateFlow()
    val currentSensorEnabled = _currentSensorEnabled.asStateFlow()
    val vespucciTags: StateFlow<Map<String, Boolean>> = _vespucciTags.asStateFlow()
    val acquisitionName: StateFlow<String> = _acquisitionName.asStateFlow()
    val streamData = _streamDataBuffered.asStateFlow()
    val sensors: StateFlow<List<ComponentWithInterface>> = _sensors.asStateFlow()
    val streamSensors: StateFlow<List<ComponentWithInterface>> = _streamSensors.asStateFlow()
    val componentStatusUpdates: StateFlow<List<JsonObject>> = _componentStatusUpdates.asStateFlow()
    val vespucciTagsActivation: StateFlow<List<String>> = _vespucciTagsActivation.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val isLogging = _isLogging.asStateFlow()
    val isSDCardInserted = _isSDCardInserted.asStateFlow()
    val statusMessage: StateFlow<PnPLSpontaneousMessageType?> = _statusMessage.asStateFlow()
    val isConnectionLost: StateFlow<Boolean> = _isConnectionLost.asStateFlow()
    val enableLog: StateFlow<Boolean> = _enableLog.asStateFlow()


//    private val _numActiveSensors = MutableStateFlow(value = 0)
//    val numActiveSensors = _numActiveSensors.asStateFlow()

//    var isBeta = false
//
//    init {
//        isBeta = stPreferences.isBetaApplication()
//    }

    private fun <T> MutableList<T>.removeFirstElements(count: Int): List<T> {
        val subList = this.subList(0, minOf(count, this.size)).toList()
        if (count in 0..this.size) {
            this.subList(0, count).clear()
        }
        return subList
    }

    private fun List<StreamDataChannel>.normalize(newSize: Int = 20): List<StreamDataChannel> {
        return (0..<newSize).map { i -> this[i * this.size / newSize] }
    }

    init {
        viewModelScope.launch {
            while (true) {
                val odr = _streamData.value?.odr ?: 1
                val plotSize = Math.ceil(odr / 10.toDouble()).roundToInt()
                val bufferSize = streamDataBuffer.size

                if (plotSize in 1..<bufferSize) {
                    Log.w(TAG, "Buffer size = $bufferSize")
                    _streamDataBuffered.update {
                        _streamData.value?.copy(
                            data = streamDataBuffer
                                .removeFirstElements(count = plotSize)
                                .normalize(newSize = 1)
                        )
                    }
                } else {
                    if (_currentSensorEnabled.value.isNotEmpty()) {
                        Log.w(TAG, "Buffering...")
                    }
                }

                delay(timeMillis = 100L)
            }
        }

        _streamData
            .onEach { data ->
                Log.w(TAG, "${data?.streamId} data = ${data?.data?.map { it.data }}")
                if (data == null) {
                    streamDataBuffer.clear()
                    _streamDataBuffered.update { null }
                } else {
                    streamDataBuffer.addAll(elements = data.data)
                }
            }
            .launchIn(scope = viewModelScope)

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

//                    _numActiveSensors.emit(sensorsEnabled.size)

                    _enableLog.update { sensorsEnabled.isNotEmpty() }
                }
        }

        viewModelScope.launch {
            isLogging.collect { value ->
                HsdlConfig.isLogging = value
                updateTags()
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

    private fun List<ComponentWithInterface>.streamSensorsFilter() = filter {
        it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR &&
                it.second.contents.find { it.name == "st_ble_stream" } != null &&
                it.first.name.startsWith(prefix = "iis3dwb", ignoreCase = true)
                    .not() // FIXME: With 2.3.0 not necessary
    }.map {
        Pair(
            it.first, it.second.copy(contents = it.second.contents.hsdl2SensorPropertyFilter())
        )
    }

    private fun List<ComponentWithInterface>.uom() = filter {
        it.first.contentType == DtmiContent.DtmiComponentContent.ContentType.SENSOR
    }.associate {
        val fsProp = it.second.contents.filterIsInstance<DtmiContent.DtmiPropertyContent>()
            .find { prop -> prop.name == "fs" }

        val localizedDisplayName = fsProp?.displayUnit?.localizedDisplayName

        val uom = if (localizedDisplayName.isNullOrEmpty().not()) {
            localizedDisplayName
        } else fsProp?.unit

        it.first.name to uom.toShortUom()
    }

    private fun String?.toShortUom() = when (this) {
        "gForce" -> "g"
        "hertz" -> "Hz"
        "gauss" -> "G"
        "degreeCelsius" -> "°C"
        "degreePerSecond" -> "dps"
        "Waveform" -> "dBSPL"
        null -> ""
        else -> this
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
            if (HsdlConfig.isVespucci) {
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
                        if (HsdlConfig.isVespucci) {
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
                            }.enableOrStatusPropertyMap().enablePropertyMap()
                                .hideDisabledTagWhenLoggingFilter()
                                .hideDisabledTagWhenConfigHasTag()
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
        _isLoading.update { true }

        componentWithInterface =
            blueManager.getDtmiModel(nodeId = nodeId, isBeta = stPreferences.isBetaApplication())
                ?.extractComponents(demoName = null) ?: emptyList()

        _sensors.update { componentWithInterface.hsdl2SensorsFilter() }

        _streamSensors.update { componentWithInterface.streamSensorsFilter() }

        _tags.update { componentWithInterface.hsdl2TagsFilter() }

        _uomKeys.update {
            componentWithInterface.uom()
        }

        _isLoading.update { false }
    }

    private fun updateTags() {
        _tags.update { componentWithInterface.hsdl2TagsFilter() }
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
            }
        }
    }

    private suspend fun sendCommand(
        nodeId: String,
        typeOfCmd: PnPLTypeOfCommand,
        cmd: PnPLCmd,
        askTheStatus: Boolean
    ) {
        if (pnplBleResponses) {
            addCommandToQueueAndCheckSend(
                nodeId = nodeId, newCommand = SetCommandPnPLRequest(
                    typeOfCommand = typeOfCmd,
                    pnpLCommand = cmd,
                    askTheStatus = askTheStatus
                )
            )
        } else {
            _isLoading.update { true }

            pnplFeature?.let {
                blueManager.writeFeatureCommand(
                    responseTimeout = 0, nodeId = nodeId, featureCommand = PnPLCommand(
                        feature = it, cmd = cmd
                    )
                )
            }

            if (askTheStatus) {
                cmd.component?.let { compName ->
                    sendGetStatusComponentInfoCommand(name = compName, nodeId = nodeId)
                }
            }
        }
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

//        if (name == "tags_info") {
//            sendCommand(
//                nodeId = nodeId,
//                typeOfCmd = PnPLTypeOfCommand.Status,
//                cmd = PnPLCmd.TAGS_INFO,
//                askTheStatus = false
//            )
//        } else {
//            sensorsActive.filter {
//                it.startsWith(sensorName)
//            }.forEach {
//                sendCommand(
//                    nodeId = nodeId,
//                    typeOfCmd = PnPLTypeOfCommand.Status,
//                    cmd = PnPLCmd(command = "get_status", request = it),
//                    askTheStatus = false
//                )
//            }
//        }

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

    private suspend fun sendSetNameCommand(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val nameFormatter = HsdlConfig.datalogNameFormat ?: "EEE MMM d yyyy HH:mm:ss"
        val sdf = SimpleDateFormat(nameFormatter, Locale.UK)
        val datetime = sdf.format(Date(timeInMillis))

        _acquisitionName.update { datetime }

        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Set,
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
                node.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength
            maxWriteLength?.let {
                if (maxWriteLength!! > (node.maxPayloadSize)) {
                    maxWriteLength = (node.maxPayloadSize)
                }
                pnplFeature?.setMaxPayLoadSize(maxWriteLength!!)
            }

            if (_sensors.value.isEmpty() || _tags.value.isEmpty()) {
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

    private fun handleRawControlledUpdate(data: RawControlledInfo) {
        val streamId = decodeRawData(data = data.data, rawFormat = rawPnPLFormat)

        if (streamId != STREAM_ID_NOT_FOUND) {
            rawPnPLFormat.firstOrNull { it.streamId == streamId }?.let { foundStream ->
                if (_currentSensorEnabled.value.isEmpty()) {
                    _currentSensorEnabled.update {
                        rawPnPLFormat.firstOrNull { it.streamId == streamId }?.componentName ?: ""
                    }
                }
                _streamData.update {
                    foundStream.formats.filter { it.format.enable }.map { rawEntry ->
                        StreamData(
                            streamId = streamId,
                            odr = rawEntry.format.odr ?: 1,
                            name = if (rawEntry.displayName != null) {
                                rawEntry.displayName ?: ""
                            } else {
                                rawEntry.name
                            },
                            uom = _uomKeys.value[_currentSensorEnabled.value]
                                ?: rawEntry.format.unit ?: "",
                            max = rawEntry.format.max,
                            min = rawEntry.format.min,
                            data = when {
                                rawEntry.format.channels != 1 && rawEntry.format.multiplyFactor != null ->
                                    rawEntry.format.valuesFloat.chunked(size = rawEntry.format.channels)
                                        .map { StreamDataChannel(data = it) }

                                rawEntry.format.channels != 1 && rawEntry.format.multiplyFactor == null ->
                                    rawEntry.format.values.chunked(size = rawEntry.format.channels)
                                        .filterIsInstance<List<Float>>()
                                        .map { StreamDataChannel(data = it) }

                                rawEntry.format.channels == 1 && rawEntry.format.multiplyFactor != null ->
                                    rawEntry.format.valuesFloat.map {
                                        StreamDataChannel(
                                            data = listOf(
                                                it
                                            )
                                        )
                                    }

                                rawEntry.format.channels == 1 && rawEntry.format.multiplyFactor == null ->
                                    rawEntry.format.values
                                        .filterIsInstance<Float>()
                                        .map { StreamDataChannel(data = listOf(it)) }

                                else -> emptyList()
                            }
                        )
                    }
                        .firstOrNull()
                }
            }
        }
    }

    private suspend fun sendRenameTagCommand(
        nodeId: String,
        index: Int,
        askTheStatus: Boolean = false
    ) {
        val newTagName = HsdlConfig.tags[index]
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Set,
            cmd = PnPLCmd(
                command = TAGS_INFO_JSON_KEY,
                request = tagNames[index],
                fields = mapOf(LABEL_JSON_KEY to newTagName)
            ),
            askTheStatus = askTheStatus
        )
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

    private suspend fun sendEnableTagCommand(
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
                fields = mapOf(ENABLED_JSON_KEY to true)
            ),
            askTheStatus = askTheStatus
        )
    }

    private suspend fun sendDisableAllSensorCommand(
        nodeId: String,
        askTheStatus: Boolean = false
    ) {
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Set,
            PnPLCmd(
                component = LOG_CONTROLLER_JSON_KEY,
                command = ENABLE_ALL_JSON_KEY,
                fields = mapOf(STATUS_JSON_KEY to false)
            ),
            askTheStatus = askTheStatus
        )
    }

    private fun PnPLSpontaneousMessageType?.addExtra(): PnPLSpontaneousMessageType? {
        if (HsdlConfig.isVespucci && this != null) {
            val shouldAddExtra = listOf("sd", "log").any { key ->
                this.message.contains(other = key, ignoreCase = true)
            }

            if (shouldAddExtra) {
                this.extraUrl = context.getString(R.string.st_hsdl_sdExtraUrl)
                this.extraMessage = context.getString(R.string.st_hsdl_sdExtraMessage)
            }
        }

        return this
    }

    private suspend fun handlePnplResponses(nodeId: String, data: PnPLConfig) {
        data.deviceStatus.value?.components?.let { json ->
            //Search the Spontaneous messages
            val message = searchInfoWarningError(json)

            message?.let {
                _statusMessage.update {
                    message.addExtra()
                }

                if (pnplBleResponses) {
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
                                }
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
                        Log.e(TAG, "handlePnplResponses")
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun initRawPnPLFormat(data: PnPLConfig) {
        data.deviceStatus.value?.let { status ->
            status.components.let { json ->
                if (json.find { it.containsKey(PNPL_RESPONSE_JSON_KEY) } == null) {
                    val hasSensorEnabled = _currentSensorEnabled.value.isEmpty().not()
                    val hasStatusForSensorEnabled =
                        json.find { it.containsKey(_currentSensorEnabled.value) } != null
                    if (hasSensorEnabled && hasStatusForSensorEnabled) {
                        readRawPnPLFormat(
                            rawPnPLFormat = rawPnPLFormat,
                            json = json,
                            modelUpdates = _modelUpdates.value
                        )
                    }
                }
            }
        }
    }

    private fun initPnPLBleResponse(data: PnPLConfig) {
        data.deviceStatus.value?.let { status ->
            status.pnplBleResponses?.let { value ->
                Log.d(TAG, "pnplBleResponses $value")

                pnplBleResponses = value
            }
        }
    }

    private suspend fun initDemo(nodeId: String) {
        if (shouldInitDemo) {
            shouldInitDemo = false
            if (_isLogging.value) {
                sendGetTagsInfoCommand(nodeId)
            } else {
                sendSetNameCommand(nodeId = nodeId)

                if (shouldRenameTags) {
                    shouldRenameTags = false
                    HsdlConfig.tags = HsdlConfig.tags.filter { it != "unlabeled" }
                    for (index in 0..<tagNames.size) {
                        if (index in 0..HsdlConfig.tags.lastIndex) {
                            sendRenameTagCommand(nodeId = nodeId, index = index)
                            sendEnableTagCommand(nodeId = nodeId, index = index)
                        } else {
                            sendDisableTagCommand(nodeId = nodeId, index = index)
                        }
                    }
                }
            }
        }

        if (_isLogging.value) {
            if (firstStopLogDone.not()) {
                firstStopLogDone = true

                stopLog(nodeId = nodeId)
            }
        } else {
            firstStopLogDone = true

            if (HsdlConfig.isVespucci && firstDisableAllDone.not()) {
                firstDisableAllDone = true
                sendDisableAllSensorCommand(nodeId = nodeId)
            } else {
                if (firstGetStatusAllDone.not()) {
                    firstGetStatusAllDone = true

                    sendGetAllCommand(nodeId = nodeId)
                }
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

    private fun handleTagsUpdate(data: PnPLConfig) {
        data.deviceStatus.value?.components?.let { json ->
            json.find { it.containsKey(TAGS_INFO_JSON_KEY) }
                ?.get(TAGS_INFO_JSON_KEY)?.jsonObject?.let { tags ->

                    val vespucciTagsMap = mutableMapOf<String, Boolean>()
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
                    _vespucciTags.update { vespucciTagsMap }
                    Log.d(TAG, "tags updateds ${_vespucciTags.value.size}")
                }
        }
    }

    private suspend fun sendEnableDisableStreamCommand(
        nodeId: String,
        sensor: String,
        enable: Boolean
    ) {
        sendCommand(
            nodeId = nodeId,
            typeOfCmd = PnPLTypeOfCommand.Set,
            cmd = PnPLCmd(
                command = sensor,
                request = STREAM_JSON_KEY,
                fields = mapOf(sensor.split("_").last() to mapOf(ENABLE_JSON_KEY to enable))
            ),
            askTheStatus = false
        )
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
            if (pnplBleResponses) {
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
            } else {
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
            _currentSensorEnabled.update { "" }
            _streamData.update { null }

            sendSetTimeCommand(nodeId = nodeId)

            //For Avoiding to change again the Acquisition name
            //setName(nodeId)

            sendCommand(
                nodeId = nodeId,
                typeOfCmd = PnPLTypeOfCommand.Log,
                cmd = PnPLCmd.START_LOG,
                askTheStatus = true
            )

            if (pnplBleResponses.not()) {
                sendGetLogControllerCommand(nodeId = nodeId)
            }
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

            if (pnplBleResponses) {
                setShouldInitDemoAtResponse = true
            } else {
                shouldInitDemo = true
            }

            if (_currentSensorEnabled.value.isNotEmpty()) {
                sendEnableDisableStreamCommand(
                    nodeId = nodeId,
                    sensor = _currentSensorEnabled.value,
                    enable = false
                )
                _currentSensorEnabled.update { "" }
                _streamData.update { null }
            }
        }

    }

    fun setEnableStopDemo(value: Boolean) {
        enableStopDemo = value
    }

    fun startDemo(nodeId: String) {
        if(enableStopDemo) {
            observeFeatureJob?.cancel()
            observeNodeStatusJob?.cancel()
            tagNames.clear()

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

                if (HsdlConfig.isVespucci) {
                    rawFeature =
                        blueManager.nodeFeatures(nodeId = nodeId)
                            .filter { it.name == RawControlled.NAME }
                            .filterIsInstance<RawControlled>().firstOrNull()
                }

                features = if (pnplFeature == null) emptyList() else listOf(pnplFeature!!) +
                        if (rawFeature == null) emptyList<Feature<*>>() else listOf(rawFeature!!)

                observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
                    features = features,
                    onFeaturesEnabled = { onFeaturesEnabled(nodeId = nodeId) })
                    .flowOn(context = Dispatchers.IO).map { it.data }.onEach { data ->
                        if (data is PnPLConfig) {
                            initRawPnPLFormat(data = data)

                            initPnPLBleResponse(data = data)

                            handleStatusUpdate(data = data)

                            initDemo(nodeId = nodeId)

                            updateUIStatus(data = data)

                            handleTagsUpdate(data = data)

                            getModel(nodeId = nodeId)

                            if (pnplBleResponses) {
                                handlePnplResponses(nodeId = nodeId, data = data)
                            }
                        }

                        if (data is RawControlledInfo) {
                            handleRawControlledUpdate(data = data)
                        }
                    }.launchIn(scope = viewModelScope)
            }
        } else {
            enableStopDemo = true
        }
    }

    fun stopDemo(nodeId: String) {
        if (enableStopDemo) {

           firstGetStatusAllDone = false

            observeFeatureJob?.cancel()
            observeNodeStatusJob?.cancel()

            _componentStatusUpdates.update { emptyList() }

            shouldInitDemo = true
            shouldRenameTags = true
            tagNames.clear()

            //Not optimal... but in this way... I am able to see the get status if demo is customized
            runBlocking {
                blueManager.disableFeatures(nodeId = nodeId, features = features)

                //pnplFeatures.clear()
                _sensors.update { emptyList() }
                _streamSensors.update { emptyList() }
                _tags.update { emptyList() }

                pnplFeature = null
                rawFeature = null
                features = emptyList()
            }
        } else {
            enableStopDemo = true
        }
    }

    fun refresh(nodeId: String) {
        viewModelScope.launch {
            sendGetAllCommand(nodeId = nodeId)
        }
    }

    fun onTagChangeState(nodeId: String, tag: String, newState: Boolean) {
        viewModelScope.launch {
            sendCommand(
                nodeId = nodeId,
                typeOfCmd = PnPLTypeOfCommand.Set,
                cmd = PnPLCmd(
                    command = TAGS_INFO_JSON_KEY,
                    request = tagNames[HsdlConfig.tags.indexOf(tag)],
                    fields = mapOf(STATUS_JSON_KEY to newState)
                ),
                askTheStatus = false
            )

            val oldTagsStatus = _vespucciTags.value.toMutableMap()
            oldTagsStatus[tag] = newState

            _vespucciTags.update { oldTagsStatus }

            if (newState) {
                _vespucciTagsActivation.update { it + tag }

                delay(timeMillis = 7000L)

                _vespucciTagsActivation.update { it - tag }
            }
        }
    }

    fun enableStreamSensor(nodeId: String, sensor: String) {
        viewModelScope.launch {
            if (_currentSensorEnabled.value == sensor) return@launch

            _streamData.update { null }

            if (_currentSensorEnabled.value.isNotEmpty()) {
                sendEnableDisableStreamCommand(
                    sensor = _currentSensorEnabled.value, nodeId = nodeId, enable = false
                )
            }

            _currentSensorEnabled.update { sensor }

            sendEnableDisableStreamCommand(
                sensor = _currentSensorEnabled.value,
                nodeId = nodeId,
                enable = true
            )

            sendGetStatusComponentInfoCommand(nodeId = nodeId, name = sensor)
        }
    }

    companion object {
        const val STREAM_JSON_KEY = "st_ble_stream"
        const val LOG_STATUS_JSON_KEY = "log_status"
        const val SD_JSON_KEY = "sd_mounted"
        const val LOG_CONTROLLER_JSON_KEY = "log_controller"
        const val PNPL_RESPONSE_JSON_KEY = "PnPL_Response"
        const val ENABLE_JSON_KEY = "enable"
        const val ENABLE_ALL_JSON_KEY = "enable_all"
        const val STATUS_JSON_KEY = "status"

        private const val TAGS_INFO_JSON_KEY = "tags_info"
        private const val ACQUISITION_INFO_JSON_KEY = "acquisition_info"
        private const val LABEL_JSON_KEY = "label"
        private const val ENABLED_JSON_KEY = "enabled"
        private const val DESC_JSON_KEY = "description"
        private const val NAME_JSON_KEY = "name"

        private const val TAG = "AIoTCraftHighSpeedDataLogViewModel"
    }
}