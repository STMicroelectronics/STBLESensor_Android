/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.bluems.ui.composable

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.bluems.R
import com.st.bluems.ui.home.HomeFragmentDirections
import com.st.bluems.ui.home.HomeViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6
import com.st.ui.utils.asString
import androidx.compose.material.FloatingActionButton as Material2FloatingActionButton
import androidx.compose.material.Scaffold as Material2Scaffold

@Composable
fun DeviceListScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel,
    isBleEnabled: Boolean,
    onEnableBle: () -> Unit
) {
    val devices by viewModel.scanBleDevices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isExpert by viewModel.isExpert.collectAsStateWithLifecycle()
    val pinnedDevices by viewModel.pinnedDevices.collectAsStateWithLifecycle(emptyList())
    val canExploreCatalog by viewModel.canExploreCatalog.collectAsStateWithLifecycle()

    val isBetaRelease by viewModel.isBetaRelease.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var forceScan by rememberSaveable {
        mutableStateOf(false)
    }

    DeviceListWithPermissionsCheck(
        modifier = modifier,
        devices = devices,
        forceScan = forceScan,
        isBleEnabled = isBleEnabled,
        pinnedDevices = pinnedDevices,
        isLoading = isLoading,
        canExploreCatalog = canExploreCatalog,
        isLoggedIn = isLoggedIn,
        isExpert = isExpert,
        isBetaRelease = isBetaRelease,
        login = {
            viewModel.login()
        },
        logout = {
            viewModel.logout()
        },
        onEnableBle = onEnableBle,
        goToProfile = {
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToProfileNavGraph()
            )
        },
        goToCatalog = {
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToCatalog()
            )
        },
        goToSourceCode = {
            viewModel.openGitHubSourceCode()
        },
        goToAboutST = {
            viewModel.openAboutUsPage()
        },
        goToPrivacyPolicy = {
            viewModel.openPrivacyPoliciPage()
        },
        switchVersionBetaRelease = {
            viewModel.switchVersionBetaRelease()
        },
        readBetaCatalog = {
            forceScan = true
            viewModel.readBetaCatalog()
            Toast.makeText(context, "Loaded Beta Catalog", Toast.LENGTH_SHORT).show()
        },
        readReleaseCatalog = {
            forceScan = true
            viewModel.readReleaseCatalog()
            Toast.makeText(context, "Loaded Release Catalog", Toast.LENGTH_SHORT).show()
        },
        onPinChange = { id, isPin ->
            if (isPin) {
                viewModel.addToPinnedDevices(id)
            } else {
                viewModel.removeFromPinnedDevices(id)
            }
        },
        onNodeSelected = { node ->
            val nodeId = node.device.address
            val maxPayloadSize = if(node.boardType == Boards.Model.WBA_BOARD) 240 else 248
            viewModel.connect(nodeId = nodeId, maxPayloadSize = maxPayloadSize) {
                navController.navigate(
                    HomeFragmentDirections.actionHomeFragmentToDemoShowCase(
                        nodeId
                    )
                )
            }
        },
        onStartScan = {
            viewModel.startScan()
        },
        onAddCatalogEntryFromFile = { fileUri ->
            forceScan = true
            viewModel.setLocalBoardCatalog(fileUri = fileUri)
        }
    )
}

@OptIn(
    ExperimentalPermissionsApi::class
)
@Composable
fun DeviceListWithPermissionsCheck(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isBleEnabled: Boolean,
    canExploreCatalog: Boolean,
    forceScan: Boolean = false,
    isLoggedIn: Boolean = false,
    isExpert: Boolean = false,
    isBetaRelease: Boolean = false,
    devices: List<Node>,
    pinnedDevices: List<String>,
    onPinChange: (String, Boolean) -> Unit,
    login: () -> Unit = { /** NOOP**/ },
    logout: () -> Unit = { /** NOOP**/ },
    goToCatalog: () -> Unit,
    goToSourceCode: () -> Unit,
    goToPrivacyPolicy: () -> Unit,
    goToAboutST: () -> Unit,
    goToProfile: () -> Unit,
    readBetaCatalog: () -> Unit,
    readReleaseCatalog: () -> Unit,
    switchVersionBetaRelease: () -> Unit,
    onStartScan: () -> Unit,
    onEnableBle: () -> Unit,
    onNodeSelected: (Node) -> Unit,
    onAddCatalogEntryFromFile: (Uri) -> Unit
) {
    val context = LocalContext.current

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    )

    if (locationPermissionState.allPermissionsGranted) {
        if (isBleEnabled) {
            LaunchedEffect(key1 = forceScan) {
                onStartScan()
            }

            DeviceList(
                modifier = modifier,
                devices = devices,
                isLoading = isLoading,
                canExploreCatalog = canExploreCatalog,
                pinnedDevices = pinnedDevices,
                onPinChange = onPinChange,
                isLoggedIn = isLoggedIn,
                isExpert = isExpert,
                isBetaRelease = isBetaRelease,
                login = login,
                logout = logout,
                goToProfile = goToProfile,
                goToCatalog = goToCatalog,
                goToSourceCode = goToSourceCode,
                goToPrivacyPolicy = goToPrivacyPolicy,
                goToAboutST = goToAboutST,
                readBetaCatalog = readBetaCatalog,
                readReleaseCatalog = readReleaseCatalog,
                switchVersionBetaRelease = switchVersionBetaRelease,
                onStartScan = onStartScan,
                onNodeSelected = onNodeSelected,
                onAddCatalogEntryFromFile = onAddCatalogEntryFromFile
            )
        } else {
            MissingBleDialog(onEnable = onEnableBle)
        }
    } else {
        MissingPermissionDialog(
            doNotShowRationale = locationPermissionState.shouldShowRationale,
            onPermissionRequest = { locationPermissionState.launchMultiplePermissionRequest() },
            goToSettings = {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                    val uri = Uri.fromParts("package", context.packageName, null)
                    it.data = uri
                    context.startActivity(it)
                }
            }
        )
    }
}

