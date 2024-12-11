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
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import com.st.blue_sdk.board_catalog.models.BoardStatus
import com.st.ui.theme.*
import com.st.ui.utils.asString
import com.st.ui.utils.getBlueStBoardImages

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CatalogListItem(
    modifier: Modifier = Modifier,
    boardPart: String,
    friendlyName: String? = null,
    boardStatus: BoardStatus? = null,
    description: String? = null,
    releaseDate: String? = null,
    boardTypeName: String,
    onClickItem: () -> Unit = { /** NOOP **/ },
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    with(sharedTransitionScope) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height = LocalDimensions.current.catalogCardHeight)
            .sharedElement(
                sharedTransitionScope.rememberSharedContentState(key = "surface-${boardPart}"),
                animatedVisibilityScope = animatedContentScope
            ),
        shape = Shapes.small,
        shadowElevation = LocalDimensions.current.elevationNormal,
        onClick = onClickItem
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.surfaceVariant),
            verticalAlignment = Alignment.CenterVertically
        ) {

                Image(
                    modifier = Modifier
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .size(size = LocalDimensions.current.imageMedium)
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "image-${boardTypeName}"),
                            animatedVisibilityScope = animatedContentScope
                        ),
                    painter = painterResource(id = getBlueStBoardImages(boardTypeName)),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .fillMaxHeight()
                        .background(color = MaterialTheme.colorScheme.background)
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .padding(start = LocalDimensions.current.paddingMedium)
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall)
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "text-${boardPart}"),
                                animatedVisibilityScope = animatedContentScope
                            ),
                        text = boardPart,
                        maxLines = TITLE_MAX_LINES,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )



                    if (friendlyName != null) {
                        Text(
                            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall)
                            .sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(key = "friendly-${friendlyName}"),
                            animatedVisibilityScope = animatedContentScope
                        ),
                            text = friendlyName,
                            maxLines = SUBTITLE_MAX_LINES,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (releaseDate != null) {
                        Text(
                            modifier = Modifier.padding(bottom = LocalDimensions.current.paddingSmall),
                            text = releaseDate.replace('_', '/'),
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }


                    if (description != null) {
                        Text(
                            text = description,
                            maxLines = DESCRIPTION_MAX_LINES,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    when (boardStatus) {
                        BoardStatus.ACTIVE -> Text(
                            modifier = Modifier
                                .padding(
                                    end = LocalDimensions.current.paddingSmall,
                                    bottom = LocalDimensions.current.paddingSmall
                                )
                                .fillMaxWidth().sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(key = "statusAct-${boardPart}"),
                                    animatedVisibilityScope = animatedContentScope
                                ),
                            textAlign = TextAlign.Right,
                            text = boardStatus.name,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = SuccessText
                        )

                        BoardStatus.NRND -> Text(
                            modifier = Modifier
                                .padding(
                                    end = LocalDimensions.current.paddingSmall,
                                    bottom = LocalDimensions.current.paddingSmall
                                ).sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(key = "statusNrn-${boardPart}"),
                                    animatedVisibilityScope = animatedContentScope
                                ),
                            text = boardStatus.name,
                            textAlign = TextAlign.Right,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorText
                        )

                        else -> {}
                    }
                }
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

//@Preview(showBackground = true)
//@Composable
//private fun CatalogListItemPreview() {
//    PreviewBlueMSTheme {
//        CatalogListItem(
//            boardPart = "BlueCoin Starter Kit",
//            friendlyName = "STEVAL-BCNKT01V1",
//            boardStatus = BoardStatus.NRND,
//            description = LoremIpsum(words = 20).asString(),
//            boardTypeName = "BLUE_COIN",
//            releaseDate = "2022_Q1"
//        )
//    }
//}
