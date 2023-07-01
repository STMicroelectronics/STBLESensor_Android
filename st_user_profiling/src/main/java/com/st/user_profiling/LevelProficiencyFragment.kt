/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.user_profiling

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.st.ui.theme.BlueMSTheme
import com.st.user_profiling.composable.LevelProficiencyScreen

class LevelProficiencyFragment : Fragment() {
    private val viewModel: ProfileViewModel by navGraphViewModels(
        navGraphId = R.id.user_profiling_nav_graph
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(context = requireContext()).apply {
            setViewCompositionStrategy(
                strategy = ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                BlueMSTheme {
                    LevelProficiencyScreen(
                        viewModel = viewModel,
                        navController = findNavController()
                    )
                }
            }
        }
    }
}
