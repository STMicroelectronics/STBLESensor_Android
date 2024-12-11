/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.catalog.CatalogViewModel
import com.st.catalog.R
import com.st.catalog.availableDemos
import com.st.ui.composables.StTopBar
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.SecondaryBlue

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CatalogList(
    modifier: Modifier = Modifier,
    nodeId: String? = null,
    navController: NavController,
    onBack: () -> Unit = { /** NOOP **/ },
    viewModel: CatalogViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    if (nodeId != null) {
        //remove the catalog list fragment before to navigate to board details
        navController.popBackStack()
        navController.navigate(
            "detail/${nodeId}"
        )
    } else {
        val boards by viewModel.boards.collectAsStateWithLifecycle()
        val boardsDescription by viewModel.boardsDescription.collectAsStateWithLifecycle()

        if (boards.isNotEmpty() && boardsDescription.isNotEmpty()) {
            CatalogList(
                modifier = modifier,
                boardFirmwares = boards,
                allBoardFirmwares = boards,
                boardsDescription = boardsDescription,
                isBeta = viewModel.isBeta,
                onBack = onBack,
                onBoardSelected = { boardPart ->
                    navController.navigate(
                        "detail/${boardPart}"
                    )
                },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CatalogList(
    modifier: Modifier = Modifier,
    boardFirmwares: List<BoardFirmware>,
    allBoardFirmwares: List<BoardFirmware>,
    boardsDescription: List<BoardDescription>,
    isBeta: Boolean = false,
    onBack: () -> Unit = { /** NOOP **/ },
    onBoardSelected: (String) -> Unit = { /** NOOP **/ },
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    var openFilter by remember { mutableStateOf(value = false) }
    var boardOrder by remember { mutableStateOf(value = BoardOrder.NONE) }
    var filters by remember { mutableStateOf(value = CatalogFilter()) }
    val filteredBoardDescriptions by remember(
        filters,
        boardsDescription,
        boardFirmwares,
        boardOrder
    ) {
        derivedStateOf {
            val filteredBoards =
                if (filters.demoGroups.isEmpty()) {
                    if (isBeta) {
                        boardsDescription
                    } else {
                        boardFirmwares.map {
                            boardsDescription.first { board ->
                                board.bleDevId == it.bleDevId
                            }
                        }
                    }

                } else {
                    allBoardFirmwares.filter { board ->
                        filters.demoGroups.firstOrNull { group ->
                            board.availableDemos().flatMap { it.group }.map { it.name }
                                .contains(group)
                        } != null
                    }.map {
                        boardsDescription.first { board ->
                            board.bleDevId == it.bleDevId
                        }
                    }
                }

            when (boardOrder) {
                BoardOrder.NONE -> filteredBoards.distinctBy { it.boardPart }
                BoardOrder.ALPHABETICAL -> filteredBoards.distinctBy { it.boardPart }
                    .sortedBy { it.boardName }

                BoardOrder.RELEASE_DATE -> filteredBoards.distinctBy { it.boardPart }
                    .sortedByDescending { it.releaseDate }
            }
        }
    }

    val releaseDatesPresent = boardsDescription.firstOrNull { it.releaseDate != null } != null

    Scaffold(modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                containerColor = SecondaryBlue,
                onClick = { openFilter = true }) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null
                )
            }
        },
        topBar = {
            StTopBar(
                title = stringResource(id = R.string.st_catalog_boardList_title),
                onBack = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                //.fillMaxSize()
                .consumeWindowInsets(paddingValues = paddingValues)
                .padding(paddingValues = paddingValues),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(filteredBoardDescriptions) {
                CatalogListItem(
                    boardPart = it.boardPart,
                    friendlyName = it.friendlyName,
                    boardStatus = it.status,
                    description = it.description,
                    boardTypeName = it.boardModel().name,
                    releaseDate = if (boardOrder == BoardOrder.RELEASE_DATE) {
                        it.releaseDate
                    } else {
                        null
                    },
                    onClickItem = {
                        onBoardSelected(it.boardPart)
                    },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }

            item {
                Spacer(
                    Modifier.windowInsetsBottomHeight(
                        WindowInsets.systemBars
                    )
                )
            }
        }
    }

    if (openFilter) {
        Dialog(onDismissRequest = { openFilter = false }) {
            CatalogFilterDialog(
                boardOrder = boardOrder,
                filters = filters,
                releaseDatesPresent = releaseDatesPresent
            ) { catalogFilter, boardOrdering ->
                filters = catalogFilter
                boardOrder = boardOrdering
                openFilter = false
            }
        }
    }
}

enum class BoardOrder {
    NONE,
    RELEASE_DATE,
    ALPHABETICAL
}

/** ----------------------- PREVIEW --------------------------------------- **/

//@Preview(showBackground = true)
//@Composable
//private fun CatalogListPreview() {
//    PreviewBlueMSTheme {
//        CatalogList(
//            boardFirmwares = listOf(
//                BoardFirmware.mock(),
//                BoardFirmware.mock(),
//                BoardFirmware.mock()
//            ),
//            allBoardFirmwares = listOf(
//                BoardFirmware.mock(),
//                BoardFirmware.mock(),
//                BoardFirmware.mock()
//            ),
//            boardsDescription = emptyList()
//        )
//    }
//}
