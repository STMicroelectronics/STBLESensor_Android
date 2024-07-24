/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.ext_config.ui.fw_upgrade

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.st.ext_config.composable.FwUpgradeScreen
import com.st.ui.theme.BlueMSTheme
import com.st.ui.theme.LocalDimensions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FwUpgradeFragment : Fragment() {

    private val viewModel: FwUpgradeViewModel by viewModels()
    private val navArgs: FwUpgradeFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nodeId = navArgs.nodeId
        val url = navArgs.url

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    FwUpgradeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        viewModel = viewModel,
                        nodeId = nodeId,
                        fwUrl = url,
                        fwLock = false
                    )
                }
            }
        }
    }
}
