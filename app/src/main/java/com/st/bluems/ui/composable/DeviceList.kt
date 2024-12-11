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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
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
import com.st.bluems.NFCConnectionViewModel
import com.st.bluems.R
import com.st.bluems.ui.home.HomeFragmentDirections
import com.st.bluems.ui.home.HomeViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Grey6
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue

@Composable
fun DeviceListScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: HomeViewModel,
    nfcViewModel: NFCConnectionViewModel,
    isBleEnabled: Boolean,
    onEnableBle: () -> Unit,
    isLocationEnable: Boolean,
    onEnableLocation: () -> Unit
) {
    val devices by viewModel.scanBleDevices.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isExpert by viewModel.isExpert.collectAsStateWithLifecycle()
    val isServerForced by viewModel.isServerForced.collectAsStateWithLifecycle()
    val pinnedDevices by viewModel.pinnedDevices.collectAsStateWithLifecycle(emptyList())
    val boardsDescription by viewModel.boardsDescription.collectAsStateWithLifecycle()
    val disableHiddenDemos by viewModel.disableHiddenDemos.collectAsStateWithLifecycle()


    val nfcNodeId by nfcViewModel.nfcNodeId.collectAsStateWithLifecycle()

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
        isLocationEnable = isLocationEnable,
        nfcNodeId = nfcNodeId,
        pinnedDevices = pinnedDevices,
        isLoading = isLoading,
        isLoggedIn = isLoggedIn,
        isExpert = isExpert,
        isServerForced = isServerForced,
        isBetaRelease = isBetaRelease,
        disableHiddenDemos = disableHiddenDemos,
        enableDisableHiddenDemos = {
            viewModel.enableDisableHiddenDemos()
        },
        login = {
            viewModel.login()
        },
        logout = {
            viewModel.logout()
        },
        onEnableBle = onEnableBle,
        onEnableLocation = onEnableLocation,
        goToProfile = {
            navController.navigate(
                HomeFragmentDirections.actionHomeFragmentToProfileNavGraph()
            )
        },
        goToCatalog = {
            if (boardsDescription.isEmpty()) {
                Toast.makeText(context, "Boards Catalog not Available", Toast.LENGTH_SHORT).show()
            } else {

                val navOptions: NavOptions = navOptions {
                    anim {
                        //enter = com.st.ui.R.anim.slide_in_from_left
                        enter = com.st.ui.R.anim.fade_in
                        exit = com.st.ui.R.anim.fade_out
                        popEnter = com.st.ui.R.anim.fade_in
                        //popExit = com.st.ui.R.anim.slide_out_to_left
                        popExit = com.st.ui.R.anim.fade_out
                    }
                }
                navController.navigate(
                    directions = HomeFragmentDirections.actionHomeFragmentToCatalog(),
                    navOptions = navOptions
                )

//                navController.navigate(
//                    HomeFragmentDirections.actionHomeFragmentToCatalog()
//                )
            }
        },
        goToSourceCode = {
            viewModel.openGitHubSourceCode()
        },
        goToAboutST = {
            viewModel.openAboutUsPage()
        },
        goToPrivacyPolicy = {
            viewModel.openPrivacyPolicyPage()
        },
        switchVersionBetaRelease = {
            viewModel.switchVersionBetaRelease()
        },
        switchServerForced = {
            viewModel.switchServerForced()
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
            val maxPayloadSize = if (node.familyType == Boards.Family.WBA_FAMILY) 240 else 248
            val enableServer = if (isServerForced) {
                true
            } else {
                !((node.boardType == Boards.Model.SENSOR_TILE_BOX_PRO) || (node.boardType == Boards.Model.SENSOR_TILE_BOX_PROB))
            }

            nfcViewModel.setNFCNodeId(null)
            viewModel.connect(
                nodeId = nodeId,
                maxPayloadSize = maxPayloadSize,
                enableServer = enableServer
            ) {
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
            val result = viewModel.setLocalBoardCatalog(fileUri = fileUri)
            result?.let { res ->
                if(res.startsWith("Added")) {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                }
            }
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
    isLocationEnable: Boolean,
    nfcNodeId: String?,
    forceScan: Boolean = false,
    isLoggedIn: Boolean = false,
    isExpert: Boolean = false,
    isServerForced: Boolean = false,
    isBetaRelease: Boolean = false,
    disableHiddenDemos: Boolean = false,
    enableDisableHiddenDemos: () -> Unit = { /** NOOP **/ },
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
    switchServerForced: () -> Unit,
    onStartScan: () -> Unit,
    onEnableBle: () -> Unit,
    onEnableLocation: () -> Unit,
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
        if (isBleEnabled && isLocationEnable) {
            LaunchedEffect(key1 = forceScan) {
                onStartScan()
            }

            if (nfcNodeId != null) {
                val node = devices.firstOrNull { it.device.address.equals(nfcNodeId.uppercase()) }
                if (node != null) {
                    onNodeSelected(node)
                }
            } else {
                DeviceList(
                    modifier = modifier,
                    devices = devices,
                    isLoading = isLoading,
                    pinnedDevices = pinnedDevices,
                    onPinChange = onPinChange,
                    isLoggedIn = isLoggedIn,
                    isExpert = isExpert,
                    isServerForced = isServerForced,
                    isBetaRelease = isBetaRelease,
                    disableHiddenDemos = disableHiddenDemos,
                    enableDisableHiddenDemos = enableDisableHiddenDemos,
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
                    switchServerForced = switchServerForced,
                    onStartScan = onStartScan,
                    onNodeSelected = onNodeSelected,
                    onAddCatalogEntryFromFile = onAddCatalogEntryFromFile
                )
            }
        } else if (!isBleEnabled) {
            MissingBleDialog(onEnable = onEnableBle)
        } else if (!isLocationEnable) {
            MissingLocationDialog(onEnable = onEnableLocation)
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
fun MissingLocationDialog(
    onEnable: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /** NOOP **/ },
        title = {
            Text(text = stringResource(R.string.missing_location_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.missing_location_dialog_body_text))
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
    isExpert: Boolean = false,
    isServerForced: Boolean = false,
    isBetaRelease: Boolean = false,
    disableHiddenDemos: Boolean = false,
    enableDisableHiddenDemos: () -> Unit = { /** NOOP **/ },
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
    switchServerForced: () -> Unit = { /** NOP **/ },
    onStartScan: () -> Unit = { /** NOOP**/ },
    onAddCatalogEntryFromFile: (Uri) -> Unit = { /** NOOP**/ },
    onNodeSelected: (Node) -> Unit = { /** NOOP**/ }
) {
    var filters by remember { mutableStateOf(value = DeviceListFilter()) }
    var openFilterDialog by rememberSaveable { mutableStateOf(value = false) }
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

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButtonPosition = FabPosition.End,
                floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                containerColor = SecondaryBlue,
                onClick = { openFilterDialog = true }) {
                Icon(
                    tint = PrimaryBlue,
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
            }
        },
        topBar = {
            MainTopBar(
                isLoggedIn = isLoggedIn,
                isExpert = isExpert,
                isServerForced = isServerForced,
                isBetaRelease = isBetaRelease,
                disableHiddenDemos = disableHiddenDemos,
                enableDisableHiddenDemos = enableDisableHiddenDemos,
                login = login,
                logout = logout,
                goToProfile = goToProfile,
                goToSourceCode = goToSourceCode,
                goToAboutST = goToAboutST,
                goToPrivacyPolicy = goToPrivacyPolicy,
                readBetaCatalog = readBetaCatalog,
                readReleaseCatalog = readReleaseCatalog,
                switchVersionBetaRelease = switchVersionBetaRelease,
                switchServerForced = switchServerForced,
                goToCatalog = goToCatalog
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
                .consumeWindowInsets(paddingValues).padding(paddingValues),
                //.padding(paddingValues = paddingValues),
            filteredDevices = filteredDevices,
            pinnedDevices = pinnedDevices,
            onPinChange = onPinChange,
            goToCatalog = goToCatalog,
            isLoading = isLoading,
            isBetaRelease = isBetaRelease,
            onStartScan = onStartScan,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    filteredDevices: List<Node>,
    pinnedDevices: List<String>,
    onPinChange: (String, Boolean) -> Unit,
    goToCatalog: () -> Unit = { /** NOOP**/ },
    isLoading: Boolean = false,
    isBetaRelease: Boolean = false,
    onStartScan: () -> Unit = { /** NOOP**/ },
    onNodeSelected: (Node) -> Unit = { /** NOOP**/ }
) {


    val pullRefreshState = rememberPullToRefreshState()

    Column(
        modifier = modifier
    ) {
        BlueMSPullToRefreshBox(
            state = pullRefreshState,
            isRefreshing = isLoading,
            onRefresh = onStartScan,
            isBetaRelease = isBetaRelease
        ) {
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
                        modifier = Modifier.animateItem(
                            fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        ),
                        isPin = pinnedDevices.contains(item.device.address),
                        item = item,
                        onNodeSelected = onNodeSelected,
                        onPinChange = { change ->
                            onPinChange(item.device.address, change)
                        }
                    )
                }

                if (filteredDevices.isEmpty()) {
                    item {
                        BlueMsButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.st_home_deviceList_discoverBtn),
                            onClick = goToCatalog
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
    var currentRotation by remember { mutableFloatStateOf(value = 0f) }
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

    FloatingActionButton(
        containerColor = SecondaryBlue,
        onClick = onStartScan
    ) {
        //Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            modifier = Modifier
                .size(LocalDimensions.current.iconSmall)
                .rotate(rotation.value),
            tint = MaterialTheme.colorScheme.primary,
            imageVector = Icons.Default.Sync,
            contentDescription = null
        )
//            Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingSmall))
//            Text(
//                style = MaterialTheme.typography.labelSmall,
//                text = "Refresh"
//            )
//        }
    }
}
