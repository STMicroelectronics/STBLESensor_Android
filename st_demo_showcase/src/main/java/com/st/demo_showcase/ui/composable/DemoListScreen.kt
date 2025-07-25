/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.composable

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.st.blue_sdk.models.Node
import com.st.demo_showcase.models.Demo
import com.st.demo_showcase.utils.DTMIModelLoadedStatus
import com.st.demo_showcase.utils.isExpertRequired
import com.st.demo_showcase.utils.isLastFwVersionMandatory
import com.st.demo_showcase.utils.isLoginRequired
import com.st.demo_showcase.utils.isPnPLMandatory
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import sh.calvin.reorderable.ReorderableItem

@SuppressLint("MissingPermission")
@Composable
fun DemoListScreen(
    modifier: Modifier = Modifier,
    device: Node?,
    isLoggedIn: Boolean,
    isExpert: Boolean,
    isBetaApplication: Boolean,
    fwUpdateAvailable: Boolean,
    statusModelDTMI: DTMIModelLoadedStatus = DTMIModelLoadedStatus.NotNecessary,
    pinnedDevices: List<String>,
    availableDemos: List<Demo>,
    onPinChange: (Boolean) -> Unit = { /** NOOP **/ },
    onLoginRequired: () -> Unit = { /** NOOP **/ },
    onExpertRequired: () -> Unit = { /** NOOP **/ },
    onLastFwRequired: ()  -> Unit = { /** NOOP **/ },
    onDemoSelected: (Demo) -> Unit = { /** NOOP **/ },
    onCustomDTMIClicked: () -> Unit = { /** NOOP **/ },
    onDemoReordered: (Int, Int) -> Unit = { _, _ -> /** NOOP **/ }
) {
    var openDeniedLoginDemoDialog by rememberSaveable { mutableStateOf(value = false) }
    var openDeniedExpertDemoDialog by rememberSaveable { mutableStateOf(value = false) }
    var openDeniedExpertLoginDemoDialog by rememberSaveable { mutableStateOf(value = false) }
    var openDeniedPnPLDemoDialog by rememberSaveable { mutableStateOf(value = false) }
    var openDeniedLastFwDialog by rememberSaveable {
        mutableStateOf(value = false)
    }

    val haptic = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (from.index > 0 && to.index > 1) {
            onDemoReordered(from.index - 1, to.index - 1)
        }
    }


    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        state = lazyListState,
        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
    ) {
        device?.let {
            item {
                if (isExpert) {
                    DeviceHeader(
                        isPin = pinnedDevices.contains(device.device.address),
                        boardTypeName = device.boardType.name,
                        name = device.device.name,
                        runningFw = device.runningFw,
                        onPinChange = onPinChange,
                        statusModelDTMI = statusModelDTMI,
                        onCustomDTMIClicked = onCustomDTMIClicked
                    )
                } else {
                    DeviceHeader(
                        isPin = pinnedDevices.contains(device.device.address),
                        boardTypeName = device.boardType.name,
                        name = device.device.name,
                        runningFw = device.runningFw,
                        onPinChange = onPinChange,
                        statusModelDTMI = DTMIModelLoadedStatus.NotNecessary
                    )
                }
            }
        }
        items(availableDemos, key = { it.name }) { item ->
            ReorderableItem(reorderableLazyListState, key = item.name) { isDragging ->
                if (isDragging) haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                val scale by animateFloatAsState(
                    if (isDragging) 1.1f else 1.0f,
                    label = "Moving Demo Scale"
                )
                val elevation by animateDpAsState(
                    if (isDragging) LocalDimensions.current.elevationMedium  else 0.dp,
                    label = "Moving Demo Elevation"
                )
                DemoListItem(
                    modifier = Modifier
                        .longPressDraggableHandle()
                        .scale(scale)
                        .shadow(elevation = elevation),
                    item = item,
                    isLoginLock = if (isBetaApplication) false else {
                        item.isLoginRequired() && !isLoggedIn
                    },
                    isExpertLock = if (isBetaApplication) false else {
                        item.isExpertRequired() && !isExpert
                    },
                    isPnPLLock = item.isPnPLMandatory() && ((statusModelDTMI == DTMIModelLoadedStatus.CustomNotLoaded) || (statusModelDTMI == DTMIModelLoadedStatus.NotNecessary)),
                    isLastFwLock = item.isLastFwVersionMandatory() && fwUpdateAvailable,
                    even = availableDemos.indexOf(item) % 2 == 0,
                    isLastOne = availableDemos.indexOf(item) == availableDemos.lastIndex,
                    onDemoSelected = onDemoSelected,
                    onLoginRequired = { openDeniedLoginDemoDialog = true },
                    onExpertRequired = { openDeniedExpertDemoDialog = true },
                    onPnPLRequired = { openDeniedPnPLDemoDialog = true },
                    onLastFwRequired = { openDeniedLastFwDialog = true },
                    onExpertLoginRequired = { openDeniedExpertLoginDemoDialog = true }
                )
            }
        }

        item {
            Spacer(
                Modifier.windowInsetsBottomHeight(
                    WindowInsets.systemBars
                )
            )
        }
    }

    if (openDeniedLoginDemoDialog) {
        LoginRestrictionDialog(
            onLoginRequired = onLoginRequired,
            onDismiss = { openDeniedLoginDemoDialog = false }
        )
    }

    if (openDeniedExpertDemoDialog) {
        ExpertRestrictionDialog(
            onExpertRequired = onExpertRequired,
            onDismiss = { openDeniedExpertDemoDialog = false }
        )
    }

    if (openDeniedPnPLDemoDialog) {
        PnPLRestrictionDialog(
            onDismiss = { openDeniedPnPLDemoDialog = false }
        )
    }

    if (openDeniedExpertLoginDemoDialog) {
        ExpertLoginRestrictionDialog(
            onOk = { openDeniedExpertLoginDemoDialog = false }
        )
    }

    if(openDeniedLastFwDialog) {
        LastFwRestrictionDialog(
            onLastFwRequired = onLastFwRequired,
            onDismiss = { openDeniedLastFwDialog = false }
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LoginRestrictionDialog(
    onDismiss: () -> Unit,
    onLoginRequired: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Demo Locked")
        },
        text = {
            Text(
                "This functionality is only available after the login\nPressing Ok the application will open the Login page"
            )
        },
        dismissButton = {
            BlueMsButtonOutlined(
                text = stringResource(id = android.R.string.cancel),
                onClick = onDismiss
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok), onClick = {
                    onLoginRequired()
                    onDismiss()
                }
            )
        }
    )
}


