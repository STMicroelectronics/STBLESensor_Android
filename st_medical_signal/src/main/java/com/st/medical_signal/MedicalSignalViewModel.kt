/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.medical_signal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.medical_signal.MedicalInfo
import com.st.blue_sdk.features.extended.medical_signal.MedicalPrecision
import com.st.blue_sdk.features.extended.medical_signal.MedicalSignal16BitFeature
import com.st.blue_sdk.features.extended.medical_signal.MedicalSignal24BitFeature
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.lang.StringBuilder
import javax.inject.Inject

@HiltViewModel
class MedicalSignalViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var medical16Feature: Feature<*>? = null
    private var medical24Feature: Feature<*>? = null

    private var observeFeatureJob16: Job? = null
    private var observeFeatureJob24: Job? = null
    private var observeFeaturePnPLJob: Job? = null

    private var TAG = "MedicalSignalViewModel"

    var isMed16Streaming: Boolean = false
    var isMed24Streaming: Boolean = false

    private val _dataFeature16 = MutableSharedFlow<MedicalInfo?>()
    val dataFeature16: Flow<MedicalInfo?>
        get() = _dataFeature16

    private val _dataFeature24 = MutableSharedFlow<MedicalInfo?>()
    val dataFeature24: Flow<MedicalInfo?>
        get() = _dataFeature24


    private val _modelUpdates =
        mutableStateOf<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>(
            emptyList()
        )
    val modelUpdates: State<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>
        get() = _modelUpdates

    private val _componentStatusUpdates = mutableStateOf<List<JsonObject>>(emptyList())
    val componentStatusUpdates: State<List<JsonObject>>
        get() = _componentStatusUpdates

    private val _enableCollapse = mutableStateOf(value = false)
    val enableCollapse: State<Boolean>
        get() = _enableCollapse

    private val _lastStatusUpdatedAt = mutableLongStateOf(value = 0L)
    val lastStatusUpdatedAt: State<Long>
        get() = _lastStatusUpdatedAt

    private val rawPnPLFormat: MutableList<RawStreamIdEntry> = mutableListOf()

    private val _syntheticData = MutableSharedFlow<String>()
    val syntheticData: Flow<String>
        get() = _syntheticData


    fun startMed16(nodeId: String) {
        isMed16Streaming = true
        observeFeatureJob16?.cancel()
        medical16Feature?.let { feature ->
            observeFeatureJob16 = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = listOf(feature)
            ).flowOn(Dispatchers.IO).onEach { featureUpdate ->
                val dataFeature = featureUpdate.data
                if (dataFeature is MedicalInfo) {
                    if ((dataFeature.sigType.value.precision == MedicalPrecision.BIT16) ||
                        (dataFeature.sigType.value.precision == MedicalPrecision.UBIT16)
                    ) {
                        _dataFeature16.emit(dataFeature)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun stopMed16(nodeId: String) {
        isMed16Streaming = false
        observeFeatureJob16?.cancel()
        medical16Feature?.let { feature ->
            coroutineScope.launch {
                blueManager.disableFeatures(
                    nodeId = nodeId, features = listOf(feature)
                )
            }
        }
    }

    fun startMed24(nodeId: String) {
        isMed24Streaming = true
        observeFeatureJob24?.cancel()
        medical24Feature?.let { feature ->
            observeFeatureJob24 = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = listOf(feature)
            ).flowOn(Dispatchers.IO).onEach { featureUpdate ->
                val dataFeature = featureUpdate.data
                if (dataFeature is MedicalInfo) {
                    if ((dataFeature.sigType.value.precision == MedicalPrecision.BIT24) ||
                        (dataFeature.sigType.value.precision == MedicalPrecision.UBIT24)
                    ) {
                        _dataFeature24.emit(dataFeature)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun stopMed24(nodeId: String) {
        isMed24Streaming = false
        observeFeatureJob24?.cancel()
        medical24Feature?.let { feature ->
            coroutineScope.launch {
                blueManager.disableFeatures(
                    nodeId = nodeId, features = listOf(feature)
                )
            }
        }
    }

    fun startDemo(nodeId: String) {
        isMed16Streaming = false
        isMed24Streaming = false

        observeFeaturePnPLJob?.cancel()

        if (medical16Feature == null) {
            val feature = blueManager.nodeFeatures(nodeId = nodeId)
                .firstOrNull { it.name == MedicalSignal16BitFeature.NAME }
            feature?.let { f ->
                medical16Feature = f
                startMed16(nodeId = nodeId)
            }
        }

        if (medical24Feature == null) {
            val feature = blueManager.nodeFeatures(nodeId = nodeId)
                .firstOrNull { it.name == MedicalSignal24BitFeature.NAME }
            feature?.let { f ->
                medical24Feature = f
                startMed24(nodeId = nodeId)
            }
        }

        //Search if there are PnPL and the RawControlled Feature
        blueManager.nodeFeatures(nodeId)
            .filter { it.name == PnPL.NAME || it.name == RawControlled.NAME }.let { feature ->

                observeFeaturePnPLJob = blueManager.getFeatureUpdates(
                    nodeId = nodeId,
                    features = feature,
                    onFeaturesEnabled = {
                        launch {

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
                                node.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength
                            maxWriteLength?.let {
                                if (maxWriteLength!! > (node.maxPayloadSize)) {
                                    maxWriteLength = (node.maxPayloadSize)
                                }
                                (featurePnPL as PnPL).setMaxPayLoadSize(maxWriteLength!!)
                            }

                            if (featurePnPL is PnPL) {
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
                    }
                ).flowOn(Dispatchers.IO).onEach { featureUpdate ->

                    val data = featureUpdate.data

                    if (data is PnPLConfig) {
                        data.deviceStatus.value?.components?.let { json ->
                            _lastStatusUpdatedAt.longValue = System.currentTimeMillis()
                            _componentStatusUpdates.value = json

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
                                var count = 1
                                foundStream.formats.forEach { formatRawPnpLEntry ->

                                    if (formatRawPnpLEntry.format.enable) {
                                        if (formatRawPnpLEntry.displayName != null) {
                                            string.append("${formatRawPnpLEntry.displayName}: ")
                                        } else {
                                            string.append("${formatRawPnpLEntry.name}: ")
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
                                        string.append("${output.name}: ")
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
                        _syntheticData.emit(string.toString())
                    }
                }.launchIn(viewModelScope)
            }
    }

    fun stopDemo(nodeId: String) {
        stopMed16(nodeId = nodeId)
        stopMed24(nodeId = nodeId)

        observeFeaturePnPLJob?.let { job ->
            //If we are looking PnPL and the RawControlled Feature

            job.cancel()

            coroutineScope.launch {
                val features = blueManager.nodeFeatures(nodeId)
                    .filter { it.name == RawControlled.NAME || it.name == PnPL.NAME }

                blueManager.disableFeatures(
                    nodeId = nodeId,
                    features = features
                )
            }
        }
    }
}
