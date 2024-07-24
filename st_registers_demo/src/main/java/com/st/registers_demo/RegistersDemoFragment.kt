/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.registers_demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.st.core.ARG_DEMO_TYPE
import com.st.core.ARG_NODE_ID
import com.st.registers_demo.common.RegistersDemoType
import com.st.registers_demo.composable.RegisterDemoContent
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistersDemoFragment : Fragment() {


    private val viewModel: RegistersDemoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        val demoTypeString = arguments?.getString(ARG_DEMO_TYPE)
            ?: "MLC"

        var demoType: RegistersDemoType = RegistersDemoType.MLC

        try {
            demoType = RegistersDemoType.valueOf(demoTypeString)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    RegistersDemoScreen(
                        modifier = Modifier
                            .fillMaxSize(),
                        demoType = demoType,
                        viewModel = viewModel,
                        nodeId = nodeId
                    )
                }
            }
        }
    }
}

@Composable
fun RegistersDemoScreen(
    modifier: Modifier,
    demoType: RegistersDemoType,
    viewModel: RegistersDemoViewModel,
    nodeId: String
) {

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> {
                viewModel.startDemo(nodeId = nodeId, demoType = demoType)
            }

            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    RegisterDemoContent(
        modifier = modifier,
        viewModel = viewModel,
        demoType = demoType
    )
}
