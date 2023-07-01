/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.ui.ext_config

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.st.blue_sdk.features.extended.ext_configuration.CustomCommand
import com.st.blue_sdk.features.extended.ext_configuration.ExtConfigCommandAnswers
import com.st.ext_config.R
import com.st.ext_config.composable.BoardControlCard
import com.st.ext_config.composable.BoardCustomCommandsCard
import com.st.ext_config.composable.BoardReportCard
import com.st.ext_config.composable.BoardSecurityCard
import com.st.ext_config.composable.BoardSettingsCard
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExtConfigFragment : Fragment() {

    private val viewModel: ExtConfigViewModel by viewModels()
    private val navArgs: ExtConfigFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nodeId = navArgs.nodeId

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    ExtConfigScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        nodeId = nodeId,
                        goToFwDownload = {
                            findNavController().navigate(
                                ExtConfigFragmentDirections.actionExtConfigFragmentToFwDownload(
                                    nodeId
                                )
                            )
                        },
                        goToCertRequest = {
                            findNavController().navigate(
                                ExtConfigFragmentDirections.actionExtConfigFragmentToCertRequest(
                                    nodeId
                                )
                            )
                        },
                        goToCertRegistration = {
                            findNavController().navigate(
                                ExtConfigFragmentDirections.actionExtConfigFragmentToCertRegistration(
                                    nodeId
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

const val NAME_PAD_END_LEN = 7

@Composable
fun ExtConfigScreen(
    modifier: Modifier,
    viewModel: ExtConfigViewModel,
    nodeId: String,
    goToFwDownload: () -> Unit = { /** NOOP **/ },
    goToCertRequest: () -> Unit = { /** NOOP **/ },
    goToCertRegistration: () -> Unit = { /** NOOP **/ }
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId = nodeId)
            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogResult by viewModel.dialogResult.collectAsStateWithLifecycle()

    ExtConfigScreen(
        modifier = modifier,
        deviceName = viewModel.getDeviceName(nodeId = nodeId),
        dialogResult = dialogResult,
        goToFwDownload = goToFwDownload,
        goToCertRequest = goToCertRequest,
        goToCertRegistration = goToCertRegistration,
        onDismissResultDialog = {
            viewModel.dismissResultDialog()
        },
        onDismissInfoDialog = {
            viewModel.dismissInfoDialog()
        },
        onDismissErrorDialog = {
            viewModel.dismissErrorDialog()
        },
        state = state,
        sendCommand = { command, arg ->
            viewModel.sendCommand(nodeId = nodeId, command = command, arg = arg)
        },
        sendCustomCommand = { command, arg ->
            viewModel.sendCustomCommand(nodeId = nodeId, command = command, arg = arg)
        }
    )
}

@Composable
fun ExtConfigScreen(
    modifier: Modifier,
    deviceName: String,
    state: ExtConfigCommandAnswers,
    dialogResult: DialogResult? = null,
    onDismissInfoDialog: () -> Unit = { /** NOOP **/ },
    onDismissErrorDialog: () -> Unit = { /** NOOP **/ },
    onDismissResultDialog: () -> Unit = { /** NOOP **/ },
    goToFwDownload: () -> Unit = { /** NOOP **/ },
    goToCertRequest: () -> Unit = { /** NOOP **/ },
    goToCertRegistration: () -> Unit = { /** NOOP **/ },
    sendCommand: (Command, Any?) -> Unit = { _, _ -> /** NOOP **/ },
    sendCustomCommand: (CustomCommand, Any?) -> Unit = { _, _ -> /** NOOP **/ }
) {
    val content: @Composable() (ColumnScope.() -> Unit) = {
        BoardReportCard(
            showGetUID = state.commandNameList.contains(Command.READ_UID),
            onGetUID = { sendCommand(Command.READ_UID, null) },
            showGetVersionFirmware = state.commandNameList.contains(Command.READ_VERSION_FW),
            onGetVersionFirmware = { sendCommand(Command.READ_VERSION_FW, null) },
            showGetInfo = state.commandNameList.contains(Command.READ_INFO),
            onGetInfo = { sendCommand(Command.READ_INFO, null) },
            showGetHelp = state.commandNameList.contains(Command.READ_HELP),
            onGetHelp = { sendCommand(Command.READ_HELP, null) },
            showGetPowerStatus = state.commandNameList.contains(Command.READ_POWER_STATUS),
            onGetPowerStatus = { sendCommand(Command.READ_POWER_STATUS, null) }
        )

        BoardSecurityCard(
            showChangePin = state.commandNameList.contains(Command.CHANGE_PIN),
            onChangePin = { sendCommand(Command.CHANGE_PIN, it) },
            showClearDB = state.commandNameList.contains(Command.CLEAR_DB),
            onClearDB = { sendCommand(Command.CLEAR_DB, null) },
            showCertRegistration = state.commandNameList.contains(Command.SET_CERTIFICATE),
            onCertRegistration = goToCertRegistration,
            showCertRequest = state.commandNameList.contains(Command.READ_CERTIFICATE),
            onCertRequest = goToCertRequest
        )

        val showSwap =
            state.commandNameList.contains(Command.BANKS_SWAP) &&
                    state.banksStatus?.fwId2?.isEmpty() == false
        BoardControlCard(
            showDFU = state.commandNameList.contains(Command.SET_DFU),
            onDFU = { sendCommand(Command.SET_DFU, null) },
            showOff = state.commandNameList.contains(Command.POWER_OFF),
            onOff = { sendCommand(Command.POWER_OFF, null) },
            showFwDownload = state.commandNameList.contains(Command.BANKS_STATUS),
            onFwDownload = goToFwDownload,
            showSwap = showSwap,
            onSwap = { sendCommand(Command.BANKS_SWAP, null) }
        )

        BoardSettingsCard(
            deviceName = deviceName,
            showSetName = state.commandNameList.contains(Command.SET_NAME),
            onSetName = { name -> sendCommand(Command.SET_NAME, name.padEnd(NAME_PAD_END_LEN)) },
            showReadCustomCommand = state.commandNameList.contains(Command.READ_CUSTOM_COMMANDS),
            onReadCustomCommand = { sendCommand(Command.READ_CUSTOM_COMMANDS, null) },
            showSetTime = state.commandNameList.contains(Command.SET_TIME),
            onSetTime = { sendCommand(Command.SET_TIME, null) },
            showSetDate = state.commandNameList.contains(Command.SET_DATE),
            onSetDate = { sendCommand(Command.SET_DATE, null) },
            showSensorConfiguration = false, // state.commandNameList.contains(Command.READ_SENSORS),
            onSensorConfiguration = { /** TODO **/ },
            showSetWiFiCredentials = state.commandNameList.contains(Command.SET_WIFI),
            onSetWiFiCredentials = { sendCommand(Command.SET_WIFI, it) }
        )

        BoardCustomCommandsCard(
            commands = state.customCommandList ?: emptyList(),
            onSendCommand = { command, args -> sendCustomCommand(command, args) }
        )
    }
    Column(modifier = modifier.verticalScroll(state = rememberScrollState()), content = content)

    if (state.error.isNullOrEmpty().not()) {
        AlertDialog(
            onDismissRequest = onDismissErrorDialog,
            title = { Text(text = stringResource(id = R.string.st_extConfig_customCommands_error)) },
            text = { Text(text = state.error ?: "") },
            confirmButton = {
                TextButton(
                    onClick = onDismissErrorDialog
                ) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }

    if (state.info.isNullOrEmpty().not()) {
        AlertDialog(
            onDismissRequest = onDismissInfoDialog,
            title = { Text(text = stringResource(id = R.string.st_extConfig_customCommands_info)) },
            text = { Text(text = state.info ?: "") },
            confirmButton = {
                TextButton(onClick = onDismissInfoDialog) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }

    if (dialogResult != null) {
        AlertDialog(
            onDismissRequest = onDismissResultDialog,
            title = { Text(text = dialogResult.title) },
            text = { Text(text = dialogResult.text) },
            confirmButton = {
                TextButton(onClick = onDismissResultDialog) {
                    Text(text = stringResource(id = android.R.string.ok))
                }
            }
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun ExtConfigScreenPreview() {
    BlueMSTheme {
        ExtConfigScreen(
            modifier = Modifier.fillMaxSize(),
            deviceName = "",
            state = ExtConfigCommandAnswers()
        )
    }
}
