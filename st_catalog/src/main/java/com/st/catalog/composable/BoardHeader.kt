/*
 * Copyright (c) 2022(-0001) STMicroelectronics.
 * All rights reserved.
 * This software is licensed under terms that can be found in the LICENSE file in
 * the root directory of this software component.
 * If no LICENSE file comes with this software, it is provided AS-IS.
 */
package com.st.catalog.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.sp
import com.st.blue_sdk.board_catalog.models.BoardDescription
import com.st.blue_sdk.board_catalog.models.BoardFirmware
import com.st.blue_sdk.board_catalog.models.BoardStatus
import com.st.blue_sdk.board_catalog.models.FirmwareMaturity
import com.st.blue_sdk.board_catalog.models.FotaDetails
import com.st.catalog.R
import com.st.ui.theme.ErrorText
import com.st.ui.theme.Grey6
import com.st.ui.theme.LocalDimensions
import com.st.ui.theme.PreviewBlueMSTheme
import com.st.ui.theme.SecondaryBlue
import com.st.ui.theme.Shapes
import com.st.ui.theme.SuccessText
import com.st.ui.utils.asString
import com.st.ui.utils.getBlueStBoardImages

@Composable
fun BoardHeader(
    modifier: Modifier = Modifier,
    board: BoardFirmware,
    boardDescOrNull: BoardDescription? = null,
    goToFw: () -> Unit = { /** NOOP **/ },
    goToDs: () -> Unit = { /** NOOP **/ }
) {
    //val openComponentDialog = remember { mutableStateOf(false) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = Shapes.small,
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
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            if (boardDescOrNull != null) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = LocalDimensions.current.paddingNormal),
                    text = "(${boardDescOrNull.friendlyName})",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
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
                        .align(alignment = Alignment.Center)
//                        .clickable {
//                            openComponentDialog.value = true
//                        },
                       ,
                    painter = painterResource(id = getBlueStBoardImages(boardType = board.boardModel().name)),
                    contentDescription = null
                )
            }

            if (boardDescOrNull != null) {
                when (boardDescOrNull.status) {
                    BoardStatus.ACTIVE -> Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        textAlign = TextAlign.Right,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        color = SuccessText,
                        text = boardDescOrNull.status.name
                    )

                    BoardStatus.NRND -> Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = LocalDimensions.current.paddingNormal),
                        textAlign = TextAlign.Right,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        letterSpacing = 0.25.sp,
                        color = ErrorText,
                        text = boardDescOrNull.status.name
                    )
                }
            }

            Divider()

            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    modifier = Modifier.weight(0.4f), onClick = goToFw
                ) {
                    Text(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp,
                        letterSpacing = 1.25.sp,
                        color = SecondaryBlue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = LocalDimensions.current.paddingNormal),
                        textAlign = TextAlign.Left,
                        text = stringResource(id = R.string.st_catalog_board_fwBtn).uppercase()
                    )
                }

                boardDescOrNull?.let {
                    if (boardDescOrNull.docURL != null) {
                        TextButton(
                            modifier = Modifier.weight(0.6f), onClick = goToDs
                        ) {
                            Text(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 16.sp,
                                letterSpacing = 1.25.sp,
                                color = SecondaryBlue,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = LocalDimensions.current.paddingNormal),
                                textAlign = TextAlign.Right,
                                text = stringResource(id = R.string.st_catalog_board_dsBtn).uppercase()
                            )
                        }
                    }
                }
            }
        }
    }

//    if (openComponentDialog.value) {
//        if (boardDescOrNull != null) {
//            if (!boardDescOrNull.components.isNullOrEmpty()) {
//                DialogBoardComponents(compList = boardDescOrNull.components!!,
//                    onDismissRequest = {
//                        openComponentDialog.value = false
//                    })
//            }
//        }
//    }
}

/** ----------------------- PREVIEW --------------------------------------- **/

@Preview(showBackground = true)
@Composable
private fun BoardHeaderNrndPreview() {
    PreviewBlueMSTheme {
        BoardHeader(
            board = BoardFirmware(
                bleDevId = "0x0A",
                bleFwId = "0x0F",
                brdName = "STEVAL-BCNKT01V1",
                fwVersion = "1.0.1",
                fwName = "FP-SNS-FLIGHT1",
                cloudApps = emptyList(),
                characteristics = emptyList(),
                optionBytes = emptyList(),
                fwDesc = LoremIpsum(words = 15).asString(),
                fota = FotaDetails(),
                maturity = FirmwareMaturity.RELEASE
            ), boardDescOrNull = BoardDescription(
                bleDevId = "123",
                usb_dev_id = "123",
                usbDevId = "123",
                uniqueDevId = 0,
                boardName = LoremIpsum(words = 5).asString(),
                boardVariant = LoremIpsum(words = 5).asString(),
                friendlyName = LoremIpsum(words = 5).asString(),
                status = BoardStatus.NRND,
                description = "",
                docURL = "www",
                orderURL = "www",
                videoURL = "www",
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BoardHeaderActivePreview() {
    PreviewBlueMSTheme {
        BoardHeader(
            board = BoardFirmware(
                bleDevId = "0x0A",
                bleFwId = "0x0F",
                brdName = "STEVAL-BCNKT01V1",
                fwVersion = "1.0.1",
                fwName = "FP-SNS-FLIGHT1",
                cloudApps = emptyList(),
                characteristics = emptyList(),
                optionBytes = emptyList(),
                fwDesc = LoremIpsum(words = 15).asString(),
                fota = FotaDetails(),
                maturity = FirmwareMaturity.RELEASE
            ), boardDescOrNull = BoardDescription(
                bleDevId = "123",
                usb_dev_id = "123",
                usbDevId = "123",
                uniqueDevId = 0,
                boardName = LoremIpsum(words = 5).asString(),
                boardVariant = LoremIpsum(words = 5).asString(),
                friendlyName = LoremIpsum(words = 5).asString(),
                status = BoardStatus.ACTIVE,
                description = "",
                docURL = "www",
                orderURL = "www",
                videoURL = "www",
            )
        )
    }
}
