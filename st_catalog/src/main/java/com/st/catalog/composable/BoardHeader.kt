/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.BoardStatus
import com.st.catalog.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Grey6
import com.st.ui.theme.SuccessText
import com.st.ui.utils.getBlueStBoardImages

@Composable
fun BoardHeader(
    modifier: Modifier = Modifier,
    board: BoardFirmware,
    boardDescOrNull :BoardDescription?=null,
    goToFw: () -> Unit = { /** NOOP **/ },
    goToDs: () -> Unit = { /** NOOP **/ }
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(size = LocalDimensions.current.cornerNormal),
        shadowElevation = LocalDimensions.current.elevationNormal
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = LocalDimensions.current.paddingNormal),
                text = board.brdName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            if(boardDescOrNull!=null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal),
                    text = "(${boardDescOrNull.friendlyName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Grey6,
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = LocalDimensions.current.imageMedium)
                    //.background(color = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Image(
                    modifier = Modifier
                        .padding(all = LocalDimensions.current.paddingNormal)
                        .size(size = LocalDimensions.current.imageLarge)
                        //.fillMaxHeight()
                        .align(alignment = Alignment.Center),
                    painter = painterResource(id = getBlueStBoardImages(boardType = board.boardModel().name)),
                    contentDescription = null
                )
            }

            if(boardDescOrNull!=null) {
                when(boardDescOrNull.status) {
                    BoardStatus.ACTIVE ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        textAlign = TextAlign.Right,
                        style = MaterialTheme.typography.bodySmall,
                        color = SuccessText,
                        text = boardDescOrNull.status.name
                    )
                    BoardStatus.NRND ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = LocalDimensions.current.paddingNormal),
                            textAlign = TextAlign.Right,
                            style = MaterialTheme.typography.bodySmall,
                            color = ErrorText,
                            text = boardDescOrNull.status.name
                        )
                }
            }

            Divider()

            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = goToFw) {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryBlue,
                        text = stringResource(id = R.string.st_catalog_board_fwBtn)
                    )
                }

                Spacer(modifier = Modifier.width(width = LocalDimensions.current.paddingNormal))

                boardDescOrNull?.let {
                    if(boardDescOrNull.docURL!=null) {
                        TextButton(onClick = goToDs) {
                            Text(
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryBlue,
                                text = stringResource(id = R.string.st_catalog_board_dsBtn)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardHeaderPreview() {
    PreviewBlueMSTheme {
        BoardHeader(
            board = BoardFirmware.mock()
//            board = BoardFirmware(
//                bleDevId = "0x0A",
//                bleFwId = "0x0F",
//                brdName = "STEVAL-BCNKT01V1",
//                fwVersion = "1.0.1",
//                fwName = "FP-SNS-FLIGHT1",
//                cloudApps = emptyList(),
//                characteristics = emptyList(),
//                optionBytes = emptyList(),
//                fwDesc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempor posuere enim, et imperdiet quam mattis at.",
//                fota = FotaDetails(),
//            )
        )
    }
}

