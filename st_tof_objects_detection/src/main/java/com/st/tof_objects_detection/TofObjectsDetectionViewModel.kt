/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.tof_objects_detection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObject
import com.st.blue_sdk.features.extended.tof_multi_object.ToFMultiObjectInfo
import com.st.blue_sdk.features.extended.tof_multi_object.request.CommandPresenceRecognition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TofObjectsDetectionViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    var mPresenceDemo = false

    private val _tofData = MutableSharedFlow<ToFMultiObjectInfo>()
    val tofData: Flow<ToFMultiObjectInfo>
        get() = _tofData

    fun startDemo(nodeId: String) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                ToFMultiObject.NAME == it.name
            }?.let { f ->
                feature = f
            }
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, listOf(it)).collect {
                    val data = it.data
                    if (data is ToFMultiObjectInfo) {
                        _tofData.emit(data)
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        feature?.let {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, listOf(it))
            }
        }
    }


    fun enableDisablePresence(enablePresence: Boolean, nodeId: String) {
        blueManager.nodeFeatures(nodeId = nodeId).find {
            it.name == ToFMultiObject.NAME
        }?.let {
            val feature = it as ToFMultiObject

            mPresenceDemo = enablePresence

            viewModelScope.launch {
                blueManager.writeFeatureCommand(
                    nodeId = nodeId,
                    featureCommand = CommandPresenceRecognition(
                        feature = feature,
                        enable = enablePresence
                    )
                )
            }
        }
    }
}
