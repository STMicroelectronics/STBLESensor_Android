package com.st.cloud_azure_iot_central.composable

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.cloud_azure_iot_central.CloudAzureIotCentralViewModel
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.BlueMSSnackBarMaterial3
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val pullRefreshState = rememberPullToRefreshState()

    var oneDeviceSelected by rememberSaveable { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.padding(all = LocalDimensions.current.paddingNormal),
        snackbarHost = {
            BlueMSSnackBarMaterial3(
                snackBarHostState = snackBarHostState
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openAddDeviceDialog = true },
                contentColor = MaterialTheme.colorScheme.primary,
                containerColor = SecondaryBlue
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

            Text(
                modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
                style = MaterialTheme.typography.bodyLarge,
                color = Grey6,
                textAlign = TextAlign.Center,
                text = "Select on of the following devices or create a new one"
            )

            if (oneDeviceSelected) {
                Text(
                    modifier = Modifier.padding(LocalDimensions.current.paddingNormal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Grey6,
                    textAlign = TextAlign.Center,
                    text = buildAnnotatedString {
                        append("Click on  '")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append("Dev Upload")
                        }
                        append("'")
                    }
                )
            }

            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

            BlueMSPullToRefreshBox(
                state = pullRefreshState,
                isRefreshing = isLoading,
                isBetaRelease = viewModel.isBeta,
                onRefresh = {
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.readDevicesFromCloud()
                    }
                }) {
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
                                cloudDevice = cloudDevice,
                                onCloudDeviceSelection = {
                                    oneDeviceSelected = true
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
            }
        }
    }

    retValue?.let {
        val text = retValue!!
        viewModel.cleanError()
        coroutineScope.launch {
            snackBarHostState.showSnackbar(message = text)
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