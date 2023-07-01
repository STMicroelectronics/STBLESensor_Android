/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.ui.cert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.st.blue_sdk.BlueManager
import com.st.blue_sdk.features.Feature
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfiguration
import com.st.blue_sdk.features.extended.ext_configuration.ExtendedFeatureResponse
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtConfigCommands
import com.st.blue_sdk.features.extended.ext_configuration.request.ExtendedFeatureCommand
import com.st.ext_config.model.WifiCredentials
import com.st.ext_config.ui.ext_config.Command
import com.st.ext_config.util.dateToString
import com.st.ext_config.util.timeToString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CertViewModel
@Inject internal constructor(
    private val blueManager: BlueManager,
    private val coroutineScope: CoroutineScope
) : ViewModel() {

    private val features = mutableListOf<Feature<*>>()
    private val _uid: MutableStateFlow<String> = MutableStateFlow("")
    val uid: StateFlow<String> = _uid.asStateFlow()
    private val _cert: MutableStateFlow<String> = MutableStateFlow("")
    val cert: StateFlow<String> = _cert.asStateFlow()

    fun startDemo(nodeId: String) {
        viewModelScope.launch {
            if (features.isEmpty()) {
                blueManager.nodeFeatures(nodeId).find { ExtConfiguration.NAME == it.name }
                    ?.let { f -> features.add(f) }

                blueManager.enableFeatures(
                    nodeId = nodeId, features = features
                )

                features.filterIsInstance<ExtConfiguration>().firstOrNull()?.let { feature ->
                    val readUidCmd = ExtendedFeatureCommand(
                        feature,
                        ExtConfigCommands.buildConfigCommand(ExtConfigCommands.READ_UID)
                    )

                    val readCertCmd = ExtendedFeatureCommand(
                        feature,
                        ExtConfigCommands.buildConfigCommand(ExtConfigCommands.READ_CERTIFICATE)
                    )

                    val uid = blueManager.writeFeatureCommand(
                        nodeId = nodeId,
                        featureCommand = readUidCmd
                    )
                    if (uid is ExtendedFeatureResponse) {
                        _uid.value = uid.response.stm32UID ?: ""
                    }

                    val cert = blueManager.writeFeatureCommand(
                        responseTimeout = 1250,
                        nodeId = nodeId,
                        featureCommand = readCertCmd
                    )
                    if (cert is ExtendedFeatureResponse) {
                        _cert.value = cert.response.certificate ?: ""
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
                                command = command.commandName, argNumber = arg as Int
                            ),
                            hasResponse = false
                        )

                        Command.SET_TIME -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName, argString = Date().timeToString()
                            ),
                            hasResponse = false
                        )

                        Command.SET_DATE -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName, argString = Date().dateToString()
                            ),
                            hasResponse = false
                        )

                        Command.SET_WIFI -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName, jsonArgs =
                                Json.encodeToJsonElement(arg as WifiCredentials)
                            ),
                            hasResponse = false
                        )

                        Command.SET_SENSORS -> null // TODO
                        Command.SET_CERTIFICATE -> null // TODO
                        Command.READ_SENSORS -> null
                        Command.SET_NAME -> ExtendedFeatureCommand(
                            feature = feature,
                            extendedCommand = ExtConfigCommands.buildConfigCommand(
                                command = command.commandName, argString = arg as String
                            ),
                            hasResponse = false
                        )
                    }

                    featureCommand?.let {
                        val response = blueManager.writeFeatureCommand(
                            responseTimeout = 1250, nodeId = nodeId, featureCommand = it
                        )

//                        if (response is ExtendedFeatureResponse) {
//                            when (command) {
//                                Command.READ_CERTIFICATE -> _dialogResult.value = DialogResult(
//                                    title = context.getString(R.string.st_extConfig_boardSecurity_certRequest),
//                                    text = response.response.certificate ?: ""
//                                )
//
//                                else -> Unit
////                                Command.READ_INFO -> _dialogResult.value = DialogResult(
////                                    title = context.getString(R.string.board_report_info),
////                                    text = response.response.info ?: ""
////                                )
////                            Command.READ_COMMANDS -> Unit
////                            Command.CHANGE_PIN -> Unit
////                            Command.CLEAR_DB -> Unit
////                            Command.SET_DFU -> Unit
////                            Command.POWER_OFF -> Unit
////                            Command.BANKS_STATUS -> Unit
////                            Command.BANKS_SWAP -> Unit
////                            Command.SET_TIME -> Unit
////                            Command.SET_DATE -> Unit
////                            Command.SET_WIFI -> Unit
////                            Command.READ_SENSORS -> Unit
////                            Command.SET_SENSORS -> Unit
////                            Command.SET_NAME -> Unit
////                            Command.SET_CERTIFICATE -> Unit
////                            Command.READ_CUSTOM_COMMANDS -> Unit
//                            }
//
//                            _uiState.value = _uiState.value.update(other = response.response)
//
//
//                        }
                    }
                }
            }
        }
    }
}
