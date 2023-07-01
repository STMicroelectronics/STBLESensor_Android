/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.ui.ext_config

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.ext_configuration.*
import com.st.blue_sdk.features.extended.ext_configuration.request.CommandName
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtConfigCommands
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.ext_config.R
import com.st.ext_config.model.CustomCommandType
import com.st.ext_config.model.WifiCredentials
import com.st.ext_config.util.customCommandType
import com.st.ext_config.util.dateToString
import com.st.ext_config.util.timeToString
import com.st.ext_config.util.update
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExtConfigViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope,
    @ApplicationContext context: Context
) : ViewModel() {

    companion object {
        const val MAX_PIN_LEN = 6
        const val MAX_NAME_LEN = 7
        const val RESPONSE_TIMEOUT = 1250L
        const val TAG = "ExtConfigViewModel"
    }

    private val resources = context.resources

    private val features = mutableListOf<Feature<*>>()
    private val _uiState: MutableStateFlow<ExtConfigCommandAnswers> =
        MutableStateFlow(ExtConfigCommandAnswers())
    val uiState: StateFlow<ExtConfigCommandAnswers> = _uiState.asStateFlow()
    private val _dialogResult: MutableStateFlow<DialogResult?> = MutableStateFlow(null)
    val dialogResult: StateFlow<DialogResult?> = _dialogResult.asStateFlow()

    @SuppressLint("MissingPermission")
    fun getDeviceName(nodeId: String) = blueManager.getNode(nodeId = nodeId)?.device?.name ?: ""

    fun startDemo(nodeId: String) {
        viewModelScope.launch {
            if (features.isEmpty()) {
                blueManager.nodeFeatures(nodeId).find { ExtConfiguration.NAME == it.name }
                    ?.let { f -> features.add(f) }

                blueManager.enableFeatures(
                    nodeId = nodeId,
                    features = features
                )

                features.filterIsInstance<ExtConfiguration>().firstOrNull()?.let { feature ->
                    val initialCommands = listOf(
                        ExtendedFeatureCommand(
                            feature,
                            ExtConfigCommands.buildConfigCommand(ExtConfigCommands.READ_COMMANDS)
                        ),
                        ExtendedFeatureCommand(
                            feature,
                            ExtConfigCommands.buildConfigCommand(ExtConfigCommands.READ_UID)
                        ),
                        ExtendedFeatureCommand(
                            feature,
                            ExtConfigCommands.buildConfigCommand(ExtConfigCommands.BANKS_STATUS)
                        )
                    )
                    initialCommands.forEach { command ->
                        val response = blueManager.writeFeatureCommand(
                            nodeId = nodeId,
                            featureCommand = command
                        )

                        if (response is ExtendedFeatureResponse) {
                            _uiState.value = _uiState.value.update(response.response)
                        }
                    }
                }
            }
        }
    }

    fun stopDemo(nodeId: String) {
        coroutineScope.launch {
            blueManager.disableFeatures(nodeId, features)
        }
    }

    fun sendCommand(nodeId: String, command: Command, arg: Any? = null) {
        viewModelScope.launch {
            if (features.isNotEmpty()) {
                val feature = features.firstOrNull { it is ExtConfiguration } as ExtConfiguration?

                if (feature != null) {
                    val featureCommand = when (command) {
                        Command.READ_COMMANDS,
                        Command.READ_CUSTOM_COMMANDS,
                        Command.READ_CERTIFICATE,
                        Command.READ_UID,
                        Command.READ_VERSION_FW,
                        Command.READ_INFO,
                        Command.READ_HELP,
                        Command.READ_POWER_STATUS,
                        Command.BANKS_STATUS,
                        Command.CLEAR_DB,
                        Command.SET_DFU,
                        Command.POWER_OFF,
                        Command.BANKS_SWAP -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName
                            )
                        )

                        Command.CHANGE_PIN -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName,
                                argNumber = arg as Int
                            ),
                            hasResponse = false
                        )

                        Command.SET_TIME -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName,
                                argString = Date().timeToString()
                            ),
                            hasResponse = false
                        )

                        Command.SET_DATE -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName,
                                argString = Date().dateToString()
                            ),
                            hasResponse = false
                        )

                        Command.SET_WIFI -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName,
                                jsonArgs = Json.encodeToJsonElement(arg as WifiCredentials)
                            ),
                            hasResponse = false
                        )

                        Command.SET_SENSORS -> null // Not supported by new ST Ble Sensor
                        Command.SET_CERTIFICATE -> null // TODO:
                        Command.READ_SENSORS -> null // Not supported by new ST Ble Sensor
                        Command.SET_NAME -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName,
                                argString = arg as String
                            ),
                            hasResponse = false
                        )
                    }

                    featureCommand?.let {
                        val response = blueManager.writeFeatureCommand(
                            responseTimeout = RESPONSE_TIMEOUT,
                            nodeId = nodeId,
                            featureCommand = it
                        )

                        if (response is ExtendedFeatureResponse) {
                            when (command) {
                                Command.READ_UID -> _dialogResult.value = DialogResult(
                                    title = resources.getString(R.string.st_extConfig_boardReport_uid),
                                    text = response.response.stm32UID ?: ""
                                )

                                Command.READ_VERSION_FW -> _dialogResult.value = DialogResult(
                                    title = resources.getString(R.string.st_extConfig_boardReport_versionFirmware),
                                    text = response.response.versionFw ?: ""
                                )

                                Command.READ_HELP -> _dialogResult.value = DialogResult(
                                    title = resources.getString(R.string.st_extConfig_boardReport_help),
                                    text = response.response.help ?: ""
                                )

                                Command.READ_POWER_STATUS -> _dialogResult.value = DialogResult(
                                    title = resources.getString(R.string.st_extConfig_boardReport_powerStatus),
                                    text = response.response.powerStatus ?: ""
                                )

                                Command.READ_CERTIFICATE -> _dialogResult.value = DialogResult(
                                    title = resources.getString(R.string.st_extConfig_boardSecurity_certRequest),
                                    text = response.response.certificate ?: ""
                                )

                                else -> Unit
//                                Handled by main observer on feature
//                                Command.READ_INFO -> _dialogResult.value = DialogResult(
//                                    title = context.getString(R.string.board_report_info),
//                                    text = response.response.info ?: ""
//                                )
//                            Command.READ_COMMANDS -> Unit
//                            Command.CHANGE_PIN -> Unit
//                            Command.CLEAR_DB -> Unit
//                            Command.SET_DFU -> Unit
//                            Command.POWER_OFF -> Unit
//                            Command.BANKS_STATUS -> Unit
//                            Command.BANKS_SWAP -> Unit
//                            Command.SET_TIME -> Unit
//                            Command.SET_DATE -> Unit
//                            Command.SET_WIFI -> Unit
//                            Command.READ_SENSORS -> Unit
//                            Command.SET_SENSORS -> Unit
//                            Command.SET_NAME -> Unit
//                            Command.SET_CERTIFICATE -> Unit
//                            Command.READ_CUSTOM_COMMANDS -> Unit
                            }

                            _uiState.value = _uiState.value.update(other = response.response)
                        }
                    }
                }
            }
        }
    }

    fun sendCustomCommand(nodeId: String, command: CustomCommand, arg: Any? = null) {
        viewModelScope.launch {
            if (features.isNotEmpty()) {
                val feature = features.firstOrNull { it is ExtConfiguration } as ExtConfiguration?

                if (feature != null) {
                    val extendedCommand = when (command.customCommandType) {
                        CustomCommandType.VOID ->
                            ExtConfigCommands.buildConfigCommand(command = CommandName(command.name!!))

                        CustomCommandType.INTEGER,
                        CustomCommandType.BOOLEAN,
                        CustomCommandType.ENUMINTEGER ->
                            ExtConfigCommands.buildConfigCommand(
                                command = CommandName(command.name!!),
                                argNumber = arg as Int
                            )

                        CustomCommandType.STRING, CustomCommandType.ENUMSTRING ->
                            ExtConfigCommands.buildConfigCommand(
                                command = CommandName(command.name!!),
                                argString = arg as String
                            )

                        CustomCommandType.UNKNOWN -> null
                    }

                    extendedCommand?.let { featureCommand ->
                        val response = blueManager.writeFeatureCommand(
                            nodeId = nodeId,
                            responseTimeout = RESPONSE_TIMEOUT,
                            featureCommand = ExtendedFeatureCommand(
                                feature = feature,
                                extendedCommand = featureCommand,
                                hasResponse = true
                            )
                        )

                        if (response is ExtendedFeatureResponse) {
                            _uiState.value = _uiState.value.update(other = response.response)
                        }
                    }
                }
            }
        }
    }

    fun dismissErrorDialog() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun dismissInfoDialog() {
        _uiState.value = _uiState.value.copy(info = null)
    }

    fun dismissResultDialog() {
        _dialogResult.value = null
    }
}

