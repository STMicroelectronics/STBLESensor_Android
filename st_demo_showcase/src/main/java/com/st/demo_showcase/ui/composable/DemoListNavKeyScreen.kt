package com.st.demo_showcase.ui.composable

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.st.demo_showcase.ui.DemoShowCaseViewModel
import com.st.demo_showcase.ui.demo_list.*
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseFwDirectUpdateNavKey
import com.st.demo_showcase.ui.demo_show_case.DemoShowCaseUserProfilingNavKey
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.PrimaryYellow

@Composable
fun DemoListNavKeyScreen(
    modifier: Modifier = Modifier,
    nodeId: String,
    viewModel: DemoShowCaseViewModel,
    externBackState: NavBackStack<NavKey>,
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isExpert by viewModel.isExpert.collectAsStateWithLifecycle()
    val demos by viewModel.availableDemo.collectAsStateWithLifecycle()
    val device by viewModel.device.collectAsStateWithLifecycle()
    val statusModelDTMI by viewModel.statusModelDTMI.collectAsStateWithLifecycle()
    val pinnedDevices by viewModel.pinnedDevices.collectAsStateWithLifecycle(
        emptyList()
    )

    val isBeta by viewModel.isBeta.collectAsStateWithLifecycle()

    val fwUpdateAvailable by viewModel.fwUpdateAvailable.collectAsStateWithLifecycle()

    val backState =
        rememberNavBackStack(DemoListNavKey)

//    LaunchedEffect(key1 = Unit) {
//        viewModel.setCurrentDemo(null)
//    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { fileUri ->
        if (fileUri != null) {
            viewModel.setDtmiModel(nodeId, fileUri)
        }
    }

    LaunchedEffect(key1 = backState.lastOrNull()) {
        viewModel.onNavKeyChange(backState)
    }

    NavDisplay(
        modifier = modifier
            .background(
                Grey0
            ),
        backStack = backState,
        onBack = {
            backState.removeLastOrNull()
            viewModel.setCurrentDemo(null)
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {

            //Demos List screen
            entry<DemoListNavKey> {
                val customNames by viewModel.customNames.collectAsStateWithLifecycle(emptyList())

                var showChangeNameForNodeId: String? by remember { mutableStateOf(null) }

                DemoListScreen(
                    modifier = modifier,
                    device = device,
                    pinnedDevices = pinnedDevices,
                    isLoggedIn = isLoggedIn,
                    isExpert = isExpert,
                    isBetaApplication = isBeta,
                    fwUpdateAvailable = fwUpdateAvailable,
                    availableDemos = demos,
                    onDemoReordered = { from, to ->
                        viewModel.saveReorder(from, to)
                    },
                    onPinChange = { isPin ->
                        device?.device?.address?.let {
                            if (isPin) {
                                viewModel.addToPinDevices(it)
                            } else {
                                viewModel.removeFromPinDevices(it)
                            }
                        }
                    },
                    showEdit = true,
                    boardHasCustomName = customNames.firstOrNull { it.first == device?.device?.address }?.second,
                    onCustomNameSelected = {
                        device?.device?.let { nodeId ->
                            showChangeNameForNodeId = nodeId.address
                        }
                    },
                    onDemoSelected = { selectedDemo ->
                        viewModel.setCurrentDemo(selectedDemo)

                        selectedDemo.navigateTo(
                            backState = backState,
                            nodeId = nodeId,
                            isExpert = isExpert
                        )
                    },
                    onLoginRequired = {
                        viewModel.login()
                    },
                    onExpertRequired = {
                        externBackState.add(DemoShowCaseUserProfilingNavKey)
                    },
                    onLastFwRequired = {

                        externBackState.add(
                            DemoShowCaseFwDirectUpdateNavKey(
                                nodeId,
                                viewModel.updateUrl.value
                            )
                        )
                    },
                    statusModelDTMI = statusModelDTMI,
                    onCustomDTMIClicked = { pickFileLauncher.launch(arrayOf(JSON_FILE_TYPE)) }
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
                                            boardTypeName = device?.boardType?.name ?: ""
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
                                        boardTypeName = device?.boardType?.name ?: ""
                                    )
                                    showChangeNameForNodeId = null
                                }
                            )
                        }
                    )
                }
            }

            //Demos
            FlowDemo()
            BeamForming()
            Environmental()
            Level()
            FitnessActivity()
            Compass()
            HighSpeedDataLog2()
            SimpleHighSpeedDataLog2()
            AIoTCraftHighSpeedDataLog2()
            BlueVoiceOpus()
            BlueVoiceFullDuplex()
            NavigationGesture()
            NEAIAnomalyDetection()
            NEAIClassification()
            NEAIExtrapolation()
            EventCounter()
            Piano()
            PnPL()
            Plot()
            NfcWriting()
            BinaryContent()
            ExtConfiguration()
            ToFObjectsDetection()
            ColorAmbientLight()
            GNSS()
            MotionIntensity()
            ActivityRecognition()
            CarryPosition()
            MemsGesture()
            MotionAlgorithms()
            Pedometer()
            ProximityGestureRecognition()
            Switch()
            Registers()
            AccelerationEvent()
            SourceLocalization()
            AudioClassification()
            LedControl()
            NodeStatus()
            TextualMonitor()
            HeartRate()
            SensorFusion()
            PredictedMaintenance()
            FftAmplitude()
            MultiNeuralNetwork()
            ExternalApplication()
            AssetTracking()
            RawPnPL()
            MotorControl()
            CloudAzureIoTCentral()
            CloudMQTT()
            MedicalSignal()
            FUOTA(externBackState, backState)
            HeadBoneConduction()
        },
        transitionSpec = {
            // Slide in from right when navigating forward
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it })
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        })
}