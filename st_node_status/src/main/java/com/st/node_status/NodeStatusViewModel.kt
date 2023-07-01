/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.node_status

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.battery.Battery
import com.st.blue_sdk.features.battery.BatteryInfo
import com.st.blue_sdk.features.battery.request.GetBatteryCapacity
import com.st.blue_sdk.features.battery.response.BatteryCapacityResponse
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.RssiData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NodeStatusViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val _batteryData = MutableSharedFlow<BatteryInfo>()
    val batteryData: Flow<BatteryInfo>
        get() = _batteryData

    private val _rssiData = MutableSharedFlow<RssiData?>()
    val rssiData: Flow<RssiData?>
        get() = _rssiData

    private val _hasFeatureFlag = MutableLiveData(false)
    val hasFeatureFlag: LiveData<Boolean>
        get() = _hasFeatureFlag

    private val _batteryCapacity = MutableSharedFlow<Int>()
    val batteryCapacity: Flow<Int>
        get() = _batteryCapacity

//    private val _batteryCurrent = MutableSharedFlow<Float>()
//    val batteryCurrent: Flow<Float>
//        get() = _batteryCurrent

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                Battery.NAME == it.name
            }?.let { f ->
                feature = f
                _hasFeatureFlag.value=true
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is BatteryInfo) {
                        _batteryData.emit(data)
                    }
                }
            }
        }

        //Request a new RSSI value every second
        viewModelScope.launch {
            while(isActive) {
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

    fun getNode(nodeId: String): Node? {
        return blueManager.getNode(nodeId)
    }

    fun readBatteryCapacity(nodeId: String) {
        feature?.let {
            viewModelScope.launch {
                val response = blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = GetBatteryCapacity(
                        feature = it as Battery
                    )
                )
                if (response is BatteryCapacityResponse) {
                    _batteryCapacity.emit(response.capacity)
                }
            }
        }
    }

//    fun readStdConsumedCurrent(nodeId: String) {
//        feature?.let {
//            viewModelScope.launch {
//                val response = blueManager.writeFeatureCommand(
//                    nodeId = nodeId,
//                    featureCommand = GetBatteryMaxAbsorbedCurrent(
//                        feature = it as Battery
//                    )
//                )
//                if (response is BatteryAbsorbedCurrentResponse) {
//                    _batteryCurrent.emit(response.current)
//                }
//            }
//        }
//    }

    fun stopDemo(nodeId: String) {
        feature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }
}
