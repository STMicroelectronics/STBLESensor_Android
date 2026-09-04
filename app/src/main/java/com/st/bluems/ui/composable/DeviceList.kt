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
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.st.blue_sdk.models.Boards
import com.st.blue_sdk.models.Node
import com.st.bluems.NFCConnectionViewModel
import com.st.bluems.R
import com.st.bluems.ui.home.HomeViewModel
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.BlueMsButtonOutlined
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.Grey6
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.st.bluems.MainActivity
import com.st.bluems.widget.BlueMSWidgetReceiver
import com.st.ui.composables.BlueMSPullToRefreshBox
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.Grey10
import com.st.ui.theme.Grey3
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryYellow
import com.st.ui.theme.SecondaryBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess


@Composable
fun DeviceListScreenNavigation(
    modifier: Modifier = Modifier,
    backState: NavBackStack<NavKey>,
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
    val customNames by viewModel.customNames.collectAsStateWithLifecycle(emptyList())
    val boardsDescription by viewModel.boardsDescription.collectAsStateWithLifecycle()
    val disableHiddenDemos by viewModel.disableHiddenDemos.collectAsStateWithLifecycle()


    val nfcNodeId by nfcViewModel.nfcNodeId.collectAsStateWithLifecycle()

    val isBetaRelease by viewModel.isBetaRelease.collectAsStateWithLifecycle()

    val addWidgetBlueMSDecisionShowedFlag by viewModel.addWidgetBlueMSDecisionShowedFlag.collectAsStateWithLifecycle()

    val context = LocalContext.current

    var forceScan by rememberSaveable {
        mutableStateOf(false)
    }

    var showChangeNameForNodeId: String? by remember { mutableStateOf(null) }
    var boardTypeNameActive: String by remember {mutableStateOf("")}

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startHomeFragment()
            else -> Unit
        }
    }

    DeviceListWithPermissionsCheck(
        modifier = modifier,
        devices = devices,
        forceScan = forceScan,
        isBleEnabled = isBleEnabled,
        isLocationEnable = isLocationEnable,
        nfcNodeId = nfcNodeId,
        pinnedDevices = pinnedDevices,
        customNames = customNames,
        onCustomNameSelected = { nodeId, boardTypeName->
            boardTypeNameActive = boardTypeName
            showChangeNameForNodeId = nodeId
        },
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
            backState.add(HomeScreenProfileNavKey)
        },
        goToCatalog = {
            if (boardsDescription.isEmpty()) {
                Toast.makeText(context, "Boards Catalog not Available", Toast.LENGTH_SHORT).show()
            } else {
                backState.add(CatalogNavKey)
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
        goToThirdPartiesLicenses = {
            backState.add(LicensesNavKey)
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
                !((node.boardType == Boards.Model.SENSOR_TILE_BOX) || (node.boardType == Boards.Model.SENSOR_TILE_BOX_PRO) || (node.boardType == Boards.Model.SENSOR_TILE_BOX_PROB) || (node.boardType == Boards.Model.SENSOR_TILE_BOX_PROC))
            }

            nfcViewModel.setNFCNodeId(null)
            viewModel.connect(
                nodeId = nodeId,
                maxPayloadSize = maxPayloadSize,
                enableServer = enableServer
            ) {
                backState.add(DemoShowCaseNavKey(nodeId = nodeId))
            }
        },
        onStartScan = {
            viewModel.startScan()
        },
        onAddCatalogEntryFromFile = { fileUri ->
            forceScan = true
            val result = viewModel.setLocalBoardCatalog(fileUri = fileUri)
            result?.let { res ->
                if (res.startsWith("Added")) {
                    Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                }
            }
        },
        addWidgetBlueMSDecisionShowedFlag = addWidgetBlueMSDecisionShowedFlag,
        onAddWidgetBlueMSDecisionShowedFlag = { viewModel.setAddWidgetDecisionShowedFlag()}

    )

    if (showChangeNameForNodeId != null) {
        val keyboardController = LocalSoftwareKeyboardController.current
        var boardHasAlreadyACustomName by remember { mutableStateOf(customNames.firstOrNull { it.first == showChangeNameForNodeId }?.second) }

        AlertDialog(
            onDismissRequest = { showChangeNameForNodeId = null },
            title = {
                Text(text = "Board Alias:")
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                ) {
                    Text(text = "Set one Alias for the current board")

                    OutlinedTextField(
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text).copy(
                            imeAction = ImeAction.Done
                        ),
                        textStyle = MaterialTheme.typography.bodySmall,
                        singleLine = true,
                        value = boardHasAlreadyACustomName ?: "",
                        onValueChange = {
                            boardHasAlreadyACustomName = it
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        )
                    )
                }
            },
            dismissButton = {
                if (boardHasAlreadyACustomName.isNullOrBlank()) {
                    BlueMsButton(
                        text = "Cancel",
                        onClick = {
                            showChangeNameForNodeId = null
                        },
                        color = PrimaryYellow,
                        textColor = PrimaryBlue,
                    )
                } else {
                    BlueMsButton(
                        text = "Reset",
                        onClick = {
                            viewModel.setCustomNameForBoardId(
                                nodeId = showChangeNameForNodeId!!,
                                customName = null,
                                boardTypeName = boardTypeNameActive
                            )
                            showChangeNameForNodeId = null
                        },
                        color = PrimaryYellow,
                        textColor = PrimaryBlue,
                    )
                }
            },
            confirmButton = {
                BlueMsButton(
                    text = stringResource(id = android.R.string.ok),
                    onClick = {
                        viewModel.setCustomNameForBoardId(
                            nodeId = showChangeNameForNodeId!!,
                            customName = boardHasAlreadyACustomName,
                            boardTypeName = boardTypeNameActive
                        )
                        showChangeNameForNodeId = null
                    }
                )
            }
        )
    }
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
    customNames: List<Pair<String, String?>> = emptyList(),
    onCustomNameSelected: (String, String) -> Unit = { _, _ -> /** NOOP **/ },
    onPinChange: (String, Boolean) -> Unit,
    login: () -> Unit = { /** NOOP**/ },
    logout: () -> Unit = { /** NOOP**/ },
    goToCatalog: () -> Unit,
    goToSourceCode: () -> Unit,
    goToPrivacyPolicy: () -> Unit,
    goToThirdPartiesLicenses: () -> Unit,
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
    onAddCatalogEntryFromFile: (Uri) -> Unit,
    addWidgetBlueMSDecisionShowedFlag: Boolean,
    onAddWidgetBlueMSDecisionShowedFlag: () -> Unit = { /** NOOP **/ }
) {
    val context = LocalContext.current

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
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
                } else {
                    SearchingNode(nodeId = nfcNodeId.uppercase())
                }
            } else {
                DeviceList(
                    modifier = modifier,
                    devices = devices,
                    isLoading = isLoading,
                    pinnedDevices = pinnedDevices,
                    customNames = customNames,
                    onCustomNameSelected = onCustomNameSelected,
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
                    goToThirdPartiesLicenses = goToThirdPartiesLicenses,
                    goToAboutST = goToAboutST,
                    readBetaCatalog = readBetaCatalog,
                    readReleaseCatalog = readReleaseCatalog,
                    switchVersionBetaRelease = switchVersionBetaRelease,
                    switchServerForced = switchServerForced,
                    onStartScan = onStartScan,
                    onNodeSelected = onNodeSelected,
                    onAddCatalogEntryFromFile = onAddCatalogEntryFromFile,
                    addWidgetBlueMSDecisionShowedFlag = addWidgetBlueMSDecisionShowedFlag,
                    onAddWidgetBlueMSDecisionShowedFlag = onAddWidgetBlueMSDecisionShowedFlag
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
            onDismissRequest = {
                // this closes the main activity
                MainActivity().finish()
                // this closes the application
                exitProcess(0)
            },
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
        } else {
            Text(
                text = "- For connecting one device the bluetooth connection permission is mandatory."
            )
            Text(
                text = "- For searching new devices the location permission is mandatory"
            )
        }
    }
}

