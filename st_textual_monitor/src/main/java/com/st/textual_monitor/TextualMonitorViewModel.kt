/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.textual_monitor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.BleCharacteristic
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.utils.NumberConversion
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureUpdate
import com.st.blue_sdk.board_catalog.models.Field
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlled
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlledInfo
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.decodeRawPnPLData
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.model.RawPnPLStreamIdEntry
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.readRawPnPLFormat
import com.st.blue_sdk.features.general_purpose.GeneralPurposeInfo
import com.st.preferences.StPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import javax.inject.Inject

@HiltViewModel
class TextualMonitorViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val stPreferences: StPreferences,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    var feature: Feature<*>? = null

    private var featuresPnPL: List<Feature<*>> = listOf()

    private var observeFeatureJob: Job? = null

    private var currentDesc: BleCharacteristic? = null

    private val _dataFeature = MutableSharedFlow<String>()
    val dataFeature: Flow<String>
        get() = _dataFeature

    private val _debugMessages = MutableStateFlow<String?>(null)
    val debugMessages: StateFlow<String?> = _debugMessages.asStateFlow()

    private val _modelUpdates =
        mutableStateOf<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>(
            emptyList()
        )
    val modelUpdates: State<List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>>
        get() = _modelUpdates

    private val _componentStatusUpdates = mutableStateOf<List<JsonObject>>(emptyList())
    val componentStatusUpdates: State<List<JsonObject>>
        get() = _componentStatusUpdates

    private val rawPnPLFormat: MutableList<RawPnPLStreamIdEntry> = mutableListOf()

    fun getNodeFwModel(nodeId: String): BoardFirmware? {
        var model: BoardFirmware?
        runBlocking {
            model = blueManager.getBoardFirmware(nodeId)
        }
        return model
    }

    fun getNodeFeatures(nodeId: String): List<Feature<*>> {
        return blueManager.nodeFeatures(nodeId)
    }

    fun setSelectedFeature(selectedFeature: Feature<*>, desc: BleCharacteristic?) {
        feature = selectedFeature
        currentDesc = desc
    }

    fun startDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        feature?.let {
            observeFeatureJob = viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    if (feature!!.type != Feature.Type.GENERAL_PURPOSE) {
                        if(feature!!.name != RawPnPLControlled.NAME) {
                            val data = it.data
                            _dataFeature.emit("\nTS =${it.timeStamp}:\n")
                            _dataFeature.emit(data.toString())
                        } else {
                            val data = it.data
                            _dataFeature.emit("\nTS =${it.timeStamp}:\n")
                            _dataFeature.emit(data.toString())
                        }
                    } else {
                        _dataFeature.emit("\nTS =${it.timeStamp}:\n")
                        val featureDataString = generalPurposeFeatureDataString(it)
                        _dataFeature.emit(featureDataString)
                    }
                }
            }
        }
    }

    private fun generalPurposeFeatureDataString(it: FeatureUpdate<*>): String {
        return if (currentDesc != null) {
            if (currentDesc!!.formatNotify != null) {
                var offset = 0
                val sampleValues = StringBuilder()

                for (field in currentDesc!!.formatNotify!!) {

                    //we Skip the timestamp
                    if (field.name != "timestamp") {
                        val data = (it.data as GeneralPurposeInfo).data.map { value -> value.value}.toByteArray()
                        var value: Float? = null
                        when (field.type) {
                            Field.Type.Float -> {
                                value =
                                    NumberConversion.LittleEndian.bytesToFloat(data, offset)
                            }
                            Field.Type.Int64 -> {
                                sampleValues.append(" Int64 not supported.. skip sample\n")
                            }
                            Field.Type.UInt32 -> {
                                value = NumberConversion.LittleEndian.bytesToUInt32(
                                    data,
                                    offset
                                ).toFloat()
                            }
                            Field.Type.Int32 -> {
                                value =
                                    NumberConversion.LittleEndian.bytesToInt32(data, offset)
                                        .toFloat()
                            }
                            Field.Type.UInt16 -> {
                                value = NumberConversion.LittleEndian.bytesToUInt16(
                                    data,
                                    offset
                                ).toFloat()
                            }
                            Field.Type.Int16 -> {
                                value =
                                    NumberConversion.LittleEndian.bytesToInt16(data, offset)
                                        .toFloat()
                            }
                            Field.Type.UInt8 -> {
                                value = NumberConversion.byteToUInt8(data, offset).toFloat()
                            }
                            Field.Type.Int8 -> {
                                value = data[offset].toFloat()

                            }
                            Field.Type.ByteArray -> {
                                sampleValues.append(" ByteArray not supported.. skip sample\n")
                            }
                            null -> {
                                sampleValues.append(" type not present.. skip sample\n")
                            }
                            else -> {
                                sampleValues.append(" type not supported.. skip sample\n")
                            }
                        }
                        if (value != null) {
                            if(field.scalefactor!=null) {
                                value *= field.scalefactor!!
                            }

                            if(field.offset!=null) {
                                value += field.offset!!
                            }

                            sampleValues.append(" ${field.name} = $value")

                            field.unit?.let { sampleValues.append(" [${field.unit}]")}

                            if((field.min!=null) || (field.max!=null)){
                                sampleValues.append(" <")
                                field.min?.let { sampleValues.append("${field.min}") }

                                sampleValues.append("...")

                                field.max?.let { sampleValues.append("${field.max}") }

                                sampleValues.append(">")
                            }

                            sampleValues.append("\n")
                        }
                        //Move to next sample
                        if(field.length!=null) {
                            offset += field.length!!
                        }
                    }
                }



                sampleValues.toString()
            } else {
                "Sample raw data size=${it.rawData.size}\n"
            }
        } else {
            it.data.toString()
        }
    }

    fun stopDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        feature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
            feature = null
        }
    }

    fun startReceiveDebugMessage(nodeId: String) {
        viewModelScope.launch {
            blueManager.getDebugMessages(nodeId = nodeId)?.collect {
                val message = it.payload
                _debugMessages.value = message
            }
        }
    }

    fun stopReceiveDebugMessage() {
        _debugMessages.value = null
    }

    fun startRawPnPLDemo(nodeId: String) {
        observeFeatureJob?.cancel()

        featuresPnPL = blueManager.nodeFeatures(nodeId)
            .filter { it.name == PnPL.NAME || it.name == RawPnPLControlled.NAME }

        if(featuresPnPL.isNotEmpty()) {
            observeFeatureJob = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = featuresPnPL,
                onFeaturesEnabled = {
                    launch {
                        _dataFeature.emit("Status Requested\n")
                        _modelUpdates.value = blueManager.getDtmiModel(
                            nodeId = nodeId,
                            isBeta = stPreferences.isBetaApplication()
                        )?.filterComponentsByProperty(propName = RawPnPLControlled.PROPERTY_NAME_ST_BLE_STREAM)
                            ?: emptyList()

                        val featurePnPL =
                            blueManager.nodeFeatures(nodeId).find { it.name == PnPL.NAME }

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
                        _componentStatusUpdates.value = json

                        //Search the RawPnPL Format
                        readRawPnPLFormat(
                            rawPnPLFormat = rawPnPLFormat,
                            json = json,
                            modelUpdates = _modelUpdates.value
                        )
                    }
                } else if (data is RawPnPLControlledInfo) {

                    val string = java.lang.StringBuilder()
                    //string.append("\nTS =${featureUpdate.timeStamp}:\n")
                    //string.append(data.toString())

                    //Search the StreamID and decode the data
                    val streamId =
                        decodeRawPnPLData(data = data.data, rawPnPLFormat = rawPnPLFormat)

                    //Print out the data decoded
                    if (streamId != RawPnPLControlled.STREAM_ID_NOT_FOUND) {
                        val foundStream = rawPnPLFormat.firstOrNull { it.streamId == streamId }

                        foundStream?.let {
                            string.append("\nStreamID= $streamId\n")
                            var count = 1
                            foundStream.formats.forEach { formatRawPnpLEntry ->

                                if (formatRawPnpLEntry.format.enable) {
                                    if (formatRawPnpLEntry.displayName != null) {
                                        string.append("\t$count) ${formatRawPnpLEntry.displayName}: ")
                                    } else {
                                        string.append("\t$count) ${formatRawPnpLEntry.name}: ")
                                    }
                                    count++

                                    string.append("[ ")

                                    formatRawPnpLEntry.format.values.forEach { value ->
                                        string.append("$value ")
                                    }

                                    string.append("] ")

                                    string.append(formatRawPnpLEntry.format.unit)

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
                                    string.append("\t$count) ${output.name}: ")
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

    fun stopRawPnPLDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        _componentStatusUpdates.value = emptyList()
        runBlocking {
            blueManager.disableFeatures(
                nodeId = nodeId,
                features = featuresPnPL
            )
        }
        featuresPnPL = emptyList()
        feature = null
    }
}
