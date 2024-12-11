/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.debug_console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.demo_showcase.ui.DebugConsoleMsg
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DebugConsoleViewModel @Inject constructor(private val blueManager: BlueManager) :
    ViewModel() {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    private val _debugMessages = MutableStateFlow<List<DebugConsoleMsg>>(emptyList())
    val debugMessages: StateFlow<List<DebugConsoleMsg>> = _debugMessages.asStateFlow()

    companion object {
        private const val PAUSE_DETECTION_TIME_MS: Long = 100 //ms

    }

    fun sendDebugMessage(nodeId: String, msg: String) {
        viewModelScope.launch {
            _debugMessages.value += DebugConsoleMsg.DebugConsoleCommand(
                command = msg, time = LocalDateTime.now().format(dateTimeFormatter)
            )

            blueManager.writeDebugMessage(
                nodeId = nodeId, msg = "$msg\n"
            )
        }
    }

    fun receiveDebugMessage(nodeId: String) {
        viewModelScope.launch {
            var lastReceivedData =  System.currentTimeMillis()
            blueManager.getDebugMessages(nodeId = nodeId)?.collect {
                val currentTime = System.currentTimeMillis()

                if((currentTime-lastReceivedData)> PAUSE_DETECTION_TIME_MS) {
                    _debugMessages.value += DebugConsoleMsg.DebugConsoleResponse(
                        response = it, time = LocalDateTime.now().format(dateTimeFormatter)
                    )
                } else  {
                    _debugMessages.value += DebugConsoleMsg.DebugConsoleResponse(
                        response = it, time = null
                    )
                }
                lastReceivedData = currentTime
            }
        }
    }

    fun clearConsole() {
        _debugMessages.value = emptyList()
    }
}