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
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlled
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlledInfo
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.decodeRawPnPLData
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.model.RawPnPLStreamIdEntry
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.readRawPnPLFormat
import com.st.preferences.StPreferences
import com.st.smart_motor_control.model.MotorControlFault
import com.st.smart_motor_control.model.MotorControlFault.Companion.getErrorCodeFromValue
import com.st.ui.composables.CommandRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    private val pnplFeatures: MutableList<Feature<*>> = mutableListOf()
    private var shouldInitDemo: Boolean = true
    private var shouldRenameTags: Boolean = true

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

    private val rawPnPLFormat: MutableList<RawPnPLStreamIdEntry> = mutableListOf()

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

    private val _isMotorRunning = MutableStateFlow(false)
    val isMotorRunning = _isMotorRunning.asStateFlow()

    private val _motorSpeed = MutableStateFlow(1024)
    val motorSpeed = _motorSpeed.asStateFlow()

    var temperatureUnit: String = "°C"
    var speedRefUnit: String = "rpm"
    var speedMeasUnit: String = "rpm"
    var busVoltageUnit: String = "Volt"

    private fun sensorPropNamePredicate(name: String): Boolean =
        name == "odr" || name == "fs" ||
                name == "enable" || name == "aop" ||
                name == "load_file" || name == "ucf_status"

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

    private suspend fun sendGetAllCommand(nodeId: String) {
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

    private suspend fun sendGetComponentStatus(nodeId: String, compName: String) {
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

    private suspend fun sendGetLogControllerCommand(nodeId: String) {
        _isLoading.value = true

        blueManager.writeFeatureCommand(
            responseTimeout = 0,

            nodeId = nodeId, featureCommand = PnPLCommand(
                feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                cmd = PnPLCmd.LOG_CONTROLLER
            )
        )
    }

    private suspend fun sendGetTagsInfoCommand(nodeId: String) {
        _isLoading.value = true

        blueManager.writeFeatureCommand(
            responseTimeout = 0,

            nodeId = nodeId, featureCommand = PnPLCommand(
                feature = pnplFeatures.filterIsInstance<PnPL>().first(),
                cmd = PnPLCmd.TAGS_INFO
            )
        )
    }

    private suspend fun setTime(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val sdf = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.ROOT)
        val datetime = sdf.format(Date(timeInMillis))

        _sendCommand(
            nodeId = nodeId, name = LOG_CONTROLLER_JSON_KEY,
            value = CommandRequest(
                commandName = "set_time",
                commandType = "",
                request = mapOf("datetime" to datetime)
            )
        )
    }

    private suspend fun setName(nodeId: String) {
        val calendar = Calendar.getInstance()
        val timeInMillis = calendar.timeInMillis
        val nameFormatter = MotorControlConfig.datalogNameFormat ?: "EEE MMM d yyyy HH:mm:ss"
        val sdf = SimpleDateFormat(nameFormatter, Locale.UK)
        val datetime = sdf.format(Date(timeInMillis))

        _acquisitionName.emit(datetime)

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

    fun startLog(nodeId: String) {
        viewModelScope.launch {
            setTime(nodeId)

            setName(nodeId)

            blueManager.writeFeatureCommand(
                responseTimeout = 0,

                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(), cmd = PnPLCmd.START_LOG
                )
            )

            sendGetLogControllerCommand(nodeId = nodeId)
        }
    }

    fun stopLog(nodeId: String) {
        viewModelScope.launch {
            blueManager.writeFeatureCommand(
                responseTimeout = 0,

                nodeId = nodeId,
                featureCommand = PnPLCommand(
                    feature = pnplFeatures.filterIsInstance<PnPL>().first(), cmd = PnPLCmd.STOP_LOG
                )
            )

            shouldInitDemo = true
            sendGetLogControllerCommand(nodeId = nodeId)
        }
    }

    fun sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        viewModelScope.launch {
            _sendCommand(nodeId, name, value)

            //sendGetAllCommand(nodeId)
            sendGetComponentStatus(nodeId = nodeId, compName = name)
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


    fun sendMotorControlCommand(nodeId: String, name: String, value: CommandRequest?) {
        viewModelScope.launch {
            _isLoading.value = true
            _sendCommand(nodeId, name, value)

            sendGetComponentStatus(nodeId = nodeId, compName = name)
        }
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        viewModelScope.launch {
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

                //sendGetAllCommand(nodeId)
                sendGetComponentStatus(nodeId = nodeId, compName = name)
            }
        }
    }

    fun startDemo(nodeId: String) {
        observeFeatureJob?.cancel()

        pnplFeatures.addAll(
            blueManager.nodeFeatures(nodeId = nodeId)
                .filter { it.name == PnPL.NAME || it.name == RawPnPLControlled.NAME }
        )

        observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
            features = pnplFeatures,
            onFeaturesEnabled = {
                viewModelScope.launch {
                    if (_sensorsActuators.value.isEmpty() || _tags.value.isEmpty()) {
                        getModel(nodeId = nodeId)

                        sendGetLogControllerCommand(nodeId)
                    }
                }
            }).flowOn(Dispatchers.IO).onEach { featureUpdate ->
            featureUpdate.data.let { data ->
                if (data is PnPLConfig) {
                    data.deviceStatus.value?.components?.let { json ->

                        //Search the RawPnPL Format
                        val rawPnPLFormatTmp: MutableList<RawPnPLStreamIdEntry> = mutableListOf()
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
                                    if(format.name== "temperature") {
                                        format.format.unit?.let { unit ->
                                            temperatureUnit = unit
                                        }
                                    }

                                    if(format.name== "ref_speed") {
                                        format.format.unit?.let { unit ->
                                            speedRefUnit = unit
                                        }
                                    }

                                    if(format.name== "speed") {
                                        format.format.unit?.let { unit ->
                                            speedMeasUnit = unit
                                        }
                                    }

                                    if(format.name== "bus_voltage") {
                                        format.format.unit?.let { unit ->
                                            busVoltageUnit = unit
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
                                _componentStatusUpdates.value = json

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

                                //delay(50L)

                                //Ask Slow telemetry status
                                sendGetComponentStatus(
                                    nodeId = nodeId,
                                    compName = SLOW_TELEMETRY_JSON_KEY
                                )

                            } else {
                                setName(nodeId = nodeId)
                                if (shouldRenameTags) {
                                    shouldRenameTags = false
                                    for (index in 0..4) {
                                        if (index in 0..MotorControlConfig.tags.lastIndex) {
                                            val tagName = MotorControlConfig.tags[index]
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
                                        } else {
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
                                            //}
                                        }
                                        //delay(50L)
                                    }
                                    //delay(100L)
                                }
                                sendGetAllCommand(nodeId)
                            }
                        }

                        getModel(nodeId = nodeId)
                    }
                } else if (data is RawPnPLControlledInfo) {

                    //Search the StreamID and decode the data
                    val streamId =
                        decodeRawPnPLData(data = data.data, rawPnPLFormat = rawPnPLFormat)

                    //Print out the data decoded
                    if (streamId != RawPnPLControlled.STREAM_ID_NOT_FOUND) {
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

                            if (value is Int?) {
                                _temperature.emit(value)
                            }

                            //Search Speed Reference
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "ref_speed" }?.format?.values?.firstOrNull()
                            if (value is Int?) {
                                _speedRef.emit(value)
                            }

                            //Search Speed Measurement
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "speed" }?.format?.values?.firstOrNull()
                            if (value is Int?) {
                                _speedMeas.emit(value)
                            }

                            //Search Bus voltage
                            value =
                                foundStream.formats.firstOrNull { entry -> entry.name == "bus_voltage" }?.format?.values?.first()
                            if (value is Int?) {
                                _busVoltage.emit(value)
                            }
                        }
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun stopDemo(nodeId: String) {
        observeFeatureJob?.cancel()

        _componentStatusUpdates.value = emptyList()
        shouldInitDemo = true
        shouldRenameTags = true

        coroutineScope.launch {
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
