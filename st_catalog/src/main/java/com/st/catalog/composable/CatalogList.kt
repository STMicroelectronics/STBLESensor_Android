/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import com.st.ui.theme.SecondaryBlue

@Composable
fun CatalogList(
    modifier: Modifier = Modifier,
    navController: NavController,
    onBack: () -> Unit = { /** NOOP **/ },
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val boards by viewModel.boards.collectAsStateWithLifecycle()
    val boardsDescription by viewModel.boardsDescription.collectAsStateWithLifecycle()
    CatalogList(
        modifier = modifier,
        boards = boards.distinctBy { it.bleDevId },
        allBoards = boards,
        boardsDescription = boardsDescription.distinctBy { it.bleDevId },
        onBack = onBack,
        onBoardSelected = { board ->
            navController.navigate(
                "detail/${board.bleDevId}"
            )
        }
    )
}

@Composable
fun CatalogList(
    modifier: Modifier = Modifier,
    boards: List<BoardFirmware>,
    allBoards: List<BoardFirmware>,
    boardsDescription: List<BoardDescription>,
    onBack: () -> Unit = { /** NOOP **/ },
    onBoardSelected: (BoardFirmware) -> Unit = { /** NOOP **/ }
) {
    var openFilter by remember { mutableStateOf(value = false) }
    var filters by remember { mutableStateOf(value = CatalogFilter()) }
    val filteredBoards by remember(key1 = filters, boards) {
        derivedStateOf {
            if (filters.demoGroups.isEmpty()) {
                boards
            } else {
                allBoards.filter {board ->
                    filters.demoGroups.firstOrNull { group ->
                        board.availableDemos().flatMap { it.group }.map { it.name }.contains(group)
                    } != null
                }.distinctBy { it.bleDevId }
            }
        }
    }
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
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues = paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            items(filteredBoards) {
                val boardDescription = boardsDescription.firstOrNull { board->
                    board.bleDevId == it.bleDevId
                }

                if(boardDescription!=null) {
                    CatalogListItem(
                        boardName = boardDescription.boardName,
                        friendlyName = "(${boardDescription.friendlyName})",
                        boardStatus = boardDescription.status,
                        description = boardDescription.description,
                        boardTypeName = it.boardModel().name,
                        onClickItem = {
                            onBoardSelected(it)
                        }
                    )
                } else {
                    CatalogListItem(
                        boardName = it.brdName,
                        boardTypeName = it.boardModel().name,
                        onClickItem = {
                            onBoardSelected(it)
                        }
                    )
                }
            }
        }
    }

    if (openFilter) {
        Dialog(onDismissRequest = { openFilter = false }) {
            CatalogFilterDialog(
                filters = filters
            ) {
                filters = it
                openFilter = false
            }
        }
    }
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
            allBoards =  listOf(
                BoardFirmware.mock(),
                BoardFirmware.mock(),
                BoardFirmware.mock()
            ),
            boardsDescription = emptyList()
        )
    }
}
