package com.st.demo_showcase.ui.composable

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.st.blue_sdk.models.Boards
import com.st.demo_showcase.R
import com.st.demo_showcase.ui.DemoShowCaseViewModel
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseDebugConsoleNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseFwDirectUpdateNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseFwUpdateNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseLogSettingsNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseMainScreenNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCasePnplSettingsNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseUserProfilingNavKey
import com.st.demo_showcase.utils.toActions
import com.st.ui.composables.ActionItem
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.theme.Grey0
import com.st.user_profiling.ProfileViewModel
import com.st.user_profiling.composable.UserProfilingNavigationScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.st.ext_config.composable.FwUpgradeScreen
import com.st.ext_config.ui.fw_upgrade.FwUpgradeViewModel
import com.st.ui.theme.LocalDimensions
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import com.st.core.GlobalConfig
import com.st.demo_showcase.ui.debug_console.DebugConsoleViewModel
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseDownloadTermsNavKey
import com.st.demo_showcase.ui.log_settings.LogSettingsViewModel
import com.st.download_terms.StDownloadTermsConfig
import com.st.download_terms.composable.DownloadLicenseAgreementScreen
import com.st.pnpl.PnplViewModel
import com.st.pnpl.composable.StPnplScreen
import com.st.ui.composables.ComposableLifecycle
import com.st.user_profiling.StUserProfilingConfig
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType

