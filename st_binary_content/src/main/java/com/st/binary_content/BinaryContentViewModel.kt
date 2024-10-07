/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.binary_content

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.models.DtmiContent
import com.st.blue_sdk.features.extended.binary_content.BinaryContent
import com.st.blue_sdk.features.extended.binary_content.BinaryContentCommand
import com.st.blue_sdk.features.extended.binary_content.RawData
import com.st.blue_sdk.features.extended.pnpl.PnPL
import com.st.blue_sdk.features.extended.pnpl.PnPLConfig
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCmd
import com.st.blue_sdk.features.extended.pnpl.request.PnPLCommand
import com.st.blue_sdk.models.ChunkProgress
import com.st.preferences.StPreferences
import com.st.ui.composables.CommandRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import java.io.FileNotFoundException
import java.io.IOError
import javax.inject.Inject

@HiltViewModel
class BinaryContentViewModel @Inject constructor(
    private val blueManager: BlueManager,
    //private val coroutineScope: CoroutineScope,
    private val stPreferences: StPreferences,
    private val application: Application
) : ViewModel() {

    private var observeFeaturePnPLJob: Job? = null
    private var observeChunkProgressJob: Job? = null

    //var maxBinaryContentWriteSize: Int = 20

    private val _maxBinaryContentWriteSize = mutableIntStateOf(value = 20)
    val maxBinaryContentWriteSize: State<Int>
        get() = _maxBinaryContentWriteSize


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

    private val _isSendingOperationOnGoing = mutableStateOf(value = false)
    val isSendingOperationOnGoing: State<Boolean>
        get() = _isSendingOperationOnGoing

    private val _enableCollapse = mutableStateOf(value = false)
    val enableCollapse: State<Boolean>
        get() = _enableCollapse

    private val _lastStatusUpdatedAt = mutableLongStateOf(value = 0L)
    val lastStatusUpdatedAt: State<Long>
        get() = _lastStatusUpdatedAt

    private var byteArrayContentToBoard: ByteArray? = null
    private var byteArrayContentFromBoard: ByteArray? = null

    private val _binaryContentReceived = mutableStateOf(value = false)
    val binaryContentReceived: State<Boolean>
        get() = _binaryContentReceived

    private val _binaryContentReadyForSending = mutableStateOf(value = false)
    val binaryContentReadyForSending: State<Boolean>
        get() = _binaryContentReadyForSending

    private val _fileOperationResultVisible = mutableStateOf(value = false)
    val fileOperationResultVisible: State<Boolean>
        get() = _fileOperationResultVisible

    var fileOperationResult: String? = null

    private val _bytesRec = mutableIntStateOf(value = 0)
    val bytesRec: State<Int>
        get() = _bytesRec

    private val _numberPackets = mutableIntStateOf(value = 0)
    val numberPackets: State<Int>
        get() = _numberPackets

    private val _chunkProgress = mutableStateOf<ChunkProgress?>(null)
    val chunkProgress: State<ChunkProgress?>
        get() = _chunkProgress

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

    private fun sendGetComponentStatus(nodeId: String, compName: String) {

        viewModelScope.launch {
            _isLoading.value = true
            val feature =
                blueManager.nodeFeatures(nodeId = nodeId).find { it.name == PnPL.NAME }
                    ?: return@launch

            if (feature is PnPL) {
                blueManager.writeFeatureCommand(
                    responseTimeout = 0,
                    nodeId = nodeId,
                    featureCommand = PnPLCommand(
                        feature = feature,
                        cmd = PnPLCmd(command = "get_status", request = compName)
                    )
                )
            }
        }
    }

    fun getModel(nodeId: String, compName: String) {
        viewModelScope.launch {
            _isLoading.value = true

            _modelUpdates.value =
                blueManager.getDtmiModel(
                    nodeId = nodeId,
                    isBeta = stPreferences.isBetaApplication()
                )?.extractComponent(compName = compName)
                    ?: emptyList()

            _enableCollapse.value = false

            _isLoading.value = false

            sendGetAllCommand(nodeId = nodeId)
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

                    sendGetComponentStatus(nodeId = nodeId, compName = name)
                }
            }
        }
    }

    fun hideFileOperation() {
        _fileOperationResultVisible.value = false
    }

    fun readBinaryContent(file: Uri?) {
        val contentResolver = application.contentResolver
        _binaryContentReadyForSending.value = false
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = file?.let { contentResolver.openInputStream(it) } ?: return@launch
                byteArrayContentToBoard = stream.readBytes()
                stream.close()
                fileOperationResult = "File read [${byteArrayContentToBoard!!.size} Bytes]"
                _fileOperationResultVisible.value = true
                _binaryContentReadyForSending.value = true
                return@launch
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                fileOperationResult = "File not Found"
                _fileOperationResultVisible.value = true
                return@launch
            } catch (e: IOError) {
                e.printStackTrace()
                fileOperationResult = "File I/O error"
                _fileOperationResultVisible.value = true
                return@launch
            }
        }
    }

    fun saveBinaryContent(file: Uri?) {
        val contentResolver = application.contentResolver
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val stream = file?.let { contentResolver.openOutputStream(it) } ?: return@launch

                stream.write(byteArrayContentFromBoard)
                stream.close()
                fileOperationResult = "File written"
                _binaryContentReceived.value = false
                _fileOperationResultVisible.value = true
                return@launch
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                fileOperationResult = "File not found"
                _fileOperationResultVisible.value = true
                return@launch
            } catch (e: IOError) {
                e.printStackTrace()
                fileOperationResult = "File I/O error"
                _fileOperationResultVisible.value = true
                return@launch
            }
        }
    }

    fun writeBinaryContentToNode(nodeId: String) {
        if (byteArrayContentToBoard != null) {
            blueManager.nodeFeatures(nodeId = nodeId).find {
                it.name == BinaryContent.NAME
            }?.let {
                val feature = it as BinaryContent
                //Set the MaxPayloadSize to the node
                //20 byte is the default size
                feature.setMaxPayLoadSize(maxBinaryContentWriteSize.value)

                //Get the chunk progress
                observeChunkProgressJob?.cancel()
                observeChunkProgressJob =viewModelScope.launch {
                    blueManager.resetChunkProgressUpdates(nodeId = nodeId)
                    blueManager.getChunkProgressUpdates(nodeId = nodeId)?.collect { chunkProgress ->
                        _chunkProgress.value = chunkProgress
                    }
                }

                viewModelScope.launch(Dispatchers.IO) {
                    _isSendingOperationOnGoing.value = true
                    blueManager.writeFeatureCommand(
                        responseTimeout = 0,
                        nodeId = nodeId,
                        featureCommand = BinaryContentCommand(
                            feature = feature,
                            data = byteArrayContentToBoard!!
                        )
                    )
                    _isSendingOperationOnGoing.value = false
                    observeChunkProgressJob?.cancel()
                    byteArrayContentToBoard = null
                    _binaryContentReadyForSending.value = false
                    _numberPackets.intValue = 0
                    _bytesRec.intValue = 0
                }
            }
        }
    }

    fun startDemo(nodeId: String) {
        observeFeaturePnPLJob?.cancel()
        _numberPackets.intValue = 0
        _bytesRec.intValue = 0

        //Set the maxPayload size used for writing to node..
//        val node = blueManager.getNode(nodeId)
//        node?.let {
//            blueManager.nodeFeatures(nodeId).find { it.name == BinaryContent.NAME }
//                .let { feature ->
//                    (feature as BinaryContent).setMaxPayLoadSize(node.maxPayloadSize)
//                }
//        }

        blueManager.nodeFeatures(nodeId)
            .filter { it.name == PnPL.NAME || it.name == BinaryContent.NAME }.let { feature ->

                observeFeaturePnPLJob = blueManager.getFeatureUpdates(
                    nodeId = nodeId,
                    features = feature,
                    onFeaturesEnabled = {
                        launch {
                            // //getModel(nodeId = nodeId, demoName = null)
                            _isLoading.value = true

                            val node = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)


                            _modelUpdates.value =
                                blueManager.getDtmiModel(
                                    nodeId = nodeId,
                                    isBeta = stPreferences.isBetaApplication()
                                )
                                    ?.extractComponent(compName = "control") ?: emptyList()

                            _enableCollapse.value = false
                            // //

                            // //Send
//                            if (byteArrayContentToBoard != null) {
//                                writeBinaryContentToNode(nodeId, byteArrayContentToBoard!!)
//                                byteArrayContentToBoard = null
//                            }
                            // //

                            val featurePnPL =
                                blueManager.nodeFeatures(nodeId).find { it.name == PnPL.NAME }

                            if (featurePnPL is PnPL) {

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

                            //Search the max write for Binary Content
                            val binaryFeature = blueManager.nodeFeatures(nodeId = nodeId).find {it.name == BinaryContent.NAME}
                            if (binaryFeature is BinaryContent) {
                                var maxWriteLength =
                                    node.catalogInfo?.characteristics?.firstOrNull { it.name == BinaryContent.NAME }?.maxWriteLength
                                maxWriteLength?.let {

                                    if (maxWriteLength!! > (node.maxPayloadSize)) {
                                        maxWriteLength = (node.maxPayloadSize)
                                    }

                                    binaryFeature.setMaxPayLoadSize(maxWriteLength!!)
                                    _maxBinaryContentWriteSize.intValue = maxWriteLength!!
                                }
                            }
                            // //
                        }
                    }
                ).flowOn(Dispatchers.IO).onEach { featureUpdate ->

                    val data = featureUpdate.data

                    if (data is PnPLConfig) {
                        data.deviceStatus.value?.components?.let { json ->
                            _lastStatusUpdatedAt.longValue = System.currentTimeMillis()
                            _componentStatusUpdates.value = json
                            _isLoading.value = false
                        }
                    } else if (data is RawData) {
                        if (data.data.value != null) {
                            //EOS received
                            byteArrayContentFromBoard = data.data.value
                            _binaryContentReceived.value = true
                        }
                        _numberPackets.intValue = data.numberPackets.value
                        _bytesRec.intValue = data.bytesRec.value

                    }
                }.launchIn(viewModelScope)
            }
    }

    fun stopDemo(nodeId: String) {
        observeFeaturePnPLJob?.cancel()

        _componentStatusUpdates.value = emptyList()

        runBlocking {
            val features = blueManager.nodeFeatures(nodeId)
                .filter { it.name == BinaryContent.NAME || it.name == PnPL.NAME }
            blueManager.disableFeatures(
                nodeId = nodeId, features = features
            )
        }
    }

    fun setMaxBinaryContentWriteSize(newValue: Int) {
     _maxBinaryContentWriteSize.intValue = newValue
    }
}
