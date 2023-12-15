/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.textual_monitor

import android.util.Log
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
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlled
import com.st.blue_sdk.features.extended.raw_pnpl_controlled.RawPnPLControlledInfo
import com.st.blue_sdk.features.general_purpose.GeneralPurposeInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class TextualMonitorViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    var feature: Feature<*>? = null

    private var observeFeatureJob: Job? = null

    private var currentDesc: BleCharacteristic? = null

    private val _dataFeature = MutableSharedFlow<String>()
    val dataFeature: Flow<String>
        get() = _dataFeature

    private val _debugMessages = MutableStateFlow<String?>(null)
    val debugMessages: StateFlow<String?> = _debugMessages.asStateFlow()

//    private var componentsDTMI: List<Pair<DtmiContent.DtmiComponentContent, DtmiContent.DtmiInterfaceContent>>?=null

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

//                componentsDTMI = blueManager.getDtmiModel(nodeId = nodeId,isBeta = stPreferences.isBetaApplication())?.filterComponentsByProperty(propName = "st_ble_stream")

                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    if (feature!!.type != Feature.Type.GENERAL_PURPOSE) {
                        if(feature!!.name != RawPnPLControlled.NAME) {
                            val data = it.data
                            _dataFeature.emit("\nTS =${it.timeStamp}:\n")
                            _dataFeature.emit(data.toString())
                        } else {
                            val data = it.data
                            _dataFeature.emit("\nTS =${it.timeStamp}:\n")

                            //Decode the Raw Controlled PnPL Feature
//                            val featureDataString = rawPnPLControlledFeatureDataString(it)
//                            _dataFeature.emit(featureDataString)
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

    private fun rawPnPLControlledFeatureDataString(it: FeatureUpdate<*>): String {
//    if(componentsDTMI!=null) {
//        // find streamId
//
//        val streamId = (it.data as RawPnPLControlledInfo).data[0].value
//
//        //List of contents that have st_ble_stream
//        val contents =  componentsDTMI!!.forEach {
//            it.second.contents.forEach { content->
//                if( content.name == "st_ble_stream") {
//                    if(content is DtmiContent.DtmiPropertyContent.DtmiComplexPropertyContent) {
//                        if(content.schema is DtmiContent.DtmiObjectContent) {
//                            val fields = (content.schema as DtmiContent.DtmiObjectContent).fields
//                            if(fields[0].name=="id") {
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//    }
        return "Sample raw data size=${it.rawData.size}\n"
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
}