@Composable
fun DemoShowCaseNavKeyScreen(
    modifier: Modifier = Modifier,
    nodeId: String,
    viewModel: DemoShowCaseViewModel
) {

    val activity = LocalActivity.current

    val backState =
        rememberNavBackStack(DemoShowCaseMainScreenNavKey)

    val showUpdateDialog by viewModel.showFwUpdate.collectAsStateWithLifecycle()
    val currentFw by viewModel.currentFw.collectAsStateWithLifecycle()
    val updateFw by viewModel.updateFw.collectAsStateWithLifecycle()
    val updateUrl by viewModel.updateUrl.collectAsStateWithLifecycle()
    val updateChangeLog by viewModel.updateChangeLog.collectAsStateWithLifecycle()

    GlobalConfig.disconnectFromNode = { viewModel.disconnect(nodeId = nodeId) }

    viewModel.onBack = {
        GlobalConfig.navigateBack3(nodeId)
    }

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                activity?.let {
                    viewModel.initLoginManager(it)
                }
                viewModel.initExpert()
                viewModel.initIsBeta()
                viewModel.checkIfDownloadTermsIsAccepted()
                viewModel.setNodeId(nodeId = nodeId)
            }

            else -> {}
        }
    }

    NavDisplay(
        modifier = modifier
            .background(
                Grey0
            ),
        backStack = backState,
        onBack = {
            backState.removeLastOrNull()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<DemoShowCaseMainScreenNavKey> {
                DemoShowCaseMainScreen(
                    nodeId = nodeId,
                    backState = backState,
                    viewModel = viewModel
                )
            }

            entry<DemoShowCaseDownloadTermsNavKey> {
                DownloadLicenseAgreementScreen(onLicenseAgree = { decision ->
                    backState.removeLastOrNull()
                    StDownloadTermsConfig.onDone(decision)
                })
            }

            entry<DemoShowCaseUserProfilingNavKey> {
                val viewModelProfile: ProfileViewModel = viewModel()

                StUserProfilingConfig.onDone = { level: LevelProficiency, type: ProfileType ->
                    viewModel.profileShow(level = level, type = type)
                    viewModel.setExpert(level)
                    backState.removeLastOrNull()
                }

                UserProfilingNavigationScreen(
                    viewModel = viewModelProfile
                )
            }

            entry<DemoShowCaseFwDirectUpdateNavKey> { key ->
                val fwUpdateViewModel: FwUpgradeViewModel = hiltViewModel()
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        0.dp
                    )
                ) {
                    DemoShowCaseTopBar(
                        demoName = "Fw Update",
                        boardActions = listOf(),
                        demoActions = listOf(),
                        showSettingsMenu = false
                    )

                    FwUpgradeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        viewModel = fwUpdateViewModel,
                        fwLock = key.fwLock,
                        nodeId = key.nodeId,
                        fwUrl = key.fwUrl
                    )
                }
            }

            entry<DemoShowCaseFwUpdateNavKey> { key ->
                val fwUpdateViewModel: FwUpgradeViewModel = hiltViewModel()
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        0.dp
                    )
                ) {
                    DemoShowCaseTopBar(
                        demoName = "Fw Update",
                        boardActions = listOf(),
                        demoActions = listOf(),
                        showSettingsMenu = false
                    )
                    FwUpgradeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        viewModel = fwUpdateViewModel,
                        fwLock = false,
                        nodeId = key.nodeId,
                        fwUrl = ""
                    )
                }

            }

            entry<DemoShowCasePnplSettingsNavKey> { key ->
                val demoViewModel: PnplViewModel = hiltViewModel()

                ComposableLifecycle { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            viewModel.setCurrentDemo(key.currentDemo)
                        }

                        else -> Unit
                    }
                }

                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        0.dp
                    )
                ) {
                    DemoShowCaseTopBar(
                        demoName = "Settings",
                        boardActions = listOf(),
                        demoActions = listOf(),
                        showSettingsMenu = false
                    )
                    StPnplScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = LocalDimensions.current.paddingNormal,
                                start = LocalDimensions.current.paddingNormal,
                                end = LocalDimensions.current.paddingNormal
                            ),
                        viewModel = demoViewModel,
                        nodeId = key.nodeId,
                        demoName = key.demoName
                    )
                }
            }

            entry<DemoShowCaseDebugConsoleNavKey> { key ->
                val demoViewModel: DebugConsoleViewModel = hiltViewModel()
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        0.dp
                    )
                ) {
                    DemoShowCaseTopBar(
                        demoName = "Debug Console",
                        boardActions = listOf(),
                        demoActions = listOf(),
                        showSettingsMenu = false
                    )
                    val debugMessages by demoViewModel.debugMessages.collectAsStateWithLifecycle()

                    ComposableLifecycle { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> {
                                viewModel.setCurrentDemo(null)
                                demoViewModel.receiveDebugMessage(nodeId = key.nodeId)
                            }

                            Lifecycle.Event.ON_STOP -> demoViewModel.stopReceivesMessage()
                            else -> Unit
                        }
                    }

                    DebugConsoleScreen(
                        debugMessages = debugMessages,
                        onClear = { demoViewModel.clearConsole() }
                    ) { msg ->
                        demoViewModel.sendDebugMessage(nodeId = key.nodeId, msg = msg)
                    }
                }
            }

            entry<DemoShowCaseLogSettingsNavKey> { key ->
                val demoViewModel: LogSettingsViewModel = hiltViewModel()

                ComposableLifecycle { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_STOP -> {
                            viewModel.setCurrentDemo(key.currentDemo)
                        }

                        else -> Unit
                    }
                }
                Column(
                    modifier = modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        0.dp
                    )
                ) {
                    DemoShowCaseTopBar(
                        demoName = "Log Settings",
                        boardActions = listOf(),
                        demoActions = listOf(),
                        showSettingsMenu = false
                    )
                    val isLogging by demoViewModel.isLogging.collectAsStateWithLifecycle()
                    val logType by demoViewModel.logType.collectAsStateWithLifecycle()
                    val numberLogs by demoViewModel.numberLogs.collectAsStateWithLifecycle()

                    val context = LocalContext.current

                    LaunchedEffect(key1 = Unit) {
                        demoViewModel.fetchLoggingStatus(nodeId = key.nodeId)
                        demoViewModel.checkLogDir()
                    }

                    LogSettingsScreen(
                        isLogging = isLogging,
                        logType = logType,
                        numberLogs = numberLogs,
                        onStartLog = {
                            demoViewModel.startLogging(nodeId)
                        },
                        onStopLog = {
                            demoViewModel.stopLogging(nodeId)
                        },
                        onClearLog = {
                            demoViewModel.clearLogging(nodeId)
                        },
                        onLogTypeChanged = {
                            demoViewModel.changeLogType(it)
                        },
                        onShareLog = {
                            demoViewModel.shareLog(context)
                        }
                    )
                }
            }

        }
    )

    UpdateAvailableDialog(
        show = showUpdateDialog,
        currentFw = currentFw,
        updateFw = updateFw,
        changeLog = updateChangeLog,
        onInstall = {
            if (viewModel.hasAcceptedDownloadTerms) {
                backState.add(DemoShowCaseFwDirectUpdateNavKey(nodeId, updateUrl))
            } else {
                StDownloadTermsConfig.fullScreen = true
                StDownloadTermsConfig.onDone = { decision ->
                    if (decision) {
                        viewModel.setDownloadTermsFlag(true)
                        backState.add(DemoShowCaseFwDirectUpdateNavKey(nodeId, updateUrl))
                    }
                }
                backState.add(DemoShowCaseDownloadTermsNavKey)
            }
        },
        dismissUpdateDialog = { viewModel.dismissUpdateDialog(it) })
}

