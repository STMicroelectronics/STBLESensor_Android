/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.FirmwareMaturity
import com.st.blue_sdk.board_catalog.models.FotaDetails
import com.st.catalog.CatalogViewModel
import com.st.catalog.R
import com.st.catalog.StCatalogConfig
import com.st.catalog.availableDemos
import com.st.demo_showcase.models.Demo
import com.st.demo_showcase.ui.composable.DemoListItem
import com.st.ui.composables.BlueMsButton
import com.st.ui.composables.StTopBar
import com.st.ui.theme.LocalDimensions

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    boardId: String,
    boardPart: String,
    viewModel: CatalogViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    LaunchedEffect(key1 = boardId, key2 = boardPart) {
        viewModel.getBoard(boardId)
        viewModel.getFirmwareListForBoardPart(boardPart)
    }

    val context = LocalContext.current
    val boardOrNull by viewModel.board.collectAsStateWithLifecycle()
    val boardDescOrNull by viewModel.boardDescription.collectAsStateWithLifecycle()

    val firmwareList by viewModel.firmwareList.collectAsStateWithLifecycle()

    if (boardOrNull != null) {
        val allDemo by remember(key1 = firmwareList) {
            derivedStateOf {
                val demosSet = mutableSetOf<Demo>()
                val firmwareListWithoutDuplicate =  firmwareList.groupBy { it.fwName }.map { list -> list.value.distinctBy { it.fwVersion } }.flatten()
                firmwareListWithoutDuplicate.forEach {
                    demosSet.addAll(it.availableDemos())
                }
                demosSet
            }
        }

        BoardScreen(
            modifier = modifier,
            board = boardOrNull!!,
            boardDescOrNull = boardDescOrNull,
            demos = allDemo.toList(),
            goToFw = {
                navController.navigate(
                    "detail/$boardPart/firmwares"
                )
            },
            onBack = {
                navController.popBackStack()
            },
            goToDs = {
                Intent(Intent.ACTION_VIEW).also { intent ->
                    var uri = "https://www.st.com/content/st_com/en.html"
                    boardDescOrNull?.let {
                        if (boardDescOrNull!!.docURL != null) {
                            uri = boardDescOrNull!!.docURL!!
                        }
                    }
                    intent.data = Uri.parse(uri)
                    context.startActivity(intent)
                }
            },
            onReadMoreClick = {
                Intent(Intent.ACTION_VIEW).also { intent ->
                    var uri = "https://www.st.com/content/st_com/en.html"
                    boardDescOrNull?.let {
                        if (boardDescOrNull!!.orderURL != null) {
                            uri = boardDescOrNull!!.orderURL!!
                        }
                    }

                    intent.data = Uri.parse(uri)
                    context.startActivity(intent)
                }
            },
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope
        )
    } else {
        boardDescOrNull?.let { boardDesc ->
            BoardScreen(
                modifier = modifier,
                board = BoardFirmware(
                    bleDevId = boardDesc.bleDevId,
                    bleFwId = "",
                    brdName = boardDesc.boardName,
                    fwVersion = "",
                    fwName = "",
                    cloudApps = emptyList(),
                    characteristics = emptyList(),
                    optionBytes = emptyList(),
                    fwDesc = "",
                    fota = FotaDetails(),
                    maturity = FirmwareMaturity.RELEASE
                ),
                boardDescOrNull = boardDescOrNull,
                demos = emptyList(),
                showGoToFw = false,
                onBack = {
                    navController.popBackStack()
                },
                goToDs = {
                    Intent(Intent.ACTION_VIEW).also { intent ->
                        var uri = "https://www.st.com/content/st_com/en.html"
                        boardDescOrNull?.let {
                            if (boardDescOrNull!!.docURL != null) {
                                uri = boardDescOrNull!!.docURL!!
                            }
                        }
                        intent.data = Uri.parse(uri)
                        context.startActivity(intent)
                    }
                },
                onReadMoreClick = {
                    Intent(Intent.ACTION_VIEW).also { intent ->
                        var uri = "https://www.st.com/content/st_com/en.html"
                        boardDescOrNull?.let {
                            if (boardDescOrNull!!.orderURL != null) {
                                uri = boardDescOrNull!!.orderURL!!
                            }
                        }

                        intent.data = Uri.parse(uri)
                        context.startActivity(intent)
                    }
                },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BoardScreen(
    modifier: Modifier = Modifier,
    board: BoardFirmware,
    boardDescOrNull: BoardDescription? = null,
    demos: List<Demo>,
    showGoToFw: Boolean = true,
    onBack: () -> Unit = { /** NOOP **/ },
    goToFw: () -> Unit = { /** NOOP **/ },
    goToDs: () -> Unit = { /** NOOP **/ },
    onReadMoreClick: () -> Unit = { /** NOOP **/ },
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    Column(modifier = modifier.fillMaxSize()) {

        StTopBar(
            title = stringResource(id = R.string.st_catalog_board_title),
            onBack = onBack
        )

        Spacer(modifier = Modifier.height(height = LocalDimensions.current.paddingNormal))

        LazyColumn(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(all = LocalDimensions.current.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(space = LocalDimensions.current.paddingNormal)
        ) {
            item {
                BoardHeader(
                    board = board,
                    boardDescOrNull = boardDescOrNull,
                    showGoToFw = showGoToFw,
                    goToFw = goToFw,
                    goToDs = goToDs,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope
                )
            }

            if (demos.isNotEmpty()) {
                if (StCatalogConfig.showDemoList) {
                    item {
                        Text(
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            text = stringResource(id = R.string.st_catalog_board_demoListLabel)
                        )
                    }

                    itemsIndexed(items = demos) { index, demo ->
                        DemoListItem(
                            item = demo,
                            even = index % 2 == 0,
                            isLastOne = index == demos.lastIndex
                        )
                    }
                }
            }

            boardDescOrNull?.let {
                if (boardDescOrNull.videoURL != null) {
                    item {
                        // BoardVideoPlayer()
                        BoardYouTubePlayer(videoId = boardDescOrNull.videoURL!!.removePrefix("https://www.youtube.com/watch?v="))
                    }
                }
            }
        }

        boardDescOrNull?.let {
            if (boardDescOrNull.orderURL != null) {
                BlueMsButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LocalDimensions.current.paddingLarge)
                        .padding(vertical = LocalDimensions.current.paddingNormal),
                    onClick = onReadMoreClick,
                    text = stringResource(id = R.string.st_catalog_board_readMoreBtn)
                )
            }
        }

        Spacer(
            Modifier.windowInsetsBottomHeight(
                WindowInsets.systemBars
            )
        )
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

//@Preview(showBackground = true)
//@Composable
//private fun BoardScreenPreview() {
//    PreviewBlueMSTheme {
//        BoardScreen(
//            board = BoardFirmware.mock(),
//            demos = Demo.values().toList()
//        )
//    }
//}
