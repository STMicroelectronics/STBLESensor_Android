/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import androidx.navigation.navArgument
import com.st.catalog.composable.BoardScreen
import com.st.catalog.composable.CatalogList
import com.st.catalog.composable.FirmwareList
import com.st.core.ARG_BOARD_ID
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CatalogFragment : Fragment() {

    private val viewModel: CatalogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val directNodeId = arguments?.getString(ARG_BOARD_ID)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    CatalogScreen(nodeId = directNodeId,viewModel = viewModel) {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogScreen(
    nodeId: String? = null,
    viewModel: CatalogViewModel,
    onCloseCatalog: () -> Unit = { /** NOOP**/ }
) {
    var jumpDirectFirstTime by remember { mutableStateOf(true) }

    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable(route = "list") {
            if (jumpDirectFirstTime) {
                jumpDirectFirstTime = false
                CatalogList(
                    nodeId = nodeId,
                    navController = navController,
                    viewModel = viewModel,
                    onBack = onCloseCatalog
                )
            } else {
                CatalogList(
                    navController = navController,
                    viewModel = viewModel,
                    onBack = onCloseCatalog
                )
            }
        }

        composable(
            route = "detail/{boardPart}",
            arguments = listOf(navArgument(name = "boardPart") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("boardPart")?.let { boardPart ->
                val boardId = viewModel.boardsDescription.value.first { it.boardPart == boardPart }.bleDevId
                BoardScreen(
                    boardId = boardId,
                    boardPart = boardPart,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }

        composable(
            route = "detail/{boardPart}/firmwares",
            arguments = listOf(navArgument(name = "boardPart") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("boardPart")?.let { boardPart ->

                FirmwareList(
                    boardPart = boardPart,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    BlueMSTheme {
        CatalogScreen(viewModel = hiltViewModel())
    }
}
