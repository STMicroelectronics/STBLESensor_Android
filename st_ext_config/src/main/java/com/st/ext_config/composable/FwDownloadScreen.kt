/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.FirmwareMaturity
import com.st.blue_sdk.features.extended.ext_configuration.BanksStatus
import com.st.ext_config.R
import com.st.ext_config.ui.fw_download.FwDownloadFragmentDirections
import com.st.ext_config.ui.fw_download.FwDownloadUiState
import com.st.ext_config.ui.fw_download.FwDownloadViewModel
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.composables.LocalLastStatusUpdatedAt
import com.st.ui.theme.DESCRIPTION_MAX_LINES
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey5
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.TITLE_MAX_LINES

@Composable
fun FwDownloadScreen(
    modifier: Modifier = Modifier,
    nodeId: String,
    viewModel: FwDownloadViewModel,
    navController: NavController,
    banksStatus: BanksStatus?
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(
                nodeId = nodeId,
                banksStatus = banksStatus
            )

            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    val state by viewModel.bankStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    FwDownloadScreen(
        modifier = modifier,
        state = state,
        onCancelClick = {
            navController.popBackStack()
        },
        onInstallClick = { url ->
            url?.let {
                navController.navigate(
                    FwDownloadFragmentDirections.actionFwDownloadFragmentToFwUpgrade(
                        nodeId, it
                    )
                )
            }
        },
        onSwap = {

            Toast.makeText(
                context,
                context.getString(R.string.st_extConfig_boardControl_swapToast),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.swapBank(nodeId = nodeId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FwDownloadScreen(
    modifier: Modifier = Modifier,
    state: FwDownloadUiState,
    onInstallClick: (String?) -> Unit = { /** NOOP **/ },
    onSwap: () -> Unit = { /** NOOP **/ },
    onCancelClick: () -> Unit = { /** NOOP **/ }
) {

    var onlyLatest by rememberSaveable { mutableStateOf(value = true) }
    var showListOfCompatibleSw by rememberSaveable { mutableStateOf(false) }

    val compatibleList by remember(key1 = state, key2 = onlyLatest) {
        derivedStateOf {
            val listFw =
                state.node?.fwCompatibleList ?: emptyList()
            if (onlyLatest) {
                listFw.groupBy { it.fwName }
                    .map { list -> list.value.maxByOrNull { it.fwVersion }!! }
            } else {
                listFw
            }
        }
    }

    var selectedFw: Int by remember(key1 = compatibleList) { mutableIntStateOf(value = -1) }

    if (showListOfCompatibleSw) {

        BackHandler {
            showListOfCompatibleSw = false
        }

        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingMedium)
    ) {
            item {
            Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = LocalDimensions.current.paddingNormal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        modifier = Modifier.padding(end = LocalDimensions.current.paddingNormal),
                    color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                        text = "Select one firmware"
                )

                    Row(Modifier.clickable {
                        onlyLatest = !onlyLatest
                    }) {
                    Icon(
                        modifier = Modifier
                            .padding(start = 4.dp),
                        tint = SecondaryBlue,
                        imageVector = if (onlyLatest) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                        contentDescription = null
                    )

                    Text(
                        modifier = Modifier
                            .padding(start = 4.dp),
                        color = if (onlyLatest) SecondaryBlue else Grey5,
                        style = MaterialTheme.typography.titleMedium,
                        text = "latest"
                    )
                    }
                }

                HorizontalDivider()
            }

            itemsIndexed(compatibleList) { index, it ->
                FirmwareListItem(
                    name = it.fwName,
                    version = it.fwVersion,
                    description = it.fwDesc,
                    fwMaturity = it.maturity,
                    onClick = {
                        selectedFw = index
                        showListOfCompatibleSw = false
                    }
                )
            }

            item {
                BlueMsButtonOutlined(
                    text = stringResource(id = android.R.string.cancel),
                    onClick = {
                        showListOfCompatibleSw = false
                        selectedFw = -1
                    }
                )
            }

            item {
                Spacer(
                    Modifier.windowInsetsBottomHeight(
                        WindowInsets.systemBars
                    )
                )
            }
        }
    } else {
        var nextRunningBank by remember { mutableIntStateOf(0) }
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            //Memory Bank1
            MemoryBank(
                modifier = Modifier.fillMaxWidth(),
                installedFwDetail = if (state.bankStatus?.currentBank == 1) {
                    state.currentFwDetail?.friendlyName()
                } else {
                    state.otherFwDetail?.friendlyName()
                },
                onSwap = {
                    nextRunningBank = 1
                    onSwap()
                },
                isCompatibleFwListNotEmpty = compatibleList.isNotEmpty(),
                onDownload = { showListOfCompatibleSw = true },
                onInstall = {
                    if (selectedFw != -1) {
                        onInstallClick(
                            compatibleList[selectedFw].fota.fwUrl
                        )
                    }
                },
                isRunningBank = state.bankStatus?.currentBank == 1,
                nextRunningBank = nextRunningBank,
                bankNum = 1,
                selectedFwName = if (selectedFw != -1) compatibleList[selectedFw].fwName else null,
                selectedFwDescription = if (selectedFw != -1) compatibleList[selectedFw].fwDesc else null
            )


            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            //Memory Bank2
            MemoryBank(
                modifier = Modifier.fillMaxWidth(),
                installedFwDetail = if (state.bankStatus?.currentBank == 1) {
                    state.otherFwDetail?.friendlyName()
                } else {
                    state.currentFwDetail?.friendlyName()
                },
                onSwap = {
                    nextRunningBank = 2
                    onSwap()
                },
                isCompatibleFwListNotEmpty = compatibleList.isNotEmpty(),
                onDownload = { showListOfCompatibleSw = true },
                onInstall = {
                    if (selectedFw != -1) {
                        onInstallClick(
                            compatibleList[selectedFw].fota.fwUrl
                        )
                    }
                },
                isRunningBank = state.bankStatus?.currentBank == 2,
                nextRunningBank = nextRunningBank,
                bankNum = 2,
                selectedFwName = if (selectedFw != -1) compatibleList[selectedFw].fwName else null,
                selectedFwDescription = if (selectedFw != -1) compatibleList[selectedFw].fwDesc else null
            )


            if (compatibleList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

//            EnumPropertyFw(
//                data = null,
//                modifier = Modifier.fillMaxWidth(),
//                initialValue = selectedFw,
//                values = compatibleList.mapIndexed { index, fw -> fw.friendlyName() to index },
//                onValueChange = {
//                    selectedFw = it
//                }
//            )

                BlueMsButtonOutlined(
                    text = stringResource(id = android.R.string.cancel),
                    onClick = onCancelClick
                )

            }

            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.systemBars
                )
            )
        }
    }
}

