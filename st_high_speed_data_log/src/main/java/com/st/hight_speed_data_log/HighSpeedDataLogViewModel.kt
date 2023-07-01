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
import com.st.ui.composables.CommandRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

private typealias ComponentWithInterface = Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>

@HiltViewModel
class HighSpeedDataLogViewModel @Inject constructor(
    private val blueManager: BlueManager, private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var observeFeatureJob: Job? = null
    private val pnplFeatures: MutableList<PnPL> = mutableListOf()
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

    private val _lastStatusUpdatedAt = MutableStateFlow(0L)
    val lastStatusUpdatedAt: StateFlow<Long>
        get() = _lastStatusUpdatedAt.asStateFlow()

    private val _isLogging = MutableStateFlow(false)
    val isLogging = _isLogging.asStateFlow()

    private val _isSDCardInserted = MutableStateFlow(false)
    val isSDCardInserted = _isSDCardInserted.asStateFlow()

    private fun sensorPropNamePredicate(name: String): Boolean =
        name == "odr" || name == "fs" ||
                name == "enable" || name == "aop" ||
                name == "load_file" || name == "ucf_status"

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
            blueManager.getDtmiModel(nodeId = nodeId)?.extractComponents(demoName = null)
                ?: emptyList()

        _sensors.value = componentWithInterface.hsdl2SensorsFilter()

        _tags.value = componentWithInterface.hsdl2TagsFilter()

        _isLoading.value = false
    }

    private suspend fun sendGetAllCommand(nodeId: String) {
        _isLoading.value = true

        blueManager.writeFeatureCommand(
            nodeId = nodeId,
            featureCommand = PnPLCommand(feature = pnplFeatures.first(), cmd = PnPLCmd.ALL)
        )
    }

    private suspend fun sendGetLogControllerCommand(nodeId: String) {
        _isLoading.value = true

        blueManager.writeFeatureCommand(
            nodeId = nodeId, featureCommand = PnPLCommand(
                feature = pnplFeatures.first(), cmd = PnPLCmd.LOG_CONTROLLER
            )
        )
    }

    private suspend fun sendGetTagsInfoCommand(nodeId: String) {
        _isLoading.value = true

        blueManager.writeFeatureCommand(
            nodeId = nodeId, featureCommand = PnPLCommand(
                feature = pnplFeatures.first(),
                cmd = PnPLCmd.TAGS_INFO
            )
        )
    }

    private suspend fun setTime(nodeId: String) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val timeInMillis = calendar.timeInMillis
        val sdf = SimpleDateFormat("yyyyMMdd_hh_mm_ss", Locale.ROOT)
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
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val timeInMillis = calendar.timeInMillis
        val sdf = SimpleDateFormat("EEE MMM d yyyy hh:mm:ss", Locale.UK)
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
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(), cmd = PnPLCmd.START_LOG
                )
            )

            sendGetLogControllerCommand(nodeId = nodeId)
        }
    }

    fun stopLog(nodeId: String) {
        viewModelScope.launch {
            blueManager.writeFeatureCommand(
                nodeId = nodeId,
                featureCommand = PnPLCommand(feature = pnplFeatures.first(), cmd = PnPLCmd.STOP_LOG)
            )

            shouldInitDemo = true
            sendGetLogControllerCommand(nodeId = nodeId)
        }
    }

    fun sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        viewModelScope.launch {
            _sendCommand(nodeId, name, value)

            sendGetAllCommand(nodeId)
        }
    }

    private suspend fun _sendCommand(nodeId: String, name: String, value: CommandRequest?) {
        value?.let {
            _isLoading.value = true

            blueManager.writeFeatureCommand(
                nodeId = nodeId, featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(), cmd = PnPLCmd(
                        component = name, command = it.commandName, fields = it.request
                    )
                )
            )
        }
    }

    fun sendChange(nodeId: String, name: String, value: Pair<String, Any>) {
        viewModelScope.launch {
            _isLoading.value = true

            value.let {
                val featureCommand = PnPLCommand(
                    feature = pnplFeatures.first(), cmd = PnPLCmd(
                        command = name, fields = mapOf(it)
                    )
                )

                blueManager.writeFeatureCommand(
                    nodeId = nodeId, featureCommand = featureCommand
                )

                sendGetAllCommand(nodeId)
            }
        }
    }

    fun startDemo(nodeId: String) {
        observeFeatureJob?.cancel()

        pnplFeatures.addAll(
            blueManager.nodeFeatures(nodeId = nodeId)
                .filter { it.name == PnPL.NAME }.filterIsInstance<PnPL>()
        )

        observeFeatureJob = blueManager.getFeatureUpdates(nodeId = nodeId,
            features = pnplFeatures,
            onFeaturesEnabled = {
                viewModelScope.launch {
                    if (_sensors.value.isEmpty() || _tags.value.isEmpty()) {
                        getModel(nodeId = nodeId)

                        sendGetLogControllerCommand(nodeId)
                    }
                }
            }).flowOn(Dispatchers.IO).onEach { featureUpdate ->
            featureUpdate.data.let { data ->
                if (data is PnPLConfig) {
                    data.deviceStatus.value?.components?.let { json ->

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

                        json.find { it.containsKey(TAGS_INFO_JSON_KEY) }
                            ?.get(TAGS_INFO_JSON_KEY)?.jsonObject?.let { tags ->
                                _lastStatusUpdatedAt.value = System.currentTimeMillis()
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
                                sendGetTagsInfoCommand(nodeId)
                            } else {
                                setName(nodeId = nodeId)
                                if (shouldRenameTags) {
                                    shouldRenameTags = false
                                    for (index in 0..4) {
                                        if (index in 0..HsdlConfig.tags.lastIndex) {
                                            val tagName = HsdlConfig.tags[index]
                                            val renameCommand = PnPLCommand(
                                                feature = pnplFeatures.first(),
                                                cmd = PnPLCmd(
                                                    command = TAGS_INFO_JSON_KEY,
                                                    request = "$TAG_JSON_KEY$index",
                                                    fields = mapOf(LABEL_JSON_KEY to tagName)
                                                )
                                            )

                                            blueManager.writeFeatureCommand(
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

                                            delay(50L)

                                            blueManager.writeFeatureCommand(
                                                nodeId = nodeId, featureCommand = enableCommand
                                            )
                                        } else {
                                            val disableCommand = PnPLCommand(
                                                feature = pnplFeatures.first(),
                                                cmd = PnPLCmd(
                                                    command = TAGS_INFO_JSON_KEY,
                                                    request = "$TAG_JSON_KEY$index",
                                                    fields = mapOf(ENABLED_JSON_KEY to false)
                                                )
                                            )

                                            blueManager.writeFeatureCommand(
                                                nodeId = nodeId, featureCommand = disableCommand
                                            )
                                        }

                                        delay(50L)
                                    }
                                    delay(100L)
                                }
                                sendGetAllCommand(nodeId)
                            }
                        }

                        getModel(nodeId = nodeId)
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
                feature = pnplFeatures.first(),
                cmd = PnPLCmd(
                    command = TAGS_INFO_JSON_KEY,
                    request = "$TAG_JSON_KEY$tag",
                    fields = mapOf(STATUS_JSON_KEY to newState)
                )
            )

            val oldTagsStatus = _vespucciTags.value.toMutableMap()
            oldTagsStatus[tag] = newState

            _vespucciTags.emit(oldTagsStatus)

            delay(50L)

            blueManager.writeFeatureCommand(
                nodeId = nodeId,
                featureCommand = enableCommand
            )
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