val ExtConfigCommandAnswers.commandNameList
    get() = commandList?.replace(" ", "")?.split(",")?.distinct()
        ?.mapNotNull { Command.fromValue(it) } ?: emptyList()

enum class Command(val commandName: CommandName) {
    READ_COMMANDS(ExtConfigCommands.READ_COMMANDS), READ_CERTIFICATE(ExtConfigCommands.READ_CERTIFICATE), READ_UID(
        ExtConfigCommands.READ_UID
    ),
    READ_VERSION_FW(ExtConfigCommands.READ_VERSION_FW), READ_INFO(ExtConfigCommands.READ_INFO), READ_HELP(
        ExtConfigCommands.READ_HELP
    ),
    READ_POWER_STATUS(ExtConfigCommands.READ_POWER_STATUS), CHANGE_PIN(ExtConfigCommands.CHANGE_PIN), CLEAR_DB(
        ExtConfigCommands.CLEAR_DB
    ),
    SET_DFU(ExtConfigCommands.SET_DFU), POWER_OFF(ExtConfigCommands.POWER_OFF), BANKS_STATUS(
        ExtConfigCommands.BANKS_STATUS
    ),
    BANKS_SWAP(ExtConfigCommands.BANKS_SWAP), SET_TIME(ExtConfigCommands.SET_TIME), SET_DATE(
        ExtConfigCommands.SET_DATE
    ),
    SET_WIFI(ExtConfigCommands.SET_WIFI), READ_SENSORS(ExtConfigCommands.READ_SENSORS), SET_SENSORS(
        ExtConfigCommands.SET_SENSORS
    ),
    SET_NAME(ExtConfigCommands.SET_NAME), SET_CERTIFICATE(ExtConfigCommands.SET_CERTIFICATE), READ_CUSTOM_COMMANDS(
        ExtConfigCommands.READ_CUSTOM_COMMANDS
    );

    companion object {
        fun fromValue(value: String): Command? {
            return values().find { it.commandName.value == value }
        }
    }
}

data class DialogResult(
    val title: String,
    val text: String
)
