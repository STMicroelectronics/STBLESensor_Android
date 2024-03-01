/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.registers_demo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeature
import com.st.blue_sdk.features.extended.registers_feature.RegistersFeatureInfo
import com.st.registers_demo.common.RegisterStatus
import com.st.registers_demo.common.RegistersDemoType
import com.st.registers_demo.common.ValueLabelMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class RegistersDemoViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private var feature: Feature<*>? = null

    private val REGISTER_INFO: Pattern = Pattern.compile("<(MLC|FSM_OUTS|STREDL)(\\d+)(_SRC)?>(.*)")
    private val VALUE_INFO: Pattern = Pattern.compile("(\\d+)='(.*)'")

    private val _registersData = MutableSharedFlow<List<RegisterStatus>>()
    val registersData: Flow<List<RegisterStatus>>
        get() = _registersData

    /**
     * object used to map the raw register value with a label,
     * when set update the view adding the labels to the registers
     */
    private var valueMapper: ValueLabelMapper? = null
        set(value) {
            updateCurrentRegisterStatus(value)
            field = value
        }

    private fun updateCurrentRegisterStatus(mapper: ValueLabelMapper?) {
        viewModelScope.launch {
            val currentStatus = registersData.last()
            val newStatus = currentStatus.map {
                RegisterStatus(
                    it.registerId, it.value,
                    mapper?.algorithmName(it.registerId),
                    mapper?.valueName(it.registerId, it.value.toInt())
                )
            }
            _registersData.emit(newStatus)
        }
    }

    fun startDemo(nodeId: String, demoType: RegistersDemoType) {
        if (feature == null) {
            blueManager.nodeFeatures(nodeId).find {
                when (demoType) {
                    RegistersDemoType.MLC -> RegistersFeature.ML_CORE_NAME == it.name
                    RegistersDemoType.FSM -> RegistersFeature.FSM_NAME == it.name
                    RegistersDemoType.STRED -> RegistersFeature.STRED_NAME == it.name
                }
            }?.let { f ->
                feature = f
            }
        }

        if (MlcConfig.registerValueToLabelMap == null) {

            viewModelScope.launch {
                val buffer = StringBuffer()
                blueManager.getDebugMessages(nodeId = nodeId)?.collect {
                    //Log.i("RegistersDemo DbGMsg",it.payload)
                    buffer.append(it.payload)
                    //Log.i("RegistersDemo","buffersize=${buffer.length}")
                    if (buffer.endsWith('\n')) {
                        //Log.i("RegistersDemo","fullRec")
                        valueMapper = buildRegisterMapperFromString(buffer.removeSuffix("\n"))
                        buffer.delete(0,buffer.length)
                    }
                }
            }
            askLabelsToNode(nodeId, demoType)

        }
        else {
            valueMapper = buildRegisterMapperFromString(MlcConfig.registerValueToLabelMap!!.removeSuffix("\n"))
        }

        feature?.let {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId,
                    listOf(it),
                    onFeaturesEnabled = {
                        readFeature(nodeId)
                    }
                ).collect {
                    val data = it.data
                    if (data is RegistersFeatureInfo) {
                        val regStatus = data.registers.map { it2 -> it2.value }.toShortArray()
                        val registersStatus = regStatus.mapIndexed { id, value ->
                            RegisterStatus(
                                id, value,
                                valueMapper?.algorithmName(id),
                                valueMapper?.valueName(id, value.toInt())
                            )
                        }
                        _registersData.emit(registersStatus)
                    }
                }
            }
        }
    }

    private fun buildRegisterMapperFromString(receivedData: CharSequence): ValueLabelMapper? {
        val registerData = receivedData.split(';').filter {  it.isNotEmpty() }
        val mapper = ValueLabelMapper()
        registerData.forEach { data ->
            val splitData = data.split(',').filter { it.isNotEmpty() }
            val (registerId, algoName) = extractRegisterInfo(splitData[0]) ?: return null
            mapper.addRegisterName(registerId, algoName)
            for (i in 1 until splitData.size) {
                val (value, name) = extractValueInfo(splitData[i]) ?: return null
                mapper.addLabel(registerId, value, name)
            }
        }
        return mapper
    }

    private fun extractValueInfo(valueInfo: String): Pair<Int, String>? {
        val match = VALUE_INFO.matcher(valueInfo)
        if (!match.matches())
            return null
        val value = match.group(1)?.toInt() ?: return null
        val name = match.group(2) ?: return null
        return Pair(value, name)
    }

    private fun extractRegisterInfo(registerInfo: String): Pair<Int, String>? {
        val match = REGISTER_INFO.matcher(registerInfo)
        if (!match.matches())
            return null
        var id = match.group(2)?.toInt() ?: return null
        // FSM index start from 1, MLC index start from 0
        if (match.group(1) == "FSM_OUTS") {
            id -= 1
        }
        val name = match.group(4) ?: return null
        return Pair(id, name)
    }

    private fun askLabelsToNode(nodeId: String, demoType: RegistersDemoType) {
        //Ask the labels only if the Node exports the feature
        feature?.let {
            val labelCommand: String = when (demoType) {
                RegistersDemoType.MLC -> "getMLCLabels"
                RegistersDemoType.FSM -> "getFSMLabels"
                RegistersDemoType.STRED -> "getSTREDLLabels"
            }

            viewModelScope.launch {
                blueManager.writeDebugMessage(
                    nodeId = nodeId, msg = labelCommand
                )
            }
        }
    }

    private fun readFeature(
        nodeId: String,
        timeout: Long = 2000
    ) {
        feature?.let {
            coroutineScope.launch {
                val data = blueManager.readFeature(nodeId, it, timeout)
                data.forEach { featureUpdate ->
                    val dataFeature = featureUpdate.data
                    if (dataFeature is RegistersFeatureInfo) {
                        val regStatus =
                            dataFeature.registers.map { it2 -> it2.value }.toShortArray()
                        val registersStatus = regStatus.mapIndexed { id, value ->
                            RegisterStatus(
                                id, value,
                                valueMapper?.algorithmName(id),
                                valueMapper?.valueName(id, value.toInt())
                            )
                        }
                        _registersData.emit(registersStatus)
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
}
