/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.high_speed_data_log

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.high_speed_data_log.composable.HsdlSensors
import com.st.pnpl.composable.PnPLInfoWarningSpontaneousMessage
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import kotlinx.serialization.json.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHighSpeedDataLog(
    modifier: Modifier,
    viewModel: SimpleHighSpeedDataLogViewModel,
    nodeId: String
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                viewModel.stopDemo(nodeId = nodeId)
            }

            Lifecycle.Event.ON_START -> {
                viewModel.startDemo(nodeId = nodeId)
            }

            else -> Unit
        }
    }

    val isLogging by viewModel.isLogging.collectAsStateWithLifecycle()
    val sensors by viewModel.sensors.collectAsStateWithLifecycle()
    val status by viewModel.componentStatusUpdates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSDCardInserted by viewModel.isSDCardInserted.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isConnectionLost by viewModel.isConnectionLost.collectAsStateWithLifecycle()

    SimpleHighSpeedDataLog(
        modifier = modifier,
        sensors = sensors,
        status = status,
        isSDCardInserted = isSDCardInserted,
        isLogging = isLogging,
        isLoading = isLoading,
        isBetaApplication = viewModel.isBeta,
        onValueChange = { name, value ->
            if (isLoading.not()) {
                viewModel.sendChange(
                    nodeId = nodeId,
                    name = name,
                    value = value
                )
            }
        },
        onBeforeUcf = { },
        onAfterUcf = {},
        onSendCommand = { name, value ->
            if (isLoading.not()) {
                viewModel.sendCommand(
                    nodeId = nodeId,
                    name = name,
                    commandRequest = value
                )
            }
        },
        onStartStopLog = {
            if (it) {
                if (isLogging.not() && isLoading.not()) {
                    viewModel.startLog(nodeId)
                }
            } else {
                viewModel.stopLog(nodeId)
            }
        },
        onRefresh = {
            if (isLogging.not() && isLoading.not()) {
                viewModel.refresh(nodeId)
            }
        }
    )

    statusMessage?.let {
        PnPLInfoWarningSpontaneousMessage(
            messageType = statusMessage!!,
            onDismissRequest = { viewModel.cleanStatusMessage() })
    }

    if (isConnectionLost) {
        BasicAlertDialog(onDismissRequest = { viewModel.resetConnectionLost() })
        {
            Surface(
                modifier = Modifier
                    //.wrapContentWidth()
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = Shapes.medium
            ) {
                Column(modifier = Modifier.padding(all = LocalDimensions.current.paddingMedium)) {
                    Text(
                        text = "Warning:",
                        modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        letterSpacing = 0.15.sp,
                        color = ErrorText
                    )
                    Text(
                        text = "Lost Connection with the Node",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                start = LocalDimensions.current.paddingSmall,
                                end = LocalDimensions.current.paddingSmall,
                                top = LocalDimensions.current.paddingNormal
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        BlueMsButton(
                            modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                            text = "Close",
                            onClick = {
                                viewModel.disconnect(nodeId = nodeId)
                            }
                        )

                        BlueMsButton(
                            modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                            text = "Cancel",
                            onClick = {
                                viewModel.resetConnectionLost()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHighSpeedDataLog(
    modifier: Modifier,
    sensors: List<ComponentWithInterface> = emptyList(),
    status: List<JsonObject>,
    isLogging: Boolean,
    isBetaApplication: Boolean,
    isSDCardInserted: Boolean = false,
    isLoading: Boolean = false,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onBeforeUcf: () -> Unit,
    onAfterUcf: () -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    onStartStopLog: (Boolean) -> Unit = { /**NOOP **/ },
    onRefresh: () -> Unit = { /**NOOP **/ }
) {
    val pullRefreshState = rememberPullToRefreshState()

    val context = LocalContext.current
    val lazyState = rememberLazyListState()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButtonPosition = FabPosition.Center,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                containerColor = SecondaryBlue,
                expanded = !lazyState.isScrollInProgress,
                onClick = {
                    if (isSDCardInserted) {
                        if (isLogging) {
                            onStartStopLog(false)
                        } else {
                            onStartStopLog(true)
                        }
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.st_hsdl_missingSdCard),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                icon = {
                    Icon(
                        tint = PrimaryBlue,
                        imageVector = if (isLogging) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                },
                text = { Text(text = if (isLogging) "Stop" else "Start") },
            )
        }
    ) { paddingValues ->
        BlueMSPullToRefreshBox(
            modifier = modifier.consumeWindowInsets(paddingValues),
            state = pullRefreshState,
            isRefreshing = isLoading,
            isBetaRelease = isBetaApplication,
            indicatorAlignment = Alignment.Center,
            onRefresh = onRefresh
        ) {
            if (isLogging.not()) {
                HsdlSensors(
                    state = lazyState,
                    sensors = sensors,
                    status = status,
                    isLoading = isLoading,
                    onValueChange = onValueChange,
                    onAfterUcf = onAfterUcf,
                    onBeforeUcf = onBeforeUcf,
                    onSendCommand = onSendCommand
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(id = R.string.st_hsdl_logging))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}