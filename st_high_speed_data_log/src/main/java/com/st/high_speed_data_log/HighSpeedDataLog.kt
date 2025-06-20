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
import com.st.high_speed_data_log.composable.HsdlSensors
import com.st.high_speed_data_log.composable.HsdlTags
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighSpeedDataLog(
    modifier: Modifier,
    viewModel: HighSpeedDataLogViewModel,
    nodeId: String
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                viewModel.initDemo()
            }
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
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val status by viewModel.componentStatusUpdates.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSDCardInserted by viewModel.isSDCardInserted.collectAsStateWithLifecycle()
    val numActiveTags by viewModel.numActiveTags.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isConnectionLost by viewModel.isConnectionLost.collectAsStateWithLifecycle()
    val numActiveSensors by viewModel.numActiveSensors.collectAsStateWithLifecycle()

    HighSpeedDataLog(
        modifier = modifier,
        sensors = sensors,
        numActiveSensors = numActiveSensors,
        tags = tags,
        status = status,
        isSDCardInserted = isSDCardInserted,
        isLogging = isLogging,
        isLoading = isLoading,
        numActiveTags = numActiveTags,
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
        onBeforeUcf = { viewModel.setEnableStartStopDemo(false) },
        onAfterUcf = { },
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
                viewModel.startLog(nodeId)
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
fun HighSpeedDataLog(
    modifier: Modifier,
    sensors: List<ComponentWithInterface> = emptyList(),
    numActiveSensors: Int,
    tags: List<ComponentWithInterface> = emptyList(),
    status: List<JsonObject>,
    numActiveTags: Int,
    isLogging: Boolean,
    isBetaApplication: Boolean,
    isSDCardInserted: Boolean = false,
    isLoading: Boolean = false,
    onValueChange: (String, Pair<String, Any>) -> Unit,
    onBeforeUcf: () -> Unit,
    onAfterUcf: () -> Unit,
    onSendCommand: (String, CommandRequest?) -> Unit,
    onStartStopLog: (Boolean) -> Unit = { /**NOOP **/ },
    onRefresh: () -> Unit = { /**NOOP **/ },
    navController: NavHostController = rememberNavController()
) {
    val sensorsTitle = stringResource(id = R.string.st_hsdl_sensors)
    val tagsTitle = stringResource(id = R.string.st_hsdl_tags)
    var currentTitle by remember { mutableStateOf(sensorsTitle) }

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
                        onStartStopLog(!isLogging)
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
                        shape = Shapes.medium
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
                                Text(text = "$numActiveTags")
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

                composable(
                    route = "Tags"
                ) {
                    HsdlTags(
                        state = lazyState,
                        tags = tags,
                        status = status,
                        isLoading = isLoading,
                        onValueChange = onValueChange,
                        onSendCommand = onSendCommand
                    )
                }
            }
        }
    }
}
