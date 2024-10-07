/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.multi_neural_network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.activity.Activity
import com.st.blue_sdk.features.activity.ActivityInfo
import com.st.blue_sdk.features.extended.audio_classification.AudioClassification
import com.st.blue_sdk.features.extended.audio_classification.AudioClassificationInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MultiNeuralNetworkViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    companion object {
        private const val GET_AVAILABLE_ALGORITHMS = "getAllAIAlgo\n"
        //private const val GET_CURRENT_ALGORITHM = "getAIAlgo\n"
        private const val SET_ALGORITHM_FORMAT = "setAIAlgo %d\n"
        //regexp: a number follow by - follow by a string follow by an optional ,
        private  val AVAILABLE_ALGORITHMS_REG_EXP: Pattern = Pattern.compile("((\\d+-.+,?)+)\\n")
        //private  const val COMMAND_TIMEOUT_MS = 500L
    }

    private var features: MutableList<Feature<*>> = mutableListOf()

    private val _activityInfo =
        MutableStateFlow<ActivityInfo?>(null)
    val activityInfo: StateFlow<ActivityInfo?>
        get() = _activityInfo.asStateFlow()


    private val _audioClassificationInfo =
        MutableStateFlow<AudioClassificationInfo?>(null)
    val audioClassificationInfo: StateFlow<AudioClassificationInfo?>
        get() = _audioClassificationInfo.asStateFlow()


    private val _algorithmsList =
        MutableStateFlow<List<AvailableAlgorithm>>(
            listOf()
        )
    val algorithmsList: StateFlow<List<AvailableAlgorithm>>
        get() = _algorithmsList.asStateFlow()


    private fun writeMessageOnDebugConsole(nodeId: String,message: String) {
        viewModelScope.launch {
            blueManager.writeDebugMessage(
                nodeId = nodeId, msg = message
            )
        }
    }

    fun startDemo(nodeId: String) {
        if (features.isEmpty()) {
            blueManager.nodeFeatures(nodeId).filter {
                Activity.NAME == it.name || AudioClassification.NAME == it.name
            }.let {
                features.addAll(it)
            }
        }

        //Collect Features Update
        if (features.isNotEmpty()) {
            viewModelScope.launch {
                blueManager.getFeatureUpdates(nodeId, features).collect {
                    val data = it.data

                    if (data is ActivityInfo) {
                        _activityInfo.emit(data)
                    }

                    if (data is AudioClassificationInfo) {
                        _audioClassificationInfo.emit(data)
                    }
                }
            }
        }

        //Collect Debug Messages
        viewModelScope.launch {
            val buffer = StringBuffer()
            blueManager.getDebugMessages(nodeId = nodeId)?.collect {
               buffer.append(it.payload)
                val matcher  = AVAILABLE_ALGORITHMS_REG_EXP.matcher(buffer)
                if(matcher.find()) {
                    // get only the char that are matching the reg exp
                    val rawData = matcher.group().removeTerminatorCharacters()
                    buffer.delete(0,buffer.length)
                    val algorithms = extractAlgorithms(rawData)

                    if(algorithms.isNotEmpty()) {
                        _algorithmsList.emit (algorithms)
                    } else {
                        _algorithmsList.emit(listOf())
                    }
                    buffer.delete(0,buffer.length)
                }

                if(buffer.contains("NNconfidence") || buffer.contains("Sending: ASC")) {
                    buffer.delete(0,buffer.length)
                }
            }
        }

        //Ask the list of available algorithms
        getAvailableAlgorithm(nodeId = nodeId)
    }

    private fun extractAlgorithms(rawData: String): List<AvailableAlgorithm> {
        return rawData.split(',').mapNotNull { algo ->
            val component = algo.split('-')
            val index = component.getOrNull(0)?.toIntOrNull()
            val name = component.getOrNull(1)
            if(index!=null && name !=null){
                AvailableAlgorithm(index,name)
            }else
                null
        }
    }

    fun stopDemo(nodeId: String) {
        if(features.isNotEmpty()) {
            coroutineScope.launch {
                blueManager.disableFeatures(nodeId, features)
            }
        }
    }

    fun selectAlgorithm(nodeId: String,selected: AvailableAlgorithm) {
        //disable features... stopDemo?
        val cmd = String.format(Locale.getDefault(),SET_ALGORITHM_FORMAT,selected.index)
        writeMessageOnDebugConsole(nodeId, cmd)
        //enable features...startDemo
    }

    private fun getAvailableAlgorithm(nodeId: String) {
        writeMessageOnDebugConsole(nodeId,GET_AVAILABLE_ALGORITHMS)
    }
}

private fun String.removeTerminatorCharacters(): String {
    return this.replace("\n", "").replace("\r", "")
}

data class AvailableAlgorithm(
    val index:Int,
    val name:String
)