@Composable
fun MissingPermissionDialog(
    doNotShowRationale: Boolean,
    goToSettings: () -> Unit,
    onPermissionRequest: () -> Unit,
    onDismissRequest: () -> Unit = { /** NOOP **/ }
) {
    // TODO: extract string resource
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Permission required")
        },
        text = {
            MissingPermissionDialogContent(doNotShowRationale = doNotShowRationale)
        },
        dismissButton = {
            BlueMsButtonOutlined(
                onClick = onDismissRequest,
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
    customNames: List<Pair<String, String?>> = emptyList(),
    onCustomNameSelected: (String, String) -> Unit = { _,_ -> /** NOOP**/ },
    onPinChange: (String, Boolean) -> Unit,
    login: () -> Unit = { /** NOOP**/ },
    logout: () -> Unit = { /** NOOP**/ },
    goToCatalog: () -> Unit = { /** NOOP**/ },
    goToProfile: () -> Unit = { /** NOOP**/ },
    goToPrivacyPolicy: () -> Unit = { /** NOOP**/ },
    goToThirdPartiesLicenses: () -> Unit = { /** NOOP**/ },
    goToSourceCode: () -> Unit = { /** NOOP**/ },
    goToAboutST: () -> Unit = { /** NOOP **/ },
    readBetaCatalog: () -> Unit = { /** NOOP **/ },
    readReleaseCatalog: () -> Unit = { /** NOOP **/ },
    switchVersionBetaRelease: () -> Unit = { /** NOOP **/ },
    switchServerForced: () -> Unit = { /** NOP **/ },
    onStartScan: () -> Unit = { /** NOOP**/ },
    onAddCatalogEntryFromFile: (Uri) -> Unit = { /** NOOP**/ },
    onNodeSelected: (Node) -> Unit = { /** NOOP**/ },
    addWidgetBlueMSDecisionShowedFlag: Boolean,
    onAddWidgetBlueMSDecisionShowedFlag: () -> Unit = { /** NOOP **/ }
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

    val context = LocalContext.current

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
                goToThirdPartiesLicenses = goToThirdPartiesLicenses,
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
                .consumeWindowInsets(paddingValues)
                .padding(paddingValues),
            //.padding(paddingValues = paddingValues),
            filteredDevices = filteredDevices,
            pinnedDevices = pinnedDevices,
            customNames = customNames,
            onCustomNameSelected = onCustomNameSelected,
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

    if(addWidgetBlueMSDecisionShowedFlag.not()) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            AlertDialog(
                onDismissRequest = onAddWidgetBlueMSDecisionShowedFlag,
                title = {
                    Text(
                        text = "BlueMS Widget",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(LocalDimensions.current.paddingNormal)
                    ) {

                        HorizontalDivider(thickness = 1.dp, color = Grey3)

                        Text(
                            "Do you want to add one widget to home Screen for a fast connection with your preferred boards?"
                        )

                        Image(
                            modifier = Modifier
                                .padding(top = LocalDimensions.current.paddingNormal)
                                .fillMaxWidth(),
                            contentScale = ContentScale.FillWidth,
                            painter = painterResource(id = R.drawable.bluems_widget_preview),
                            contentDescription = "Background image"
                        )

                    }
                },
                dismissButton = {
                    BlueMsButton(
                        text = "Add Widget",
                        onClick = {
                            val myProvider =
                                ComponentName(context, BlueMSWidgetReceiver::class.java)

                            val successCallback = PendingIntent.getBroadcast(
                                context,
                                0,
                                Intent(context, BlueMSWidgetReceiver::class.java),
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )

                            appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                            onAddWidgetBlueMSDecisionShowedFlag()
                        }
                    )
                },
                confirmButton = {
                    BlueMsButton(
                        color = PrimaryYellow,
                        textColor = Grey10,
                        text = "Cancel",
                        onClick = onAddWidgetBlueMSDecisionShowedFlag
                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceList(
    modifier: Modifier = Modifier,
    filteredDevices: List<Node>,
    pinnedDevices: List<String>,
    customNames: List<Pair<String, String?>> = emptyList(),
    onCustomNameSelected: (String, String) -> Unit = { _, _ -> /** NOOP**/ },
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
                            showEdit = true,
                            boardHasCustomName = customNames.firstOrNull { it.first == item.device.address }?.second,
                            onCustomNameSelected = {
                                onCustomNameSelected(item.device.address,item.boardType.name)
                            },
                            onNodeSelected = onNodeSelected,
                            onPinChange = { change ->
                                onPinChange(item.device.address, change)
                            }
                        )
                    }
                } else {
                    item {
                        EmptyDeviceList()
                    }
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


fun Date.toDayMonthYearString(): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(this)
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
