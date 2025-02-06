/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.medical_signal

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    private val _isMed16Streaming =
        MutableStateFlow<Boolean>(false)
    val isMed16Streaming: StateFlow<Boolean>
        get() = _isMed16Streaming.asStateFlow()

    private val _isMed24Streaming =
        MutableStateFlow<Boolean>(false)
    val isMed24Streaming: StateFlow<Boolean>
        get() = _isMed24Streaming.asStateFlow()


    val dataFeature16 = mutableListOf<MedicalInfo>()
    val dataFeature24 = mutableListOf<MedicalInfo>()


    private val _dataFeature16Time =
        MutableStateFlow<Int>(0)
    val dataFeature16Time: StateFlow<Int>
        get() = _dataFeature16Time.asStateFlow()

    private val _dataFeature24Time =
        MutableStateFlow<Int>(0)
    val dataFeature24Time: StateFlow<Int>
        get() = _dataFeature24Time.asStateFlow()

//    private val _dataFeature16 =
//        MutableStateFlow<Pair<Int,MedicalInfo?>>(Pair(0,null))
//    val dataFeature16: StateFlow<Pair<Int,MedicalInfo?>>
//        get() = _dataFeature16.asStateFlow()
//
//    private val _dataFeature24 =
//        MutableStateFlow<Pair<Int,MedicalInfo?>>(Pair(0,null))
//    val dataFeature24: StateFlow<Pair<Int,MedicalInfo?>>
//        get() = _dataFeature24.asStateFlow()

    private var modelUpdates: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>> =
        emptyList()

    private val rawPnPLFormat: MutableList<RawStreamIdEntry> = mutableListOf()

    private val _syntheticData = MutableStateFlow<String?>(null)
    val syntheticData: StateFlow<String?>
        get() = _syntheticData

    fun startMed16(nodeId: String) {

        observeFeatureJob16?.cancel()
        medical16Feature?.let { feature ->

            viewModelScope.launch {
                _isMed16Streaming.emit(true)
            }

            observeFeatureJob16 = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = listOf(feature)
            ).flowOn(Dispatchers.IO).onEach { featureUpdate ->
                val dataFeature = featureUpdate.data
                if (dataFeature is MedicalInfo) {
                    if ((dataFeature.sigType.value.precision == MedicalPrecision.BIT16) ||
                        (dataFeature.sigType.value.precision == MedicalPrecision.UBIT16)
                    ) {
                        dataFeature16.add(dataFeature)
                        _dataFeature16Time.emit(dataFeature.internalTimeStamp.value)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun stopMed16(nodeId: String) {
        observeFeatureJob16?.cancel()
        medical16Feature?.let { feature ->
            coroutineScope.launch {
                _isMed16Streaming.emit(false)
                blueManager.disableFeatures(
                    nodeId = nodeId, features = listOf(feature)
                )
            }
        }
    }

    fun startMed24(nodeId: String) {
        observeFeatureJob24?.cancel()
        medical24Feature?.let { feature ->

            viewModelScope.launch {
                _isMed24Streaming.emit(true)
            }

            observeFeatureJob24 = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = listOf(feature)
            ).flowOn(Dispatchers.IO).onEach { featureUpdate ->
                val dataFeature = featureUpdate.data
                if (dataFeature is MedicalInfo) {
                    if ((dataFeature.sigType.value.precision == MedicalPrecision.BIT24) ||
                        (dataFeature.sigType.value.precision == MedicalPrecision.UBIT24)
                    ) {
                        dataFeature24.add(dataFeature)
                        _dataFeature24Time.emit(dataFeature.internalTimeStamp.value)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }

    fun stopMed24(nodeId: String) {
        observeFeatureJob24?.cancel()
        medical24Feature?.let { feature ->
            coroutineScope.launch {
                _isMed24Streaming.emit(false)
                blueManager.disableFeatures(
                    nodeId = nodeId, features = listOf(feature)
                )
            }
        }
    }

    fun startDemo(nodeId: String) {

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

                            modelUpdates = blueManager.getDtmiModel(
                                nodeId = nodeId,
                                isBeta = stPreferences.isBetaApplication()
                            )?.filterComponentsByProperty(propName = PROPERTY_NAME_ST_BLE_STREAM)
                                ?: emptyList()

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

                            //Search the RawPnPL Format
                            readRawPnPLFormat(
                                rawPnPLFormat = rawPnPLFormat,
                                json = json,
                                modelUpdates = modelUpdates
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

                                        if (formatRawPnpLEntry.format.channels != 1) {
                                            //The channels are interleaved
                                            string.append("[ ")
                                            if (formatRawPnpLEntry.format.multiplyFactor != null) {
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

                                            if (formatRawPnpLEntry.format.multiplyFactor != null) {
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
        medical16Feature = null
        stopMed24(nodeId = nodeId)
        medical24Feature = null

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
