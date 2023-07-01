package com.st.demo_showcase.ui.serial_console

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SerialConsoleViewModel
@Inject internal constructor(
    private val blueManager: BlueManager
) : ViewModel() {

    private val _debugMessages = MutableStateFlow<String?>(null)
    val debugMessages: StateFlow<String?> = _debugMessages.asStateFlow()

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