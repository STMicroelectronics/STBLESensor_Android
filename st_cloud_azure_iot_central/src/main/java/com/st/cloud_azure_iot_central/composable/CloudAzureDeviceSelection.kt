package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.ui.composables.BlueMSSnackBar
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey0
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CloudAzureDeviceSelection(
    modifier: Modifier = Modifier,
    viewModel: CloudAzureIotCentralViewModel
) {
    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                CoroutineScope(Dispatchers.IO).launch {
                    //Read the Devices List
                    viewModel.readDevicesFromCloud()
                    //Read the Templates List
                    viewModel.readTemplatesFromCloud()
                }
            }

            else -> Unit
        }
    }

    val cloudDevices by viewModel.cloudDevices.collectAsStateWithLifecycle()

    val cloudTemplates by viewModel.cloudTemplates.collectAsStateWithLifecycle()

    val boardUid by viewModel.boardUid.collectAsStateWithLifecycle()

    val selectedDevice by viewModel.selectedCloudDeviceNum.collectAsStateWithLifecycle()

    val retValue by viewModel.retValue.collectAsStateWithLifecycle()

    var openAddDeviceDialog by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    val coroutineScope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.readDevicesFromCloud()
            }
        }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.padding(all = LocalDimensions.current.paddingNormal),
        snackbarHost = {
            BlueMSSnackBar(
                snackbarHostState = snackbarHostState,
                onDismiss = { snackbarHostState.currentSnackbarData?.dismiss() })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDeviceDialog = true },
                contentColor = Grey0,
                backgroundColor = SecondaryBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValue ->
        Column(
            modifier = modifier
                .padding(paddingValue)
                .fillMaxSize()
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                text = "Cloud Devices"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

//        Surface(
//            modifier = modifier.fillMaxWidth(),
//            shape = Shapes.small,
//            shadowElevation = LocalDimensions.current.elevationSmall
//        ) {
            Text(
                modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
                style = MaterialTheme.typography.bodyLarge,
                color = Grey6,
                textAlign = TextAlign.Center,
                text = "Select on of the following devices or create a new one"
            )
            //}

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingMedium))

            Text(
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                text = "Available devices:"
            )

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))

            Box(modifier = Modifier.pullRefresh(state = pullRefreshState)) {
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                    verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                ) {
                    if (cloudDevices.isNotEmpty()) {
                        itemsIndexed(cloudDevices) { index, cloudDevice ->
                            CloudDeviceItem(
                                boardUid = boardUid,
                                isSelected = index == selectedDevice,
                                hasCredentials = cloudDevice.credentials != null,
                                cloudDevice = cloudDevice,
                                onCloudDeviceSelection = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    cloudDevice.selected = true
                                    viewModel.setSelectedCloudDevice(index)
                                    cloudDevices.filter { it.id != cloudDevice.id }
                                        .map { it.selected = false }
                                },
                                onCloudDeviceDeleting = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (index == selectedDevice) {
                                        viewModel.setSelectedCloudDevice(viewModel.deviceCloutNotSELECTED)
                                    }
                                    CoroutineScope(Dispatchers.IO).launch {
                                        viewModel.deleteDeviceById(cloudDevice.id)
                                        viewModel.readDevicesFromCloud()
                                    }
                                })
                        }
                    } else {
                        item {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                text = "No available devices\nCreate a new one"
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isLoading,
                    state = pullRefreshState,
                    modifier = Modifier.align(alignment = Alignment.TopCenter),
                    scale = true
                )
            }
        }
    }

    retValue?.let {
        val text = retValue!!
        viewModel.cleanError()
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message = text)
        }
    }

    if (openAddDeviceDialog) {
        if (cloudTemplates.isNotEmpty()) {
            AzureCloudAddDeviceDialog(
                cloudTemplates = cloudTemplates,
                boardUid = boardUid,
                deviceTemplate = cloudTemplates.firstOrNull { it.id == viewModel.selectedCloudApp!!.cloudApp.dtmi }?.displayName
                    ?: cloudTemplates.first().displayName ?: "Default Name",
                onDismiss = { openAddDeviceDialog = false },
                onConfirmation = { newDevice ->
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.createNewDevice(newDevice)
                        viewModel.readDevicesFromCloud()
                    }
                    openAddDeviceDialog = false
                })
        }
    }
}