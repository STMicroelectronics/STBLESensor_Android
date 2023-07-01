/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.composable

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.features.extended.ext_configuration.BanksStatus
import com.st.ext_config.R
import com.st.ext_config.ui.fw_download.FwDownloadFragmentDirections
import com.st.ext_config.ui.fw_download.FwDownloadUiState
import com.st.ext_config.ui.fw_download.FwDownloadViewModel
import com.st.ui.composables.*
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6

@Composable
fun FwDownloadScreen(
    modifier: Modifier = Modifier,
    nodeId: String,
    viewModel: FwDownloadViewModel,
    navController: NavController
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId = nodeId)
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

@Composable
fun FwDownloadScreen(
    modifier: Modifier = Modifier,
    state: FwDownloadUiState,
    onInstallClick: (String?) -> Unit = { /** NOOP **/ },
    onSwap: () -> Unit = { /** NOOP **/ },
    onCancelClick: () -> Unit = { /** NOOP **/ }
) {
    val compatibleList by remember(key1 = state) {
        derivedStateOf { state.node?.fwCompatibleList ?: emptyList() }
    }
    var selectedFw: Int by remember { mutableStateOf(value = 0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
    ) {
        CurrentBank(
            modifier = Modifier.fillMaxWidth(),
            currentBank = state.bankStatus?.currentBank,
            currentFwDetail = state.currentFwDetail?.friendlyName()
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        OtherBank(
            modifier = Modifier.fillMaxWidth(),
            otherFwDetail = state.otherFwDetail?.friendlyName(),
            onSwap = onSwap
        )

        if (state.updateFwDetail != null) {
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Text(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                text = stringResource(id = R.string.st_extConfig_fwDownload_updateAvailableTitle)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = SecondaryBlue,
                style = MaterialTheme.typography.bodyLarge,
                text = state.updateFwDetail.friendlyName()
            )
            Text(
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                text = stringResource(id = R.string.st_extConfig_fwDownload_changeLogTitle)
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                color = SecondaryBlue,
                style = MaterialTheme.typography.bodyLarge,
                text = state.updateFwDetail.changelog ?: ""
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingLarge))
            Divider()

            LaunchedEffect(key1 = state.updateFwDetail) {
                selectedFw = compatibleList.indexOfFirst {
                    it.bleDevId == state.updateFwDetail.bleDevId && it.bleFwId == state.updateFwDetail.bleFwId
                }
            }
        }

        if (compatibleList.isNotEmpty()) {
            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Text(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                text = stringResource(id = R.string.st_extConfig_fwDownload_compatibleFwLabel)
            )

            EnumPropertyFw(
                data = null,
                modifier = Modifier.fillMaxWidth(),
                initialValue = selectedFw,
                values = compatibleList.mapIndexed { index, fw -> fw.friendlyName() to index },
                onValueChange = {
                    selectedFw = it
                }
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Text(
                color = Grey6,
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(id = R.string.st_extConfig_fwDownload_descriptionLabel)
            )
            Text(
                color = Grey6,
                style = MaterialTheme.typography.bodyLarge,
                text = compatibleList[selectedFw].fwDesc
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            Row(modifier = Modifier.fillMaxWidth()) {
                BlueMsButtonOutlined(
                    modifier = Modifier.weight(weight = 0.5f),
                    text = stringResource(id = android.R.string.cancel),
                    onClick = onCancelClick
                )

                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                BlueMsButton(
                    modifier = Modifier.weight(weight = 0.5f),
                    text = stringResource(
                        id = R.string.st_extConfig_fwDownload_installBtn,
                        compatibleList[selectedFw].friendlyName()
                    ),
                    onClick = {
                        onInstallClick(
                            compatibleList[selectedFw].fota.fwUrl,
                        )
                    }
                )
            }
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
    var internalState by rememberSaveable(initialValue, lastStatusUpdatedAt) {
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
                Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
                DropdownMenu(expanded = expanded, modifier = Modifier.fillMaxWidth(0.9f),onDismissRequest = {
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