@Composable
fun MissingPermissionDialogContent(
    doNotShowRationale: Boolean = false
) {
    Column(
        modifier = Modifier.padding(all = LocalDimensions.current.paddingNormal)
    ) {
        if (doNotShowRationale) {
            Text(
                text = "- Impossible to connect one device without the bluetooth connection permission enabled"
            )
            Text(
                text = "- Impossible to search for new devices without the location permission enabled"
            )
            Text(
                text = "- Impossible to test audio feature without the record audio permission enabled"
            )
        } else {
            Text(
                text = "- For connecting one device the bluetooth connection permission is mandatory."
            )
            Text(
                text = "- For searching new devices the location permission is mandatory"
            )
            Text(
                text = "- For test audio features the record audio permission is mandatory"
            )
        }
    }
}

@Composable
fun MissingPermissionDialog(
    doNotShowRationale: Boolean,
    goToSettings: () -> Unit,
    onPermissionRequest: () -> Unit
) {
    // TODO: extract string resource
    AlertDialog(
        onDismissRequest = { /** NOOP **/ },
        title = {
            Text(text = "Permission required")
        },
        text = {
            MissingPermissionDialogContent(doNotShowRationale = doNotShowRationale)
        },
        dismissButton = {
            BlueMsButtonOutlined(
                onClick = { /** NOOP **/ },
                text = stringResource(id = android.R.string.cancel)
            )
        },
        confirmButton = {
            if (doNotShowRationale) {
                BlueMsButton(
                    onClick = goToSettings,
                    text = stringResource(id = R.string.st_home_missingPermission_goToSettingsBtn)
                )
            } else {
                BlueMsButton(
                    onClick = onPermissionRequest,
                    text = stringResource(id = android.R.string.ok)
                )
            }
        }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun ProfileRestrictionDialog(
    onDismiss: () -> Unit,
    onChangeProfile: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.profile_restricted_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.profile_restricted_dialog_body_text))
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
                onClick = onChangeProfile
            )
        }
    )
}

@SuppressLint("MissingPermission")
@Composable
fun MissingBleDialog(
    onEnable: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /** NOOP **/ },
        title = {
            Text(text = stringResource(R.string.missing_ble_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.missing_ble_dialog_body_text))
        },
        dismissButton = {
            BlueMsButtonOutlined(
                text = stringResource(id = android.R.string.cancel),
                onClick = { /** NOOP **/ }
            )
        },
        confirmButton = {
            BlueMsButton(
                text = stringResource(id = android.R.string.ok),
                onClick = onEnable
            )
        }
    )
}

