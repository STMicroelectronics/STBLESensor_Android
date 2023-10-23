/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.blue_sdk.board_catalog.models.BoardFotaType
import com.st.blue_sdk.bt.advertise.getFwInfo
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.blue_sdk.models.OTAMemoryAddress
import com.st.blue_sdk.services.ota.FirmwareType
import com.st.blue_sdk.services.ota.characteristic.CharacteristicFwUpgrade
import com.st.blue_sdk.utils.WbOTAUtils
import com.st.ext_config.R
import com.st.ext_config.model.FwUpdateState
import com.st.ext_config.ui.fw_upgrade.FwUpgradeViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.ErrorText
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
    val errorMessageCodeValue by viewModel.errorMessageCode.collectAsState()
    FwUpgradeScreen(
        modifier = modifier,
        state = fwUpdateState,
        wbOta = viewModel.isWbOta(nodeId = nodeId),
        isRebootedYet = viewModel.isRebootedYet(nodeId = nodeId),
        fileSize = fwUpdateState.fwSize.toLongOrNull(),
        errorMessageCode = errorMessageCodeValue,
        boardModel = viewModel.boardModel(nodeId = nodeId),
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
        onStartUploadFile = { boardType, fwType, address, nbSectorsToErase ->
            var canStart = true
            var errorMessageCode = -1
            if(boardType != null && fwType != null) { // wbOta
                errorMessageCode = WbOTAUtils.checkAddressAndNbSectorsInRange(boardType, fwType, address, nbSectorsToErase)
                canStart = (errorMessageCode == -1)
            }
            viewModel.changeErrorMessageCode(errorMessageCode)
            if(canStart) {
                viewModel.startUpgradeFW(nodeId, boardType, fwType, address, nbSectorsToErase)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FwUpgradeScreen(
    modifier: Modifier = Modifier,
    state: FwUpdateState,
    wbOta: Boolean = false,
    isRebootedYet: Boolean = true,
    fileSize: Long? = null,
    errorMessageCode: Int = -1,
    boardModel: Boards.Model? = null,
    onChangeFile: () -> Unit = { /** NOOP **/ },
    onDismissSuccessDialog: () -> Unit = { /** NOOP **/ },
    onDismissErrorDialog: () -> Unit = { /** NOOP **/ },
    onStartUploadFile: (WbOTAUtils.WBBoardType?, FirmwareType?, String?, String?) -> Unit = { _, _, _, _ -> /** NOOP **/ }
) {
    val supportedBoardModels = listOf(

        //Boards.Model.WB_BOARD,
        Boards.Model.WB55_NUCLEO_BOARD,
        Boards.Model.WB5M_DISCOVERY_BOARD,
        Boards.Model.WB55_USB_DONGLE_BOARD,
        Boards.Model.WB15_NUCLEO_BOARD,
        Boards.Model.WB1M_DISCOVERY_BOARD,

        //Boards.Model.WBA_BOARD,
        Boards.Model.WBA5X_NUCLEO_BOARD,
        Boards.Model.WBA_DISCOVERY_BOARD,

        Boards.Model.NUCLEO_WB09KE)




    fun getDefaultAddress(boardModel: Boards.Model?, radioSelection: Int, selectedBoardIndex: Int): String {
        val currentBoard = if(boardModel != null && supportedBoardModels.contains(boardModel)) {
            boardModel
        } else {
            when(selectedBoardIndex) {
                //2 -> Boards.Model.WBA_BOARD
                2 -> Boards.Model.WBA_DISCOVERY_BOARD
                //else -> Boards.Model.WB_BOARD
                1 -> Boards.Model.WB15_NUCLEO_BOARD
                else -> Boards.Model.WB55_NUCLEO_BOARD
            }
        }

        return when(currentBoard) {
            //Boards.Model.WB_BOARD -> if(radioSelection == 0) "0x007000" else "0x000000"
            Boards.Model.WB55_NUCLEO_BOARD,
            Boards.Model.WB5M_DISCOVERY_BOARD,
            Boards.Model.WB55_USB_DONGLE_BOARD -> if(radioSelection == 0) "0x007000" else "0x011000"

            Boards.Model.WB15_NUCLEO_BOARD,
            Boards.Model.WB1M_DISCOVERY_BOARD -> "0x007000"

            Boards.Model.WBA5X_NUCLEO_BOARD,
            Boards.Model.WBA_DISCOVERY_BOARD,-> if(radioSelection == 0) "0x080000" else "0x0F6000"
            Boards.Model.NUCLEO_WB09KE -> if(radioSelection == 0) "0x3F800" else "0x07E000"
            else -> "" //shouldn't happen
        }
    }

    var radioSelection by remember { mutableStateOf(value = 0) }
    var selectedBoardIndex by remember { mutableStateOf(value = 0) }

    var address by remember { mutableStateOf(value = getDefaultAddress(boardModel, radioSelection, 0)) }
    val selectFileText = stringResource(id = R.string.st_extConfig_fwUpgrade_selectFile)
    var nbSectorsToErase by remember { mutableStateOf(value = selectFileText) }

    var lastFileSize: Long? = null // this allows to update the number of sectors to erase each time the file changes while still being able to change it manually (textfield)
    var isUploadStarted by remember { mutableStateOf(value = false) }

    fun getCurrentBoardType(): WbOTAUtils.WBBoardType {
        //return if(boardModel == null || !supportedBoardModels.contains(boardModel) || boardModel == Boards.Model.WB_BOARD) {

        val isWB = (boardModel == Boards.Model.WB55_NUCLEO_BOARD) ||
                (boardModel == Boards.Model.WB5M_DISCOVERY_BOARD) ||
                (boardModel == Boards.Model.WB55_USB_DONGLE_BOARD) ||
                (boardModel == Boards.Model.WB15_NUCLEO_BOARD) ||
                (boardModel == Boards.Model.WB1M_DISCOVERY_BOARD)

        return if(boardModel == null || !supportedBoardModels.contains(boardModel) || isWB) {
            when(selectedBoardIndex) {
                0 -> WbOTAUtils.WBBoardType.WB5xOrWB3x
                1 -> WbOTAUtils.WBBoardType.WB1x
                else -> WbOTAUtils.WBBoardType.WBA
            }
        } else {
            when(boardModel) {
                //Boards.Model.WBA_BOARD -> WbOTAUtils.WBBoardType.WBA
                Boards.Model.WBA5X_NUCLEO_BOARD,
                Boards.Model.WBA_DISCOVERY_BOARD -> WbOTAUtils.WBBoardType.WBA

                Boards.Model.NUCLEO_WB09KE -> WbOTAUtils.WBBoardType.WB09
                else -> WbOTAUtils.WBBoardType.WBA
            }
        }
    }

    fun getCurrentFwType(): FirmwareType {
        return if (radioSelection == 0) {
            FirmwareType.BOARD_FW
        } else {
            FirmwareType.BLE_FW
        }
    }

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
                    if(fileSize != null && fileSize != lastFileSize && !isUploadStarted) {
                        lastFileSize = fileSize
                        nbSectorsToErase = WbOTAUtils.getNumberOfSectorsToDelete(
                            getCurrentBoardType(),
                            getCurrentFwType(),
                            fileSize
                        ).toString()
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {

                        val isNotSupportedBoard = (boardModel == null || !supportedBoardModels.contains(boardModel))
                        //val isWb = boardModel == Boards.Model.WB_BOARD
                        val isWb = (boardModel == Boards.Model.WB55_NUCLEO_BOARD) ||
                                (boardModel == Boards.Model.WB5M_DISCOVERY_BOARD) ||
                                (boardModel == Boards.Model.WB55_USB_DONGLE_BOARD) ||
                                (boardModel == Boards.Model.WB15_NUCLEO_BOARD) ||
                                (boardModel == Boards.Model.WB1M_DISCOVERY_BOARD)

                        val isWBA = (boardModel == Boards.Model.WBA5X_NUCLEO_BOARD) ||
                                (boardModel == Boards.Model.WBA_DISCOVERY_BOARD)

                        if(isNotSupportedBoard || isWb) {
                            BoardDropdown(selectedIndex = selectedBoardIndex, wbOnly = isWb) { boardIndex ->
                                selectedBoardIndex = boardIndex
                                address = getDefaultAddress(boardModel, radioSelection, selectedBoardIndex)
                            }
                        }
                        val radioOptions = mutableListOf(
                            stringResource(id = R.string.st_extConfig_fwUpgrade_otaOpt1),
                            //stringResource(id = if(selectedBoardIndex == 2 || boardModel == Boards.Model.WBA_BOARD || boardModel == Boards.Model.NUCLEO_WB09KE) R.string.st_extConfig_fwUpgrade_otaOpt2bis else R.string.st_extConfig_fwUpgrade_otaOpt2)
                            stringResource(id = if(selectedBoardIndex == 2 || isWBA || boardModel == Boards.Model.NUCLEO_WB09KE) R.string.st_extConfig_fwUpgrade_otaOpt2bis else R.string.st_extConfig_fwUpgrade_otaOpt2)
                        )

                        radioOptions.forEachIndexed { index, text ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(selected = (index == radioSelection),
                                        onClick = {
                                            radioSelection = index
                                            address = getDefaultAddress(boardModel, index, selectedBoardIndex)
                                        }
                                    )
                                    .padding(horizontal = LocalDimensions.current.paddingNormal),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (index == radioSelection),
                                    onClick = {
                                        radioSelection = index
                                        address = getDefaultAddress(boardModel, index, selectedBoardIndex)
                                    }
                                )
                                Text(
                                    color = Grey6,
                                    style = MaterialTheme.typography.bodyLarge,
                                    text = text,
                                    modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal)
                                )
                            }
                        }
                        TextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { androidx.compose.material.Text(text = stringResource(id = R.string.st_extConfig_fwUpgrade_address)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(height = 60.dp)
                                .wrapContentHeight(),
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        //if(!(boardModel == Boards.Model.WB_BOARD && isRebootedYet)) {
                        if(!(isWb && isRebootedYet)) {
                            TextField(
                                value = nbSectorsToErase,
                                onValueChange = { nbSectorsToErase = it },
                                label = { androidx.compose.material.Text(text = stringResource(id = R.string.st_extConfig_fwUpgrade_nbSectors)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(height = 60.dp)
                                    .wrapContentHeight(),
                                colors = ExposedDropdownMenuDefaults.textFieldColors()
                            )
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

        val errorMessage = when(errorMessageCode) {
            0 ->  stringResource(id = R.string.st_extConfig_fwUpgrade_errorMessage0)
            1 ->  stringResource(id = R.string.st_extConfig_fwUpgrade_errorMessage1)
            2 -> stringResource(id = R.string.st_extConfig_fwUpgrade_errorMessage2)
            3 -> stringResource(id = R.string.st_extConfig_fwUpgrade_errorMessage3)
            else -> ""
        }

        Text(text = errorMessage, color = ErrorText, modifier = Modifier.padding(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.weight(weight = 1f))

            BlueMsButton(
                enabled = state.downloadFinished,
                text = stringResource(id = R.string.st_extConfig_fwUpgrade_upgradeBtn),
                onClick = {
                    isUploadStarted = true
                    if (wbOta) {
                        val boardType = getCurrentBoardType()
                        val firmwareType = getCurrentFwType()

                        onStartUploadFile(boardType, firmwareType, address, nbSectorsToErase)
                    } else {
                        onStartUploadFile(null, null, null, null)
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