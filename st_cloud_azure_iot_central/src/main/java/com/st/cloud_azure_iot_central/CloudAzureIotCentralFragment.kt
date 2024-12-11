/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.cloud_azure_iot_central

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.st.cloud_azure_iot_central.composable.CloudAzureIotCentralStartScreen
import com.st.ui.composables.ComposableLifecycle
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint
import com.st.core.ARG_NODE_ID

@AndroidEntryPoint
class CloudAzureIotCentralFragment : Fragment() {

    private val viewModel: CloudAzureIotCentralViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nodeId = arguments?.getString(ARG_NODE_ID)
            ?: throw IllegalArgumentException("Missing string $ARG_NODE_ID arguments")

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    CloudAzureIotCentralScreen(
                        modifier = Modifier
                            .fillMaxSize().padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                        viewModel = viewModel,
                        nodeId = nodeId
                    )
                }
            }
        }
    }
}

@Composable
fun CloudAzureIotCentralScreen(
    modifier: Modifier,
    viewModel: CloudAzureIotCentralViewModel,
    nodeId: String
) {

    ComposableLifecycle { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> viewModel.startDemo(nodeId = nodeId)
            Lifecycle.Event.ON_STOP -> viewModel.stopDemo(nodeId = nodeId)
            else -> Unit
        }
    }

    CloudAzureIotCentralStartScreen(
        modifier = modifier,
        viewModel = viewModel
    )
}