@Composable
fun FirmwareListItem(
    modifier: Modifier = Modifier,
    name: String,
    description: String,
    version: String,
    onClick: () -> Unit = { /** NOOP **/ },
    fwMaturity: FirmwareMaturity = FirmwareMaturity.RELEASE
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = LocalDimensions.current.paddingNormal)
        ) {
            Text(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                text = name,
                maxLines = TITLE_MAX_LINES,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = LocalDimensions.current.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                    text = "V$version",
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                if (fwMaturity != FirmwareMaturity.RELEASE) {
                    Text(
                        text = "$fwMaturity FW",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        color = ErrorText
                    )
                }
            }
            Text(
                text = description,
                maxLines = DESCRIPTION_MAX_LINES,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun <T : Any> EnumPropertyFw(
    modifier: Modifier = Modifier,
    data: T?,
    label: String = "",
    unit: String = "",
    enabled: Boolean = true,
    color: String = "",
    description: String = "",
    comment: String = "",
    initialValue: T,
    values: List<Pair<String, T>> = emptyList(),
    colors: List<Pair<T, String>> = emptyList(),
    onValueChange: (T) -> Unit
) {
    val lastStatusUpdatedAt = LocalLastStatusUpdatedAt.current
    var internalState by rememberSaveable(data, initialValue, lastStatusUpdatedAt) {
        mutableStateOf(value = data ?: initialValue)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val propName = if (unit.isEmpty()) label else "$label [$unit]"
        Text(text = propName, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingSmall))

        var expanded by remember { mutableStateOf(value = false) }

        Box {
            Row(
                modifier = Modifier.clickable {
                    expanded = !expanded
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text = values.first { it.second == internalState }.first
                Text(text = text)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                DropdownMenu(
                    expanded = expanded,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    onDismissRequest = {
                        expanded = false
                    }) {
                    values.forEach {
                        DropdownMenuItem(
                            onClick = {
                                internalState = it.second
                                if (enabled) {
                                    onValueChange(internalState)
                                }
                                expanded = false
                            },
                            text = {
                                Text(it.first, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun FwDownloadScreenPreview() {
    PreviewBlueMSTheme {
        FwDownloadScreen(
            state = FwDownloadUiState(
                bankStatus = BanksStatus(currentBank = 2, fwId1 = "", fwId2 = "")
            )
        )
    }
}