@SuppressLint("MissingPermission")
@Composable
fun LastFwRestrictionDialog(
    onDismiss: () -> Unit,
    onLastFwRequired: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Demo Locked")
        },
        text = {
            Text(
                "This functionality is only available only with the latest Fw version\nPressing Ok the application will download the latest Fw version"
            )
        },
        dismissButton = {
            BlueMsButtonOutlined(
                text = stringResource(id = android.R.string.cancel),
                onClick = onDismiss
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok), onClick = {
                    onLastFwRequired()
                    onDismiss()
                }
            )
        }
    )
}


@SuppressLint("MissingPermission")
@Composable
fun ExpertRestrictionDialog(
    onDismiss: () -> Unit,
    onExpertRequired: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Demo Locked")
        },
        text = {
            Text(
                "This functionality is only for expert user.\nPressing Ok the application will open the Profile selection page"
            )
        },
        dismissButton = {
            BlueMsButtonOutlined(
                text = stringResource(id = android.R.string.cancel),
                onClick = onDismiss
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onExpertRequired
                //onClick = onDismiss
            )
        }
    )
}


@Composable
fun PnPLRestrictionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Demo Locked")
        },
        text = {
            Text(
                "Not able to reach the models repository\nThis functionality is available only with a Internet connection\nCheck the connection or Load a Custom Model"
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onDismiss
            )
        }
    )
}


@SuppressLint("MissingPermission")
@Composable
fun ExpertLoginRestrictionDialog(
    onOk: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onOk,
        title = {
            Text(text = "Demo Locked")
        },
        text = {
            Text(
                "This functionality is only for expert user and after the login.\nChange the profile level and make the login for enabling it"
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onOk
            )
        }
    )
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun DemoListScreenPreview() {
    PreviewBlueMSTheme {
        DemoListScreen(
            device = null,
            isLoggedIn = true,
            isExpert = true,
            isBetaApplication = false,
            fwUpdateAvailable = false,
            pinnedDevices = emptyList(),
            availableDemos = Demo.entries
        )
    }
}