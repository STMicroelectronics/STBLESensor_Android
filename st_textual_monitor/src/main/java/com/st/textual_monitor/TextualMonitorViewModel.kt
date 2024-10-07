/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.textual_monitor

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
import com.st.blue_sdk.features.extended.raw_controlled.RawControlled
import com.st.blue_sdk.features.extended.raw_controlled.RawControlledInfo
import com.st.blue_sdk.features.extended.raw_controlled.decodeRawData
import com.st.blue_sdk.features.extended.raw_controlled.model.RawStreamIdEntry
import com.st.blue_sdk.features.extended.raw_controlled.readRawPnPLFormat
import com.st.blue_sdk.features.general_purpose.GeneralPurposeInfo
import com.st.preferences.StPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var dataValues: MutableList<String> = mutableListOf()

    private val _featureTime: MutableStateFlow<Long?> =
        MutableStateFlow(
            null
        )
    val featureTime: StateFlow<Long?>
        get() = _featureTime.asStateFlow()

    private var modelUpdates: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>> =
        emptyList()

    private var componentStatusUpdates: List<JsonObject> = emptyList()

    private val rawPnPLFormat: MutableList<RawStreamIdEntry> = mutableListOf()

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

    private fun getNodeDataNotifyFeatures(nodeId: String): List<Feature<*>> {
        return blueManager.nodeFeatures(nodeId).filter { it.isDataNotifyFeature }
    }

    fun getNodeFeatureList(nodeId: String): List<GenericTextualFeature> {
        val features = getNodeDataNotifyFeatures(nodeId)
        //retrieve the Fw model
        val fwModel = getNodeFwModel(nodeId)

        return features.map {
            retrieveBleCharDescription(it, fwModel)
        }
    }

    fun setSelectedFeature(selectedFeature: Feature<*>, desc: BleCharacteristic?) {
        feature = selectedFeature
        currentDesc = desc
    }

    private suspend fun addToListAndEmit(value: String, time: Long) {
        dataValues.add(0, value)
        dataValues.add(0, "\nTS =$time:\n")
        dataValues = dataValues.take(24).toMutableList()
        _featureTime.emit(time)
    }

    fun startDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        dataValues.clear()
        feature?.let {
            observeFeatureJob = viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    if (feature!!.type != Feature.Type.GENERAL_PURPOSE) {
                        if (feature!!.name != RawControlled.NAME) {
                            val data = it.data
                            addToListAndEmit(data.toString(), it.timeStamp)
                        } else {
                            val data = it.data
                            addToListAndEmit(data.toString(), it.timeStamp)
                        }
                    } else {
                        val featureDataString = generalPurposeFeatureDataString(it)
                        addToListAndEmit(featureDataString, it.timeStamp)
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
                        val data = (it.data as GeneralPurposeInfo).data.map { value -> value.value }
                            .toByteArray()
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
                            if (field.scalefactor != null) {
                                value *= field.scalefactor!!
                            }

                            if (field.offset != null) {
                                value += field.offset!!
                            }

                            sampleValues.append(" ${field.name} = $value")

                            field.unit?.let { sampleValues.append(" [${field.unit}]") }

                            if ((field.min != null) || (field.max != null)) {
                                sampleValues.append(" <")
                                field.min?.let { sampleValues.append("${field.min}") }

                                sampleValues.append("...")

                                field.max?.let { sampleValues.append("${field.max}") }

                                sampleValues.append(">")
                            }

                            sampleValues.append("\n")
                        }
                        //Move to next sample
                        if (field.length != null) {
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

    fun startRawPnPLDemo(nodeId: String) {
        observeFeatureJob?.cancel()

        featuresPnPL = blueManager.nodeFeatures(nodeId)
            .filter { it.name == PnPL.NAME || it.name == RawControlled.NAME }

        if (featuresPnPL.isNotEmpty()) {
            observeFeatureJob = blueManager.getFeatureUpdates(
                nodeId = nodeId,
                features = featuresPnPL,
                onFeaturesEnabled = {
                    launch {
                        modelUpdates = blueManager.getDtmiModel(
                            nodeId = nodeId,
                            isBeta = stPreferences.isBetaApplication()
                        )
                            ?.filterComponentsByProperty(propName = RawControlled.PROPERTY_NAME_ST_BLE_STREAM)
                            ?: emptyList()

                        val featurePnPL =
                            blueManager.nodeFeatures(nodeId).find { it.name == PnPL.NAME }

                        if (featurePnPL is PnPL) {

                            val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)
                            var maxWriteLength =
                                node.catalogInfo?.characteristics?.firstOrNull { it.name == PnPL.NAME }?.maxWriteLength
                            maxWriteLength?.let {

                                if (maxWriteLength!! > (node.maxPayloadSize)) {
                                    maxWriteLength = (node.maxPayloadSize)
                                }

                                featurePnPL.setMaxPayLoadSize(maxWriteLength!!)
                            }

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
                        componentStatusUpdates = json

                        //Search the RawPnPL Format
                        readRawPnPLFormat(
                            rawPnPLFormat = rawPnPLFormat,
                            json = json,
                            modelUpdates = modelUpdates
                        )
                    }
                } else if (data is RawControlledInfo) {

                    val string = java.lang.StringBuilder()
                    //string.append("\nTS =${featureUpdate.timeStamp}:\n")
                    //string.append(data.toString())

                    //Search the StreamID and decode the data
                    val streamId =
                        decodeRawData(data = data.data, rawFormat = rawPnPLFormat)

                    //Print out the data decoded
                    if (streamId != RawControlled.STREAM_ID_NOT_FOUND) {
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
                    addToListAndEmit(string.toString(), featureUpdate.timeStamp)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun stopRawPnPLDemo(nodeId: String) {
        observeFeatureJob?.cancel()
        componentStatusUpdates = emptyList()
        runBlocking {
            blueManager.disableFeatures(
                nodeId = nodeId,
                features = featuresPnPL
            )
        }
        featuresPnPL = emptyList()
        feature = null
    }

    //retrieve the Feature Description from Fw Model if the feature is a General Purpose
    private fun retrieveBleCharDescription(
        it: Feature<*>,
        fwModel: BoardFirmware?
    ): GenericTextualFeature {
        var bleCharDesc: BleCharacteristic? = null
        val uuid = it.uuid
        if (it.type == Feature.Type.GENERAL_PURPOSE) {
            bleCharDesc = fwModel?.characteristics?.firstOrNull { it.uuid == uuid.toString() }
        }

        val name: String = if (bleCharDesc == null) {
            it.name
        } else {
            "GP " + bleCharDesc.name
        }
        val desc: String = bleCharDesc?.name ?: it.name
        return GenericTextualFeature(name, desc, it, bleCharDesc)
    }
}