@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    devices: List<Node> = emptyList(),
    isLoggedIn: Boolean = false,
    isExpert: Boolean  = false,
    isBetaRelease: Boolean = false,
    canExploreCatalog: Boolean = false,
    isLoading: Boolean = false,
    pinnedDevices: List<String>,
    onPinChange: (String, Boolean) -> Unit,
    login: () -> Unit = { /** NOOP**/ },
    logout: () -> Unit = { /** NOOP**/ },
    goToCatalog: () -> Unit = { /** NOOP**/ },
    goToProfile: () -> Unit = { /** NOOP**/ },
    goToPrivacyPolicy: () -> Unit = { /** NOOP**/ },
    goToSourceCode: () -> Unit = { /** NOOP**/ },
    goToAboutST: () -> Unit = { /** NOOP **/ },
    readBetaCatalog: () -> Unit = { /** NOOP **/ },
    readReleaseCatalog: () -> Unit = { /** NOOP **/ },
    switchVersionBetaRelease: () -> Unit = { /** NOOP **/ },
    onStartScan: () -> Unit = { /** NOOP**/ },
    onAddCatalogEntryFromFile: (Uri) -> Unit = { /** NOOP**/ },
    onNodeSelected: (Node) -> Unit = { /** NOOP**/ }
) {
    var filters by remember { mutableStateOf(value = DeviceListFilter()) }
    var openFilterDialog by rememberSaveable { mutableStateOf(value = false) }
    var openDeniedCatalogDialog by rememberSaveable { mutableStateOf(value = false) }
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        if (fileUri != null) {
            onAddCatalogEntryFromFile(fileUri)
        }
    }

    val filteredDevices by remember(key1 = filters, devices, pinnedDevices) {
        derivedStateOf {
            val rssiFilterDevices = devices.filter {
                it.rssi == null || it.rssi!!.rssi >= filters.rssi
            }

            rssiFilterDevices.filter { pinnedDevices.contains(it.device.address) } + rssiFilterDevices.filter {
                pinnedDevices.contains(
                    it.device.address
                ).not()
            }
        }
    }

    Material2Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = androidx.compose.material.FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            DeviceScanFAB(
                isLoading = isLoading,
                onStartScan = onStartScan
            )
        },
        bottomBar = {
            MainBottomBar(
                openCatalog = {
                    if (canExploreCatalog) goToCatalog() else openDeniedCatalogDialog = true
                },
                openFilter = { openFilterDialog = true }
            )
        },
        topBar = {
            MainTopBar(
                isLoggedIn = isLoggedIn,
                isExpert = isExpert,
                isBetaRelease = isBetaRelease,
                login = login,
                logout = logout,
                goToProfile = goToProfile,
                goToSourceCode = goToSourceCode,
                goToAboutST = goToAboutST,
                goToPrivacyPolicy = goToPrivacyPolicy,
                readBetaCatalog = readBetaCatalog,
                readReleaseCatalog = readReleaseCatalog,
                switchVersionBetaRelease = switchVersionBetaRelease
            ) {
                pickFileLauncher.launch(
                    arrayOf(
                        JSON_FILE_TYPE
                    )
                )
            }
        }
    ) { paddingValues ->
        DeviceList(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = paddingValues),
            filteredDevices = filteredDevices,
            pinnedDevices = pinnedDevices,
            onPinChange = onPinChange,
            goToCatalog = goToCatalog,
            onNodeSelected = onNodeSelected
        )
    }

    if (openFilterDialog) {
        Dialog(onDismissRequest = { openFilterDialog = false }) {
            DeviceListFilterDialog(filters = filters) {
                filters = it
                openFilterDialog = false
            }
        }
    }

    if (openDeniedCatalogDialog) {
        ProfileRestrictionDialog(
            onChangeProfile = goToProfile,
            onDismiss = { openDeniedCatalogDialog = false }
        )
    }
}

@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    filteredDevices: List<Node>,
    pinnedDevices: List<String>,
    onPinChange: (String, Boolean) -> Unit,
    goToCatalog: () -> Unit = { /** NOOP**/ },
    onNodeSelected: (Node) -> Unit = { /** NOOP**/ }
) {
    Column(
        modifier = modifier
    ) {
        Box(modifier = Modifier) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
            ) {
                if (filteredDevices.isNotEmpty()) {
                    item {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = LocalDimensions.current.paddingNormal),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Grey6,
                            text = stringResource(id = R.string.st_home_deviceList_welcomeText)
                        )
                    }
                } else {
                    item {
                        EmptyDeviceList()
                    }
                }

                itemsIndexed(items = filteredDevices) { _, item ->
                    DeviceListItem(
                        isPin = pinnedDevices.contains(item.device.address),
                        item = item,
                        onNodeSelected = onNodeSelected,
                        onPinChange = { change ->
                            onPinChange(item.device.address, change)
                        }
                    )
                }

                item {
                    BlueMsButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.st_home_deviceList_discoverBtn),
                        onClick = goToCatalog
                    )
                }
            }
        }
    }
}

private const val DEGREES = 360f
private const val DURATION_MILLIS = 1500

@Composable
fun DeviceScanFAB(
    isLoading: Boolean,
    onStartScan: () -> Unit
) {
    var currentRotation by remember { mutableStateOf(value = 0f) }
    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(key1 = isLoading) {
        if (isLoading) {
            rotation.animateTo(
                targetValue = currentRotation - DEGREES,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = DURATION_MILLIS, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            ) {
                currentRotation = value
            }
        }
    }

    Material2FloatingActionButton(
        backgroundColor = SecondaryBlue,
        shape = CircleShape,
        onClick = onStartScan
    ) {
        Icon(
            modifier = Modifier.rotate(rotation.value),
            tint = MaterialTheme.colorScheme.primary,
            imageVector = Icons.Default.Sync,
            contentDescription = null
        )
    }
}
