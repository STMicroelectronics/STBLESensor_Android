/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.demo_showcase.ui.debug_console

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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.st.demo_showcase.ui.DemoShowCaseViewModel
import com.st.demo_showcase.ui.composable.DebugConsoleScreen
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DebugConsoleFragment : Fragment() {

    private val viewModel: DebugConsoleViewModel by viewModels()
    private val demoViewModel: DemoShowCaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    val nodeId by demoViewModel.nodeId.collectAsStateWithLifecycle()
                    val debugMessages by viewModel.debugMessages.collectAsStateWithLifecycle()


                    ComposableLifecycle { _, event ->
                        when (event) {
                            Lifecycle.Event.ON_START -> {
                                demoViewModel.setCurrentDemo(null)
                                viewModel.receiveDebugMessage(nodeId = nodeId)
                            }

                            Lifecycle.Event.ON_STOP -> viewModel.stopReceivesMessage()
                            else -> Unit
                        }
                    }

//                    LaunchedEffect(key1 = Unit) {
//                        demoViewModel.setCurrentDemo(null)
//                        viewModel.receiveDebugMessage(nodeId = nodeId)
//                    }

                    DebugConsoleScreen(
                        debugMessages = debugMessages,
                        onClear = { viewModel.clearConsole() }
                    ) { msg ->
                        viewModel.sendDebugMessage(nodeId = nodeId, msg = msg)
                    }
                }
            }
        }
    }
}
