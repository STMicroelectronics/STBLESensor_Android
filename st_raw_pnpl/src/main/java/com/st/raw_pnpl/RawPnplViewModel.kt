/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.raw_pnpl

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled.Companion.PROPERTY_NAME_ST_BLE_STREAM
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled.Companion.STREAM_ID_NOT_FOUND
import com.st.blue_sdk.features.extended.raw_controlled.RawControlledInfo
import com.st.blue_sdk.features.extended.raw_controlled.decodeRawData
import com.st.blue_sdk.features.extended.raw_controlled.model.RawStreamIdEntry
import com.st.blue_sdk.features.extended.raw_controlled.readRawPnPLFormat
import com.st.preferences.StPreferences
import com.st.ui.composables.CommandRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import java.lang.StringBuilder
import javax.inject.Inject

@HiltViewModel
class RawPnplViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var observeFeaturePnPLJob: Job? = null

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

    private val _lastStatusUpdatedAt = mutableLongStateOf(value = 0L)
    val lastStatusUpdatedAt: State<Long>
        get() = _lastStatusUpdatedAt

    private val _dataFeature = MutableSharedFlow<String>()
    val dataFeature: Flow<String>
        get() = _dataFeature

    private val rawPnPLFormat: MutableList<RawStreamIdEntry> = mutableListOf()

    private fun sendGetComponentStatus(nodeId: String, compName: String) {

        viewModelScope.launch {
            _isLoading.value = true
            val feature =
                blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }
                    ?: return@launch

            if (feature is PnPL) {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(
                        feature = feature,
                        cmd = PnPLCmd(command = "get_status", request = compName)
                    )
                )
            }
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
                        nodeId = nodeId,
                        featureCommand = PnPLCommand(
                            feature = feature,
                            cmd = PnPLCmd(
                                component = name,
                                command = it.commandName,
                                fields = it.request
                            )
                        )
                    )

                    sendGetComponentStatus(nodeId = nodeId, compName = name)
                }
            }
        }
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

                    //sendGetComponentStatus(nodeId = nodeId, compName = name)
                    sendGetAllCommand(nodeId = nodeId)
                }
            }
        }
    }

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

    fun startDemo(nodeId: String) {
        observeFeaturePnPLJob?.cancel()

        blueManager.nodeFeatures(nodeId)
            .filter { it.name == PnPL.NAME || it.name == RawControlled.NAME }.let { feature ->

                observeFeaturePnPLJob = blueManager.getFeatureUpdates(
                    nodeId = nodeId,
                    features = feature,
                    onFeaturesEnabled = {
                        launch {
                            _isLoading.value = true

                            _modelUpdates.value = blueManager.getDtmiModel(
                                nodeId = nodeId,
                                isBeta = stPreferences.isBetaApplication()
                            )?.filterComponentsByProperty(propName = PROPERTY_NAME_ST_BLE_STREAM)
                                ?: emptyList()

                            _enableCollapse.value = true

                            val featurePnPL =
                                blueManager.nodeFeatures(nodeId).find { it.name == PnPL.NAME }

                            val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)
                            var maxWriteLength =
                                node?.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength
                                    ?: 20
                            node?.let {
                                if (maxWriteLength > (node.maxPayloadSize)) {
                                    maxWriteLength = (node.maxPayloadSize)
                                }
                            }
                            (featurePnPL as PnPL).setMaxPayLoadSize(maxWriteLength)

                                blueManager.writeFeatureCommand(
                                    responseTimeout = 0,
                                    nodeId = nodeId,
                                    featureCommand = PnPLCommand(
                                        feature = featurePnPL,
                                        cmd = PnPLCmd.ALL
                                    )
                                )
                            }
                        }
                ).flowOn(Dispatchers.IO).onEach { featureUpdate ->

                    val data = featureUpdate.data

                    if (data is PnPLConfig) {
                        data.deviceStatus.value?.components?.let { json ->
                            _lastStatusUpdatedAt.value = System.currentTimeMillis()
                            _componentStatusUpdates.value = json
                            _isLoading.value = false

                            //Search the RawPnPL Format
                            readRawPnPLFormat(
                                rawPnPLFormat = rawPnPLFormat,
                                json = json,
                                modelUpdates = _modelUpdates.value
                            )
                        }
                    } else if (data is RawControlledInfo) {

                        val string = StringBuilder()
                        //string.append("\nTS =${featureUpdate.timeStamp}:\n")
                        //string.append(data.toString())

                        //Search the StreamID and decode the data
                        val streamId =
                            decodeRawData(data = data.data, rawFormat = rawPnPLFormat)

                        //Print out the data decoded
                        if (streamId != STREAM_ID_NOT_FOUND) {
                            val foundStream = rawPnPLFormat.firstOrNull { it.streamId == streamId }

                            foundStream?.let {
                                string.append("StreamID= $streamId\n")
                                var count = 1
                                foundStream.formats.forEach { formatRawPnpLEntry ->

                                    if (formatRawPnpLEntry.format.enable) {
                                        if (formatRawPnpLEntry.displayName != null) {
                                            string.append("$count) ${formatRawPnpLEntry.displayName}: ")
                                        } else {
                                            string.append("$count) ${formatRawPnpLEntry.name}: ")
                                        }
                                        count++

                                        if(formatRawPnpLEntry.format.channels!= 1) {
                                            //The channels are interleaved
                                            string.append("[ ")
                                            if(formatRawPnpLEntry.format.multiplyFactor!=null) {
                                                val values =
                                                    formatRawPnpLEntry.format.valuesFloat.chunked(
                                                        formatRawPnpLEntry.format.channels
                                                    )
                                                values.forEach { channel ->
                                                    string.append("[ ")
                                                    channel.forEach { value ->
                                                        string.append("$value ")
                                                    }
                                                    string.append("] ")
                                                }
                                            } else {
                                                val values =
                                                    formatRawPnpLEntry.format.values.chunked(
                                                        formatRawPnpLEntry.format.channels
                                                    )
                                                values.forEach { channel ->
                                                    string.append("[ ")
                                                    channel.forEach { value ->
                                                        string.append("$value ")
                                                    }
                                                    string.append("] ")
                                                }
                                            }
                                            string.append("] ")
                                        } else {
                                            string.append("[ ")

                                            if(formatRawPnpLEntry.format.multiplyFactor!=null) {
                                                formatRawPnpLEntry.format.valuesFloat.forEach { value ->
                                                    string.append("$value ")
                                                }
                                            } else {
                                                formatRawPnpLEntry.format.values.forEach { value ->
                                                    string.append("$value ")
                                                }
                                            }

                                            string.append("] ")
                                        }

                                        formatRawPnpLEntry.format.unit?.let { unit ->
                                            string.append(unit)
                                        }

                                        if ((formatRawPnpLEntry.format.min != null) || (formatRawPnpLEntry.format.max != null))
                                            string.append(" {")

                                        formatRawPnpLEntry.format.min?.let { min ->
                                            string.append("min =$min ")
                                        }
                                        formatRawPnpLEntry.format.max?.let { max ->
                                            string.append("max =$max ")
                                        }

                                        if ((formatRawPnpLEntry.format.min != null) || (formatRawPnpLEntry.format.max != null))
                                            string.append("}")

                                        string.append("\n")

                                    }
                                }

                                foundStream.customFormat?.let { customFormat ->
                                    customFormat.output.forEach { output ->
                                        string.append("$count) ${output.name}: ")
                                        count++
                                        string.append("[ ")
                                        output.values.forEach { value ->
                                            string.append("$value ")
                                        }
                                        string.append("]\n")
                                    }
                                }
                            }
                        }
                        _dataFeature.emit(string.toString())
                    }
                }.launchIn(viewModelScope)
            }
    }


    fun stopDemo(nodeId: String) {
        observeFeaturePnPLJob?.cancel()

        _componentStatusUpdates.value = emptyList()

        runBlocking {
            val features = blueManager.nodeFeatures(nodeId)
                .filter { it.name == RawControlled.NAME || it.name == PnPL.NAME }
            blueManager.disableFeatures(
                nodeId = nodeId, features = features
            )
        }
    }
}
