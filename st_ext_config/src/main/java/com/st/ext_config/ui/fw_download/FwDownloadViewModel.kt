/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.ui.fw_download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.board_catalog.BoardCatalogRepo
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.bt.advertise.getFwInfo
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.ext_configuration.BanksStatus
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.ext_configuration.ExtendedFeatureResponse
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtConfigCommands
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.blue_sdk.models.Node
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class FwDownloadViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope,
    private val catalog: BoardCatalogRepo
) : ViewModel() {

    companion object {
        const val TAG = "FwDownloadViewModel"
    }

    private val features = mutableListOf<Feature<*>>()
    private val _uiState: MutableStateFlow<FwDownloadUiState> =
        MutableStateFlow(FwDownloadUiState())
    val bankStatus: StateFlow<FwDownloadUiState> = _uiState.asStateFlow()
    private val _device: MutableStateFlow<Node?> = MutableStateFlow(null)

    fun startDemo(nodeId: String, banksStatus: BanksStatus? = null) {
        viewModelScope.launch {
            if (features.isEmpty()) {
                //if (banksStatus == null) {
                    blueManager.nodeFeatures(nodeId).find { ExtConfiguration.NAME == it.name }
                        ?.let { f -> features.add(f) }

                    blueManager.enableFeatures(
                        nodeId = nodeId, features = features
                    )
                //}

                _device.value = blueManager.getNodeWithFirmwareInfo(nodeId = nodeId)

                if (banksStatus == null) {
                    features.filterIsInstance<ExtConfiguration>().firstOrNull()?.let { feature ->
                        val response = blueManager.writeFeatureCommand(
                            nodeId = nodeId, featureCommand = ExtendedFeatureCommand(
                                feature = feature,
                                extendedCommand = ExtConfigCommands.buildConfigCommand(
                                    ExtConfigCommands.BANKS_STATUS
                                )
                            )
                        )
                        if (response is ExtendedFeatureResponse) {
                            response.response.banksStatus?.let { bankStatus ->
                                var fwDetail1: BoardFirmware? = null
                                var fwDetail2: BoardFirmware? = null
                                val advInfo = _device.value?.advertiseInfo

                                advInfo?.getFwInfo()?.let {
                                    fwDetail1 = catalog.getFwDetailsNode(
                                        deviceId = it.deviceId,
                                        bleFwId = bankStatus.fwId1
                                    )
                                    fwDetail2 = catalog.getFwDetailsNode(
                                        deviceId = it.deviceId,
                                        bleFwId = bankStatus.fwId2
                                    )
                                }
                                val updateFwDetail =
                                    if (fwDetail2?.fwName != _device.value?.fwUpdate?.fwName &&
                                        fwDetail2?.fwVersion != _device.value?.fwUpdate?.fwVersion
                                    )
                                        _device.value?.fwUpdate
                                    else
                                        null

                                _uiState.value = FwDownloadUiState(
                                    node = _device.value,
                                    bankStatus = bankStatus,
                                    currentFwDetail = fwDetail1,
                                    otherFwDetail = fwDetail2,
                                    updateFwDetail = updateFwDetail
                                )
                            }
                        }
                    }
                } else {
                    var fwDetail1: BoardFirmware? = null
                    var fwDetail2: BoardFirmware? = null
                    val advInfo = _device.value?.advertiseInfo

                    advInfo?.getFwInfo()?.let {
                        fwDetail1 = catalog.getFwDetailsNode(
                            deviceId = it.deviceId,
                            bleFwId = banksStatus.fwId1
                        )
                        fwDetail2 = catalog.getFwDetailsNode(
                            deviceId = it.deviceId,
                            bleFwId = banksStatus.fwId2
                        )
                    }
                    val updateFwDetail =
                        if (fwDetail2?.fwName != _device.value?.fwUpdate?.fwName &&
                            fwDetail2?.fwVersion != _device.value?.fwUpdate?.fwVersion
                        )
                            _device.value?.fwUpdate
                        else
                            null

                    _uiState.value = FwDownloadUiState(
                        node = _device.value,
                        bankStatus = banksStatus,
                        currentFwDetail = fwDetail1,
                        otherFwDetail = fwDetail2,
                        updateFwDetail = updateFwDetail
                    )
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        //coroutineScope.launch {
        //Not optimal... but in this way... I will not loose notifications
        runBlocking {
            blueManager.disableFeatures(nodeId, features)
        }
    }

    fun swapBank(nodeId: String) {
        viewModelScope.launch {
            if (features.isNotEmpty()) {
                val feature = features.firstOrNull { it is ExtConfiguration } as ExtConfiguration?

                if (feature != null) {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand =
                            ExtConfigCommands.buildConfigCommand(command = ExtConfigCommands.BANKS_SWAP),
                            hasResponse = false
                        )
                    )
                }
            }
        }
    }
}

data class FwDownloadUiState(
    val node: Node? = null,
    val bankStatus: BanksStatus? = null,
    val currentFwDetail: BoardFirmware? = null,
    val otherFwDetail: BoardFirmware? = null,
    val updateFwDetail: BoardFirmware? = null
)
