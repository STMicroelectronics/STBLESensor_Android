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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.West
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.st.high_speed_data_log.composable.StopLoggingDialog
import com.st.high_speed_data_log.model.StreamData
import com.st.pnpl.composable.PnPLInfoWarningSpontaneousMessage
import com.st.ui.composables.BlueMSAnimatedIconsRow
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.BlueMSSnackBarMaterial3
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIoTCraftHighSpeedDataLog(
    modifier: Modifier,
    viewModel: AIoTCraftHighSpeedDataLogViewModel,
    nodeId: String
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_DESTROY -> {
                viewModel.stopDemo(nodeId = nodeId)
            }

            Lifecycle.Event.ON_CREATE -> {
                viewModel.startDemo(nodeId = nodeId)
            }

            else -> Unit
        }
    }

    val isLogging by viewModel.isLogging.collectAsStateWithLifecycle()
    val sensors by viewModel.sensors.collectAsStateWithLifecycle()
    val streamSensors by viewModel.streamSensors.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val status by viewModel.componentStatusUpdates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSDCardInserted by viewModel.isSDCardInserted.collectAsStateWithLifecycle()
    val acquisitionName by viewModel.acquisitionName.collectAsStateWithLifecycle()
    val vespucciTags by viewModel.vespucciTags.collectAsStateWithLifecycle()
    val vespucciTagsActivation by viewModel.vespucciTagsActivation.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isConnectionLost by viewModel.isConnectionLost.collectAsStateWithLifecycle()
    val currentSensorEnabled by viewModel.currentSensorEnabled.collectAsStateWithLifecycle()
    val streamData by viewModel.streamData.collectAsStateWithLifecycle()
    val enableLog by viewModel.enableLog.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    var ucfError: String? by remember { mutableStateOf(value = null) }

    val showedTelemetryPlotExplanation by viewModel.showedTelemetryPlotExplanation.collectAsStateWithLifecycle()

    var showTelemetryPlotExplanation by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = showedTelemetryPlotExplanation) {
        if (showedTelemetryPlotExplanation.not()) {
            showTelemetryPlotExplanation = true
        }
    }


    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    AIoTCraftHighSpeedDataLog(
        modifier = modifier,
        snackbarMessage = snackbarMessage,
        sensors = sensors,
        streamSensors = streamSensors,
        tags = tags,
        isBeta = viewModel.isBeta,
        status = status,
        enableLog = enableLog,
        vespucciTagsActivation = vespucciTagsActivation,
        isSDCardInserted = isSDCardInserted,
        currentSensorEnabled = currentSensorEnabled,
        streamData = streamData,
        isLogging = isLogging,
        isLoading = isLoading,
        vespucciTags = vespucciTags,
        acquisitionName = acquisitionName,
        onTagChangeState = { tag, newState ->
            viewModel.onTagChangeState(nodeId, tag, newState)
        },
        onValueChange = { name, value ->
            if (isLoading.not()) {
                viewModel.sendChange(
                    nodeId = nodeId,
                    name = name,
                    value = value
                )
            }
        },
        onBeforeUcf = { viewModel.setEnableStopDemo(false) },
        onAfterUcf = {},
        onErrorUcf = { error ->
            ucfError = error
        },
        onClearMessage = { viewModel.cleanMessage() },
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
        },
        onSensorSelected = {
            viewModel.enableStreamSensor(nodeId = nodeId, sensor = it)
        }
    )

    if (showTelemetryPlotExplanation && isLogging) {
        ModalBottomSheet(
            onDismissRequest = {
                showTelemetryPlotExplanation = false
            },
            sheetState = sheetState,
        ) {
            TelemetryPlotExplanation(onDismissShowTelemetryPlotExplanation = {
                showTelemetryPlotExplanation = false
            }, onDontShowAgain = { viewModel.setShowedTelemetryPlotExplanation(it) })
        }
    }

    statusMessage?.let {
        PnPLInfoWarningSpontaneousMessage(
            messageType = statusMessage!!,
            onDismissRequest = { viewModel.cleanStatusMessage() })
    }

    ucfError?.let { error ->
        BasicAlertDialog(onDismissRequest = { ucfError = null })
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
                        text = "Error Sending UCF:",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        modifier = Modifier.padding(start = LocalDimensions.current.paddingMedium),
                        text = error,
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
                        horizontalArrangement = Arrangement.End
                    ) {

                        BlueMsButton(
                            modifier = Modifier.padding(end = LocalDimensions.current.paddingSmall),
                            text = "OK",
                            onClick = {
                                ucfError = null
                            }
                        )
                    }
                }
            }
        }
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
fun AIoTCraftHighSpeedDataLog(
    modifier: Modifier,
    snackbarMessage: String? = null,
    sensors: List<ComponentWithInterface> = emptyList(),
    streamSensors: List<ComponentWithInterface> = emptyList(),
    tags: List<ComponentWithInterface> = emptyList(),
    isBeta: Boolean = false,
    streamData: StreamData? = null,
    status: List<JsonObject>,
    vespucciTagsActivation: List<String>,
    vespucciTags: Map<String, Boolean>,
    enableLog: Boolean,
    isLogging: Boolean,
    isSDCardInserted: Boolean = false,
    isLoading: Boolean = false,
    currentSensorEnabled: String = "",
    acquisitionName: String = "",
    onSensorSelected: (String) -> Unit,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onBeforeUcf: () -> Unit,
    onClearMessage: () -> Unit,
    onAfterUcf: () -> Unit,
    onErrorUcf: (String) -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    onTagChangeState: (String, Boolean) -> Unit = { _, _ -> /**NOOP**/ },
    onStartStopLog: (Boolean) -> Unit = { /**NOOP **/ },
    onRefresh: () -> Unit = { /**NOOP **/ }
) {
    val sensorsTitle = stringResource(id = R.string.st_hsdl_sensors)
    val tagsTitle = stringResource(id = R.string.st_hsdl_tags)
    var currentTitle by remember { mutableStateOf(sensorsTitle) }
    var openStopDialog by remember { mutableStateOf(value = false) }
//    var openResetDialog by remember { mutableStateOf(value = false) }

    val pullRefreshState = rememberPullToRefreshState()


    val backState =
        rememberNavBackStack(if (isLogging) AIoTHsdlTagsNavKey else AIoTHsdlSensorsNavKey)

    val lastState = backState.lastOrNull()
    val selectedIndex by remember(key1 = lastState) {
        derivedStateOf {
            if (lastState == AIoTHsdlTagsNavKey) 1 else 0
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    val haptic = LocalHapticFeedback.current

    val context = LocalContext.current

    val lazyState = rememberLazyListState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = {
            BlueMSSnackBarMaterial3(
//                modifier = Modifier.padding(
//                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
//                ),
                snackBarHostState = snackBarHostState
            )
        },
        topBar = {
            Column {
                HsdlConfig.hsdlTabBar?.invoke(
                    currentTitle,
                    isLoading,
                    vespucciTagsActivation.isEmpty()
                ) {
                    if (HsdlConfig.isVespucci) {
                        if (vespucciTagsActivation.isEmpty()) {
                            onStartStopLog(false)
                        } else {
                            Toast.makeText(
                                context,
                                "Data collection or tagging should be at least 5 seconds long.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        onStartStopLog(false)
                    }
                }
                PrimaryTabRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    selectedTabIndex = selectedIndex,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = selectedIndex,
                                matchContentSize = false
                            ),
                            width = 60.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            height = 4.dp,
                            shape = Shapes.medium
                        )
                    }) {
                    Tab(
                        selected = 0 == selectedIndex,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentTitle = sensorsTitle
                            backState.removeLastOrNull()
                            backState.add(AIoTHsdlSensorsNavKey)
                        },
                        icon = {

                            Icon(
                                painter = painterResource(id = R.drawable.ic_sensors),
                                contentDescription = stringResource(id = R.string.st_hsdl_sensors)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.st_hsdl_sensors)) },
                        enabled = !isLoading
                    )

                    Tab(
                        selected = 1 == selectedIndex,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            currentTitle = tagsTitle
                            backState.removeLastOrNull()
                            backState.add(AIoTHsdlTagsNavKey)
                        },
                        icon = {

                            Icon(
                                painter = painterResource(id = R.drawable.ic_tags),
                                contentDescription = stringResource(id = R.string.st_hsdl_tags)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.st_hsdl_tags)) },
                        enabled = !isLoading
                    )
                }
            }
        },
        floatingActionButton = {
//            if (showedTelemetryPlotExplanation || !isLogging) {
            ExtendedFloatingActionButton(
                modifier = Modifier.padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding()
                ),
                containerColor = SecondaryBlue,
                expanded = !lazyState.isScrollInProgress,
                onClick = {
                    if (isSDCardInserted) {
                        if (isLogging) {
                            if (HsdlConfig.isVespucci) {
                                if (vespucciTagsActivation.isEmpty()) {
                                    onStartStopLog(false)
                                    openStopDialog = HsdlConfig.showStopDialog
//                                        openResetDialog = HsdlConfig.showResetDialog
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Data collection or tagging should be at least 5 seconds long",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                onStartStopLog(false)
                                openStopDialog = HsdlConfig.showStopDialog
//                                    openResetDialog = HsdlConfig.showResetDialog
                            }
                        } else {
                            if (enableLog) {
                                onStartStopLog(true)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Enable at least one sensor",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please insert a microSD card into the board to proceed.",
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
            //}
        }
    ) { paddingValues ->
        BlueMSPullToRefreshBox(
            modifier = modifier
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues),
            state = pullRefreshState,
            isBetaRelease = isBeta,
            isRefreshing = isLoading,
            onRefresh = onRefresh
        ) {
            NavDisplay(
                backStack = backState,
                onBack = { backState.removeLastOrNull() },
                entryProvider = entryProvider {
                    AIoTCraftHsdlSensorsOrChartScreen(
                        isLogging,
                        sensors,
                        lazyState,
                        status,
                        isLoading,
                        onValueChange,
                        onAfterUcf,
                        onBeforeUcf,
                        onErrorUcf,
                        onSendCommand,
                        streamSensors,
                        streamData,
                        currentSensorEnabled,
                        vespucciTags,
                        onSensorSelected
                    )

                    AIoTCraftHsdlTagsScreen(
                        lazyState,
                        tags,
                        status,
                        isLoading,
                        onValueChange,
                        onSendCommand,
                        acquisitionName,
                        vespucciTagsActivation,
                        vespucciTags,
                        isLogging,
                        onTagChangeState
                    )
                }
            )
        }
    }

    snackbarMessage?.let { message ->
        onClearMessage()
        coroutineScope.launch {
            snackBarHostState.showSnackbar(
                message = message,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Indefinite
            )
        }
    }

    if (openStopDialog) {
        StopLoggingDialog(
            onDismissRequest = { openStopDialog = false }
        )
    }

//    if (openResetDialog) {
//        ResetBoardDialog(
//            onDismissRequest = {
//                openResetDialog = false
//            },
//            onRestartRequest = {
//                openResetDialog = false
//
//                GlobalConfig.navigateBack?.invoke(nodeId)
//            }
//        )
//    }
}

@Composable
fun TelemetryPlotExplanation(
    modifier: Modifier = Modifier,
    onDismissShowTelemetryPlotExplanation: () -> Unit = { /**NOOP **/ },
    onDontShowAgain: (Boolean) -> Unit = { /** NOOP **/ }
) {

    var checkbox by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = LocalDimensions.current.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBlue,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            text = "Data Storage & Streaming"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = LocalDimensions.current.paddingNormal,
                    start = LocalDimensions.current.paddingNormal,
                    end = LocalDimensions.current.paddingNormal
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium,
                text = "Micro-SD Card"
            )

            Text(
                color = PrimaryBlue,
                style = MaterialTheme.typography.titleMedium,
                text = "Live Chart"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(LocalDimensions.current.iconNormal),
                tint = PrimaryBlue,
                imageVector = Icons.Filled.SdCard,
                contentDescription = null
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall)
            ) {
                BlueMSAnimatedIconsRow(
                    tint = SuccessText,
                    durationMillis = 800,
                    imageVector = Icons.Filled.SdCard, isToLeft = true
                )

                Icon(
                    modifier = Modifier.size(18.dp),
                    tint = SuccessText,
                    imageVector = Icons.Default.West,
                    contentDescription = null
                )
            }

            Icon(
                modifier = Modifier.size(LocalDimensions.current.iconNormal),
                tint = PrimaryBlue,
                imageVector = Icons.Filled.Memory,
                contentDescription = null
            )

            Column(
                modifier = Modifier.padding(bottom = LocalDimensions.current.paddingNormal),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingSmall)
            ) {
                BlueMSAnimatedIconsRow(
                    tint = SuccessText,
                    durationMillis = 2000,
                    imageVector = Icons.Filled.SdCard,
                    isToLeft = false
                )

                Icon(
                    modifier = Modifier.size(18.dp),
                    tint = SuccessText,
                    imageVector = Icons.Default.East,
                    contentDescription = null
                )
            }

            Icon(
                modifier = Modifier.size(LocalDimensions.current.iconNormal),
                tint = PrimaryBlue,
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null
            )

        }

        Text(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
            text = "Please understand how we handle sensors data logs when stored on micro-SD or streamed to the phone:",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic
        )

        Text(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("\u2022 Micro-SD Card:")
                }
                append(" all sensor data logs are saved to the microSD card at")
                append(" the selected Output Data Rate,")
                append(" without data loss.")
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("\u2022 Live Chart:")
                }
                append(" the graph displayed")
                append(" in this app represents only a preview.")
                append(" The displayed data may not include all samples due to Bluetooth bandwidth limits.")
            },
            style = MaterialTheme.typography.bodyMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Checkbox(
                checked = checkbox,
                onCheckedChange = {
                    checkbox = it
                    onDontShowAgain(it)
                }
            )
            Text(
                modifier = Modifier.padding(start = LocalDimensions.current.paddingNormal),
                text = "Don't show again",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        Spacer(
            Modifier.windowInsetsBottomHeight(
                WindowInsets.navigationBars
            )
        )
    }
}


fun String.formatDate(): String {
    val inputSdf = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.ROOT)
    val date = inputSdf.parse(this)
    val sdf = SimpleDateFormat("EEE MMM d yyyy HH:mm:ss", Locale.UK)
    return sdf.format(date)
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun TelemetryPlotExplanationPreview() {
    PreviewBlueMSTheme {
        TelemetryPlotExplanation()
    }
}

