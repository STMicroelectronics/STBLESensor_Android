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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.findNavController
import androidx.navigation.navArgument
import com.st.catalog.composable.BoardScreen
import com.st.catalog.composable.CatalogList
import com.st.catalog.composable.FirmwareList
import com.st.ui.theme.BlueMSTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CatalogFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BlueMSTheme {
                    CatalogScreen {
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogScreen(
    onCloseCatalog: () -> Unit = { /** NOOP**/ }
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable(route = "list") {
            CatalogList(
                navController = navController,
                onBack = onCloseCatalog
            )
        }

        composable(
            route = "detail/{boardId}",
            arguments = listOf(navArgument(name = "boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("boardId")?.let { boardId ->
                BoardScreen(
                    boardId = boardId,
                    navController = navController
                )
            }
        }

        composable(
            route = "detail/{boardId}/firmwares",
            arguments = listOf(navArgument(name = "boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            backStackEntry.arguments?.getString("boardId")?.let { boardId ->
                FirmwareList(
                    boardId = boardId,
                    navController = navController
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
        CatalogScreen()
    }
}
