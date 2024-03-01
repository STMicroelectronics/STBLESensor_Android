/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.catalog.CatalogViewModel
import com.st.catalog.R
import com.st.catalog.availableDemos
import com.st.ui.composables.StTopBar
import com.st.ui.theme.Grey0
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.PrimaryBlue
import com.st.ui.theme.SecondaryBlue
import kotlinx.coroutines.launch

@Composable
fun CatalogList(
    modifier: Modifier = Modifier,
    nodeId: String? = null,
    navController: NavController,
    onBack: () -> Unit = { /** NOOP **/ },
    viewModel: CatalogViewModel = hiltViewModel()
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
                boards = boards.distinctBy { it.bleDevId },
                allBoards = boards,
                boardsDescription = boardsDescription.distinctBy { it.bleDevId },
                onBack = onBack,
                onBoardSelected = { bleDevId ->
                    navController.navigate(
                        "detail/${bleDevId}"
                    )
                }
            )
        }
    }
}

@Composable
fun CatalogList(
    modifier: Modifier = Modifier,
    boards: List<BoardFirmware>,
    allBoards: List<BoardFirmware>,
    boardsDescription: List<BoardDescription>,
    onBack: () -> Unit = { /** NOOP **/ },
    onBoardSelected: (String) -> Unit = { /** NOOP **/ }
) {
    var openFilter by remember { mutableStateOf(value = false) }
    var boardOrder by remember { mutableStateOf(value = BoardOrder.NONE) }
    var filters by remember { mutableStateOf(value = CatalogFilter()) }
    val filteredBoardDescriptions by remember(filters, boards, boardOrder) {
        derivedStateOf {
            val filteredBoards =
                if (filters.demoGroups.isEmpty()) {
                    boards.map {
                        boardsDescription.first { board ->
                            board.bleDevId == it.bleDevId
                        }
                    }
                } else {
                    allBoards.filter { board ->
                        filters.demoGroups.firstOrNull { group ->
                            board.availableDemos().flatMap { it.group }.map { it.name }
                                .contains(group)
                        } != null
                    }.distinctBy { it.bleDevId }.map {
                        boardsDescription.first { board ->
                            board.bleDevId == it.bleDevId
                        }
                    }
                }

            when (boardOrder) {
                BoardOrder.NONE -> filteredBoards
                BoardOrder.ALPHABETICAL -> filteredBoards.sortedBy { it.boardName }
                BoardOrder.RELEASE_DATE -> filteredBoards.sortedByDescending { it.releaseDate }
            }
        }
    }

    val releaseDatesPresent = boardsDescription.firstOrNull { it.releaseDate != null } != null

    Scaffold(modifier = modifier.fillMaxSize(),
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        floatingActionButton = {
            FloatingActionButton(
                backgroundColor = SecondaryBlue,
                shape = CircleShape,
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
        },
        bottomBar = {
            BottomAppBar(
                modifier = modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primary,
                contentColor = Grey0,
                cutoutShape = CircleShape,
                contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            ) { /** NOOP **/ }
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            var column by rememberSaveable {
                mutableStateOf(true)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "ListView",
                    style = MaterialTheme.typography.bodySmall
                )

                Icon(
                    modifier = Modifier.clickable { column = !column },
                    tint = PrimaryBlue,
                    imageVector = if (column) {
                        Icons.Default.GridView
                    } else {
                        Icons.Default.ViewList
                    },
                    contentDescription = "Grid/List View"
                )
            }

            if (column) {
                Box(modifier = Modifier.weight(1f)) {
                    val state = rememberLazyListState()

                    val coroutineScope = rememberCoroutineScope()

                    LazyColumn(
                        state = state,
                        modifier = Modifier
                            .padding(paddingValues = paddingValues),
                        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(filteredBoardDescriptions) {
                            CatalogListItem(
                                boardName = it.boardName,
                                boardVariant = it.boardVariant,
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
                                    onBoardSelected(it.bleDevId)
                                }
                            )
                        }
                    }

                    val showButton by remember {
                        derivedStateOf {
                            state.firstVisibleItemIndex > 0
                        }
                    }

                    if (showButton) {
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(LocalDimensions.current.paddingNormal),
                            contentColor = SecondaryBlue,
                            backgroundColor = Grey0,
                            shape = CircleShape,
                            onClick = {
                                coroutineScope.launch {
                                    state.animateScrollToItem(index = 0)
                                }
                            }) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                imageVector = Icons.Default.KeyboardDoubleArrowUp,
                                contentDescription = null
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    val state = rememberLazyGridState()

                    val coroutineScope = rememberCoroutineScope()

                    LazyVerticalGrid(
                        state = state,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .padding(paddingValues = paddingValues),
                        contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
                        verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal),
                        horizontalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
                    ) {
                        items(filteredBoardDescriptions) {
                            CatalogListThumbnailItem(
                                boardName = it.boardName,
                                boardVariant = it.boardVariant,
                                friendlyName = it.friendlyName,
                                boardStatus = it.status,
                                boardTypeName = it.boardModel().name,
                                releaseDate = if (boardOrder == BoardOrder.RELEASE_DATE) {
                                    it.releaseDate
                                } else {
                                    null
                                },
                                onClickItem = {
                                    onBoardSelected(it.bleDevId)
                                }
                            )
                        }
                    }

                    val showButton by remember {
                        derivedStateOf {
                            state.firstVisibleItemIndex > 0
                        }
                    }

                    if (showButton) {
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(LocalDimensions.current.paddingNormal),
                            contentColor = SecondaryBlue,
                            backgroundColor = Grey0,
                            shape = CircleShape,
                            onClick = {
                                coroutineScope.launch {
                                    state.animateScrollToItem(index = 0)
                                }
                            }) {
                            Icon(
                                tint = MaterialTheme.colorScheme.primary,
                                imageVector = Icons.Default.KeyboardDoubleArrowUp,
                                contentDescription = null
                            )
                        }
                    }
                }
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

@Preview(showBackground = true)
@Composable
private fun CatalogListPreview() {
    PreviewBlueMSTheme {
        CatalogList(
            boards = listOf(
                BoardFirmware.mock(),
                BoardFirmware.mock(),
                BoardFirmware.mock()
            ),
            allBoards = listOf(
                BoardFirmware.mock(),
                BoardFirmware.mock(),
                BoardFirmware.mock()
            ),
            boardsDescription = emptyList()
        )
    }
}
