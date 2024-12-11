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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.st.core.GlobalConfig
import com.st.high_speed_data_log.composable.HsdlSensors
import com.st.high_speed_data_log.composable.HsdlTags
import com.st.high_speed_data_log.composable.ResetBoardDialog
import com.st.high_speed_data_log.composable.StopLoggingDialog
import com.st.high_speed_data_log.composable.VespucciCharts
import com.st.high_speed_data_log.composable.VespucciHsdlTags
import com.st.high_speed_data_log.model.StreamData
import com.st.pnpl.composable.PnPLInfoWarningSpontaneousMessage
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.CommandRequest
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey10
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
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
    val numActiveSensors by viewModel.numActiveSensors.collectAsStateWithLifecycle()

    AIoTCraftHighSpeedDataLog(
        modifier = modifier,
        nodeId = nodeId,
        sensors = sensors,
        numActiveSensors = numActiveSensors,
        streamSensors = streamSensors,
        tags = tags,
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
        isBetaApplication = viewModel.isBeta,
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
fun AIoTCraftHighSpeedDataLog(
    modifier: Modifier,
    nodeId: String,
    sensors: List<ComponentWithInterface> = emptyList(),
    numActiveSensors: Int,
    streamSensors: List<ComponentWithInterface> = emptyList(),
    tags: List<ComponentWithInterface> = emptyList(),
    streamData: StreamData? = null,
    status: List<JsonObject>,
    vespucciTagsActivation: List<String>,
    vespucciTags: Map<String, Boolean>,
    enableLog: Boolean,
    isLogging: Boolean,
    isBetaApplication: Boolean,
    isSDCardInserted: Boolean = false,
    isLoading: Boolean = false,
    currentSensorEnabled: String = "",
    acquisitionName: String = "",
    onSensorSelected: (String) -> Unit,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onBeforeUcf: () -> Unit,
    onAfterUcf: () -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    onTagChangeState: (String, Boolean) -> Unit = { _, _ -> /**NOOP**/ },
    onStartStopLog: (Boolean) -> Unit = { /**NOOP **/ },
    onRefresh: () -> Unit = { /**NOOP **/ },
    navController: NavHostController = rememberNavController()
) {
    val sensorsTitle = stringResource(id = R.string.st_hsdl_sensors)
    val tagsTitle = stringResource(id = R.string.st_hsdl_tags)
    var currentTitle by remember { mutableStateOf(sensorsTitle) }
    var openStopDialog by remember { mutableStateOf(value = false) }
    var openResetDialog by remember { mutableStateOf(value = false) }

    val pullRefreshState = rememberPullToRefreshState()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val selectedIndex by remember(key1 = currentRoute) {
        derivedStateOf {
            if (currentRoute == "Tags") 1 else 0
        }
    }

    val haptic = LocalHapticFeedback.current

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
                            openStopDialog = HsdlConfig.showStopDialog
                            openResetDialog = HsdlConfig.showResetDialog
                        } else {
                            if (enableLog) {
                                onStartStopLog(true)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.st_hsdl_missingSensors),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
        },
        topBar = {
            Column() {
                if (HsdlConfig.hsdlTabBar != null) {
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
                                    context.getString(R.string.st_hsdl_waitingMinDatalog),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            onStartStopLog(false)
                        }
                    }
                }
                PrimaryTabRow(modifier = Modifier
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
                            shape = RoundedCornerShape(size = LocalDimensions.current.cornerMedium)
                        )
                    }) {
                    Tab(
                        selected = 0 == selectedIndex,
                        onClick = {
                            if (!isLoading) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentTitle = sensorsTitle
                                navController.navigate("Sensors") {
                                    navController.graph.startDestinationRoute?.let { screenRoute ->
                                        popUpTo(screenRoute) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            BadgedBox(badge = {
                                Badge(
                                    containerColor = SecondaryBlue,
                                    contentColor = Grey10
                                ) {
                                    Text(text = "$numActiveSensors")
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sensors),
                                    contentDescription = stringResource(id = R.string.st_hsdl_sensors)
                                )
                            }
                        },
                        text = { Text(text = stringResource(id = R.string.st_hsdl_sensors)) },
                        enabled = !isLoading
                    )

                    Tab(
                        selected = 1 == selectedIndex,
                        onClick = {
                            if (!isLoading) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentTitle = tagsTitle
                                navController.navigate("Tags") {
                                    navController.graph.startDestinationRoute?.let { screenRoute ->
                                        popUpTo(screenRoute) {
                                            saveState = true
                                        }
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            BadgedBox(badge = {
                                Badge(
                                    containerColor = SecondaryBlue,
                                    contentColor = Grey10
                                ) {
                                    Text(text = "${vespucciTags.size}")
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_tags),
                                    contentDescription = stringResource(id = R.string.st_hsdl_tags)
                                )
                            }
                        },
                        text = { Text(text = stringResource(id = R.string.st_hsdl_tags)) },
                        enabled = !isLoading
                    )
                }
            }
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
            NavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = "Sensors"
            ) {
                composable(
                    route = "Sensors"
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
                        if (HsdlConfig.isVespucci.not()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = stringResource(id = R.string.st_hsdl_logging))
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            }
                        } else {
                            VespucciCharts(
                                sensors = streamSensors,
                                status = status,
                                streamData = streamData,
                                currentSensorEnabled = currentSensorEnabled,
                                vespucciTags = vespucciTags,
                                onSensorSelected = onSensorSelected,
                                showTagsEnabled = true
                            )
                        }
                    }
                }

                composable(
                    route = "Tags"
                ) {
                    if (HsdlConfig.isVespucci.not()) {
                        HsdlTags(
                            state = lazyState,
                            tags = tags,
                            status = status,
                            isLoading = isLoading,
                            onValueChange = onValueChange,
                            onSendCommand = onSendCommand
                        )
                    } else {
                        VespucciHsdlTags(
                            acquisitionInfo = acquisitionName.formatDate(),
                            vespucciTagsActivation = vespucciTagsActivation,
                            vespucciTags = vespucciTags,
                            isLoading = isLoading,
                            isLogging = isLogging,
                            onTagChangeState = onTagChangeState
                        )
                    }
                }
            }
        }
    }


    if (openStopDialog) {
        StopLoggingDialog(
            onDismissRequest = { openStopDialog = false }
        )
    }

    if (openResetDialog) {
        ResetBoardDialog(
            onDismissRequest = {
                openResetDialog = false
            },
            onRestartRequest = {
                openResetDialog = false

                GlobalConfig.navigateBack?.invoke(nodeId)
            }
        )
    }
}


private fun String.formatDate(): String {
    val inputSdf = SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.ROOT)
    val date = inputSdf.parse(this)
    val sdf = SimpleDateFormat("EEE MMM d yyyy HH:mm:ss", Locale.UK)
    return sdf.format(date)
}