@Composable
fun DemoShowCaseMainScreen(
    modifier: Modifier = Modifier,
    nodeId: String,
    backState: NavBackStack<NavKey>,
    viewModel: DemoShowCaseViewModel
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            0.dp
        )
    ) {
        val isDemoList by viewModel.isDemoList.collectAsStateWithLifecycle()

        BackHandler(isDemoList) {

            GlobalConfig.navigateBack3(nodeId)
        }

        val showSettingsMenu by viewModel.showSettingsMenu.collectAsStateWithLifecycle()
        val currentDemoSelected by viewModel.currentDemo.collectAsStateWithLifecycle()
        val hasPnplSettings by viewModel.hasPnplSettings.collectAsStateWithLifecycle()
        val isExpert by viewModel.isExpert.collectAsStateWithLifecycle()
        val device by viewModel.device.collectAsStateWithLifecycle()
        val hasDebugFeature by viewModel.hasDebugFeature.collectAsStateWithLifecycle()
        val openFwDBModelDialog = remember { mutableStateOf(false) }

        LaunchedEffect(key1 = currentDemoSelected) {
        }

        val demoActions = currentDemoSelected?.settings.toActions(
            backState = backState,
            nodeId = nodeId,
            demo = currentDemoSelected
        ) + if (hasPnplSettings) {
            listOf(
                ActionItem(
                    label = stringResource(R.string.st_demoShowcase_menuAction_demoSettings),
                    imageVector = Icons.Default.Settings,
                    action = {
                        currentDemoSelected!!.navigateToPnplSettings(backState, nodeId)
                    })
            )
        } else {
            emptyList()
        } + ActionItem(
            label = "Disconnect",
            imageVector = Icons.AutoMirrored.Filled.Logout,
            action = {
                viewModel.exitFromDemoShowCase(nodeId)
            }
        )

        val pickFileLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { fileUri ->
            if (fileUri != null) {
                viewModel.setDtmiModel(nodeId, fileUri)
            }
        }

        val boardActions by remember(key1 = isExpert) {
            derivedStateOf {
                val currentAction = mutableListOf<ActionItem>()
                if (isExpert) {
                    if (device?.catalogInfo != null) {
                        currentAction.add(
                            ActionItem(
                                label = "Show Fw DB Entry",
                                action = {
                                    openFwDBModelDialog.value = true
                                })
                        )
                    }
                    currentAction.add(
                        ActionItem(
                            label = "Add Custom DTDL Entry",
                            action = {
                                pickFileLauncher.launch(arrayOf(JSON_FILE_TYPE))
                            })
                    )


                    if (hasDebugFeature) {
                        currentAction.add(
                            ActionItem(
                                label = "Open Debug Console",
                                action = {
                                    backState.add(DemoShowCaseDebugConsoleNavKey(nodeId = nodeId))
                                })
                        )
                    }

                    if ((device?.familyType != Boards.Family.WB_FAMILY) && (device?.familyType != Boards.Family.WBA_FAMILY) && (device?.boardType != Boards.Model.WB0X_NUCLEO_BOARD)) {
                        currentAction.add(
                            ActionItem(
                                label = "Firmware Update",
                                action = {
                                    backState.add(DemoShowCaseFwUpdateNavKey(nodeId = nodeId))
                                })
                        )
                    }
                }
                currentAction.add(
                    ActionItem(
                        label = "Log Settings",
                        action = {
                            backState.add(
                                DemoShowCaseLogSettingsNavKey(
                                    nodeId = nodeId,
                                    currentDemo = currentDemoSelected
                                )
                            )
                        }
                    ))
                currentAction.toList()
            }
        }

        if (openFwDBModelDialog.value) {
            if (device?.catalogInfo != null) {
                ShowFwDBModel(
                    catalogInfo = device?.catalogInfo!!,
                    onDismissRequest = { openFwDBModelDialog.value = false })
            }
        }


        DemoShowCaseTopBar(
            demoName = currentDemoSelected?.displayName,
            boardActions = boardActions,
            demoActions = demoActions,
            showSettingsMenu = showSettingsMenu
        )

        DemoListNavKeyScreen(
            modifier = Modifier.weight(2f),
            nodeId = nodeId,
            externBackState = backState,
            viewModel = viewModel
        )
    }
}