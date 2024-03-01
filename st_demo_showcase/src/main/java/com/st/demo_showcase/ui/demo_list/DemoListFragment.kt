/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.demo_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.st.demo_showcase.ui.DemoShowCaseViewModel
import com.st.demo_showcase.ui.composable.DemoListScreen
import com.st.ui.composables.JSON_FILE_TYPE
import com.st.ui.theme.BlueMSTheme
import com.st.user_profiling.StUserProfilingConfig
import com.st.user_profiling.model.LevelProficiency
import com.st.user_profiling.model.ProfileType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DemoListFragment : Fragment() {

    private val viewModel: DemoShowCaseViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.initLoginManager(requireActivity())
        viewModel.initExpert()
        viewModel.initIsBeta()

        StUserProfilingConfig.onDone = { level: LevelProficiency, type: ProfileType ->
            viewModel.profileShow(level = level, type = type)
            val direction = DemoListFragmentDirections.actionUserProfilingNavGraphToDemoList()
            findNavController().navigate(directions = direction)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    val nodeId by viewModel.nodeId.collectAsStateWithLifecycle()
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

                    LaunchedEffect(key1 = Unit) {
                        viewModel.setCurrentDemo(null)
                    }

                    val pickFileLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.OpenDocument()
                    ) { fileUri ->
                        if (fileUri != null) {
                            viewModel.setDtmiModel(nodeId, fileUri)
                        }
                    }

                    DemoListScreen(
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
                        onDemoSelected = { selectedDemo ->
                            viewModel.setCurrentDemo(selectedDemo)

                            selectedDemo.navigateTo(
                                navController = findNavController(),
                                nodeId = nodeId,
                                isExpert = isExpert
                            )
                        },
                        onLoginRequired = {
                            viewModel.login()
                        },
                        onExpertRequired = {
                            val direction =
                                DemoListFragmentDirections.actionDemoListToUserProfilingNavGraph()
                            findNavController().navigate(directions = direction)
                        },
                        onLastFwRequired = {
                            findNavController().navigate(directions = DemoListFragmentDirections.actionDemoListToFwUpgrade(
                                nodeId,
                                viewModel.updateUrl.value
                            ))
                        },
                        statusModelDTMI = statusModelDTMI,
                        onCustomDTMIClicked = { pickFileLauncher.launch(arrayOf(JSON_FILE_TYPE)) }
                    )
                }
            }
        }
    }
}
