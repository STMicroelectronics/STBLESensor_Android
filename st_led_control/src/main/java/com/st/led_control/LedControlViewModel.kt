/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.led_control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.FeatureField
import com.st.blue_sdk.features.external.stm32.P2PConfiguration
import com.st.blue_sdk.features.external.stm32.led_and_reboot.ControlLedAndReboot
import com.st.blue_sdk.features.external.stm32.led_and_reboot.ControlLedCommand
import com.st.blue_sdk.features.external.stm32.switch_status.SwitchInfo
import com.st.blue_sdk.features.external.stm32.switch_status.SwitchStatus
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.RssiData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedControlViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var features: MutableList<Feature<*>> = mutableListOf()

    private val _switchData =
        MutableStateFlow<Pair<SwitchInfo, Long?>>(
            Pair(
                SwitchInfo(
                    deviceId = FeatureField(
                        name = "DeviceId",
                        value = 0,
                    ), isSwitchPressed = FeatureField(
                        name = "SwitchPressed",
                        value = false
                    )
                ), null
            )
        )
    val switchData: StateFlow<Pair<SwitchInfo, Long?>>
        get() = _switchData.asStateFlow()

    private val _rssiData =
        MutableStateFlow<RssiData?>(null)
    val rssiData: StateFlow<RssiData?>
        get() = _rssiData.asStateFlow()

    fun startDemo(nodeId: String) {
        if (features.isEmpty()) {
            val filteredFeatures = blueManager.nodeFeatures(nodeId).asSequence().filter {
                SwitchStatus.NAME == it.name || ControlLedAndReboot.NAME == it.name
            }.toList()

            features.addAll(filteredFeatures)

            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId = nodeId, features = features)
                    .collect {
                        val data = it.data
                        if (data is SwitchInfo) {
                            _switchData.emit(Pair(data, it.timeStamp))
                        }
                    }
            }

            //Request a new RSSI value every second
            viewModelScope.launch {
                while (isActive) {
                    blueManager.getRssi(nodeId)
                    delay(1000)
                }
            }

            viewModelScope.launch {
                blueManager.getNodeStatus(nodeId = nodeId).collect {
                    _rssiData.emit(it.rssi)
                }
            }
        }
    }

    fun getNode(nodeId: String): Node? {
        return blueManager.getNode(nodeId)
    }

    fun stopDemo(nodeId: String) {
        if (features.isEmpty()) {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId = nodeId, features = features)
            }
        }
    }

    fun writeSwitchCommand(
        nodeId: String,
        mCurrentDevice: P2PConfiguration.DeviceId,
        currentValue: Boolean
    ) {
        features.firstOrNull { it.name == ControlLedAndReboot.NAME }?.let {
            viewModelScope.launch {
                if (currentValue) {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = ControlLedCommand(
                            feature = it,
                            turnOn = true,
                            commandId = ControlLedAndReboot.SWITCH_ON_COMMAND,
                            deviceId = mCurrentDevice.id
                        )
                    )
                } else {
                    blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = ControlLedCommand(
                            feature = it,
                            turnOn = false,
                            commandId = ControlLedAndReboot.SWITCH_OFF_COMMAND,
                            deviceId = mCurrentDevice.id
                        )
                    )
                }
            }
        }
    }
}
