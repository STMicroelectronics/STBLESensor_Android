/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.log_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.demo_showcase.ui.DemoShowCaseViewModel
import com.st.demo_showcase.ui.composable.LogSettingsScreen
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LogSettingsFragment : Fragment() {

    private val viewModel: LogSettingsViewModel by viewModels()
    private val demoViewModel: DemoShowCaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    val nodeId by demoViewModel.nodeId.collectAsStateWithLifecycle()
                    val isLogging by viewModel.isLogging.collectAsStateWithLifecycle()
                    val logType by viewModel.logType.collectAsStateWithLifecycle()
                    val numberLogs by viewModel.numberLogs.collectAsStateWithLifecycle()

                    LaunchedEffect(key1 = Unit) {
//                        demoViewModel.setCurrentDemo(null)
                        viewModel.fetchLoggingStatus(nodeId = nodeId)
                        viewModel.checkLogDir()
                    }

                    LogSettingsScreen(
                        isLogging = isLogging,
                        logType = logType,
                        numberLogs = numberLogs,
                        onStartLog = {
                            viewModel.startLogging(nodeId)
                        },
                        onStopLog = {
                            viewModel.stopLogging(nodeId)
                        },
                        onClearLog = {
                            viewModel.clearLogging(nodeId)
                        },
                        onLogTypeChanged = {
                            viewModel.changeLogType(it)
                        },
                        onShareLog = {
                            viewModel.shareLog(this.context)
                        }
                    )
                }
            }
        }
    }
}