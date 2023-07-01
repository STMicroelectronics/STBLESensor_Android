/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.services.ota.FirmwareType
import com.st.blue_sdk.utils.WbOTAUtils
import com.st.ext_config.R
import com.st.ext_config.model.FwUpdateState
import com.st.ext_config.ui.fw_upgrade.FwUpgradeViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.Grey6

@Composable
fun FwUpgradeScreen(
    modifier: Modifier,
    viewModel: FwUpgradeViewModel,
    nodeId: String,
    fwUrl: String
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId, fwUrl)
            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        if (fileUri != null) {
            viewModel.changeFile(uri = fileUri)
        }
    }

    val fwUpdateState by viewModel.fwUpdateState.collectAsStateWithLifecycle()
    FwUpgradeScreen(
        modifier = modifier,
        state = fwUpdateState,
        wbOta = viewModel.isWbOta(nodeId = nodeId),
        onDismissSuccessDialog = {
            viewModel.clearFwUpdateState()
        },
        onDismissErrorDialog = {
            viewModel.clearFwUpdateState()
        },
        onChangeFile = {
            //pickFileLauncher.launch(arrayOf("application/octet-stream"))
            pickFileLauncher.launch(arrayOf("*/*"))
        },
        onStartUploadFile = { boardType, fwType ->
            viewModel.startUpgradeFW(nodeId, boardType, fwType)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FwUpgradeScreen(
    modifier: Modifier = Modifier,
    state: FwUpdateState,
    wbOta: Boolean = false,
    onChangeFile: () -> Unit = { /** NOOP **/ },
    onDismissSuccessDialog: () -> Unit = { /** NOOP **/ },
    onDismissErrorDialog: () -> Unit = { /** NOOP **/ },
    onStartUploadFile: (WbOTAUtils.WBBoardType?, FirmwareType?) -> Unit = { _, _ -> /** NOOP **/ }
) {
    var radioSelection by remember { mutableStateOf(value = 0) }
    var selectedBoardIndex by remember { mutableStateOf(value = 0) }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(
                        id = R.string.st_extConfig_fwUpgrade_nameLabel,
                        state.boardInfo?.boardName ?: ""
                    )
                )
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(
                        id = R.string.st_extConfig_fwUpgrade_versionLabel,
                        state.boardInfo?.toString() ?: ""
                    )
                )
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(
                        id = R.string.st_extConfig_fwUpgrade_mcuLabel,
                        state.boardInfo?.mcuType ?: ""
                    )
                )

                if (wbOta) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        BoardDropdown(selectedIndex = selectedBoardIndex) { boardIndex ->
                            selectedBoardIndex = boardIndex
                        }

                        val radioOptions = listOf(
                            stringResource(id = R.string.st_extConfig_fwUpgrade_otaOpt1),
                            stringResource(id = R.string.st_extConfig_fwUpgrade_otaOpt2)
                        )
                        radioOptions.forEachIndexed { index, text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = (index == radioSelection),
                                        onClick = {
                                            radioSelection = index
                                        }
                                    )
                                    .padding(horizontal = LocalDimensions.current.paddingNormal),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (index == radioSelection),
                                    onClick = { radioSelection = index }
                                )
                                Text(
                                    color = Grey6,
                                    style = MaterialTheme.typography.bodyLarge,
                                    text = text,
                                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal),
            shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
            shadowElevation = LocalDimensions.current.elevationNormal,
            onClick = onChangeFile
        ) {
            Column(modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal)) {
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = stringResource(id = R.string.st_extConfig_fwUpgrade_selectedLabel)
                )
                Text(
                    color = Grey6,
                    style = MaterialTheme.typography.bodyLarge,
                    text = state.fwName
                )
            }
        }

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(weight = 1f))

            BlueMsButton(
                enabled = state.downloadFinished,
                text = stringResource(id = R.string.st_extConfig_fwUpgrade_upgradeBtn),
                onClick = {
                    if (wbOta) {
                        val boardType = if (selectedBoardIndex == 0) {
                            WbOTAUtils.WBBoardType.WB5xOrWB3x
                        } else {
                            WbOTAUtils.WBBoardType.WB1x
                        }
                        val firmwareType = if (radioSelection == 0) {
                            FirmwareType.BOARD_FW
                        } else {
                            FirmwareType.BLE_FW
                        }

                        onStartUploadFile(boardType, firmwareType)
                    } else {
                        onStartUploadFile(null, null)
                    }
                },
                iconPainter = painterResource(id = R.drawable.ic_file_upload)
            )
        }
    }

    if (state.isComplete) {
        FwUpgradeSuccessDialog(
            onPositiveButtonPressed = onDismissSuccessDialog,
            seconds = state.duration
        )
    }

    if (state.isInProgress) {
        FwUpdateProgressDialog(progress = state.progress)
    }

    if (state.error != null) {
        FwUpgradeErrorDialog(
            fwUploadError = state.error,
            onPositiveButtonPressed = onDismissErrorDialog
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun FwUpgradeScreenPreview() {
    PreviewBlueMSTheme {
        FwUpgradeScreen(state = FwUpdateState())
    }
}

@Preview(showBackground = true)
@Composable
private fun FwUpgradeScreenWbOtaPreview() {
    PreviewBlueMSTheme {
        FwUpgradeScreen(wbOta = true, state = FwUpdateState())
    }
}
